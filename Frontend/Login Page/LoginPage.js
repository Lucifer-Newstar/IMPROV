document.getElementById("loginform").addEventListener("submit", function(event) {
    event.preventDefault();
    handleLogin();
});


async function handleLogin() {
    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;

    const response = await fetch("http://localhost:8080/api/users/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            "user_name":username,
             "user_password":password })
    });

    const data = await response.json();

    if (data.status === "success") {
        alert("Login Successful!");
        window.location.href = "home.html";
    } else {
        alert("wrong username or password: " + data.message);
    }
   
}