package ma.emsi.chatapp.service;


import ma.emsi.chatapp.dto.MessageDto;
import ma.emsi.chatapp.entity.Message;
import ma.emsi.chatapp.entity.User;
import ma.emsi.chatapp.exception.BadRequestException;
import ma.emsi.chatapp.repository.MessageRepository;
import ma.emsi.chatapp.repository.UserRepository;
import ma.emsi.chatapp.ws.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Transactional
    public Message saveFromWebSocket(ChatMessage chatMessage) {
        if (chatMessage.getSender() == null || chatMessage.getSender().trim().isEmpty()) {
            throw new BadRequestException("Sender is required");
        }

        String username = chatMessage.getSender().trim();

        User sender = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("Unknown user: " + username + " (register/login first)"));

        // Ensure timestamp
        LocalDateTime ts = chatMessage.getTimestamp() != null ? chatMessage.getTimestamp() : LocalDateTime.now();

        Message entity = Message.builder()
                .sender(sender)
                .content(chatMessage.getContent())
                .audioPath(chatMessage.getAudioPath())
                .timestamp(ts)
                .build();

        return messageRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<MessageDto> getHistory() {
        return messageRepository.findAllByOrderByTimestampAsc()
                .stream()
                .map(m -> new MessageDto(
                        m.getId(),
                        m.getSender().getUsername(),
                        m.getContent(),
                        m.getAudioPath(),
                        m.getTimestamp()
                ))
                .toList();
    }
}
