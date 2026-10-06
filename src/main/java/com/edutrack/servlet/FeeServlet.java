package com.edutrack.servlet;

import com.edutrack.model.FeeInvoice;
import com.edutrack.model.Payment;
import com.edutrack.model.Receipt;
import com.edutrack.model.User;
import com.edutrack.service.FeeService;
import com.edutrack.service.RegistrationService;
import com.edutrack.service.TermService;
import com.edutrack.service.TimetableService;
import com.edutrack.util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Fee / Payment controller (Member 04). Invoices, payment attempts with
 * duplicate-ID flagging, gateway-simulated verification, receipts and refunds.
 */
@WebServlet({"/fees", "/fees/add", "/fees/edit", "/fees/void", "/fees/view",
        "/payments", "/payments/add", "/payments/verify", "/payments/refund",
        "/receipts", "/receipts/view"})
public class FeeServlet extends BaseServlet {

    private final FeeService feeService = new FeeService();
    private final TimetableService timetableService = new TimetableService();
    private final TermService termService = new TermService();
    private final RegistrationService registrationService = new RegistrationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        String path = request.getServletPath();
        switch (path) {
            case "/fees" -> {
                int page = Math.max(1, intParam(request, "page", 1));
                int pageSize = 10;
                String q = stringParam(request, "q");
                String status = stringParam(request, "status");
                Long studentId = resolveSelfOrChildStudentId(user, request);
                request.setAttribute("invoices", feeService.searchInvoices(q, studentId, null, null, status,
                        (page - 1) * pageSize, pageSize));
                request.setAttribute("total", feeService.countInvoices(q, studentId, null, null, status));
                request.setAttribute("page", page);
                request.setAttribute("pageSize", pageSize);
                request.setAttribute("terms", termService.allTerms());
                request.setAttribute("status", status);
                request.setAttribute("q", q);
                view(request, response, "fees/list.jsp");
            }
            case "/fees/add" -> {
                request.setAttribute("students", registrationService.searchStudents(null, null, "ACTIVE", 0, 500));
                request.setAttribute("classes", timetableService.searchClasses(null, null, null, "ACTIVE", 0, 100));
                request.setAttribute("terms", termService.allTerms());
                view(request, response, "fees/form.jsp");
            }
            case "/fees/edit" -> {
                request.setAttribute("invoice", feeService.findInvoice(longParam(request, "id", -1)));
                request.setAttribute("terms", termService.allTerms());
                view(request, response, "fees/form.jsp");
            }
            case "/fees/view" -> {
                FeeInvoice invoice = feeService.findInvoice(longParam(request, "id", -1));
                if (invoice == null) {
                    redirect(request, response, "/fees");
                    return;
                }
                // Record-level ownership: students/parents may only open their own invoices.
                java.util.Set<Long> visible = visibleStudentIds(user);
                if (visible != null && !visible.contains(invoice.getStudentId())) {
                    redirect(request, response, "/dashboard?denied=fees");
                    return;
                }
                request.setAttribute("invoice", invoice);
                request.setAttribute("payments", feeService.searchPayments(null, invoice.getId(), null, null, null, 0, 50));
                view(request, response, "fees/view.jsp");
            }
            case "/payments" -> {
                String q = stringParam(request, "q");
                String status = stringParam(request, "status");
                String method = stringParam(request, "method");
                Long studentId = resolveSelfOrChildStudentId(user, request);
                request.setAttribute("payments", feeService.searchPayments(q, null, studentId, status, method, 0, 50));
                request.setAttribute("q", q);
                request.setAttribute("status", status);
                request.setAttribute("method", method);
                view(request, response, "fees/payments.jsp");
            }
            case "/payments/add" -> {
                request.setAttribute("invoice", feeService.findInvoice(longParam(request, "invoiceId", -1)));
                view(request, response, "fees/payment_form.jsp");
            }
            case "/receipts" -> {
                String q = stringParam(request, "q");
                Long studentId = resolveSelfOrChildStudentId(user, request);
                request.setAttribute("receipts", feeService.searchReceipts(q, studentId, 0, 50));
                request.setAttribute("q", q);
                view(request, response, "fees/receipts.jsp");
            }
            case "/receipts/view" -> {
                Receipt receipt = feeService.findReceipt(longParam(request, "id", -1));
                java.util.Set<Long> visible = visibleStudentIds(user);
                if (visible != null) {
                    boolean owned = false;
                    if (receipt != null) {
                        Payment payment = feeService.findPayment(receipt.getPaymentId());
                        if (payment != null) {
                            FeeInvoice invoice = feeService.findInvoice(payment.getInvoiceId());
                            owned = invoice != null && visible.contains(invoice.getStudentId());
                        }
                    }
                    if (!owned) {
                        redirect(request, response, "/dashboard?denied=receipts");
                        return;
                    }
                }
                request.setAttribute("receipt", receipt);
                view(request, response, "fees/receipt_view.jsp");
            }
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = currentUser(request);
        String path = request.getServletPath();
        try {
            switch (path) {
                case "/fees/add" -> {
                    FeeInvoice invoice = new FeeInvoice();
                    invoice.setStudentId(longParam(request, "studentId", -1));
                    invoice.setClassId(longParam(request, "classId", -1) == -1 ? null : longParam(request, "classId", -1));
                    invoice.setTermId(longParam(request, "termId", -1) == -1 ? null : longParam(request, "termId", -1));
                    String amount = stringParam(request, "amount");
                    invoice.setAmount(amount == null || amount.isBlank() ? null : new BigDecimal(amount));
                    String discount = stringParam(request, "discount");
                    invoice.setDiscount(discount == null || discount.isBlank() ? BigDecimal.ZERO : new BigDecimal(discount));
                    String due = stringParam(request, "dueDate");
                    invoice.setDueDate(due == null || due.isBlank() ? null : LocalDate.parse(due));
                    invoice.setRemarks(stringParam(request, "remarks"));
                    FeeInvoice saved = feeService.createInvoice(user, invoice);
                    Flash.success(request.getSession(), "Invoice " + saved.getInvoiceNo() + " created.");
                    redirect(request, response, "/fees/view?id=" + saved.getId());
                }
                case "/fees/edit" -> {
                    FeeInvoice changes = new FeeInvoice();
                    changes.setId(longParam(request, "id", -1));
                    String amount = stringParam(request, "amount");
                    changes.setAmount(amount == null || amount.isBlank() ? null : new BigDecimal(amount));
                    String discount = stringParam(request, "discount");
                    changes.setDiscount(discount == null || discount.isBlank() ? BigDecimal.ZERO : new BigDecimal(discount));
                    String due = stringParam(request, "dueDate");
                    changes.setDueDate(due == null || due.isBlank() ? null : LocalDate.parse(due));
                    changes.setRemarks(stringParam(request, "remarks"));
                    feeService.updateInvoice(user, changes);
                    Flash.success(request.getSession(), "Invoice updated.");
                    redirect(request, response, "/fees/view?id=" + changes.getId());
                }
                case "/fees/void" -> {
                    feeService.voidInvoice(user, longParam(request, "id", -1), stringParam(request, "reason"));
                    Flash.success(request.getSession(), "Invoice voided. History retained.");
                    redirect(request, response, "/fees");
                }
                case "/payments/add" -> {
                    long invoiceId = longParam(request, "invoiceId", -1);
                    String amount = stringParam(request, "amount");
                    Payment payment = feeService.recordPayment(user, invoiceId, stringParam(request, "transactionId"),
                            amount == null || amount.isBlank() ? null : new BigDecimal(amount),
                            stringParam(request, "method"), stringParam(request, "notes"));
                    Flash.success(request.getSession(), "Payment attempt recorded. Verify it to issue the receipt.");
                    redirect(request, response, "/payments");
                }
                case "/payments/verify" -> {
                    long id = longParam(request, "id", -1);
                    boolean approved = "approve".equals(stringParam(request, "decision"));
                    feeService.verifyPayment(user, id, approved);
                    Flash.success(request.getSession(), approved
                            ? "Payment verified and receipt issued."
                            : "Payment marked FAILED and routed for staff review.");
                    redirect(request, response, "/payments");
                }
                case "/payments/refund" -> {
                    feeService.refundPayment(user, longParam(request, "id", -1), stringParam(request, "reason"));
                    Flash.success(request.getSession(), "Refund recorded with reason. History retained.");
                    redirect(request, response, "/payments");
                }
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (RuntimeException e) {
            handle(request, response, "/fees", e);
        }
    }
}
