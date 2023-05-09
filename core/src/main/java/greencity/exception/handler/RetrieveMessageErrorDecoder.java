package greencity.exception.handler;

import feign.Response;
import feign.codec.ErrorDecoder;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.http.AccessDeniedException;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class RetrieveMessageErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder errorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        Map<String, Object> errorAttributes = new HashMap<>();
        errorAttributes.put("message", "An error occurred");
        errorAttributes.put("timestamp", "");

        ExceptionResponce exception = new ExceptionResponce(errorAttributes);

        try (InputStream body = response.body().asInputStream()) {
            exception = ExceptionResponce
                .builder()
                .message(IOUtils.toString(body, StandardCharsets.UTF_8)).build();
        } catch (IOException e) {
            return new Exception(exception.getMessage());
        }

        switch (response.status()) {
            case 400:
                return new BadRequestException(exception.getMessage());
            case 403:
                return new AccessDeniedException(exception.getMessage());
            case 404:
                return new NotFoundException(exception.getMessage());
            default:
                return errorDecoder.decode(methodKey, response);
        }
    }
}
