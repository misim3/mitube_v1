package com.misim.mitube_v1.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.misim.mitube_v1.VideoService;
import com.misim.mitube_v1.domain.VideoMetadata;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    void videoUpload_shouldSaveAndReturnLocation_whenSuccessful() {

        //given
        String filename = "file";
        String originalFilename = "originalFilename.mp4";
        String contentType = "video/mp4";
        String content = "content";

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(MediaType.parseMediaType(contentType));

        HttpEntity<ByteArrayResource> filePart = new HttpEntity<>(
            new ByteArrayResource(content.getBytes()) {
                @Override
                public String getFilename() {
                    return originalFilename;
                }
            },
            fileHeaders
        );

        body.add(filename, filePart);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        //when
        ResponseEntity<Void> response = restTemplate.postForEntity("/videos", request, Void.class);

        //then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String location = Objects.requireNonNull(response.getHeaders().getLocation()).toString();
        assertThat(location).matches("^/videos/\\d+$");

        Long id = Long.parseLong(location.split("/")[2]);

        List<VideoMetadata> videoMetadata = videoService.getList();

        assertThat(videoMetadata.size()).isEqualTo(1);
        assertThat(videoMetadata.get(0).id()).isEqualTo(id);
        assertThat(videoMetadata.get(0).videoFile().originalFileName()).isEqualTo(originalFilename);
        assertThat(videoMetadata.get(0).videoFile().mimeType()).isEqualTo(contentType);

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
