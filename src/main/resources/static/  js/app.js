const API = {
    logout: "/api/auth/logout",
    history: "/api/messages/history",
    uploadAudio: "/api/audio/upload"
};

const state = {
    username: localStorage.getItem("chatapp_username"),
    stompClient: null,
    connected: false,
    mediaRecorder: null,
    chunks: [],
    isRecording: false
};

const els = {
    currentUser: document.getElementById("currentUser"),
    wsStatus: document.getElementById("wsStatus"),
    chatArea: document.getElementById("chatArea"),
    usersList: document.getElementById("usersList"),
    messageInput: document.getElementById("messageInput"),
    sendBtn: document.getElementById("sendBtn"),
    recordBtn: document.getElementById("recordBtn"),
    stopBtn: document.getElementById("stopBtn"),
    logoutBtn: document.getElementById("logoutBtn"),
    previewAudio: document.getElementById("previewAudio")
};

function ensureLoggedIn() {
    if (!state.username) {
        window.location.href = "login.html";
        return false;
    }
    els.currentUser.textContent = state.username;
    return true;
}

function setWsStatus(text, ok) {
    els.wsStatus.textContent = text;
    els.wsStatus.classList.toggle("text-success", !!ok);
    els.wsStatus.classList.toggle("text-muted", !ok);
}

function scrollChatToBottom() {
    els.chatArea.scrollTop = els.chatArea.scrollHeight;
}

function fmtTime(ts) {
    if (!ts) return "";
    try {
        const d = new Date(ts);
        return d.toLocaleString();
    } catch (_) {
        return ts;
    }
}

function addSystemMessage(text) {
    const div = document.createElement("div");
    div.className = "msg system";
    div.innerHTML = `<div class="meta">system</div><div>${escapeHtml(text)}</div>`;
    els.chatArea.appendChild(div);
    scrollChatToBottom();
}

function addChatMessage({ sender, content, audioPath, timestamp, type }) {
    const isMe = sender === state.username;

    // System messages (JOIN/LEAVE)
    if (type === "JOIN" || type === "LEAVE") {
        addSystemMessage(content || `${sender} ${type.toLowerCase()}`);
        return;
    }

    const div = document.createElement("div");
    div.className = `msg ${isMe ? "me" : "other"}`;

    const meta = document.createElement("div");
    meta.className = "meta";
    meta.textContent = `${sender || "unknown"} • ${fmtTime(timestamp)}`;

    const body = document.createElement("div");
    body.innerHTML = content ? escapeHtml(content) : "";

    div.appendChild(meta);
    div.appendChild(body);

    if (audioPath) {
        const audio = document.createElement("audio");
        audio.controls = true;
        audio.src = audioPath;
        div.appendChild(audio);
    }

    els.chatArea.appendChild(div);
    scrollChatToBottom();
}

function escapeHtml(str) {
    return String(str)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

async function loadHistory() {
    const res = await fetch(API.history);
    if (!res.ok) return;

    const history = await res.json();
    els.chatArea.innerHTML = "";

    history.forEach(m => {
        addChatMessage({
            sender: m.sender,
            content: m.content,
            audioPath: m.audioPath,
            timestamp: m.timestamp,
            type: "CHAT"
        });
    });

    addSystemMessage("History loaded.");
}

function renderUsers(users) {
    els.usersList.innerHTML = "";
    (users || []).forEach(u => {
        const li = document.createElement("li");
        li.className = "list-group-item d-flex justify-content-between align-items-center";

        const name = document.createElement("span");
        name.textContent = u.username;

        const badge = document.createElement("span");
        badge.className = "badge text-bg-success badge-online";
        badge.textContent = "online";

        li.appendChild(name);
        li.appendChild(badge);
        els.usersList.appendChild(li);
    });

    if (!users || users.length === 0) {
        const li = document.createElement("li");
        li.className = "list-group-item text-muted";
        li.textContent = "No users online";
        els.usersList.appendChild(li);
    }
}

function connectWebSocket() {
    setWsStatus("Connecting...", false);

    const socket = new SockJS("/ws");
    const stomp = Stomp.over(socket);

    // mute stomp logs
    stomp.debug = () => {};

    stomp.connect({}, async () => {
        state.stompClient = stomp;
        state.connected = true;
        setWsStatus("Connected", true);

        // subscribe chat messages
        stomp.subscribe("/topic/public", (msg) => {
            const payload = JSON.parse(msg.body);
            addChatMessage(payload);
        });

        // subscribe online users
        stomp.subscribe("/topic/users", (msg) => {
            const payload = JSON.parse(msg.body);
            renderUsers(payload.users);
        });

        // load history after connect
        await loadHistory();

        // announce join
        stomp.send("/app/chat.addUser", {}, JSON.stringify({
            type: "JOIN",
            sender: state.username,
            content: null,
            audioPath: null,
            timestamp: new Date().toISOString()
        }));
    }, () => {
        state.connected = false;
        setWsStatus("Disconnected", false);
        addSystemMessage("WebSocket disconnected.");
    });
}

function sendTextMessage() {
    const text = els.messageInput.value.trim();
    if (!text) return;
    if (!state.connected || !state.stompClient) {
        addSystemMessage("Not connected to WebSocket.");
        return;
    }

    state.stompClient.send("/app/chat.send", {}, JSON.stringify({
        type: "CHAT",
        sender: state.username,
        content: text,
        audioPath: null,
        timestamp: new Date().toISOString()
    }));

    els.messageInput.value = "";
}

async function uploadAudioBlob(blob) {
    const form = new FormData();
    // MediaRecorder usually outputs webm
    form.append("file", blob, "recording.webm");

    const res = await fetch(API.uploadAudio, {
        method: "POST",
        body: form
    });

    const text = await res.text();
    let data = null;
    try { data = JSON.parse(text); } catch (_) {}

    if (!res.ok) {
        throw new Error(data?.message || text || "Audio upload failed");
    }

    return data.audioPath;
}

async function startRecording() {
    if (state.isRecording) return;

    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
        addSystemMessage("Your browser does not support audio recording.");
        return;
    }

    try {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
        state.chunks = [];

        const recorder = new MediaRecorder(stream);
        state.mediaRecorder = recorder;

        recorder.ondataavailable = (e) => {
            if (e.data && e.data.size > 0) state.chunks.push(e.data);
        };

        recorder.onstop = async () => {
            // stop microphone tracks
            stream.getTracks().forEach(t => t.stop());

            const blob = new Blob(state.chunks, { type: "audio/webm" });
            state.chunks = [];

            // preview
            const url = URL.createObjectURL(blob);
            els.previewAudio.src = url;
            els.previewAudio.classList.remove("d-none");

            try {
                const audioPath = await uploadAudioBlob(blob);

                if (!state.connected || !state.stompClient) {
                    addSystemMessage("Audio uploaded but not connected to WebSocket.");
                    return;
                }

                state.stompClient.send("/app/chat.send", {}, JSON.stringify({
                    type: "CHAT",
                    sender: state.username,
                    content: null,
                    audioPath: audioPath,
                    timestamp: new Date().toISOString()
                }));

            } catch (e) {
                addSystemMessage("Audio error: " + e.message);
            }
        };

        recorder.start();
        state.isRecording = true;

        els.recordBtn.classList.add("d-none");
        els.stopBtn.classList.remove("d-none");

        addSystemMessage("Recording started...");
    } catch (e) {
        addSystemMessage("Microphone permission denied or error.");
    }
}

function stopRecording() {
    if (!state.isRecording || !state.mediaRecorder) return;

    state.mediaRecorder.stop();
    state.isRecording = false;

    els.stopBtn.classList.add("d-none");
    els.recordBtn.classList.remove("d-none");

    addSystemMessage("Recording stopped. Uploading...");
}

async function logout() {
    try {
        await fetch(API.logout, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username: state.username })
        });
    } catch (_) {}

    localStorage.removeItem("chatapp_username");
    window.location.href = "login.html";
}

// Events
els.sendBtn.addEventListener("click", sendTextMessage);
els.messageInput.addEventListener("keydown", (e) => {
    if (e.key === "Enter") sendTextMessage();
});
els.recordBtn.addEventListener("click", startRecording);
els.stopBtn.addEventListener("click", stopRecording);
els.logoutBtn.addEventListener("click", logout);

// Init
if (ensureLoggedIn()) {
    connectWebSocket();
}
