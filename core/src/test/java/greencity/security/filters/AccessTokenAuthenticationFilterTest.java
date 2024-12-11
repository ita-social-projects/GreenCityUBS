package greencity.security.filters;

import greencity.client.UserRemoteClient;
import greencity.dto.user.UserVO;
import greencity.security.JwtTool;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccessTokenAuthenticationFilterTest {
    private PrintStream systemOut;
    private ByteArrayOutputStream systemOutContent;

    @Mock
    JwtTool jwtTool;
    @Mock
    ProviderManager providerManager;
    @Mock
    UserRemoteClient userRemoteClient;

    HttpServletRequest request = new MockHttpServletRequest();

    HttpServletResponse response = new MockHttpServletResponse();

    FilterChain chain = new MockFilterChain();

    private static final String TOKEN = "SuperSecretAccessToken";

    @InjectMocks
    private AccessTokenAuthenticationFilter authenticationFilter;

    @BeforeEach
    void setUp() {
        systemOut = System.out;
        systemOutContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(systemOutContent));
    }

    @AfterEach
    void restoreSystemOutStream() {
        System.setOut(systemOut);
    }

    @Test
    @Disabled("Not implemented by the original author")
    void doFilterInternalTest() throws IOException, ServletException {
        when(jwtTool.getTokenFromHttpServletRequest(request)).thenReturn("SuperSecretAccessToken");
        when(providerManager.authenticate(any()))
            .thenReturn(new UsernamePasswordAuthenticationToken("test@mail.com", null));
        when(userRemoteClient.findNotDeactivatedByEmail("test@mail.com"))
            .thenReturn(Optional.of(UserVO.builder().id(1L).build()));

        authenticationFilter.doFilterInternal(request, response, chain);

        verify(jwtTool).getTokenFromHttpServletRequest(request);
        verify(providerManager).authenticate(any());
        verify(userRemoteClient).findNotDeactivatedByEmail("test@mail.com");
    }

    @Test
    void doFilterInternalTokenHasExpiredTest() throws IOException, ServletException {
        when(jwtTool.getTokenFromHttpServletRequest(request)).thenReturn(TOKEN);
        when(providerManager.authenticate(
            new UsernamePasswordAuthenticationToken(TOKEN, null)))
            .thenThrow(ExpiredJwtException.class);
        authenticationFilter.doFilterInternal(request, response, chain);

        assertTrue(systemOutContent.toString().contains("Token has expired: "));

        verify(jwtTool).getTokenFromHttpServletRequest(request);
        verify(providerManager).authenticate(providerManager.authenticate(
            new UsernamePasswordAuthenticationToken(TOKEN, null)));
    }

    @Test
    @Disabled("Not implemented by the original author")
    void doFilterInternalAccessDeniedTest() throws IOException, ServletException {
        when(jwtTool.getTokenFromHttpServletRequest(request)).thenReturn(TOKEN);
        when(providerManager.authenticate(any()))
            .thenReturn(new UsernamePasswordAuthenticationToken("test@mail.com", null));
        when(userRemoteClient.findNotDeactivatedByEmail("test@mail.com")).thenThrow(RuntimeException.class);

        authenticationFilter.doFilterInternal(request, response, chain);

        assertTrue(systemOutContent.toString().contains("Access denied with token: "));

        verify(jwtTool).getTokenFromHttpServletRequest(request);
        verify(providerManager).authenticate(any());
        verify(userRemoteClient).findNotDeactivatedByEmail("test@mail.com");
    }
}
