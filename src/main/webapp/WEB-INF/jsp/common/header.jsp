<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<header class="et-header">
    <div class="d-flex align-items-center gap-3">
        <button class="btn btn-outline-secondary btn-sm d-lg-none" id="sidebarToggle" type="button" aria-label="Open menu"><i class="bi bi-list"></i></button>
        <div class="et-search d-none d-md-flex">
            <i class="bi bi-search"></i>
            <input type="text" placeholder="Search students, courses, or anything...">
        </div>
    </div>
    <div class="d-flex align-items-center gap-3">
        <button class="workspace-theme-toggle" id="workspaceThemeToggle" type="button" aria-label="Dark mode" aria-pressed="true"><span aria-hidden="true">◐</span><span class="theme-label">Dark</span></button>
        <button class="et-bell" type="button" aria-label="Notifications">
            <i class="bi bi-bell"></i>
            <span class="et-bell-dot">3</span>
        </button>
        <details class="account-menu">
        <summary class="user-chip" aria-label="Account options">
            <div class="avatar"><i class="bi bi-person-fill"></i></div>
            <div class="user-meta d-none d-sm-block"><div class="name">${user.fullName}</div><div class="handle">${user.primaryRole()}</div></div>
            <i class="bi bi-chevron-down text-muted small" aria-hidden="true"></i>
        </summary>
        <nav class="account-menu-panel" aria-label="Account navigation">
            <a href="${pageContext.request.contextPath}/profile"><i class="bi bi-person-circle" aria-hidden="true"></i> My Profile</a>
            <a href="${pageContext.request.contextPath}/logout"><i class="bi bi-box-arrow-right" aria-hidden="true"></i> Sign Out</a>
        </nav>
        </details>
    </div>
</header>
<div class="et-page-heading px-4">
    <div>
        <h1 class="page-title">${pageTitle}</h1>
        <c:choose>
            <c:when test="${pageTitle == 'Dashboard'}">
                <p class="page-sub">Welcome back, ${user.fullName}! Here's what's happening today.</p>
            </c:when>
            <c:otherwise>
                <p class="page-sub text-muted">${user.primaryRole()} workspace</p>
            </c:otherwise>
        </c:choose>
    </div>
    <div class="text-end d-none d-md-block">
        <div class="page-date" id="etTodayDate"></div>
        <div class="page-greeting"><i class="bi bi-sun-fill"></i> <span id="etGreeting">Good day!</span></div>
    </div>
</div>
