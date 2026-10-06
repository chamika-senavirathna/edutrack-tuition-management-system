<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Fees"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <p class="text-muted mb-0">Member 04 · Fee / Payment Management · receipts follow verified payments only (BR-FEE-01)</p>
                <c:if test="${user.hasRole('ADMIN') || user.hasRole('FINANCE')}">
                    <a class="btn btn-primary" href="${pageContext.request.contextPath}/fees/add">+ New Invoice</a>
                </c:if>
            </div>

            <form class="card card-body mb-3 row g-2 flex-md-row align-items-md-end" method="get">
                <div class="col-md-4"><label class="form-label small">Invoice no.</label><input class="form-control" name="q" value="${q}"></div>
                <div class="col-md-3"><label class="form-label small">Student ID</label><input class="form-control" name="studentId" value="${studentId}"></div>
                <div class="col-md-3"><label class="form-label small">Status</label>
                    <select class="form-select" name="status"><option value="">All</option>
                        <option ${status == 'PENDING' ? 'selected' : ''}>PENDING</option>
                        <option ${status == 'PART_PAID' ? 'selected' : ''}>PART_PAID</option>
                        <option ${status == 'PAID' ? 'selected' : ''}>PAID</option>
                        <option ${status == 'VOID' ? 'selected' : ''}>VOID</option>
                    </select></div>
                <div class="col-md-2"><button class="btn btn-outline-primary w-100">Filter</button></div>
            </form>

            <div class="card">
                <table class="table table-hover mb-0">
                    <thead><tr><th>Invoice</th><th>Student</th><th>Term</th><th>Amount</th><th>Balance</th><th>Due</th><th>Status</th><th></th></tr></thead>
                    <tbody>
                    <c:if test="${empty invoices}">
                        <tr><td colspan="8"><div class="empty-state"><div class="icon">🧾</div>No invoices found.</div></td></tr>
                    </c:if>
                    <c:forEach items="${invoices}" var="f">
                        <tr>
                            <td class="fw-semibold">${f.invoiceNo}</td>
                            <td>${f.studentName} <small class="text-muted">${f.studentRegNo}</small></td>
                            <td>${f.termName}</td>
                            <td>LKR ${f.amount}</td>
                            <td>LKR ${f.balance}</td>
                            <td>${f.dueDate}</td>
                            <td><span class="badge badge-${f.status}">${f.status}</span></td>
                            <td class="text-end">
                                <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/fees/view?id=${f.id}">View</a>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
            <p class="text-muted small mt-2">${total} invoice(s)</p>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/edutrack.js"></script>
</body>
</html>
