
document.addEventListener('DOMContentLoaded', function () {
    const urlParams = new URLSearchParams(window.location.search);
    const assignmentId = urlParams.get('id');
    const role = sessionStorage.getItem('role');
    const personId = sessionStorage.getItem('personId');
    const totalGoalCount = document.getElementById('total-goal-count');
    const sidebar = document.querySelector('.side-bar');
    const deleteBtn = document.querySelector('.delete-assignment-btn');
    const splitBtn = document.querySelector(".split-btn");
    sidebar.classList.remove('red', 'yellow', 'green'); // Remove any existing color classes
    const welcomeMessage = document.querySelector('.user-info span');
    const personName = sessionStorage.getItem('personName');

    if (personName) {
        welcomeMessage.textContent = ` ${personName}`;
    }

    if (personId) {
        profileLink.href = `profile.html?personId=${personId}`;
    }

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
        fetch(`api/fetchAssignmentDetails?id=${assignmentId}`)
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
        fetch(`api/fetchGoals?id=${assignmentId}`)
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

    function deleteHomeworks() {
        if (!assignmentId) {
            console.error("Assignment ID is not provided");
            return;
        }

        fetch(`api/deleteHomeworks?homeworkId=${assignmentId}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': "application/json"
            }
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error("Network response was not ok");
                }
                return response.json();
            })
            .then(data => {
                console.log("Homework deleted successfully", data);
                // Only navigate if the deletion was successful
                window.location.href = 'assignments-teacher.html';
            })
            .catch(error => {
                console.error("There was a problem with the fetch operation: ", error);
            });
    }

    if (deleteBtn) {
        deleteBtn.addEventListener('click', function () {
            deleteHomeworks();
        });
    }

    const editBtn = document.querySelector(".edit-assignment-btn");
    if (editBtn) {
        editBtn.addEventListener("click", () => {
            window.location.href = `edit-assignment.html?homeworkId=${assignmentId}`;
        });
    }

    function fetchSplitRequests() {
        const splitRequestsList = document.getElementById("split-request-ul");
        fetch(`api/fetchSplitRequest?homeworkId=${assignmentId}&role=${role}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! Status: ${response.status}`);
                }
                return response.json();
            })
            .then(requests => {
                requests.forEach(request => {
                    const listItem = document.createElement("li");

                    const reqSpan = document.createElement("span");
                    reqSpan.classList.add("student-name");
                    reqSpan.textContent = request.person_name;

                    const splitCont = document.createElement("div");
                    splitCont.classList.add("split-request-buttons-container");

                    const descBtn = document.createElement("button");
                    descBtn.classList.add("request-description-btn");
                    descBtn.innerText = "See description";
                    descBtn.addEventListener("click", () => {
                        showDescriptionPopup(request.request_description);
                    });

                    const reqDiv = document.createElement("div");

                    const accBtn = document.createElement("button");
                    accBtn.classList.add("button-accept");
                    accBtn.innerText = "Accept";
                    accBtn.addEventListener("click", () => {
                        fetch(`api/fetchSplitRequestAnswer?id=${request.split_request_id}&answer=true`).then(response => {
                            if (!response.ok) {
                                throw new Error(`HTTP error! Status: ${response.status}`);
                            }
                            return response.json();
                        }).then(() => {
                            splitRequestsList.innerHTML = "";
                            fetchSplitRequests();
                        });
                    });

                    const rejBtn = document.createElement("button");
                    rejBtn.classList.add("button-reject");
                    rejBtn.innerText = "Reject";
                    rejBtn.addEventListener("click", () => {
                        fetch(`api/fetchSplitRequestAnswer?id=${request.split_request_id}&answer=false`).then(response => {
                            if (!response.ok) {
                                throw new Error(`HTTP error! Status: ${response.status}`);
                            }
                            return response.json();
                        }).then(() => {
                            splitRequestsList.innerHTML = "";
                            fetchSplitRequests();
                        });
                    });

                    reqDiv.appendChild(accBtn);
                    reqDiv.appendChild(rejBtn);
                    splitCont.appendChild(descBtn);
                    splitCont.appendChild(reqDiv);
                    listItem.appendChild(reqSpan);
                    listItem.appendChild(splitCont);
                    splitRequestsList.appendChild(listItem);
                });
            })
            .catch(error => console.error('Error fetching split requests:', error));
    }

    const logoutBtn = document.querySelector(".log-out");
    function logoutAndRedirect() {
        console.log("baba")
        sessionStorage.clear();

        var baseUrl = window.location.origin + "/SomtodayHomeworkPlanner_war/";
        window.location.href = baseUrl;
    }

    logoutBtn.addEventListener("click", logoutAndRedirect)

    function showDescriptionPopup(description) {
        const descriptionPopup = document.querySelector('.request-description-pop-up');
        const descriptionOverlay = document.querySelector('.request-description-pop-up-overlay');
        const descriptionContent = document.getElementById('description-content');
        const closeButton = document.querySelector('.request-description-pop-up .close-button');

        if (descriptionContent) {
            descriptionContent.textContent = description;
        }

        if (descriptionPopup && descriptionOverlay) {
            descriptionPopup.classList.add('visible');
            descriptionOverlay.classList.add('visible');
        }

        if (closeButton) {
            closeButton.addEventListener('click', () => {
                descriptionPopup.classList.remove('visible');
                descriptionOverlay.classList.remove('visible');
            });
        }
    }

    if (assignmentId && role && personId) {
        fetchDetails();
        fetchGoals();
        fetchSplitRequests();
    } else {
        console.error('No assignment ID provided in URL');
    }
});