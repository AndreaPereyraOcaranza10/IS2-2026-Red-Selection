document.addEventListener('DOMContentLoaded', function () {
    const sidebarToggle = document.querySelector('.sidebar-toggle');
    const appShell = document.querySelector('.app-shell');

    if (sidebarToggle && appShell) {
        sidebarToggle.addEventListener('click', () => {
            appShell.classList.toggle('sidebar-collapsed');
        });
    }
});
