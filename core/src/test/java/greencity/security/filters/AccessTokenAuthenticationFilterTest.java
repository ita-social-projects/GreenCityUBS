package greencity.security.filters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Optional;
import greencity.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import greencity.client.UserRemoteClient;
import greencity.security.JwtTool;
import io.jsonwebtoken.ExpiredJwtException;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
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

    @Mock
    private UserRepository userRepository;

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
    void doFilterInternalTest() throws IOException, ServletException {
        when(jwtTool.getTokenFromHttpServletRequest(request)).thenReturn("SuperSecretAccessToken");
        when(providerManager.authenticate(any()))
            .thenReturn(new UsernamePasswordAuthenticationToken("test@mail.com", null));
        String uuid = "uuid";
        when(userRepository.findUuidByRecipientEmail("test@mail.com")).thenReturn(Optional.of(uuid));
        when(userRemoteClient.checkIfUserExistsByUuid(uuid))
            .thenReturn(true);

        authenticationFilter.doFilterInternal(request, response, chain);

        verify(jwtTool).getTokenFromHttpServletRequest(request);
        verify(providerManager).authenticate(any());
        verify(userRemoteClient).checkIfUserExistsByUuid(uuid);
    }

    @Test
    void doFilterInternalTokenHasExpiredTest() throws IOException, ServletException {
        String token = "SuperSecretAccessToken";

        when(jwtTool.getTokenFromHttpServletRequest(request)).thenReturn(token);
        when(providerManager.authenticate(
            new UsernamePasswordAuthenticationToken(token, null)))
            .thenThrow(ExpiredJwtException.class);
        authenticationFilter.doFilterInternal(request, response, chain);

        assertTrue(systemOutContent.toString().contains("Token has expired: "));

        verify(jwtTool).getTokenFromHttpServletRequest(request);
        verify(providerManager).authenticate(providerManager.authenticate(
            new UsernamePasswordAuthenticationToken(token, null)));
    }

    @Test
    void doFilterInternalAccessDeniedTest() throws IOException, ServletException {
        String token = "SuperSecretAccessToken";
        when(jwtTool.getTokenFromHttpServletRequest(request)).thenReturn(token);
        when(providerManager.authenticate(any()))
            .thenReturn(new UsernamePasswordAuthenticationToken("test@mail.com", null));
        String uuid = "uuid";
        when(userRepository.findUuidByRecipientEmail("test@mail.com")).thenReturn(Optional.of(uuid));
        when(userRemoteClient.checkIfUserExistsByUuid(uuid)).thenThrow(RuntimeException.class);

        authenticationFilter.doFilterInternal(request, response, chain);

        assertTrue(systemOutContent.toString().contains("Access denied with token: "));

        verify(jwtTool).getTokenFromHttpServletRequest(request);
        verify(providerManager).authenticate(any());
        verify(userRemoteClient).checkIfUserExistsByUuid(uuid);
    }

    @Test
    @SneakyThrows
    void doFilterInternalWhenNoTokenTest() {
        String token = null;
        FilterChain spyChain = spy(chain);
        when(jwtTool.getTokenFromHttpServletRequest(request)).thenReturn(token);

        authenticationFilter.doFilterInternal(request, response, spyChain);

        verify(spyChain).doFilter(request, response);
    }
}
