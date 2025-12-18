package ma.emsi.chatapp.ws;

import ma.emsi.chatapp.dto.UserDto;
import ma.emsi.chatapp.entity.Message;
import ma.emsi.chatapp.service.MessageService;
import ma.emsi.chatapp.service.UserService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class ChatWebSocketController {

    private final MessageService messageService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(MessageService messageService,
                                   UserService userService,
                                   SimpMessagingTemplate messagingTemplate) {
        this.messageService = messageService;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        chatMessage.setType(MessageType.CHAT);
        if (chatMessage.getTimestamp() == null) {
            chatMessage.setTimestamp(LocalDateTime.now());
        }

        // Save in DB
        Message saved = messageService.saveFromWebSocket(chatMessage);
        chatMessage.setId(saved.getId());

        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage,
                               SimpMessageHeaderAccessor headerAccessor) {

        // store username in session
        if (headerAccessor.getSessionAttributes() != null) {
            headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        }

        // mark connected=true in DB
        userService.setConnected(chatMessage.getSender(), true);

        // broadcast online users list
        List<UserDto> online = userService.getOnlineUsers();
        messagingTemplate.convertAndSend(
                "/topic/users",
                new OnlineUsersMessage(online, LocalDateTime.now())
        );

        // return JOIN message to public chat
        chatMessage.setType(MessageType.JOIN);
        if (chatMessage.getTimestamp() == null) {
            chatMessage.setTimestamp(LocalDateTime.now());
        }
        chatMessage.setContent(chatMessage.getSender() + " joined the chat");
        return chatMessage;
    }
}
