package com.misim.mitube_v1;

import com.misim.mitube_v1.domain.VideoMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoMetadataRepository extends JpaRepository<VideoMetadata, Long> {

}
