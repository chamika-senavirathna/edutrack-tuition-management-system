<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head><%@ include file="../common/head.jsp" %><link href="${pageContext.request.contextPath}/css/cinema.css?v=20261005-glass-images" rel="stylesheet"><script src="${pageContext.request.contextPath}/js/cinema.js?v=20261004-12" defer></script></head>
<body class="login-page atelier-landing cinema-page navy-hero">
<a class="skip-link" href="#signin">Skip to sign in</a>
<div class="cinema-stage" id="top">
<video class="cinema-video" id="cinemaVideo" muted autoplay loop playsinline preload="metadata" aria-hidden="true" tabindex="-1"><source src="https://d8j0ntlcm91z4.cloudfront.net/user_38xzZboKViGWJOttwIXH07lWA1P/hf_20260314_131748_f2ca2a28-fed7-44c8-b9a9-bd9acdd5ec31.mp4" type="video/mp4"></video>

    <nav class="cinema-nav" aria-label="Main navigation">
        <a class="cinema-brand" href="#top" style="font-family: 'Instrument Serif', serif">EduTrack<sup>®</sup></a>
        <button class="header-menu-button liquid-glass" type="button" aria-controls="hero-navigation" aria-expanded="false">Menu <span aria-hidden="true">☰</span></button>
        <div class="cinema-links" id="hero-navigation"><a href="#top" aria-current="page">Home</a><a href="#discover">About</a><a href="#features">Services</a><a href="#teachers">Teachers</a><a href="#gallery">Gallery</a><a href="#faq">FAQ</a><details class="feature-menu more-menu"><summary>More Info <span aria-hidden="true">⌄</span></summary><div class="feature-menu-panel"><a href="#pricing">Plans &amp; pricing</a><a href="#possibilities">Your workspace</a><a href="#signin">Student portal</a></div></details><a href="#contact">Contact</a></div>
        <a href="#signin" class="liquid-glass cinema-nav-login">Begin your journey</a>
    </nav>
    <section class="cinema-hero" aria-labelledby="hero-title">
        <h1 id="hero-title" style="font-family: 'Instrument Serif', serif">Where <em>potential</em> grows<br>into <em>possibility.</em></h1>
        <p>A connected space for curious students, inspiring teachers, and supportive families. EduTrack brings your academic world together, so every learning journey has room to grow.</p>
        <a class="cinema-entry liquid-glass" href="#signin">Begin your journey</a>
    </section>    <div class="cinema-bottom"><span>EDUTRACK / SRI LANKA<br><b>Built around your potential.</b></span><a class="cinema-scroll liquid-glass" href="#discover" aria-label="Scroll to discover EduTrack">↓</a><button class="cinema-motion liquid-glass" type="button" id="cinemaMotion" aria-pressed="false">Pause motion <span aria-hidden="true">Ⅱ</span></button></div>
</div>
<main>
<section class="editorial-about editorial-width" id="discover" data-reveal>
    <span class="cinema-kicker">01 / ABOUT EDUTRACK</span>
    <h2>A little more <em>connection.</em><br>A world of <em>possibility.</em></h2>
    <div class="editorial-about-note"><span>MADE FOR YOUR ACADEMIC WORLD</span><p>Great education starts with people. EduTrack brings students, teachers and families together in one place, making more room for the moments that move learning forward.</p></div>
</section>
<section class="editorial-film editorial-width" id="gallery" aria-label="The EduTrack experience" data-reveal>
    <img class="editorial-upload-image" src="${pageContext.request.contextPath}/images/landing/about-edutrack.png" alt="A tutor supporting a student in a welcoming learning space" loading="lazy" decoding="async">
    <div class="editorial-film-shade" aria-hidden="true"></div>
    <span class="editorial-film-label">THE BIGGER PICTURE / EDUTRACK</span>
    <div class="editorial-film-bottom"><div class="editorial-film-copy liquid-glass"><span class="cinema-kicker">OUR APPROACH</span><h3>Less to manage.<br><em>More to discover.</em></h3><p>Classes, communication and progress, thoughtfully connected. The details stay organised, so learning can take centre stage.</p></div><a class="editorial-pill liquid-glass" href="#possibilities">Find your space <span aria-hidden="true">↗</span></a></div>
</section>
<section class="editorial-philosophy editorial-width" data-reveal>
    <span class="cinema-kicker">02 / OUR PHILOSOPHY</span><h2>Learning <em>×</em> Possibility.</h2>
    <div class="editorial-split"><div class="editorial-philosophy-film"><img class="editorial-upload-image" src="${pageContext.request.contextPath}/images/landing/our-philosophy.png" alt="A calm study space with a digital learning planner" loading="lazy" decoding="async"><span>THE NEXT CHAPTER IS YOURS.</span></div>
    <div class="editorial-principles"><article><span class="cinema-kicker">A PLACE FOR EVERYONE</span><h3>Your journey. Connected.</h3><p>Students follow their learning. Teachers keep their classes moving. Families stay informed. Each person has a workspace shaped around their role.</p></article><article><span class="cinema-kicker">CLARITY FOR WHAT COMES NEXT</span><h3>Keep the bigger picture.</h3><p>From timetables and attendance to results and fees, bring the everyday details into focus. Spend less time finding information and more time acting on it.</p></article></div></div>
</section>
<section class="editorial-spaces editorial-width" id="possibilities">
    <div class="editorial-section-heading" data-reveal><div><span class="cinema-kicker">03 / YOUR WORKSPACE</span><h2>Find your <em>place.</em></h2></div><p>One academic community.<br>A space for every role.</p></div>
    <div class="editorial-cards">
        <article class="editorial-card liquid-glass" data-reveal><div class="editorial-card-film"><img class="editorial-upload-image" src="${pageContext.request.contextPath}/images/landing/students-families.png" alt="A student and family member following learning together" loading="lazy" decoding="async"><img class="editorial-upload-image editorial-image-alternate" src="${pageContext.request.contextPath}/images/landing/student-discovery.png" alt="A student exploring a library filled with possibilities" loading="lazy" decoding="async"><span class="editorial-card-number">01</span></div><div class="editorial-card-body"><div class="editorial-card-top"><span class="cinema-kicker">STUDENTS &amp; FAMILIES</span><a href="#signin" class="editorial-circle liquid-glass" aria-label="Sign in to the student and family workspace">↗</a></div><h3>Your next chapter.</h3><p>Your timetable, attendance, results and fee information in one connected space. Stay informed and keep your learning journey in sight.</p><div class="editorial-tags"><span>Timetables</span><span>Results</span><span>Notices</span></div></div></article>
        <article class="editorial-card liquid-glass" data-reveal><div class="editorial-card-film"><img class="editorial-upload-image" src="${pageContext.request.contextPath}/images/landing/teachers-staff.png" alt="A teacher preparing an inspiring lesson" loading="lazy" decoding="async"><span class="editorial-card-number">02</span></div><div class="editorial-card-body"><div class="editorial-card-top"><span class="cinema-kicker">TEACHERS &amp; STAFF</span><a href="#signin" class="editorial-circle liquid-glass" aria-label="Sign in to the teacher and staff workspace">↗</a></div><h3>Make room for teaching.</h3><p>Keep classes, attendance and academic records organised. Access the tools available to your role and support your community every day.</p><div class="editorial-tags"><span>Classes</span><span>Attendance</span><span>Reports</span></div></div></article>
    </div>
</section>
<section class="directory-section editorial-width" id="features" aria-labelledby="features-title">
    <div class="editorial-section-heading" data-reveal><div><span class="cinema-kicker">04 / EVERYDAY ESSENTIALS</span><h2 id="features-title">The details, <em>connected.</em></h2></div><p>Thoughtful tools for<br>your academic everyday.</p></div>
    <div class="directory-grid"><article class="directory-card" id="feature-attendance" data-reveal>
    <span class="directory-icon" aria-hidden="true">✓</span>
    <h3>Attendance</h3><p>Keep daily registers and student attendance history organised.</p>
    <details class="feature-detail-disclosure"><summary>Learn more <span aria-hidden="true">↗</span></summary><p>Review attendance by class, student and date, with corrections managed through the existing attendance workflow.</p><a href="#signin">Sign in to your workspace <span aria-hidden="true">→</span></a></details>
</article>
<article class="directory-card" id="feature-payments" data-reveal>
    <span class="directory-icon" aria-hidden="true">↗</span>
    <h3>Fee management</h3><p>Bring invoices, payments and receipts into one clear view.</p>
    <details class="feature-detail-disclosure"><summary>Learn more <span aria-hidden="true">↗</span></summary><p>Follow fee records and payment history through the existing fee workspace, with access based on your role.</p><a href="#signin">Sign in to your workspace <span aria-hidden="true">→</span></a></details>
</article>
<article class="directory-card" id="feature-students" data-reveal>
    <span class="directory-icon" aria-hidden="true">◎</span>
    <h3>Student management</h3><p>Keep student profiles, enrolments and guardian details connected.</p>
    <details class="feature-detail-disclosure"><summary>Learn more <span aria-hidden="true">↗</span></summary><p>Find student records and manage enrolments using the existing student and guardian workspaces.</p><a href="#signin">Sign in to your workspace <span aria-hidden="true">→</span></a></details>
</article>
<article class="directory-card" id="feature-schedule" data-reveal>
    <span class="directory-icon" aria-hidden="true">▦</span>
    <h3>Class schedules</h3><p>Give every class, teacher and room a place in your timetable.</p>
    <details class="feature-detail-disclosure"><summary>Learn more <span aria-hidden="true">↗</span></summary><p>Review class timetables and teaching schedules in the existing timetable workspace.</p><a href="#signin">Sign in to your workspace <span aria-hidden="true">→</span></a></details>
</article>
<article class="directory-card" id="feature-reports" data-reveal>
    <span class="directory-icon" aria-hidden="true">▥</span>
    <h3>Results &amp; reports</h3><p>See academic progress through marks, exams and report cards.</p>
    <details class="feature-detail-disclosure"><summary>Learn more <span aria-hidden="true">↗</span></summary><p>Organise examinations, record marks and review student report cards through the academic workspace.</p><a href="#signin">Sign in to your workspace <span aria-hidden="true">→</span></a></details>
</article>
<article class="directory-card" id="feature-institute" data-reveal>
    <span class="directory-icon" aria-hidden="true">⌂</span>
    <h3>Institute management</h3><p>Connect teachers, classes, notices and role-based workspaces.</p>
    <details class="feature-detail-disclosure"><summary>Learn more <span aria-hidden="true">↗</span></summary><p>Manage the people and classes in your academic community, and share updates through notices.</p><a href="#signin">Sign in to your workspace <span aria-hidden="true">→</span></a></details>
</article></div>
</section>
<section class="directory-section teacher-directory editorial-width" id="teachers" aria-labelledby="teachers-title">
    <div class="editorial-section-heading" data-reveal><div><span class="cinema-kicker">05 / THE PEOPLE BEHIND THE PROGRESS</span><h2 id="teachers-title">Meet our <em>educators.</em></h2></div><p>A subject you love.<br>A teacher who inspires.</p></div>
    <div class="educator-toolbar"><label for="teacher-search">Find your teacher or subject</label><input id="teacher-search" type="search" placeholder="Search by name or subject" autocomplete="off"><span id="teacher-count" role="status" aria-live="polite">12 educators</span></div>
    <div class="educator-grid"><article class="educator-card" data-educator><div class="educator-portrait"><img src="${pageContext.request.contextPath}/images/educators/204549.png" alt="Charitha Dissanayake" loading="lazy" decoding="async" style="width:446.7822%;left:-7.6733%;top:-37.4233%"></div><div class="educator-copy"><h3>Charitha Dissanayake</h3><p>Chemistry</p></div></article>
<article class="educator-card" data-educator><div class="educator-portrait"><img src="${pageContext.request.contextPath}/images/educators/204549.png" alt="Hemal Perera" loading="lazy" decoding="async" style="width:446.7822%;left:-117.5743%;top:-37.4233%"></div><div class="educator-copy"><h3>Hemal Perera</h3><p>Accounting</p></div></article>
<article class="educator-card" data-educator><div class="educator-portrait"><img src="${pageContext.request.contextPath}/images/educators/204549.png" alt="Asela Mallikarathna" loading="lazy" decoding="async" style="width:446.7822%;left:-227.4752%;top:-37.4233%"></div><div class="educator-copy"><h3>Asela Mallikarathna</h3><p>Economics</p></div></article>
<article class="educator-card" data-educator><div class="educator-portrait"><img src="${pageContext.request.contextPath}/images/educators/204549.png" alt="Bhanuka Ekanayake" loading="lazy" decoding="async" style="width:446.7822%;left:-337.3762%;top:-37.4233%"></div><div class="educator-copy"><h3>Bhanuka Ekanayake</h3><p>Information &amp; Communication Technology</p></div></article>
<article class="educator-card" data-educator><div class="educator-portrait"><img src="${pageContext.request.contextPath}/images/educators/204607.png" alt="Malith Wasalage" loading="lazy" decoding="async" style="width:447.1464%;left:-11.1663%;top:-19.2012%"></div><div class="educator-copy"><h3>Malith Wasalage</h3><p>Science for Technology</p></div></article>
<article class="educator-card" data-educator><div class="educator-portrait"><img src="${pageContext.request.contextPath}/images/educators/204607.png" alt="Srimal Wijesinghe" loading="lazy" decoding="async" style="width:447.1464%;left:-121.34%;top:-19.2012%"></div><div class="educator-copy"><h3>Srimal Wijesinghe</h3><p>Engineering Technology</p></div></article>
<article class="educator-card" data-educator><div class="educator-portrait"><img src="${pageContext.request.contextPath}/images/educators/204607.png" alt="Suranga Senadeera" loading="lazy" decoding="async" style="width:447.1464%;left:-231.5136%;top:-19.2012%"></div><div class="educator-copy"><h3>Suranga Senadeera</h3><p>Business Studies</p></div></article>
<article class="educator-card" data-educator><div class="educator-portrait"><img src="${pageContext.request.contextPath}/images/educators/204607.png" alt="Duminda Rathnayaka" loading="lazy" decoding="async" style="width:447.1464%;left:-341.6873%;top:-19.2012%"></div><div class="educator-copy"><h3>Duminda Rathnayaka</h3><p>Physics</p></div></article>
<article class="educator-card" data-educator><div class="educator-portrait"><img src="${pageContext.request.contextPath}/images/educators/204623.png" alt="Dr. Charitha Munasinghe" loading="lazy" decoding="async" style="width:444.665%;left:-7.196%;top:-11.8162%"></div><div class="educator-copy"><h3>Dr. Charitha Munasinghe</h3><p>Biology</p></div></article>
<article class="educator-card" data-educator><div class="educator-portrait"><img src="${pageContext.request.contextPath}/images/educators/204623.png" alt="Prasanna Maheepala" loading="lazy" decoding="async" style="width:444.665%;left:-117.3697%;top:-8.8621%"></div><div class="educator-copy"><h3>Prasanna Maheepala</h3><p>Combined Maths</p></div></article>
<article class="educator-card" data-educator><div class="educator-portrait"><img src="${pageContext.request.contextPath}/images/educators/204623.png" alt="Sampath Singha Arachchi" loading="lazy" decoding="async" style="width:444.665%;left:-227.5434%;top:-11.8162%"></div><div class="educator-copy"><h3>Sampath Singha Arachchi</h3><p>Media</p></div></article>
<article class="educator-card" data-educator><div class="educator-portrait"><img src="${pageContext.request.contextPath}/images/educators/204623.png" alt="Bhathiya Ranasinghe" loading="lazy" decoding="async" style="width:444.665%;left:-337.7171%;top:-11.8162%"></div><div class="educator-copy"><h3>Bhathiya Ranasinghe</h3><p>Geography</p></div></article></div>
    <p class="educator-empty" hidden>No teachers match your search. Try another name or subject.</p>
</section>
<section class="edutrack-pricing editorial-width" id="pricing" aria-labelledby="pricing-title">
    <div class="pricing-heading">
        <div data-reveal><span class="pricing-label"><span aria-hidden="true"></span> EduTrack plans</span><h2 id="pricing-title">Room to learn.<br><em>Space to grow.</em></h2></div>
        <p data-reveal>From individual tutors to growing institutions. Find your fit with straightforward monthly pricing in Sri Lankan rupees.</p>
    </div>
    <div class="pricing-grid">
        <article class="pricing-shell" data-pricing-card data-reveal aria-labelledby="tutor-title">
            <div class="pricing-card">
                
                <h3 id="tutor-title">Individual Tutor</h3>
                <div class="pricing-price"><span class="pricing-currency">LKR</span> 2,000<span class="pricing-equals">/=</span></div>
                <p class="pricing-period">per month</p>
                <p class="pricing-description">A focused start for your independent teaching journey.</p>
                <a class="pricing-button" href="#signin"><span>Explore EduTrack</span><span aria-hidden="true">↗</span></a>
                <p class="pricing-list-label">Suggested plan features</p><ul class="pricing-features"><li><span aria-hidden="true">✓</span> Student profiles and enrolments</li><li><span aria-hidden="true">✓</span> Class attendance records</li><li><span aria-hidden="true">✓</span> Fee and payment tracking</li><li><span aria-hidden="true">✓</span> Teaching timetable</li></ul>
            </div>
        </article>
        <article class="pricing-shell" data-pricing-card data-reveal aria-labelledby="standard-title">
            <div class="pricing-card">
                
                <h3 id="standard-title">Standard</h3>
                <div class="pricing-price"><span class="pricing-currency">LKR</span> 5,000<span class="pricing-equals">/=</span></div>
                <p class="pricing-period">per month</p>
                <p class="pricing-description">Keep your growing academic community connected.</p>
                <a class="pricing-button" href="#signin"><span>Explore EduTrack</span><span aria-hidden="true">↗</span></a>
                <p class="pricing-list-label">Suggested plan features</p><ul class="pricing-features"><li><span aria-hidden="true">✓</span> Student and guardian records</li><li><span aria-hidden="true">✓</span> Class and teacher organisation</li><li><span aria-hidden="true">✓</span> Attendance and fee records</li><li><span aria-hidden="true">✓</span> Examinations and report cards</li></ul>
            </div>
        </article>
        <article class="pricing-shell pricing-featured" data-pricing-card data-reveal aria-labelledby="premium-title">
            <div class="pricing-card">
                <span class="pricing-badge">Premium</span>
                <h3 id="premium-title">Premium</h3>
                <div class="pricing-price"><span class="pricing-currency">LKR</span> 20,000<span class="pricing-equals">/=</span></div>
                <p class="pricing-period">per month</p>
                <p class="pricing-description">Make room for the next chapter of your institution.</p>
                <a class="pricing-button pricing-button-primary" href="#signin"><span>Explore EduTrack</span><span aria-hidden="true">↗</span></a>
                <p class="pricing-list-label">Suggested plan features</p><ul class="pricing-features"><li><span aria-hidden="true">✓</span> Academic administration</li><li><span aria-hidden="true">✓</span> Teacher and staff workspaces</li><li><span aria-hidden="true">✓</span> Role-based access</li><li><span aria-hidden="true">✓</span> Notices and academic reporting</li></ul>
            </div>
        </article>
    </div>
    <p class="pricing-footnote">All prices are for one month. Feature groupings are a preview; existing account access remains the same.</p>
</section>
<section class="directory-section editorial-width landing-faq" id="faq" aria-labelledby="faq-title">
    <div class="editorial-section-heading" data-reveal><div><span class="cinema-kicker">06 / A LITTLE CLARITY</span><h2 id="faq-title">Good questions.<br><em>Clear answers.</em></h2></div></div>
    <details><summary>How do I sign in to EduTrack?</summary><p>Choose “Begin your journey” or “Student portal”, then enter your EduTrack username and password. If you do not have an account, contact your institute administrator.</p></details>
    <details><summary>Who can use EduTrack?</summary><p>EduTrack brings students, families, teachers and staff together. The information and tools you can access depend on your assigned role.</p></details>
    <details><summary>Are the plan prices monthly?</summary><p>Yes. Individual Tutor is LKR 2,000, Standard is LKR 5,000, and Premium is LKR 20,000 for one month.</p></details>
    <details><summary>Where can I see my classes and results?</summary><p>Sign in to your workspace to view the class, attendance and academic information available to your account.</p></details>
</section>
<section class="editorial-width landing-contact" id="contact" aria-labelledby="contact-title"><div><span class="cinema-kicker">LET’S GET YOU CONNECTED</span><h2 id="contact-title">A little help.<br><em>A brighter start.</em></h2><p>For account access, class information or enrolment questions, contact your institute administrator. Already have an account? Your workspace is ready.</p></div><a href="#signin" class="editorial-pill liquid-glass">Go to sign in <span aria-hidden="true">↗</span></a></section>
<div class="editorial-signin-intro editorial-width" data-reveal><span class="cinema-kicker">07 / BEGIN YOUR JOURNEY</span><h2>Your world of learning.<br><em>Ready when you are.</em></h2></div>
<section class="signin-section" id="signin" aria-label="Sign in to EduTrack">
<div class="login-card">
    <div class="login-header">
        <div class="atelier-eyebrow">YOUR NEXT CHAPTER STARTS HERE</div>
        <h1>A familiar place.<br><em>A fresh perspective.</em></h1>
        <p class="lead-text">Welcome to your academic world. Your people, your progress, your possibilities.</p>
        <div class="signin-art" aria-hidden="true">✦</div>
        <div class="quote">Education opens doors.<br>Let's open yours.</div>
    </div>
    <div class="card-body">
        <div class="welcome-logo"><i class="bi bi-book-half"></i></div>
        <h2>Welcome back.</h2>
        <p class="form-sub">Sign in to continue to EduTrack</p>

        <c:if test="${not empty param.error}"><div class="alert alert-warning py-2 small"><c:choose><c:when test="${param.error == 'auth'}">Please sign in to continue.</c:when><c:when test="${param.error == 'loggedout'}">You have been signed out.</c:when><c:otherwise>${param.error}</c:otherwise></c:choose></div></c:if>
        <c:if test="${not empty flashError}"><div class="alert alert-danger py-2 small">${flashError}</div></c:if>
        <c:if test="${not empty flashSuccess}"><div class="alert alert-success py-2 small">${flashSuccess}</div></c:if>
        <form method="post" action="${pageContext.request.contextPath}/login">
            <input type="hidden" name="csrfToken" value="${csrfToken}">
            <div class="mb-3"><label class="form-label" for="loginUsername">Username</label><div class="input-group"><span class="input-group-text bg-light border-end-0"><i class="bi bi-person"></i></span><input type="text" class="form-control border-start-0" id="loginUsername" name="username" placeholder="Enter your username" required autocomplete="username"></div></div>
            <div class="mb-3"><label class="form-label" for="loginPassword">Password</label><div class="input-group"><span class="input-group-text bg-light border-end-0"><i class="bi bi-lock"></i></span><input type="password" class="form-control border-start-0" id="loginPassword" name="password" placeholder="Enter your password" required autocomplete="current-password"></div></div>
            <div class="d-flex justify-content-between align-items-center mb-3"><label class="small text-muted"><input type="checkbox" class="form-check-input me-1"> Remember me</label><a href="${pageContext.request.contextPath}/forgot-password" class="small">Forgot password?</a></div>
            <button type="submit" class="btn btn-primary w-100">Sign In <i class="bi bi-arrow-right ms-1"></i></button>
        </form>
        <div class="divider-or">OR</div>
        <button type="button" class="btn-google" disabled title="Not configured">
            <svg width="18" height="18" viewBox="0 0 18 18" xmlns="http://www.w3.org/2000/svg"><path fill="#4285F4" d="M17.64 9.2c0-.64-.06-1.25-.16-1.84H9v3.48h4.84a4.14 4.14 0 0 1-1.8 2.72v2.26h2.9c1.7-1.57 2.7-3.88 2.7-6.62z"/><path fill="#34A853" d="M9 18c2.43 0 4.47-.8 5.96-2.18l-2.9-2.26c-.8.54-1.84.86-3.06.86-2.35 0-4.34-1.59-5.05-3.72H.98v2.33A9 9 0 0 0 9 18z"/><path fill="#FBBC05" d="M3.95 10.7A5.4 5.4 0 0 1 3.67 9c0-.59.1-1.16.28-1.7V4.97H.98A9 9 0 0 0 0 9c0 1.45.35 2.83.98 4.03l2.97-2.33z"/><path fill="#EA4335" d="M9 3.58c1.32 0 2.51.46 3.44 1.35l2.58-2.58C13.46.89 11.43 0 9 0A9 9 0 0 0 .98 4.97L3.95 7.3C4.66 5.17 6.65 3.58 9 3.58z"/></svg>
            Sign in with Google
        </button>

        <div class="text-center mt-4 small text-muted"><i class="bi bi-shield-check me-1"></i> Your account is protected by EduTrack access controls.</div>
    </div>
    <div class="card-footer small text-muted">© 2026 EduTrack Sri Lanka. All rights reserved. · <a href="${pageContext.request.contextPath}/dashboard">Bright Future Through Education</a></div>
</div>
</section>
</main>
<footer class="atelier-footer"><a class="atelier-brand" href="#top"><span class="brand-seal">E</span> EduTrack</a><span>Built around education. Inspired by possibility.</span><a href="#top">Back to top ↑</a></footer>
</body>
</html>









