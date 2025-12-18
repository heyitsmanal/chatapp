package ma.emsi.chatapp.service;

import ma.emsi.chatapp.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Service
public class AudioService {

    private static final Path AUDIO_DIR = Paths.get("uploads", "audio").toAbsolutePath().normalize();

    // Accept common browser recorder outputs
    private static final Set<String> ALLOWED_EXT = Set.of("webm", "wav", "mp3", "ogg", "m4a");

    public String saveAudio(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Audio file is required");
        }

        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "audio";
        String ext = getExtension(original).toLowerCase();

        // If no ext from filename, try content-type fallback
        if (ext.isBlank()) {
            ext = extFromContentType(file.getContentType());
        }

        if (ext.isBlank() || !ALLOWED_EXT.contains(ext)) {
            throw new BadRequestException("Unsupported audio format. Allowed: " + ALLOWED_EXT);
        }

        try {
            Files.createDirectories(AUDIO_DIR);
        } catch (IOException e) {
            throw new BadRequestException("Could not create uploads/audio directory: " + e.getMessage());
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
        String safeName = "audio_" + timestamp + "_" + Math.abs(original.hashCode()) + "." + ext;

        Path target = AUDIO_DIR.resolve(safeName).normalize();
        if (!target.startsWith(AUDIO_DIR)) {
            throw new BadRequestException("Invalid file path");
        }

        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BadRequestException("Failed to save audio file: " + e.getMessage());
        }

        // Return public URL path
        return "/uploads/audio/" + safeName;
    }

    private String getExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx < 0 || idx == filename.length() - 1) return "";
        return filename.substring(idx + 1);
    }

    private String extFromContentType(String contentType) {
        if (contentType == null) return "";
        return switch (contentType.toLowerCase()) {
            case "audio/webm" -> "webm";
            case "audio/wav", "audio/x-wav" -> "wav";
            case "audio/mpeg" -> "mp3";
            case "audio/ogg" -> "ogg";
            case "audio/mp4" -> "m4a";
            default -> "";
        };
    }
}
