package com.misim.mitube_v1;

import com.misim.mitube_v1.api.GetProgressResponse;
import com.misim.mitube_v1.api.PutProgressRequest;
import com.misim.mitube_v1.db.VideoProgressRepository;
import org.springframework.stereotype.Service;

@Service
public class ProgressService {

    private final RedisProgressBuffer buffer;
    private final VideoProgressRepository repository;


    public ProgressService(RedisProgressBuffer buffer, VideoProgressRepository repository) {
        this.buffer = buffer;
        this.repository = repository;
    }

//    @Transactional
//    public GetProgressResponse recordSync(Long userId, Long videoId, PutProgressRequest req) {
//        long dur = Math.max(0, req.durationMs());
//        long pos = Math.max(0, Math.min(req.positionMs(), dur));
//        repository.upsert(userId, videoId, pos, dur);
//        return new GetProgressResponse(pos, dur, System.currentTimeMillis());
//    }

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

//    @Transactional(readOnly = true)
//    public GetProgressResponse read(Long userId, Long videoId) {
//        return repository.findByUserIdAndVideoId(userId, videoId)
//            .map(e -> new GetProgressResponse(e.getPositionMs(), e.getDurationMs(), e.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()))
//            .orElse(null);
//    }
}
