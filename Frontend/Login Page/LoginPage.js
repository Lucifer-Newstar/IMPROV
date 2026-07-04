document.getElementById("loginform").addEventListener("submit", function(event) {
    event.preventDefault();
    handleLogin();
    });


async function handleLogin() {
    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;

    const response = await fetch("http://localhost:8080/api/Users/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            "username": username,
            "password": password
        })
    });

    const data = await response.json();

    if (data.id) {
        alert("Login Successful!");
        window.location.href = "workout-homepage.html.html";
    } else {
        alert("Wrong username or password!");
    }
}