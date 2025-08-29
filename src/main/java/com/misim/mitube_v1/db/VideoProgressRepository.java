package com.misim.mitube_v1.db;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VideoProgressRepository extends JpaRepository<VideoProgressEntity, Long> {

    Optional<VideoProgressEntity> findByUserIdAndVideoId(Long userId, Long videoId);


    @Modifying
    @Query(value = """
          INSERT INTO video_progress_entity (user_id, video_id, position_ms, duration_ms, created_at, updated_at)
          VALUES (:userId, :videoId, :pos, :dur, NOW(), NOW())
          ON DUPLICATE KEY UPDATE
            position_ms = VALUES(position_ms),
            duration_ms = VALUES(duration_ms),
            updated_at = NOW()
        """, nativeQuery = true)
    void upsert(@Param("userId") long userId,
        @Param("videoId") long videoId,
        @Param("pos") long positionMs,
        @Param("dur") long durationMs);
}
