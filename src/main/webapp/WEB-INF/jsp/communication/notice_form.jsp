<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="${notice != null ? 'Edit Notice' : 'New Notice'}"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <form method="post" action="${pageContext.request.contextPath}${notice != null ? '/notices/edit' : '/notices/add'}">
                <input type="hidden" name="csrfToken" value="${csrfToken}">
                <c:if test="${notice != null}"><input type="hidden" name="id" value="${notice.id}"></c:if>
                <div class="row g-4">
                    <div class="col-lg-8">
                        <div class="card"><div class="card-header">Notice</div>
                            <div class="card-body row g-3">
                                <div class="col-12"><label class="form-label required-label">Title</label>
                                    <input class="form-control" name="title" value="${notice.title}" required></div>
                                <div class="col-12"><label class="form-label required-label">Content</label>
                                    <textarea class="form-control" name="content" rows="5" required>${notice.content}</textarea></div>
                                <div class="col-md-4"><label class="form-label">Category</label>
                                    <select class="form-select" name="category">
                                        <option ${notice == null || notice.category == 'GENERAL' ? 'selected' : ''}>GENERAL</option>
                                        <option ${notice.category == 'ACADEMIC' ? 'selected' : ''}>ACADEMIC</option>
                                        <option ${notice.category == 'FINANCIAL' ? 'selected' : ''}>FINANCIAL</option>
                                        <option ${notice.category == 'EVENT' ? 'selected' : ''}>EVENT</option>
                                        <option ${notice.category == 'EMERGENCY' ? 'selected' : ''}>EMERGENCY</option>
                                    </select></div>
                                <div class="col-md-4"><label class="form-label">Priority</label>
                                    <select class="form-select" name="priority">
                                        <option ${notice == null || notice.priority == 'NORMAL' ? 'selected' : ''}>NORMAL</option>
                                        <option ${notice.priority == 'HIGH' ? 'selected' : ''}>HIGH</option>
                                        <option ${notice.priority == 'EMERGENCY' ? 'selected' : ''}>EMERGENCY</option>
                                    </select></div>
                                <div class="col-md-4"><label class="form-label">Channel</label>
                                    <select class="form-select" name="channel">
                                        <option ${notice == null || notice.channel == 'IN_SYSTEM' ? 'selected' : ''}>IN_SYSTEM</option>
                                        <option ${notice.channel == 'EMAIL' ? 'selected' : ''}>EMAIL</option>
                                        <option ${notice.channel == 'SMS' ? 'selected' : ''}>SMS</option>
                                    </select>
                                    <div class="form-text">Delivery is tracked, not guaranteed (use case boundary).</div></div>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg-4">
                        <div class="card mb-3"><div class="card-header">Audience Targeting</div>
                            <div class="card-body">
                                <select class="form-select mb-2" name="audienceType">
                                    <option value="ALL" ${notice.audienceType == 'ALL' ? 'selected' : ''}>Whole school</option>
                                    <option value="CLASS" ${notice.audienceType == 'CLASS' ? 'selected' : ''}>A class</option>
                                    <option value="SUBJECT" ${notice.audienceType == 'SUBJECT' ? 'selected' : ''}>A subject group</option>
                                    <option value="ROLE" ${notice.audienceType == 'ROLE' ? 'selected' : ''}>A role</option>
                                </select>
                                <select class="form-select" name="audienceRefId">
                                    <option value="-1">Not applicable</option>
                                    <c:forEach items="${classes}" var="c"><option value="${c.id}" ${notice.audienceType == 'CLASS' && notice.audienceRefId == c.id ? 'selected' : ''}>${c.name}</option></c:forEach>
                                    <c:forEach items="${roles}" var="r"><option value="${r.id}" ${notice.audienceType == 'ROLE' && notice.audienceRefId == r.id ? 'selected' : ''}>Role: ${r.name}</option></c:forEach>
                                    <c:forEach items="${subjects}" var="s"><option value="${s.id}" ${notice.audienceType == 'SUBJECT' && notice.audienceRefId == s.id ? 'selected' : ''}>${s.name}</option></c:forEach>
                                </select>
                                <div class="form-text">Class/subject audiences reach enrolled students and their parents.</div>
                            </div>
                        </div>
                        <button class="btn btn-outline-secondary w-100 mb-2" type="submit" name="submitAction" value="draft">Save as Draft</button>
                        <button class="btn btn-primary w-100" type="submit" name="submitAction" value="publish">Publish Now</button>
                    </div>
                </div>
            </form>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/edutrack.js"></script>
</body>
</html>
