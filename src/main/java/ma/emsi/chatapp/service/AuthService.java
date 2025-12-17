package ma.emsi.chatapp.service;

import ma.emsi.chatapp.dto.AuthResponse;
import ma.emsi.chatapp.dto.LoginRequest;
import ma.emsi.chatapp.dto.LogoutRequest;
import ma.emsi.chatapp.dto.RegisterRequest;
import ma.emsi.chatapp.entity.User;
import ma.emsi.chatapp.exception.BadRequestException;
import ma.emsi.chatapp.exception.NotFoundException;
import ma.emsi.chatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        validateUsernamePassword(request.getUsername(), request.getPassword());

        String username = request.getUsername().trim();

        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("Username already exists: " + username);
        }

        User user = User.builder()
                .username(username)
                .password(request.getPassword()) // simple auth as requested (no hashing)
                .connected(false)
                .build();

        userRepository.save(user);

        return new AuthResponse("User registered successfully", user.getUsername(), user.isConnected());
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        validateUsernamePassword(request.getUsername(), request.getPassword());

        String username = request.getUsername().trim();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new BadRequestException("Invalid credentials");
        }

        user.setConnected(true);
        userRepository.save(user);

        return new AuthResponse("Login successful", user.getUsername(), true);
    }

    @Transactional
    public AuthResponse logout(LogoutRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new BadRequestException("Username is required");
        }

        String username = request.getUsername().trim();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));

        user.setConnected(false);
        userRepository.save(user);

        return new AuthResponse("Logout successful", user.getUsername(), false);
    }

    private void validateUsernamePassword(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new BadRequestException("Username is required");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new BadRequestException("Password is required");
        }
    }
}
