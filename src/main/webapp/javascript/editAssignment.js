document.addEventListener('DOMContentLoaded', function() {
    const homeworkId = new URLSearchParams(window.location.search).get('homeworkId');
    const role = sessionStorage.getItem('role');
    const personId = sessionStorage.getItem('personId');

    const classSelector = document.getElementById("class-selector");
    const lessonSelector = document.getElementById("lesson-selector");
    const assignmentNameInput = document.getElementById('assignment-name');
    const dueDateInput = document.getElementById('due-date');
    const timeIndicationInput = document.getElementById('time-indication');
    const descriptionInput = document.getElementById('assignment-description');
    const goalsList = document.getElementById('goals-list');
    const homeworkSplittableInput = document.getElementById('is-splittable');
    const totalGoalCount = document.getElementById('total-goal-count');

    if (personId) {
        profileLink.href = `profile.html?personId=${personId}`;
    }

    let savedData = {
        homeworkId: homeworkId,
        homeworkName: '',
        dueDate: null,
        publishDate: null,
        classId: 0,
        lessonId: 0,
        description: '',
        goals: [],
        timeIndication: null,
        homeworkSplittable: false
    };

    function fetchClasses() {
        console.log('Fetching classes...');
        fetch(`api/fetchClasses?role=${role}&personId=${personId}`)
            .then(response => response.json())
            .then(classes => {
                classes.forEach(ownClass => {
                    const option = document.createElement('option');
                    option.value = ownClass.class_id;
                    option.textContent = ownClass.class_name;
                    classSelector.appendChild(option);
                });
            })
            .catch(error => console.error('Error fetching classes:', error));
    }

    function convertTimeToMinutes(timeStr) {
        const [hours, minutes, seconds] = timeStr.split(':').map(Number);
        const totalMinutes = hours * 60 + minutes;
        return totalMinutes;
    }

    function fetchLessons(classId) {
        console.log('Fetching lessons...');
        fetch(`api/fetchLessons?classId=${classId}`)
            .then(response => response.json())
            .then(lessons => {
                lessons.forEach(lesson => {
                    const option = document.createElement('option');
                    option.value = lesson.lesson_id;
                    option.textContent = lesson.lesson_name;
                    lessonSelector.appendChild(option);
                });
            })
            .catch(error => console.error('Error fetching lessons:', error));
    }

    function fetchAssignmentDetails() {
        console.log('Fetching assignment details...');
        fetch(`api/editAssignment?homeworkId=${homeworkId}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! Status: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                assignmentNameInput.value = data.homeworkName;
                dueDateInput.value = data.dueDate;
                timeIndicationInput.value = convertTimeToMinutes(data.timeIndication);
                descriptionInput.value = data.description;
                classSelector.value = data.classId;
                fetchLessons(data.classId);
                lessonSelector.value = data.lessonId;
                homeworkSplittableInput.checked = data.homeworkSplittable;

                goalsList.innerHTML = '';
                data.goals.forEach(goal => {
                    const goalItem = document.createElement('li');
                    goalItem.innerHTML =
                        `<span class="goal-name">${goal.name}</span>
                             <button type="button" class="remove-goal-btn">x</button>`;
                    goalItem.querySelector('.remove-goal-btn').addEventListener('click', function() {
                        goalItem.remove();
                        updateTotalGoals();
                    });
                    goalsList.appendChild(goalItem);
                });
                updateTotalGoals();
            })
            .catch(error => console.error('Error fetching assignment details:', error));
    }

    function updateTotalGoals() {
        const goals = goalsList.querySelectorAll('li');
        totalGoalCount.textContent = `Total Goal Count: ${goals.length}`;
    }

    function validateForm() {
        let isValid = true;

        // Clear previous error messages
        document.getElementById('assignment-name-error').textContent = '';
        document.getElementById('due-date-error').textContent = '';
        document.getElementById('class-selector-error').textContent = '';
        document.getElementById('lesson-selector-error').textContent = '';
        document.getElementById('time-indication-error').textContent = '';
        document.getElementById('assignment-description-error').textContent = '';

        if (!assignmentNameInput.value) {
            isValid = false;
            document.getElementById('assignment-name-error').textContent = 'Assignment name is required.';
        }
        if (!dueDateInput.value) {
            isValid = false;
            document.getElementById('due-date-error').textContent = 'Due date is required.';
        }
        if (!classSelector.value) {
            isValid = false;
            document.getElementById('class-selector-error').textContent = 'Class selection is required.';
        }
        if (!lessonSelector.value) {
            isValid = false;
            document.getElementById('lesson-selector-error').textContent = 'Lesson selection is required.';
        }
        if (!timeIndicationInput.value) {
            isValid = false;
            document.getElementById('time-indication-error').textContent = 'Time indication is required.';
        }
        if (!descriptionInput.value) {
            isValid = false;
            document.getElementById('assignment-description-error').textContent = 'Assignment description is required.';
        }

        return isValid;
    }

    document.getElementById('save-btn').addEventListener('click', function() {
        if (!validateForm()) return;

        savedData.homeworkName = assignmentNameInput.value;
        savedData.dueDate = dueDateInput.value;
        savedData.classId = classSelector.value;
        savedData.lessonId = lessonSelector.value;
        savedData.description = descriptionInput.value;
        savedData.timeIndication = timeIndicationInput.value;
        savedData.homeworkSplittable = homeworkSplittableInput.checked;
        savedData.goals = [];
        goalsList.querySelectorAll('li').forEach(goalItem => {
            const goalName = goalItem.querySelector('.goal-name').textContent;
            savedData.goals.push({ name: goalName });
        });

        localStorage.setItem('assignmentData', JSON.stringify(savedData));
        document.getElementById('last-saved').textContent = `Last saved: ${new Date().toLocaleTimeString()}`;
    });

    document.getElementById('publish-btn').addEventListener('click', function() {
        if (!validateForm()) return;

        savedData.publishDate = new Date().toISOString().split('T')[0];

        fetch('api/editAssignment', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(savedData)
        })
            .then(response => {
                if (response.ok) {
                    console.log('Assignment updated successfully');
                    window.location.href = 'assignments-teacher.html';
                } else {
                    return response.json().then(err => { throw new Error(err.message); });
                }
            })
            .catch(error => console.error('Error updating assignment:', error));
    });

    const goalForm = document.getElementById('add-goal-form');
    const goalNameInput = document.getElementById('goal-name');

    goalForm.addEventListener('submit', function(event) {
        event.preventDefault();
        const goalName = goalNameInput.value;

        if (goalName) {
            const goalItem = document.createElement('li');
            goalItem.innerHTML =
                `<span class="goal-name">${goalName}</span>
                     <button type="button" class="remove-goal-btn">x</button>`;

            goalItem.querySelector('.remove-goal-btn').addEventListener('click', function() {
                goalItem.remove();
                updateTotalGoals();
            });

            goalsList.appendChild(goalItem);
            updateTotalGoals();

            // Reset form inputs
            goalNameInput.value = '';
        }
    });

    fetchClasses();
    fetchAssignmentDetails();

    const logoutBtn = document.querySelector(".log-out");
    function logoutAndRedirect() {
        console.log("baba")
        sessionStorage.clear();

        var baseUrl = window.location.origin + "/SomtodayHomeworkPlanner_war/";
        window.location.href = baseUrl;
    }

    logoutBtn.addEventListener("click", logoutAndRedirect)

});