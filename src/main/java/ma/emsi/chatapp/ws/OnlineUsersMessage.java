package ma.emsi.chatapp.ws;

import ma.emsi.chatapp.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class OnlineUsersMessage {
    private List<UserDto> users;
    private LocalDateTime timestamp;
}

