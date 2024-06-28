
document.addEventListener("DOMContentLoaded", ()=>{
    const role = sessionStorage.getItem('role');
    const sidebar = document.querySelector('.side-bar');

    sidebar.classList.remove('red', 'yellow', 'green');
    if (role === 'admin') {
        sidebar.classList.add('green');
    } else if (role === 'teacher') {
        sidebar.classList.add('yellow');
    } else if (role === 'student') {
        sidebar.classList.add('red');
    }

    const logoutBtn = document.querySelector(".log-out");
    function logoutAndRedirect() {
        console.log("baba")
        sessionStorage.clear();

        var baseUrl = window.location.origin + "/SomtodayHomeworkPlanner_war/";
        window.location.href = baseUrl;
    }

    logoutBtn.addEventListener("click", logoutAndRedirect)

})