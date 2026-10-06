<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Share Material"/>
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
                    <form method="post" action="${pageContext.request.contextPath}/materials/${empty material ? 'add' : 'edit'}">
                        <input type="hidden" name="id" value="${material.id}">
                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                        <div class="card"><div class="card-header">Share a Teaching Material</div>
                            <div class="card-body row g-3">
                                <div class="col-12"><label class="form-label required-label">Title</label>
                                    <input class="form-control" name="title" value="<c:out value='${material.title}'/>" required></div>
                                <div class="col-md-6"><label class="form-label">Class</label>
                                    <select class="form-select" name="classId" required>
                                        <option value="-1">Select…</option>
                                        <c:forEach items="${classes}" var="c"><option value="${c.id}" ${material.classId == c.id ? 'selected' : ''}>${c.name}</option></c:forEach>
                                    </select></div>
                                <div class="col-md-6"><label class="form-label">Subject</label>
                                    <select class="form-select" name="subjectId">
                                        <option value="-1">Use class subject</option>
                                        <c:forEach items="${subjects}" var="s"><option value="${s.id}" ${material.subjectId == s.id ? 'selected' : ''}>${s.name}</option></c:forEach>
                                    </select></div>
                                <div class="col-12"><label class="form-label required-label">Material link</label>
                                    <input class="form-control" name="link" value="<c:out value='${material.filePath}'/>" required placeholder="https://… or shared network path">
                                    <div class="form-text">First release stores a reference link; binary upload limits are an open decision.</div></div>
                                <div class="col-12"><label class="form-label">Description</label>
                                    <textarea class="form-control" name="description" rows="2"><c:out value="${material.description}"/></textarea></div>
                            </div>
                        </div>
                        <button class="btn btn-primary mt-3">${empty material ? 'Share Material' : 'Save Changes'}</button>
                    </form>
                </div>
            </div>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/edutrack.js"></script>
</body>
</html>
