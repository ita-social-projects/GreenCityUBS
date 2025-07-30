package greencity.exception.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    public RetrieveMessageErrorDecoder() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        try (InputStream body = response.body().asInputStream()) {
            String bodyString = IOUtils.toString(body, StandardCharsets.UTF_8);
            ExceptionResponse exception = getExceptionResponse(bodyString);

            return switch (response.status()) {
                case 400 -> new BadRequestException(exception.getMessage());
                case 403 -> new AccessDeniedException(exception.getMessage());
                case 404 -> new NotFoundException(exception.getMessage());
                default -> new RemoteServerUnavailableException(exception.getMessage());
            };
        } catch (IOException e) {
            return new Exception(e.getMessage());
        }
    }

    private ExceptionResponse getExceptionResponse(String bodyString) {
        ExceptionResponse exception;
        try {
            exception = objectMapper.readValue(bodyString, ExceptionResponse.class);
        } catch (Exception e) {
            exception = ExceptionResponse.builder().message(bodyString).build();
        }
        return exception;
    }
}
