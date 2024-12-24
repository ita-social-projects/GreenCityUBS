package greencity.controller;

import greencity.configuration.SecurityConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
class CustomErrorControllerTest {
    @Mock
    HttpServletRequest request;
    @InjectMocks
    CustomErrorController customErrorController;
    private static final String RESPONSE = "NOT_FOUND";

    @Test
    void testHandleError() {
        when(request.getAttribute("jakarta.servlet.error.status_code")).thenReturn(404);
        String result = customErrorController.handleError(request);
        assertNotNull(result);
    }

    @Test
    void getErrorPathTest() {
        String expected = "/error";
        String actual = customErrorController.getErrorPath();
        assertEquals(expected, actual);
    }
}
