document.addEventListener('DOMContentLoaded', function() {
    const role = sessionStorage.getItem('role');
    const personId = sessionStorage.getItem('personId');
    const sidebar = document.querySelector('.side-bar');
    const addBtn = document.getElementById("add-button");
    const welcomeMessage = document.querySelector('.user-info span');
    const personName = sessionStorage.getItem('personName');

    if (personName) {
        welcomeMessage.textContent = ` ${personName}`;
    }

    if (personId) {
        profileLink.href = `profile.html?personId=${personId}`;
    }

    // Get homeworkId from URL parameters
    const urlParams = new URLSearchParams(window.location.search);
    const homeworkId = urlParams.get('homeworkId');

    sidebar.classList.remove('red', 'yellow', 'green'); // Remove any existing color classes
    if (role === 'admin') {
        console.log('Setting sidebar color to green for admin');
        console.log('Admin - add assignment button removed');
        sidebar.classList.add('green');
        addBtn.classList.add(`hidden`);
    } else if (role === 'teacher') {
        console.log('Setting sidebar color to yellow for teacher');
        sidebar.classList.add('yellow');
    } else if (role === 'student') {
        console.log('Setting sidebar color to red for student');
        console.log('Student - add assignment button removed');
        sidebar.classList.add('red');
        addBtn.classList.add(`hidden`);
    }

    if (role && personId && homeworkId) {
        fetch(`api/fetchAssignmentsOfClass?role=${role}&personId=${personId}&homeworkId=${homeworkId}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! Status: ${response.status}`);
                }
                return response.json();
            })
            .then(assignments => {
                const assignmentContainer = document.getElementById('table-body');
                assignmentContainer.innerHTML = '';

                // Sort assignments to have teacher's homeworks and splits at the top
                assignments.sort((a, b) => b.is_teacher - a.is_teacher);

                assignments.forEach(assignmentData => {
                    const row = document.createElement('tr');

                    const studentName = document.createElement('td');
                    studentName.innerText = assignmentData.student_name;

                    const assignmentName = document.createElement('td');
                    assignmentName.innerText = assignmentData.homework_name;

                    const publishDate = document.createElement('td');
                    publishDate.innerText = assignmentData.publish_date;

                    const dueDate = document.createElement('td');
                    dueDate.innerText = assignmentData.due_date;

                    const detailsButton = document.createElement('td');
                    const detailsLink = document.createElement('a');
                    if (!assignmentData.is_teacher) {
                        detailsLink.href = assignmentData.split_id ? `assignment-split-details.html?splitId=${assignmentData.split_id}&homeworkId=${assignmentData.homework_id}` : `assignment-details-student.html?id=${assignmentData.homework_id}`;
                        detailsLink.style.backgroundColor = "#DB133D";
                        detailsLink.style.color = "#fff";
                    } else {
                        detailsLink.href = assignmentData.split_id ? `assignment-split-details.html?splitId=${assignmentData.split_id}&homeworkId=${assignmentData.homework_id}` : `assignment-details-teacher.html?id=${assignmentData.homework_id}`;
                    }
                    detailsLink.innerText = 'See Details';
                    detailsButton.appendChild(detailsLink);

                    row.appendChild(studentName);
                    row.appendChild(assignmentName);
                    row.appendChild(publishDate);
                    row.appendChild(dueDate);
                    row.appendChild(detailsButton);

                    assignmentContainer.appendChild(row);
                });
            })
            .catch(error => console.error('Error fetching assignments:', error));
    } else {
        console.error('Missing role, personId, or homeworkId in session storage or URL parameters');
    }

    const logoutBtn = document.querySelector(".log-out");
    function logoutAndRedirect() {
        console.log("baba")
        sessionStorage.clear();

        var baseUrl = window.location.origin + "/SomtodayHomeworkPlanner_war/";
        window.location.href = baseUrl;
    }

    logoutBtn.addEventListener("click", logoutAndRedirect)
});

