package com.misim.mitube_v1.api;

import com.misim.mitube_v1.VideoService;
import com.misim.mitube_v1.domain.VideoMetadata;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class VideoController {

    private final VideoService videoService;

    VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    @PostMapping("/videos")
    public ResponseEntity<Void> videoUpload(MultipartFile file) {

        String id = videoService.upload(file);

        return ResponseEntity.created(URI.create("/videos/" + id)).build();

    }

    @GetMapping("/videos/{id}")
    public ResponseEntity<Resource> videoStream(@PathVariable long id) {

        Resource resource = videoService.stream(id);

        return ResponseEntity.ok().body(resource);

    }

    @GetMapping("/videos")
    public ResponseEntity<List<VideoMetadata>> videoList() {

        List<VideoMetadata> videoMetadataList = videoService.getList();

        return ResponseEntity.ok().body(videoMetadataList);

    }

    @PostMapping("/videos/{id}/views")
    public ResponseEntity<Long> views(@PathVariable long id, HttpServletRequest request) {
        long total = videoService.recordImpression(id,
            fingerprint(request, request.getUserPrincipal()));
        return ResponseEntity.ok(total);
    }

    private String fingerprint(HttpServletRequest req, Principal principal) {
        if (principal != null) {
            return "u:" + principal.getName();
        }
        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = req.getRemoteAddr();
        }
        String ua = req.getHeader("User-Agent");
        return "g:" + ip + ":" + (ua == null ? "" : Integer.toHexString(ua.hashCode()));
    }

    @GetMapping("videos/{id}/views/peek")
    public ResponseEntity<Long> viewsPeek(@PathVariable long id) {
        long total = videoService.currentViews(id);
        return ResponseEntity.ok(total);
    }
}
