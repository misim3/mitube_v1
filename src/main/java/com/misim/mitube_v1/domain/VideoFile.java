package com.misim.mitube_v1.domain;

import java.nio.file.Path;
import java.nio.file.Paths;

public record VideoFile(long fileSize, String fileName, String filePath, String mimeType) {
}
