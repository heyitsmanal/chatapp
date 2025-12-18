package ma.emsi.chatapp.controller;


import ma.emsi.chatapp.dto.AudioUploadResponse;
import ma.emsi.chatapp.service.AudioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/audio")
@RequiredArgsConstructor
public class AudioController {

    private final AudioService audioService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AudioUploadResponse> upload(@RequestPart("file") MultipartFile file) {
        String audioPath = audioService.saveAudio(file);
        return ResponseEntity.ok(new AudioUploadResponse(audioPath));
    }
}

