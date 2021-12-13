package greencity.controller;

import greencity.client.RestClient;
import greencity.configuration.SecurityConfig;

import greencity.converters.UserArgumentResolver;
import greencity.entity.enums.SortingOrder;
import greencity.service.ubs.OrdersAdminsPageService;
import greencity.service.ubs.UserViolationsService;
import greencity.service.ubs.ValuesForUserTableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;

import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;

import static greencity.ModelUtils.getPrincipal;
import static greencity.ModelUtils.getUuid;
import static org.mockito.ArgumentMatchers.anyList;
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

    @Mock
    private UserViolationsService userViolationsService;

    @Mock
    private ValuesForUserTableService valuesForUserTable;

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
        mockMvc.perform(get(management + "/tableParams/{userId}", 1))
            .andExpect(status().isOk());
        verify(ordersAdminsPageService).getParametersForOrdersTable(1L);
    }

    @Test
    void getAllViolationsByUser() throws Exception {
        mockMvc.perform(get(management + "/{userId}/violationsAll", 1))
            .andExpect(status().isOk());
        verify(userViolationsService).getAllViolations(1L);
    }
}
