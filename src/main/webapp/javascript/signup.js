document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("signupForm");
    const errorEl = document.getElementById("error-message");

    if (!form) {
        console.error("signupForm not found. Make sure your form has id='signupForm'.");
        return;
    }

    function showError(msg) {
        if (!errorEl) return;
        errorEl.style.display = "block";
        errorEl.textContent = msg;
    }

    function hideError() {
        if (!errorEl) return;
        errorEl.style.display = "none";
        errorEl.textContent = "";
    }


    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        hideError();

        const personName = document.getElementById("person-name")?.value.trim();
        const email = document.getElementById("email")?.value.trim();
        const username = document.getElementById("username")?.value.trim();
        const password = document.getElementById("password")?.value;
        const birthDate = document.getElementById("birth-date")?.value; // yyyy-mm-dd
        const gender = document.getElementById("gender")?.value;
        const role = document.getElementById("role")?.value;

        if (!personName || !email || !username || !password || !birthDate || !gender || !role) {
            showError("Please fill in all fields.");
            showToast("Please fill in all fields.", "error",8000);
            return;
        }

        const body = new URLSearchParams();
        body.append("personName", personName);
        body.append("email", email);
        body.append("username", username);
        body.append("password", password);
        body.append("birthDate", birthDate);
        body.append("gender", gender);
        body.append("role", role);

        try {
            // ✅ IMPORTANT: relative URL, same style as login.js
            const res = await fetch("api/signup", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: body.toString(),
            });

            const text = await res.text();
            let data = null;
            try { data = JSON.parse(text); } catch (_) {}

            if (!res.ok) {
                const msg =
                    (data && (data.error || data.message)) ||
                    `Signup failed (status ${res.status}).`;
                showError(msg);
                showToast(msg, "error",5000);
                console.error("Signup error response:", text);
                return;
            }

            showToast("Account created successfully!", "success",2000);

            // optional: store returned values (if your backend returns them)
            if (data?.personId) sessionStorage.setItem("personId", String(data.personId));
            if (data?.role) sessionStorage.setItem("role", data.role);
            if (data?.personName) sessionStorage.setItem("personName", data.personName);

            setTimeout(() => {
                window.location.href = "login.html";
            }, 5000);

        } catch (err) {
            console.error("Signup request failed:", err);
            showError("Network error. Is the backend running?");
            showToast("Network error. Is the backend running?", "error",4000);
        }
    });
});
