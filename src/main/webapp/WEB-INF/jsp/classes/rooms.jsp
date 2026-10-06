<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Rooms"/>
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
                        <div class="card-header">${empty room ? 'Add Room' : 'Edit Room'}</div>
                        <div class="card-body">
                            <form method="post" action="${pageContext.request.contextPath}/rooms/save">
                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                <input type="hidden" name="id" value="${room.id}">
                                <div class="mb-3"><label class="form-label required-label">Name</label>
                                    <input class="form-control" name="name" value="<c:out value='${room.name}'/>" required placeholder="Room 101"></div>
                                <div class="mb-3"><label class="form-label">Type</label>
                                    <select class="form-select" name="roomType">
                                        <option value="CLASSROOM" ${room.roomType == 'CLASSROOM' ? 'selected' : ''}>Classroom</option>
                                        <option value="HALL" ${room.roomType == 'HALL' ? 'selected' : ''}>Hall</option>
                                        <option value="LAB" ${room.roomType == 'LAB' ? 'selected' : ''}>Lab</option>
                                    </select></div>
                                <div class="mb-3"><label class="form-label required-label">Capacity</label>
                                    <input type="number" class="form-control" name="capacity" min="1" required value="${empty room ? 30 : room.capacity}"></div>
                                <button class="btn btn-primary w-100">Save Room</button>
                            </form>
                        </div>
                    </div>
                </div>
                <div class="col-lg-7">
                    <div class="card">
                        <div class="card-header">Rooms</div>
                        <table class="table mb-0">
                            <thead><tr><th>Name</th><th>Type</th><th>Capacity</th></tr></thead>
                            <tbody>
                            <c:forEach items="${rooms}" var="r">
                                <tr><td class="fw-semibold">${r.name}</td><td>${r.roomType}</td><td>${r.capacity}
                                    <c:if test="${user.hasRole('ADMIN') || user.hasRole('ACADEMIC_COORDINATOR')}">
                                        <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/rooms?id=${r.id}">Edit</a>
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
