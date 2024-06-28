
document.addEventListener('DOMContentLoaded', function() {
    const sidebar = document.getElementById('sidebar');
    const role = sessionStorage.getItem('role');
    const personId = sessionStorage.getItem('personId');
    const welcomeMessage = document.querySelector('.user-info span');
    const personName = sessionStorage.getItem('personName');

    if (personName) {
        welcomeMessage.textContent = ` ${personName}`;
    }

    sidebar.classList.remove('red', 'yellow', 'green');
    if (role === 'admin') {
        sidebar.classList.add('green');
    } else if (role === 'teacher') {
        sidebar.classList.add('yellow');
    } else {
        sidebar.classList.add('red');
    }

    const backButton = document.querySelector('.back-button button');
    const classId = new URLSearchParams(window.location.search).get('classId');

    backButton.addEventListener('click', function() {
        window.location.href = `class.html?classId=${classId}`;
    });

    // fetch all unassigned students and show them
    function fetchUnassignedStudents() {
        fetch('api/fetchStudents?unassigned=true')
            .then(response => response.json())
            .then(students => {
                const allStudentsSelect = document.getElementById('all-students-select');
                allStudentsSelect.innerHTML = ''; // Clear the list before adding new options
                students.forEach(student => {
                    const option = document.createElement('option');
                    option.value = student.student_id;
                    option.textContent = student.person_name;
                    allStudentsSelect.appendChild(option);
                });
            })
            .catch(error => console.error('Error fetching students:', error));
    }

    function fetchClassDetails(classId) {
        fetch(`api/fetchClassDetails?classId=${classId}`)
            .then(response => response.json())
            .then(data => {
                displayClassDetails(data);
            })
            .catch(error => console.error('Error fetching class details:', error));
    }

    function displayClassDetails(data) {
        const currentStudentsSelect = document.getElementById('current-students-select');

        // to view current students in class
        currentStudentsSelect.innerHTML = '';
        data.students.forEach(student => {
            const option = document.createElement('option');
            option.value = student.student_id;
            option.textContent = student.person_name;
            currentStudentsSelect.appendChild(option);
        });
    }

    if (classId) {
        fetchClassDetails(classId);
    } else {
        console.error('Class ID not found in URL');
    }

    if (personId) {
        profileLink.href = `profile.html?personId=${personId}`;
    }

    fetchUnassignedStudents();

    // add selected students from all students to current students
    document.getElementById('add-student-btn').addEventListener('click', function() {
        const allStudentsSelect = document.getElementById('all-students-select');
        const currentStudentsSelect = document.getElementById('current-students-select');
        Array.from(allStudentsSelect.selectedOptions).forEach(option => {
            currentStudentsSelect.appendChild(option);
        });
    });

    // remove selected students from current students and add them to all students.
    document.getElementById('remove-student-btn').addEventListener('click', function() {
        const allStudentsSelect = document.getElementById('all-students-select');
        const currentStudentsSelect = document.getElementById('current-students-select');
        Array.from(currentStudentsSelect.selectedOptions).forEach(option => {
            allStudentsSelect.appendChild(option);
        });
    });

    // save changes button to submit form
    document.getElementById('edit-students-form').addEventListener('submit', function(event) {
        event.preventDefault();
        const currentStudentsSelect = document.getElementById('current-students-select');
        const studentIds = Array.from(currentStudentsSelect.options).map(option => option.value);

        fetch('api/editStudentsInClass', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                classId: classId,
                studentIds: studentIds
            })
        })
            .then(response => response.json())
            .then(result => {
                if (result.success) {
                    alert('Students updated successfully');
                    window.location.href = `class.html?classId=${classId}`;
                } else {
                    alert('Error updating students');
                }
            })
            .catch(error => console.error('Error updating students:', error));
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