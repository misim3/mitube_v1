package com.misim.mitube_v1.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VideoMetadataRepository extends JpaRepository<VideoMetadataEntity, Long> {


    // MySQL: LAST_INSERT_ID 트릭으로 "증가값을 같은 커넥션에서 즉시 조회"
    @Modifying
    @Query(value = """
        UPDATE video_metadata_entity
        SET view_count = LAST_INSERT_ID(view_count + :delta)
        WHERE id = :id
        """, nativeQuery = true)
    int incrementReturningWithLiid(@Param("id") Long id, @Param("delta") long delta);

    @Query(value = "SELECT LAST_INSERT_ID()", nativeQuery = true)
    long getLastLiid();

    @Modifying
    @Query("update VideoMetadataEntity v set v.viewCount = v.viewCount + :delta where v.id = :id")
    int incrementViewCount(@Param("id") Long id, @Param("delta") long delta);
}
