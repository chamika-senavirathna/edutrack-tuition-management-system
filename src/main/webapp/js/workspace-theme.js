// Local appearance preference only; no network requests or account changes.
(function () {
    var key = 'edutrack-workspace-theme';
    var theme = 'dark';
    try { if (localStorage.getItem(key) === 'light') theme = 'light'; } catch (_) {}
    document.documentElement.setAttribute('data-workspace-theme', theme);
    document.addEventListener('DOMContentLoaded', function () {
        var button = document.getElementById('workspaceThemeToggle');
        if (!button) return;
        function apply() {
            document.documentElement.setAttribute('data-workspace-theme', theme);
            button.setAttribute('aria-pressed', String(theme === 'dark'));
            button.querySelector('.theme-label').textContent = theme === 'dark' ? 'Dark' : 'Light';
            button.title = theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode';
        }
        apply();
        button.addEventListener('click', function () {
            theme = theme === 'dark' ? 'light' : 'dark';
            try { localStorage.setItem(key, theme); } catch (_) {}
            apply();
        });
    });
}());