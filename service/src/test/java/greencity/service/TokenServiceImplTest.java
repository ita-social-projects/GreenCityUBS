package greencity.service;

import greencity.exceptions.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TokenServiceImplTest {
    @Mock
    private HttpServletResponse httpServletResponse;
    @Mock
    private Cookie cookie;
    @InjectMocks
    TokenServiceImpl tokenService;

    @Test
    void testPassTokenToCookies() {
        String checkToken = "eyJhbGciOiJIUzI1NiB9";
        String accessToken = "eyJhbGciOiJIUzI1NiJ9";

        assertThrows(BadRequestException.class, () -> tokenService.passTokenToCookies(checkToken, null));

        tokenService.passTokenToCookies(accessToken, httpServletResponse);

        verify(httpServletResponse).addCookie(any(Cookie.class));
    }
}
