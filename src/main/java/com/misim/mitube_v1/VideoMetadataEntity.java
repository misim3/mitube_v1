package com.misim.mitube_v1;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class VideoMetadataEntity extends BaseEntity {

    @Column
    private String fileName;

    @Column
    private String filePath;

    @Column
    private Long fileSize;

    @Column
    private String mimeType;

    protected VideoMetadataEntity() {}

    public VideoMetadataEntity(String fileName, String filePath, Long fileSize, String mimeType) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.mimeType = mimeType;
    }

    public String getFileName() {
        return fileName;
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
