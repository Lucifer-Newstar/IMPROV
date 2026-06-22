document.getElementById("registerform").addEventListener("submit", function(event){event.preventDefault();})

const userdata={
    Firstname: document.getElementById("Firstname").value,
    Lastname: document.getElementById("Lastname").value,
    username: document.getElementById("username").value,
    email: document.getElementById("email").value,
    gender: document.querySelector("input[name='gender']:checked").value,
    height: document.getElementById("height").value,
    weight: document.getElementById("weight").value,
    password: document.getElementById("password").value
}

fetch("http://localhost:8080/api/Users/register",{
    method: "POST",
    headers: {
        "Content-Type": "application/json"
    },
    body: JSON.stringify(userdata)
})
.then(response => response.json())
.then(data => {
    alert("registered successfully");
    console.log(data);
})
.catch(error => {
    console.error("Error:", error);
});