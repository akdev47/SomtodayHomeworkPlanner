
document.addEventListener("DOMContentLoaded", ()=>{
    const role = sessionStorage.getItem('role');
    const sidebar = document.querySelector('.side-bar');
    const personId = sessionStorage.getItem('personId');

    sidebar.classList.remove('red', 'yellow', 'green');
    if (role === 'admin') {
        sidebar.classList.add('green');
    } else if (role === 'teacher') {
        sidebar.classList.add('yellow');
    } else if (role === 'student') {
        sidebar.classList.add('red');
    }

    if (personId) {
        profileLink.href = `profile.html?personId=${personId}`;
    }

    const logoutBtn = document.querySelector(".log-out");
    function logoutAndRedirect() {
        sessionStorage.clear();

        const ctx = "/" + window.location.pathname.split("/")[1];
        window.location.href = `${window.location.origin}${ctx}/login.html`;
    }

    logoutBtn.addEventListener("click", logoutAndRedirect)

})