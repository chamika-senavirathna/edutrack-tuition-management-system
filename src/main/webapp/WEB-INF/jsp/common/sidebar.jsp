<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%-- Permission-based navigation (Section 20): items render only for allowed roles. --%>
<c:set var="r" value="${user.primaryRole()}"/>
<c:set var="isAdmin" value="${fn:contains('ADMIN', r) || user.hasRole('ADMIN')}"/>
<nav class="et-sidebar" id="etSidebar">
    <a class="brand" href="${pageContext.request.contextPath}/dashboard">Edu<span>Track</span> Sri Lanka</a>

    <ul class="nav flex-column mt-2">
        <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/dashboard"><i class="bi bi-grid-1x2-fill me-2"></i>Dashboard</a></li>
    </ul>

    <c:if test="${user.hasRole('ADMIN') || user.hasRole('ACADEMIC_COORDINATOR') || user.hasRole('PRINCIPAL') || user.hasRole('TEACHER')}">
        <div class="nav-section">Registration &amp; Profiles · M01</div>
        <ul class="nav flex-column">
            <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/students"><i class="bi bi-people-fill me-2"></i>Students</a></li>
            <c:if test="${user.hasRole('ADMIN') || user.hasRole('ACADEMIC_COORDINATOR') || user.hasRole('PRINCIPAL')}">
                <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/teachers"><i class="bi bi-person-badge-fill me-2"></i>Teachers</a></li>
                <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/guardians"><i class="bi bi-person-hearts me-2"></i>Guardians</a></li>
            </c:if>
        </ul>
    </c:if>

    <%-- M02 staff management: class, timetable, subject, room administration. --%>
    <c:if test="${user.hasRole('ADMIN') || user.hasRole('ACADEMIC_COORDINATOR') || user.hasRole('PRINCIPAL') || user.hasRole('TEACHER')}">
        <div class="nav-section">Classes &amp; Timetable · M02</div>
        <ul class="nav flex-column">
            <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/classes"><i class="bi bi-collection-fill me-2"></i>Classes</a></li>
            <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/timetable"><i class="bi bi-calendar3 me-2"></i>Timetable</a></li>
            <c:if test="${user.hasRole('ADMIN') || user.hasRole('ACADEMIC_COORDINATOR') || user.hasRole('PRINCIPAL')}">
                <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/subjects"><i class="bi bi-journal-bookmark-fill me-2"></i>Subjects</a></li>
                <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/rooms"><i class="bi bi-building-fill me-2"></i>Rooms</a></li>
            </c:if>
        </ul>
        <div class="nav-section">Communication · M02</div>
        <ul class="nav flex-column">
            <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/notices"><i class="bi bi-megaphone-fill me-2"></i>Notices</a></li>
            <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/materials"><i class="bi bi-folder-fill me-2"></i>Materials</a></li>
        </ul>
    </c:if>

    <%-- Students/parents: direct access to their own published timetable and notices. --%>
    <c:if test="${user.hasRole('STUDENT') || user.hasRole('PARENT')}">
        <div class="nav-section">My Timetable · M02</div>
        <ul class="nav flex-column">
            <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/timetable"><i class="bi bi-calendar3 me-2"></i>My Timetable</a></li>
            <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/notices"><i class="bi bi-megaphone-fill me-2"></i>Notices</a></li>
            <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/materials"><i class="bi bi-folder-fill me-2"></i>Materials</a></li>
        </ul>
    </c:if>

    <%-- Attendance: staff manage the register; students/parents view own history (record-scoped). --%>
    <c:if test="${user.hasRole('ADMIN') || user.hasRole('ACADEMIC_COORDINATOR') || user.hasRole('PRINCIPAL') || user.hasRole('TEACHER') || user.hasRole('STUDENT') || user.hasRole('PARENT')}">
        <div class="nav-section">Attendance · M03</div>
        <ul class="nav flex-column">
            <c:choose>
                <c:when test="${user.hasRole('STUDENT') || user.hasRole('PARENT')}">
                    <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/attendance"><i class="bi bi-clipboard2-check-fill me-2"></i>My Attendance</a></li>
                </c:when>
                <c:otherwise>
                    <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/attendance"><i class="bi bi-clipboard2-check-fill me-2"></i>Attendance Register</a></li>
                </c:otherwise>
            </c:choose>
            <c:if test="${user.hasRole('ADMIN') || user.hasRole('ACADEMIC_COORDINATOR') || user.hasRole('PRINCIPAL') || user.hasRole('TEACHER')}">
                <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/attendance/chronic"><i class="bi bi-exclamation-triangle-fill me-2"></i>Chronic Absence</a></li>
            </c:if>
        </ul>
    </c:if>

    <c:if test="${user.hasRole('ADMIN') || user.hasRole('FINANCE') || user.hasRole('PRINCIPAL') || user.hasRole('STUDENT') || user.hasRole('PARENT')}">
        <div class="nav-section">Fees &amp; Payments · M04</div>
        <ul class="nav flex-column">
            <c:choose>
                <c:when test="${user.hasRole('STUDENT') || user.hasRole('PARENT')}">
                    <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/fees"><i class="bi bi-wallet2 me-2"></i>My Fees</a></li>
                    <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/receipts"><i class="bi bi-receipt-cutoff me-2"></i>My Receipts</a></li>
                </c:when>
                <c:otherwise>
                    <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/fees"><i class="bi bi-wallet2 me-2"></i>Fees</a></li>
                    <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/payments"><i class="bi bi-credit-card-2-front-fill me-2"></i>Payments</a></li>
                    <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/receipts"><i class="bi bi-receipt-cutoff me-2"></i>Receipts</a></li>
                </c:otherwise>
            </c:choose>
        </ul>
    </c:if>

    <c:if test="${user.hasRole('ADMIN') || user.hasRole('ACADEMIC_COORDINATOR') || user.hasRole('PRINCIPAL') || user.hasRole('TEACHER') || user.hasRole('STUDENT') || user.hasRole('PARENT')}">
        <div class="nav-section">Examinations &amp; Reporting · M06</div>
        <ul class="nav flex-column">
            <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/examinations"><i class="bi bi-mortarboard-fill me-2"></i>Examinations</a></li>
            <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/results"><i class="bi bi-bar-chart-fill me-2"></i>Results</a></li>
            <c:if test="${user.hasRole('ADMIN') || user.hasRole('PRINCIPAL') || user.hasRole('ACADEMIC_COORDINATOR') || user.hasRole('STUDENT') || user.hasRole('PARENT')}">
                <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/reportcards"><i class="bi bi-file-earmark-text-fill me-2"></i>Report Cards</a></li>
            </c:if>
            <c:if test="${user.hasRole('ADMIN') || user.hasRole('PRINCIPAL') || user.hasRole('ACADEMIC_COORDINATOR') || user.hasRole('FINANCE')}">
                <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/reports"><i class="bi bi-graph-up-arrow me-2"></i>Reports</a></li>
            </c:if>
        </ul>
    </c:if>

    <c:if test="${user.hasRole('ADMIN')}">
        <div class="nav-section">User &amp; Access · M05</div>
        <ul class="nav flex-column">
            <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/users"><i class="bi bi-shield-lock-fill me-2"></i>Users</a></li>
            <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/audit"><i class="bi bi-clock-history me-2"></i>Audit Logs</a></li>
        </ul>
    </c:if>

    <div class="nav-section">Account</div>
    <ul class="nav flex-column">
        <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/profile"><i class="bi bi-person-circle me-2"></i>My Profile</a></li>
        <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/logout"><i class="bi bi-box-arrow-right me-2"></i>Sign Out</a></li>
    </ul>
<div class="sidebar-footer">
    <div class="sidebar-footer-icon"><i class="bi bi-lightbulb-fill"></i></div>
    <div class="sidebar-footer-title">Education Creates<br>Opportunities</div>
</div>
</nav>
