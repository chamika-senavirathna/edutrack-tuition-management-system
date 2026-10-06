<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Payments"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <p class="text-muted">Member 04 · Payment verification gateway-simulated; duplicate transaction IDs are flagged (BR-FEE-02)</p>

            <form class="card card-body mb-3 row g-2 flex-md-row align-items-md-end" method="get">
                <div class="col-md-3"><label class="form-label small">Transaction ID</label><input class="form-control" name="q" value="${q}"></div>
                <div class="col-md-3"><label class="form-label small">Status</label>
                    <select class="form-select" name="status"><option value="">All</option>
                        <option ${status == 'PENDING' ? 'selected' : ''}>PENDING</option>
                        <option ${status == 'VERIFIED' ? 'selected' : ''}>VERIFIED</option>
                        <option ${status == 'FAILED' ? 'selected' : ''}>FAILED</option>
                        <option ${status == 'REFUNDED' ? 'selected' : ''}>REFUNDED</option>
                    </select></div>
                <div class="col-md-3"><label class="form-label small">Method</label>
                    <select class="form-select" name="method"><option value="">All</option>
                        <option ${method == 'CASH' ? 'selected' : ''}>CASH</option>
                        <option ${method == 'BANK_TRANSFER' ? 'selected' : ''}>BANK_TRANSFER</option>
                        <option ${method == 'CARD' ? 'selected' : ''}>CARD</option>
                        <option ${method == 'ONLINE' ? 'selected' : ''}>ONLINE</option>
                    </select></div>
                <div class="col-md-2"><button class="btn btn-outline-primary w-100">Filter</button></div>
            </form>

            <div class="card">
                <table class="table table-hover mb-0 align-middle">
                    <thead><tr><th>Transaction</th><th>Student</th><th>Invoice</th><th>Amount</th><th>Method</th><th>Status</th><th class="text-end">Actions</th></tr></thead>
                    <tbody>
                    <c:if test="${empty payments}">
                        <tr><td colspan="7"><div class="empty-state"><div class="icon">💳</div>No payments found.</div></td></tr>
                    </c:if>
                    <c:forEach items="${payments}" var="p">
                        <tr>
                            <td class="text-break small">${p.transactionId}</td>
                            <td>${p.studentName}</td>
                            <td>${p.invoiceNo}</td>
                            <td>LKR ${p.amount}</td>
                            <td>${p.method}</td>
                            <td><span class="badge badge-${p.status}">${p.status}</span>
                                ${p.receiptNo != null ? '<span class="badge bg-dark">RCP</span>' : ''}</td>
                            <td class="text-end">
                                <c:if test="${p.status == 'PENDING' && (user.hasRole('FINANCE') || user.hasRole('ADMIN'))}">
                                    <form class="d-inline" method="post" action="${pageContext.request.contextPath}/payments/verify">
                                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                                        <input type="hidden" name="id" value="${p.id}"><input type="hidden" name="decision" value="approve">
                                        <button class="btn btn-sm btn-success">Verify &amp; Receipt</button>
                                    </form>
                                    <form class="d-inline" method="post" action="${pageContext.request.contextPath}/payments/verify">
                                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                                        <input type="hidden" name="id" value="${p.id}"><input type="hidden" name="decision" value="reject">
                                        <button class="btn btn-sm btn-outline-danger">Reject</button>
                                    </form>
                                </c:if>
                                <c:if test="${(p.status == 'VERIFIED') && (user.hasRole('FINANCE') || user.hasRole('ADMIN'))}">
                                    <form class="d-inline" method="post" action="${pageContext.request.contextPath}/payments/refund"
                                          data-confirm="Record a reviewed refund? A reason is required and the action is audited.">
                                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                                        <input type="hidden" name="id" value="${p.id}">
                                        <input class="form-control form-control-sm d-inline w-auto me-1" name="reason" placeholder="Refund reason" required>
                                        <button class="btn btn-sm btn-outline-warning">Refund</button>
                                    </form>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/edutrack.js"></script>
</body>
</html>
