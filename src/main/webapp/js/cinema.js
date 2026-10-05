// Decorative pointer spotlight, scoped to pricing cards.
(function () {
    var reduced = window.matchMedia('(prefers-reduced-motion: reduce)');
    document.querySelectorAll('[data-pricing-card]').forEach(function (card) {
        card.addEventListener('pointermove', function (event) {
            if (event.pointerType === 'touch' || reduced.matches || document.body.classList.contains('motion-paused')) return;
            var bounds = card.getBoundingClientRect();
            card.style.setProperty('--spot-x', (event.clientX - bounds.left) + 'px');
            card.style.setProperty('--spot-y', (event.clientY - bounds.top) + 'px');
        });
        card.addEventListener('pointerleave', function () {
            card.style.setProperty('--spot-x', '-9999px');
            card.style.setProperty('--spot-y', '-9999px');
        });
    });
}());
// Native looping background; display controls never touch application forms.
(function () {
    var video = document.getElementById('cinemaVideo');
    var button = document.getElementById('cinemaMotion');
    if (!video || !button) return;
    var reduced = window.matchMedia('(prefers-reduced-motion: reduce)');
    var paused = false;
    function sync() {
        var stop = paused || reduced.matches || document.hidden;
        document.body.classList.toggle('motion-paused', paused || reduced.matches);
        button.setAttribute('aria-pressed', String(paused || reduced.matches));
        button.textContent = reduced.matches ? 'Reduced motion on' : paused ? 'Resume motion ▷' : 'Pause motion Ⅱ';
        button.disabled = reduced.matches;
        if (stop) video.pause();
        else { video.muted = true; var play = video.play(); if(play) play.catch(function () { paused = true; sync(); }); }
    }
    button.addEventListener('click', function () { paused = !paused; sync(); });
    reduced.addEventListener('change', sync);
    document.addEventListener('visibilitychange', sync);
    window.addEventListener('pagehide', function () { video.pause(); });
    window.addEventListener('pageshow', sync);
    sync();
}());

// Native smooth navigation preserves wheel/touch control and reduced motion.
document.addEventListener('click', function (event) {
    var link = event.target.closest('a[href^="#"]');
    if (!link || event.defaultPrevented || event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) return;
    var target = document.getElementById(link.getAttribute('href').slice(1));
    if (!target) return;
    event.preventDefault();
    var reduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    var original = target.getAttribute('tabindex');
    if (original === null) target.setAttribute('tabindex', '-1');
    target.focus({ preventScroll: true });
    if (original === null) target.addEventListener('blur', function () { target.removeAttribute('tabindex'); }, { once: true });
    history.pushState(null, '', link.getAttribute('href'));
    target.scrollIntoView({ behavior: reduced ? 'instant' : 'smooth', block: 'start' });
});

// Only play supporting films while visible; honour the existing motion switch.
(function () {
    var videos = Array.from(document.querySelectorAll('[data-editorial-video]'));
    if (!videos.length) return;
    var reduced = window.matchMedia('(prefers-reduced-motion: reduce)');
    var visible = new Set();
    function sync() {
        videos.forEach(function (video) {
            if (visible.has(video) && !reduced.matches && !document.hidden && !document.body.classList.contains('motion-paused')) {
                video.muted = true;
                var promise = video.play();
                if (promise) promise.catch(function () { /* Keep readable cards when playback is unavailable. */ });
            } else video.pause();
        });
    }
    if ('IntersectionObserver' in window) {
        var observer = new IntersectionObserver(function (entries) {
            entries.forEach(function (entry) { if (entry.isIntersecting) visible.add(entry.target); else visible.delete(entry.target); });
            sync();
        }, { threshold: .12 });
        videos.forEach(function (video) { observer.observe(video); });
    }
    new MutationObserver(sync).observe(document.body, { attributes: true, attributeFilter: ['class'] });
    reduced.addEventListener('change', sync);
    document.addEventListener('visibilitychange', sync);
    window.addEventListener('pagehide', function () { videos.forEach(function (video) { video.pause(); }); });
    window.addEventListener('pageshow', sync);
}());

// Accessible disclosure navigation: links, Escape and outside clicks dismiss it.
(function () {
    var menu = document.querySelector('.feature-menu');
    if (!menu) return;
    document.addEventListener('click', function (event) {
        if (!menu.contains(event.target) || event.target.closest('a')) menu.open = false;
    });
    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && menu.open) {
            menu.open = false;
            menu.querySelector('summary').focus();
        }
    });
}());

// Presentation-only educator search and compact hero navigation.
(function () {
    var input = document.getElementById('teacher-search');
    var cards = Array.from(document.querySelectorAll('[data-educator]'));
    if (input) input.addEventListener('input', function () {
        var query = input.value.trim().toLocaleLowerCase();
        var count = 0;
        cards.forEach(function (card) {
            card.hidden = !card.textContent.toLocaleLowerCase().includes(query);
            if (!card.hidden) count++;
        });
        document.getElementById('teacher-count').textContent = count + (count === 1 ? ' educator' : ' educators');
        document.querySelector('.educator-empty').hidden = count !== 0;
    });
    var button = document.querySelector('.header-menu-button');
    var nav = document.getElementById('hero-navigation');
    if (!button || !nav) return;
    document.body.classList.add('nav-ready');
    function close() { nav.classList.remove('is-open'); button.setAttribute('aria-expanded', 'false'); }
    button.addEventListener('click', function () {
        var open = nav.classList.toggle('is-open');
        button.setAttribute('aria-expanded', String(open));
    });
    nav.addEventListener('click', function (event) { if (event.target.closest('a')) close(); });
    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && nav.classList.contains('is-open')) { close(); button.focus(); }
    });
    document.addEventListener('click', function (event) { if (!nav.contains(event.target) && !button.contains(event.target)) close(); });
}());
