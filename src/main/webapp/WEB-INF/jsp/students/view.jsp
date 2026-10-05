<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Student · ${student.regNo}"/>
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
                            <span>Profile · ${student.regNo}</span>
                            <span class="badge badge-${student.status}">${student.status}</span>
                        </div>
                        <div class="card-body row g-3">
                            <div class="col-md-6"><div class="small text-muted">Full name</div><div class="fw-semibold">${student.fullName()}</div></div>
                            <div class="col-md-6"><div class="small text-muted">Name with initials</div>${student.nameWithInitials}</div>
                            <div class="col-md-4"><div class="small text-muted">NIC / BC</div>${student.nic}</div>
                            <div class="col-md-4"><div class="small text-muted">Gender</div>${student.gender}</div>
                            <div class="col-md-4"><div class="small text-muted">Date of birth</div>${student.dob}</div>
                            <div class="col-md-6"><div class="small text-muted">Phone</div>${student.phone}</div>
                            <div class="col-md-6"><div class="small text-muted">Email</div>${student.email}</div>
                            <div class="col-md-6"><div class="small text-muted">Guardian</div>${student.guardianName} ${empty student.guardianPhone ? '' : student.guardianPhone}</div>
                            <div class="col-md-6"><div class="small text-muted">Admitted</div>${student.admissionDate}</div>
                            <div class="col-12"><div class="small text-muted">Address</div>${student.address}</div>
                        </div>
                    </div>

                    <div class="card">
                        <div class="card-header">Class Enrolments</div>
                        <div class="card-body p-0">
                            <c:if test="${empty enrolments}">
                                <div class="empty-state"><div class="icon">🏫</div>Not enrolled in any class yet.</div>
                            </c:if>
                            <table class="table mb-0">
                                <thead><tr><th>Class</th><th>Subject</th><th>Enrolled</th><th>Status</th></tr></thead>
                                <tbody>
                                <c:forEach items="${enrolments}" var="e">
                                    <tr>
                                        <td>${e.className}</td><td>${e.subjectName}</td><td>${e.enrolledDate}</td>
                                        <td><span class="badge badge-${e.status}">${e.status}</span></td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <div class="col-lg-5">
                    <c:if test="${user.hasRole('ADMIN') || user.hasRole('ACADEMIC_COORDINATOR')}">
                        <div class="card mb-4">
                            <div class="card-header">Actions</div>
                            <div class="card-body">
                                <a class="btn btn-outline-primary w-100 mb-2" href="${pageContext.request.contextPath}/students/edit?id=${student.id}">Edit Profile</a>

                                <form method="post" action="${pageContext.request.contextPath}/students/enrol" class="mb-2"
                                      data-confirm="Enrol this student into the selected class?">
                                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                                    <input type="hidden" name="id" value="${student.id}">
                                    <label class="form-label small">Enrol into class</label>
                                    <div class="input-group">
                                        <select class="form-select" name="classId">
                                            <c:forEach items="${classes}" var="c"><option value="${c.id}">${c.name} · ${c.enrolledCount}/${c.capacity}</option></c:forEach>
                                        </select>
                                        <button class="btn btn-outline-success" type="submit">Enrol</button>
                                    </div>
                                    <div class="form-check mt-1">
                                        <input class="form-check-input" type="checkbox" name="approveReenrolment" id="ar2">
                                        <label class="form-check-label small" for="ar2">Admin-approve re-enrolment of withdrawn student</label>
                                    </div>
                                </form>

                                <c:if test="${student.status != 'WITHDRAWN'}">
                                    <form method="post" action="${pageContext.request.contextPath}/students/withdraw"
                                          data-confirm="Withdraw this student? History is retained and re-enrolment will need admin approval.">
                                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                                        <input type="hidden" name="id" value="${student.id}">
                                        <label class="form-label small">Withdrawal reason</label>
                                        <input class="form-control mb-2" name="reason" required placeholder="e.g. Relocated abroad">
                                        <button class="btn btn-outline-danger w-100" type="submit">Withdraw Student</button>
                                    </form>
                                </c:if>
                            </div>
                        </div>
                    </c:if>

                    <div class="card">
                        <div class="card-header">Quick Links</div>
                        <div class="card-body d-grid gap-2">
                            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/attendance?studentId=${student.id}">Attendance records (M03)</a>
                            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/fees?studentId=${student.id}">Fee records (M04)</a>
                            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/results?studentId=${student.id}">Results (M06)</a>
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
