package greencity.exception.handler;

import greencity.exceptions.BadRequestException;
import greencity.exceptions.FoundException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.UnprocessableEntityException;
import greencity.exceptions.courier.CourierAlreadyExists;
import greencity.exceptions.http.AccessDeniedException;
import greencity.exceptions.http.RemoteServerUnavailableException;
import greencity.exceptions.notification.IncorrectTemplateException;
import greencity.exceptions.notification.TemplateDeleteException;
import greencity.exceptions.service.ServiceAlreadyExistsException;
import greencity.exceptions.tariff.TariffAlreadyExistsException;
import greencity.exceptions.address.AddressNotWithinLocationAreaException;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.MappingException;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import jakarta.validation.ConstraintViolationException;
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
     * {@link CourierAlreadyExists}, {@link IncorrectTemplateException},
     * {@link TemplateDeleteException}.
     *
     * @param request contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({
        BadRequestException.class,
        ConstraintViolationException.class,
        MappingException.class,
        CourierAlreadyExists.class,
        ServiceAlreadyExistsException.class,
        IncorrectTemplateException.class
    })
    public final ResponseEntity<Object> handleBadRequestException(WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
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
        HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
        HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<ValidationExceptionDto> collect =
            ex.getBindingResult().getFieldErrors().stream()
                .map(ValidationExceptionDto::new)
                .collect(Collectors.toList());
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(collect);
    }

    private Map<String, Object> getErrorAttributes(WebRequest webRequest) {
        return new HashMap<>(errorAttributes.getErrorAttributes(webRequest,
            ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE)));
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
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(exceptionResponse);
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
    public final ResponseEntity<Object> handleNotFoundException(NotFoundException exception, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
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
    public final ResponseEntity<Object> handleFoundException(FoundException ex,
        WebRequest webRequest) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.FOUND).body(exceptionResponse);
    }

    /**
     * Method intercepts exceptions of types {@link AccessDeniedException},
     * {@link org.springframework.security.access.AccessDeniedException} and
     * {@link TemplateDeleteException}.
     *
     * @param request Contains details about the occurred exception.
     * @return ResponseEntity which contains http status FORBIDDEN (403) and a body
     *         with the message of the exception.
     */
    @ExceptionHandler({
        TemplateDeleteException.class,
        AccessDeniedException.class,
        org.springframework.security.access.AccessDeniedException.class
    })
    public final ResponseEntity<Object> handleForbiddenExceptions(WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        log.trace(exceptionResponse.getMessage(), exceptionResponse.getTrace());
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
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
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
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exceptionResponse);
    }

    /**
     * Exception handler for {@link AddressNotWithinLocationAreaException}.
     *
     * @param request {@link WebRequest} with error details.
     * @return {@link ResponseEntity} with http status and exception message.
     */
    @ExceptionHandler(AddressNotWithinLocationAreaException.class)
    public final ResponseEntity<Object> handleAddressNotWithinLocationAreaException(WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    /**
     * Exception handler for {@link EntityNotFoundException}.
     *
     * @param request {@link WebRequest} with error details.
     * @return {@link ResponseEntity} with http status and exception message.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public final ResponseEntity<Object> handleEntityNotFoundException(WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse);
    }
}
