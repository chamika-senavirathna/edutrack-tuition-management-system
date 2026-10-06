<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Notices"/>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
    <%@ include file="../common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="../common/header.jsp" %>
        <%@ include file="../common/flash.jsp" %>
        <div class="px-4 pt-3">
            <c:set var="isStaff" value="${user.hasRole('ADMIN') || user.hasRole('ACADEMIC_COORDINATOR') || user.hasRole('PRINCIPAL') || user.hasRole('TEACHER') || user.hasRole('FINANCE')}"/>
            <div class="d-flex justify-content-between align-items-center mb-3">
                <p class="text-muted mb-0">Member 02 · Communication Management (absorbed from the former Module 07)</p>
                <c:if test="${isStaff}">
                    <a class="btn btn-primary" href="${pageContext.request.contextPath}/notices/add">+ New Notice</a>
                </c:if>
            </div>

            <c:if test="${isStaff}">
                <form class="card card-body mb-3 row g-2 flex-md-row align-items-md-end" method="get">
                    <div class="col-md-4"><label class="form-label small">Search title</label>
                        <input class="form-control" name="q" value="${q}"></div>
                    <div class="col-md-3"><label class="form-label small">Category</label>
                        <select class="form-select" name="category"><option value="">All</option>
                            <option ${category == 'GENERAL' ? 'selected' : ''}>GENERAL</option>
                            <option ${category == 'ACADEMIC' ? 'selected' : ''}>ACADEMIC</option>
                            <option ${category == 'FINANCIAL' ? 'selected' : ''}>FINANCIAL</option>
                            <option ${category == 'EVENT' ? 'selected' : ''}>EVENT</option>
                            <option ${category == 'EMERGENCY' ? 'selected' : ''}>EMERGENCY</option>
                        </select></div>
                    <div class="col-md-3"><label class="form-label small">Status</label>
                        <select class="form-select" name="status"><option value="">All</option>
                            <option ${status == 'DRAFT' ? 'selected' : ''}>DRAFT</option>
                            <option ${status == 'PUBLISHED' ? 'selected' : ''}>PUBLISHED</option>
                            <option ${status == 'ARCHIVED' ? 'selected' : ''}>ARCHIVED</option>
                        </select></div>
                    <div class="col-md-2"><button class="btn btn-outline-primary w-100">Filter</button></div>
                </form>
            </c:if>

            <div class="row g-3">
                <c:if test="${empty notices}">
                    <div class="col-12"><div class="card"><div class="empty-state"><div class="icon">📣</div>No notices to show.</div></div></div>
                </c:if>
                <c:forEach items="${notices}" var="n">
                    <div class="col-md-6 col-xl-4">
                        <div class="card h-100">
                            <div class="card-body">
                                <div class="d-flex justify-content-between align-items-start mb-2">
                                    <span class="badge bg-secondary">${n.category}</span>
                                    <span class="badge badge-${n.status}">${n.status}</span>
                                </div>
                                <h5 class="h6 fw-bold mb-1">${n.title}</h5>
                                <p class="small text-muted mb-2">
                                    <c:choose><c:when test="${fn:length(n.content) > 120}">${fn:substring(n.content, 0, 120)}…</c:when>
                                        <c:otherwise>${n.content}</c:otherwise></c:choose>
                                </p>
                                <div class="small text-muted mb-2">
                                    Audience: ${n.audienceType} ${n.audienceRefName != null ? '· '.concat(n.audienceRefName) : ''}
                                    · Channel: ${n.channel}
                                </div>
                                <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/notices/view?id=${n.id}">Read</a>
                                <c:if test="${isStaff && n.status != 'ARCHIVED'}">
                                    <a class="btn btn-sm btn-outline-secondary" href="${pageContext.request.contextPath}/notices/edit?id=${n.id}">Edit</a>
                                    <form class="d-inline" method="post" action="${pageContext.request.contextPath}/notices/archive"
                                          data-confirm="Archive this notice? Delivery history is kept.">
                                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                                        <input type="hidden" name="id" value="${n.id}">
                                        <button class="btn btn-sm btn-outline-danger">Archive</button>
                                    </form>
                                </c:if>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/edutrack.js"></script>
</body>
</html>
