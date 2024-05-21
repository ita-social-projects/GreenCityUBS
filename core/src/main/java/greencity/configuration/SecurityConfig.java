package greencity.configuration;

import greencity.security.JwtTool;
import greencity.security.filters.AccessTokenAuthenticationFilter;
import greencity.security.providers.JwtAuthenticationProvider;
import greencity.service.FeignClientCallAsync;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

import static greencity.constant.AppConstant.*;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final JwtTool jwtTool;
    private final FeignClientCallAsync userRemoteClient;

    /**
     * Constructor.
     */
    @Autowired
    public SecurityConfig(JwtTool jwtTool, FeignClientCallAsync userRemoteClient) {
        this.jwtTool = jwtTool;
        this.userRemoteClient = userRemoteClient;
    }

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

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors()
            .and()
            .csrf()
            .disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .addFilterBefore(
                new AccessTokenAuthenticationFilter(jwtTool, authenticationManager(), userRemoteClient),
                UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling()
            .authenticationEntryPoint((req, resp, exc) -> resp.sendError(SC_UNAUTHORIZED, "Authorize first."))
            .accessDeniedHandler((req, resp, exc) -> resp.sendError(SC_FORBIDDEN, "You don't have authorities."))
            .and()
            .authorizeRequests()
            .antMatchers(HttpMethod.GET,
                UBS_LINK + "/order-details-for-tariff",
                ADMIN_EMPL_LINK + "/get-employees/{tariffId}",
                UBS_LINK + "/tariffs/{locationId}",
                UBS_LINK + "/getAllActiveCouriers",
                UBS_LINK + "/locations/{courierId}")
            .permitAll()
            .antMatchers(HttpMethod.POST, UBS_LINK + "/userProfile/user/create")
            .permitAll()
            .and()
            .authorizeRequests()
            .antMatchers(HttpMethod.GET,
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
                UBS_MANAG_LINK + "/orderTableColumnsWidth",
                UBS_LINK + "/order_history/{orderId}",
                // ADMIN_EMPL_LINK + "/**",
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
                SUPER_ADMIN_LINK + "/**")
            .hasAnyRole(ADMIN, UBS_EMPLOYEE)
            .antMatchers(HttpMethod.POST,
                UBS_MANAG_LINK + "/addCertificate",
                UBS_MANAG_LINK + "/addViolationToUser",
                UBS_MANAG_LINK + "/add-manual-payment/{id}",
                UBS_MANAG_LINK + "/add-bonuses-user/{id}",
                UBS_MANAG_LINK + "/order/{id}/cancellation",
                UBS_MANAG_LINK + "/save-order-for-refund/{orderId}",
                ADMIN_EMPL_LINK + "/**",
                SUPER_ADMIN_LINK + "/add-new-tariff",
                SUPER_ADMIN_LINK + "/check-if-tariff-exists",
                SUPER_ADMIN_LINK + "/addLocations",
                SUPER_ADMIN_LINK + "/createCourier",
                SUPER_ADMIN_LINK + "/{tariffId}/createService",
                SUPER_ADMIN_LINK + "/{tariffId}/createTariffService",
                SUPER_ADMIN_LINK + "/create-receiving-station",
                SUPER_ADMIN_LINK + "/locations/edit",
                SUPER_ADMIN_LINK + "/**")
            .hasAnyRole(ADMIN, UBS_EMPLOYEE)
            .antMatchers(HttpMethod.PUT,
                UBS_MANAG_LINK + "/changeOrdersTableView",
                UBS_MANAG_LINK + "/updateViolationToUser",
                UBS_MANAG_LINK + "/all-order-page-admin-info",
                UBS_MANAG_LINK + "/update-manual-payment/{id}",
                UBS_MANAG_LINK + "/changingOrder",
                UBS_MANAG_LINK + "/blockOrders",
                UBS_MANAG_LINK + "/unblockOrders",
                UBS_MANAG_LINK + "/save-reason/{id}",
                UBS_MANAG_LINK + "/orderTableColumnsWidth",
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
                SUPER_ADMIN_LINK + "/**")
            .hasAnyRole(ADMIN, UBS_EMPLOYEE)
            .antMatchers(HttpMethod.DELETE,
                ADMIN_EMPL_LINK + "/**",
                UBS_MANAG_LINK + "/delete-violation-from-order/{id}",
                UBS_MANAG_LINK + "/delete-manual-payment/{id}",
                UBS_MANAG_LINK + "/deleteCertificate/{code}",
                SUPER_ADMIN_LINK + "/**")
            .hasAnyRole(ADMIN, UBS_EMPLOYEE)
            .antMatchers(HttpMethod.PATCH,
                SUPER_ADMIN_LINK + "/deactivateCourier/{id}",
                SUPER_ADMIN_LINK + "/switchTariffStatus/{tariffId}")
            .hasAnyRole(ADMIN, UBS_EMPLOYEE)
            .antMatchers(HttpMethod.PATCH,
                UBS_MANAG_LINK + "/update-order-page-admin-info/{id}",
                SUPER_ADMIN_LINK + "/activeLocations/{id}")
            .hasAnyRole(UBS_EMPLOYEE)
            .antMatchers(HttpMethod.POST,
                UBS_MANAG_LINK + "/**",
                ADMIN_LINK + "/**",
                "/accountinfo")
            .hasAnyRole(ADMIN)
            .antMatchers(HttpMethod.GET,
                UBS_MANAG_LINK + "/**",
                SUPER_ADMIN_LINK + "/**",
                UBS_LINK + "/order/{id}/cancellation",
                ADMIN_LINK + "/notification/get-all",
                ADMIN_LINK + "/notification/{id}",
                // ADMIN_LINK + "/**",
                "/accountinfo",
                "/removewebhook",
                "/setwebhook")
            .hasAnyRole(ADMIN, UBS_EMPLOYEE)
            .antMatchers(HttpMethod.PATCH,
                UBS_MANAG_LINK + "/**",
                SUPER_ADMIN_LINK + "/**",
                ADMIN_LINK + "/**")
            .hasAnyRole(ADMIN)
            .antMatchers(HttpMethod.DELETE,
                UBS_MANAG_LINK + "/**",
                ADMIN_LINK + "/**")
            .hasAnyRole(ADMIN)
            .antMatchers(HttpMethod.PUT,
                UBS_MANAG_LINK + "/**",
                UBS_LINK + "/update-recipients-data",
                ADMIN_LINK + "/notification/update",
                ADMIN_LINK + "/**",
                "/notifications/updateTemplateForOTHER",
                "/notifications/updateTemplateForSITE")
            .hasAnyRole(ADMIN)
            .antMatchers(HttpMethod.HEAD,
                UBS_MANAG_LINK + "/**",
                SUPER_ADMIN_LINK + "/**",
                ADMIN_LINK + "/**")
            .hasAnyRole(ADMIN)
            .antMatchers(HttpMethod.OPTIONS,
                UBS_MANAG_LINK + "/**",
                SUPER_ADMIN_LINK + "/**",
                ADMIN_LINK + "/**")
            .hasAnyRole(ADMIN)
            .antMatchers(HttpMethod.TRACE,
                UBS_MANAG_LINK + "/**",
                SUPER_ADMIN_LINK + "/**",
                ADMIN_LINK + "/**")
            .hasAnyRole(ADMIN)
            .antMatchers(HttpMethod.POST,
                UBS_LINK + "/userProfile/**",
                UBS_LINK + "/order/**",
                UBS_LINK + "/processOrder",
                UBS_LINK + "/processLiqPayOrder",
                UBS_LINK + "/processLiqPayOrder/{id}",
                UBS_LINK + "/save-order-address",
                UBS_LINK + "/client/**",
                UBS_LINK + "/details-for-existing-order/{orderId}",
                "/notifications/**")
            .hasAnyRole(USER, ADMIN)
            .antMatchers(HttpMethod.GET,
                UBS_LINK + "/**",
                UBS_LINK + "/client/**",
                "/notifications",
                "/notifications/**",
                UBS_LINK + "/check-if-tariff-exists/{id}",
                UBS_LINK + "/locations",
                "/notifications/quantityUnreadenNotifications")
            .hasAnyRole(USER, ADMIN, UBS_EMPLOYEE)
            .antMatchers(HttpMethod.PUT,
                UBS_LINK + "/userProfile/**",
                UBS_LINK + "/update-order-address")
            .hasAnyRole(USER, ADMIN)
            .antMatchers(HttpMethod.GET,
                UBS_LINK + "/userProfile/**",
                UBS_LINK + "/get-all-districts")
            .hasAnyRole(USER, ADMIN)
            .antMatchers(HttpMethod.PATCH,
                UBS_LINK + "/userProfile/**",
                UBS_LINK + "/client/**",
                UBS_LINK + "/makeAddressActual/{addressId}")
            .hasAnyRole(USER, ADMIN)
            .antMatchers(HttpMethod.DELETE,
                UBS_LINK + "/userProfile/**",
                UBS_LINK + "/order-addresses/**",
                UBS_LINK + "/client/delete-order/{id}")
            .hasAnyRole(USER, ADMIN)
            .antMatchers(HttpMethod.TRACE,
                UBS_LINK + "/userProfile/**")
            .hasAnyRole(USER, ADMIN)
            .antMatchers(HttpMethod.OPTIONS,
                UBS_LINK + "/userProfile/**")
            .hasAnyRole(USER, ADMIN)
            .antMatchers(HttpMethod.HEAD,
                UBS_LINK + "/userProfile/**")
            .hasAnyRole(USER, ADMIN);
    }

    /**
     * Method for configure matchers that will be ignored in security.
     *
     * @param web {@link WebSecurity}
     */
    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers(UBS_LINK + "/receivePayment");
        web.ignoring().antMatchers(UBS_LINK + "/receiveLiqPayPayment");
        web.ignoring().antMatchers("/bot");
        web.ignoring().antMatchers("/v2/api-docs/**");
        web.ignoring().antMatchers("/swagger.json");
        web.ignoring().antMatchers("/swagger-ui.html");
        web.ignoring().antMatchers("/swagger-resources/**");
        web.ignoring().antMatchers("/webjars/**");
    }

    /**
     * Method for configure type of authentication provider.
     *
     * @param auth {@link AuthenticationManagerBuilder}
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(new JwtAuthenticationProvider(jwtTool));
    }

    /**
     * Provides AuthenticationManager.
     *
     * @return {@link AuthenticationManager}
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

    /**
     * Bean {@link CorsConfigurationSource} that uses for CORS setup.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList("*"));
        configuration.setAllowedMethods(
            Arrays.asList("GET", "POST", "OPTIONS", "DELETE", "PUT", "PATCH"));
        configuration.setAllowedHeaders(
            Arrays.asList(
                "X-Requested-With", "Origin", "Content-Type", "Accept", "Authorization"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
