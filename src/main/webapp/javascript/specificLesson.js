
    document.addEventListener('DOMContentLoaded', function() {
    const role = sessionStorage.getItem('role');
    const personId = sessionStorage.getItem('personId');
    const personName = sessionStorage.getItem('personName');
    const lessonName = new URLSearchParams(window.location.search).get('lessonName');
    const lessonDescription = new URLSearchParams(window.location.search).get('lessonDescription');
    const teacherName = new URLSearchParams(window.location.search).get('teacherName');
    const lessonId = new URLSearchParams(window.location.search).get('lessonId');
    const sidebar = document.querySelector('.side-bar');
    const welcomeMessage = document.querySelector('.user-info span');

    document.getElementById('lesson-name').textContent = lessonName + " taught by " + teacherName + ".";
    document.getElementById('lesson-description').textContent = lessonDescription;

    if (personName) {
    welcomeMessage.textContent = `${personName}`;
}

    if (personId) {
    profileLink.href = `profile.html?personId=${personId}`;
}

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

    function fetchHomeworkForLesson() {
    fetch(`api/fetchHomeworkForLesson?lessonId=${lessonId}&personId=${personId}&role=${role}`)
    .then(response => response.json())
    .then(homework => {
    const activeAssignmentsContainer = document.querySelector('.active-assignments .assignment-list');
    const pastAssignmentsContainer = document.querySelector('.past-assignments .assignment-list');

    activeAssignmentsContainer.innerHTML = '';
    pastAssignmentsContainer.innerHTML = '';

    homework.forEach(hw => {
    const dueDate = new Date(hw.due_date);
    const currentDate = new Date();
    const assignmentItem = document.createElement('div');
    assignmentItem.classList.add('assignment-item');

    const classSpan = document.createElement('span');
    classSpan.textContent = hw.class_name;

    const nameSpan = document.createElement('span');
    nameSpan.textContent = hw.homework_name;

    const dateSpan = document.createElement('span');
    dateSpan.textContent = dueDate.toLocaleDateString();

    const detailsButton = document.createElement('button');
    detailsButton.textContent = 'See Details';
    detailsButton.addEventListener('click', () => {
    if (role === 'student') {
    window.location.href = `assignment-details-student.html?id=${hw.homework_id}`;
} else if (role === 'teacher') {
    window.location.href = `assignment-details-teacher.html?id=${hw.homework_id}`;
}
});

    assignmentItem.appendChild(classSpan);
    assignmentItem.appendChild(nameSpan);
    assignmentItem.appendChild(dateSpan);
    assignmentItem.appendChild(detailsButton);

    if (dueDate >= currentDate) {
    activeAssignmentsContainer.appendChild(assignmentItem);
} else {
    pastAssignmentsContainer.appendChild(assignmentItem);
}
});
})
    .catch(error => console.error('Error fetching homework for lesson:', error));
}

    fetchHomeworkForLesson();

    const logoutBtn = document.querySelector(".log-out");
    function logoutAndRedirect() {
    console.log("baba")
    sessionStorage.clear();

    var baseUrl = window.location.origin + "/SomtodayHomeworkPlanner_war/";
    window.location.href = baseUrl;
}

    logoutBtn.addEventListener("click", logoutAndRedirect)
});
