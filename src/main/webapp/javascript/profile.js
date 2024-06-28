document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    const viewedPersonId = urlParams.get('personId');
    const role = sessionStorage.getItem('role');
    const personId = sessionStorage.getItem('personId');
    const personName = sessionStorage.getItem('personName');

    const sidebar = document.querySelector('.side-bar');
    const editProfileBtn = document.getElementById('editProfileBtn');
    const cancelEditBtn = document.getElementById('cancelEditBtn');
    const editProfileForm = document.getElementById('editProfileForm');
    const profileView = document.getElementById('profile-view');
    const editProfileView = document.getElementById('edit-profile-view');
    const usernameField = document.getElementById('username-field');
    const passwordField = document.getElementById('password-field');

    console.log('Role:', role);
    console.log('Person ID:', personId);
    console.log('Viewed Person ID:', viewedPersonId);
    console.log('Person Name:', personName);

    sidebar.classList.remove('red', 'yellow', 'green');
    if (role === 'admin') {
        sidebar.classList.add('green');
    } else if (role === 'teacher') {
        sidebar.classList.add('yellow');
    } else if (role === 'student') {
        sidebar.classList.add('red');
    }

    function fetchProfileInfo() {
        fetch(`api/profile?personId=${viewedPersonId}`)
            .then(response => response.json())
            .then(data => {
                document.getElementById('personName').innerText = data.person_name;
                document.getElementById('firstName').innerText = data.person_name.split(' ')[0];
                document.getElementById('birthDate').innerText = data.birth_date;
                document.getElementById('gender').innerText = data.person_gender;
                document.getElementById('email').innerText = data.email_address;
                if (viewedPersonId === personId) {
                    document.getElementById('username').innerText = data.username;
                    document.getElementById('password').innerText = "Hidden for privacy";
                    document.getElementById('usernameE').value = data.username;
                    document.getElementById('passwordE').value = data.user_password;
                }

                document.getElementById('firstNameE').value = data.person_name.split(' ')[0];
                document.getElementById('birthDateE').value = data.birth_date;
                document.getElementById('genderE').value = data.person_gender;
                document.getElementById('emailE').value = data.email_address;
            })
            .catch(error => console.error('Error fetching profile info:', error));
    }

    function showEditProfileView() {
        profileView.style.display = 'none';
        editProfileView.style.display = 'block';
    }

    function showProfileView() {
        profileView.style.display = 'block';
        editProfileView.style.display = 'none';
    }

    if (viewedPersonId !== personId) {
        editProfileBtn.style.display = 'none';
        usernameField.style.display = 'none';
        passwordField.style.display = 'none';
    } else {
        editProfileBtn.addEventListener('click', showEditProfileView);
        cancelEditBtn.addEventListener('click', showProfileView);

        editProfileForm.addEventListener('submit', (event) => {
            event.preventDefault();

            const formData = new FormData(editProfileForm);
            const jsonData = {};
            formData.forEach((value, key) => {
                jsonData[key] = value;
            });

            fetch(`api/profile?personId=${personId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(jsonData)
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        fetchProfileInfo();
                        showProfileView();
                    } else {
                        console.error('Error updating profile:', data.message);
                    }
                })
                .catch(error => console.error('Error updating profile:', error));
        });
    }

    fetchProfileInfo();

    const logoutBtn = document.querySelector(".log-out");

    function logoutAndRedirect() {
        sessionStorage.clear();
        var baseUrl = window.location.origin + "/SomtodayHomeworkPlanner_war/";
        window.location.href = baseUrl;
    }

    logoutBtn.addEventListener("click", logoutAndRedirect)
});
