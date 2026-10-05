// EduTrack Sri Lanka - small progressive-enhancement helpers.
// Business validation and authorization remain server-side.

document.addEventListener('DOMContentLoaded', function () {
    // Header date + time-of-day greeting (display-only, purely client side).
    var dateEl = document.getElementById('etTodayDate');
    var greetEl = document.getElementById('etGreeting');
    if (dateEl) {
        dateEl.textContent = new Date().toLocaleDateString(undefined, { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });
    }
    if (greetEl) {
        var hour = new Date().getHours();
        greetEl.textContent = hour < 12 ? 'Good Morning!' : (hour < 17 ? 'Good Afternoon!' : 'Good Evening!');
    }

    var toggle = document.getElementById('sidebarToggle');
    var sidebar = document.getElementById('etSidebar');

    if (toggle && sidebar) {
        toggle.addEventListener('click', function () {
            sidebar.classList.toggle('open');
        });
    }

    // Highlight the current section without changing server-side navigation.
    if (sidebar) {
        var currentPath = window.location.pathname.replace(/\/$/, '');
        sidebar.querySelectorAll('a.nav-link').forEach(function (link) {
            var href = link.getAttribute('href');
            if (!href || href.indexOf('javascript:') === 0) return;
            try {
                var linkPath = new URL(href, window.location.origin).pathname.replace(/\/$/, '');
                if (linkPath === currentPath) link.classList.add('active');
            } catch (e) { /* Ignore malformed optional links. */ }
        });

        sidebar.querySelectorAll('a.nav-link').forEach(function (link) {
            link.addEventListener('click', function () {
                if (window.innerWidth <= 991) sidebar.classList.remove('open');
            });
        });
    }

    document.querySelectorAll('form[data-confirm]').forEach(function (form) {
        form.addEventListener('submit', function (event) {
            if (!window.confirm(form.getAttribute('data-confirm'))) event.preventDefault();
        });
    });

    document.querySelectorAll('.et-flash .alert-success').forEach(function (alertEl) {
        setTimeout(function () {
            var bs = window.bootstrap && bootstrap.Alert ? bootstrap.Alert.getOrCreateInstance(alertEl) : null;
            if (bs) bs.close(); else alertEl.style.display = 'none';
        }, 5000);
    });
});
