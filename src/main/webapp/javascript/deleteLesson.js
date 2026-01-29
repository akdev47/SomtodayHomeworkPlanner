document.addEventListener('DOMContentLoaded', function() {
    const lessonSelect = document.getElementById('lesson-select');
    const lessonNameSpan = document.querySelector('.lesson-name');
    const deleteLessonBtn = document.getElementById('delete-lesson-btn');
    const classId = new URLSearchParams(window.location.search).get('classId');

    const role = sessionStorage.getItem('role');
    const sidebar = document.querySelector('.side-bar');
    const personId = sessionStorage.getItem('personId');

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

    function fetchLessons() {
        fetch(`api/fetchLessons?classId=${classId}`)
            .then(response => response.json())
            .then(lessons => {
                populateLessonSelect(lessons);
            })
            .catch(error => console.error('Error fetching lessons:', error));
    }

    function populateLessonSelect(lessons) {
        lessonSelect.innerHTML = '<option value="">Select a lesson</option>';
        lessons.forEach(lesson => {
            const option = document.createElement('option');
            option.value = lesson.lesson_id;
            option.textContent = lesson.lesson_name;
            lessonSelect.appendChild(option);
        });
    }

    lessonSelect.addEventListener('change', function() {
        const selectedOption = lessonSelect.options[lessonSelect.selectedIndex];
        if (selectedOption.value) {
            lessonNameSpan.textContent = selectedOption.text;
        } else {
            lessonNameSpan.textContent = '';
        }
    });

    deleteLessonBtn.addEventListener('click', function() {
        const lessonId = lessonSelect.value;
        if (lessonId) {
            if (confirm('Are you sure you want to delete this lesson?')) {
                deleteLesson(lessonId);
            }
        } else {
            showToast('Please select a lesson to delete.',"error",5000);
        }
    });

    function deleteLesson(lessonId) {
        fetch('api/deleteLesson', {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ lesson_id: lessonId })
        })
            .then(response => response.json())
            .then(result => {
                if (result.success) {
                    showToast('Lesson deleted successfully!',"success",5000);
                    window.location.href = `class.html?classId=${classId}`;
                    fetchLessons();
                    lessonNameSpan.textContent = '';
                } else {
                    showToast('Error deleting lesson.',"error",);
                }
            })
            .catch(error => {
                console.error('Error deleting lesson:', error);
                showToast('Error deleting lesson.',"error",5000);
            });
    }

    fetchLessons();

    const logoutBtn = document.querySelector(".log-out");
    function logoutAndRedirect() {
        sessionStorage.clear();

        const ctx = "/" + window.location.pathname.split("/")[1];
        window.location.href = `${window.location.origin}${ctx}/login.html`;
    }

    logoutBtn.addEventListener("click", logoutAndRedirect)

});