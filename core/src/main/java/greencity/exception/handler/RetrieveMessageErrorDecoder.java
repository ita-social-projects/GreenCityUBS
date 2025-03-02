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

        return switch (response.status()) {
            case 400 -> new BadRequestException(exception.getMessage());
            case 403 -> new AccessDeniedException(exception.getMessage());
            case 404 -> new NotFoundException(exception.getMessage());
            default -> new RemoteServerUnavailableException(exception.getMessage());
        };
    }
}
