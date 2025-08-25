package com.misim.mitube_v1;

import com.misim.mitube_v1.db.VideoMetadataEntity;
import com.misim.mitube_v1.db.VideoMetadataRepository;
import com.misim.mitube_v1.domain.VideoFile;
import com.misim.mitube_v1.domain.VideoMetadata;
import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
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
            throw new IllegalArgumentException("Invalid video type.");
        }

        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("File is too large.");
        }

        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to create upload directory", e);
        }

        String uniqueFileName = UUID.randomUUID() + Objects.requireNonNull(
            file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf("."));

        Path filePath = Paths.get(UPLOAD_DIR, uniqueFileName);

        try {
            Files.write(filePath, file.getBytes());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store file", e);
        }

        VideoMetadataEntity videoMetadataEntity = new VideoMetadataEntity(file.getOriginalFilename(), filePath.toString(), file.getSize(), file.getContentType());

        videoMetadataRepository.save(videoMetadataEntity);

        return videoMetadataEntity.getId().toString();

    }

    public Resource stream(long videoId) {

        VideoMetadataEntity videoMetadataEntity = videoMetadataRepository.findById(videoId)
            .orElseThrow(() -> new EntityNotFoundException("Video not found"));

        try {

            Path path = Paths.get(videoMetadataEntity.getFilePath());
            Resource resource = new UrlResource(path.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new IllegalStateException("Not found or readable file");
            }

        } catch (MalformedURLException e) {
            throw new UncheckedIOException(
                "Error while loading video file: " + videoMetadataEntity.getFilePath(), e);
        }

    }

    public List<VideoMetadata> getList() {

        List<VideoMetadataEntity> videoMetadataEntities = videoMetadataRepository.findAll();

        return videoMetadataEntities
            .stream()
            .map(v -> new VideoMetadata(v.getId(),
                new VideoFile(v.getFileSize(), v.getOriginalFilename(), v.getFilePath(),
                    v.getMimeType()),
                v.getCreatedAt()))
            .toList();
    }
}
