package ma.emsi.chatapp.ws;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    private Long id;               // DB id after save (optional)
    private MessageType type;      // CHAT / JOIN / LEAVE
    private String sender;         // username
    private String content;        // text message
    private String audioPath;      // link to audio (later)
    private LocalDateTime timestamp;
}
