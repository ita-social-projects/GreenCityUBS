package greencity.configuration;

import greencity.client.UserRemoteClient;
import greencity.converters.UserArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Lazy
    @Autowired
    private UserRemoteClient userRemoteClient;

    /**
     * Method to get single threaded executor.
     *
     * @return {@link ExecutorService}
     */
    @Bean("singleThreadedExecutor")
    public ExecutorService singleThreadedExecutor() {
        return Executors.newSingleThreadExecutor();
    }

    /**
     * Method for determining which locale is going to be used.
     *
     * @return {@link SessionLocaleResolver}
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        localeResolver.setDefaultLocale(Locale.ENGLISH);
        return localeResolver;
    }

    /**
     * Method for switching to a new locale based on the value of the lang parameter
     * appended to a request.
     *
     * @return {@link LocaleChangeInterceptor}
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    /**
     * Method that returns MultipartResolver as CommonsMultipartyResolver has been
     * superseded by StandardServletMultipartResolver after migration to SpringBoot
     * 3.1.5.
     *
     * @return {@link MultipartResolver}
     */
    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new UserArgumentResolver(userRemoteClient));
    }

    /**
     * Needed for unit tests to provide fixed clock.
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
