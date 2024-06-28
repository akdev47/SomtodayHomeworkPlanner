document.addEventListener('DOMContentLoaded', function() {
    const role = sessionStorage.getItem('role');
    const personId = sessionStorage.getItem('personId');
    const personName = sessionStorage.getItem('personName');
    const lessonsTableBody = document.querySelector('.table tbody');
    const sidebar = document.querySelector('.side-bar');
    const welcomeMessage = document.querySelector('.user-info span');

    if (personName) {
        welcomeMessage.textContent = ` ${personName}`;
    }

    if (personId) {
        profileLink.href = `profile.html?personId=${personId}`;
    }

    // sidebar color based on role
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

    function fetchLessons() {
        fetch(`api/fetchLessonsPage?role=${role}&personId=${personId}`)
            .then(response => response.json())
            .then(lessons => {
                lessonsTableBody.innerHTML = '';
                lessons.forEach(lesson => {
                    const row = document.createElement('tr');
                    row.innerHTML = `
                            <td>${lesson.class_name}</td>
                            <td>${lesson.lesson_name}</td>
                            <td>${lesson.teacher_name}</td>
                            <td><button class="details-button" data-lesson-id="${lesson.lesson_id}" data-class-name="${lesson.class_name}" data-lesson-name="${lesson.lesson_name}" data-lesson-description="${lesson.lesson_description}" data-teacher-name="${lesson.teacher_name}">See Details</button></td>
                        `;
                    lessonsTableBody.appendChild(row);
                });

                document.querySelectorAll('.details-button').forEach(button => {
                    button.addEventListener('click', function() {
                        const lessonId = this.getAttribute('data-lesson-id');
                        const className = this.getAttribute('data-class-name');
                        const lessonName = this.getAttribute('data-lesson-name');
                        const lessonDescription = this.getAttribute('data-lesson-description');
                        const teacherName = this.getAttribute('data-teacher-name');

                        window.location.href = `specificlesson.html?lessonId=${lessonId}&className=${encodeURIComponent(className)}&lessonName=${encodeURIComponent(lessonName)}&lessonDescription=${encodeURIComponent(lessonDescription)}&teacherName=${encodeURIComponent(teacherName)}`;
                    });
                });
            })
            .catch(error => console.error('Error fetching lessons:', error));
    }

    fetchLessons();

    const logoutBtn = document.querySelector(".log-out");
    function logoutAndRedirect() {
        console.log("baba")
        sessionStorage.clear();

        var baseUrl = window.location.origin + "/SomtodayHomeworkPlanner_war/";
        window.location.href = baseUrl;
    }

    logoutBtn.addEventListener("click", logoutAndRedirect)
});