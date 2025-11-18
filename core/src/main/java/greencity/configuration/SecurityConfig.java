package greencity.configuration;

import greencity.client.UserRemoteClient;
import greencity.properties.AuthorizationProperties;
import greencity.repository.UserRepository;
import greencity.security.JwtTool;
import greencity.security.filters.AccessTokenAuthenticationFilter;
import greencity.security.providers.JwtAuthenticationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import java.util.Arrays;
import java.util.List;
import static greencity.constant.AppConstant.*;
import static jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@EnableGlobalAuthentication
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTool jwtTool;
    private final UserRemoteClient userRemoteClient;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final UserRepository userRepository;
    private final AuthorizationProperties authorizationProperties;

    @Value("${spring.messaging.stomp.websocket.allowed-origins}")
    private String[] allowedOrigins;

    /**
     * Bean {@link PasswordEncoder} that uses in coding password.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Method for configure security.
     *
     * @param http {@link HttpSecurity}
     */
    @SuppressWarnings("java:S4502")
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(corsCustomizer -> corsCustomizer.configurationSource(request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOriginPatterns(List.of(allowedOrigins));
            config.setAllowedMethods(
                Arrays.asList("GET", "POST", "OPTIONS", "DELETE", "PUT", "PATCH", "HEAD"));
            config.setAllowedHeaders(
                Arrays.asList("Access-Control-Allow-Origin", "Access-Control-Allow-Headers",
                    "X-Requested-With", "Origin", "Content-Type", "Accept", "Authorization", "secretKey"));
            config.setAllowCredentials(true);
            config.setMaxAge(3600L);
            return config;
        }))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
            .addFilterBefore(
                new AccessTokenAuthenticationFilter(jwtTool, authenticationManager(), userRemoteClient, userRepository),
                UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(exception -> exception.authenticationEntryPoint((req, resp, exc) -> resp
                .sendError(SC_UNAUTHORIZED, "Authorize first."))
                .accessDeniedHandler((req, resp, exc) -> resp.sendError(SC_FORBIDDEN, "You don't have authorities.")))
            .authorizeHttpRequests(req -> req
                .requestMatchers("/error").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.GET,
                    "/socket/**",
                    UBS_LINK + "/getAllActiveCouriers",
                    UBS_LINK + "/locations/{courierId}",
                    UBS_LINK + "/tariffinfo/**",
                    UBS_LINK + "/locationsByCourier/{courierId}",
                    UBS_LINK + "/tariffs/{locationId}",
                    USER_AGREEMENT_LINK + "/latest",
                    UBS_LINK + "/districts-for-kyiv",
                    UBS_LINK + "/order-details-for-tariff",
                    UBS_LINK + "/activeTariffsInfo",
                    COMMIT_INFO,
                    SUPER_ADMIN_LINK + "/settingsText")
                .permitAll()
                .requestMatchers("/v2/api-docs/**",
                    "/v3/api-docs/**",
                    "/swagger.json",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/swagger-resources/**",
                    "/springwolf/**",
                    "/webjars/**")
                .permitAll()
                .requestMatchers(HttpMethod.GET,
                    UBS_MANAG_LINK + "/getAllCertificates",
                    UBS_MANAG_LINK + "/bigOrderTable",
                    UBS_MANAG_LINK + "/getOrdersViewParameters",
                    UBS_MANAG_LINK + "/tableParams",
                    UBS_MANAG_LINK + "/usersAll",
                    UBS_MANAG_LINK + "/get-data-for-order/{id}",
                    UBS_MANAG_LINK + "/violation-details/{id}",
                    UBS_MANAG_LINK + "/{id}/ordersAll",
                    UBS_MANAG_LINK + "/get-order-cancellation-reason/{id}",
                    UBS_MANAG_LINK + "/get-not-taken-order-reason/{id}",
                    UBS_MANAG_LINK + "/check-status-transition/formed-to-canceled/{id}",
                    UBS_MANAG_LINK + "/orderTableColumnsWidth",
                    UBS_MANAG_LINK + "/city-list",
                    UBS_MANAG_LINK + "/districts-list",
                    UBS_LINK + "/order_history/{orderId}",
                    ADMIN_EMPL_LINK + "/**",
                    ADMIN_LINK + "/notification/get-all-templates",
                    ADMIN_LINK + "/notification/get-template/{id}",
                    SUPER_ADMIN_LINK + "/get-all-receiving-station",
                    SUPER_ADMIN_LINK + "/getLocations",
                    SUPER_ADMIN_LINK + "/getActiveLocations",
                    SUPER_ADMIN_LINK + "/getDeactivatedLocations",
                    SUPER_ADMIN_LINK + "/getCouriers",
                    SUPER_ADMIN_LINK + "/tariffs",
                    SUPER_ADMIN_LINK + "/{tariffId}/getTariffService",
                    SUPER_ADMIN_LINK + "/{tariffId}/getService",
                    SUPER_ADMIN_LINK + "/getTariffLimits/{tariffId}",
                    SUPER_ADMIN_LINK + "/**",
                    USER_AGREEMENT_LINK,
                    USER_AGREEMENT_LINK + "/{id}",
                    UBS_MANAG_LINK + "/locations-details",
                    UBS_LINK + "/get-address-for-order/{orderId}",
                    UBS_LINK_USERPROFILE + "/reasons")
                .hasAnyRole(ADMIN, UBS_EMPLOYEE)
                .requestMatchers(HttpMethod.POST,
                    UBS_MANAG_LINK + "/addCertificate",
                    UBS_MANAG_LINK + "/addViolationToUser",
                    UBS_MANAG_LINK + "/add-manual-payment/{id}",
                    UBS_MANAG_LINK + "/order/{id}/cancellation",
                    ADMIN_EMPL_LINK + "/**",
                    SUPER_ADMIN_LINK + "/add-new-tariff",
                    SUPER_ADMIN_LINK + "/check-if-tariff-exists",
                    SUPER_ADMIN_LINK + "/addLocations",
                    SUPER_ADMIN_LINK + "/createCourier",
                    SUPER_ADMIN_LINK + "/{tariffId}/createService",
                    SUPER_ADMIN_LINK + "/{tariffId}/createTariffService",
                    SUPER_ADMIN_LINK + "/create-receiving-station",
                    SUPER_ADMIN_LINK + "/locations/edit",
                    SUPER_ADMIN_LINK + "/**",
                    USER_AGREEMENT_LINK + "/**")
                .hasAnyRole(ADMIN, UBS_EMPLOYEE)
                .requestMatchers(HttpMethod.POST,
                    LOGS_LINKS)
                .hasAnyRole(ADMIN, UBS_EMPLOYEE, USER)
                .requestMatchers(HttpMethod.PUT,
                    UBS_MANAG_LINK + "/changeOrdersTableView",
                    UBS_MANAG_LINK + "/updateViolationToUser",
                    UBS_MANAG_LINK + "/all-order-page-admin-info",
                    UBS_MANAG_LINK + "/update-manual-payment/{id}",
                    UBS_MANAG_LINK + "/changingOrder",
                    UBS_MANAG_LINK + "/blockOrders",
                    UBS_MANAG_LINK + "/unblockOrders",
                    UBS_MANAG_LINK + "/save-reason/{id}",
                    UBS_MANAG_LINK + "/orderTableColumnsWidth",
                    UBS_MANAG_LINK + "/saveOrderTableColumnsWidthIsFreeze",
                    ADMIN_EMPL_LINK + "/**",
                    ADMIN_LINK + "/notification/update-template/{id}",
                    ADMIN_LINK + "/notification/change-template-status/{id}",
                    SUPER_ADMIN_LINK + "/update-courier",
                    SUPER_ADMIN_LINK + "/update-receiving-station",
                    SUPER_ADMIN_LINK + "/editTariffService/{id}",
                    SUPER_ADMIN_LINK + "/editService/{id}",
                    SUPER_ADMIN_LINK + "/setTariffLimits/{tariffId}",
                    SUPER_ADMIN_LINK + "/editTariffInfo/{id}",
                    SUPER_ADMIN_LINK + "/activate-employee/{id}",
                    SUPER_ADMIN_LINK + "/**",
                    SUPER_ADMIN_LINK + "/settingsText/section",
                    UBS_LINK_USERPROFILE + "/status/{userId}")
                .hasAnyRole(ADMIN, UBS_EMPLOYEE)
                .requestMatchers(HttpMethod.DELETE,
                    ADMIN_EMPL_LINK + "/**",
                    UBS_MANAG_LINK + "/delete-violation-from-order/{id}",
                    UBS_MANAG_LINK + "/delete-manual-payment/{id}",
                    UBS_MANAG_LINK + "/deleteCertificate/{code}",
                    SUPER_ADMIN_LINK + "/**",
                    USER_AGREEMENT_LINK + "/**")
                .hasAnyRole(ADMIN, UBS_EMPLOYEE)
                .requestMatchers(HttpMethod.PATCH,
                    SUPER_ADMIN_LINK + "/deactivateCourier/{id}",
                    SUPER_ADMIN_LINK + "/switchTariffStatus/{tariffId}",
                    UBS_MANAG_LINK + "/addChatLink")
                .hasAnyRole(ADMIN, UBS_EMPLOYEE)
                .requestMatchers(HttpMethod.PATCH,
                    UBS_MANAG_LINK + "/update-order-page-admin-info/{id}",
                    SUPER_ADMIN_LINK + "/activeLocations/{id}",
                    UBS_LINK + "/update-address")
                .hasAnyRole(UBS_EMPLOYEE)
                .requestMatchers(HttpMethod.POST,
                    ADMIN_LINK + "/notification/add-template")
                .hasAnyRole(ADMIN, UBS_EMPLOYEE)
                .requestMatchers(HttpMethod.POST,
                    UBS_MANAG_LINK + "/**",
                    ADMIN_LINK + "/**")
                .hasAnyRole(ADMIN)
                .requestMatchers(HttpMethod.GET,
                    UBS_MANAG_LINK + "/**",
                    SUPER_ADMIN_LINK + "/**",
                    ADMIN_LINK + "/**",
                    UBS_LINK_USERPROFILE + "/user/status")
                .hasAnyRole(ADMIN, UBS_EMPLOYEE)
                .requestMatchers(HttpMethod.DELETE,
                    ADMIN_LINK + "/notification/remove-custom-template/{id}")
                .hasAnyRole(ADMIN, UBS_EMPLOYEE)
                .requestMatchers(HttpMethod.PATCH,
                    UBS_MANAG_LINK + "/**",
                    SUPER_ADMIN_LINK + "/**",
                    ADMIN_LINK + "/**")
                .hasAnyRole(ADMIN)
                .requestMatchers(HttpMethod.DELETE,
                    UBS_MANAG_LINK + "/**",
                    ADMIN_LINK + "/**")
                .hasAnyRole(ADMIN)
                .requestMatchers(HttpMethod.PUT,
                    UBS_MANAG_LINK + "/**",
                    ADMIN_LINK + "/notification/update",
                    ADMIN_LINK + "/**",
                    "/notifications/updateTemplateForOTHER",
                    "/notifications/updateTemplateForSITE")
                .hasAnyRole(ADMIN)
                .requestMatchers(HttpMethod.PUT,
                    TELEGRAM_LINK + NOTIFICATIONS_LINK,
                    UBS_LINK + "/update-recipients-data")
                .hasAnyRole(ADMIN, UBS_EMPLOYEE, USER)
                .requestMatchers(HttpMethod.HEAD,
                    UBS_MANAG_LINK + "/**",
                    SUPER_ADMIN_LINK + "/**",
                    ADMIN_LINK + "/**")
                .hasAnyRole(ADMIN)
                .requestMatchers(HttpMethod.OPTIONS,
                    UBS_MANAG_LINK + "/**",
                    SUPER_ADMIN_LINK + "/**",
                    ADMIN_LINK + "/**")
                .hasAnyRole(ADMIN)
                .requestMatchers(HttpMethod.TRACE,
                    UBS_MANAG_LINK + "/**",
                    SUPER_ADMIN_LINK + "/**",
                    ADMIN_LINK + "/**")
                .hasAnyRole(ADMIN)
                .requestMatchers(HttpMethod.POST,
                    UBS_LINK + "/order/**",
                    UBS_LINK + "/processOrder",
                    UBS_LINK + "/processOrder/{id}",
                    UBS_LINK + "/cancelPaymentAttempt/{id}",
                    UBS_LINK + "/save-order-address",
                    UBS_CLIENT_LINK + "/**",
                    "/notifications/**")
                .hasAnyRole(USER, ADMIN)
                .requestMatchers(HttpMethod.GET,
                    UBS_CLIENT_LINK + "/**",
                    UBS_LINK + "/order/{id}/cancellation",
                    UBS_LINK + "/certificate/{responseCode}",
                    "/notifications",
                    "/notifications/**",
                    "/notifications/quantityUnreadNotifications",
                    UBS_LINK + "/check-if-tariff-exists/{id}",
                    UBS_LINK + "/locations",
                    LOGS_LINKS,
                    EXPORT_SETTINGS_LINKS,
                    UBS_LINK + "/findAll-order-address",
                    UBS_LINK + "/order-details-for-tariff",
                    UBS_LINK + "/personal-data",
                    UBS_LINK + "/details-for-existing-order/{orderId}",
                    UBS_LINK + "/orders/{id}/tariff",
                    TELEGRAM_LINK + NOTIFICATIONS_LINK)
                .hasAnyRole(USER, ADMIN, UBS_EMPLOYEE)
                .requestMatchers(HttpMethod.GET,
                    TELEGRAM_LINK + "/**",
                    SUPER_ADMIN_LINK + "/tariff/{id}")
                .hasRole(UBS_EMPLOYEE)
                .requestMatchers(HttpMethod.POST,
                    TELEGRAM_LINK + "/**")
                .hasRole(UBS_EMPLOYEE)
                .requestMatchers(HttpMethod.PUT,
                    TELEGRAM_LINK + "/**")
                .hasRole(UBS_EMPLOYEE)
                .requestMatchers(HttpMethod.DELETE,
                    TELEGRAM_LINK + "/**")
                .hasAnyRole(UBS_EMPLOYEE, ADMIN)
                .requestMatchers(HttpMethod.PATCH,
                    "/notifications/{notificationId}/viewNotification",
                    "/notifications/{notificationId}/unreadNotification")
                .hasAnyRole(USER, ADMIN, UBS_EMPLOYEE)
                .requestMatchers(HttpMethod.POST,
                    UBS_LINK_USERPROFILE + "/user/create")
                .hasAnyRole(USER, ADMIN)
                .requestMatchers(HttpMethod.PUT,
                    UBS_LINK_USERPROFILE + "/**",
                    UBS_LINK + "/update-order-address")
                .hasAnyRole(USER, ADMIN, UBS_EMPLOYEE)
                .requestMatchers(HttpMethod.GET,
                    UBS_LINK_USERPROFILE + "/**",
                    UBS_LINK + "/get-all-districts",
                    UBS_EXPORT)
                .hasAnyRole(USER, ADMIN)
                .requestMatchers(HttpMethod.PATCH,
                    UBS_LINK_USERPROFILE + "/**",
                    UBS_CLIENT_LINK + "/**",
                    UBS_LINK + "/makeAddressActual/{addressId}")
                .hasAnyRole(USER, ADMIN)
                .requestMatchers(HttpMethod.DELETE,
                    UBS_LINK_USERPROFILE + "/**",
                    UBS_LINK + "/order-addresses/**",
                    UBS_CLIENT_LINK + "/delete-order/{id}")
                .hasAnyRole(USER, ADMIN)
                .requestMatchers(HttpMethod.DELETE,
                    "/notifications/{notificationId}",
                    UBS_LINK_USERPROFILE + "/user/delete")
                .hasAnyRole(USER, ADMIN, UBS_EMPLOYEE)
                .requestMatchers(HttpMethod.TRACE,
                    UBS_LINK_USERPROFILE + "/**")
                .hasAnyRole(USER, ADMIN)
                .requestMatchers(HttpMethod.OPTIONS,
                    UBS_LINK_USERPROFILE + "/**")
                .hasAnyRole(USER, ADMIN)
                .requestMatchers(HttpMethod.HEAD,
                    UBS_LINK_USERPROFILE + "/**")
                .hasAnyRole(USER, ADMIN));
        return http.build();
    }

    /**
     * Method for configure type of authentication provider.
     *
     * @param auth {@link AuthenticationManagerBuilder}
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(new JwtAuthenticationProvider(authorizationProperties, jwtTool));
    }

    /**
     * Provides AuthenticationManager.
     *
     * @return {@link AuthenticationManager}
     */
    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
            .requestMatchers(UBS_LINK + "/receivePayment")
            .requestMatchers(UBS_LINK + "/payment/return");
    }
}
