<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Teacher · ${teacher.staffNo}"/>
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
                            <span>Profile · ${teacher.staffNo}</span>
                            <span class="badge badge-${teacher.status}">${teacher.status}</span>
                        </div>
                        <div class="card-body row g-3">
                            <div class="col-md-6"><div class="small text-muted">Name</div><div class="fw-semibold">${teacher.fullName}</div></div>
                            <div class="col-md-6"><div class="small text-muted">NIC</div>${teacher.nic}</div>
                            <div class="col-md-6"><div class="small text-muted">Email</div>${teacher.email}</div>
                            <div class="col-md-6"><div class="small text-muted">Phone</div>${teacher.phone}</div>
                            <div class="col-md-6"><div class="small text-muted">Qualifications</div>${teacher.qualification}</div>
                            <div class="col-md-6"><div class="small text-muted">Specialization</div>${teacher.specialization}</div>
                            <div class="col-md-6"><div class="small text-muted">Joined</div>${teacher.joinedDate}</div>
                            <c:if test="${not empty teacher.resignedReason}">
                                <div class="col-12"><div class="small text-muted">Status reason</div>${teacher.resignedReason}</div>
                            </c:if>
                        </div>
                    </div>
                    <div class="card">
                        <div class="card-header">Assigned Classes · M02</div>
                        <table class="table mb-0">
                            <thead><tr><th>Class</th><th>Subject</th><th>Status</th><th>Enrolled</th></tr></thead>
                            <tbody>
                            <c:forEach items="${classes}" var="c">
                                <tr><td>${c.name}</td><td>${c.subjectName}</td>
                                    <td><span class="badge badge-${c.status}">${c.status}</span></td>
                                    <td>${c.enrolledCount}/${c.capacity}</td></tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
                <div class="col-lg-5">
                    <div class="card">
                        <div class="card-header">Actions</div>
                        <div class="card-body">
                            <a class="btn btn-outline-primary w-100 mb-3" href="${pageContext.request.contextPath}/teachers/edit?id=${teacher.id}">Edit Profile</a>
                            <c:if test="${teacher.status == 'ACTIVE' && (user.hasRole('ADMIN') || user.hasRole('ACADEMIC_COORDINATOR'))}">
                                <form method="post" action="${pageContext.request.contextPath}/teachers/resign"
                                      data-confirm="Mark this teacher as RESIGNED? History is retained.">
                                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                                    <input type="hidden" name="id" value="${teacher.id}">
                                    <label class="form-label small">Reason</label>
                                    <input class="form-control mb-2" name="reason" required placeholder="e.g. Contract ended">
                                    <button class="btn btn-outline-danger w-100">Mark Resigned</button>
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
