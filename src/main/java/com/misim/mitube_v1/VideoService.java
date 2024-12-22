package com.misim.mitube_v1;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VideoService {

    // MAX_SIZE: 100MB
    private static final long MAX_SIZE = 100L * 1024 * 1024;
    private static final String UPLOAD_DIR = "uploads/videos";
    private final VideoMetadataRepository videoMetadataRepository;

    public VideoService(VideoMetadataRepository videoMetadataRepository) {
        this.videoMetadataRepository = videoMetadataRepository;
    }

    public String upload(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (!Objects.requireNonNull(file.getContentType()).equals("video/mp4")) {
            throw new RuntimeException("Invalid video type.");
        }

        if (file.getSize() > MAX_SIZE) {
            throw new RuntimeException("File is too large.");
        }

        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            if (!uploadDir.mkdirs()) {
                throw new RuntimeException("Unable to create upload directory.");
            }
        }

        String uniqueFileName = UUID.randomUUID() + Objects.requireNonNull(
            file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf("."));

        Path filePath = Paths.get(UPLOAD_DIR, uniqueFileName);

        try {
            Files.write(filePath, file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }

        VideoMetadataEntity videoMetadataEntity = new VideoMetadataEntity(file.getOriginalFilename(), filePath.toString(), file.getSize(), file.getContentType());

        videoMetadataRepository.save(videoMetadataEntity);

        return videoMetadataEntity.getId().toString();

    }


}
