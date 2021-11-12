package greencity.configuration;

import greencity.client.RestClient;
import greencity.security.JwtTool;
import greencity.security.filters.AccessTokenAuthenticationFilter;
import greencity.security.providers.JwtAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
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

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private static final String UBS_LINK = "/ubs";
    private static final String ADMIN_LINK = "/admin";
    private final JwtTool jwtTool;
    private final RestClient restClient;

    /**
     * Constructor.
     */
    @Autowired
    public SecurityConfig(JwtTool jwtTool, RestClient restClient) {
        this.jwtTool = jwtTool;
        this.restClient = restClient;
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
                new AccessTokenAuthenticationFilter(jwtTool, authenticationManager(), restClient),
                UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling()
            .authenticationEntryPoint((req, resp, exc) -> resp.sendError(SC_UNAUTHORIZED, "Authorize first."))
            .accessDeniedHandler((req, resp, exc) -> resp.sendError(SC_FORBIDDEN, "You don't have authorities."))
            .and()
            .authorizeRequests()
            .antMatchers(HttpMethod.POST,
                UBS_LINK + "/management/**",
                UBS_LINK + "/superAdmin/**",
                UBS_LINK + "/ubs/order/{id}/cancellation/",
                ADMIN_LINK + "/**",
                "/accountinfo")
            .hasAnyRole("ADMIN")
            .antMatchers(HttpMethod.GET,
                UBS_LINK + "/management/**",
                UBS_LINK + "/superAdmin/**",
                UBS_LINK + "/ubs/order/{id}/cancellation/",
                ADMIN_LINK + "/**",
                "/accountinfo",
                "/removewebhook",
                "/setwebhook")
            .hasAnyRole("ADMIN")
            .antMatchers(HttpMethod.PATCH,
                UBS_LINK + "/management/**",
                UBS_LINK + "/superAdmin/**",
                ADMIN_LINK + "/**")
            .hasAnyRole("ADMIN")
            .antMatchers(HttpMethod.DELETE,
                UBS_LINK + "/management/**",
                UBS_LINK + "/superAdmin/**",
                ADMIN_LINK + "/**")
            .hasAnyRole("ADMIN")
            .antMatchers(HttpMethod.PUT,
                UBS_LINK + "/management/**",
                UBS_LINK + "/superAdmin/**",
                UBS_LINK + "/update-recipients-data",
                ADMIN_LINK + "/**")
            .hasAnyRole("ADMIN")
            .antMatchers(HttpMethod.HEAD,
                UBS_LINK + "/management/**",
                UBS_LINK + "/superAdmin/**",
                ADMIN_LINK + "/**")
            .hasAnyRole("ADMIN")
            .antMatchers(HttpMethod.OPTIONS,
                UBS_LINK + "/management/**",
                UBS_LINK + "/superAdmin/**",
                ADMIN_LINK + "/**")
            .hasAnyRole("ADMIN")
            .antMatchers(HttpMethod.TRACE,
                UBS_LINK + "/management/**",
                UBS_LINK + "/superAdmin/**",
                ADMIN_LINK + "/**")
            .hasAnyRole("ADMIN")
            .antMatchers(HttpMethod.POST,
                UBS_LINK + "/userProfile/**",
                UBS_LINK + "/order/**",
                UBS_LINK + "/processOrder",
                UBS_LINK + "/processLiqPayOrder",
                UBS_LINK + "/client/**",
                "/notifications/**")
            .hasAnyRole("USER", "ADMIN")
            .antMatchers(HttpMethod.GET,
                UBS_LINK + "/**",
                UBS_LINK + "/client/**",
                "/notifications/**")
            .hasAnyRole("USER", "ADMIN")
            .antMatchers(HttpMethod.PUT,
                UBS_LINK + "/userProfile/**")
            .hasAnyRole("USER", "ADMIN")
            .antMatchers(HttpMethod.PATCH,
                UBS_LINK + "/userProfile/**",
                UBS_LINK + "/client/**")
            .hasAnyRole("USER", "ADMIN")
            .antMatchers(HttpMethod.DELETE,
                UBS_LINK + "/userProfile/**")
            .hasAnyRole("USER", "ADMIN")
            .antMatchers(HttpMethod.TRACE,
                UBS_LINK + "/userProfile/**")
            .hasAnyRole("USER", "ADMIN")
            .antMatchers(HttpMethod.OPTIONS,
                UBS_LINK + "/userProfile/**")
            .hasAnyRole("USER", "ADMIN")
            .antMatchers(HttpMethod.HEAD,
                UBS_LINK + "/userProfile/**")
            .hasAnyRole("USER", "ADMIN");
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