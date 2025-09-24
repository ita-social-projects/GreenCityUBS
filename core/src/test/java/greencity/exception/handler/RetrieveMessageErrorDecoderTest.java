package greencity.exception.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.http.AccessDeniedException;
import greencity.exceptions.http.RemoteServerUnavailableException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import feign.Response;
import greencity.exceptions.BadRequestException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.ObjectMapper;

class RetrieveMessageErrorDecoderTest {
    private final RetrieveMessageErrorDecoder decoder = new RetrieveMessageErrorDecoder();
    private final Response mockResponse = mock(Response.class);
    private final Response.Body mockBody = mock(Response.Body.class);

    private String wrapJsonMessage(String message) throws IOException {
        return new ObjectMapper().writeValueAsString(new ExceptionResponse(message, null, null, null));
    }

    @Test
    void decodeThrowsBadRequestExceptionTest() throws IOException {
        String errorMessage = "This is a bad request";
        String jsonBody = wrapJsonMessage(errorMessage);

        when(mockResponse.status()).thenReturn(400);
        when(mockResponse.body()).thenReturn(mockBody);
        when(mockBody.asInputStream()).thenReturn(IOUtils.toInputStream(jsonBody, StandardCharsets.UTF_8));

        Exception exception = decoder.decode("methodKey", mockResponse);
        assertEquals(BadRequestException.class, exception.getClass());
        assertEquals(errorMessage, exception.getMessage());

        verify(mockResponse).status();
        verify(mockResponse).body();
        verify(mockBody).asInputStream();
    }

    @Test
    void decodeThrowsAccessDeniedExceptionTest() throws IOException {
        String errorMessage = "Access denied";
        String jsonBody = wrapJsonMessage(errorMessage);

        when(mockResponse.status()).thenReturn(403);
        when(mockResponse.body()).thenReturn(mockBody);
        when(mockBody.asInputStream()).thenReturn(IOUtils.toInputStream(jsonBody, StandardCharsets.UTF_8));

        Exception exception = decoder.decode("methodKey", mockResponse);
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals(errorMessage, exception.getMessage());

        verify(mockResponse).status();
        verify(mockResponse).body();
        verify(mockBody).asInputStream();
    }

    @Test
    void decodeThrowsNotFoundExceptionTest() throws IOException {
        String errorMessage = "Not found";
        String jsonBody = wrapJsonMessage(errorMessage);

        when(mockResponse.status()).thenReturn(404);
        when(mockResponse.body()).thenReturn(mockBody);
        when(mockBody.asInputStream()).thenReturn(IOUtils.toInputStream(jsonBody, StandardCharsets.UTF_8));

        Exception exception = decoder.decode("methodKey", mockResponse);
        assertEquals(NotFoundException.class, exception.getClass());
        assertEquals(errorMessage, exception.getMessage());

        verify(mockResponse).status();
        verify(mockResponse).body();
        verify(mockBody).asInputStream();
    }

    @Test
    void decodeThrowsIOExceptionTest() throws IOException {
        when(mockResponse.body()).thenReturn(mockBody);
        when(mockBody.asInputStream()).thenThrow(new IOException("An error occurred"));
        Exception exception = decoder.decode("methodKey", mockResponse);
        assertEquals(Exception.class, exception.getClass());

        verify(mockResponse).body();
        verify(mockBody).asInputStream();
    }

    @Test
    void decodeThrowsRemoteServerUnavailableExceptionTest() throws IOException {
        String errorMessage = "Remote error";
        String jsonBody = wrapJsonMessage(errorMessage);

        when(mockResponse.status()).thenReturn(500);
        when(mockResponse.body()).thenReturn(mockBody);
        when(mockBody.asInputStream()).thenReturn(IOUtils.toInputStream(jsonBody, StandardCharsets.UTF_8));

        Exception exception = decoder.decode("methodKey", mockResponse);
        assertEquals(RemoteServerUnavailableException.class, exception.getClass());
        assertEquals(errorMessage, exception.getMessage());

        verify(mockResponse).status();
        verify(mockResponse).body();
        verify(mockBody).asInputStream();
    }

    @Test
    void decodeWithPlainExceptionFallsBackToRawMessage() throws IOException {
        String rawMessage = "Plain raw exception";

        when(mockResponse.status()).thenReturn(400);
        when(mockResponse.body()).thenReturn(mockBody);
        when(mockBody.asInputStream()).thenReturn(IOUtils.toInputStream(rawMessage, StandardCharsets.UTF_8));

        Exception exception = decoder.decode("methodKey", mockResponse);
        assertEquals(BadRequestException.class, exception.getClass());
        assertEquals(rawMessage, exception.getMessage());

        verify(mockResponse).status();
        verify(mockResponse).body();
        verify(mockBody).asInputStream();
    }

    @Test
    void decodeWithUnknownStatusReturnsRemoteServerUnavailableException() throws IOException {
        String errorMessage = "I'm a teapot";
        String jsonBody = wrapJsonMessage(errorMessage);

        when(mockResponse.status()).thenReturn(418);
        when(mockResponse.body()).thenReturn(mockBody);
        when(mockBody.asInputStream()).thenReturn(IOUtils.toInputStream(jsonBody, StandardCharsets.UTF_8));

        Exception exception = decoder.decode("methodKey", mockResponse);

        assertEquals(RemoteServerUnavailableException.class, exception.getClass());
        assertEquals(errorMessage, exception.getMessage());

        verify(mockResponse).status();
        verify(mockResponse).body();
        verify(mockBody).asInputStream();
    }
}
