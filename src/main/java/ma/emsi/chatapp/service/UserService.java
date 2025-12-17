package ma.emsi.chatapp.service;

import ma.emsi.chatapp.dto.UserDto;
import ma.emsi.chatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserDto> getOnlineUsers() {
        return userRepository.findByConnectedTrue()
                .stream()
                .map(u -> new UserDto(u.getId(), u.getUsername(), u.isConnected()))
                .toList();
    }
}
