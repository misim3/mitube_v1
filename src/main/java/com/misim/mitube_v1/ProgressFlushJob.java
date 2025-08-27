package com.misim.mitube_v1;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ProgressFlushJob {

    private final Logger log = LoggerFactory.getLogger(ProgressFlushJob.class);
    private final StringRedisTemplate redis;
    private final RedisProgressBuffer buffer;
    private final VideoProgressBatchWriter writer;
    private final long matureMs;
    private final int batchSize;
    private final boolean postgres;

    public ProgressFlushJob(
        StringRedisTemplate redis,
        RedisProgressBuffer buffer,
        VideoProgressBatchWriter writer,
        @Value("${progress.flush.mature-ms:5000}") long matureMs,
        @Value("${progress.flush.batch-size:500}") int batchSize,
        @Value("${app.db.postgres:false}") boolean postgres
    ) {
        this.redis = redis;
        this.buffer = buffer;
        this.writer = writer;
        this.matureMs = matureMs;
        this.batchSize = batchSize;
        this.postgres = postgres;
    }

    @Scheduled(fixedDelayString = "${progress.flush.interval-ms:1000}")
    public void flush() {
        log.info("PROGRESS flush tick");
        long until = Instant.now().toEpochMilli() - matureMs;
        Set<TypedTuple<String>> tuples = redis.opsForZSet()
            .rangeByScoreWithScores("progress:dirty", 0, until, 0, batchSize);
        if (tuples == null || tuples.isEmpty()) {
            return;
        }

        log.info("PROGRESS flush candidates={}", tuples.size());
        List<String> members = tuples.stream().map(TypedTuple::getValue).toList();
        List<ProgressUpsert> rows = new ArrayList<>(members.size());

        for (String m : members) {
            String[] parts = m.split(":");
            Long uid = Long.valueOf(parts[0]);
            Long vid = Long.valueOf(parts[1]);

            ProgressResponse r = buffer.readLatest(uid, vid);
            if (r != null) {
                rows.add(
                    new ProgressUpsert(uid, vid, r.positionMs(), r.durationMs(), r.updatedAtMs()));
            }
        }

        if (!rows.isEmpty()) {
            writer.upsertAll(rows, postgres);
        }

        log.info("PROGRESS upsert rows={}", rows.size());
        redis.opsForZSet().remove("progress:dirty", members.toArray());
    }
}
