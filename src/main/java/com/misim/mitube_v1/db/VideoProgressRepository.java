package com.misim.mitube_v1.db;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoProgressRepository extends JpaRepository<VideoProgressEntity, Long> {

    Optional<VideoProgressEntity> findByUserIdAndVideoId(Long userId, Long videoId);
}
