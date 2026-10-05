<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Edit Guardian"/>
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
                    <form method="post" action="${pageContext.request.contextPath}/guardians/save">
                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                        <input type="hidden" name="id" value="${guardian.id}">
                        <div class="card"><div class="card-header">Guardian Details</div>
                            <div class="card-body row g-3">
                                <div class="col-md-6"><label class="form-label required-label">Full name</label>
                                    <input class="form-control" name="fullName" value="${guardian.fullName}" required></div>
                                <div class="col-md-6"><label class="form-label">NIC</label>
                                    <input class="form-control" name="nic" value="${guardian.nic}"></div>
                                <div class="col-md-6"><label class="form-label required-label">Phone</label>
                                    <input class="form-control" name="phone" value="${guardian.phone}" required></div>
                                <div class="col-md-6"><label class="form-label">Email</label>
                                    <input type="email" class="form-control" name="email" value="${guardian.email}"></div>
                                <div class="col-md-6"><label class="form-label">Occupation</label>
                                    <input class="form-control" name="occupation" value="${guardian.occupation}"></div>
                                <div class="col-md-6"><label class="form-label">Relationship</label>
                                    <select class="form-select" name="relationship">
                                        <option value="Father" ${guardian.relationship == 'Father' ? 'selected' : ''}>Father</option>
                                        <option value="Mother" ${guardian.relationship == 'Mother' ? 'selected' : ''}>Mother</option>
                                        <option value="Legal Guardian" ${guardian.relationship == 'Legal Guardian' ? 'selected' : ''}>Legal Guardian</option>
                                    </select></div>
                                <div class="col-12"><label class="form-label">Address</label>
                                    <input class="form-control" name="address" value="${guardian.address}"></div>
                            </div>
                        </div>
                        <button class="btn btn-primary mt-3">Save Guardian</button>
                    </form>
                </div>
            </div>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/edutrack.js"></script>
</body>
</html>
