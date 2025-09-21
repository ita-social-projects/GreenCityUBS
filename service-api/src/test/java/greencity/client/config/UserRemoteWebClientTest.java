package greencity.client.config;

import static greencity.ModelUtils.getDeleteFileDto;
import static greencity.ModelUtils.getUploadFileDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import greencity.dto.files.DeleteFileDto;
import greencity.dto.files.UploadFileDto;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(MockitoExtension.class)
class UserRemoteWebClientTest {
    static MockWebServer mockWebServer;
    UserRemoteWebClient userRemoteWebClient;
    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void init() {
        String baseUrl = "http://localhost:%s".formatted(mockWebServer.getPort());
        userRemoteWebClient = new UserRemoteWebClient(WebClient.builder().baseUrl(baseUrl).build());
    }

    @SneakyThrows
    @Test
    void uploadFileTest() {
        String expectedUrl = "upload-file-url";
        String expectedRequestPath = "/files";
        String expectedRequestMethod = HttpMethod.POST.name();

        mockWebServer.enqueue(new MockResponse()
            .setBody(expectedUrl)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        UploadFileDto uploadFileDto = getUploadFileDto();
        String actualResult = userRemoteWebClient.uploadFile(uploadFileDto);

        assertEquals(expectedUrl, actualResult);
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals(expectedRequestMethod, recordedRequest.getMethod());
        assertEquals(expectedRequestPath, recordedRequest.getPath());
        assertTrue(recordedRequest.getHeader(HttpHeaders.CONTENT_TYPE).startsWith(MediaType.MULTIPART_FORM_DATA_VALUE));
    }

    @SneakyThrows
    @Test
    void deleteFileTest() {
        DeleteFileDto deleteFileDto = getDeleteFileDto();
        String expectedRequestBody = objectMapper.writeValueAsString(deleteFileDto);
        String expectedRequestMethod = HttpMethod.DELETE.name();

        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        userRemoteWebClient.deleteFile(deleteFileDto);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        String requestBody = recordedRequest.getBody().readUtf8();

        assertEquals(expectedRequestMethod, recordedRequest.getMethod());
        assertEquals(expectedRequestBody, requestBody);
    }
}
