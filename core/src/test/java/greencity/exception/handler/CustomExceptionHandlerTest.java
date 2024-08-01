package greencity.exception.handler;

import greencity.exceptions.FoundException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.UnprocessableEntityException;
import greencity.exceptions.http.AccessDeniedException;
import greencity.exceptions.notification.TemplateDeleteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

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
    HttpMessageNotReadableException notReadableException;

    @Mock
    NotFoundException notFoundException;

    @Mock
    FoundException foundException;

    @Mock
    TemplateDeleteException templateDeleteException;

    @Mock
    HttpStatus status;

    @InjectMocks
    CustomExceptionHandler customExceptionHandler;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
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
    void handleFoundExceptionTest() {
        ExceptionResponse exceptionResponse = new ExceptionResponse(objectMap);
        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)))
            .thenReturn(objectMap);
        assertEquals(customExceptionHandler.handleFoundException(foundException, webRequest),
            ResponseEntity.status(HttpStatus.FOUND).body(exceptionResponse));
        verify(errorAttributes).getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class));
    }

    @Test
    void handleForbiddenExceptionsTest() {
        ExceptionResponse exceptionResponse = new ExceptionResponse(objectMap);
        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)))
            .thenReturn(objectMap);
        assertEquals(customExceptionHandler.handleForbiddenExceptions(webRequest),
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
}
