package greencity.controller;

import greencity.client.RestClient;
import greencity.configuration.SecurityConfig;
import greencity.converters.UserArgumentResolver;
import greencity.service.ubs.OrdersForUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
public class AdminUbsControllerTest {
    private static final String ubsLink = "/ubs/management";
    private MockMvc mockMvc;

    @Mock
    OrdersForUserService ordersForUserService;

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

    @Test
    void testGetAllOrdersForUser() throws Exception {
        this.mockMvc.perform(get(ubsLink + "/{userId}/ordersAll", 7L));
        verify(ordersForUserService).getAllOrders(7L);
    }
}
