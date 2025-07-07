package greencity.client.config;

import greencity.constant.AppConstant;
import greencity.security.JwtTool;
import io.netty.channel.ChannelOption;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Configuration
@RequiredArgsConstructor
public class UserRemoteClientConfig {
    @Value("${greencity.redirect.user-server-address}")
    private String greenCityUserBaseUrl;

    @Value("${greencity.authorization.service-email}")
    private String systemEmail;

    @Value("${webclient.connection-timeout-millis}")
    private Integer connectionTimeoutMillis;

    @Value("${webclient.response-timeout-millis}")
    private Integer responseTimeoutMillis;

    private final JwtTool jwtTool;

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.baseUrl(greenCityUserBaseUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .filter(authorizationHeaderFilter())
            .clientConnector(
                new ReactorClientHttpConnector(
                    HttpClient.create()
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeoutMillis)
                        .responseTimeout(Duration.ofMillis(responseTimeoutMillis))))
            .build();
    }

    private ExchangeFilterFunction authorizationHeaderFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            String jwt = jwtTool.createAccessToken(systemEmail, 1);
            String authHeader = AppConstant.TOKEN_PREFIX + jwt;

            ClientRequest authorizedRequest = ClientRequest.from(clientRequest)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .build();

            return Mono.just(authorizedRequest);
        });
    }
}
