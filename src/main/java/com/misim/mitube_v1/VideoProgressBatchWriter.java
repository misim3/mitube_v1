package com.misim.mitube_v1;

import java.util.List;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class VideoProgressBatchWriter {

    // DB 종류에 따라 하나만 남겨서 사용하세요.
    private static final String UPSERT_PG = """
        INSERT INTO video_progress_entity (user_id, video_id, position_ms, duration_ms, created_at, updated_at)
        VALUES (?, ?, ?, ?, NOW(), NOW())
        ON CONFLICT (user_id, video_id) DO UPDATE
        SET position_ms = EXCLUDED.position_ms,
            duration_ms = EXCLUDED.duration_ms,
            updated_at = NOW()
        """;
    private static final String UPSERT_MY = """
        INSERT INTO video_progress_entity (user_id, video_id, position_ms, duration_ms, created_at, updated_at)
        VALUES (?, ?, ?, ?, NOW(), NOW())
        ON DUPLICATE KEY UPDATE
            position_ms = VALUES(position_ms),
            duration_ms = VALUES(duration_ms),
            updated_at = NOW()
        """;
    private final JdbcTemplate jdbc;

    public VideoProgressBatchWriter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void upsertAll(List<ProgressUpsert> rows, boolean postgres) {
        final String sql = postgres ? UPSERT_PG : UPSERT_MY;
        jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(java.sql.PreparedStatement ps, int i)
                throws java.sql.SQLException {
                ProgressUpsert p = rows.get(i);
                ps.setLong(1, p.userId());
                ps.setLong(2, p.videoId());
                ps.setLong(3, p.positionMs());
                ps.setLong(4, p.durationMs());
            }

            @Override
            public int getBatchSize() {
                return rows.size();
            }
        });
    }
}
