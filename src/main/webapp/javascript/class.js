function updateUIBasedOnRole(role) {
    const sidebar = document.querySelector('.side-bar');
    const editButtons = document.querySelectorAll('.edit-btn');
    const deleteButton = document.querySelector('.delete-class-btn');


    if (role === 'admin') {
        sidebar.classList.remove('yellow', 'red');
        sidebar.classList.add('green');
        editButtons.forEach(button => button.style.display = 'block');
        deleteButton.style.display = 'block';
    } else if (role === 'teacher') {
        sidebar.classList.remove('green', 'red');
        sidebar.classList.add('yellow');
        editButtons.forEach(button => button.style.display = 'none');
        deleteButton.style.display = 'none';
    } else if (role === 'student') {
        sidebar.classList.remove('green', 'yellow');
        sidebar.classList.add('red');
        editButtons.forEach(button => button.style.display = 'none');
        deleteButton.style.display = 'none';
    }
}

document.addEventListener('DOMContentLoaded', function () {
    const urlParams = new URLSearchParams(window.location.search);
    const classId = urlParams.get('classId');
    const role = sessionStorage.getItem('role');
    const welcomeMessage = document.querySelector('.user-info span');
    const personName = sessionStorage.getItem('personName');

    console.log('Role:', role);

    if (personName) {
        welcomeMessage.textContent = `${personName}`;
    }

    updateUIBasedOnRole(role);

    function fetchClassDetails(classId) {
        fetch(`api/fetchClassDetails?classId=${classId}`)
            .then(response => response.json())
            .then(data => {
                displayClassDetails(data);
            })
            .catch(error => console.error('Error fetching class details:', error));
    }

    function displayClassDetails(data) {
        const studentsList = document.getElementById('students-list');
        const lessonsList = document.getElementById('lessons-list');
        const workloadHours = document.querySelector('.workload-hours');

        studentsList.innerHTML = '';
        data.students.forEach(student => {
            const li = document.createElement('li');
            li.innerHTML = `<span>${student.person_name}</span>
                                <button class="profile-btn" data-person-id="${student.person_id}">Profile</button>`;
            studentsList.appendChild(li);
        });

        lessonsList.innerHTML = '';
        data.lessons.forEach(lesson => {
            const li = document.createElement('li');
            li.innerHTML = `<span>${lesson.lesson_name}</span> <button class="see-lesson-btn" data-lesson-id="${lesson.lesson_id}" data-lesson-name="${lesson.lesson_name}" data-lesson-description="${lesson.lesson_description}" data-teacher-name="${lesson.teacher_name}">See lesson</button>`;
            lessonsList.appendChild(li);
        });

        workloadHours.textContent = data.workload;
    }

    if (classId) {
        fetchClassDetails(classId);
    } else {
        console.error('Class ID not found in URL');
    }

    document.getElementById('edit-lessons-btn').addEventListener('click', function() {
        window.location.href = `lesson_add.html?classId=${classId}`;
    });

    document.querySelector('.edit-btn').addEventListener('click', function() {
        window.location.href = `edit_students.html?classId=${classId}`;
    });

    document.querySelector('.delete-class-btn').addEventListener('click', function() {
        let classIdInt = parseInt(classId);
        if (confirm('Are you sure you want to delete this class?')) {
            fetch('api/deleteClass', {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ classId: classIdInt })
            })
                .then(response => response.json())
                .then(result => {
                    if (result.success) {
                        alert('Class deleted successfully.');
                        window.location.href = 'classes.html';
                    } else {
                        alert('Error deleting class');
                    }
                })
                .catch(error => console.error('Error deleting class:', error));
        }
    });

    document.getElementById('lessons-list').addEventListener('click', function(event) {
        if (event.target.classList.contains('see-lesson-btn')) {
            const lessonId = event.target.getAttribute('data-lesson-id');
            const lessonName = event.target.getAttribute('data-lesson-name');
            const lessonDescription = event.target.getAttribute('data-lesson-description');
            const teacherName = event.target.getAttribute('data-teacher-name');
            window.location.href = `specificlesson.html?lessonId=${lessonId}&lessonName=${lessonName}&lessonDescription=${lessonDescription}&teacherName=${teacherName}`;
        }
    });

    document.getElementById('students-list').addEventListener('click', function(event) {
        if (event.target.classList.contains('profile-btn')) {
            const personId = event.target.getAttribute('data-person-id');
            window.location.href = `profile.html?personId=${personId}`;
        }
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