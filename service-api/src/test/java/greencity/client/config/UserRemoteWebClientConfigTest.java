package greencity.client.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.GreenCityUserServiceException;
import greencity.exceptions.NotFoundException;
import greencity.security.JwtTool;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class UserRemoteWebClientConfigTest {
    static MockWebServer mockWebServer;

    @InjectMocks
    UserRemoteWebClientConfig userRemoteWebClientConfig;

    @Mock
    JwtTool jwtTool;

    WebClient webClient;

    @BeforeAll
    static void startServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void shutdown() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void setUp() {
        when(jwtTool.createAccessToken(anyString(), anyInt()))
            .thenReturn("mocked-jwt-token");

        setField(userRemoteWebClientConfig, "greenCityUserBaseUrl",
            mockWebServer.url("/").toString());
        setField(userRemoteWebClientConfig, "systemEmail", "test@gmail.com");
        setField(userRemoteWebClientConfig, "connectionTimeoutMillis", 1000);
        setField(userRemoteWebClientConfig, "responseTimeoutMillis", 1000);

        webClient = userRemoteWebClientConfig.webClient(WebClient.builder());
    }

    @Test
    void notFoundResponseThrowsNotFoundExceptionTest() throws Exception {
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(404)
            .setBody("{\"message\": \"Not Found error from API\"}")
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Mono<String> body = webClient.get().uri("/")
            .retrieve()
            .bodyToMono(String.class);
        NotFoundException ex = assertThrows(NotFoundException.class, body::block);
        RecordedRequest request = mockWebServer.takeRequest();

        assertTrue(ex.getMessage().contains("Not Found error from API"));
        assertEquals("/", request.getPath());
        assertEquals("GET", request.getMethod());
    }

    @Test
    void badRequestResponseThrowsBadRequestExceptionTest() throws Exception {
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(400)
            .setBody("{\"message\": \"Bad Request error from API\"}")
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Mono<String> body = webClient.get().uri("/")
            .retrieve()
            .bodyToMono(String.class);
        BadRequestException ex = assertThrows(BadRequestException.class, body::block);
        RecordedRequest request = mockWebServer.takeRequest();

        assertTrue(ex.getMessage().contains("Bad Request error from API"));
        assertEquals("/", request.getPath());
        assertEquals("GET", request.getMethod());
    }

    @Test
    void internalServerErrorThrowsGreenCityServiceExceptionTest() throws Exception {
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(500)
            .setBody("{\"message\": \"Internal Server Error from API\"}")
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Mono<String> body = webClient.get().uri("/")
            .retrieve()
            .bodyToMono(String.class);
        GreenCityUserServiceException ex = assertThrows(GreenCityUserServiceException.class, body::block);
        RecordedRequest request = mockWebServer.takeRequest();

        assertTrue(ex.getMessage().contains("Internal Server Error from API"));
        assertEquals("/", request.getPath());
        assertEquals("GET", request.getMethod());
    }

    @Test
    void handleWebClientExceptionWithDefaultThrowsIllegalArgumentExceptionTest() throws Exception {
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(503)
            .setBody("{\"message\": \"Internal Server Error from API\"}")
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Mono<String> body = webClient.get().uri("/")
            .retrieve()
            .bodyToMono(String.class);
        IllegalStateException ex = assertThrows(IllegalStateException.class, body::block);
        RecordedRequest request = mockWebServer.takeRequest();

        assertTrue(ex.getMessage().contains("Internal Server Error from API"));
        assertEquals("/", request.getPath());
        assertEquals("GET", request.getMethod());
    }

    @Test
    void okResponseReturnsBodyTest() throws Exception {
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("Success")
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        String response = webClient.get()
            .uri("/")
            .retrieve()
            .bodyToMono(String.class)
            .block();
        RecordedRequest request = mockWebServer.takeRequest();

        Assertions.assertEquals("Success", response);
        assertEquals("/", request.getPath());
        assertEquals("GET", request.getMethod());
    }

    @Test
    void encodePlusInQueryReplacesPlus() throws Exception {
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("Success")
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        String response = webClient.get()
            .uri("/search?email=java+spring+boot&categoryEmail=tutorial+guide")
            .retrieve()
            .bodyToMono(String.class)
            .block();
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        String requestPath = recordedRequest.getPath();

        assertEquals("Success", response);
        assertNotNull(requestPath);
        assertTrue(requestPath.contains("java%2Bspring%2Bboot"));
        assertTrue(requestPath.contains("tutorial%2Bguide"));
        assertFalse(requestPath.contains("java+spring"));
    }

    @Test
    void encodePlusInQueryLeavesUrlWithoutPlusUnchanged() throws Exception {
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("Success")
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        String response = webClient.get()
            .uri("/search?query=java-spring-boot&category=tutorial")
            .retrieve()
            .bodyToMono(String.class)
            .block();
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        String requestPath = recordedRequest.getPath();

        assertEquals("Success", response);
        assertNotNull(requestPath);
        assertTrue(requestPath.contains("java-spring-boot"));
        assertTrue(requestPath.contains("category=tutorial"));
        assertFalse(requestPath.contains("%2B"));
    }

    private void setField(Object target, String name, Object value) {
        try {
            var field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
