document.getElementById("loginform").addEventListener("submit", function (event) {
    event.preventDefault();
    handleLogin();
});

async function handleLogin() {
    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;
    const submit = document.querySelector('#loginform input[type="submit"]');

    if (submit) { submit.disabled = true; }

    try {
        // Same-origin: nginx proxies /api to the backend. Never hardcode a
        // host here — see config.js and the CI check that enforces it.
        const response = await fetch(window.IMPROV.apiBase + window.IMPROV.endpoints.login, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                "username": username,
                "password": password
            })
        });

        if (!response.ok) {
            // The backend currently answers a bad login with 500 rather than
            // 401 (tracked P0 bug), so don't assert on the exact status here.
            alert("Wrong username or password!");
            return;
        }

        const data = await response.json();

        if (data && data.id) {
            alert("Login Successful!");
            window.location.href = "workout-homepage.html";
        } else {
            alert("Wrong username or password!");
        }
    } catch (error) {
        // Previously an unhandled fetch rejection meant the button silently
        // did nothing whenever the backend was down.
        console.error("Login failed:", error);
        alert("Could not reach the server. Check your connection and try again.");
    } finally {
        if (submit) { submit.disabled = false; }
    }
}
