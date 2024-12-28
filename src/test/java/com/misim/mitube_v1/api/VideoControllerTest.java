package com.misim.mitube_v1.api;

import com.misim.mitube_v1.VideoService;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class VideoControllerTest {

    @Autowired
    private VideoService videoService;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void videoUpload_shouldSaveAndReturnLocation_whenSuccessful() throws IOException {

        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.mp4", "video/mp4",
            "Hello World".getBytes());

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(MediaType.parseMediaType("video/mp4")); // 비디오 타입 설정
        HttpEntity<ByteArrayResource> filePart = new HttpEntity<>(
            new ByteArrayResource(multipartFile.getBytes()) {
                @Override
                public String getFilename() {
                    return multipartFile.getOriginalFilename();
                }
            }, fileHeaders);
        body.add("file", filePart);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Void> response = restTemplate.postForEntity("/videos", request, Void.class);
    }

    @Test
    void videoUpload_shouldThrowException_whenFileEmpty() {
    }

    @Test
    void videoUpload_shouldThrowException_whenNotSupportFileType() {
    }

    @Test
    void videoUpload_shouldThrowException_whenNotSupportFileSize() {
    }

    @Test
    void videoUpload_shouldThrowException_whenDirectoryNotCreated() {
    }

    @Test
    void videoUpload_shouldThrowException_whenFileNotWritten() {
    }

    @Test
    void videoStream_shouldReturnResource_whenSuccessful() {
    }

    @Test
    void videoStream_shouldThrowException_whenMetadataNotFound() {
    }

    @Test
    void videoStream_shouldThrowException_whenFileNotFound() {
    }

    @Test
    void videoStream_shouldThrowException_whenFileNotReadable() {
    }

    @Test
    void videoStream_shouldThrowException_whenFileNotLoaded() {
    }

    @Test
    void videoList_shouldReturnMetadata_whenSuccessful() {
    }
}
