package greencity.exception.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import greencity.exceptions.GreenCityUserServiceException;
import greencity.exceptions.ForbiddenException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.ResourceNotFoundException;
import greencity.exceptions.UnprocessableEntityException;
import greencity.exceptions.WrongSignatureException;
import greencity.exceptions.api.GoogleApiException;
import greencity.exceptions.user.UserNotFoundException;
import greencity.exceptions.validation.ValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@ExtendWith(MockitoExtension.class)
class CustomExceptionHandlerTest {
    @Mock
    WebRequest webRequest;

    @Mock
    ErrorAttributes errorAttributes;

    Map<String, Object> objectMap;

    @Mock
    HttpHeaders headers;

    @Mock
    MethodArgumentNotValidException notValidException;

    @Mock
    UnprocessableEntityException unprocessableEntityException;

    @Mock
    WrongSignatureException wrongSignatureException;

    @Mock
    GoogleApiException googleApiException;

    @Mock
    UserNotFoundException userNotFoundException;

    @Mock
    HttpMessageNotReadableException notReadableException;

    @Mock
    ResourceNotFoundException resourceNotFoundException;

    @Mock
    ValidationException validationException;

    @Mock
    NotFoundException notFoundException;

    @Mock
    ForbiddenException forbiddenException;

    @Mock
    private static GreenCityUserServiceException greenCityUserServiceException;

    @Mock
    private static WebClientRequestException webClientRequestException;

    @Mock
    private static WebClientResponseException webClientResponseException;

    @Mock
    HttpStatus status;

    @InjectMocks
    CustomExceptionHandler customExceptionHandler;

    @BeforeEach
    void init() {
        objectMap = new HashMap<>();
        objectMap.put("path", "/ownSecurity/restorePassword");
        objectMap.put("message", "test");
        objectMap.put("timestamp", "2021-02-06T17:27:50.569+0000");
        objectMap.put("trace", "Internal Server Error");
    }

    @Test
    void handleBadRequestExceptionTest() {
        ExceptionResponse exceptionResponse = new ExceptionResponse(objectMap);
        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)))
            .thenReturn(objectMap);
        assertEquals(customExceptionHandler.handleBadRequestException(webRequest),
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse));
        verify(errorAttributes).getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class));
    }

    @Test
    void handleHttpMessageNotReadableTest() {
        ExceptionResponse exceptionResponse = new ExceptionResponse(objectMap);
        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)))
            .thenReturn(objectMap);
        assertEquals(
            customExceptionHandler.handleHttpMessageNotReadable(notReadableException, headers, status, webRequest),
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse));
        verify(errorAttributes).getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class));
    }

    @Test
    void handleMethodArgumentNotValidTest() {
        FieldError fieldError = new FieldError("G", "field", "default");
        var dto = new ValidationExceptionDto(fieldError);

        final BindingResult bindingResult = mock(BindingResult.class);

        when(notValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));
        assertEquals(
            customExceptionHandler.handleMethodArgumentNotValid(notValidException, headers, status, webRequest),
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonList(dto)));
        verify(notValidException).getBindingResult();
        verify(bindingResult).getFieldErrors();
    }

    @Test
    void handleUnprocessableEntityExceptionTest() {
        ExceptionResponse exceptionResponse = new ExceptionResponse(objectMap);
        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)))
            .thenReturn(objectMap);
        assertEquals(
            customExceptionHandler.handleUnprocessableEntityException(unprocessableEntityException, webRequest),
            ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(exceptionResponse));
        verify(errorAttributes).getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class));
    }

    @Test
    void handleNotFoundExceptionTest() {
        ExceptionResponse exceptionResponse = new ExceptionResponse(objectMap);
        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)))
            .thenReturn(objectMap);
        assertEquals(customExceptionHandler.handleNotFoundException(notFoundException, webRequest),
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse));
        verify(errorAttributes).getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class));
    }

    @Test
    void handleAccessDeniedExceptionTest() {
        ExceptionResponse exceptionResponse = new ExceptionResponse(objectMap);
        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)))
            .thenReturn(objectMap);
        assertEquals(customExceptionHandler.handleAccessDeniedException(webRequest),
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(exceptionResponse));
        verify(errorAttributes).getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class));
    }

    @Test
    void handleRemoteServerUnavailableExceptionTest() {
        ExceptionResponse exceptionResponse = new ExceptionResponse(objectMap);
        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)))
            .thenReturn(objectMap);
        assertEquals(customExceptionHandler.handleRemoteServerUnavailableException(webRequest),
            ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(exceptionResponse));
        verify(errorAttributes).getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class));
    }

    @Test
    void handleTariffAlreadyExistsExceptionTest() {
        ExceptionResponse exceptionResponse = new ExceptionResponse(objectMap);
        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)))
            .thenReturn(objectMap);
        assertEquals(customExceptionHandler.handleTariffAlreadyExistsException(webRequest),
            ResponseEntity.status(HttpStatus.CONFLICT).body(exceptionResponse));
        verify(errorAttributes).getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class));
    }

    @Test
    void handleAddressNotWithinLocationAreaExceptionTest() {
        ExceptionResponse exceptionResponse = new ExceptionResponse(objectMap);
        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)))
            .thenReturn(objectMap);
        assertEquals(customExceptionHandler.handleAddressNotWithinLocationAreaException(webRequest),
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse));
        verify(errorAttributes).getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class));
    }

    @Test
    void handleEntityNotFoundExceptionTest() {
        ExceptionResponse exceptionResponse = new ExceptionResponse(objectMap);
        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)))
            .thenReturn(objectMap);
        assertEquals(customExceptionHandler.handleEntityNotFoundException(webRequest),
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse));
        verify(errorAttributes).getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class));
    }

    @Test
    void handleResourceNotFoundExceptionTest() {
        ExceptionResponse exceptionResponse = new ExceptionResponse(objectMap);
        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)))
            .thenReturn(objectMap);
        assertEquals(
            customExceptionHandler.handleResourceNotFoundException(resourceNotFoundException, webRequest),
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse));
        verify(errorAttributes).getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class));
    }

    @Test
    void handleValidationExceptionTest() {
        ExceptionResponse exceptionResponse = new ExceptionResponse(objectMap);
        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)))
            .thenReturn(objectMap);
        assertEquals(
            customExceptionHandler.handleValidationException(validationException, webRequest),
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse));
        verify(errorAttributes).getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class));
    }

    @Test
    void handleGoogleApiExceptionTest() {
        ExceptionResponse exceptionResponse = new ExceptionResponse(objectMap);
        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)))
            .thenReturn(objectMap);
        assertEquals(customExceptionHandler.handleGoogleApiException(googleApiException, webRequest),
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse));
        verify(errorAttributes).getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class));
    }

    @Test
    void handleUserNotFoundExceptionTest() {
        ExceptionResponse exceptionResponse = new ExceptionResponse(objectMap);
        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)))
            .thenReturn(objectMap);
        assertEquals(customExceptionHandler.handleUserNotFoundException(userNotFoundException, webRequest),
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse));
        verify(errorAttributes).getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class));
    }

    @Test
    void handleWrongSignatureExceptionTest() {
        ExceptionResponse exceptionResponse = new ExceptionResponse(objectMap);
        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)))
            .thenReturn(objectMap);
        assertEquals(customExceptionHandler.handleWrongSignatureException(wrongSignatureException, webRequest),
            ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(exceptionResponse));
        verify(errorAttributes).getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class));
    }

    @Test
    void handleConstraintViolationExceptionSingleViolation() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);

        when(path.toString()).thenReturn("fieldName");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must not be null");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)))
            .thenReturn(objectMap);

        ExceptionResponse expectedResponse = new ExceptionResponse(objectMap);
        expectedResponse.setMessage("Validation failed: fieldName: must not be null");

        ResponseEntity<Object> response =
            customExceptionHandler.handleConstraintViolationException(ex, webRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());

        verify(errorAttributes).getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class));
    }

    @Test
    void handleConstraintViolationExceptionMultipleViolations() {
        ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
        ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
        Path path1 = mock(Path.class);
        Path path2 = mock(Path.class);

        when(path1.toString()).thenReturn("username");
        when(path2.toString()).thenReturn("password");

        when(violation1.getPropertyPath()).thenReturn(path1);
        when(violation1.getMessage()).thenReturn("must not be null");

        when(violation2.getPropertyPath()).thenReturn(path2);
        when(violation2.getMessage()).thenReturn("size must be at least 3");

        Set<ConstraintViolation<?>> orderedViolations = new LinkedHashSet<>();
        orderedViolations.add(violation1);
        orderedViolations.add(violation2);

        ConstraintViolationException ex = new ConstraintViolationException(orderedViolations);

        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)))
            .thenReturn(objectMap);

        ExceptionResponse expectedResponse = new ExceptionResponse(objectMap);
        expectedResponse.setMessage("Validation failed: password: size must be at least 3, username: must not be null");

        ResponseEntity<Object> response =
            customExceptionHandler.handleConstraintViolationException(ex, webRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());

        verify(errorAttributes).getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class));
    }

    @Test
    void handleConstraintViolationExceptionWithNoViolations() {
        ConstraintViolationException ex = new ConstraintViolationException(Collections.emptySet());

        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)))
            .thenReturn(objectMap);

        ExceptionResponse expectedResponse = new ExceptionResponse(objectMap);
        expectedResponse.setMessage("Validation failed with no specific details.");

        ResponseEntity<Object> response =
            customExceptionHandler.handleConstraintViolationException(ex, webRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());

        verify(errorAttributes).getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class));
    }

    @Test
    void handleUserServiceExceptionWithWebClientRequestExceptionTest() {
        ExceptionResponse exceptionResponse = new ExceptionResponse(objectMap);
        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)))
            .thenReturn(objectMap);
        assertEquals(customExceptionHandler.handleUserServiceException(webClientRequestException, webRequest),
            ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(exceptionResponse));
        verify(errorAttributes).getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class));
    }

    @ParameterizedTest
    @MethodSource("provideExceptionsForHandleUserServiceException")
    void handleUserServiceExceptionTest(Exception exception, HttpStatus expectedStatus) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(objectMap);
        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)))
            .thenReturn(objectMap);
        assertEquals(customExceptionHandler.handleUserServiceException(exception, webRequest),
            ResponseEntity.status(expectedStatus).body(exceptionResponse));
        verify(errorAttributes).getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class));
    }

    private static Stream<Arguments> provideExceptionsForHandleUserServiceException() {
        return Stream.of(
            Arguments.of(webClientRequestException, HttpStatus.SERVICE_UNAVAILABLE),
            Arguments.of(greenCityUserServiceException, HttpStatus.INTERNAL_SERVER_ERROR),
            Arguments.of(webClientResponseException, HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    void handleForbiddenExceptionTest() {
        ExceptionResponse exceptionResponse = new ExceptionResponse(objectMap);
        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)))
            .thenReturn(objectMap);
        assertEquals(customExceptionHandler.handleForbiddenException(forbiddenException, webRequest),
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(exceptionResponse));
        verify(errorAttributes).getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class));
    }

}
