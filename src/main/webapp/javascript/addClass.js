
document.addEventListener('DOMContentLoaded', function() {
    const sidebar = document.getElementById('sidebar');
    const role = sessionStorage.getItem('role');
    const personId = sessionStorage.getItem('personId');

    if (personId) {
        profileLink.href = `profile.html?personId=${personId}`;
    }

    sidebar.classList.remove('red', 'yellow', 'green');
    if (role === 'admin') {
        sidebar.classList.add('green');
    } else if (role === 'teacher') {
        sidebar.classList.add('yellow');
    } else {
        sidebar.classList.add('red');
    }

    console.log("add class test");

    fetch('api/fetchStudents?unassigned=true')
        .then(response => response.json())
        .then(students => {
            const allStudentsSelect = document.getElementById('all-students-select');
            students.forEach(student => {
                const option = document.createElement('option');
                option.value = student.student_id;
                option.textContent = student.person_name;
                allStudentsSelect.appendChild(option);
            });
        })
        .catch(error => console.error('Error fetching students:', error));

    document.getElementById('add-student-btn').addEventListener('click', function() {
        const allStudentsSelect = document.getElementById('all-students-select');
        const currentStudentsSelect = document.getElementById('current-students-select');
        Array.from(allStudentsSelect.selectedOptions).forEach(option => {
            currentStudentsSelect.appendChild(option);
        });
    });

    const logoutBtn = document.querySelector(".log-out");
    function logoutAndRedirect() {
        console.log("baba")
        sessionStorage.clear();

        var baseUrl = window.location.origin + "/SomtodayHomeworkPlanner_war/";
        window.location.href = baseUrl;
    }

    logoutBtn.addEventListener("click", logoutAndRedirect)
});