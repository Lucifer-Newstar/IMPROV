document.getElementById("registerform").addEventListener("submit", function(event) {
    event.preventDefault();
    handleRegistration(event);
    });
    async function handleRegistration(event){
    const userdata = {
        user_firstname: document.getElementById("Firstname").value,
        user_lastname: document.getElementById("Lastname").value,
        user_name: document.getElementById("username").value,
        user_email: document.getElementById("email").value,
        user_gender: document.querySelector("input[name='gender']:checked").value,
        user_height: document.getElementById("height").value,
        user_weight: document.getElementById("weight").value,
        user_password: document.getElementById("password").value
    };

    fetch("http://localhost:8080/api/users/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(userdata)
    })
    .then(response => response.json())
    .then(data => {
        alert("Registered successfully!");
        console.log(data);
        window.location.href = "LoginPage.html";
    })
    .catch(error => {
        console.error("Error:", error);
        alert("Registration failed. Please try again.");
    });
}
