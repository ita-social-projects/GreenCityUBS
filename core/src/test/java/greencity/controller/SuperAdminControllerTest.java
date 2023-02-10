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
import greencity.dto.service.ServiceDto;
import greencity.dto.service.GetServiceDto;
import greencity.dto.service.GetTariffServiceDto;
import greencity.dto.service.TariffServiceDto;
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
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static greencity.ModelUtils.getReceivingStationDto;
import static greencity.ModelUtils.getUuid;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
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
        TariffServiceDto dto = ModelUtils.getTariffServiceDto();
        GetTariffServiceDto responseDto = ModelUtils.getGetTariffServiceDto();
        String requestDtoJSON = new ObjectMapper().writeValueAsString(dto);
        String uuid = UUID.randomUUID().toString();

        when(userRemoteClient.findUuidByEmail(principal.getName())).thenReturn(uuid);
        when(superAdminService.addTariffService(1L, dto, uuid)).thenReturn(responseDto);

        mockMvc.perform(post(ubsLink + "/{tariffId}/createTariffService", 1L)
            .principal(principal)
            .param("uuid", uuid)
            .param("tariffId", "1")
            .content(requestDtoJSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andReturn();

        verify(userRemoteClient).findUuidByEmail(principal.getName());
        verify(superAdminService).addTariffService(anyLong(), any(TariffServiceDto.class), anyString());
    }

    @Test
    void createTariffServiceNotFoundException() throws Exception {
        TariffServiceDto dto = ModelUtils.getTariffServiceDto();
        String requestDtoJSON = new ObjectMapper().writeValueAsString(dto);
        String uuid = UUID.randomUUID().toString();

        when(userRemoteClient.findUuidByEmail(principal.getName())).thenReturn(uuid);
        when(superAdminService.addTariffService(anyLong(), any(TariffServiceDto.class), anyString()))
            .thenThrow(new NotFoundException(ErrorMessage.TARIFF_NOT_FOUND));

        mockMvc.perform(post(ubsLink + "/{tariffId}/createTariffService", 1L)
            .principal(principal)
            .param("uuid", uuid)
            .param("tariffId", "1")
            .content(requestDtoJSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
            .andExpect(result -> assertEquals(ErrorMessage.TARIFF_NOT_FOUND,
                Objects.requireNonNull(result.getResolvedException()).getMessage()));

        verify(userRemoteClient).findUuidByEmail(principal.getName());
        verify(superAdminService).addTariffService(anyLong(), any(TariffServiceDto.class), anyString());
        verifyNoMoreInteractions(superAdminService, userRemoteClient);
    }

    @Test
    void getTariffService() throws Exception {
        mockMvc.perform(get(ubsLink + "/{tariffId}/getTariffService", 1L)
            .principal(principal)
            .param("tariffId", "1L"))
            .andExpect(status().isOk())
            .andReturn();

        verify(superAdminService).getTariffService(1L);
        verifyNoMoreInteractions(superAdminService);
    }

    @Test
    void getTariffServiceIfTariffNotFoundException() throws Exception {
        when(superAdminService.getTariffService(1L))
            .thenThrow(new NotFoundException(ErrorMessage.TARIFF_NOT_FOUND));

        mockMvc.perform(get(ubsLink + "/{tariffId}/getTariffService", 1L)
            .principal(principal)
            .param("tariffId", "1L"))
            .andExpect(status().isNotFound())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
            .andExpect(result -> assertEquals(ErrorMessage.TARIFF_NOT_FOUND,
                Objects.requireNonNull(result.getResolvedException()).getMessage()));

        verify(superAdminService).getTariffService(1L);
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
        TariffServiceDto dto = ModelUtils.getTariffServiceDto();
        GetTariffServiceDto responseDto = ModelUtils.getGetTariffServiceDto();
        String requestDtoJSON = new ObjectMapper().writeValueAsString(dto);
        String uuid = UUID.randomUUID().toString();

        when(userRemoteClient.findUuidByEmail(principal.getName())).thenReturn(uuid);
        when(superAdminService.editTariffService(dto, 1, uuid)).thenReturn(responseDto);

        mockMvc.perform(put(ubsLink + "/editTariffService/" + 1L)
            .content(requestDtoJSON)
            .contentType(MediaType.APPLICATION_JSON)
            .principal(principal)
            .param("uuid", uuid))
            .andExpect(status().isOk())
            .andReturn();

        verify(userRemoteClient).findUuidByEmail(principal.getName());
        verify(superAdminService).editTariffService(any(TariffServiceDto.class), anyInt(), anyString());
        verifyNoMoreInteractions(superAdminService, userRemoteClient);
    }

    @Test
    void editTariffServiceNotFoundException() throws Exception {
        TariffServiceDto dto = ModelUtils.getTariffServiceDto();
        String requestDtoJSON = new ObjectMapper().writeValueAsString(dto);
        String uuid = UUID.randomUUID().toString();

        when(userRemoteClient.findUuidByEmail(principal.getName())).thenReturn(uuid);
        when(superAdminService.editTariffService(dto, 1, uuid))
            .thenThrow(new NotFoundException(ErrorMessage.BAG_NOT_FOUND));

        mockMvc.perform(put(ubsLink + "/editTariffService/" + 1L)
            .content(requestDtoJSON)
            .contentType(MediaType.APPLICATION_JSON)
            .principal(principal)
            .param("uuid", uuid))
            .andExpect(status().isNotFound())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
            .andExpect(result -> assertEquals(ErrorMessage.BAG_NOT_FOUND,
                Objects.requireNonNull(result.getResolvedException()).getMessage()));

        verify(userRemoteClient).findUuidByEmail(principal.getName());
        verify(superAdminService).editTariffService(any(TariffServiceDto.class), anyInt(), anyString());
        verifyNoMoreInteractions(superAdminService, userRemoteClient);
    }

    @Test
    void createService() throws Exception {
        ServiceDto dto = ModelUtils.getServiceDto();
        String requestedJson = new ObjectMapper().writeValueAsString(dto);
        String uuid = UUID.randomUUID().toString();

        when(userRemoteClient.findUuidByEmail(principal.getName())).thenReturn(uuid);

        mockMvc.perform(post(ubsLink + "/{tariffId}/createService", 1L)
            .principal(principal)
            .param("uuid", uuid)
            .param("tariffId", "1")
            .content(requestedJson)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());

        verify(userRemoteClient).findUuidByEmail(principal.getName());
        verifyNoMoreInteractions(userRemoteClient);
    }

    @Test
    void createServiceIfServiceAlreadyExistsException() throws Exception {
        ServiceDto dto = ModelUtils.getServiceDto();
        String requestedJson = new ObjectMapper().writeValueAsString(dto);
        String uuid = UUID.randomUUID().toString();

        when(userRemoteClient.findUuidByEmail(principal.getName())).thenReturn(uuid);
        when(superAdminService.addService(anyLong(), any(ServiceDto.class), anyString()))
            .thenThrow(new ServiceAlreadyExistsException(ErrorMessage.SERVICE_ALREADY_EXISTS));

        mockMvc.perform(post(ubsLink + "/{tariffId}/createService", 1L)
            .principal(principal)
            .param("uuid", uuid)
            .param("tariffId", "1")
            .content(requestedJson)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof ServiceAlreadyExistsException))
            .andExpect(result -> assertEquals(ErrorMessage.SERVICE_ALREADY_EXISTS,
                Objects.requireNonNull(result.getResolvedException()).getMessage()));

        verify(userRemoteClient).findUuidByEmail(principal.getName());
        verify(superAdminService).addService(anyLong(), any(ServiceDto.class), anyString());
        verifyNoMoreInteractions(superAdminService, userRemoteClient);
    }

    @Test
    void createServiceIfEmployeeNotFoundException() throws Exception {
        ServiceDto dto = ModelUtils.getServiceDto();
        String requestedJson = new ObjectMapper().writeValueAsString(dto);
        String uuid = UUID.randomUUID().toString();

        when(userRemoteClient.findUuidByEmail(principal.getName())).thenReturn(uuid);
        when(superAdminService.addService(anyLong(), any(ServiceDto.class), anyString()))
            .thenThrow(new NotFoundException(ErrorMessage.EMPLOYEE_WITH_UUID_NOT_FOUND));

        mockMvc.perform(post(ubsLink + "/{tariffId}/createService", 1L)
            .principal(principal)
            .param("uuid", uuid)
            .param("tariffId", "1")
            .content(requestedJson)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
            .andExpect(result -> assertEquals(ErrorMessage.EMPLOYEE_WITH_UUID_NOT_FOUND,
                Objects.requireNonNull(result.getResolvedException()).getMessage()));

        verify(userRemoteClient).findUuidByEmail(principal.getName());
        verify(superAdminService).addService(anyLong(), any(ServiceDto.class), anyString());
        verifyNoMoreInteractions(superAdminService, userRemoteClient);
    }

    @Test
    void createServiceIfTariffNotFoundException() throws Exception {
        ServiceDto dto = ModelUtils.getServiceDto();
        String requestedJson = new ObjectMapper().writeValueAsString(dto);
        String uuid = UUID.randomUUID().toString();

        when(userRemoteClient.findUuidByEmail(principal.getName())).thenReturn(uuid);
        when(superAdminService.addService(anyLong(), any(ServiceDto.class), anyString()))
            .thenThrow(new NotFoundException(ErrorMessage.TARIFF_NOT_FOUND));

        mockMvc.perform(post(ubsLink + "/{tariffId}/createService", 1L)
            .principal(principal)
            .param("uuid", uuid)
            .param("tariffId", "1")
            .content(requestedJson)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
            .andExpect(result -> assertEquals(ErrorMessage.TARIFF_NOT_FOUND,
                Objects.requireNonNull(result.getResolvedException()).getMessage()));

        verify(userRemoteClient).findUuidByEmail(principal.getName());
        verify(superAdminService).addService(anyLong(), any(ServiceDto.class), anyString());
        verifyNoMoreInteractions(superAdminService, userRemoteClient);
    }

    @Test
    void getServiceDto() throws Exception {
        GetServiceDto dto = ModelUtils.getGetServiceDto();

        when(superAdminService.getService(1L)).thenReturn(dto);

        mockMvc.perform(get(ubsLink + "/{tariffId}/getService", 1L)
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

        mockMvc.perform(get(ubsLink + "/{tariffId}/getService", 1L)
            .principal(principal)
            .param("tariffId", "1L"))
            .andExpect(status().isOk())
            .andReturn();

        verify(superAdminService).getService(1L);
        verifyNoMoreInteractions(superAdminService);
    }

    @Test
    void getServiceIfTariffNotFoundException() throws Exception {
        long tariffId = 1L;
        when(superAdminService.getService(tariffId))
            .thenThrow(new NotFoundException(ErrorMessage.TARIFF_NOT_FOUND + tariffId));

        mockMvc.perform(get(ubsLink + "/{tariffId}/getService", tariffId)
            .principal(principal)
            .param("tariffId", "1L"))
            .andExpect(status().isNotFound())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
            .andExpect(result -> assertEquals(ErrorMessage.TARIFF_NOT_FOUND + 1L,
                Objects.requireNonNull(result.getResolvedException()).getMessage()));

        verify(superAdminService).getService(tariffId);
        verifyNoMoreInteractions(superAdminService);
    }

    @Test
    void editService() throws Exception {
        GetServiceDto dto = ModelUtils.getGetServiceDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String requestedJson = objectMapper.writeValueAsString(dto);
        String uuid = UUID.randomUUID().toString();

        when(userRemoteClient.findUuidByEmail(principal.getName())).thenReturn(uuid);

        mockMvc.perform(put(ubsLink + "/editService/{id}", 1L)
            .principal(principal)
            .param("uuid", uuid)
            .param("id", "1")
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
        long id = 1L;

        when(userRemoteClient.findUuidByEmail(principal.getName())).thenReturn(uuid);
        when(superAdminService.editService(anyLong(), any(ServiceDto.class), anyString()))
            .thenThrow(new NotFoundException(ErrorMessage.SERVICE_IS_NOT_FOUND_BY_ID + id));

        mockMvc.perform(put(ubsLink + "/editService/{id}", id)
            .principal(principal)
            .param("uuid", uuid)
            .param("id", "1")
            .content(requestedJson)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
            .andExpect(result -> assertEquals(ErrorMessage.SERVICE_IS_NOT_FOUND_BY_ID + id,
                Objects.requireNonNull(result.getResolvedException()).getMessage()));

        verify(userRemoteClient).findUuidByEmail(principal.getName());
        verify(superAdminService).editService(anyLong(), any(ServiceDto.class), anyString());
        verifyNoMoreInteractions(superAdminService, userRemoteClient);
    }

    @Test
    void editServiceIfEmployeeNotFoundException() throws Exception {
        ServiceDto dto = ModelUtils.getServiceDto();
        String requestedJson = new ObjectMapper().writeValueAsString(dto);
        String uuid = UUID.randomUUID().toString();

        when(userRemoteClient.findUuidByEmail(principal.getName())).thenReturn(uuid);
        when(superAdminService.editService(anyLong(), any(ServiceDto.class), anyString()))
            .thenThrow(new NotFoundException(ErrorMessage.EMPLOYEE_WITH_UUID_NOT_FOUND));

        mockMvc.perform(put(ubsLink + "/editService/{id}", 1L)
            .principal(principal)
            .param("uuid", uuid)
            .param("id", "1")
            .content(requestedJson)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
            .andExpect(result -> assertEquals(ErrorMessage.EMPLOYEE_WITH_UUID_NOT_FOUND,
                Objects.requireNonNull(result.getResolvedException()).getMessage()));

        verify(userRemoteClient).findUuidByEmail(principal.getName());
        verify(superAdminService).editService(anyLong(), any(ServiceDto.class), anyString());
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
                Objects.requireNonNull(result.getResolvedException()).getMessage()));

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
        var dto = EditPriceOfOrder.builder().max(10000L).min(1000L).build();
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
        GetTariffServiceDto dto = ModelUtils.getGetTariffServiceDto();

        when(superAdminService.includeBag(1)).thenReturn(dto);

        mockMvc.perform(patch(ubsLink + "/includeBag/{id}", 1L)
            .principal(principal)
            .param("id", "1"))
            .andExpect(status().isOk())
            .andReturn();

        verify(superAdminService).includeBag(1);
    }

    @Test
    void includeBagThrowNotFoundException() throws Exception {
        when(superAdminService.includeBag(1))
            .thenThrow(new NotFoundException(ErrorMessage.BAG_NOT_FOUND));

        mockMvc.perform(patch(ubsLink + "/includeBag/{id}", 1L)
            .principal(principal)
            .param("id", "1"))
            .andExpect(status().isNotFound())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
            .andExpect(result -> assertEquals(ErrorMessage.BAG_NOT_FOUND,
                Objects.requireNonNull(result.getResolvedException()).getMessage()));

        verify(superAdminService).includeBag(1);
    }

    @Test
    void includeBagThrowBadRequestException() throws Exception {
        when(superAdminService.includeBag(1))
            .thenThrow(new BadRequestException(ErrorMessage.BAG_WITH_THIS_STATUS_ALREADY_SET));

        mockMvc.perform(patch(ubsLink + "/includeBag/{id}", 1L)
            .principal(principal)
            .param("id", "1"))
            .andExpect(status().isBadRequest())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof BadRequestException))
            .andExpect(result -> assertEquals(ErrorMessage.BAG_WITH_THIS_STATUS_ALREADY_SET,
                Objects.requireNonNull(result.getResolvedException()).getMessage()));

        verify(superAdminService).includeBag(1);
    }

    @Test
    void excludeBag() throws Exception {
        GetTariffServiceDto dto = ModelUtils.getGetTariffServiceDto();

        when(superAdminService.excludeBag(1)).thenReturn(dto);

        mockMvc.perform(patch(ubsLink + "/excludeBag/{id}", 1L)
            .principal(principal)
            .param("id", "1"))
            .andExpect(status().isOk())
            .andReturn();

        verify(superAdminService).excludeBag(1);
    }

    @Test
    void excludeBagThrowNotFoundException() throws Exception {
        when(superAdminService.excludeBag(1))
            .thenThrow(new NotFoundException(ErrorMessage.BAG_NOT_FOUND));

        mockMvc.perform(patch(ubsLink + "/excludeBag/{id}", 1L)
            .principal(principal)
            .param("id", "1"))
            .andExpect(status().isNotFound())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof NotFoundException))
            .andExpect(result -> assertEquals(ErrorMessage.BAG_NOT_FOUND,
                Objects.requireNonNull(result.getResolvedException()).getMessage()));

        verify(superAdminService).excludeBag(1);
    }

    @Test
    void excludeBagThrowBadRequestException() throws Exception {
        when(superAdminService.excludeBag(1))
            .thenThrow(new BadRequestException(ErrorMessage.BAG_WITH_THIS_STATUS_ALREADY_SET));

        mockMvc.perform(patch(ubsLink + "/excludeBag/{id}", 1L)
            .principal(principal)
            .param("id", "1"))
            .andExpect(status().isBadRequest())
            .andExpect(result -> assertTrue(result.getResolvedException() instanceof BadRequestException))
            .andExpect(result -> assertEquals(ErrorMessage.BAG_WITH_THIS_STATUS_ALREADY_SET,
                Objects.requireNonNull(result.getResolvedException()).getMessage()));

        verify(superAdminService).excludeBag(1);
    }

    @Test
    void deactivateTariffForChosenParamBadRequest() throws Exception {
        mockMvc.perform(post(ubsLink + "/deactivate"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void deactivateTariffFotChosenParam() throws Exception {
        Optional<List<Long>> regionsIds = Optional.of(List.of(1L));
        Optional<List<Long>> citiesIds = Optional.empty();
        Optional<List<Long>> stationsIds = Optional.empty();
        Optional<Long> courierId = Optional.empty();
        DetailsOfDeactivateTariffsDto details = DetailsOfDeactivateTariffsDto.builder()
            .regionsIds(regionsIds)
            .citiesIds(citiesIds)
            .stationsIds(stationsIds)
            .courierId(courierId)
            .build();

        mockMvc.perform(post(ubsLink + "/deactivate/")
            .param("regionsIds", "1")).andExpect(status().isOk());
        verify(superAdminService).deactivateTariffForChosenParam(details);
    }

    @Test
    void deactivateCourier() throws Exception {
        mockMvc.perform(patch(ubsLink + "/deactivateCourier/{id}", 1L)).andExpect(status().isOk());
        verify(superAdminService).deactivateCourier(1L);
    }
}
