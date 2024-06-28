document.getElementById('loginForm').addEventListener('submit', function(event) {
    event.preventDefault();

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    fetch('/SomtodayHomeworkPlanner_war/api/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: new URLSearchParams({
            username: username,
            password: password
        })
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Login failed');
            }
            return response.json();
        })
        .then(data => {
            if (data.error) {
                document.getElementById('error-message').textContent = 'Invalid username or password';
                document.getElementById('error-message').style.display = 'block';
            } else {
                // save cookies to use later!
                sessionStorage.setItem('role', data.role);
                sessionStorage.setItem('personId', data.personId);
                sessionStorage.setItem('personName', data.personName);
                window.location.href = 'dashboard.html';
            }
        })
        .catch(error => {
            console.error('Error:', error);
            document.getElementById('error-message').textContent = 'Wrong username or password. Please try again.';
            document.getElementById('error-message').style.display = 'block';
        });

});

const urlParams = new URLSearchParams(window.location.search);
if (urlParams.has('error')) {
    document.getElementById('error-message').style.display = 'block';
}