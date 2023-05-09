package greencity.exception.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import greencity.exceptions.NotFoundException;
import greencity.exceptions.http.AccessDeniedException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import feign.Response;
import greencity.exceptions.BadRequestException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

class RetrieveMessageErrorDecoderTest {
    private final RetrieveMessageErrorDecoder decoder = new RetrieveMessageErrorDecoder();
    private final Response mockResponse = mock(Response.class);
    private final Response.Body mockBody = mock(Response.Body.class);

    @Test
    void decodeBadRequestExceptionTest() throws IOException {
        String errorMessage = "This is a bad request";
        when(mockResponse.status()).thenReturn(400);
        when(mockResponse.body()).thenReturn(mockBody);
        when(mockBody.asInputStream()).thenReturn(IOUtils.toInputStream(errorMessage, StandardCharsets.UTF_8));
        Exception exception = decoder.decode("methodKey", mockResponse);
        assertEquals(BadRequestException.class, exception.getClass());
        assertEquals(errorMessage, exception.getMessage());

        verify(mockResponse).status();
        verify(mockResponse).body();
        verify(mockBody).asInputStream();
    }

    @Test
    void decodeAccessDeniedExceptionTest() throws IOException {
        String errorMessage = "This is an access denied";
        when(mockResponse.status()).thenReturn(403);
        when(mockResponse.body()).thenReturn(mockBody);
        when(mockBody.asInputStream()).thenReturn(IOUtils.toInputStream(errorMessage, StandardCharsets.UTF_8));
        Exception exception = decoder.decode("methodKey", mockResponse);
        assertEquals(AccessDeniedException.class, exception.getClass());
        assertEquals(errorMessage, exception.getMessage());

        verify(mockResponse).status();
        verify(mockResponse).body();
        verify(mockBody).asInputStream();
    }

    @Test
    void decodeNotFoundExceptionTest() throws IOException {
        String errorMessage = "This is a not found";
        when(mockResponse.status()).thenReturn(404);
        when(mockResponse.body()).thenReturn(mockBody);
        when(mockBody.asInputStream()).thenReturn(IOUtils.toInputStream(errorMessage, StandardCharsets.UTF_8));
        Exception exception = decoder.decode("methodKey", mockResponse);
        assertEquals(NotFoundException.class, exception.getClass());
        assertEquals(errorMessage, exception.getMessage());

        verify(mockResponse).status();
        verify(mockResponse).body();
        verify(mockBody).asInputStream();
    }
}
