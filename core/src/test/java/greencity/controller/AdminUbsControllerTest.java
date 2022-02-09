package greencity.controller;

import greencity.client.RestClient;
import greencity.configuration.SecurityConfig;

import greencity.converters.UserArgumentResolver;
import greencity.service.ubs.OrdersAdminsPageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;

import static greencity.ModelUtils.getUuid;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
class AdminUbsControllerTest {
    private MockMvc mockMvc;

    @Mock
    private OrdersAdminsPageService ordersAdminsPageService;

    private static final String management = "/ubs/management";
    @InjectMocks
    AdminUbsController adminUbsController;

    private Principal principal = getUuid();

    @Mock
    RestClient restClient;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(adminUbsController)
            .setCustomArgumentResolvers(new UserArgumentResolver(restClient))
            .build();
    }

    @Test
    void getTableParameters() throws Exception {
        when(restClient.findUuidByEmail((anyString()))).thenReturn("35467585763t4sfgchjfuyetf");
        mockMvc.perform(get(management + "/tableParams")
            .principal(principal))
            .andExpect(status().isOk());
        verify(ordersAdminsPageService).getParametersForOrdersTable("35467585763t4sfgchjfuyetf");
    }
}
