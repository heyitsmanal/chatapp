package ma.emsi.chatapp.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MessageDto {
    private Long id;
    private String sender;
    private String content;
    private String audioPath;
    private LocalDateTime timestamp;
}
