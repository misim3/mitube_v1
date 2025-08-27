package com.misim.mitube_v1;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisProgressBuffer {

    private final StringRedisTemplate redis;
    private final Duration ttl;

    public RedisProgressBuffer(
        StringRedisTemplate redis,
        @Value("${progress.redis.ttl-days:14}") long ttlDays
    ) {
        this.redis = redis;
        this.ttl = Duration.ofDays(ttlDays);
    }

    static String hashKey(Long uid, Long vid) {
        return "progress:h:" + uid + ":" + vid;
    }

    static String member(Long uid, Long vid) {
        return uid + ":" + vid;
    }

    public void buffer(ProgressUpsert p) {
        String hKey = hashKey(p.userId(), p.videoId());
        String member = member(p.userId(), p.videoId());

        // 해시 업데이트 + TTL
        redis.opsForHash().put(hKey, "pos", String.valueOf(p.positionMs()));
        redis.opsForHash().put(hKey, "dur", String.valueOf(p.durationMs()));
        redis.opsForHash().put(hKey, "ts", String.valueOf(p.clientTs()));
        redis.expire(hKey, ttl);

        // 더티 표식 (ZSET) - 최신 ts로 갱신
        redis.opsForZSet().add("progress:dirty", member, p.clientTs());
    }

    public ProgressResponse readLatest(Long userId, Long videoId) {
        String hKey = hashKey(userId, videoId);
        Object pos = redis.opsForHash().get(hKey, "pos");
        Object dur = redis.opsForHash().get(hKey, "dur");
        Object ts = redis.opsForHash().get(hKey, "ts");
        if (pos == null || dur == null || ts == null) {
            return null;
        }
        return new ProgressResponse(Long.valueOf(pos.toString()), Long.valueOf(dur.toString()),
            Long.valueOf(ts.toString()));
    }
}
