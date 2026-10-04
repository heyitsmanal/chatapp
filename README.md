# ChatApp (Spring Boot + WebSocket + MySQL)

Real-time client–server chat application built with Spring Boot, WebSocket (SockJS + STOMP), Spring Data JPA, and MySQL.

## ✨ Features
- Register and log in
- Real-time public chat
- Persist message history in MySQL
- Upload and send audio messages
- Display online users in real time

## 🧰 Tech Stack
- **Backend:** Spring Boot, Spring WebSocket, Spring Data JPA, Hibernate
- **Database:** MySQL
- **Frontend:** HTML, CSS, JavaScript, Bootstrap, SockJS, STOMP
- **Build:** Maven

## 🔄 WebSocket Flow
- SockJS endpoint: `/ws`
- Send messages: `/app/chat.send`
- Add users: `/app/chat.addUser`
- Subscribe to chat: `/topic/public`
- Subscribe to online users: `/topic/users`

## 🚀 Run locally

### 1. Configure MySQL
Create a database named `chatapp_db` and set the connection values through environment variables. See `.env.example`.

### 2. Build
```bash
mvn clean package -DskipTests
```

### 3. Start
```bash
mvn spring-boot:run
```

Open:

```text
http://localhost:8080/login.html
```

## 🔐 Configuration
Database credentials are read from environment variables. Do not commit real credentials.
