document.addEventListener('DOMContentLoaded', function() {
    const role = sessionStorage.getItem('role');
    const personId = sessionStorage.getItem('personId');
    const personName = sessionStorage.getItem('personName');
    const welcomeMessage = document.querySelector('.user-info span');
    const sidebar = document.querySelector('.side-bar');

    console.log('Role:', role);
    console.log('Person ID:', personId);
    console.log('Person Name:', personName);

    // Set welcome text with the name of the user
    if (personName) {
        welcomeMessage.textContent = `Welcome, ${personName}!`;
    }

    if (personId) {
        profileLink.href = `profile.html?personId=${personId}`;
    }

    // Set sidebar color based on role
    sidebar.classList.remove('red', 'yellow', 'green');
    if (role === 'admin') {
        console.log('Setting sidebar color to green for admin');
        sidebar.classList.add('green');
    } else if (role === 'teacher') {
        console.log('Setting sidebar color to yellow for teacher');
        sidebar.classList.add('yellow');
    } else if (role === 'student') {
        console.log('Setting sidebar color to red for student');
        sidebar.classList.add('red');
    }

    const classId = new URLSearchParams(window.location.search).get('classId');
    document.getElementById('class-id').value = classId;

    // Fetch teachers
    fetchTeachers();

    document.getElementById('lesson-form').addEventListener('submit', function(event) {
        event.preventDefault();
        saveLesson();
    });

    document.getElementById('switch-to-delete-button').addEventListener('click', function() {
        window.location.href = `delete_lesson.html?classId=${classId}`;
    });

});

function fetchTeachers() {
    fetch('api/fetchAllTeachers')
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response error');
            }
            return response.json();
        })
        .then(teachers => {
            const teacherSelect = document.getElementById('lesson-teacher');
            teachers.forEach(teacher => {
                const option = document.createElement('option');
                option.value = teacher.teacher_id;
                option.textContent = teacher.person_name;
                teacherSelect.appendChild(option);
            });
        })
        .catch(error => console.error('Error fetching teachers:', error));
}

function saveLesson() {
    const lessonName = document.getElementById('lesson-name').value;
    const lessonDescription = document.getElementById('description').value;
    const teacherId = document.getElementById('lesson-teacher').value;
    const classId = document.getElementById('class-id').value;

    const lessonData = {
        lesson_name: lessonName,
        lesson_description: lessonDescription,
        teacher_id: teacherId,
        class_id: classId
    };

    fetch('api/addLesson', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(lessonData)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response error');
            }
            return response.json();
        })
        .then(result => {
            if (result.success) {
                alert('Lesson added successfully!');
                window.location.href = `class.html?classId=${classId}`;
            } else {
                alert('Error adding lesson.');
            }
        })
        .catch(error => console.error('Error saving lesson:', error));
}

const logoutBtn = document.querySelector(".log-out");
function logoutAndRedirect() {
    console.log("baba")
    sessionStorage.clear();

    var baseUrl = window.location.origin + "/SomtodayHomeworkPlanner_war/";
    window.location.href = baseUrl;
}

logoutBtn.addEventListener("click", logoutAndRedirect)
