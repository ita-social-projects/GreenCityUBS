package greencity.exception.handler;

import greencity.exceptions.address.AddressAlreadyExistException;
import greencity.exceptions.address.NotFoundOrderAddressException;
import greencity.exceptions.admin.ServiceNotFoundException;
import greencity.exceptions.admin.UpdateAdminPageInfoException;
import greencity.exceptions.bag.BagWithThisStatusAlreadySetException;
import greencity.exceptions.bag.NotEnoughBagsException;
import greencity.exceptions.certificate.CertificateExpiredException;
import greencity.exceptions.certificate.CertificateIsUsedException;
import greencity.exceptions.certificate.CertificateNotFoundException;
import greencity.exceptions.certificate.TooManyCertificatesEntered;
import greencity.exceptions.courier.CourierLocationException;
import greencity.exceptions.courier.CourierNotFoundException;
import greencity.exceptions.courier.TariffNotFoundException;
import greencity.exceptions.employee.*;
import greencity.exceptions.http.AccessDeniedException;
import greencity.exceptions.http.NotFoundException;
import greencity.exceptions.image.ImageUrlParseException;
import greencity.exceptions.language.LanguageNotFoundException;
import greencity.exceptions.location.*;
import greencity.exceptions.notification.NotificationNotFoundException;
import greencity.exceptions.number.IncorrectEcoNumberFormatException;
import greencity.exceptions.number.PhoneNumberParseException;
import greencity.exceptions.order.*;
import greencity.exceptions.payment.BagNotFoundException;
import greencity.exceptions.payment.LiqPayPaymentException;
import greencity.exceptions.payment.PaymentNotFoundException;
import greencity.exceptions.payment.PaymentValidationException;
import greencity.exceptions.position.PositionNotFoundException;
import greencity.exceptions.position.PositionValidationException;
import greencity.exceptions.user.UnexistingUuidExeption;
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
     * Method interceptor exception {@link CertificateExpiredException}.
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
     * Method interceptor exception {@link CertificateIsUsedException}.
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
     * Method interceptor exception {@link ServiceNotFoundException}.
     *
     * @param ex         Exception which should be intercepted.
     * @param webRequest contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({ServiceNotFoundException.class})
    public final ResponseEntity<Object> handleServiceIsNotFind(ServiceNotFoundException ex,
        WebRequest webRequest) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponce);
    }

    /**
     * Method interceptor exception {@link LocationNotFoundException}.
     *
     * @param ex         Exception which should be intercepted.
     * @param webRequest contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({LocationNotFoundException.class})
    public final ResponseEntity<Object> handleLocationNotFound(LocationNotFoundException ex,
        WebRequest webRequest) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponce);
    }

    /**
     * Method interceptor exception {@link LanguageNotFoundException}.
     *
     * @param ex         Exception which should be intercepted.
     * @param webRequest contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({LanguageNotFoundException.class})
    public final ResponseEntity<Object> handleLanguageNotFound(LanguageNotFoundException ex,
        WebRequest webRequest) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponce);
    }

    /**
     * Method interceptor exception {@link LocationStatusAlreadyExistException}.
     *
     * @param ex         Exception which should be intercepted.
     * @param webRequest contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({LocationStatusAlreadyExistException.class})
    public final ResponseEntity<Object> handleLocationExist(LocationStatusAlreadyExistException ex,
        WebRequest webRequest) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponce);
    }

    /**
     * Method interceptor exception {@link BagWithThisStatusAlreadySetException}.
     *
     * @param ex         Exception which should be intercepted.
     * @param webRequest contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({BagWithThisStatusAlreadySetException.class})
    public final ResponseEntity<Object> handleBagWithThisStatusAlreadySet(BagWithThisStatusAlreadySetException ex,
        WebRequest webRequest) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponce);
    }

    /**
     * Method interceptor exception {@link CourierNotFoundException}.
     *
     * @param ex         Exception which should be intercepted.
     * @param webRequest contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({CourierNotFoundException.class})
    public final ResponseEntity<Object> handleCourierNotFound(CourierNotFoundException ex,
        WebRequest webRequest) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponce);
    }

    /**
     * Method interceptor exception {@link LiqPayPaymentException}.
     *
     * @param ex         Exception which should be intercepted.
     * @param webRequest contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({LiqPayPaymentException.class})
    public final ResponseEntity<Object> handleLiqPayPaymentException(LiqPayPaymentException ex,
        WebRequest webRequest) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponce);
    }

    /**
     * Method interceptor exception {@link UpdateAdminPageInfoException}.
     *
     * @param ex         Exception which should be intercepted.
     * @param webRequest contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({UpdateAdminPageInfoException.class})
    public final ResponseEntity<Object> handleUpdateAdminInfoException(UpdateAdminPageInfoException ex,
        WebRequest webRequest) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponce);
    }

    /**
     * Method interceptor exception {@link NotEnoughBagsException}.
     *
     * @param ex         Exception which should be intercepted.
     * @param webRequest contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({NotEnoughBagsException.class})
    public final ResponseEntity<Object> handleNotEnoughBagsException(NotEnoughBagsException ex,
        WebRequest webRequest) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(webRequest));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponce);
    }

    /**
     * Method interceptor exception {@link SumOfOrderException}.
     *
     * @param ex         Exception which should be intercepted.
     * @param webRequest contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({SumOfOrderException.class})
    public final ResponseEntity<Object> handleSumOfOrderException(SumOfOrderException ex,
        WebRequest webRequest) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(webRequest));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponce);
    }

    /**
     * Method interceptor exception {@link CourierLocationException}.
     *
     * @param ex         Exception which should be intercepted.
     * @param webRequest contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({CourierLocationException.class})
    public final ResponseEntity<Object> handleCourierLocationException(CourierLocationException ex,
        WebRequest webRequest) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(webRequest));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponce);
    }

    /**
     * Method interceptor exception {@link LocationAlreadyCreatedException}.
     *
     * @param ex         Exception which should be intercepted.
     * @param webRequest contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({LocationAlreadyCreatedException.class})
    public final ResponseEntity<Object> handleRegionNotFoundException(LocationAlreadyCreatedException ex,
        WebRequest webRequest) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(webRequest));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponce);
    }

    /**
     * Method interceptor exception {@link NotFoundException}.
     *
     * @param ex      Exception which should be intercepted.
     * @param request contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler(NotFoundException.class)
    public final ResponseEntity<Object> handleNotFound(NotFoundException ex, WebRequest request) {
        ExceptionResponce exceptionResponse = new ExceptionResponce(getErrorAttributes(request));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse);
    }

    /**
     * Method interceptor exception {@link IncorrectEcoNumberFormatException}.
     *
     * @param ex      Exception which should be intercepted.
     * @param request contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler(IncorrectEcoNumberFormatException.class)
    public final ResponseEntity<Object> incorrectEcoNumberFormat(IncorrectEcoNumberFormatException ex,
        WebRequest request) {
        ExceptionResponce exceptionResponse = new ExceptionResponce(getErrorAttributes(request));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
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
     * Method interceptor exception {@link TariffNotFoundException}.
     *
     * @param ex         Exception which should be intercepted.
     * @param webRequest contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler(TariffNotFoundException.class)
    public final ResponseEntity<Object> handleTariffNotFound(TariffNotFoundException ex, WebRequest webRequest) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponce);
    }

    /**
     * Exception handler for {@link RemoteServerUnavailableException}.
     *
     * @param ex      {@link RemoteServerUnavailableException} exception to handle.
     * @param request {@link WebRequest} with error details.
     * @return {@link ResponseEntity} with http status and exception message.
     */
    @ExceptionHandler(RemoteServerUnavailableException.class)
    public final ResponseEntity<Object> handleRemoteServerUnavailableException(
        RemoteServerUnavailableException ex, WebRequest request) {
        ExceptionResponce exceptionResponse = new ExceptionResponce(getErrorAttributes(request));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(exceptionResponse);
    }
}
