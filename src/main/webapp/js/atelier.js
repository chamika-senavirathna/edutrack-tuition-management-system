// Presentation only. Forms, requests and business validation are untouched.
document.addEventListener('DOMContentLoaded', function () {
    var motion = window.matchMedia('(prefers-reduced-motion: reduce)');
    var targets = document.querySelectorAll('[data-reveal]');
    if (!motion.matches && 'IntersectionObserver' in window) {
        var observer = new IntersectionObserver(function (entries) {
            entries.forEach(function (entry) {
                if (entry.isIntersecting) {
                    entry.target.classList.add('is-visible');
                    observer.unobserve(entry.target);
                }
            });
        }, { threshold: 0.08 });
        targets.forEach(function (target) { target.classList.add('will-reveal'); observer.observe(target); });
    }
    var scene = document.querySelector('.atelier-scene');
    if (scene && window.matchMedia('(hover: hover)').matches) {
        scene.addEventListener('pointermove', function (event) {
            if (motion.matches) return;
            var rect = scene.getBoundingClientRect();
            scene.style.setProperty('--tilt-x', ((event.clientY - rect.top) / rect.height - 0.5) * -12 + 'deg');
            scene.style.setProperty('--tilt-y', ((event.clientX - rect.left) / rect.width - 0.5) * 16 + 'deg');
        });
        scene.addEventListener('pointerleave', function () {
            scene.style.setProperty('--tilt-x', '0deg');
            scene.style.setProperty('--tilt-y', '0deg');
        });
    }
    // Keep the existing mobile menu toggle; add dismissal and accessible state.
    var sidebar = document.getElementById('etSidebar');
    var toggle = document.getElementById('sidebarToggle');
    if (sidebar && toggle) {
        toggle.setAttribute('aria-controls', 'etSidebar');
        toggle.setAttribute('aria-expanded', 'false');
        new MutationObserver(function () {
            toggle.setAttribute('aria-expanded', String(sidebar.classList.contains('open')));
        }).observe(sidebar, { attributes: true, attributeFilter: ['class'] });
        document.addEventListener('keydown', function (event) {
            if (event.key === 'Escape' && sidebar.classList.contains('open')) { sidebar.classList.remove('open'); toggle.focus(); }
        });
        document.addEventListener('click', function (event) {
            if (!sidebar.contains(event.target) && !toggle.contains(event.target)) sidebar.classList.remove('open');
        });
    }
});

// Native account disclosure: dismiss without intercepting navigation or sign-out.
document.addEventListener('DOMContentLoaded', function () {
    var menu = document.querySelector('.account-menu');
    if (!menu) return;
    document.addEventListener('click', function (event) {
        if (!menu.contains(event.target) || event.target.closest('.account-menu-panel a')) menu.open = false;
    });
    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && menu.open) {
            menu.open = false;
            menu.querySelector('summary').focus();
        }
    });
});
