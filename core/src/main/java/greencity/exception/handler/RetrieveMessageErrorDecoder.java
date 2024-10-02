package greencity.exception.handler;

import feign.Response;
import feign.codec.ErrorDecoder;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.http.AccessDeniedException;
import greencity.exceptions.http.RemoteServerUnavailableException;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
public class RetrieveMessageErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        ExceptionResponse exception;
        try (InputStream body = response.body().asInputStream()) {
            exception = ExceptionResponse
                .builder()
                .message(IOUtils.toString(body, StandardCharsets.UTF_8)).build();
        } catch (IOException e) {
            return new Exception(e.getMessage());
        }

        switch (response.status()) {
            case 400:
                return new BadRequestException(exception.getMessage());
            case 403:
                return new AccessDeniedException(exception.getMessage());
            case 404:
                return new NotFoundException(exception.getMessage());
            default:
                return new RemoteServerUnavailableException(exception.getMessage());
        }
    }
}
