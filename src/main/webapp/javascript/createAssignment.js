document.addEventListener('DOMContentLoaded', function() {
    // Fetch classes and update frontend
    const role = sessionStorage.getItem('role');
    const personId = sessionStorage.getItem('personId');

    if (personId) {
        profileLink.href = `profile.html?personId=${personId}`;
    }

    const classSelector = document.getElementById("class-selector");
    const lessonSelector = document.getElementById("lesson-selector");

    let classId;
    classSelector.addEventListener("change", () => {
        lessonSelector.innerHTML = "";
        classId = classSelector.value;
        fetchLessons();
    });

    function fetchClasses() {
        console.log('Fetching classes...');
        fetch(`api/fetchClasses?role=${role}&personId=${personId}`)
            .then(response => {
                console.log('Fetch response:', response);
                if (!response.ok) {
                    throw new Error(`HTTP error! Status: ${response.status}`);
                }
                return response.json();
            })
            .then(classes => {
                console.log('Classes fetched:', classes);
                classes.forEach(ownClass => {
                    const option = document.createElement('option');
                    option.value = ownClass.class_id;
                    option.textContent = ownClass.class_name;
                    classSelector.appendChild(option);
                });

                // Automatically select the first class and fetch lessons
                if (classes.length > 0) {
                    classSelector.value = classes[0].class_id;
                    classId = classes[0].class_id;
                    fetchLessons();
                }
            })
            .catch(error => console.error('Error fetching classes:', error));
    }

    function fetchLessons() {
        console.log('Fetching lessons...');
        fetch(`api/fetchLessons?role=${role}&personId=${personId}&classId=${classId}`)
            .then(response => {
                console.log('Fetch response:', response);
                if (!response.ok) {
                    throw new Error(`HTTP error! Status: ${response.status}`);
                }
                return response.json();
            })
            .then(lessons => {
                console.log('Lessons fetched:', lessons);
                lessons.forEach(lesson => {
                    const option = document.createElement('option');
                    option.value = lesson.lesson_id;
                    option.textContent = lesson.lesson_name;
                    lessonSelector.appendChild(option);
                });
            })
            .catch(error => console.error('Error fetching lessons:', error));
    }

    if (role && personId) {
        setTimeout(fetchClasses, 50);
    } else {
        console.error('Missing role or personId in session storage');
    }

    const saveButton = document.getElementById('save-btn');
    const publishButton = document.getElementById('publish-btn');
    const assignmentNameInput = document.getElementById('assignment-name');
    const dueDateInput = document.getElementById('due-date');
    const timeIndicationInput = document.getElementById('time-indication');
    const descriptionInput = document.getElementById('assignment-description');
    const goalsList = document.getElementById('goals-list');
    const homeworkSplittableInput = document.getElementById('is-splittable');
    const totalGoalCount = document.getElementById('total-goal-count');



    let savedData = {
        personId: personId,
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

    saveButton.addEventListener('click', function() {
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
        savedData.publishDate = new Date().toISOString().split('T')[0];

        console.log(JSON.stringify(savedData));
    });

    publishButton.addEventListener('click', function() {
        if (!validateForm()) return;

        savedData.homeworkName = assignmentNameInput.value;
        savedData.dueDate = dueDateInput.value;
        savedData.classId = classSelector.value;
        savedData.lessonId = lessonSelector.value;
        savedData.description = descriptionInput.value;
        savedData.timeIndication = timeIndicationInput.value;
        savedData.homeworkSplittable = homeworkSplittableInput.checked;
        savedData.publishDate = new Date().toISOString().split('T')[0];

        fetch('api/addAssignment', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(savedData)
        })
            .then(response => {
                if (response.ok) {
                    console.log('Assignment published successfully');
                    window.location.href = 'assignments-teacher.html';
                } else {
                    return response.json().then(err => { throw new Error(err.message); });
                }
            })
            .catch(error => console.error('Error publishing assignment:', error));
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

    function updateTotalGoals() {
        const goals = goalsList.querySelectorAll('li');
        totalGoalCount.textContent = `Total Goal Count: ${goals.length}`;
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
