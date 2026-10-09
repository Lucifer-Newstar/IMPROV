document.getElementById('loginform').addEventListener('submit', function (event) {
    event.preventDefault();
    handleLogin();
});

async function handleLogin() {
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;
    const submit = document.querySelector('#loginform input[type="submit"]');

    if (submit) { submit.disabled = true; }

    try {
        // On success the server sets the auth cookies; the response is the
        // profile. The API client handles errors (401, network) uniformly.
        await window.improvApi.post(window.IMPROV.endpoints.login, {
            username: username,
            password: password
        });
        window.location.href = 'workout-homepage.html';
    } catch (error) {
        alert(error.message || 'Login failed. Please try again.');
    } finally {
        if (submit) { submit.disabled = false; }
    }
}
