document.addEventListener('DOMContentLoaded', function() {
    const role = sessionStorage.getItem('role');
    const personId = sessionStorage.getItem('personId');
    const personName = sessionStorage.getItem('personName');
    const welcomeMessage = document.querySelector('.user-info span');
    const sidebar = document.querySelector('.side-bar');

    console.log('Role:', role);
    console.log('Person ID:', personId);
    console.log('Person Name:', personName);

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

    if (role !== 'admin') {
        fetchNotifications(personId);
    } else {
        const notificationContainer = document.querySelector('.notification-container');
        if (notificationContainer) {
            notificationContainer.innerHTML = '<p>No notifications for admin.</p>';
        }
    }

    function fetchNotifications(personId) {
        fetch(`api/fetchNotifications?personId=${personId}`)
            .then(response => response.json())
            .then(notifications => {
                populateNotificationsTable(notifications);
            })
            .catch(error => console.error('Error fetching notifications:', error));
    }

    function populateNotificationsTable(notifications) {
        const notificationsTableBody = document.querySelector('.notifications-table tbody');
        if (notificationsTableBody) {
            notificationsTableBody.innerHTML = ''; // Clear the table body

            notifications.forEach(notification => {
                const row = document.createElement('tr');
                const dateCell = document.createElement('td');
                const fromCell = document.createElement('td');
                const infoCell = document.createElement('td');

                dateCell.textContent = new Date(notification.date).toLocaleDateString();
                fromCell.textContent = notification.sender;
                infoCell.innerHTML = `<i class="info-icon fas fa-info-circle"></i> ${notification.info}`;

                row.appendChild(dateCell);
                row.appendChild(fromCell);
                row.appendChild(infoCell);

                notificationsTableBody.appendChild(row);
            });
        } else {
            console.error('Notifications table body element not found.');
        }
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