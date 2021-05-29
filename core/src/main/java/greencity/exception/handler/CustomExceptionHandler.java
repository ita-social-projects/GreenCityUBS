package greencity.exception.handler;

import greencity.exceptions.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@AllArgsConstructor
@RestControllerAdvice
@Slf4j
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {
    private ErrorAttributes errorAttributes;

    /**
     * Method interceptor exception {@link CertificateNotFoundException}.
     *
     * @param ex      Exception which should be intercepted.
     * @param request contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler(CertificateNotFoundException.class)
    public final ResponseEntity<Object> handleCertificateNotFound(CertificateNotFoundException ex, WebRequest request) {
        ExceptionResponce exceptionResponse = new ExceptionResponce(getErrorAttributes(request));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse);
    }

    /**
     * Method interceptor exception {@link IncorrectValueException}.
     *
     * @param ex      Exception which should be intercepted.
     * @param request contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler(IncorrectValueException.class)
    public final ResponseEntity<Object> handleInvalidDistance(IncorrectValueException ex, WebRequest request) {
        ExceptionResponce exceptionResponse = new ExceptionResponce(getErrorAttributes(request));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    /**
     * Method interceptor exception {@link TooManyCertificatesEntered}.
     *
     * @param ex      Exception which should be intercepted.
     * @param request contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({TooManyCertificatesEntered.class, PaymentValidationException.class})
    public final ResponseEntity<Object> handleInvalidDistance(RuntimeException ex, WebRequest request) {
        ExceptionResponce exceptionResponse = new ExceptionResponce(getErrorAttributes(request));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    /**
     * Method interceptor exception {@link ActiveOrdersNotFoundException}.
     *
     * @param ex      Exception which should be intercepted.
     * @param request contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler(ActiveOrdersNotFoundException.class)
    public final ResponseEntity<Object> handleNoUndeliveredOrders(ActiveOrdersNotFoundException ex,
        WebRequest request) {
        ExceptionResponce exceptionResponse = new ExceptionResponce(getErrorAttributes(request));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse);
    }

    /**
     * Method interceptor exception {@link ConstraintViolationException}.
     *
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> constraintViolationException(ConstraintViolationException ex, WebRequest webRequest) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponce);
    }

    /**
     * Method interceptor exception
     * {@link greencity.exceptions.CertificateExpiredException}.
     *
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler(CertificateExpiredException.class)
    public ResponseEntity<Object> handleCertificateExpiredException(CertificateExpiredException ex,
        WebRequest webRequest) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponce);
    }

    /**
     * Method interceptor exception
     * {@link greencity.exceptions.CertificateIsUsedException}.
     *
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler(CertificateIsUsedException.class)
    public final ResponseEntity<Object> handleCertificateIsUsedException(CertificateIsUsedException ex,
        WebRequest webRequest) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponce);
    }

    /**
     * Method interceptor exception {@link BagNotFoundException}.
     *
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler(BagNotFoundException.class)
    public final ResponseEntity<Object> handleBagNotFoundException(BagNotFoundException ex, WebRequest webRequest) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponce);
    }

    /**
     * Customize the response for HttpMessageNotReadableException.
     *
     * @param ex      the exception
     * @param headers the headers to be written to the response
     * @param status  the selected response status
     * @param request the current request
     * @return a {@code ResponseEntity} message
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
     * Method interceptor exception {@link UnexistingUuidExeption}.
     *
     * @param ex      Exception which should be intercepted.
     * @param request contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({UnexistingUuidExeption.class, UnexistingOrderException.class})
    public final ResponseEntity<Object> handleUuidNotFound(RuntimeException ex, WebRequest request) {
        ExceptionResponce exceptionResponse = new ExceptionResponce(getErrorAttributes(request));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse);
    }

    /**
     * Method interceptor exception {@link NotFoundOrderAddressException}.
     *
     * @param ex         Exception which should be intercepted.
     * @param webRequest contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({NotFoundOrderAddressException.class})
    public final ResponseEntity<Object> handleNotFoundOrderAddressException(NotFoundOrderAddressException ex,
        WebRequest webRequest) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponce);
    }
}
