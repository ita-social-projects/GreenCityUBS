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
import greencity.exceptions.http.RemoteServerUnavailableException;
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
     * Method interceptor exception {@link TooManyCertificatesEntered}.
     *
     * @param ex      Exception which should be intercepted.
     * @param request contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({TooManyCertificatesEntered.class, PaymentValidationException.class,
        IncorrectValueException.class, ConstraintViolationException.class,
        CertificateExpiredException.class, CertificateIsUsedException.class,
        BadOrderStatusRequestException.class, ImageUrlParseException.class,
        LocationStatusAlreadyExistException.class, BagWithThisStatusAlreadySetException.class,
        CourierNotFoundException.class, LiqPayPaymentException.class,
        UpdateAdminPageInfoException.class, NotEnoughBagsException.class,
        IncorrectEcoNumberFormatException.class, TariffNotFoundException.class})
    public final ResponseEntity<Object> handleBadRequestExeption(RuntimeException ex, WebRequest request) {
        ExceptionResponce exceptionResponse = new ExceptionResponce(getErrorAttributes(request));
        log.trace(ex.getMessage(), ex);
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

    /**
     * Method interceptor exception {@link SumOfOrderException}.
     *
     * @param ex         Exception which should be intercepted.
     * @param webRequest contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({SumOfOrderException.class, CourierLocationException.class,
        LocationAlreadyCreatedException.class})
    public final ResponseEntity<Object> handleSumOfOrderException(RuntimeException ex,
        WebRequest webRequest) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(webRequest));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponce);
    }

    private Map<String, Object> getErrorAttributes(WebRequest webRequest) {
        return new HashMap<>(errorAttributes.getErrorAttributes(webRequest, true));
    }

    /**
     * Method interceptor exception {@link EmployeeValidationException}.
     *
     * @param ex         Exception which should be intercepted.
     * @param webRequest contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({EmployeeValidationException.class, PositionValidationException.class,
        ReceivingStationValidationException.class, EmployeeIllegalOperationException.class})
    public final ResponseEntity<Object> handleUnprocessableEntityException(RuntimeException ex,
        WebRequest webRequest) {
        ExceptionResponce exceptionResponce = new ExceptionResponce(getErrorAttributes(webRequest));
        log.trace(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(exceptionResponce);
    }

    /**
     * Method interceptor exception {@link PaymentNotFoundException}.
     *
     * @param exception Exception which should be intercepted.
     * @param request   contain detail about occur exception.
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler({PaymentNotFoundException.class, PhoneNumberParseException.class,
        AddressAlreadyExistException.class, OrderViolationException.class,
        NotificationNotFoundException.class, CertificateNotFoundException.class,
        ActiveOrdersNotFoundException.class, NotFoundException.class,
        UnexistingUuidExeption.class, UnexistingOrderException.class,
        LanguageNotFoundException.class, EventsNotFoundException.class,
        ReceivingStationNotFoundException.class, PositionNotFoundException.class,
        EmployeeNotFoundException.class, OrderNotFoundException.class,
        NotFoundOrderAddressException.class, BagNotFoundException.class,
        LocationNotFoundException.class, ServiceNotFoundException.class})
    public final ResponseEntity<Object> handleNotFoundExeption(RuntimeException exception, WebRequest request) {
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
    @ExceptionHandler({EmployeeAlreadyAssignedForOrder.class, EmployeeAlreadyExist.class, EmployeeIsNotAssigned.class})
    public final ResponseEntity<Object> handleFoundExeption(RuntimeException ex,
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
}
