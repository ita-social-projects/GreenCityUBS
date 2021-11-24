package greencity.controller;

import greencity.client.RestClient;
import greencity.configuration.SecurityConfig;
import greencity.converters.UserArgumentResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
public class AdminUbsControllerTest {
    private MockMvc mockMvc;

    @Mock
    RestClient restClient;

    @InjectMocks
    AdminUbsController adminUbsController;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(adminUbsController)
            .setCustomArgumentResolvers(new UserArgumentResolver(restClient))
            .build();
    }
}
