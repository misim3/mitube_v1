package com.misim.mitube_v1.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class VideoMetadataEntity extends BaseEntity {

    @Column
    private String originalFilename;

    @Column
    private String filePath;

    @Column
    private Long fileSize;

    @Column
    private String mimeType;

    protected VideoMetadataEntity() {}

    public VideoMetadataEntity(String originalFilename, String filePath, Long fileSize,
        String mimeType) {
        this.originalFilename = originalFilename;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getFilePath() {
        return filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public String getMimeType() {
        return mimeType;
    }

}
