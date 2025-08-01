package greencity.client.config;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import greencity.security.JwtTool;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
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
    void setUp(){
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
    void notFoundResponseThrowsNotFoundExceptionTest() {
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(404)
            .setBody("{\"message\": \"Not Found error from API\"}")
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Mono<String> result = webClient.get().uri("/").retrieve().bodyToMono(String.class);

        try {
            result.block();
            Assertions.fail("Expected NotFoundException to be thrown");
        } catch (Exception ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;

            Assertions.assertEquals("NotFoundException", cause.getClass().getSimpleName());
            Assertions.assertTrue(cause.getMessage().contains("Not Found error from API"));
        }
    }

    @Test
    void badRequestResponseThrowsBadRequestExceptionTest() {
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(400)
            .setBody("{\"message\": \"Bad Request error from API\"}")
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Mono<String> result = webClient.get().uri("/").retrieve().bodyToMono(String.class);

        try {
            result.block();
            Assertions.fail("Expected BadRequestException to be thrown");
        } catch (Exception ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;

            Assertions.assertEquals("BadRequestException", cause.getClass().getSimpleName());
            Assertions.assertTrue(cause.getMessage().contains("Bad Request error from API"));
        }
    }

    @Test
    void internalServerErrorThrowsGreenCityServiceExceptionTest() {
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(500)
            .setBody("{\"message\": \"Internal Server Error from API\"}")
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Mono<String> result = webClient.get().uri("/").retrieve().bodyToMono(String.class);

        try {
            result.block();
            Assertions.fail("Expected GreenCityServiceException to be thrown");
        } catch (Exception ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;

            Assertions.assertEquals("GreenCityUserServiceException", cause.getClass().getSimpleName());
            Assertions.assertTrue(cause.getMessage().contains("Internal Server Error from API"));
        }
    }

    @Test
    void okResponseReturnsBodyTest() {
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("Success")
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        String response = webClient.get().uri("/").retrieve().bodyToMono(String.class).block();

        Assertions.assertEquals("Success", response);
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
