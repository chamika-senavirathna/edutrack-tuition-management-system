<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Record Payment"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <div class="row">
                <div class="col-lg-7">
                    <form method="post" action="${pageContext.request.contextPath}/payments/add">
                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                        <input type="hidden" name="invoiceId" value="${invoice.id}">
                        <div class="card"><div class="card-header">Payment Attempt · ${invoice.invoiceNo}</div>
                            <div class="card-body row g-3">
                                <div class="col-12 small text-muted">
                                    ${invoice.studentName} · balance <strong>LKR ${invoice.balance}</strong>.
                                    The receipt is issued only after verification (BR-FEE-01).
                                </div>
                                <div class="col-md-6"><label class="form-label required-label">Transaction reference</label>
                                    <input class="form-control" name="transactionId" required placeholder="e.g. TXN-20260906-001">
                                    <div class="form-text">Duplicate IDs are blocked and flagged for review (BR-FEE-02).</div></div>
                                <div class="col-md-6"><label class="form-label required-label">Amount (LKR)</label>
                                    <input type="number" step="0.01" min="0.01" class="form-control" name="amount" required
                                           value="${invoice.balance}"></div>
                                <div class="col-md-6"><label class="form-label required-label">Method</label>
                                    <select class="form-select" name="method">
                                        <option value="CASH">Cash</option>
                                        <option value="BANK_TRANSFER">Bank transfer</option>
                                        <option value="CARD">Card</option>
                                        <option value="ONLINE">Online</option>
                                    </select></div>
                                <div class="col-md-6"><label class="form-label">Notes</label>
                                    <input class="form-control" name="notes"></div>
                            </div>
                        </div>
                        <button class="btn btn-primary mt-3">Record Payment</button>
                    </form>
                </div>
            </div>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/edutrack.js"></script>
</body>
</html>
