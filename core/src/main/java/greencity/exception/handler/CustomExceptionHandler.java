package greencity.exception.handler;

import greencity.exceptions.*;
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

    /**
     * Method interceptor exception {@link OrderNotFoundException}.
     *
     * @param ex         Exception which should be intercepted.
     * @param webRequest contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({OrderNotFoundException.class})
    public final ResponseEntity<Object> handleOrderNotFoundException(OrderNotFoundException ex,
        WebRequest webRequest) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponce);
    }

    /**
     * Method interceptor exception {@link BadOrderStatusRequestException}.
     *
     * @param ex         Exception which should be intercepted.
     * @param webRequest contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({BadOrderStatusRequestException.class})
    public final ResponseEntity<Object> handleBadOrderStatusRequestException(BadOrderStatusRequestException ex,
        WebRequest webRequest) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponce);
    }

    /**
     * Method interceptor exception {@link EmployeeNotFoundException}.
     *
     * @param ex         Exception which should be intercepted.
     * @param webRequest contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({EmployeeNotFoundException.class})
    public final ResponseEntity<Object> handleEmployeeNotFoundException(EmployeeNotFoundException ex,
        WebRequest webRequest) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponce);
    }

    /**
     * Method interceptor exception {@link EmployeeValidationException}.
     *
     * @param ex         Exception which should be intercepted.
     * @param webRequest contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({EmployeeValidationException.class})
    public final ResponseEntity<Object> handleEmployeeValidationException(EmployeeValidationException ex,
        WebRequest webRequest) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(exceptionResponce);
    }

    /**
     * Method interceptor exception {@link PositionValidationException}.
     *
     * @param ex         Exception which should be intercepted.
     * @param webRequest contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({PositionValidationException.class})
    public final ResponseEntity<Object> handlePositionValidationException(PositionValidationException ex,
        WebRequest webRequest) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(exceptionResponce);
    }

    /**
     * Method interceptor exception {@link ReceivingStationValidationException}.
     *
     * @param ex         Exception which should be intercepted.
     * @param webRequest contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({ReceivingStationValidationException.class})
    public final ResponseEntity<Object> handleReceivingStationValidationException(
        ReceivingStationValidationException ex,
        WebRequest webRequest) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(exceptionResponce);
    }

    /**
     * Method interceptor exception {@link PositionNotFoundException}.
     *
     * @param ex         Exception which should be intercepted.
     * @param webRequest contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({PositionNotFoundException.class})
    public final ResponseEntity<Object> handlePositionNotFoundException(PositionNotFoundException ex,
        WebRequest webRequest) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponce);
    }

    /**
     * Method interceptor exception {@link ReceivingStationNotFoundException}.
     *
     * @param ex         Exception which should be intercepted.
     * @param webRequest contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({ReceivingStationNotFoundException.class})
    public final ResponseEntity<Object> handleReceivingStationNotFoundException(ReceivingStationNotFoundException ex,
        WebRequest webRequest) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponce);
    }

    /**
     * Method interceptor exception {@link EmployeeIllegalOperationException}.
     *
     * @param ex         Exception which should be intercepted.
     * @param webRequest contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({EmployeeIllegalOperationException.class})
    public final ResponseEntity<Object> handleEmployeeIllegalOperationException(EmployeeIllegalOperationException ex,
        WebRequest webRequest) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(exceptionResponce);
    }

    /**
     * Method interceptor exception {@link ImageUrlParseException}.
     *
     * @param ex         Exception which should be intercepted.
     * @param webRequest contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({ImageUrlParseException.class})
    public final ResponseEntity<Object> handleImageUrlParseException(ImageUrlParseException ex,
        WebRequest webRequest) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponce);
    }

    /**
     * Method interceptor exception {@link PaymentNotFoundException}.
     *
     * @param exception Exception which should be intercepted.
     * @param request   contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({PaymentNotFoundException.class})
    public final ResponseEntity<Object> handleNotFoundPayment(RuntimeException exception, WebRequest request) {
        ExceptionResponce exceptionResponse = new ExceptionResponce(getErrorAttributes(request));
        log.trace(exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse);
    }

    /**
     * Method interceptor exception {@link PhoneNumberParseException}.
     *
     * @param exception Exception which should be intercepted.
     * @param request   contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({PhoneNumberParseException.class})
    public final ResponseEntity<Object> handleNotFoundPayment(PhoneNumberParseException exception, WebRequest request) {
        ExceptionResponce exceptionResponse = new ExceptionResponce(getErrorAttributes(request));
        log.trace(exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse);
    }

    /**
     * Method interceptor exception {@link PhoneNumberParseException}.
     *
     * @param exception Exception which should be intercepted.
     * @param request   contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({AddressAlreadyExistException.class})
    public final ResponseEntity<Object> handleAlreadyExistingAddress(AddressAlreadyExistException exception,
        WebRequest request) {
        ExceptionResponce exceptionResponse = new ExceptionResponce(getErrorAttributes(request));
        log.trace(exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse);
    }

    /**
     * Method interceptor exception {@link OrderViolationException}.
     *
     * @param exception Exception which should be intercepted.
     * @param request   contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({OrderViolationException.class})
    public final ResponseEntity<Object> handleAlreadyExistingAddress(OrderViolationException exception,
        WebRequest request) {
        ExceptionResponce exceptionResponse = new ExceptionResponce(getErrorAttributes(request));
        log.trace(exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse);
    }

    /**
     * Method interceptor exception {@link EventsNotFoundException}.
     *
     * @param ex         Exception which should be intercepted.
     * @param webRequest contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({EventsNotFoundException.class})
    public final ResponseEntity<Object> handleEventsNotFoundException(EventsNotFoundException ex,
        WebRequest webRequest) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponce);
    }

    /**
     * Method interceptor exception {@link EmployeeAlreadyAssignedForOrder}.
     *
     * @param ex         Exception which should be intercepted.
     * @param webRequest contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({EmployeeAlreadyAssignedForOrder.class})
    public final ResponseEntity<Object> handleEmployeeAlreadyExistForOrder(EmployeeAlreadyAssignedForOrder ex,
        WebRequest webRequest) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.FOUND).body(exceptionResponce);
    }

    /**
     * Method interceptor exception {@link EmployeeAlreadyExist}.
     *
     * @param ex         Exception which should be intercepted.
     * @param webRequest contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({EmployeeAlreadyExist.class})
    public final ResponseEntity<Object> handleEmployeeAlreadyExist(EmployeeAlreadyExist ex,
        WebRequest webRequest) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.FOUND).body(exceptionResponce);
    }

    /**
     * Method interceptor exception {@link EmployeeIsNotAssigned}.
     *
     * @param ex         Exception which should be intercepted.
     * @param webRequest contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({EmployeeIsNotAssigned.class})
    public final ResponseEntity<Object> handleEmployeeIsNotAssign(EmployeeIsNotAssigned ex,
        WebRequest webRequest) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.FOUND).body(exceptionResponce);
    }

    /**
     * Method interceptor exception {@link NotificationNotFoundException}.
     *
     * @param exception Exception which should be intercepted.
     * @param request   contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({NotificationNotFoundException.class})
    public final ResponseEntity<Object> handleNotificationNotFoundException(NotificationNotFoundException exception,
        WebRequest request) {
        ExceptionResponce exceptionResponse = new ExceptionResponce(getErrorAttributes(request));
        log.trace(exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse);
    }
}
