const API = {
    register: "/api/auth/register",
    login: "/api/auth/login"
};

const els = {
    username: document.getElementById("username"),
    password: document.getElementById("password"),
    loginBtn: document.getElementById("loginBtn"),
    registerBtn: document.getElementById("registerBtn"),
    errorBox: document.getElementById("errorBox"),
    successBox: document.getElementById("successBox")
};

function showError(msg) {
    els.successBox.classList.add("d-none");
    els.errorBox.textContent = msg;
    els.errorBox.classList.remove("d-none");
}

function showSuccess(msg) {
    els.errorBox.classList.add("d-none");
    els.successBox.textContent = msg;
    els.successBox.classList.remove("d-none");
}

function saveSession(username) {
    localStorage.setItem("chatapp_username", username);
}

async function postJson(url, body) {
    const res = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body)
    });

    const text = await res.text();
    let data = null;
    try { data = JSON.parse(text); } catch (_) {}

    if (!res.ok) {
        const msg = data?.message || text || "Request failed";
        throw new Error(msg);
    }
    return data;
}

els.loginBtn.addEventListener("click", async () => {
    const username = els.username.value.trim();
    const password = els.password.value;

    if (!username || !password) {
        showError("Username and password are required.");
        return;
    }

    try {
        const data = await postJson(API.login, { username, password });
        saveSession(data.username || username);
        window.location.href = "chat.html";
    } catch (e) {
        showError(e.message);
    }
});

els.registerBtn.addEventListener("click", async () => {
    const username = els.username.value.trim();
    const password = els.password.value;

    if (!username || !password) {
        showError("Username and password are required.");
        return;
    }

    try {
        await postJson(API.register, { username, password });
        showSuccess("Registered successfully. You can now login.");
    } catch (e) {
        showError(e.message);
    }
});

// If already logged in, redirect
const existing = localStorage.getItem("chatapp_username");
if (existing) {
    // optional: keep user on login page; but we auto redirect for convenience
    // window.location.href = "chat.html";
}
