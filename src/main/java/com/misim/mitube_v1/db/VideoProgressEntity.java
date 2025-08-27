package com.misim.mitube_v1.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class VideoProgressEntity extends BaseEntity {

    @Column
    private Long userId;

    @Column
    private Long videoId;

    @Column
    private Long positionMs;

    @Column
    private Long durationMs;

    protected VideoProgressEntity() {
    }

    public VideoProgressEntity(Long userId, Long videoId, Long positionMs, Long durationMs) {
        this.userId = userId;
        this.videoId = videoId;
        this.positionMs = positionMs;
        this.durationMs = durationMs;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getVideoId() {
        return videoId;
    }

    public Long getPositionMs() {
        return positionMs;
    }

    public void setPositionMs(Long positionMs) {
        this.positionMs = positionMs;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }
}
