<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Receipt · ${receipt.receiptNo}"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <div class="row justify-content-center">
                <div class="col-lg-6">
                    <div class="card">
                        <div class="card-header text-center fw-bold">EduTrack Sri Lanka · Payment Receipt</div>
                        <div class="card-body">
                            <div class="text-center mb-3">
                                <div class="h5 fw-bold">${receipt.receiptNo}</div>
                                <div class="text-muted small">Issued ${receipt.issuedAt} by ${receipt.issuedByName}</div>
                            </div>
                            <table class="table">
                                <tr><th>Student</th><td>${receipt.studentName} (${receipt.studentRegNo})</td></tr>
                                <tr><th>Invoice</th><td>${receipt.invoiceNo}</td></tr>
                                <tr><th>Transaction</th><td class="text-break">${receipt.transactionId}</td></tr>
                                <tr><th>Amount paid</th><td class="fw-bold">LKR ${receipt.amount}</td></tr>
                            </table>
                            <p class="small text-muted text-center mb-0">
                                This receipt was generated only after the payment was verified (BR-FEE-01).
                            </p>
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
