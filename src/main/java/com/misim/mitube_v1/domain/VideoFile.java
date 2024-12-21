package com.misim.mitube_v1.domain;

import java.nio.file.Path;
import java.nio.file.Paths;

public record VideoFile(long fileSize, Path filePath, String mimeType) {
    public static VideoFile of(long fileSize, String fileName, String filePath, String mimeType) {
        return new VideoFile(fileSize, Paths.get(filePath + fileName), mimeType);
    }
}
