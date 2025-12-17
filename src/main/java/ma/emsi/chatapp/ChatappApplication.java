package ma.emsi.chatapp;

import ma.emsi.chatapp.entity.Message;
import ma.emsi.chatapp.entity.User;
import ma.emsi.chatapp.repository.MessageRepository;
import ma.emsi.chatapp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

@SpringBootApplication(scanBasePackages = {"ma.emsi.chatapp", "ma.emsi.chatapp"})
public class ChatappApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatappApplication.class, args);
    }

    @Bean
    CommandLineRunner seed(UserRepository userRepository, MessageRepository messageRepository) {
        return args -> {
            User user = userRepository.findByUsername("ahmed")
                    .orElseGet(() -> userRepository.save(
                            User.builder()
                                    .username("ahmed")
                                    .password("1234")
                                    .connected(true)
                                    .build()
                    ));

            if (messageRepository.count() == 0) {
                Message m = Message.builder()
                        .sender(user)
                        .content("Bonjour")
                        .audioPath(null)
                        .timestamp(LocalDateTime.now())
                        .build();

                messageRepository.save(m);
            }
        };
    }
}
