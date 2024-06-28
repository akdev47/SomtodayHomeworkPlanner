document.addEventListener('DOMContentLoaded', function () {
    const urlParams = new URLSearchParams(window.location.search);
    const assignmentId = urlParams.get('id');
    const role = sessionStorage.getItem('role');
    const personId = sessionStorage.getItem('personId');
    const totalGoalCount = document.getElementById('total-goal-count');
    const sidebar = document.querySelector('.side-bar');
    const splitBtn = document.querySelector(".split-btn");

    if (personId) {
        profileLink.href = `profile.html?personId=${personId}`;
    }

    sidebar.classList.remove('red', 'yellow', 'green'); // Remove any existing color classes
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

    function convertTimeToMinutes(timeStr) {
        const [hours, minutes, seconds] = timeStr.split(':').map(Number);
        const totalMinutes = hours * 60 + minutes;
        return totalMinutes;
    }

    function fetchDetails() {
        fetch(`api/fetchAssignmentDetails?id=${assignmentId}&role=${role}&personId=${personId}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! Status: ${response.status}`);
                }
                return response.json();
            })
            .then(assignment => {
                assignment.forEach(assignmentData => {
                    const descText = document.getElementById('description-text');
                    if (descText) {
                        descText.innerText = assignmentData.hw_description;
                    }

                    const heading = document.getElementById('page-heading');
                    if (heading) {
                        heading.innerText = assignmentData.homework_name;
                    }

                    const title = document.querySelector("title");
                    if (title) {
                        title.innerText = assignmentData.homework_name;
                    }

                    const timeIndication = document.querySelector('.time-indication');
                    if (timeIndication) {
                        timeIndication.innerHTML = convertTimeToMinutes(assignmentData.time_indication) + " mins";
                    }

                    if (assignmentData.homework_splitable === false){
                        splitBtn.style.display = "none";
                    }

                })
            })
            .catch(error => console.error('Error fetching assignment details:', error));
    }

    function fetchGoals() {
        const goalsList = document.getElementById("goals-ul");
        fetch(`api/fetchGoals?id=${assignmentId}&role=${role}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! Status: ${response.status}`);
                }
                return response.json();
            })
            .then(goals => {
                let i = 1;
                goals.forEach(goal => {
                    const listItem = document.createElement("li");

                    const goalName = document.createElement("span");
                    goalName.textContent = "Goal " + i + ": " + goal.goal_name;

                    listItem.appendChild(goalName);
                    goalsList.appendChild(listItem);

                    i++; // Increment the goal count
                });
                if (totalGoalCount) {
                    totalGoalCount.textContent = i - 1;
                }
            })
            .catch(error => console.error('Error fetching goals:', error));
    }

    const logoutBtn = document.querySelector(".log-out");
    function logoutAndRedirect() {
        sessionStorage.clear();

        var baseUrl = window.location.origin + "/SomtodayHomeworkPlanner_war/";
        window.location.href = baseUrl;
    }

    logoutBtn.addEventListener("click", logoutAndRedirect)


    if (assignmentId && role && personId) {
        fetchDetails();
        fetchGoals();
    } else {
        console.error('No assignment ID provided in URL');
    }
});
