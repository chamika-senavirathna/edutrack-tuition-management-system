<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Subjects"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <div class="row g-4">
                <div class="col-lg-5">
                    <div class="card">
                        <div class="card-header">${empty subject ? 'Add Subject' : 'Edit Subject'}</div>
                        <div class="card-body">
                            <form method="post" action="${pageContext.request.contextPath}/subjects/save">
                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                <input type="hidden" name="id" value="${subject.id}">
                                <input type="hidden" name="active" value="${subject.active == false ? 'off' : 'on'}">
                                <div class="mb-3"><label class="form-label required-label">Code</label>
                                    <input class="form-control" name="code" value="<c:out value='${subject.code}'/>" required placeholder="MATH10"></div>
                                <div class="mb-3"><label class="form-label required-label">Name</label>
                                    <input class="form-control" name="name" value="<c:out value='${subject.name}'/>" required placeholder="Mathematics"></div>
                                <div class="mb-3"><label class="form-label">Description</label>
                                    <textarea class="form-control" name="description" rows="2"><c:out value="${subject.description}"/></textarea></div>
                                <button class="btn btn-primary w-100">Save Subject</button>
                            </form>
                        </div>
                    </div>
                </div>
                <div class="col-lg-7">
                    <div class="card">
                        <div class="card-header">Subject Catalogue</div>
                        <table class="table mb-0">
                            <thead><tr><th>Code</th><th>Name</th><th>Active</th></tr></thead>
                            <tbody>
                            <c:forEach items="${subjects}" var="s">
                                <tr><td class="fw-semibold">${s.code}</td><td>${s.name}</td>
                                    <td><span class="badge ${s.active ? 'badge-ACTIVE' : 'badge-ARCHIVED'}">${s.active ? 'ACTIVE' : 'INACTIVE'}</span>
                                    <c:if test="${user.hasRole('ADMIN') || user.hasRole('ACADEMIC_COORDINATOR')}">
                                        <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/subjects?id=${s.id}">Edit</a>
                                        <form class="d-inline" method="post" action="${pageContext.request.contextPath}/subjects/save">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                                            <input type="hidden" name="id" value="${s.id}">
                                            <input type="hidden" name="code" value="<c:out value='${s.code}'/>">
                                            <input type="hidden" name="name" value="<c:out value='${s.name}'/>">
                                            <input type="hidden" name="description" value="<c:out value='${s.description}'/>">
                                            <input type="hidden" name="active" value="${s.active ? 'off' : 'on'}">
                                            <button class="btn btn-sm btn-outline-secondary">${s.active ? 'Deactivate' : 'Activate'}</button>
                                        </form>
                                    </c:if></td></tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/edutrack.js"></script>
</body>
</html>
