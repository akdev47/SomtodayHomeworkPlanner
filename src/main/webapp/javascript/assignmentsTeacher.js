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

    sidebar.classList.remove('red', 'yellow', 'green'); // Remove any existing color classes
    if (role === 'admin') {
        console.log('Setting sidebar color to green for admin');
        console.log('Admin - add assignment button removed');
        sidebar.classList.add('green');
        addBtn.classList.add('hidden');
    } else if (role === 'teacher') {
        console.log('Setting sidebar color to yellow for teacher');
        sidebar.classList.add('yellow');
    } else if (role === 'student') {
        console.log('Setting sidebar color to red for student');
        console.log('Student - add assignment button removed');
        sidebar.classList.add('red');
        addBtn.classList.add('hidden');
    }

    if (role && personId) {
        fetch(`api/fetchAssignments?role=${role}&personId=${personId}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! Status: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                console.log('Fetched data:', data);
                const assignments = data.homeworks;
                const splits = data.splits;
                const assignmentContainer = document.getElementById('table-body');
                assignmentContainer.innerHTML = '';

                if (Array.isArray(assignments)) {
                    assignments.forEach(assignmentData => {
                        const row = document.createElement('tr');

                        const className = document.createElement('td');
                        className.innerText = assignmentData.class_name;

                        const homeworkType = document.createElement('td');
                        homeworkType.innerText = assignmentData.homework_name;

                        const publishDate = document.createElement('td');
                        publishDate.innerText = assignmentData.publish_date;

                        const dueDate = document.createElement('td');
                        dueDate.innerText = assignmentData.due_date;

                        const detailsButton = document.createElement('td');
                        const detailsLink = document.createElement('a');
                        if (role === 'student') {
                            detailsLink.href = `assignment-details-student.html?id=${assignmentData.homework_id}`;
                        } else {
                            detailsLink.href = `assignments-of-class.html?homeworkId=${assignmentData.homework_id}`;
                        }
                        detailsLink.innerHTML = 'See Details';
                        detailsButton.appendChild(detailsLink);

                        row.appendChild(className);
                        row.appendChild(homeworkType);
                        row.appendChild(publishDate);
                        row.appendChild(dueDate);
                        row.appendChild(detailsButton);

                        assignmentContainer.appendChild(row);
                    });
                } else {
                    console.error('Assignments data is missing or not an array');
                }

                if (Array.isArray(splits)) {
                    splits.forEach(splitData => {
                        const correspondingHomework = assignments.find(hw => hw.homework_id === splitData.homework_id);
                        if (correspondingHomework) {
                            const row = document.createElement('tr');
                            row.innerHTML = `
                                    <td>${correspondingHomework.class_name}</td>
                                    <td>${correspondingHomework.homework_name} ${splitData.split_name}</td>
                                    <td>${correspondingHomework.publish_date}</td>
                                    <td>${correspondingHomework.due_date}</td>
                                    <td><a href="assignment-split-details.html?splitId=${splitData.split_id}&homeworkId=${splitData.homework_id}">See Details</a></td>
                                `;
                            assignmentContainer.appendChild(row);
                        }
                    });
                } else {
                    console.error('Splits data is missing or not an array');
                }
            })
            .catch(error => console.error('Error fetching assignments:', error));
    } else {
        console.error('Missing role or personId in session storage');
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