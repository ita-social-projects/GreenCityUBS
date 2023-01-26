package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.client.UserRemoteClient;
import greencity.configuration.SecurityConfig;
import greencity.constant.ErrorMessage;
import greencity.converters.UserArgumentResolver;
import greencity.dto.AddNewTariffDto;
import greencity.dto.DetailsOfDeactivateTariffsDto;
import greencity.dto.bag.EditAmountOfBagDto;
import greencity.dto.courier.AddingReceivingStationDto;
import greencity.dto.courier.CourierTranslationDto;
import greencity.dto.courier.CourierUpdateDto;
import greencity.dto.courier.CreateCourierDto;
import greencity.dto.courier.ReceivingStationDto;
import greencity.dto.location.LocationCreateDto;
import greencity.dto.order.EditPriceOfOrder;
import greencity.dto.service.AddServiceDto;
import greencity.dto.service.CreateServiceDto;
import greencity.dto.service.ServiceDto;
import greencity.dto.tariff.EditTariffServiceDto;
import greencity.dto.tariff.GetTariffsInfoDto;
import greencity.dto.tariff.TariffsInfoDto;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.courier.CourierAlreadyExists;
import greencity.exceptions.service.ServiceAlreadyExistsException;
import greencity.exceptions.tariff.TariffAlreadyExistsException;
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
import java.util.Optional;
import java.util.UUID;

import static greencity.ModelUtils.getReceivingStationDto;
import static greencity.ModelUtils.getUuid;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
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
        String ServiceResponceDtoJSON = new ObjectMapper().writeValueAsString(dto);
        String uuid = UUID.randomUUID().toString();

        when(userRemoteClient.findUuidByEmail(principal.getName())).thenReturn(uuid);
        when(superAdminService.addTariffService(dto, uuid)).thenReturn(dto);

        mockMvc.perform(post(ubsLink + "/createTariffService")
            .principal(principal)
            .param("uuid", uuid)
            .content(ServiceResponceDtoJSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andReturn();

        verify(userRemoteClient).findUuidByEmail(principal.getName());
        verify(superAdminService).addTariffService(any(AddServiceDto.class), anyString());
    }

    @Test
    void createTariffServiceNotFoundException() throws Exception {
        AddServiceDto dto = ModelUtils.getAddServiceDto();
        String ServiceResponceDtoJSON = new ObjectMapper().writeValueAsString(dto);
        String uuid = UUID.randomUUID().toString();

        when(userRemoteClient.findUuidByEmail(principal.getName())).thenReturn(uuid);
        when(superAdminService.addTariffService(any(AddServiceDto.class), anyString()))
            .thenThrow(new NotFoundException(ErrorMessage.LOCATION_DOESNT_FOUND));

        mockMvc.perform(post(ubsLink + "/createTariffService")
            .principal(principal)
            .param("uuid", uuid)
            .content(ServiceResponceDtoJSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
            .andExpect(result -> assertEquals(ErrorMessage.LOCATION_DOESNT_FOUND,
                result.getResolvedException().getMessage()));

        verify(userRemoteClient).findUuidByEmail(principal.getName());
        verify(superAdminService).addTariffService(any(AddServiceDto.class), anyString());
        verifyNoMoreInteractions(superAdminService, userRemoteClient);

    }

    @Test
    void getTariffService() throws Exception {
        mockMvc.perform(get(ubsLink + "/getTariffService"))
            .andExpect(status().isOk())
            .andReturn();
        verify(superAdminService).getTariffService();
        verifyNoMoreInteractions(superAdminService);
    }

    @Test
    void deleteTariffService() throws Exception {
        mockMvc.perform(delete(ubsLink + "/deleteTariffService/" + 1L))
            .andExpect(status().isOk());
        verify(superAdminService).deleteTariffService(1);
        verifyNoMoreInteractions(superAdminService);
    }

    @Test
    void deleteTariffServiceNotFoundException() throws Exception {
        doThrow(new NotFoundException(ErrorMessage.BAG_NOT_FOUND))
            .when(superAdminService).deleteTariffService(1);

        mockMvc.perform(delete(ubsLink + "/deleteTariffService/" + 1L))
            .andExpect(status().isNotFound())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
            .andExpect(result -> assertEquals(ErrorMessage.BAG_NOT_FOUND,
                result.getResolvedException().getMessage()));

        verify(superAdminService).deleteTariffService(1);
        verifyNoMoreInteractions(superAdminService);
    }

    @Test
    void editTariffService() throws Exception {
        EditTariffServiceDto dto = ModelUtils.getEditTariffServiceDto();
        String ServiceResponseDtoJSON = new ObjectMapper().writeValueAsString(dto);
        String uuid = UUID.randomUUID().toString();

        when(userRemoteClient.findUuidByEmail(principal.getName())).thenReturn(uuid);
        when(superAdminService.editTariffService(dto, 1, uuid)).thenReturn(ModelUtils.getTariffServiceDto());

        mockMvc.perform(put(ubsLink + "/editTariffService/" + 1L)
            .content(ServiceResponseDtoJSON)
            .contentType(MediaType.APPLICATION_JSON)
            .principal(principal)
            .param("uuid", uuid))
            .andExpect(status().isOk())
            .andReturn();

        verify(userRemoteClient).findUuidByEmail(principal.getName());
        verify(superAdminService).editTariffService(any(EditTariffServiceDto.class), anyInt(), anyString());
        verifyNoMoreInteractions(superAdminService, userRemoteClient);
    }

    @Test
    void editTariffServiceNotFoundException() throws Exception {
        EditTariffServiceDto dto = ModelUtils.getEditTariffServiceDto();
        String ServiceResponseDtoJSON = new ObjectMapper().writeValueAsString(dto);
        String uuid = UUID.randomUUID().toString();

        when(userRemoteClient.findUuidByEmail(principal.getName())).thenReturn(uuid);
        when(superAdminService.editTariffService(dto, 1, uuid))
            .thenThrow(new NotFoundException(ErrorMessage.BAG_NOT_FOUND));

        mockMvc.perform(put(ubsLink + "/editTariffService/" + 1L)
            .content(ServiceResponseDtoJSON)
            .contentType(MediaType.APPLICATION_JSON)
            .principal(principal)
            .param("uuid", uuid))
            .andExpect(status().isNotFound())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
            .andExpect(result -> assertEquals(ErrorMessage.BAG_NOT_FOUND,
                result.getResolvedException().getMessage()));

        verify(userRemoteClient).findUuidByEmail(principal.getName());
        verify(superAdminService).editTariffService(any(EditTariffServiceDto.class), anyInt(), anyString());
        verifyNoMoreInteractions(superAdminService, userRemoteClient);
    }

    @Test
    void createService() throws Exception {
        CreateServiceDto dto = ModelUtils.createServiceDto();
        String requestedJson = new ObjectMapper().writeValueAsString(dto);
        String uuid = UUID.randomUUID().toString();

        when(userRemoteClient.findUuidByEmail(principal.getName())).thenReturn(uuid);

        mockMvc.perform(post(ubsLink + "/createService")
            .principal(principal)
            .param("uuid", uuid)
            .content(requestedJson)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());

        verify(userRemoteClient).findUuidByEmail(principal.getName());
        verifyNoMoreInteractions(userRemoteClient);
    }

    @Test
    void createServiceIfServiceAlreadyExistsException() throws Exception {
        CreateServiceDto dto = ModelUtils.createServiceDto();
        String requestedJson = new ObjectMapper().writeValueAsString(dto);
        String uuid = UUID.randomUUID().toString();

        Mockito.when(userRemoteClient.findUuidByEmail(principal.getName())).thenReturn(uuid);
        Mockito.when(superAdminService.addService(any(CreateServiceDto.class), anyString()))
            .thenThrow(new ServiceAlreadyExistsException(ErrorMessage.SERVICE_ALREADY_EXISTS));

        mockMvc.perform(post(ubsLink + "/createService")
            .principal(principal)
            .param("uuid", uuid)
            .content(requestedJson)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof ServiceAlreadyExistsException))
            .andExpect(result -> assertEquals(ErrorMessage.SERVICE_ALREADY_EXISTS,
                result.getResolvedException().getMessage()));

        verify(userRemoteClient).findUuidByEmail(principal.getName());
        verify(superAdminService).addService(any(CreateServiceDto.class), anyString());
        verifyNoMoreInteractions(superAdminService, userRemoteClient);
    }

    @Test
    void createServiceIfEmployeeNotFoundException() throws Exception {
        CreateServiceDto dto = ModelUtils.createServiceDto();
        String requestedJson = new ObjectMapper().writeValueAsString(dto);
        String uuid = UUID.randomUUID().toString();

        when(userRemoteClient.findUuidByEmail(principal.getName())).thenReturn(uuid);
        when(superAdminService.addService(any(CreateServiceDto.class), anyString()))
            .thenThrow(new NotFoundException(ErrorMessage.EMPLOYEE_WITH_UUID_NOT_FOUND));

        mockMvc.perform(post(ubsLink + "/createService")
            .principal(principal)
            .param("uuid", uuid)
            .content(requestedJson)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
            .andExpect(result -> assertEquals(ErrorMessage.EMPLOYEE_WITH_UUID_NOT_FOUND,
                result.getResolvedException().getMessage()));

        verify(userRemoteClient).findUuidByEmail(principal.getName());
        verify(superAdminService).addService(any(CreateServiceDto.class), anyString());
        verifyNoMoreInteractions(superAdminService, userRemoteClient);
    }

    @Test
    void createServiceIfTariffNotFoundException() throws Exception {
        CreateServiceDto dto = ModelUtils.createServiceDto();
        String requestedJson = new ObjectMapper().writeValueAsString(dto);
        String uuid = UUID.randomUUID().toString();

        when(userRemoteClient.findUuidByEmail(principal.getName())).thenReturn(uuid);
        when(superAdminService.addService(any(CreateServiceDto.class), anyString()))
            .thenThrow(new NotFoundException(ErrorMessage.TARIFF_NOT_FOUND));

        mockMvc.perform(post(ubsLink + "/createService")
            .principal(principal)
            .param("uuid", uuid)
            .content(requestedJson)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
            .andExpect(result -> assertEquals(ErrorMessage.TARIFF_NOT_FOUND,
                result.getResolvedException().getMessage()));

        verify(userRemoteClient).findUuidByEmail(principal.getName());
        verify(superAdminService).addService(any(CreateServiceDto.class), anyString());
        verifyNoMoreInteractions(superAdminService, userRemoteClient);
    }

    @Test
    void getService() throws Exception {
        ServiceDto dto = ModelUtils.getServiceDto();

        when(superAdminService.getService(1L)).thenReturn(dto);

        mockMvc.perform(get(ubsLink + "/1/getService")
            .principal(principal)
            .param("tariffId", "1L"))
            .andExpect(status().isOk())
            .andReturn();

        verify(superAdminService).getService(1L);
        verifyNoMoreInteractions(superAdminService);
    }

    @Test
    void getServiceIfServiceNotExists() throws Exception {
        when(superAdminService.getService(1L)).thenReturn(null);

        mockMvc.perform(get(ubsLink + "/1/getService")
            .principal(principal)
            .param("tariffId", "1L"))
            .andExpect(status().isOk())
            .andReturn();

        Mockito.verify(superAdminService).getService(1L);
        Mockito.verifyNoMoreInteractions(superAdminService);
    }

    @Test
    void getServiceIfTariffNotFoundException() throws Exception {
        long tariffId = 1L;
        Mockito.when(superAdminService.getService(tariffId))
            .thenThrow(new NotFoundException(ErrorMessage.TARIFF_NOT_FOUND + tariffId));

        mockMvc.perform(get(ubsLink + "/" + tariffId + "/getService")
            .principal(principal)
            .param("tariffId", "1L"))
            .andExpect(status().isNotFound())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
            .andExpect(result -> assertEquals(ErrorMessage.TARIFF_NOT_FOUND + tariffId,
                result.getResolvedException().getMessage()));

        Mockito.verify(superAdminService).getService(tariffId);
        Mockito.verifyNoMoreInteractions(superAdminService);
    }

    @Test
    void editService() throws Exception {
        ServiceDto dto = ModelUtils.getServiceDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String requestedJson = objectMapper.writeValueAsString(dto);
        String uuid = UUID.randomUUID().toString();

        when(userRemoteClient.findUuidByEmail(principal.getName())).thenReturn(uuid);

        mockMvc.perform(put(ubsLink + "/editService")
            .principal(principal)
            .param("uuid", uuid)
            .content(requestedJson)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(userRemoteClient).findUuidByEmail(principal.getName());
        verifyNoMoreInteractions(userRemoteClient);
    }

    @Test
    void editServiceIfServiceNotFoundException() throws Exception {
        ServiceDto dto = ModelUtils.getServiceDto();
        String requestedJson = new ObjectMapper().writeValueAsString(dto);
        String uuid = UUID.randomUUID().toString();
        long id = dto.getId();

        when(userRemoteClient.findUuidByEmail(principal.getName())).thenReturn(uuid);
        when(superAdminService.editService(any(ServiceDto.class), anyString()))
            .thenThrow(new NotFoundException(ErrorMessage.SERVICE_IS_NOT_FOUND_BY_ID + id));

        mockMvc.perform(put(ubsLink + "/editService")
            .principal(principal)
            .param("uuid", uuid)
            .content(requestedJson)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
            .andExpect(result -> assertEquals(ErrorMessage.SERVICE_IS_NOT_FOUND_BY_ID + id,
                result.getResolvedException().getMessage()));

        verify(userRemoteClient).findUuidByEmail(principal.getName());
        verify(superAdminService).editService(any(ServiceDto.class), anyString());
        verifyNoMoreInteractions(superAdminService, userRemoteClient);
    }

    @Test
    void editServiceIfEmployeeNotFoundException() throws Exception {
        ServiceDto dto = ModelUtils.getServiceDto();
        String requestedJson = new ObjectMapper().writeValueAsString(dto);
        String uuid = UUID.randomUUID().toString();

        when(userRemoteClient.findUuidByEmail(principal.getName())).thenReturn(uuid);
        when(superAdminService.editService(any(ServiceDto.class), anyString()))
            .thenThrow(new NotFoundException(ErrorMessage.EMPLOYEE_WITH_UUID_NOT_FOUND));

        mockMvc.perform(put(ubsLink + "/editService")
            .principal(principal)
            .param("uuid", uuid)
            .content(requestedJson)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
            .andExpect(result -> assertEquals(ErrorMessage.EMPLOYEE_WITH_UUID_NOT_FOUND,
                result.getResolvedException().getMessage()));

        verify(userRemoteClient).findUuidByEmail(principal.getName());
        verify(superAdminService).editService(any(ServiceDto.class), anyString());
        verifyNoMoreInteractions(superAdminService, userRemoteClient);
    }

    @Test
    void deleteService() throws Exception {
        mockMvc.perform(delete(ubsLink + "/deleteService/" + 1L)
            .principal(principal)
            .param("id", "1L"))
            .andExpect(status().isOk());

        verify(superAdminService).deleteService(1L);
        verifyNoMoreInteractions(superAdminService);
    }

    @Test
    void deleteServiceIfServiceNotFoundException() throws Exception {
        long id = 1L;
        doThrow(new NotFoundException(ErrorMessage.SERVICE_IS_NOT_FOUND_BY_ID + id))
            .when(superAdminService).deleteService(id);

        mockMvc.perform(delete(ubsLink + "/deleteService/" + 1L)
            .principal(principal)
            .param("id", "1L"))
            .andExpect(status().isNotFound())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
            .andExpect(result -> assertEquals(ErrorMessage.SERVICE_IS_NOT_FOUND_BY_ID + id,
                result.getResolvedException().getMessage()));

        verify(superAdminService).deleteService(id);
        verifyNoMoreInteractions(superAdminService);
    }

    @Test
    void getLocations() throws Exception {
        mockMvc.perform(get(ubsLink + "/getLocations")).andExpect(status().isOk());

        verify(superAdminService).getAllLocation();
        verifyNoMoreInteractions(superAdminService);
    }

    @Test
    void getActiveLocations() throws Exception {
        mockMvc.perform(get((ubsLink + "/getActiveLocations"))).andExpect(status().isOk());

        verify(superAdminService).getActiveLocations();
        verifyNoMoreInteractions(superAdminService);
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
    void createCourierIfCourierAlreadyExistsException() throws Exception {
        CreateCourierDto dto = ModelUtils.getCreateCourierDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String requestedJson = objectMapper.writeValueAsString(dto);
        String uuid = UUID.randomUUID().toString();

        Mockito.when(userRemoteClient.findUuidByEmail(principal.getName())).thenReturn(uuid);
        Mockito.when(superAdminService.createCourier(dto, uuid))
            .thenThrow(new CourierAlreadyExists(ErrorMessage.COURIER_ALREADY_EXISTS));

        mockMvc.perform(post(ubsLink + "/createCourier")
            .principal(principal)
            .content(requestedJson)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof CourierAlreadyExists))
            .andExpect(result -> assertEquals(ErrorMessage.COURIER_ALREADY_EXISTS,
                result.getResolvedException().getMessage()));

        Mockito.verify(userRemoteClient).findUuidByEmail(principal.getName());
        Mockito.verify(superAdminService).createCourier(dto, uuid);
        Mockito.verifyNoMoreInteractions(superAdminService, userRemoteClient);
    }

    @Test
    void addNewTariffIfTariffAlreadyExistsException() throws Exception {
        AddNewTariffDto dto = ModelUtils.getAddNewTariffDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String requestedJson = objectMapper.writeValueAsString(dto);
        String uuid = UUID.randomUUID().toString();

        Mockito.when(userRemoteClient.findUuidByEmail(principal.getName())).thenReturn(uuid);
        Mockito.when(superAdminService.addNewTariff(dto, uuid))
            .thenThrow(new TariffAlreadyExistsException(ErrorMessage.TARIFF_IS_ALREADY_EXISTS));

        mockMvc.perform(post(ubsLink + "/add-new-tariff")
            .principal(principal)
            .content(requestedJson)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof TariffAlreadyExistsException))
            .andExpect(result -> assertEquals(ErrorMessage.TARIFF_IS_ALREADY_EXISTS,
                result.getResolvedException().getMessage()));

        Mockito.verify(userRemoteClient).findUuidByEmail(principal.getName());
        Mockito.verify(superAdminService).addNewTariff(dto, uuid);
        Mockito.verifyNoMoreInteractions(superAdminService, userRemoteClient);
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
        List<CourierTranslationDto> dtoList = List.of(CourierTranslationDto.builder()
            .nameUk("УБС")
            .nameEn("UBS")
            .build());
        CourierUpdateDto dto = CourierUpdateDto.builder()
            .courierId(1L)
            .nameEn("Test")
            .nameEn("Тест")
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
    void checkIfTariffExistsTest() throws Exception {
        AddNewTariffDto dto = ModelUtils.getAddNewTariffDto();
        ObjectMapper objectMapper = new ObjectMapper();
        mockMvc.perform(post(ubsLink + "/check-if-tariff-exists")
            .content(objectMapper.writeValueAsString(dto))
            .contentType(MediaType.APPLICATION_JSON)
            .principal(principal))
            .andExpect(status().isOk());
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
            .andExpect(status().isOk());
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
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void deactivateTariffTest() {
        mockMvc.perform(put(ubsLink + "/deactivateTariff/{tariffId}", 1L)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void setLimitDescriptionTest() throws Exception {
        TariffsInfoDto dto = ModelUtils.getLimitDescriptionDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String responseJSON = objectMapper.writeValueAsString(dto);
        mockMvc.perform(patch(ubsLink + "/setLimitDescription/{tariffId}", 1L)
            .principal(principal)
            .content(responseJSON)
            .contentType(MediaType.APPLICATION_JSON))
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

    @Test
    void deactivateTariffForChosenParamBadRequest() throws Exception {
        mockMvc.perform(post(ubsLink + "/deactivate"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void deactivateTariffFotChosenParam() throws Exception {
        Optional<List<Long>> regionsId = Optional.of(List.of(1L));
        Optional<List<Long>> citiesId = Optional.empty();
        Optional<List<Long>> stationsId = Optional.empty();
        Optional<Long> courierId = Optional.empty();

        DetailsOfDeactivateTariffsDto details = DetailsOfDeactivateTariffsDto.builder()
            .regionsId(regionsId)
            .citiesId(citiesId)
            .stationsId(stationsId)
            .courierId(courierId)
            .build();

        mockMvc.perform(post(ubsLink + "/deactivate/")
            .param("regionsId", "1")).andExpect(status().isOk());
        verify(superAdminService).deactivateTariffForChosenParam(details);
    }

    @Test
    void deactivateCourier() throws Exception {
        mockMvc.perform(patch(ubsLink + "/deactivateCourier/{id}", 1L)).andExpect(status().isOk());
        verify(superAdminService).deactivateCourier(1L);
    }
}
