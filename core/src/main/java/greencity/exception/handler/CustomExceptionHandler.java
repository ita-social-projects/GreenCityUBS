package greencity.exception.handler;

import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.ResourceNotFoundException;
import greencity.exceptions.UnprocessableEntityException;
import greencity.exceptions.WrongSignatureException;
import greencity.exceptions.bots.TelegramBotExecutionException;
import greencity.exceptions.courier.CourierAlreadyExists;
import greencity.exceptions.http.AccessDeniedException;
import greencity.exceptions.http.RemoteServerUnavailableException;
import greencity.exceptions.notification.IncorrectTemplateException;
import greencity.exceptions.notification.TemplateDeleteException;
import greencity.exceptions.service.ServiceAlreadyExistsException;
import greencity.exceptions.tariff.TariffAlreadyExistsException;
import greencity.exceptions.api.GoogleApiException;
import greencity.exceptions.address.AddressNotWithinLocationAreaException;
import greencity.exceptions.user.UserNotFoundException;
import greencity.exceptions.validation.ValidationException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
     * exception.
     */
    @ExceptionHandler({
        BadRequestException.class,
        MappingException.class,
        CourierAlreadyExists.class,
        ServiceAlreadyExistsException.class,
        IncorrectTemplateException.class,
        TemplateDeleteException.class
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
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
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
     * exception.
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
     * exception.
     */
    @ExceptionHandler({NotFoundException.class})
    public final ResponseEntity<Object> handleNotFoundException(NotFoundException exception, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        log.trace(exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse);
    }

    /**
     * Method interceptor exception {@link AccessDeniedException}.
     *
     * @param request contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     * exception.
     */
    @ExceptionHandler({AccessDeniedException.class,
        org.springframework.security.access.AccessDeniedException.class})
    public final ResponseEntity<Object> handleAccessDeniedException(WebRequest request) {
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
     * Exception handler for {@link AddressNotWithinLocationAreaException} This
     * method handles exceptions related to an address not being within a valid
     * location area or an invalid address. It captures the error details from the
     * {@link WebRequest}, wraps them in an {@link ExceptionResponse}, and returns a
     * {@code 400 Bad Request} HTTP status along with the error message.
     *
     * @param request {@link WebRequest} containing the details of the error.
     * @return {@link ResponseEntity} containing the {@link ExceptionResponse} with
     * the error attributes and a {@code 400 Bad Request} status.
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

    /**
     * Method intercepts exception {@link ResourceNotFoundException}.
     *
     * @param ex      Exception that should be intercepted.
     * @param request Contains details about the occurred exception.
     * @return {@code ResponseEntity} which contains the HTTP status and body with
     * the exception message.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public final ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex,
                                                                        WebRequest request) {
        log.error(ex.getMessage(), ex);
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse);
    }

    /**
     * Method intercepts exception
     * {@link greencity.exceptions.validation.ValidationException}.
     *
     * @param ex      Exception that should be intercepted.
     * @param request Contains details about the occurred exception.
     * @return {@code ResponseEntity} which contains the HTTP status and body with
     * the exception message.
     */
    @ExceptionHandler(ValidationException.class)
    public final ResponseEntity<Object> handleValidationException(ValidationException ex,
                                                                  WebRequest request) {
        log.error(ex.getMessage(), ex);
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    /**
     * Exception handler for {@link GoogleApiException}.
     *
     * @param ex         Exception which should be intercepted.
     * @param webRequest contain detail about occur exception.
     * @return {@code ResponseEntity} which contain http status and body with
     * message of exception.
     */
    @ExceptionHandler(GoogleApiException.class)
    public final ResponseEntity<Object> handleGoogleApiException(GoogleApiException ex,
                                                                 WebRequest webRequest) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    /**
     * Exception handler for {@link UserNotFoundException}.
     *
     * @param ex         Exception which should be intercepted.
     * @param webRequest contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     * exception.
     */
    @ExceptionHandler(UserNotFoundException.class)
    public final ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException ex,
                                                                    WebRequest webRequest) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse);
    }

    /**
     * Exception handler for {@link WrongSignatureException}.
     *
     * @param ex         Exception which should be intercepted.
     * @param webRequest contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     * exception.
     */
    @ExceptionHandler(WrongSignatureException.class)
    public final ResponseEntity<Object> handleWrongSignatureException(WrongSignatureException ex,
                                                                      WebRequest webRequest) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(exceptionResponse);
    }

    /**
     * Handles exceptions of type {@link ConstraintViolationException} thrown during
     * validation of method parameters or path variables. Extracts detailed
     * violation messages from the exception and sets them into a custom
     * {@link ExceptionResponse} object. The response is sent with HTTP status 400
     * (Bad Request).
     *
     * @param ex      the {@link ConstraintViolationException} containing validation
     *                errors
     * @param request the current {@link WebRequest} context
     * @return a {@link ResponseEntity} containing the {@link ExceptionResponse}
     * with aggregated violation messages and HTTP 400 status
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public final ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex,
                                                                           WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        log.debug("Constraint violation occurred: {}", ex.getMessage());

        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();

        String detailedMessage;
        if (violations == null || violations.isEmpty()) {
            detailedMessage = "Validation failed with no specific details.";
        } else {
            detailedMessage = violations.stream()
                .sorted(Comparator.comparing(v -> v.getPropertyPath().toString()))
                .map(violation -> String.format("%s: %s", violation.getPropertyPath(), violation.getMessage()))
                .collect(Collectors.joining(", ", "Validation failed: ", ""));
        }

        exceptionResponse.setMessage(detailedMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    /**
     * Exception handler for {@link TelegramBotExecutionException}.
     *
     * @param ex         Exception which should be intercepted.
     * @param webRequest contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     * exception.
     */
    @ExceptionHandler(TelegramBotExecutionException.class)
    public final ResponseEntity<Object> handleTelegramBotExecutionException(TelegramBotExecutionException ex,
                                                                            WebRequest webRequest) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(exceptionResponse);
    }
}
