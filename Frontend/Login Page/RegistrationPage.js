document.getElementById('registerform').addEventListener('submit', function (event) {
    event.preventDefault();
    handleRegistration(event);
});

async function handleRegistration(event) {
    const form = event.target;
    const submit = form.querySelector('input[type="submit"]');

    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirm-password').value;

    if (password.length < 8) {
        alert('Password must be at least 8 characters.');
        return;
    }
    // This field existed in the markup but was never checked. Validate it
    // before doing any network work.
    if (password !== confirmPassword) {
        alert('Passwords do not match.');
        return;
    }

    // Was: document.querySelector("input[name='gender']:checked").value, which
    // threw a TypeError when no radio was selected and killed the handler with
    // no feedback. Gender is optional, so tolerate it being absent.
    const genderInput = document.querySelector("input[name='gender']:checked");

    // The server computes "today" in the user's timezone (D2), so send it.
    const timezone = (Intl.DateTimeFormat().resolvedOptions().timeZone) || 'UTC';

    const userdata = {
        firstname: document.getElementById('Firstname').value.trim(),
        lastname: document.getElementById('Lastname').value.trim(),
        username: document.getElementById('username').value.trim(),
        email: document.getElementById('email').value.trim() || null,
        gender: genderInput ? genderInput.value : null,
        // The backend binds height/weight to Long. The inputs allow decimals,
        // so truncate rather than sending "68.5" and having Jackson fail the
        // whole request.
        height: toWholeNumber(document.getElementById('height').value),
        weight: toWholeNumber(document.getElementById('weight').value),
        password: password,
        timezone: timezone
    };

    if (submit) { submit.disabled = true; }

    try {
        await window.improvApi.post(window.IMPROV.endpoints.register, userdata);
        alert('Registered successfully! Please log in.');
        window.location.href = 'LoginPage.html';
    } catch (error) {
        // 409 = username taken, 400 = validation — the API client renders the
        // problem document's detail (and field errors) into the message.
        alert(error.message || 'Registration failed. Please check your details and try again.');
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
