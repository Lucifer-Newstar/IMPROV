document.getElementById("registerform").addEventListener("submit", function (event) {
    event.preventDefault();
    handleRegistration(event);
});

async function handleRegistration(event) {
    const form = event.target;
    const submit = form.querySelector('input[type="submit"]');

    const password = document.getElementById("password").value;
    const confirmPassword = document.getElementById("confirm-password").value;

    // This field existed in the markup but was never checked. Validate it
    // before doing any network work.
    if (password !== confirmPassword) {
        alert("Passwords do not match.");
        return;
    }

    // Was: document.querySelector("input[name='gender']:checked").value, which
    // threw a TypeError when no radio was selected and killed the handler with
    // no feedback. Gender is optional, so tolerate it being absent.
    const genderInput = document.querySelector("input[name='gender']:checked");

    const userdata = {
        firstname: document.getElementById("Firstname").value,
        lastname: document.getElementById("Lastname").value,
        username: document.getElementById("username").value,
        email: document.getElementById("email").value,
        gender: genderInput ? genderInput.value : null,
        // The backend binds height/weight to Long. The inputs allow decimals,
        // so truncate rather than sending "68.5" and having Jackson fail the
        // whole request. Change this to a Double server-side if decimals are
        // wanted — see docs/REFERENCE_ANALYSIS.md section 4.
        height: toWholeNumber(document.getElementById("height").value),
        weight: toWholeNumber(document.getElementById("weight").value),
        password: password
    };

    if (submit) { submit.disabled = true; }

    try {
        const response = await fetch(window.IMPROV.apiBase + window.IMPROV.endpoints.register, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(userdata)
        });

        if (!response.ok) {
            const detail = await response.text().catch(() => "");
            console.error("Registration rejected:", response.status, detail);
            alert("Registration failed. Please check your details and try again.");
            return;
        }

        const data = await response.json();
        console.log("Registered:", data);
        alert("Registered successfully!");
        window.location.href = "LoginPage.html";
    } catch (error) {
        console.error("Registration error:", error);
        alert("Could not reach the server. Check your connection and try again.");
    } finally {
        if (submit) { submit.disabled = false; }
    }
}

/** '' -> null, '68.5' -> 68, 'abc' -> null */
function toWholeNumber(value) {
    if (value === null || value === undefined || value === "") { return null; }
    const n = Number(value);
    return Number.isFinite(n) ? Math.trunc(n) : null;
}
