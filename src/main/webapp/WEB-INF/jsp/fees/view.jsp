<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Invoice · ${invoice.invoiceNo}"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <div class="row g-4">
                <div class="col-lg-7">
                    <div class="card mb-4">
                        <div class="card-header d-flex justify-content-between align-items-center">
                            <span>${invoice.invoiceNo}
                                <c:if test="${invoice.attendanceLinked}">
                                    <span class="badge bg-info text-dark" title="Auto-validated from attendance marking (M04&harr;M03)">attendance-linked</span>
                                </c:if>
                            </span>
                            <span class="badge badge-${invoice.status}">${invoice.status}</span>
                        </div>
                        <div class="card-body row g-3">
                            <div class="col-md-6"><div class="small text-muted">Student</div>${invoice.studentName} (${invoice.studentRegNo})</div>
                            <div class="col-md-6"><div class="small text-muted">Class</div>${invoice.className}</div>
                            <div class="col-md-4"><div class="small text-muted">Amount</div>LKR ${invoice.amount}</div>
                            <div class="col-md-4"><div class="small text-muted">Paid</div>LKR ${invoice.amountPaid}</div>
                            <div class="col-md-4"><div class="small text-muted">Balance</div><strong>LKR ${invoice.balance}</strong></div>
                            <div class="col-md-6"><div class="small text-muted">Issued</div>${invoice.issuedDate}</div>
                            <div class="col-md-6"><div class="small text-muted">Due</div>${invoice.dueDate}</div>
                            <div class="col-md-6"><div class="small text-muted">Attendance-linked sessions</div>${invoice.presentCount}</div>
                            <div class="col-md-6"><div class="small text-muted">Last attendance date</div>${invoice.lastAttendanceDate}</div>
                        </div>
                    </div>
                    <div class="card">
                        <div class="card-header">Payment Attempts</div>
                        <table class="table mb-0">
                            <thead><tr><th>Transaction</th><th>Amount</th><th>Method</th><th>Status</th><th>Receipt</th></tr></thead>
                            <tbody>
                            <c:forEach items="${payments}" var="p">
                                <tr>
                                    <td class="text-break">${p.transactionId}</td>
                                    <td>LKR ${p.amount}</td>
                                    <td>${p.method}</td>
                                    <td><span class="badge badge-${p.status}">${p.status}</span></td>
                                    <td>${p.receiptNo}</td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
                <div class="col-lg-5">
                    <div class="card">
                        <div class="card-header">Actions (Finance)</div>
                        <div class="card-body">
                            <a class="btn btn-outline-primary w-100 mb-2" href="${pageContext.request.contextPath}/payments/add?invoiceId=${invoice.id}">Record Payment Attempt</a>
                            <c:if test="${invoice.status != 'VOID' && invoice.status != 'PAID'}">
                                <form method="post" action="${pageContext.request.contextPath}/fees/void"
                                      data-confirm="Void this invoice? History is retained (no deletion).">
                                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                                    <input type="hidden" name="id" value="${invoice.id}">
                                    <label class="form-label small">Void reason</label>
                                    <input class="form-control mb-2" name="reason" required>
                                    <button class="btn btn-outline-danger w-100">Void Invoice</button>
                                </form>
                            </c:if>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/edutrack.js"></script>
</body>
</html>
