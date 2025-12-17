package ma.emsi.chatapp.controller;

import ma.emsi.chatapp.dto.UserDto;
import ma.emsi.chatapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/online")
    public ResponseEntity<List<UserDto>> onlineUsers() {
        return ResponseEntity.ok(userService.getOnlineUsers());
    }
}
