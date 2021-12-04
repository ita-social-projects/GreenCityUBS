package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.client.RestClient;
import greencity.configuration.SecurityConfig;
import greencity.converters.UserArgumentResolver;
import greencity.dto.*;
import greencity.service.SuperAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;

import static greencity.ModelUtils.getUuid;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
class SuperAdminControllerTest {
    private MockMvc mockMvc;
    @Mock
    RestClient restClient;

    @Mock
    SuperAdminService superAdminService;

    @InjectMocks
    SuperAdminController superAdminController;

    private static final String ubsLink = "/ubs/superAdmin";

    private Principal principal = getUuid();

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(superAdminController)
            .setCustomArgumentResolvers(
                new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(restClient))
            .build();
    }

    @Test
    void createTariffServiceTest() throws Exception {
        AddServiceDto dto = ModelUtils.getAddServiceDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String ServiceResponceDtoJSON = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post(ubsLink + "/createTariffService")
            .principal(principal)
            .content(ServiceResponceDtoJSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void getTariffService() throws Exception {
        mockMvc.perform(get(ubsLink + "/getTariffService")).andExpect(status().isOk());
    }

    @Test
    void deleteTariffService() throws Exception {
        mockMvc.perform(delete(ubsLink + "/deleteTariffService/" + 1L))
            .andExpect(status().isOk());
    }

    @Test
    void editTariffService() throws Exception {

        EditTariffServiceDto dto = ModelUtils.getEditTariffServiceDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String ServiceResponseDtoJSON = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put(ubsLink + "/editTariffService/" + 1L)
            .content(ServiceResponseDtoJSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void editInfoAboutTariff() throws Exception {
        EditTariffInfoDto dto = ModelUtils.getEditTariffInfoDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String responseJSON = objectMapper.writeValueAsString(dto);
        mockMvc.perform(patch(ubsLink + "/editInfoAboutTariff")
            .principal(principal)
            .content(responseJSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void setAmountOfSum() throws Exception {
        EditAmountOfBagDto dto = ModelUtils.getAmountOfSum();
        ObjectMapper objectMapper = new ObjectMapper();
        String responseJSON = objectMapper.writeValueAsString(dto);
        mockMvc.perform(patch(ubsLink + "/setAmountOfBag/" + 1L)
            .principal(principal)
            .content(responseJSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void editService() throws Exception {
        EditServiceDto dto = ModelUtils.getEditServiceDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String ServiceResponseDtoJSON = objectMapper.writeValueAsString(dto);
        mockMvc.perform(put(ubsLink + "/editService/" + 1L)
            .content(ServiceResponseDtoJSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void createService() throws Exception {
        CreateServiceDto dto = ModelUtils.createServiceDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String ServiceResponceDtoJSON = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post(ubsLink + "/createService")
            .principal(principal)
            .content(ServiceResponceDtoJSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    void getService() throws Exception {
        mockMvc.perform(get(ubsLink + "/getService")).andExpect(status().isOk());
    }

    @Test
    void getLocations() throws Exception {
        mockMvc.perform(get(ubsLink + "/getLocations")).andExpect(status().isOk());
    }

    @Test
    void deleteService() throws Exception {
        mockMvc.perform(delete(ubsLink + "/deleteService/" + 1L))
            .andExpect(status().isOk());
    }

    @Test
    void getAllCouriers() throws Exception {
        mockMvc.perform(get(ubsLink + "/getCouriers")).andExpect(status().isOk());
    }
}
