package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.client.RestClient;
import greencity.configuration.SecurityConfig;
import greencity.converters.UserArgumentResolver;
import greencity.dto.*;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.exceptions.LocationAlreadyCreatedException;
import greencity.exceptions.LocationNotFoundException;
import greencity.service.SuperAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Import;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.List;

import static greencity.ModelUtils.getUuid;
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

    private ErrorAttributes errorAttributes = new DefaultErrorAttributes();

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(superAdminController)
            .setCustomArgumentResolvers(
                new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(restClient))
            .setControllerAdvice(new CustomExceptionHandler(errorAttributes))
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
            .andExpect(status().isCreated());
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
            .andExpect(status().isCreated());
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

    @Test
    void addLocation() throws Exception {
        List<LocationCreateDto> dto = ModelUtils.getLocationCreateDtoList();
        ObjectMapper objectMapper = new ObjectMapper();
        String requestJson = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post(ubsLink + "/addLocations").principal(principal)
            .content(requestJson)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
    }

    @Test
    void addLocationInterceptLocationAlreadyCreatedException() throws Exception {
        List<LocationCreateDto> dto = ModelUtils.getLocationCreateDtoList();
        ObjectMapper objectMapper = new ObjectMapper();
        String requestJson = objectMapper.writeValueAsString(dto);

        Mockito.doThrow(LocationAlreadyCreatedException.class).when(superAdminService).addLocation(dto);

        mockMvc.perform(post(ubsLink + "/addLocations").principal(principal)
            .content(requestJson)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void deactivateLocation() throws Exception {
        mockMvc.perform(patch(ubsLink + "/deactivateLocations/" + 1L)).andExpect(status().isOk());
    }

    @Test
    void activateException() throws Exception {
        mockMvc.perform(patch(ubsLink + "/activeLocations/" + 1L)).andExpect(status().isOk());
    }

    @Test
    void addNewLocationForCourier() throws Exception {
        NewLocationForCourierDto dto = ModelUtils.getNewLocationForCourierDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String requestJson = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put(ubsLink + "/courier/location")
            .principal(principal)
            .content(requestJson)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
    }

    @Test
    void createCourierTest() throws Exception {
        CreateCourierDto dto = ModelUtils.getCreateCourierDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String requestedJson = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post(ubsLink + "/createCourier")
            .principal(principal)
            .content(requestedJson)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
    }

    @Test
    void createCourierInterceptExceptionTest() throws Exception {
        CreateCourierDto dto = ModelUtils.getCreateCourierDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String requestedJson = objectMapper.writeValueAsString(dto);

        Mockito.when(superAdminService.createCourier(dto)).thenThrow(LocationNotFoundException.class);

        mockMvc.perform(post(ubsLink + "/createCourier")
            .principal(principal)
            .content(requestedJson)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
}
