package com.misim.mitube_v1.domain;

import java.time.LocalDateTime;

public record VideoMetadata(long id, VideoFile videoFile, LocalDateTime uploadedAt) {
}
