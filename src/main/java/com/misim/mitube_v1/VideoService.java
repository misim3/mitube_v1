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
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VideoService {

    private static final String VIEW_DELTA = "views:counter";
    private static final String VIEW_BASE = "views:base";
    private static final String VIEW_DEDUPE_NS = "views:dedupe:";
    private static final Duration DEDUPE_TTL = Duration.ofSeconds(30);

    // MAX_SIZE: 100MB
    private static final long MAX_SIZE = 100L * 1024 * 1024;
    private static final String UPLOAD_DIR = "uploads/videos";

    private final VideoMetadataRepository videoMetadataRepository;
    private final StringRedisTemplate redis;
    private final DefaultRedisScript<Long> incrAndGetScript;

    public VideoService(VideoMetadataRepository videoMetadataRepository,
        StringRedisTemplate redisTemplate, DefaultRedisScript<Long> incrAndGetScript) {
        this.videoMetadataRepository = videoMetadataRepository;
        this.redis = redisTemplate;
        this.incrAndGetScript = incrAndGetScript;
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
//
//    public List<VideoMetadata> getList() {
//
//        List<VideoMetadataEntity> videoMetadataEntities = videoMetadataRepository.findAll();
//
//        return videoMetadataEntities
//            .stream()
//            .map(v -> new VideoMetadata(v.getId(),
//                new VideoFile(v.getFileSize(), v.getOriginalFilename(), v.getFilePath(),
//                    v.getMimeType()),
//                v.getUpdatedAt()))
//            .toList();
//    }

//    /** 증가 + 즉시 합산값 반환 (Lua 한 번) */
//    public long incrementAndGet(Long videoId) {
//        Long total = redis.execute(incrAndGetScript,
//            List.of(VIEW_DELTA, VIEW_BASE), String.valueOf(videoId), "1");
//        if (total != null) return total;
//
//        // Redis 이슈 시 안전망: DB 직접 증가 후 값 반환
//        videoMetadataRepository.incrementViewCount(videoId, 1L);
//        return videoMetadataRepository.findById(videoId).map(VideoMetadataEntity::getViewCount)
//            .orElseThrow(() -> new EntityNotFoundException("video not found: " + videoId));
//    }

    /**
     * 증가(중복 억제 포함). 응답은 현재 total(base+delta)
     */
    public long recordImpression(Long videoId, String fingerprint) {
        String key = VIEW_DEDUPE_NS + videoId + ":" + fingerprint;
        Boolean first = redis.opsForValue().setIfAbsent(key, "1", DEDUPE_TTL);
        if (Boolean.TRUE.equals(first)) {
            redis.opsForHash().increment(VIEW_DELTA, String.valueOf(videoId), 1L);
        }
        return currentViews(videoId);
    }

    /**
     * 읽기 전용: base + delta (네트워크 2회, 필요시 파이프라인 가능)
     */
    public long currentViews(Long videoId) {
        String id = String.valueOf(videoId);
        Object b = redis.opsForHash().get(VIEW_BASE, id);
        Object d = redis.opsForHash().get(VIEW_DELTA, id);
        long base = (b == null) ? warmBaseFromDb(videoId) : Long.parseLong(b.toString());
        long delta = (d == null) ? 0L : Long.parseLong(d.toString());
        return base + delta;
    }

    private long warmBaseFromDb(Long videoId) {
        long base = videoMetadataRepository.findById(videoId).map(VideoMetadataEntity::getViewCount)
            .orElse(0L);
        redis.opsForHash().putIfAbsent(VIEW_BASE, String.valueOf(videoId), String.valueOf(base));
        return base;
    }

    /**
     * 1분 플러시(이전 설계) 보강: DB 반영 + base도 함께 올려서 읽기일관성 유지
     */
    @Transactional
    @Scheduled(fixedDelay = 60_000)
    public void flushViewCounters() {
        String staging = VIEW_DELTA + ":staging";
        try {
            redis.rename(VIEW_DELTA, staging);
        } catch (Exception e) {
            return; // 증가 없음/경합 시 스킵
        }
        Map<Object, Object> all = redis.opsForHash().entries(staging);
        if (all.isEmpty()) {
            redis.delete(staging);
            return;
        }

        all.forEach((k, v) -> {
            Long id = Long.valueOf(k.toString());
            long delta = Long.parseLong(v.toString());
            if (delta == 0) {
                return;
            }
            videoMetadataRepository.incrementViewCount(id, delta);
            // base도 동일 delta만큼 올려서 base+delta 합이 계속 실제값과 같도록 유지
            redis.opsForHash().increment(VIEW_BASE, k.toString(), delta);
        });
        redis.delete(staging);
    }

    // 스트리밍 직전에 호출: 증가 + 증가 후 값 반환(동일 커넥션 보장 위해 @Transactional)
//    @Transactional
//    public long addViewAndGet(Long videoId) {
//        int updated = videoMetadataRepository.incrementReturningWithLiid(videoId, 1L);
//        if (updated == 0) throw new EntityNotFoundException("video not found: " + videoId);
//        return videoMetadataRepository.getLastLiid();
//    }

//    public long getViews(Long videoId) {
//        return videoMetadataRepository.findById(videoId).map(VideoMetadataEntity::getViewCount)
//            .orElseThrow(() -> new EntityNotFoundException("video not found: " + videoId));
//    }

    public List<VideoMetadata> getList() {
        return videoMetadataRepository.findAll().stream()
            .map(v -> new VideoMetadata(
                v.getId(),
                new VideoFile(v.getFileSize(), v.getOriginalFilename(), v.getFilePath(),
                    v.getMimeType()),
                v.getViewCount(),
                v.getUpdatedAt()))
            .toList();
    }
}
