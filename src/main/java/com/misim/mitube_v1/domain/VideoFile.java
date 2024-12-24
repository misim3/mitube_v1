package com.misim.mitube_v1.domain;

public record VideoFile(long fileSize, String originalFileName, String savedPath, String mimeType) {
}
