package greencity.client.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
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
    void init(){
        String baseUrl = "http://localhost:%s".formatted(mockWebServer.getPort());
        userRemoteWebClient = new UserRemoteWebClient(WebClient.builder().baseUrl(baseUrl).build());
    }

    @SneakyThrows
    @Test
    void uploadAllFilesTest() {
        List<String> expectedUrls = List.of("url1", "url2");
        String expectedJson = toJson(expectedUrls);
        String expectedRequestPath = "/files";
        String expectedRequestMethod = HttpMethod.POST.name();

        MultipartFile file1 = createMockMultipartFile("file1.txt", "content1");
        MultipartFile file2 = createMockMultipartFile("file2.txt", "content2");
        List<MultipartFile> files = Arrays.asList(file1, file2);

        mockWebServer.enqueue(new MockResponse()
            .setBody(expectedJson)
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        List<String> actualResult = userRemoteWebClient.uploadAllFiles(files);

        assertEquals(expectedUrls, actualResult);
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals(expectedRequestMethod, recordedRequest.getMethod());
        assertEquals(expectedRequestPath, recordedRequest.getPath());
        assertTrue(recordedRequest.getHeader(HttpHeaders.CONTENT_TYPE).startsWith(MediaType.MULTIPART_FORM_DATA_VALUE));
    }

    @SneakyThrows
    @Test
    void uploadFileTest() {
        String expectedUrl = "upload-file-url";
        String expectedRequestPath = "/files/single";
        String expectedRequestMethod = HttpMethod.POST.name();

        MultipartFile file = createMockMultipartFile("file.txt", "content");

        mockWebServer.enqueue(new MockResponse()
            .setBody(expectedUrl)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        String actualResult = userRemoteWebClient.uploadFile(file);

        assertEquals(expectedUrl, actualResult);
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals(expectedRequestMethod, recordedRequest.getMethod());
        assertEquals(expectedRequestPath, recordedRequest.getPath());
        assertTrue(recordedRequest.getHeader(HttpHeaders.CONTENT_TYPE).startsWith(MediaType.MULTIPART_FORM_DATA_VALUE));
    }

    @SneakyThrows
    @Test
    void deleteAllFilesTest() {
        List<String> pathToDelete = List.of("path1", "path2");
        String expectedRequestPath = "/files";
        String expectedRequestMethod = HttpMethod.DELETE.name();

        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        userRemoteWebClient.deleteAllFiles(pathToDelete);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals(expectedRequestMethod, recordedRequest.getMethod());
        assertEquals(expectedRequestPath, recordedRequest.getPath());

        String requestBody = recordedRequest.getBody().readUtf8();
        List<String> actualPath = fromJson(requestBody, new TypeReference<>() {
        });
        assertEquals(pathToDelete, actualPath);
    }

    @SneakyThrows
    @Test
    void deleteFileTest() {
        String expectedPath = "delete-file-url";
        String expectedRequestPath = "/files/single?path=" + URLEncoder.encode(expectedPath, StandardCharsets.UTF_8);
        String expectedRequestMethod = HttpMethod.DELETE.name();

        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        userRemoteWebClient.deleteFile(expectedPath);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals(expectedRequestMethod, recordedRequest.getMethod());
        assertEquals(expectedRequestPath, recordedRequest.getPath());

        String requestBody = recordedRequest.getBody().readUtf8();
        assertTrue(requestBody.isEmpty());
    }

    @SneakyThrows
    private String toJson(Object object) {
        return objectMapper.writeValueAsString(object);
    }

    private MultipartFile createMockMultipartFile(String name, String content) {
        return new MockMultipartFile(name, name, "text/plain", content.getBytes());
    }

    @SneakyThrows
    private <T> T fromJson(String json, TypeReference<T> typeReference) {
        return objectMapper.readValue(json, typeReference);
    }

}
