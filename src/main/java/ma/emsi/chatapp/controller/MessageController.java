package ma.emsi.chatapp.controller;

import ma.emsi.chatapp.dto.MessageDto;
import ma.emsi.chatapp.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/history")
    public ResponseEntity<List<MessageDto>> history() {
        return ResponseEntity.ok(messageService.getHistory());
    }
}
