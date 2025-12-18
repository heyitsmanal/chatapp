package ma.emsi.chatapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AudioUploadResponse {
    private String audioPath; // ex: /uploads/audio/xxx.webm
}
