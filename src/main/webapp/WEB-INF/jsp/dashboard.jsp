<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <c:set var="pageTitle" value="Dashboard"/>
    <%@ include file="common/head.jsp" %>
</head>
<body>
    <%@ include file="common/sidebar.jsp" %>
    <div class="et-main">
        <%@ include file="common/header.jsp" %>
        <%@ include file="common/flash.jsp" %>

        <div class="px-4 pt-3">
            <section class="workspace-hero" aria-label="Workspace welcome">
                <div><div class="atelier-eyebrow">YOUR EDUTRACK WORKSPACE</div><h2>A new day.<br><em>A world of possibility.</em></h2><p>Everything you need to keep learning moving.</p></div>
                <div class="workspace-motif" aria-hidden="true"><span>✦</span><small>LEARN · GROW · BECOME</small></div>
            </section>
            <c:if test="${not empty denied}">
                <div class="alert alert-warning">You do not have permission to open <code>${denied}</code>.</div>
            </c:if>

            <%-- staff overview (Admin / Principal / Coordinator / Finance) --%>
            <c:if test="${not empty stats}">
                <div class="row g-3 mb-4">
                    <div class="col-6 col-md-3">
                        <div class="card stat-card-v2 blue p-3">
                            <div class="stat-icon"><i class="bi bi-people-fill"></i></div>
                            <div class="stat-label">Active Students</div>
                            <div class="stat-value">${stats.students}</div>
                            <div class="stat-trend flat"><i class="bi bi-dash"></i> Module M01</div>
                        </div>
                    </div>
                    <div class="col-6 col-md-3">
                        <div class="card stat-card-v2 purple p-3">
                            <div class="stat-icon"><i class="bi bi-collection-fill"></i></div>
                            <div class="stat-label">Active Classes</div>
                            <div class="stat-value">${stats.classes}</div>
                            <div class="stat-trend flat"><i class="bi bi-dash"></i> Module M02</div>
                        </div>
                    </div>
                    <div class="col-6 col-md-3">
                        <div class="card stat-card-v2 green p-3">
                            <div class="stat-icon"><i class="bi bi-person-badge-fill"></i></div>
                            <div class="stat-label">Teachers</div>
                            <div class="stat-value">${stats.teachers}</div>
                            <div class="stat-trend flat"><i class="bi bi-dash"></i> Module M01</div>
                        </div>
                    </div>
                    <div class="col-6 col-md-3">
                        <div class="card stat-card-v2 red p-3">
                            <div class="stat-icon"><i class="bi bi-exclamation-circle-fill"></i></div>
                            <div class="stat-label">Invoices Outstanding</div>
                            <div class="stat-value">${stats.outstanding}</div>
                            <div class="stat-trend flat"><i class="bi bi-dash"></i> Module M04</div>
                        </div>
                    </div>
                    <div class="col-6 col-md-3">
                        <div class="card stat-card-v2 blue p-3">
                            <div class="stat-icon"><i class="bi bi-mortarboard-fill"></i></div>
                            <div class="stat-label">Scheduled Exams</div>
                            <div class="stat-value">${stats.exams}</div>
                            <div class="stat-trend flat"><i class="bi bi-dash"></i> Module M06</div>
                        </div>
                    </div>
                    <div class="col-6 col-md-3">
                        <div class="card stat-card-v2 green p-3">
                            <div class="stat-icon"><i class="bi bi-megaphone-fill"></i></div>
                            <div class="stat-label">Published Notices</div>
                            <div class="stat-value">${stats.notices}</div>
                            <div class="stat-trend flat"><i class="bi bi-dash"></i> Module M02</div>
                        </div>
                    </div>
                    <div class="col-6 col-md-3">
                        <div class="card stat-card-v2 purple p-3">
                            <div class="stat-icon"><i class="bi bi-hourglass-split"></i></div>
                            <div class="stat-label">Marks Awaiting Moderation</div>
                            <div class="stat-value">${stats.marksPending}</div>
                            <div class="stat-trend flat"><i class="bi bi-dash"></i> Module M06</div>
                        </div>
                    </div>
                    <div class="col-6 col-md-3">
                        <div class="card stat-card-v2 blue p-3">
                            <div class="stat-icon"><i class="bi bi-shield-lock-fill"></i></div>
                            <div class="stat-label">Active Users</div>
                            <div class="stat-value">${stats.users}</div>
                            <div class="stat-trend flat"><i class="bi bi-dash"></i> Module M05</div>
                        </div>
                    </div>
                </div>
                <c:if test="${not empty financeInvoiced}">
                    <div class="row g-4 mb-4">
                        <div class="col-lg-7">
                            <div class="card chart-card h-100">
                                <div class="card-header"><span><i class="bi bi-graph-up-arrow me-2 text-primary"></i>Fees Overview (LKR) · M04</span></div>
                                <div class="card-body"><canvas id="financeChart" height="220"></canvas></div>
                            </div>
                        </div>
                        <div class="col-lg-5">
                            <div class="row g-3 h-100">
                                <div class="col-12">
                                    <div class="card stat-card-v2 blue p-3"><div class="stat-icon"><i class="bi bi-receipt"></i></div><div class="stat-label">Invoiced</div><div class="stat-value">LKR ${financeInvoiced}</div></div>
                                </div>
                                <div class="col-12">
                                    <div class="card stat-card-v2 green p-3"><div class="stat-icon"><i class="bi bi-cash-coin"></i></div><div class="stat-label">Collected</div><div class="stat-value">LKR ${financeCollected}</div></div>
                                </div>
                                <div class="col-12">
                                    <div class="card stat-card-v2 red p-3"><div class="stat-icon"><i class="bi bi-exclamation-triangle-fill"></i></div><div class="stat-label">Outstanding</div><div class="stat-value">LKR ${financeOutstanding}</div></div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <script>
                        window.addEventListener('DOMContentLoaded', function () {
                            var ctx = document.getElementById('financeChart');
                            if (!ctx || !window.Chart) return;
                            new Chart(ctx, {
                                type: 'bar',
                                data: {
                                    labels: ['Invoiced', 'Collected', 'Outstanding'],
                                    datasets: [{
                                        data: [${financeInvoiced}, ${financeCollected}, ${financeOutstanding}],
                                        backgroundColor: ['#2563eb', '#10b981', '#ef4444'],
                                        borderRadius: 8,
                                        maxBarThickness: 64
                                    }]
                                },
                                options: {
                                    plugins: { legend: { display: false } },
                                    scales: { y: { beginAtZero: true, grid: { color: '#f0f3f8' } }, x: { grid: { display: false } } }
                                }
                            });
                        });
                    </script>
                </c:if>
            </c:if>

            <div class="row g-4">
                <%-- teacher panel --%>
                <c:if test="${not empty mySlots && not empty myClasses}">
                    <div class="col-lg-7">
                        <div class="card h-100">
                            <div class="card-header"><i class="bi bi-calendar3 me-2 text-primary"></i>My Published Timetable · M02</div>
                            <div class="card-body p-0">
                                <table class="table mb-0">
                                    <thead><tr><th>Day</th><th>Time</th><th>Class</th><th>Subject</th></tr></thead>
                                    <tbody>
                                    <c:forEach items="${mySlots}" var="slot">
                                        <tr>
                                            <td>${slot.dayOfWeek}</td>
                                            <td>${slot.startTime}–${slot.endTime}</td>
                                            <td>${slot.className}</td>
                                            <td>${slot.subjectName}</td>
                                        </tr>
                                    </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </c:if>

                <%-- student / parent panel --%>
                <c:if test="${not empty myEnrolments}">
                    <div class="col-lg-7 dashboard-student-panels">
                        <div class="card mb-4">
                            <div class="card-header"><i class="bi bi-mortarboard me-2 text-primary"></i>My Enrolment · M01</div>
                            <ul class="list-group list-group-flush">
                                <c:forEach items="${myEnrolments}" var="e">
                                    <li class="list-group-item d-flex justify-content-between align-items-center">
                                        ${e.className} <span class="badge badge-${e.status}">${e.status}</span>
                                    </li>
                                </c:forEach>
                            </ul>
                        </div>
                        <c:if test="${not empty myInvoices}">
                            <div class="card mb-4">
                                <div class="card-header"><i class="bi bi-wallet2 me-2 text-primary"></i>My Fee Status · M04</div>
                                <table class="table mb-0">
                                    <thead><tr><th>Invoice</th><th>Term</th><th>Balance</th><th>Status</th></tr></thead>
                                    <tbody>
                                    <c:forEach items="${myInvoices}" var="inv">
                                        <tr>
                                            <td>${inv.invoiceNo}</td>
                                            <td>${inv.termName}</td>
                                            <td>LKR ${inv.balance}</td>
                                            <td><span class="badge badge-${inv.status}">${inv.status}</span></td>
                                        </tr>
                                    </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:if>
                        <c:if test="${not empty attendanceTrend}">
                            <section class="card attendance-summary" aria-labelledby="attendance-summary-title">
                                <div class="card-header"><span id="attendance-summary-title"><i class="bi bi-clipboard2-check me-2 text-primary" aria-hidden="true"></i>My attendance</span><a href="${pageContext.request.contextPath}/attendance">View attendance history <span aria-hidden="true">→</span></a></div>
                                <div class="card-body">
                                    <p class="attendance-summary-note">Your recorded attendance, grouped by status.</p>
                                    <dl class="attendance-summary-grid">
                                        <div class="attendance-summary-tile attendance-present"><dt><span aria-hidden="true">✓</span> Present</dt><dd>${attendanceTrend.present}</dd><p>Marked present</p></div>
                                        <div class="attendance-summary-tile attendance-absent"><dt><span aria-hidden="true">−</span> Absent</dt><dd>${attendanceTrend.absent}</dd><p>Marked absent</p></div>
                                        <div class="attendance-summary-tile attendance-late"><dt><span aria-hidden="true">◷</span> Late</dt><dd>${attendanceTrend.late}</dd><p>Marked late</p></div>
                                    </dl>
                                </div>
                            </section>
                        </c:if>
                    </div>
                </c:if>

                <%-- notices for every role --%>
                <div class="col-lg-5">
                    <div class="card chart-card h-100">
                        <div class="card-header">
                            <span><i class="bi bi-megaphone-fill me-2 text-primary"></i>Latest Notices · M02</span>
                            <a href="${pageContext.request.contextPath}/notices" class="small text-decoration-none">View All</a>
                        </div>
                        <div class="card-body p-0">
                            <c:if test="${empty notices}">
                                <div class="empty-state"><div class="icon">📣</div>No notices published for you yet.</div>
                            </c:if>
                            <ul class="recent-list">
                                <c:forEach items="${notices}" var="n" varStatus="ns">
                                    <li>
                                        <div class="r-avatar"><i class="bi bi-megaphone"></i></div>
                                        <a class="text-decoration-none flex-grow-1" href="${pageContext.request.contextPath}/notices/view?id=${n.id}">
                                            <div class="r-name">${n.title}</div>
                                            <div class="r-meta">${n.category} · ${n.audienceType}</div>
                                        </a>
                                        <c:if test="${ns.index lt 2}"><span class="r-badge">New</span></c:if>
                                    </li>
                                </c:forEach>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <c:if test="${not empty financeInvoiced}">
        <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.4/dist/chart.umd.min.js"></script>
    </c:if>
    <script src="${pageContext.request.contextPath}/js/edutrack.js"></script>
</body>
</html>

