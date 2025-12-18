# ChatApp (Spring Boot + WebSocket + MySQL)

Real-time client–server chat app using **Spring Boot**, **WebSocket (SockJS + STOMP)**, **Spring Data JPA (Hibernate)** and **MySQL**.
Supports:
- Simple authentication (register/login)
- Real-time chat (broadcast to all)
- Message history stored in DB
- Audio messages (upload + stored on server)
- Online users list (real-time)

---

## Tech Stack
- Backend: Spring Boot, Spring WebSocket, Spring Data JPA, Hibernate
- Database: MySQL
- Frontend: Bootstrap, HTML/CSS, JavaScript, SockJS + STOMP
- Build: Maven
- Lombok

---

## Run the app

1) Build
mvn clean package -DskipTests

2) Start
mvn spring-boot:run


App runs on:

http://localhost:8080/login.html

WebSocket
Endpoint

SockJS endpoint: /ws

Client send destinations

/app/chat.addUser

/app/chat.send

Subscriptions

/topic/public (chat)

/topic/users (online users)
