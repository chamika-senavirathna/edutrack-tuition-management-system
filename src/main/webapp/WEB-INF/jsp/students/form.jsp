<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="${student != null ? 'Edit Student' : 'Register Student'}"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <form method="post"
                  action="${pageContext.request.contextPath}${student != null ? '/students/edit' : '/students/add'}">
                <input type="hidden" name="csrfToken" value="${csrfToken}">
                <c:if test="${student != null}"><input type="hidden" name="id" value="${student.id}"></c:if>

                <div class="row g-4">
                    <div class="col-lg-7">
                        <div class="card mb-4">
                            <div class="card-header">Student Details</div>
                            <div class="card-body row g-3">
                                <div class="col-md-6">
                                    <label class="form-label required-label">First name</label>
                                    <input class="form-control" name="firstName" value="${student.firstName}" required>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label required-label">Last name</label>
                                    <input class="form-control" name="lastName" value="${student.lastName}" required>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Name with initials</label>
                                    <input class="form-control" name="nameWithInitials" value="${student.nameWithInitials}">
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">NIC / birth certificate</label>
                                    <input class="form-control" name="nic" value="${student.nic}" placeholder="950123456V or 199501234567">
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label required-label">Gender</label>
                                    <select class="form-select" name="gender" required>
                                        <option value="" ${empty student.gender ? 'selected' : ''}>Select…</option>
                                        <option value="MALE" ${student.gender == 'MALE' ? 'selected' : ''}>Male</option>
                                        <option value="FEMALE" ${student.gender == 'FEMALE' ? 'selected' : ''}>Female</option>
                                        <option value="OTHER" ${student.gender == 'OTHER' ? 'selected' : ''}>Other</option>
                                    </select>
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label">Date of birth</label>
                                    <input type="date" class="form-control" name="dob" value="${student.dob}">
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label">Phone</label>
                                    <input class="form-control" name="phone" value="${student.phone}" placeholder="0771234567">
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Email</label>
                                    <input type="email" class="form-control" name="email" value="${student.email}">
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label">Address</label>
                                    <input class="form-control" name="address" value="${student.address}">
                                </div>
                                <div class="col-12">
                                    <label class="form-label">Medical notes (confidential)</label>
                                    <textarea class="form-control" name="medicalNotes" rows="2">${student.medicalNotes}</textarea>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-lg-5">
                        <c:if test="${student == null}">
                            <div class="card mb-4">
                                <div class="card-header">Guardian Link</div>
                                <div class="card-body row g-3">
                                    <div class="col-12 small text-muted">Leave blank to register without a guardian. Duplicate guardians are matched by NIC.</div>
                                    <div class="col-md-6">
                                        <label class="form-label">Guardian name</label>
                                        <input class="form-control" name="guardianName" value="${param.guardianName}">
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label">Guardian NIC</label>
                                        <input class="form-control" name="guardianNic" value="${param.guardianNic}">
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label">Guardian phone</label>
                                        <input class="form-control" name="guardianPhone" value="${param.guardianPhone}">
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label">Relationship</label>
                                        <select class="form-select" name="guardianRelationship">
                                            <option value="Father">Father</option>
                                            <option value="Mother">Mother</option>
                                            <option value="Legal Guardian">Legal Guardian</option>
                                        </select>
                                    </div>
                                    <div class="col-12">
                                        <label class="form-label">Guardian address</label>
                                        <input class="form-control" name="guardianAddress">
                                    </div>
                                </div>
                            </div>

                            <div class="card mb-4">
                                <div class="card-header">Class Enrolment (optional)</div>
                                <div class="card-body">
                                    <label class="form-label">Class · capacity enforced (BR-REG-03)</label>
                                    <select class="form-select" name="classId">
                                        <option value="-1">Enrol later</option>
                                        <c:forEach items="${classes}" var="c">
                                            <option value="${c.id}">${c.name} · ${c.enrolledCount}/${c.capacity}</option>
                                        </c:forEach>
                                    </select>
                                    <div class="form-check mt-2">
                                        <input class="form-check-input" type="checkbox" name="approveReenrolment" id="approveReenrolment">
                                        <label class="form-check-label small" for="approveReenrolment">
                                            Admin approval for re-enrolling a withdrawn student
                                        </label>
                                    </div>
                                </div>
                            </div>
                        </c:if>

                        <c:if test="${student != null}">
                            <div class="card mb-4">
                                <div class="card-header">Guardian / Status</div>
                                <div class="card-body row g-3">
                                    <div class="col-12">
                                        <label class="form-label">Guardian</label>
                                        <select class="form-select" name="guardianId">
                                            <option value="-1">None</option>
                                            <c:forEach items="${guardians}" var="g">
                                                <option value="${g.id}" ${student.guardianId == g.id ? 'selected' : ''}>${g.fullName} (${g.phone})</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                    <div class="col-12">
                                        <label class="form-label">Status</label>
                                        <input class="form-control" value="${student.status}" disabled>
                                        <div class="form-text">Withdrawals are performed from the profile page with a reason (history kept).</div>
                                    </div>
                                </div>
                            </div>
                        </c:if>

                        <button type="submit" class="btn btn-primary w-100 mb-4">
                            ${student != null ? 'Save Changes' : 'Register Student'}
                        </button>
                    </div>
                </div>
            </form>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/edutrack.js"></script>
</body>
</html>
