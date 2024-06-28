document.addEventListener('DOMContentLoaded', function() {
    const classSelect = document.getElementById('class-select');
    const classNameSpan = document.querySelector('.class-name');
    const deleteClassBtn = document.getElementById('delete-class-btn');
    const role = sessionStorage.getItem('role');
    const personId = sessionStorage.getItem('personId');
    const sidebar = document.getElementById('sidebar');

    if (personId) {
        profileLink.href = `profile.html?personId=${personId}`;
    }

    // Update sidebar based on role
    if (role === 'admin') {
        sidebar.classList.add('green');
    } else if (role === 'teacher') {
        sidebar.classList.add('yellow');
    } else {
        sidebar.classList.add('red');
    }

    // Function to fetch classes based on role and personId
    function fetchClasses() {
        fetch(`api/fetchClasses?role=${role}&personId=${personId}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! Status: ${response.status}`);
                }
                return response.json();
            })
            .then(classes => {
                populateClassSelect(classes);
            })
            .catch(error => console.error('Error fetching classes:', error));
    }

    // Populate the class selection dropdown
    function populateClassSelect(classes) {
        classSelect.innerHTML = '<option value="">Select a class</option>';
        classes.forEach(classData => {
            const option = document.createElement('option');
            option.value = classData.class_id;
            option.textContent = classData.class_name;
            classSelect.appendChild(option);
        });
    }

    // Update the displayed class name when a class is selected
    classSelect.addEventListener('change', function() {
        const selectedOption = classSelect.options[classSelect.selectedIndex];
        if (selectedOption.value) {
            classNameSpan.textContent = selectedOption.text;
        } else {
            classNameSpan.textContent = '';
        }
    });

    // Handle delete class button click event
    deleteClassBtn.addEventListener('click', function() {
        const classId = classSelect.value;
        if (classId) {
            if (confirm('Are you sure you want to delete this class?')) {
                deleteClass(classId);
            }
        } else {
            alert('Please select a class to delete.');
        }
    });

    // Function to delete a class
    function deleteClass(classId) {
        fetch('api/deleteClass', {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ classId: classId })
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! Status: ${response.status}`);
                }
                return response.json();
            })
            .then(result => {
                if (result.success) {
                    alert('Class deleted successfully!');
                    window.location.href = 'classes.html';
                } else {
                    alert('Error deleting class.');
                }
            })
            .catch(error => console.error('Error deleting class:', error));
    }

    // Fetch classes on page load
    fetchClasses();

    const logoutBtn = document.querySelector(".log-out");
    function logoutAndRedirect() {
        console.log("baba")
        sessionStorage.clear();

        var baseUrl = window.location.origin + "/SomtodayHomeworkPlanner_war/";
        window.location.href = baseUrl;
    }

    logoutBtn.addEventListener("click", logoutAndRedirect)
});
