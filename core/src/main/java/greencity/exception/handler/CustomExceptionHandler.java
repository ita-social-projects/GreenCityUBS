package greencity.exception.handler;

import greencity.exceptions.BadRequestException;
import greencity.exceptions.FoundException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.UnprocessableEntityException;
import greencity.exceptions.courier.CourierAlreadyExists;
import greencity.exceptions.http.AccessDeniedException;
import greencity.exceptions.http.RemoteServerUnavailableException;
import greencity.exceptions.tariff.TariffAlreadyExists;
import greencity.exceptions.tariff.TariffAlreadyExistsException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.MappingException;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@RestControllerAdvice
@Slf4j
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {
    private ErrorAttributes errorAttributes;

    /**
     * Method interceptor exception {@link BadRequestException},
     * {@link ConstraintViolationException}, {@link MappingException},
     * {@link CourierAlreadyExists}.
     *
     * @param request contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({
        BadRequestException.class,
        ConstraintViolationException.class,
        MappingException.class,
        CourierAlreadyExists.class
    })
    public final ResponseEntity<Object> handleBadRequestException(WebRequest request) {
        ExceptionResponce exceptionResponse = new ExceptionResponce(getErrorAttributes(request));
        log.trace(exceptionResponse.getMessage(), exceptionResponse.getTrace());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    /**
     * Customize the response for HttpMessageNotReadableException.
     *
     * @param ex      the exception.
     * @param headers the headers to be written to the response.
     * @param status  the selected response status.
     * @param request the current request.
     * @return a {@code ResponseEntity} message.
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
        HttpHeaders headers, HttpStatus status,
        WebRequest request) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(request));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponce);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
        HttpHeaders headers, HttpStatus status,
        WebRequest request) {
        List<ValidationExceptionDto> collect =
            ex.getBindingResult().getFieldErrors().stream()
                .map(ValidationExceptionDto::new)
                .collect(Collectors.toList());
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(collect);
    }

    private Map<String, Object> getErrorAttributes(WebRequest webRequest) {
        return new HashMap<>(errorAttributes.getErrorAttributes(webRequest, true));
    }

    /**
     * Method interceptor exception {@link UnprocessableEntityException}.
     *
     * @param ex         Exception which should be intercepted.
     * @param webRequest contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({UnprocessableEntityException.class})
    public final ResponseEntity<Object> handleUnprocessableEntityException(UnprocessableEntityException ex,
        WebRequest webRequest) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(exceptionResponce);
    }

    /**
     * Method interceptor exception {@link NotFoundException}.
     *
     * @param exception Exception which should be intercepted.
     * @param request   contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({NotFoundException.class})
    public final ResponseEntity<Object> handleNotFoundExeption(NotFoundException exception, WebRequest request) {
        ExceptionResponce exceptionResponse = new ExceptionResponce(getErrorAttributes(request));
        log.trace(exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse);
    }

    /**
     * Method interceptor exception {@link FoundException}.
     *
     * @param ex         Exception which should be intercepted.
     * @param webRequest contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({FoundException.class})
    public final ResponseEntity<Object> handleFoundExeption(FoundException ex,
        WebRequest webRequest) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.FOUND).body(exceptionResponce);
    }

    /**
     * Method interceptor exception {@link AccessDeniedException}.
     *
     * @param ex      Exception which should be intercepted.
     * @param request contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public final ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex,
        WebRequest request) {
        ExceptionResponce exceptionResponse = new ExceptionResponce(getErrorAttributes(request));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(exceptionResponse);
    }

    /**
     * Exception handler for {@link RemoteServerUnavailableException}.
     *
     * @param request {@link WebRequest} with error details.
     * @return {@link ResponseEntity} with http status and exception message.
     */
    @ExceptionHandler(RemoteServerUnavailableException.class)
    public final ResponseEntity<Object> handleRemoteServerUnavailableException(WebRequest request) {
        ExceptionResponce exceptionResponse = new ExceptionResponce(getErrorAttributes(request));
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(exceptionResponse);
    }

    /**
     * Exception handler for {@link TariffAlreadyExistsException}.
     *
     * @param request {@link WebRequest} with error details.
     * @return {@link ResponseEntity} with http status and exception message.
     */
    @ExceptionHandler(TariffAlreadyExistsException.class)
    public final ResponseEntity<Object> handleTariffAlreadyExistsException(WebRequest request) {
        ExceptionResponce exceptionResponse = new ExceptionResponce(getErrorAttributes(request));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exceptionResponse);
    }
}
