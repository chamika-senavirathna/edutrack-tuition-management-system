<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="${teacher != null ? 'Edit Teacher' : 'Register Teacher'}"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <form method="post" action="${pageContext.request.contextPath}${teacher != null ? '/teachers/edit' : '/teachers/add'}">
                <input type="hidden" name="csrfToken" value="${csrfToken}">
                <c:if test="${teacher != null}"><input type="hidden" name="id" value="${teacher.id}"></c:if>
                <div class="row g-4">
                    <div class="col-lg-8">
                        <div class="card"><div class="card-header">Teacher Details</div>
                            <div class="card-body row g-3">
                                <div class="col-md-6"><label class="form-label required-label">Full name</label>
                                    <input class="form-control" name="fullName" value="${teacher.fullName}" required></div>
                                <div class="col-md-3"><label class="form-label">NIC</label>
                                    <input class="form-control" name="nic" value="${teacher.nic}"></div>
                                <div class="col-md-3"><label class="form-label">Gender</label>
                                    <select class="form-select" name="gender">
                                        <option value="MALE" ${teacher.gender == 'MALE' ? 'selected' : ''}>Male</option>
                                        <option value="FEMALE" ${teacher.gender == 'FEMALE' ? 'selected' : ''}>Female</option>
                                        <option value="OTHER" ${teacher.gender == 'OTHER' ? 'selected' : ''}>Other</option>
                                    </select></div>
                                <div class="col-md-6"><label class="form-label">Email</label>
                                    <input type="email" class="form-control" name="email" value="${teacher.email}"></div>
                                <div class="col-md-6"><label class="form-label">Phone</label>
                                    <input class="form-control" name="phone" value="${teacher.phone}"></div>
                                <div class="col-md-6"><label class="form-label">Qualifications</label>
                                    <input class="form-control" name="qualification" value="${teacher.qualification}"></div>
                                <div class="col-md-6"><label class="form-label">Specialization</label>
                                    <input class="form-control" name="specialization" value="${teacher.specialization}"></div>
                                <div class="col-md-6"><label class="form-label">Address</label>
                                    <input class="form-control" name="address" value="${teacher.address}"></div>
                                <div class="col-md-6"><label class="form-label">Joined date</label>
                                    <input type="date" class="form-control" name="joinedDate" value="${teacher.joinedDate}"></div>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg-4">
                        <div class="card mb-3"><div class="card-header">Lifecycle</div>
                            <div class="card-body small text-muted">
                                Staff numbers are auto-generated (BR-REG-01) and duplicate NICs are blocked (BR-REG-02).
                                Resignation archives the record with history (safe lifecycle).
                            </div>
                        </div>
                        <button class="btn btn-primary w-100">${teacher != null ? 'Save Changes' : 'Register Teacher'}</button>
                    </div>
                </div>
            </form>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/edutrack.js"></script>
</body>
</html>
