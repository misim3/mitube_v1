package com.misim.mitube_v1;

import com.misim.mitube_v1.api.GetProgressResponse;
import com.misim.mitube_v1.api.PutProgressRequest;
import com.misim.mitube_v1.db.VideoProgressEntity;
import com.misim.mitube_v1.db.VideoProgressRepository;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProgressService {

    private final RedisProgressBuffer buffer;
    private final VideoProgressRepository repository;


    public ProgressService(RedisProgressBuffer buffer, VideoProgressRepository repository) {
        this.buffer = buffer;
        this.repository = repository;
    }

    @Transactional
    public GetProgressResponse recordSync(Long userId, Long videoId, PutProgressRequest req) {
        if (req == null || req.positionMs() == null || req.durationMs() == null) {
            throw new IllegalArgumentException("positionMs and durationMs are required");
        }

        long dur = Math.max(0, req.durationMs());
        long pos = Math.max(0, Math.min(req.positionMs(), dur)); // 0 ≤ pos ≤ dur

        Optional<VideoProgressEntity> found = repository.findByUserIdAndVideoId(userId, videoId);
        VideoProgressEntity entity = found.orElseGet(() ->
            new VideoProgressEntity(userId, videoId, pos, dur)
        );
        if (found.isPresent()) {
            entity.setPositionMs(pos);
            entity.setDurationMs(dur);
        }

        // 동기 저장 (기다렸다가 200 OK 응답)
        repository.saveAndFlush(entity);

        return new GetProgressResponse(entity.getPositionMs(), entity.getDurationMs(),
            Instant.now().toEpochMilli());
    }

    public void record(Long userId, Long videoId, PutProgressRequest req) {
        long now = (req.clientTsMs() != null) ? req.clientTsMs() : System.currentTimeMillis();
        buffer.buffer(new ProgressUpsert(userId, videoId, req.positionMs(), req.durationMs(), now));
    }

    public GetProgressResponse read(Long userId, Long videoId) {
        ProgressResponse r = buffer.readLatest(userId, videoId);
        if (r == null) {
            return null;
        }
        return new GetProgressResponse(r.positionMs(), r.durationMs(), r.updatedAtMs());
    }
}
