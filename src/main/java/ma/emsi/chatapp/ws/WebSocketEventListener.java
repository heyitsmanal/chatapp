package ma.emsi.chatapp.ws;

import ma.emsi.chatapp.dto.UserDto;
import ma.emsi.chatapp.service.UserService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;

    public WebSocketEventListener(SimpMessagingTemplate messagingTemplate, UserService userService) {
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        Object usernameObj = headerAccessor.getSessionAttributes() != null
                ? headerAccessor.getSessionAttributes().get("username")
                : null;

        if (usernameObj == null) {
            return;
        }

        String username = usernameObj.toString();

        // mark connected=false in DB (best effort)
        try {
            userService.setConnected(username, false);
        } catch (Exception ignored) {
            // if user doesn't exist, we just skip DB update
        }

        // broadcast LEAVE message to chat
        ChatMessage leaveMessage = ChatMessage.builder()
                .type(MessageType.LEAVE)
                .sender(username)
                .content(username + " left the chat")
                .timestamp(LocalDateTime.now())
                .build();
        messagingTemplate.convertAndSend("/topic/public", leaveMessage);

        // broadcast online users list
        List<UserDto> online = userService.getOnlineUsers();
        messagingTemplate.convertAndSend(
                "/topic/users",
                new OnlineUsersMessage(online, LocalDateTime.now())
        );
    }
}
