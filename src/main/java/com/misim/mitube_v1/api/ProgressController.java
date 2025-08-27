package com.misim.mitube_v1.api;

import com.misim.mitube_v1.ProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/videos/{videoId}/progress")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

//    @PutMapping("/sync")
//    public ResponseEntity<GetProgressResponse> put(
//        @PathVariable Long videoId,
//        @RequestBody PutProgressRequest req,
//        @RequestHeader("X-User-Id") Long userId
//    ) {
//        GetProgressResponse resp = progressService.recordSync(userId, videoId, req);
//        return ResponseEntity.ok(resp);
//    }

    @PutMapping
    public ResponseEntity<Void> put(
        @PathVariable Long videoId,
        @RequestBody PutProgressRequest req,
        @RequestHeader(value = "X-User-Id") Long userId // 데모: 헤더로 전달
    ) {
        progressService.record(userId, videoId, req);
        return ResponseEntity.accepted().build(); // 즉시 반환
    }

    @GetMapping
    public ResponseEntity<GetProgressResponse> get(
        @PathVariable Long videoId,
        @RequestHeader("X-User-Id") Long userId
    ) {
        GetProgressResponse resp = progressService.read(userId, videoId);
        return (resp == null) ? ResponseEntity.noContent().build() : ResponseEntity.ok(resp);
    }
}
