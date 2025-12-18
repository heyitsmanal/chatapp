package ma.emsi.chatapp.service;


import ma.emsi.chatapp.dto.UserDto;
import ma.emsi.chatapp.entity.User;
import ma.emsi.chatapp.exception.BadRequestException;
import ma.emsi.chatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserDto> getOnlineUsers() {
        return userRepository.findByConnectedTrue()
                .stream()
                .map(u -> new UserDto(u.getId(), u.getUsername(), u.isConnected()))
                .toList();
    }

    @Transactional
    public void setConnected(String username, boolean connected) {
        if (username == null || username.trim().isEmpty()) {
            throw new BadRequestException("Username is required");
        }

        String u = username.trim();

        User user = userRepository.findByUsername(u)
                .orElseThrow(() -> new BadRequestException("Unknown user: " + u + " (register/login first)"));

        user.setConnected(connected);
        userRepository.save(user);
    }
}

