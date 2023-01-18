package greencity.service.ubs;

import greencity.ModelUtils;
import greencity.constant.ErrorMessage;
import greencity.dto.AddNewTariffDto;
import greencity.dto.DetailsOfDeactivateTariffsDto;
import greencity.dto.courier.*;
import greencity.dto.location.EditLocationDto;
import greencity.dto.location.LocationCreateDto;
import greencity.dto.location.LocationInfoDto;
import greencity.dto.service.AddServiceDto;
import greencity.dto.service.CreateServiceDto;
import greencity.dto.service.EditServiceDto;
import greencity.dto.service.GetServiceDto;
import greencity.dto.tariff.ChangeTariffLocationStatusDto;
import greencity.dto.tariff.EditTariffServiceDto;
import greencity.dto.tariff.GetTariffsInfoDto;
import greencity.dto.tariff.SetTariffLimitsDto;
import greencity.entity.order.*;
import greencity.entity.user.Location;
import greencity.entity.user.Region;
import greencity.entity.user.User;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.ReceivingStation;
import greencity.enums.*;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.UnprocessableEntityException;
import greencity.exceptions.courier.CourierAlreadyExists;
import greencity.exceptions.service.ServiceAlreadyExistsException;
import greencity.exceptions.tariff.TariffAlreadyExistsException;
import greencity.filters.TariffsInfoFilterCriteria;
import greencity.filters.TariffsInfoSpecification;
import greencity.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static greencity.ModelUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuperAdminServiceImplTest {
    @InjectMocks
    private SuperAdminServiceImpl superAdminService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private BagRepository bagRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private ServiceRepository serviceRepository;
    @Mock
    private CourierRepository courierRepository;
    @Mock
    private RegionRepository regionRepository;
    @Mock
    private ReceivingStationRepository receivingStationRepository;
    @Mock
    private TariffsInfoRepository tariffsInfoRepository;
    @Mock
    private TariffLocationRepository tariffsLocationRepository;
    @Mock
    private DeactivateChosenEntityRepository deactivateTariffsForChosenParamRepository;

    @AfterEach
    void afterEach() {
        verifyNoMoreInteractions(
            userRepository,
            employeeRepository,
            bagRepository,
            locationRepository,
            modelMapper,
            serviceRepository,
            courierRepository,
            regionRepository,
            receivingStationRepository,
            tariffsInfoRepository,
            tariffsLocationRepository,
            deactivateTariffsForChosenParamRepository);
    }

    @Test
    void addTariffServiceTest() {
        User user = ModelUtils.getUser();
        Bag bag = ModelUtils.getTariffBag();
        AddServiceDto dto = ModelUtils.addServiceDto();

        when(userRepository.findByUuid("123233")).thenReturn(user);
        when(locationRepository.findById(1L)).thenReturn(Optional.of(ModelUtils.getLocation()));
        when(bagRepository.save(bag)).thenReturn(bag);

        superAdminService.addTariffService(dto, "123233");

        verify(userRepository).findByUuid("123233");
        verify(locationRepository).findById(1L);
        verify(bagRepository).save(bag);
        verify(modelMapper).map(bag, AddServiceDto.class);
    }

    @Test
    void getTariffServiceTest() {
        when(bagRepository.findAll()).thenReturn(ModelUtils.getBag5list());
        superAdminService.getTariffService();
        verify(bagRepository, times(1)).findAll();
    }

    @Test
    void deleteTariffServiceTest() {
        when(bagRepository.findById(1)).thenReturn(ModelUtils.getBag());

        superAdminService.deleteTariffService(1);

        verify(bagRepository).delete(ModelUtils.getBag().get());
    }

    @Test
    void getAllCouriersTest() {
        when(courierRepository.findAll()).thenReturn(List.of(getCourier()));
        when(modelMapper.map(getCourier(), CourierDto.class))
            .thenReturn(getCourierDto());

        assertEquals(getCourierDtoList(), superAdminService.getAllCouriers());

        verify(courierRepository).findAll();
        verify(modelMapper).map(getCourier(), CourierDto.class);
    }

    @Test
    void deleteTariffServiceThrowException() {
        assertThrows(NotFoundException.class, () -> superAdminService.deleteTariffService(1));
        verify(bagRepository).findById(anyInt());
    }

    @Test
    void editTariffService_Throw_Exception() {
        EditTariffServiceDto dto = new EditTariffServiceDto();
        assertThrows(NotFoundException.class, () -> superAdminService.editTariffService(dto, 1, "testUUid"));
        verify(userRepository).findByUuid(anyString());
        verify(bagRepository).findById(anyInt());
    }

    @Test
    void editTariffService() {
        String uuid = "testUUid";
        EditTariffServiceDto dto = ModelUtils.getEditTariffServiceDto();
        Bag bag = Bag.builder()
            .id(1)
            .name("Test")
            .nameEng("Name Test")
            .minAmountOfBags(MinAmountOfBag.INCLUDE)
            .location(Location.builder()
                .id(1L)
                .build())
            .build();
        User user = new User();
        user.setRecipientName("John");
        user.setRecipientSurname("Doe");

        when(userRepository.findByUuid(uuid)).thenReturn(user);
        when(bagRepository.findById(1)).thenReturn(Optional.of(bag));

        superAdminService.editTariffService(dto, 1, uuid);

        verify(bagRepository).findById(1);
        verify(bagRepository).save(bag);
    }

    @Test
    void deleteService() {
        Service service = ModelUtils.getService();

        when(serviceRepository.findById(service.getId())).thenReturn(Optional.of(service));

        superAdminService.deleteService(service.getId());

        verify(serviceRepository).findById(1L);
        verify(serviceRepository).delete(service);
    }

    @Test
    void deleteServiceThrowNotFoundException() {
        Service service = ModelUtils.getService();

        when(serviceRepository.findById(service.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> superAdminService.deleteService(service.getId()));

        verify(serviceRepository).findById(1L);
        verify(serviceRepository, never()).delete(service);
    }

    @Test
    void getService() {
        Service service = ModelUtils.getService();
        GetServiceDto getServiceDto = ModelUtils.getServiceDto();
        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();

        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffsInfo));
        when(serviceRepository.findServiceByTariffsInfoId(service.getId())).thenReturn(Optional.of(service));
        when(modelMapper.map(service, GetServiceDto.class)).thenReturn(getServiceDto);

        assertEquals(getServiceDto, superAdminService.getService(1L));

        verify(tariffsInfoRepository).findById(1L);
        verify(serviceRepository).findServiceByTariffsInfoId(1L);
        verify(modelMapper).map(service, GetServiceDto.class);
    }

    @Test
    void getServiceThrowServiceNotFoundException() {
        Service service = ModelUtils.getService();
        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();

        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffsInfo));
        when(serviceRepository.findServiceByTariffsInfoId(service.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> superAdminService.getService(1L));

        verify(tariffsInfoRepository).findById(1L);
        verify(serviceRepository).findServiceByTariffsInfoId(1L);
    }

    @Test
    void getServiceThrowTariffNotFoundException() {
        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> superAdminService.getService(1L));

        verify(tariffsInfoRepository).findById(1L);
    }

    @Test
    void editService() {
        Service service = ModelUtils.getEditedService();
        Employee employee = ModelUtils.getEmployee();
        EditServiceDto editServiceDto = ModelUtils.getEditServiceDto();
        GetServiceDto getServiceDto = ModelUtils.getServiceDto();
        String uuid = UUID.randomUUID().toString();

        when(serviceRepository.findServiceById(1L)).thenReturn(Optional.of(service));
        when(employeeRepository.findByUuid(uuid)).thenReturn(Optional.of(employee));
        when(serviceRepository.save(service)).thenReturn(service);
        when(modelMapper.map(service, GetServiceDto.class)).thenReturn(getServiceDto);

        assertEquals(getServiceDto, superAdminService.editService(1L, editServiceDto, uuid));

        verify(serviceRepository).findServiceById(1L);
        verify(employeeRepository).findByUuid(uuid);
        verify(serviceRepository).save(service);
        verify(modelMapper).map(service, GetServiceDto.class);
    }

    @Test
    void editServiceServiceNotFoundException() {
        EditServiceDto editServiceDto = ModelUtils.getEditServiceDto();
        Employee employee = ModelUtils.getEmployee();
        String uuid = UUID.randomUUID().toString();

        when(employeeRepository.findByUuid(uuid)).thenReturn(Optional.of(employee));
        when(serviceRepository.findServiceById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> superAdminService.editService(1L, editServiceDto, uuid));

        verify(employeeRepository).findByUuid(uuid);
        verify(serviceRepository).findServiceById(1L);
        verify(serviceRepository, never()).save(any(Service.class));
        verify(modelMapper, never()).map(any(Service.class), any(GetServiceDto.class));
    }

    @Test
    void editServiceEmployeeNotFoundException() {
        EditServiceDto editServiceDto = ModelUtils.getEditServiceDto();
        String uuid = UUID.randomUUID().toString();

        when(employeeRepository.findByUuid(uuid)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> superAdminService.editService(1L, editServiceDto, uuid));

        verify(employeeRepository).findByUuid(uuid);
        verify(serviceRepository, never()).findServiceById(1L);
        verify(serviceRepository, never()).save(any(Service.class));
        verify(modelMapper, never()).map(any(Service.class), any(GetServiceDto.class));
    }

    @Test
    void addService() {
        Service createdService = ModelUtils.getService();
        Service service = ModelUtils.getNewService();
        Employee employee = ModelUtils.getEmployee();
        CreateServiceDto createServiceDto = ModelUtils.getCreateServiceDto();
        GetServiceDto getServiceDto = ModelUtils.getServiceDto();
        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();
        String uuid = UUID.randomUUID().toString();

        when(employeeRepository.findByUuid(uuid)).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findById(tariffsInfo.getId())).thenReturn(Optional.of(tariffsInfo));
        when(serviceRepository.findServiceByTariffsInfoId(tariffsInfo.getId())).thenReturn(Optional.empty());
        when(serviceRepository.save(service)).thenReturn(createdService);
        when(modelMapper.map(createdService, GetServiceDto.class)).thenReturn(getServiceDto);

        assertEquals(getServiceDto, superAdminService.addService(createServiceDto, uuid));

        verify(employeeRepository).findByUuid(uuid);
        verify(tariffsInfoRepository).findById(1L);
        verify(serviceRepository).findServiceByTariffsInfoId(1L);
        verify(serviceRepository).save(service);
        verify(modelMapper).map(createdService, GetServiceDto.class);
    }

    @Test
    void addServiceThrowServiceAlreadyExistsException() {
        Service createdService = ModelUtils.getService();
        Employee employee = ModelUtils.getEmployee();
        CreateServiceDto createServiceDto = ModelUtils.getCreateServiceDto();
        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();
        String uuid = UUID.randomUUID().toString();

        when(employeeRepository.findByUuid(uuid)).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findById(tariffsInfo.getId())).thenReturn(Optional.of(tariffsInfo));
        when(serviceRepository.findServiceByTariffsInfoId(tariffsInfo.getId())).thenReturn(Optional.of(createdService));

        assertThrows(ServiceAlreadyExistsException.class,
            () -> superAdminService.addService(createServiceDto, uuid));

        verify(employeeRepository).findByUuid(uuid);
        verify(tariffsInfoRepository).findById(1L);
        verify(serviceRepository).findServiceByTariffsInfoId(1L);
    }

    @Test
    void addServiceThrowEmployeeNotFoundException() {
        CreateServiceDto createServiceDto = ModelUtils.getCreateServiceDto();
        String uuid = UUID.randomUUID().toString();

        when(employeeRepository.findByUuid(uuid)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> superAdminService.addService(createServiceDto, uuid));

        verify(employeeRepository).findByUuid(uuid);
    }

    @Test
    void addServiceThrowTariffNotFoundException() {
        Employee employee = ModelUtils.getEmployee();
        CreateServiceDto createServiceDto = ModelUtils.getCreateServiceDto();
        String uuid = UUID.randomUUID().toString();

        when(employeeRepository.findByUuid(uuid)).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> superAdminService.addService(createServiceDto, uuid));

        verify(employeeRepository).findByUuid(uuid);
        verify(tariffsInfoRepository).findById(1L);
    }

    @Test
    void setLimitDescriptionThrowsUnsupportedOperationExceptionTest() {
        assertThrows(UnsupportedOperationException.class,
            () -> superAdminService.setLimitDescription(1L, "limitDescription"));
    }

    @Test
    void includeBag() {
        when(bagRepository.findById(10))
            .thenReturn(Optional.of(Bag.builder().name("Useless paper").description("Description")
                .minAmountOfBags(MinAmountOfBag.EXCLUDE).location(Location.builder().id(1L).build()).build()));
        assertEquals(MinAmountOfBag.INCLUDE.toString(), superAdminService.includeBag(10).getMinAmountOfBag());
        verify(bagRepository).save(any(Bag.class));
        verify(bagRepository, times(1)).findById(anyInt());
    }

    @Test
    void getAllLocationTest() {
        List<Region> regionList = ModelUtils.getAllRegion();

        when(regionRepository.findAll()).thenReturn(regionList);

        superAdminService.getAllLocation();

        verify(regionRepository).findAll();
        regionList.forEach(region -> verify(modelMapper).map(region, LocationInfoDto.class));
    }

    @Test
    void getActiveLocationsTest() {
        List<Region> regionList = ModelUtils.getAllRegion();

        when(regionRepository.findRegionsWithActiveLocations()).thenReturn(regionList);

        superAdminService.getActiveLocations();

        verify(regionRepository).findRegionsWithActiveLocations();
        regionList.forEach(region -> verify(modelMapper).map(region, LocationInfoDto.class));
    }

    @Test
    void addLocationTest() {
        List<LocationCreateDto> locationCreateDtoList = ModelUtils.getLocationCreateDtoList();
        Region region = ModelUtils.getRegion();

        when(regionRepository.findRegionByEnNameAndUkrName("Kyiv region", "Київська область"))
            .thenReturn(Optional.of(region));

        superAdminService.addLocation(locationCreateDtoList);

        verify(regionRepository).findRegionByEnNameAndUkrName("Kyiv region", "Київська область");
        verify(locationRepository).findLocationByNameAndRegionId(anyString(), anyString(), anyLong());
        verify(locationRepository).save(any(Location.class));
    }

    @Test
    void addLocationCreateNewRegionTest() {
        List<LocationCreateDto> locationCreateDtoList = ModelUtils.getLocationCreateDtoList();
        Location location = ModelUtils.getLocationForCreateRegion();
        when(regionRepository.findRegionByEnNameAndUkrName("Kyiv region", "Київська область"))
            .thenReturn(Optional.empty());
        when(regionRepository.save(any())).thenReturn(ModelUtils.getRegion());
        superAdminService.addLocation(locationCreateDtoList);
        verify(locationRepository).findLocationByNameAndRegionId("Київ", "Kyiv", 1L);
        verify(regionRepository).findRegionByEnNameAndUkrName("Kyiv region", "Київська область");
        verify(locationRepository).save(location);
    }

    @Test
    void activateLocation() {
        Location location = ModelUtils.getLocationDto();

        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));

        superAdminService.activateLocation(1L);

        verify(locationRepository).findById(1L);
        verify(locationRepository).save(location);
    }

    @Test
    void deactivateLocation() {
        Location location = ModelUtils.getLocationDto();
        location.setLocationStatus(LocationStatus.ACTIVE);

        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));

        superAdminService.deactivateLocation(1L);

        verify(locationRepository).findById(1L);
        verify(locationRepository).save(any());
    }

    @Test
    void excludeBag() {
        Optional<Bag> bag = Optional.ofNullable(ModelUtils.bagDto());

        when(bagRepository.findById(1)).thenReturn(bag);

        superAdminService.excludeBag(1);

        verify(bagRepository).findById(1);
        verify(bagRepository).save(bag.get());
    }

    @Test
    void addTariffServiceExceptionTest() {
        AddServiceDto dto = ModelUtils.addServiceDto();
        assertThrows(NotFoundException.class, () -> superAdminService.addTariffService(dto, "uuid"));
        verify(userRepository).findByUuid(anyString());
        verify(locationRepository).findById(anyLong());
    }

    @Test
    void addLocationThrowLocationAlreadyCreatedExceptionTest() {
        List<LocationCreateDto> locationCreateDtoList = ModelUtils.getLocationCreateDtoList();
        Location location = ModelUtils.getLocation();
        when(regionRepository.findRegionByEnNameAndUkrName("Kyiv region", "Київська область"))
            .thenReturn(Optional.of(ModelUtils.getRegion()));
        when(locationRepository.findLocationByNameAndRegionId("Київ", "Kyiv", 1L)).thenReturn(Optional.of(location));
        assertThrows(NotFoundException.class, () -> superAdminService.addLocation(locationCreateDtoList));

        verify(locationRepository).findLocationByNameAndRegionId("Київ", "Kyiv", 1L);
    }

    @Test
    void deactivateLocationExceptionTest() {
        assertThrows(NotFoundException.class, () -> superAdminService.deactivateLocation(1L));
        verify(locationRepository).findById(anyLong());
    }

    @Test
    void deactivateLocationException2Test() {
        Location location = ModelUtils.getLocationDto();
        location.setLocationStatus(LocationStatus.DEACTIVATED);

        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));

        assertThrows(BadRequestException.class, () -> superAdminService.deactivateLocation(1L));
    }

    @Test
    void activateLocationExceptionTest() {
        assertThrows(NotFoundException.class, () -> superAdminService.activateLocation(1L));
        verify(locationRepository).findById(anyLong());
    }

    @Test
    void activateLocationException2Test() {
        Location location = ModelUtils.getLocationDto();
        location.setLocationStatus(LocationStatus.ACTIVE);
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        assertThrows(BadRequestException.class, () -> superAdminService.activateLocation(1L));
    }

    @Test
    void excludeBagExceptionTest() {
        assertThrows(NotFoundException.class, () -> superAdminService.excludeBag(1));
        verify(bagRepository).findById(anyInt());
    }

    @Test
    void excludeBagException2Test() {
        Optional<Bag> bag = Optional.ofNullable(ModelUtils.bagDto2());

        when(bagRepository.findById(1)).thenReturn(bag);

        assertThrows(BadRequestException.class, () -> superAdminService.excludeBag(1));
    }

    @Test
    void includeBagExceptionTest() {
        assertThrows(NotFoundException.class, () -> superAdminService.includeBag(1));
        verify(bagRepository).findById(anyInt());
    }

    @Test
    void includeBagException2Test() {
        Optional<Bag> bag = Optional.ofNullable(ModelUtils.bagDto());

        when(bagRepository.findById(1)).thenReturn(bag);

        assertThrows(BadRequestException.class, () -> superAdminService.includeBag(1));
    }

    @Test
    void deleteCourierTest() {
        Courier courier = ModelUtils.getCourier();
        courier.setCourierStatus(CourierStatus.DELETED);

        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));
        when(courierRepository.save(courier)).thenReturn(courier);

        superAdminService.deleteCourier(1L);
        assertEquals(CourierStatus.DELETED, courier.getCourierStatus());

        verify(courierRepository).findById(1L);
        verify(courierRepository).save(courier);
    }

    @Test
    void deleteCourierThrowCourierNotFoundException() {
        when(courierRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> superAdminService.deleteCourier(1L));

        verify(courierRepository).findById(1L);
    }

    @Test
    void createCourier() {
        Courier courier = ModelUtils.getCourier();
        courier.setId(null);
        CreateCourierDto createCourierDto = ModelUtils.getCreateCourierDto();

        when(employeeRepository.findByUuid(anyString())).thenReturn(Optional.ofNullable(getEmployee()));
        when(courierRepository.findAll()).thenReturn(List.of(Courier.builder()
            .nameEn("Test1")
            .nameUk("Тест1")
            .build()));
        when(courierRepository.save(any())).thenReturn(courier);
        when(modelMapper.map(any(), eq(CreateCourierDto.class))).thenReturn(createCourierDto);

        assertEquals(createCourierDto,
            superAdminService.createCourier(createCourierDto, ModelUtils.TEST_USER.getUuid()));

        verify(courierRepository).save(any());
        verify(modelMapper).map(any(), eq(CreateCourierDto.class));
    }

    @Test
    void createCourierAlreadyExists() {
        Courier courier = ModelUtils.getCourier();
        courier.setId(null);
        CreateCourierDto createCourierDto = ModelUtils.getCreateCourierDto();
        String uuid = ModelUtils.TEST_USER.getUuid();

        when(employeeRepository.findByUuid(anyString())).thenReturn(Optional.ofNullable(getEmployee()));
        when(courierRepository.findAll()).thenReturn(List.of(getCourier(), getCourier()));

        Throwable throwable = assertThrows(CourierAlreadyExists.class,
            () -> superAdminService.createCourier(createCourierDto, uuid));
        assertEquals(ErrorMessage.COURIER_ALREADY_EXISTS, throwable.getMessage());
        verify(employeeRepository).findByUuid(anyString());
        verify(courierRepository).findAll();
    }

    @Test
    void updateCourierTest() {
        Courier courier = getCourier();

        CourierUpdateDto dto = CourierUpdateDto.builder()
            .courierId(1L)
            .nameUk("УБС")
            .nameEn("UBS")
            .build();

        Courier courierToSave = Courier.builder()
            .id(courier.getId())
            .courierStatus(courier.getCourierStatus())
            .nameUk("УБС")
            .nameEn("UBS")
            .build();

        CourierDto courierDto = CourierDto.builder()
            .courierId(courier.getId())
            .courierStatus("Active")
            .nameUk("УБС")
            .nameEn("UBS")
            .build();

        when(courierRepository.findById(dto.getCourierId())).thenReturn(Optional.of(courier));
        when(courierRepository.save(courier)).thenReturn(courierToSave);
        when(modelMapper.map(courierToSave, CourierDto.class)).thenReturn(courierDto);

        CourierDto actual = superAdminService.updateCourier(dto);
        CourierDto expected = CourierDto.builder()
            .courierId(getCourier().getId())
            .courierStatus("Active")
            .nameUk("УБС")
            .nameEn("UBS")
            .build();

        assertEquals(expected, actual);
    }

    @Test
    void updateCourierNotFound() {
        Courier courier = getCourier();

        CourierUpdateDto dto = CourierUpdateDto.builder()
            .courierId(1L)
            .nameUk("УБС")
            .nameEn("UBS")
            .build();

        when(courierRepository.findById(courier.getId()))
            .thenThrow(new NotFoundException(ErrorMessage.COURIER_IS_NOT_FOUND_BY_ID));
        assertThrows(NotFoundException.class, () -> superAdminService.updateCourier(dto));
    }

    @Test
    void getAllTariffsInfoTest() {
        when(tariffsInfoRepository.findAll(any(TariffsInfoSpecification.class)))
            .thenReturn(List.of(ModelUtils.getTariffsInfo()));
        when(modelMapper.map(any(TariffsInfo.class), eq(GetTariffsInfoDto.class))).thenReturn(getAllTariffsInfoDto());

        superAdminService.getAllTariffsInfo(TariffsInfoFilterCriteria.builder().build());

        verify(tariffsInfoRepository).findAll(any(TariffsInfoSpecification.class));
    }

    @Test
    void CreateReceivingStation() {
        String test = TEST_USER.getUuid();
        AddingReceivingStationDto stationDto = AddingReceivingStationDto.builder().name("Петрівка").build();
        when(receivingStationRepository.existsReceivingStationByName(any())).thenReturn(false, true);
        lenient().when(modelMapper.map(any(ReceivingStation.class), eq(ReceivingStationDto.class)))
            .thenReturn(getReceivingStationDto());
        when(receivingStationRepository.save(any())).thenReturn(getReceivingStation(), getReceivingStation());
        when(employeeRepository.findByUuid(test)).thenReturn(Optional.ofNullable(getEmployee()));
        superAdminService.createReceivingStation(stationDto, test);

        verify(receivingStationRepository, times(1)).existsReceivingStationByName(any());
        verify(receivingStationRepository, times(1)).save(any());
        verify(modelMapper, times(1))
            .map(any(ReceivingStation.class), eq(ReceivingStationDto.class));

        Exception thrown = assertThrows(UnprocessableEntityException.class,
            () -> superAdminService.createReceivingStation(stationDto, test));
        assertEquals(thrown.getMessage(), ErrorMessage.RECEIVING_STATION_ALREADY_EXISTS
            + stationDto.getName());
        verify(employeeRepository).findByUuid(any());
    }

    @Test
    void createReceivingStationSaveCorrectValue() {
        String receivingStationName = "Петрівка";
        Employee employee = getEmployee();
        AddingReceivingStationDto addingReceivingStationDto =
            AddingReceivingStationDto.builder().name(receivingStationName).build();
        ReceivingStation activatedReceivingStation = ReceivingStation.builder()
            .name(receivingStationName)
            .createdBy(employee)
            .createDate(LocalDate.now())
            .stationStatus(StationStatus.ACTIVE)
            .build();

        ReceivingStationDto receivingStationDto = getReceivingStationDto();

        when(employeeRepository.findByUuid(any())).thenReturn(Optional.ofNullable(employee));
        when(receivingStationRepository.existsReceivingStationByName(any())).thenReturn(false);
        when(receivingStationRepository.save(any())).thenReturn(activatedReceivingStation);
        when(modelMapper.map(any(), eq(ReceivingStationDto.class)))
            .thenReturn(receivingStationDto);

        superAdminService.createReceivingStation(addingReceivingStationDto, employee.getUuid());

        verify(employeeRepository).findByUuid(any());
        verify(receivingStationRepository).existsReceivingStationByName(any());
        verify(receivingStationRepository).save(activatedReceivingStation);
        verify(modelMapper)
            .map(any(ReceivingStation.class), eq(ReceivingStationDto.class));

    }

    @Test
    void updateReceivingStation() {
        ReceivingStationDto stationDto = getReceivingStationDto();

        when(receivingStationRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getReceivingStation()));
        when(receivingStationRepository.save(any())).thenReturn(ModelUtils.getReceivingStation());
        when(modelMapper.map(any(), eq(ReceivingStationDto.class))).thenReturn(ModelUtils.getReceivingStationDto());

        superAdminService.updateReceivingStation(stationDto);

        verify(receivingStationRepository).findById(anyLong());
        verify(receivingStationRepository).save(any());
        verify(modelMapper).map(any(), eq(ReceivingStationDto.class));
    }

    @Test
    void updateReceivingStationThrowsExceptionWhenReceivingStationNonFoundTest() {
        ReceivingStationDto receivingStationDto = getReceivingStationDto();
        when(receivingStationRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> superAdminService.updateReceivingStation(receivingStationDto));
        verify(receivingStationRepository).findById(anyLong());
    }

    @Test
    void getAllReceivingStation() {
        when(receivingStationRepository.findAll()).thenReturn(List.of(getReceivingStation()));
        when(modelMapper.map(any(), any())).thenReturn(getReceivingStationDto());

        List<ReceivingStationDto> stationDtos = superAdminService.getAllReceivingStations();

        assertEquals(1, stationDtos.size());

        verify(receivingStationRepository, times(1)).findAll();
    }

    @Test
    void deleteReceivingStation() {
        ReceivingStation station = getReceivingStation();
        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(station));

        superAdminService.deleteReceivingStation(1L);

        verify(receivingStationRepository, times(1)).findById(1L);
        verify(receivingStationRepository, times(1)).delete(station);

        when(receivingStationRepository.findById(2L)).thenReturn(Optional.empty());

        Exception thrown1 = assertThrows(NotFoundException.class,
            () -> superAdminService.deleteReceivingStation(2L));

        assertEquals(ErrorMessage.RECEIVING_STATION_NOT_FOUND_BY_ID + 2L, thrown1.getMessage());
    }

    @Test
    void editNewTariffSuccess() {
        AddNewTariffDto dto = ModelUtils.getAddNewTariffDto();
        when(locationRepository.findAllByIdAndRegionId(dto.getLocationIdList(), dto.getRegionId()))
            .thenReturn(ModelUtils.getLocationList());
        when(employeeRepository.findByUuid(any())).thenReturn(Optional.ofNullable(getEmployee()));
        when(receivingStationRepository.findAllById(List.of(1L))).thenReturn(ModelUtils.getReceivingList());
        when(courierRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getCourier()));
        when(tariffsLocationRepository.findAllByCourierIdAndLocationIds(1L, List.of(1L)))
            .thenReturn(Collections.emptyList());
        when(tariffsInfoRepository.save(any())).thenReturn(ModelUtils.getTariffInfo());
        superAdminService.addNewTariff(dto, "35467585763t4sfgchjfuyetf");
        verify(tariffsInfoRepository, times(2)).save(any());
        verify(tariffsLocationRepository).saveAll(anySet());
    }

    @Test
    void addNewTariffThrowsExceptionWhenListOfLocationsIsEmptyTest() {
        AddNewTariffDto dto = ModelUtils.getAddNewTariffDto();

        when(courierRepository.findById(1L)).thenReturn(Optional.of(ModelUtils.getCourier()));
        when(employeeRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(Optional.ofNullable(getEmployee()));
        when(tariffsInfoRepository.save(any())).thenReturn(ModelUtils.getTariffInfo());
        when(tariffsLocationRepository.findAllByCourierIdAndLocationIds(1L, List.of(1L)))
            .thenReturn(Collections.emptyList());
        when(receivingStationRepository.findAllById(List.of(1L))).thenReturn(ModelUtils.getReceivingList());
        when(locationRepository.findAllByIdAndRegionId(dto.getLocationIdList(),
            dto.getRegionId())).thenReturn(Collections.emptyList());

        assertThrows(NotFoundException.class,
            () -> superAdminService.addNewTariff(dto, "35467585763t4sfgchjfuyetf"));

        verify(courierRepository).findById(1L);
        verify(employeeRepository).findByUuid("35467585763t4sfgchjfuyetf");
        verify(tariffsInfoRepository, times(1)).save(any());
        verify(tariffsLocationRepository).findAllByCourierIdAndLocationIds(1L, List.of(1L));
        verify(receivingStationRepository).findAllById(List.of(1L));
        verify(locationRepository).findAllByIdAndRegionId(dto.getLocationIdList(), dto.getRegionId());
    }

    @Test
    void addNewTariffThrowsExceptionWhenSuchTariffIsAlreadyExistsTest() {
        AddNewTariffDto dto = ModelUtils.getAddNewTariffDto();
        TariffLocation tariffLocation = TariffLocation
            .builder()
            .id(1L)
            .location(ModelUtils.getLocation())
            .build();
        when(courierRepository.findById(1L)).thenReturn(Optional.of(ModelUtils.getCourier()));
        when(tariffsLocationRepository.findAllByCourierIdAndLocationIds(dto.getCourierId(),
            dto.getLocationIdList())).thenReturn(List.of(tariffLocation));

        assertThrows(TariffAlreadyExistsException.class,
            () -> superAdminService.addNewTariff(dto, "35467585763t4sfgchjfuyetf"));

        verify(courierRepository).findById(1L);
        verify(tariffsLocationRepository).findAllByCourierIdAndLocationIds(dto.getCourierId(),
            dto.getLocationIdList());
        verifyNoMoreInteractions(courierRepository, tariffsLocationRepository);
    }

    @Test
    void addNewTariffThrowsException2() {
        AddNewTariffDto dto = ModelUtils.getAddNewTariffDto();
        when(courierRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getCourier()));
        assertThrows(NotFoundException.class,
            () -> superAdminService.addNewTariff(dto, "35467585763t4sfgchjfuyetf"));
        verify(courierRepository).findById(anyLong());
        verify(receivingStationRepository).findAllById(any());
        verify(tariffsLocationRepository).findAllByCourierIdAndLocationIds(anyLong(), anyList());
    }

    @Test
    void addNewTariffThrowsException3() {
        AddNewTariffDto dto = ModelUtils.getAddNewTariffDto();
        when(courierRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
            () -> superAdminService.addNewTariff(dto, "35467585763t4sfgchjfuyetf"));
    }

    @Test
    void checkIfTariffDoesNotExistsTest() {
        AddNewTariffDto dto = ModelUtils.getAddNewTariffDto();
        when(tariffsLocationRepository.findAllByCourierIdAndLocationIds(anyLong(), any()))
            .thenReturn(Collections.emptyList());
        boolean actual = superAdminService.checkIfTariffExists(dto);
        assertFalse(actual);
        verify(tariffsLocationRepository).findAllByCourierIdAndLocationIds(anyLong(), any());
    }

    @Test
    void checkIfTariffExistsTest() {
        AddNewTariffDto dto = ModelUtils.getAddNewTariffDto();
        TariffLocation tariffLocation = TariffLocation
            .builder()
            .id(1L)
            .tariffsInfo(ModelUtils.getTariffsInfo())
            .location(ModelUtils.getLocation())
            .locationStatus(LocationStatus.ACTIVE)
            .build();
        when(tariffsLocationRepository.findAllByCourierIdAndLocationIds(anyLong(), any()))
            .thenReturn(List.of(tariffLocation));
        boolean actual = superAdminService.checkIfTariffExists(dto);
        assertTrue(actual);
        verify(tariffsLocationRepository).findAllByCourierIdAndLocationIds(anyLong(), any());
    }

    @Test
    void editTariffTest() {
        when(tariffsInfoRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getTariffInfo()));
        superAdminService.setTariffLimitByAmountOfBags(1L, ModelUtils.getAmountOfBagDto());
        verify(tariffsInfoRepository).save(any());
    }

    @Test
    void setTariffLimitBySumOfOrderTest() {
        when(tariffsInfoRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getTariffInfo()));
        superAdminService.setTariffLimitBySumOfOrder(1L, ModelUtils.getEditPriceOfOrder());
        verify(tariffsInfoRepository).save(any());
    }

    @Test
    void setTariffLimitsWithAmountOfBigBags() {
        when(tariffsInfoRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getTariffInfo()));
        when(bagRepository.getBagsByTariffsInfoAndMinAmountOfBags(any(TariffsInfo.class), any(MinAmountOfBag.class)))
            .thenReturn(ModelUtils.getBaglist());

        superAdminService.setTariffLimits(1L, ModelUtils.setTariffLimitsWithAmountOfBigBags());
        verify(tariffsInfoRepository).save(any());
    }

    @Test
    void setTariffLimitsWithPriceOfOrder() {
        when(tariffsInfoRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getTariffInfo()));
        when(bagRepository.getBagsByTariffsInfoAndMinAmountOfBags(any(TariffsInfo.class), any(MinAmountOfBag.class)))
            .thenReturn(ModelUtils.getBaglist());

        superAdminService.setTariffLimits(1L, ModelUtils.setTariffLimitsWithPriceOfOrder());
        verify(tariffsInfoRepository).save(any());
    }

    @Test
    void setTariffLimitsWithPriceOfOrderMaxVatueIsGreaterThanMin() {
        SetTariffLimitsDto setTariffLimitsDto = ModelUtils.setTariffLimitsWithPriceOfOrderWhereMaxValueIsGreater();

        when(tariffsInfoRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getTariffInfo()));
        when(bagRepository.getBagsByTariffsInfoAndMinAmountOfBags(any(TariffsInfo.class), any(MinAmountOfBag.class)))
            .thenReturn(ModelUtils.getBaglist());

        assertThrows(BadRequestException.class,
            () -> superAdminService.setTariffLimits(1L,
                setTariffLimitsDto));
    }

    @Test
    void setTariffLimitsWithAmountOfBigBagMaxVatueIsGreaterThanMin() {
        SetTariffLimitsDto setTariffLimitsDto = ModelUtils.setTariffLimitsWithAmountOfBigBagsWhereMaxValueIsGreater();

        when(tariffsInfoRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getTariffInfo()));
        when(bagRepository.getBagsByTariffsInfoAndMinAmountOfBags(any(TariffsInfo.class), any(MinAmountOfBag.class)))
            .thenReturn(ModelUtils.getBaglist());

        assertThrows(BadRequestException.class,
            () -> superAdminService.setTariffLimits(1L,
                setTariffLimitsDto));
    }

    @Test
    void setTariffLimitsWithBothLimitsInputed() {
        SetTariffLimitsDto setTariffLimitsDto = ModelUtils.setTariffLimitsWithBothLimitsInputed();

        when(tariffsInfoRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getTariffInfo()));
        when(bagRepository.getBagsByTariffsInfoAndMinAmountOfBags(any(TariffsInfo.class), any(MinAmountOfBag.class)))
            .thenReturn(ModelUtils.getBaglist());

        assertThrows(BadRequestException.class,
            () -> superAdminService.setTariffLimits(1L, setTariffLimitsDto));
    }

    @Test
    void setTariffLimitsWithNoneLimitsInputed() {
        SetTariffLimitsDto setTariffLimitsDto = ModelUtils.setTariffLimitsWithNoneLimitsInputed();

        when(tariffsInfoRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getTariffInfo()));
        when(bagRepository.getBagsByTariffsInfoAndMinAmountOfBags(any(TariffsInfo.class), any(MinAmountOfBag.class)))
            .thenReturn(ModelUtils.getBaglist());

        assertThrows(BadRequestException.class,
            () -> superAdminService.setTariffLimits(1L, setTariffLimitsDto));
    }

    @Test
    void setTariffLimitsBagWithSuitableParametersNotFound() {
        SetTariffLimitsDto setTariffLimitsDto = ModelUtils.setTariffLimitsWithAmountOfBigBags();

        when(tariffsInfoRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getTariffInfo()));
        when(bagRepository.getBagsByTariffsInfoAndMinAmountOfBags(any(TariffsInfo.class), any(MinAmountOfBag.class)))
            .thenReturn(List.of());

        assertThrows(BadRequestException.class,
            () -> superAdminService.setTariffLimits(1L, setTariffLimitsDto));
    }

    @Test
    void editTariffTestThrows() {
        var dto = ModelUtils.getEditPriceOfOrder();
        when(tariffsInfoRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
            () -> superAdminService.setTariffLimitBySumOfOrder(1L, dto));
    }

    @Test
    void deactivateTariff() {
        TariffsInfo tariffsInfo = ModelUtils.getTariffInfoWithLimitOfBags();
        when(tariffsInfoRepository.findById(anyLong())).thenReturn(Optional.of(tariffsInfo));
        superAdminService.deactivateTariffCard(1L);
        verify(tariffsInfoRepository).findById(anyLong());
        verify(tariffsInfoRepository).save(tariffsInfo);
    }

    @Test
    void deactivateTariffTestThrows() {
        when(tariffsInfoRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
            () -> superAdminService.deactivateTariffCard(1L));
    }

    @Test
    void editLocation() {
        Location location = ModelUtils.getLocation();
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(location));
        EditLocationDto dto = new EditLocationDto(1L, "Lviv", "Львів");
        superAdminService.editLocations(List.of(dto));
        verify(locationRepository).save(location);
        verify(locationRepository).findById(anyLong());
        verify(locationRepository).existsByNameUkAndNameEnAndRegion("Львів", "Lviv", location.getRegion());
        assertEquals(location.getNameEn(), dto.getNameEn());
    }

    @Test
    void changeTariffLocationsStatusParamActivate() {
        TariffsInfo tariffsInfo = ModelUtils.getTariffInfo();
        TariffLocation tariffLocation = tariffsInfo.getTariffLocations().stream().findFirst().get();
        Location location = tariffLocation.getLocation();

        List<Long> locationsId = new ArrayList<>();
        locationsId.add(location.getId());
        ChangeTariffLocationStatusDto dto = new ChangeTariffLocationStatusDto().setLocationIds(locationsId);

        when(tariffsInfoRepository.findById(anyLong())).thenReturn(Optional.of(tariffsInfo));
        superAdminService.changeTariffLocationsStatus(1L, dto, "activate");
        verify(tariffsLocationRepository).changeStatusAll(1L, locationsId, LocationStatus.ACTIVE.name());
    }

    @Test
    void changeTariffLocationsStatusParamDeactivate() {
        TariffsInfo tariffsInfo = ModelUtils.getTariffInfo();
        TariffLocation tariffLocation = tariffsInfo.getTariffLocations().stream().findFirst().get();
        Location location = tariffLocation.getLocation();

        List<Long> locationsId = new ArrayList<>();
        locationsId.add(location.getId());
        ChangeTariffLocationStatusDto dto = new ChangeTariffLocationStatusDto().setLocationIds(locationsId);

        when(tariffsInfoRepository.findById(anyLong())).thenReturn(Optional.of(tariffsInfo));
        superAdminService.changeTariffLocationsStatus(1L, dto, "deactivate");
        verify(tariffsLocationRepository).changeStatusAll(1L, locationsId, LocationStatus.DEACTIVATED.name());
    }

    @Test
    void changeTariffLocationsStatusParamUnresolvable() {
        TariffsInfo tariffsInfo = ModelUtils.getTariffInfo();
        TariffLocation tariffLocation = tariffsInfo.getTariffLocations().stream().findFirst().get();
        Location location = tariffLocation.getLocation();

        List<Long> locationsId = new ArrayList<>();
        locationsId.add(location.getId());
        ChangeTariffLocationStatusDto dto = new ChangeTariffLocationStatusDto().setLocationIds(locationsId);

        when(tariffsInfoRepository.findById(anyLong())).thenReturn(Optional.of(tariffsInfo));
        assertThrows(BadRequestException.class,
            () -> superAdminService.changeTariffLocationsStatus(1L, dto, "unresolvable"));
    }

    @Test
    void deactivateTariffByOneRegion() {
        DetailsOfDeactivateTariffsDto details = ModelUtils.getDetailsOfDeactivateTariffsDtoWithRegion();

        when(deactivateTariffsForChosenParamRepository.isRegionsExists(anyList())).thenReturn(true);
        superAdminService.deactivateTariffForChosenParam(details);
        verify(deactivateTariffsForChosenParamRepository).isRegionsExists(List.of(1L));
        verify(deactivateTariffsForChosenParamRepository).deactivateTariffsByRegions(List.of(1L));
    }

    @Test
    void deactivateTariffByTwoRegions() {
        DetailsOfDeactivateTariffsDto details = ModelUtils.getDetailsOfDeactivateTariffsDtoWithRegion();
        details.setRegionsId(Optional.of(List.of(1L, 2L)));

        when(deactivateTariffsForChosenParamRepository.isRegionsExists(anyList())).thenReturn(true);
        superAdminService.deactivateTariffForChosenParam(details);
        verify(deactivateTariffsForChosenParamRepository).isRegionsExists(List.of(1L, 2L));
        verify(deactivateTariffsForChosenParamRepository).deactivateTariffsByRegions(List.of(1L, 2L));
    }

    @Test
    void deactivateTariffByNotExistingRegionThrows() {
        DetailsOfDeactivateTariffsDto details = ModelUtils.getDetailsOfDeactivateTariffsDtoWithRegion();

        when(deactivateTariffsForChosenParamRepository.isRegionsExists(anyList())).thenReturn(false);
        assertThrows(NotFoundException.class,
            () -> superAdminService.deactivateTariffForChosenParam(details));
        verify(deactivateTariffsForChosenParamRepository).isRegionsExists(List.of(1L));
        verify(deactivateTariffsForChosenParamRepository, never()).deactivateTariffsByRegions(anyList());
    }

    @Test
    void deactivateTariffByOneRegionAndCities() {
        DetailsOfDeactivateTariffsDto details = ModelUtils.getDetailsOfDeactivateTariffsDtoWithRegionAndCities();

        when(regionRepository.existsRegionById(anyLong())).thenReturn(true);
        when(deactivateTariffsForChosenParamRepository.isCitiesExistForRegion(anyList(), anyLong())).thenReturn(true);
        superAdminService.deactivateTariffForChosenParam(details);
        verify(regionRepository).existsRegionById(1L);
        verify(deactivateTariffsForChosenParamRepository).isCitiesExistForRegion(List.of(1L, 11L), 1L);
        verify(deactivateTariffsForChosenParamRepository).deactivateTariffsByRegionsAndCities(List.of(1L, 11L), 1L);
    }

    @Test
    void deactivateTariffByOneRegionAndNotExistingCitiesThrows() {
        DetailsOfDeactivateTariffsDto details = ModelUtils.getDetailsOfDeactivateTariffsDtoWithRegionAndCities();

        when(regionRepository.existsRegionById(anyLong())).thenReturn(true);
        when(deactivateTariffsForChosenParamRepository.isCitiesExistForRegion(anyList(), anyLong())).thenReturn(false);
        assertThrows(NotFoundException.class,
            () -> superAdminService.deactivateTariffForChosenParam(details));
        verify(regionRepository).existsRegionById(1L);
        verify(deactivateTariffsForChosenParamRepository).isCitiesExistForRegion(List.of(1L, 11L), 1L);
        verify(deactivateTariffsForChosenParamRepository, never()).deactivateTariffsByRegionsAndCities(anyList(),
            anyLong());
    }

    @Test
    void deactivateTariffByOneNotExistingRegionAndCitiesThrows() {
        DetailsOfDeactivateTariffsDto details = ModelUtils.getDetailsOfDeactivateTariffsDtoWithRegionAndCities();

        when(regionRepository.existsRegionById(anyLong())).thenReturn(false);
        assertThrows(NotFoundException.class,
            () -> superAdminService.deactivateTariffForChosenParam(details));
        verify(regionRepository).existsRegionById(1L);
        verify(deactivateTariffsForChosenParamRepository, never()).deactivateTariffsByRegionsAndCities(anyList(),
            anyLong());
    }

    @Test
    void deactivateTariffByCourier() {
        DetailsOfDeactivateTariffsDto details = ModelUtils.getDetailsOfDeactivateTariffsDtoWithCourier();

        when(courierRepository.existsCourierById(anyLong())).thenReturn(true);
        superAdminService.deactivateTariffForChosenParam(details);
        verify(courierRepository).existsCourierById(1L);
        verify(deactivateTariffsForChosenParamRepository).deactivateTariffsByCourier(1L);
    }

    @Test
    void deactivateTariffByNotExistingCourierThrows() {
        DetailsOfDeactivateTariffsDto details = ModelUtils.getDetailsOfDeactivateTariffsDtoWithCourier();

        when(courierRepository.existsCourierById(anyLong())).thenReturn(false);
        assertThrows(NotFoundException.class,
            () -> superAdminService.deactivateTariffForChosenParam(details));
        verify(courierRepository).existsCourierById(1L);
        verify(deactivateTariffsForChosenParamRepository, never()).deactivateTariffsByCourier(anyLong());
    }

    @Test
    void deactivateTariffByReceivingStations() {
        DetailsOfDeactivateTariffsDto details = ModelUtils.getDetailsOfDeactivateTariffsDtoWithReceivingStations();

        when(deactivateTariffsForChosenParamRepository.isReceivingStationsExists(anyList())).thenReturn(true);
        superAdminService.deactivateTariffForChosenParam(details);
        verify(deactivateTariffsForChosenParamRepository).isReceivingStationsExists(List.of(1L, 12L));
        verify(deactivateTariffsForChosenParamRepository).deactivateTariffsByReceivingStations(List.of(1L, 12L));
    }

    @Test
    void deactivateTariffByNotExistingReceivingStationsThrows() {
        DetailsOfDeactivateTariffsDto details = ModelUtils.getDetailsOfDeactivateTariffsDtoWithReceivingStations();

        when(deactivateTariffsForChosenParamRepository.isReceivingStationsExists(anyList())).thenReturn(false);
        assertThrows(NotFoundException.class,
            () -> superAdminService.deactivateTariffForChosenParam(details));
        verify(deactivateTariffsForChosenParamRepository).isReceivingStationsExists(List.of(1L, 12L));
        verify(deactivateTariffsForChosenParamRepository, never()).deactivateTariffsByReceivingStations(anyList());
    }

    @Test
    void deactivateTariffByCourierAndReceivingStations() {
        DetailsOfDeactivateTariffsDto details =
            ModelUtils.getDetailsOfDeactivateTariffsDtoWithCourierAndReceivingStations();

        when(courierRepository.existsCourierById(anyLong())).thenReturn(true);
        when(deactivateTariffsForChosenParamRepository.isReceivingStationsExists(anyList())).thenReturn(true);
        superAdminService.deactivateTariffForChosenParam(details);
        verify(courierRepository).existsCourierById(1L);
        verify(deactivateTariffsForChosenParamRepository).isReceivingStationsExists(List.of(1L, 12L));
        verify(deactivateTariffsForChosenParamRepository)
            .deactivateTariffsByCourierAndReceivingStations(1L, List.of(1L, 12L));
    }

    @Test
    void deactivateTariffByNotExistingCourierAndReceivingStationsTrows() {
        DetailsOfDeactivateTariffsDto details =
            ModelUtils.getDetailsOfDeactivateTariffsDtoWithCourierAndReceivingStations();

        when(courierRepository.existsCourierById(anyLong())).thenReturn(false);
        assertThrows(NotFoundException.class,
            () -> superAdminService.deactivateTariffForChosenParam(details));
        verify(courierRepository).existsCourierById(1L);
        verify(deactivateTariffsForChosenParamRepository, never())
            .deactivateTariffsByCourierAndReceivingStations(anyLong(), anyList());
    }

    @Test
    void deactivateTariffByCourierAndNotExistingReceivingStationsTrows() {
        DetailsOfDeactivateTariffsDto details =
            ModelUtils.getDetailsOfDeactivateTariffsDtoWithCourierAndReceivingStations();

        when(courierRepository.existsCourierById(anyLong())).thenReturn(true);
        when(deactivateTariffsForChosenParamRepository.isReceivingStationsExists(anyList())).thenReturn(false);
        assertThrows(NotFoundException.class,
            () -> superAdminService.deactivateTariffForChosenParam(details));
        verify(courierRepository).existsCourierById(1L);
        verify(deactivateTariffsForChosenParamRepository).isReceivingStationsExists(List.of(1L, 12L));
        verify(deactivateTariffsForChosenParamRepository, never())
            .deactivateTariffsByCourierAndReceivingStations(anyLong(), anyList());
    }

    @Test
    void deactivateTariffByCourierAndOneRegion() {
        DetailsOfDeactivateTariffsDto details = ModelUtils.getDetailsOfDeactivateTariffsDtoWithCourierAndRegion();

        when(regionRepository.existsRegionById(anyLong())).thenReturn(true);
        when(courierRepository.existsCourierById(anyLong())).thenReturn(true);
        superAdminService.deactivateTariffForChosenParam(details);
        verify(regionRepository).existsRegionById(1L);
        verify(courierRepository).existsCourierById(1L);
        verify(deactivateTariffsForChosenParamRepository).deactivateTariffsByCourierAndRegion(1L, 1L);
    }

    @Test
    void deactivateTariffByNotExistingCourierAndOneRegionThrows() {
        DetailsOfDeactivateTariffsDto details = ModelUtils.getDetailsOfDeactivateTariffsDtoWithCourierAndRegion();

        when(regionRepository.existsRegionById(anyLong())).thenReturn(true);
        when(courierRepository.existsCourierById(anyLong())).thenReturn(false);
        assertThrows(NotFoundException.class,
            () -> superAdminService.deactivateTariffForChosenParam(details));
        verify(regionRepository).existsRegionById(1L);
        verify(courierRepository).existsCourierById(1L);
        verify(deactivateTariffsForChosenParamRepository, never()).deactivateTariffsByCourierAndRegion(anyLong(),
            anyLong());
    }

    @Test
    void deactivateTariffByCourierAndNotExistingRegionThrows() {
        DetailsOfDeactivateTariffsDto details = ModelUtils.getDetailsOfDeactivateTariffsDtoWithCourierAndRegion();

        when(regionRepository.existsRegionById(anyLong())).thenReturn(false);
        assertThrows(NotFoundException.class,
            () -> superAdminService.deactivateTariffForChosenParam(details));
        verify(regionRepository).existsRegionById(1L);
        verify(deactivateTariffsForChosenParamRepository, never()).deactivateTariffsByCourierAndRegion(anyLong(),
            anyLong());
    }

    @Test
    void deactivateTariffByOneRegionAndCityAndStation() {
        DetailsOfDeactivateTariffsDto details =
            ModelUtils.getDetailsOfDeactivateTariffsDtoWithRegionAndCityAndStation();

        doMockForTariffWithRegionAndCityAndStation(true, true, true);
        superAdminService.deactivateTariffForChosenParam(details);
        verifyForTariffWithRegionAndCityAndStation(true, true, true);
    }

    @ParameterizedTest
    @MethodSource("deactivateTariffByNotExistingRegionOrCityOrStationProvider")
    void deactivateTariffByNotExistingRegionAndCityAndStationThrows(
        boolean isRegionExists,
        boolean isCitiesExistForRegion,
        boolean isReceivingStationsExists) {

        DetailsOfDeactivateTariffsDto details =
            ModelUtils.getDetailsOfDeactivateTariffsDtoWithRegionAndCityAndStation();
        doMockForTariffWithRegionAndCityAndStation(isRegionExists, isCitiesExistForRegion, isReceivingStationsExists);
        assertThrows(NotFoundException.class,
            () -> superAdminService.deactivateTariffForChosenParam(details));
        verifyForTariffWithRegionAndCityAndStation(isRegionExists, isCitiesExistForRegion, isReceivingStationsExists);
        verify(deactivateTariffsForChosenParamRepository, never())
            .deactivateTariffsByRegionAndCitiesAndStations(anyLong(), anyList(), anyList());
    }

    static Stream<Arguments> deactivateTariffByNotExistingRegionOrCityOrStationProvider() {
        return Stream.of(
            arguments(false, true, true),
            arguments(false, false, true),
            arguments(false, true, false),
            arguments(true, false, true),
            arguments(true, false, false),
            arguments(true, true, false),
            arguments(false, false, false));
    }

    @Test
    void deactivateTariffByAllValidParams() {
        DetailsOfDeactivateTariffsDto details = ModelUtils.getDetailsOfDeactivateTariffsDtoWithAllParams();

        doMockForTariffWithAllParams(true, true, true, true);
        superAdminService.deactivateTariffForChosenParam(details);
        verifyForTariffWithAllParams(true, true, true, true);
    }

    @ParameterizedTest
    @MethodSource("deactivateTariffByAllWithNotExistingParamProvider")
    void deactivateTariffByAllWithNotExistingParamThrows(
        boolean isRegionExists,
        boolean isCitiesExistForRegion,
        boolean isReceivingStationsExists,
        boolean isCourierExists) {
        DetailsOfDeactivateTariffsDto details = ModelUtils.getDetailsOfDeactivateTariffsDtoWithAllParams();

        doMockForTariffWithAllParams(isRegionExists, isCitiesExistForRegion, isReceivingStationsExists,
            isCourierExists);
        assertThrows(NotFoundException.class,
            () -> superAdminService.deactivateTariffForChosenParam(details));
        verifyForTariffWithAllParams(isRegionExists, isCitiesExistForRegion, isReceivingStationsExists,
            isCourierExists);
        verify(deactivateTariffsForChosenParamRepository, never())
            .deactivateTariffsByAllParam(anyLong(), anyList(), anyList(), anyLong());
    }

    private static Stream<Arguments> deactivateTariffByAllWithNotExistingParamProvider() {
        return Stream.of(
            arguments(false, true, true, true),
            arguments(true, false, true, true),
            arguments(true, true, false, true),
            arguments(true, true, true, false),
            arguments(false, false, true, true),
            arguments(false, true, false, true),
            arguments(false, true, true, false),
            arguments(false, true, false, false),
            arguments(true, false, false, true),
            arguments(true, false, true, false),
            arguments(true, true, false, false),
            arguments(false, false, false, true),
            arguments(false, false, true, false),
            arguments(true, false, false, false),
            arguments(false, false, false, false));
    }

    @Test
    void deactivateTariffByAllEmptyParams() {
        DetailsOfDeactivateTariffsDto details = ModelUtils.getDetailsOfDeactivateTariffsDtoWithEmptyParams();

        assertThrows(BadRequestException.class,
            () -> superAdminService.deactivateTariffForChosenParam(details));
    }

    @ParameterizedTest
    @MethodSource("deactivateTariffByDifferentParamWithTwoRegionsProvider")
    void deactivateTariffByDifferentParamWithTwoRegionsThrows(DetailsOfDeactivateTariffsDto details) {
        assertThrows(BadRequestException.class,
            () -> superAdminService.deactivateTariffForChosenParam(details));
    }

    private static Stream<Arguments> deactivateTariffByDifferentParamWithTwoRegionsProvider() {
        return Stream.of(
            arguments(ModelUtils.getDetailsOfDeactivateTariffsDtoWithCourierAndRegion()
                .setRegionsId(Optional.of(List.of(1L, 2L)))),
            arguments(ModelUtils.getDetailsOfDeactivateTariffsDtoWithRegionAndCities()
                .setRegionsId(Optional.of(List.of(1L, 2L)))),
            arguments(ModelUtils.getDetailsOfDeactivateTariffsDtoWithRegionAndCityAndStation()
                .setRegionsId(Optional.of(List.of(1L, 2L)))),
            arguments(ModelUtils.getDetailsOfDeactivateTariffsDtoWithAllParams()
                .setRegionsId(Optional.of(List.of(1L, 2L)))));
    }

    private void doMockForTariffWithRegionAndCityAndStation(
        boolean isRegionExists,
        boolean isCitiesExistForRegion,
        boolean isReceivingStationsExists) {
        when(regionRepository.existsRegionById(anyLong())).thenReturn(isRegionExists);
        if (isRegionExists) {
            when(deactivateTariffsForChosenParamRepository
                .isCitiesExistForRegion(anyList(), anyLong())).thenReturn(isCitiesExistForRegion);
            if (isCitiesExistForRegion) {
                when(deactivateTariffsForChosenParamRepository
                    .isReceivingStationsExists(anyList())).thenReturn(isReceivingStationsExists);
            }
        }
    }

    private void verifyForTariffWithRegionAndCityAndStation(
        boolean isRegionExists,
        boolean isCitiesExistForRegion,
        boolean isReceivingStationsExists) {
        verify(regionRepository).existsRegionById(1L);
        if (isRegionExists) {
            verify(deactivateTariffsForChosenParamRepository).isCitiesExistForRegion(List.of(1L, 11L), 1L);
            if (isCitiesExistForRegion) {
                verify(deactivateTariffsForChosenParamRepository).isReceivingStationsExists(List.of(1L, 12L));
                if (isReceivingStationsExists) {
                    verify(deactivateTariffsForChosenParamRepository)
                        .deactivateTariffsByRegionAndCitiesAndStations(1L, List.of(1L, 11L), List.of(1L, 12L));
                }
            }
        }
    }

    private void doMockForTariffWithAllParams(
        boolean isRegionExists,
        boolean isCitiesExistForRegion,
        boolean isReceivingStationsExists,
        boolean isCourierExists) {
        when(regionRepository.existsRegionById(anyLong())).thenReturn(isRegionExists);
        if (isRegionExists) {
            when(deactivateTariffsForChosenParamRepository
                .isCitiesExistForRegion(anyList(), anyLong())).thenReturn(isCitiesExistForRegion);
            if (isCitiesExistForRegion) {
                when(deactivateTariffsForChosenParamRepository
                    .isReceivingStationsExists(anyList())).thenReturn(isReceivingStationsExists);
                if (isReceivingStationsExists) {
                    when(courierRepository.existsCourierById(anyLong())).thenReturn(isCourierExists);
                }
            }
        }
    }

    private void verifyForTariffWithAllParams(
        boolean isRegionExists,
        boolean isCitiesExistForRegion,
        boolean isReceivingStationsExists,
        boolean isCourierExists) {
        verify(regionRepository).existsRegionById(1L);
        if (isRegionExists) {
            verify(deactivateTariffsForChosenParamRepository).isCitiesExistForRegion(List.of(1L, 11L), 1L);
            if (isCitiesExistForRegion) {
                verify(deactivateTariffsForChosenParamRepository).isReceivingStationsExists(List.of(1L, 12L));
                if (isReceivingStationsExists) {
                    verify(courierRepository).existsCourierById(1L);
                    if (isCourierExists) {
                        verify(deactivateTariffsForChosenParamRepository)
                            .deactivateTariffsByAllParam(1L, List.of(1L, 11L), List.of(1L, 12L), 1L);
                    }
                }
            }
        }
    }
}