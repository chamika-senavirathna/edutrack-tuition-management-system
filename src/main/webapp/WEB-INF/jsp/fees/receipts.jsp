<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Receipts"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <p class="text-muted">Member 04 · Receipts exist only for verified payments (BR-FEE-01)</p>
            <form class="card card-body mb-3 row g-2 flex-md-row align-items-md-end" method="get">
                <div class="col-md-4"><label class="form-label small">Receipt no.</label><input class="form-control" name="q" value="${q}"></div>
                <div class="col-md-2"><button class="btn btn-outline-primary w-100">Search</button></div>
            </form>
            <div class="card">
                <table class="table mb-0">
                    <thead><tr><th>Receipt</th><th>Student</th><th>Invoice</th><th>Amount</th><th>Issued by</th><th></th></tr></thead>
                    <tbody>
                    <c:forEach items="${receipts}" var="r">
                        <tr>
                            <td class="fw-semibold">${r.receiptNo}</td>
                            <td>${r.studentName}</td>
                            <td>${r.invoiceNo}</td>
                            <td>LKR ${r.amount}</td>
                            <td>${r.issuedByName}</td>
                            <td class="text-end"><a class="btn btn-sm btn-outline-primary"
                                href="${pageContext.request.contextPath}/receipts/view?id=${r.id}">View</a></td>
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
