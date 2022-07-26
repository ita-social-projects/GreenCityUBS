package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.client.UserRemoteClient;
import greencity.configuration.SecurityConfig;
import greencity.converters.UserArgumentResolver;
import greencity.dto.bag.EditAmountOfBagDto;
import greencity.dto.courier.*;
import greencity.dto.location.LocationCreateDto;
import greencity.dto.order.EditPriceOfOrder;
import greencity.dto.service.AddServiceDto;
import greencity.dto.service.CreateServiceDto;
import greencity.dto.service.EditServiceDto;
import greencity.dto.tariff.EditTariffServiceDto;
import greencity.dto.tariff.GetTariffsInfoDto;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.exceptions.BadRequestException;
import greencity.filters.TariffsInfoFilterCriteria;
import greencity.service.SuperAdminService;
import lombok.SneakyThrows;
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
import org.springframework.validation.Validator;

import java.security.Principal;
import java.util.List;

import static greencity.ModelUtils.getReceivingStationDto;
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
    UserRemoteClient userRemoteClient;

    @Mock
    SuperAdminService superAdminService;

    @Mock
    private Validator mockValidator;

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
                new UserArgumentResolver(userRemoteClient))
            .setControllerAdvice(new CustomExceptionHandler(errorAttributes))
            .setValidator(mockValidator)
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
            .contentType(MediaType.APPLICATION_JSON)
            .principal(principal))
            .andExpect(status().isOk());
    }

    @Test
    void editService() throws Exception {
        EditServiceDto dto = ModelUtils.getEditServiceDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String ServiceResponseDtoJSON = objectMapper.writeValueAsString(dto);
        mockMvc.perform(put(ubsLink + "/editService/" + 1L)
            .content(ServiceResponseDtoJSON)
            .contentType(MediaType.APPLICATION_JSON)
            .principal(principal))
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

        Mockito.doThrow(BadRequestException.class).when(superAdminService).addLocation(dto);

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
    void createReceivingStation() throws Exception {
        AddingReceivingStationDto dto = AddingReceivingStationDto.builder().name("Qqq-qqq").build();
        ObjectMapper objectMapper = new ObjectMapper();
        String requestedJson = objectMapper.writeValueAsString(dto);
        mockMvc.perform(post(ubsLink + "/create-receiving-station")
            .principal(principal)
            .content(requestedJson)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
    }

    @Test
    void updateReceivingStation() throws Exception {
        ReceivingStationDto dto = getReceivingStationDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put(ubsLink + "/update-receiving-station")
            .principal(principal)
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(superAdminService, times(1)).updateReceivingStation(dto);
    }

    @Test
    @SneakyThrows
    void updateCourierTest() {
        List dtoList = List.of(CourierTranslationDto.builder()
            .name("УБС")
            .languageCode("ua")
            .build(),
            CourierTranslationDto.builder()
                .name("UBS")
                .languageCode("en")
                .build());
        CourierUpdateDto dto = CourierUpdateDto.builder()
            .courierId(1L)
            .courierTranslationDtos(dtoList)
            .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put(ubsLink + "/update-courier")
            .principal(principal)
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(superAdminService, times(1)).updateCourier(dto);
    }

    @Test
    void getAllReceivingStation() throws Exception {
        mockMvc.perform(get(ubsLink + "/get-all-receiving-station")
            .principal(principal)).andExpect(status().isOk());
        verify(superAdminService, times(1)).getAllReceivingStations();
    }

    @Test
    void deleteReceivingStation() throws Exception {
        mockMvc.perform(delete(ubsLink + "/delete-receiving-station" + "/1").principal(principal))
            .andExpect(status().isOk());
        verify(superAdminService, times(1)).deleteReceivingStation(1L);
    }

    @Test
    void getAllTariffsInfoTest() throws Exception {
        GetTariffsInfoDto getTariffsInfoDto = ModelUtils.getAllTariffsInfoDto();

        ObjectMapper objectMapper = new ObjectMapper();
        String result = objectMapper.writeValueAsString(getTariffsInfoDto);

        Mockito.when(superAdminService.getAllTariffsInfo(TariffsInfoFilterCriteria.builder().build()))
            .thenReturn(List.of(getTariffsInfoDto));

        mockMvc.perform(get(ubsLink + "/tariffs")
            .content(result)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void addNewTariffTest() {
        var dto = ModelUtils.getAddNewTariffDto();
        ObjectMapper objectMapper = new ObjectMapper();
        mockMvc.perform(post(ubsLink + "/add-new-tariff")
            .content(objectMapper.writeValueAsString(dto))
            .contentType(MediaType.APPLICATION_JSON)
            .principal(principal))
            .andExpect(status().isCreated());
    }

    @Test
    @SneakyThrows
    void editInfoAboutTariff() {
        var dto = EditPriceOfOrder.builder().maxPriceOfOrder(10000L).minPriceOfOrder(1000L).build();
        ObjectMapper objectMapper = new ObjectMapper();
        String responseJSON = objectMapper.writeValueAsString(dto);
        mockMvc.perform(patch(ubsLink + "/setLimitsBySumOfOrder/{tariffId}", 1L)
            .principal(principal)
            .content(responseJSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isAccepted());
    }

    @Test
    @SneakyThrows
    void setAmountOfSum() {
        EditAmountOfBagDto dto = ModelUtils.getAmountOfSum();
        ObjectMapper objectMapper = new ObjectMapper();
        String responseJSON = objectMapper.writeValueAsString(dto);
        mockMvc.perform(patch(ubsLink + "/setLimitsByAmountOfBags/{tariffId}", 1L)
            .principal(principal)
            .content(responseJSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isAccepted());
    }

    @Test
    @SneakyThrows
    void deactivateTariffTest() {
        mockMvc.perform(put(ubsLink + "/deactivateTariff/{tariffId}", 1L)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isAccepted());
    }

    @Test
    void setLimitDescription() throws Exception {
        mockMvc.perform(patch(ubsLink + "/setLimitDescription/{courierId}", 1L))
            .andExpect(status().isOk());
    }

    @Test
    void includeBag() throws Exception {
        mockMvc.perform(patch(ubsLink + "/includeBag/{id}", 1L)).andExpect(status().isOk());
    }

    @Test
    void excludeBag() throws Exception {
        mockMvc.perform(patch(ubsLink + "/excludeBag/{id}", 1L)).andExpect(status().isOk());
    }
}
