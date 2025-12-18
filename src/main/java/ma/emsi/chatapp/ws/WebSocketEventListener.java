package ma.emsi.chatapp.ws;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;

@Component
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketEventListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        Object usernameObj = headerAccessor.getSessionAttributes() != null
                ? headerAccessor.getSessionAttributes().get("username")
                : null;

        if (usernameObj != null) {
            String username = usernameObj.toString();

            ChatMessage leaveMessage = ChatMessage.builder()
                    .type(MessageType.LEAVE)
                    .sender(username)
                    .content(username + " left the chat")
                    .timestamp(LocalDateTime.now())
                    .build();

            messagingTemplate.convertAndSend("/topic/public", leaveMessage);
        }
    }
}
