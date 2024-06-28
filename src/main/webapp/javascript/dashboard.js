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
        welcomeMessage.textContent = `Welcome, ${personName}!`;
    }

    if (personId) {
        profileLink.href = `profile.html?personId=${personId}`;
    }

    sidebar.classList.remove('red', 'yellow', 'green');
    if (role === 'admin') {
        sidebar.classList.add('green');
    } else if (role === 'teacher') {
        sidebar.classList.add('yellow');
    } else if (role === 'student') {
        sidebar.classList.add('red');
    }

    const logoutBtn = document.querySelector(".log-out");
    function logoutAndRedirect() {
        console.log("baba")
        sessionStorage.clear();

        var baseUrl = window.location.origin + "/SomtodayHomeworkPlanner_war/";
        window.location.href = baseUrl;
    }

    logoutBtn.addEventListener("click", logoutAndRedirect)

    function fetchHomework() {
        fetch(`api/fetchHomework?role=${role}&personId=${personId}`)
            .then(response => response.json())
            .then(homework => {
                if (role === 'admin') {
                    return;
                }

                if (role === 'teacher') {
                    const uniqueHomework = [];
                    const seenHomework = new Set();

                    homework.forEach(hw => {
                        if (!seenHomework.has(hw.homework_name + hw.class_id)) {
                            uniqueHomework.push(hw);
                            seenHomework.add(hw.homework_name + hw.class_id);
                        }
                    });

                    renderHomeworkOnCalendar(uniqueHomework);
                } else if (role === 'student') {
                    renderHomeworkOnCalendar(homework);
                }
            })
            .catch(error => console.error('Error fetching homework:', error));
    }

    function getTimeClass(timeIndication) {
        const timeParts = timeIndication.split(':');
        const minutes = parseInt(timeParts[1], 10);

        if (minutes < 15) {
            return 'light-green';
        } else if (minutes <= 45) {
            return 'orange';
        } else {
            return 'red';
        }
    }

    function renderHomeworkOnCalendar(homework) {
        const calendarGrid = document.getElementById('calendar-grid');
        const weekCalendarGrid = document.getElementById('week-calendar-grid');

        // clear items on calendar
        calendarGrid.querySelectorAll('.day .task').forEach(task => task.remove());

        homework.forEach(hw => {
            const dueDate = new Date(hw.due_date);
            const dueDay = dueDate.getDate();
            const dueMonth = dueDate.getMonth();
            const dueYear = dueDate.getFullYear();
            const timeClass = getTimeClass(hw.time_indication);

            // Month view
            if (currentMonth === dueMonth && currentYear === dueYear) {
                const dayCells = calendarGrid.querySelectorAll('.day');
                dayCells.forEach(dayCell => {
                    if (parseInt(dayCell.textContent) === dueDay) {
                        const hwElement = document.createElement('div');
                        hwElement.classList.add('task', timeClass);
                        if (role === 'student') {
                            hwElement.innerHTML = `<a href="assignment-details-student.html?id=${hw.homework_id}">${hw.homework_name}</a>`;
                        } else if (role === 'teacher') {
                            hwElement.innerHTML = `<a href="assignment-details-teacher.html?id=${hw.homework_id}">${hw.homework_name}</a>`;
                        }
                        dayCell.appendChild(hwElement);
                    }
                });
            }

            // Week view
            const weekStart = new Date(currentWeekStart);
            const weekEnd = new Date(currentWeekStart);
            weekEnd.setDate(weekEnd.getDate() + 6);

            if (dueDate >= weekStart && dueDate <= weekEnd) {
                const weekCells = weekCalendarGrid.querySelectorAll('td');
                weekCells.forEach((weekCell, index) => {
                    const cellDate = new Date(weekStart);
                    cellDate.setDate(weekStart.getDate() + index);
                    if (cellDate.toDateString() === dueDate.toDateString()) {
                        const hwElement = document.createElement('div');
                        hwElement.classList.add('task', timeClass);
                        if (role === 'student') {
                            hwElement.innerHTML = `<a href="assignment-details-student.html?id=${hw.homework_id}">${hw.homework_name}</a>`;
                        } else if (role === 'teacher') {
                            hwElement.innerHTML = `<a href="assignment-details-teacher.html?id=${hw.homework_id}">${hw.homework_name}</a>`;
                        }
                        weekCell.appendChild(hwElement);
                    }
                });
            }
        });
    }

    function fetchClasses() {
        fetch(`api/fetchClasses?role=${role}&personId=${personId}`)
            .then(response => response.json())
            .then(classes => {
                const classesList = document.getElementById('classes-list');
                classesList.innerHTML = '';
                classes.forEach(cls => {
                    const listItem = document.createElement('li');
                    listItem.textContent = cls.class_name;
                    const button = document.createElement('button');
                    button.textContent = 'Go to page';
                    button.addEventListener('click', () => {
                        window.location.href = `class.html?classId=${cls.class_id}`;
                    });
                    listItem.appendChild(button);
                    classesList.appendChild(listItem);
                });
            })
            .catch(error => console.error('Error fetching classes:', error));
    }

    function fetchAssignments() {
        fetch(`api/fetchAssignments?role=${role}&personId=${personId}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! Status: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                const assignments = data.homeworks;  // Assuming server returns an object with 'homeworks' key
                const assignmentContainer = document.getElementById('assignments-ul');
                assignmentContainer.innerHTML = '';

                if (Array.isArray(assignments)) {
                    assignments.forEach(assignmentData => {
                        const row = document.createElement('li');

                        const link = document.createElement("a");
                        link.innerHTML = assignmentData.class_name + " - " + assignmentData.homework_name;
                        link.href = `assignment-details-teacher.html?id=${assignmentData.homework_id}`;

                        link.style = "color: #262F35; text-decoration: none;";

                        row.appendChild(link);
                        assignmentContainer.appendChild(row);
                    });
                } else {
                    console.error('Assignments data is missing or not an array');
                }
            })
            .catch(error => console.error('Error fetching assignments:', error));
    }

    let currentMonth = new Date().getMonth();
    let currentYear = new Date().getFullYear();
    let currentWeekStart = new Date();
    currentWeekStart.setDate(currentWeekStart.getDate() - currentWeekStart.getDay() + 1);

    function renderCalendar(month, year) {
        const calendarGrid = document.getElementById('calendar-grid');
        calendarGrid.innerHTML = '';
        const firstDay = new Date(year, month).getDay();
        const daysInMonth = new Date(year, month + 1, 0).getDate();

        for (let i = 0; i < firstDay; i++) {
            const emptyCell = document.createElement('div');
            emptyCell.classList.add('day');
            calendarGrid.appendChild(emptyCell);
        }

        for (let day = 1; day <= daysInMonth; day++) {
            const dayCell = document.createElement('div');
            dayCell.classList.add('day');
            dayCell.dataset.date = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
            dayCell.textContent = day;
            calendarGrid.appendChild(dayCell);
        }

        const monthYearSpan = document.getElementById('current-month-year');
        monthYearSpan.textContent = new Date(year, month).toLocaleString('default', { month: 'long', year: 'numeric' });

        fetchHomework();
    }

    function renderWeekView(weekStart) {
        const weekDaysHeader = document.getElementById('week-days-header');
        const weekTasks = document.getElementById('week-tasks');
        weekDaysHeader.innerHTML = '';
        weekTasks.innerHTML = '';

        const currentWeekRange = document.getElementById('current-week-range');
        const weekEnd = new Date(weekStart);
        weekEnd.setDate(weekEnd.getDate() + 6);
        currentWeekRange.textContent = `Current Week (${weekStart.toLocaleDateString('default', { day: 'numeric', month: 'long' })} - ${weekEnd.toLocaleDateString('default', { day: 'numeric', month: 'long' })})`;

        const daysOfWeek = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
        for (let i = 0; i < 7; i++) {
            const dayHeader = document.createElement('th');
            const date = new Date(weekStart);
            date.setDate(weekStart.getDate() + i);
            dayHeader.textContent = `${daysOfWeek[i]} (${date.toLocaleDateString('default', { day: 'numeric', month: 'long' })})`;
            weekDaysHeader.appendChild(dayHeader);

            const dayTaskCell = document.createElement('td');
            weekTasks.appendChild(dayTaskCell);
        }

        fetchHomework();
    }

    document.getElementById('prev-month-button').addEventListener('click', () => {
        currentMonth--;
        if (currentMonth < 0) {
            currentMonth = 11;
            currentYear--;
        }
        renderCalendar(currentMonth, currentYear);
    });

    document.getElementById('next-month-button').addEventListener('click', () => {
        currentMonth++;
        if (currentMonth > 11) {
            currentMonth = 0;
            currentYear++;
        }
        renderCalendar(currentMonth, currentYear);
    });

    document.getElementById('month-select').addEventListener('change', (event) => {
        currentMonth = parseInt(event.target.value);
        renderCalendar(currentMonth, currentYear);
    });

    document.getElementById('prev-week-button').addEventListener('click', () => {
        currentWeekStart.setDate(currentWeekStart.getDate() - 7);
        renderWeekView(currentWeekStart);
    });

    document.getElementById('next-week-button').addEventListener('click', () => {
        currentWeekStart.setDate(currentWeekStart.getDate() + 7);
        renderWeekView(currentWeekStart);
    });

    document.getElementById('month-view-button').addEventListener('click', () => {
        document.getElementById('calendar-grid').style.display = 'grid';
        document.getElementById('week-calendar-grid').style.display = 'none';
        document.getElementById('prev-month-button').style.display = 'inline';
        document.getElementById('next-month-button').style.display = 'inline';
        document.getElementById('current-month-year').style.display = 'inline';
        document.getElementById('month-select').style.display = 'inline';
        document.getElementById('prev-week-button').style.display = 'none';
        document.getElementById('next-week-button').style.display = 'none';
        document.getElementById('current-week-range').style.display = 'none';
    });

    document.getElementById('week-view-button').addEventListener('click', () => {
        document.getElementById('calendar-grid').style.display = 'none';
        document.getElementById('week-calendar-grid').style.display = 'block';
        document.getElementById('prev-month-button').style.display = 'none';
        document.getElementById('next-month-button').style.display = 'none';
        document.getElementById('current-month-year').style.display = 'none';
        document.getElementById('month-select').style.display = 'none';
        document.getElementById('prev-week-button').style.display = 'inline';
        document.getElementById('next-week-button').style.display = 'inline';
        document.getElementById('current-week-range').style.display = 'inline';
        renderWeekView(currentWeekStart);
    });

    renderCalendar(currentMonth, currentYear);
    fetchClasses();
    fetchAssignments();
});