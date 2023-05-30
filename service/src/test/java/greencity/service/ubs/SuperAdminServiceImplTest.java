package greencity.service.ubs;

import greencity.ModelUtils;
import greencity.constant.ErrorMessage;
import greencity.dto.AddNewTariffDto;
import greencity.dto.DetailsOfDeactivateTariffsDto;
import greencity.dto.bag.BagLimitDto;
import greencity.dto.courier.AddingReceivingStationDto;
import greencity.dto.courier.CourierDto;
import greencity.dto.courier.CourierUpdateDto;
import greencity.dto.courier.CreateCourierDto;
import greencity.dto.courier.ReceivingStationDto;
import greencity.dto.location.EditLocationDto;
import greencity.dto.location.LocationCreateDto;
import greencity.dto.location.LocationInfoDto;
import greencity.dto.service.TariffServiceDto;
import greencity.dto.service.ServiceDto;
import greencity.dto.service.GetServiceDto;
import greencity.dto.tariff.*;
import greencity.dto.service.GetTariffServiceDto;
import greencity.entity.order.Bag;
import greencity.entity.order.Courier;
import greencity.entity.order.Service;
import greencity.entity.order.TariffLocation;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.Location;
import greencity.entity.user.Region;
import greencity.entity.user.employee.Employee;
import greencity.entity.user.employee.ReceivingStation;
import greencity.enums.CourierStatus;
import greencity.enums.LocationStatus;
import greencity.enums.StationStatus;
import greencity.enums.TariffStatus;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.UnprocessableEntityException;
import greencity.exceptions.courier.CourierAlreadyExists;
import greencity.exceptions.service.ServiceAlreadyExistsException;
import greencity.exceptions.tariff.TariffAlreadyExistsException;
import greencity.filters.TariffsInfoFilterCriteria;
import greencity.filters.TariffsInfoSpecification;
import greencity.repository.BagRepository;
import greencity.repository.CourierRepository;
import greencity.repository.DeactivateChosenEntityRepository;
import greencity.repository.EmployeeRepository;
import greencity.repository.LocationRepository;
import greencity.repository.ReceivingStationRepository;
import greencity.repository.RegionRepository;
import greencity.repository.ServiceRepository;
import greencity.repository.TariffLocationRepository;
import greencity.repository.TariffsInfoRepository;
import greencity.repository.UserRepository;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static greencity.ModelUtils.TEST_USER;
import static greencity.ModelUtils.getAllTariffsInfoDto;
import static greencity.ModelUtils.getCourier;
import static greencity.ModelUtils.getCourierDto;
import static greencity.ModelUtils.getCourierDtoList;
import static greencity.ModelUtils.getEmployee;
import static greencity.ModelUtils.getReceivingStation;
import static greencity.ModelUtils.getReceivingStationDto;
import static greencity.ModelUtils.getDeactivatedCourier;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        Bag bag = ModelUtils.getNewBag();
        Employee employee = ModelUtils.getEmployee();
        String uuid = UUID.randomUUID().toString();
        TariffServiceDto dto = ModelUtils.TariffServiceDto();
        GetTariffServiceDto responseDto = ModelUtils.getGetTariffServiceDto();
        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();

        when(employeeRepository.findByUuid(uuid)).thenReturn(Optional.of(employee));
        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffsInfo));
        when(bagRepository.save(bag)).thenReturn(bag);
        when(modelMapper.map(bag, GetTariffServiceDto.class)).thenReturn(responseDto);

        superAdminService.addTariffService(1L, dto, uuid);

        verify(employeeRepository).findByUuid(uuid);
        verify(tariffsInfoRepository).findById(1L);
        verify(bagRepository).save(bag);
        verify(modelMapper).map(bag, GetTariffServiceDto.class);
    }

    @Test
    void addTariffServiceIfEmployeeNotFoundExceptionTest() {
        String uuid = UUID.randomUUID().toString();
        TariffServiceDto dto = ModelUtils.TariffServiceDto();
        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();

        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffsInfo));
        when(employeeRepository.findByUuid(uuid)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
            () -> superAdminService.addTariffService(1L, dto, uuid));

        verify(tariffsInfoRepository).findById(1L);
        verify(employeeRepository).findByUuid(uuid);
        verify(bagRepository, never()).save(any(Bag.class));
    }

    @Test
    void addTariffServiceIfTariffNotFoundExceptionTest() {
        String uuid = UUID.randomUUID().toString();
        TariffServiceDto dto = ModelUtils.TariffServiceDto();

        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> superAdminService.addTariffService(1L, dto, uuid));

        verify(tariffsInfoRepository).findById(1L);
        verify(bagRepository, never()).save(any(Bag.class));
    }

    @Test
    void getTariffServiceTest() {
        List<Bag> bags = List.of(ModelUtils.getOptionalBag().get());
        GetTariffServiceDto dto = ModelUtils.getGetTariffServiceDto();

        when(tariffsInfoRepository.existsById(1L)).thenReturn(true);
        when(bagRepository.getAllByTariffsInfoId(1L)).thenReturn(bags);
        when(modelMapper.map(bags.get(0), GetTariffServiceDto.class)).thenReturn(dto);

        superAdminService.getTariffService(1);

        verify(tariffsInfoRepository).existsById(1L);
        verify(bagRepository).getAllByTariffsInfoId(1L);
        verify(modelMapper).map(bags.get(0), GetTariffServiceDto.class);
    }

    @Test
    void getTariffServiceIfTariffNotFoundException() {
        when(tariffsInfoRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class,
            () -> superAdminService.getTariffService(1));

        verify(tariffsInfoRepository).existsById(1L);
        verify(bagRepository, never()).getAllByTariffsInfoId(1L);
    }

    @Test
    void deleteTariffServiceWhenTariffBagsWithLimits() {
        Bag bag = ModelUtils.getBag();
        TariffsInfo tariffsInfo = ModelUtils.getTariffInfo();
        bag.setTariffsInfo(tariffsInfo);

        when(bagRepository.findById(1)).thenReturn(Optional.of(bag));
        doNothing().when(bagRepository).delete(bag);
        when(bagRepository.findBagsByTariffsInfoId(1L)).thenReturn(List.of(bag));
        superAdminService.deleteTariffService(1);
        assertEquals(TariffStatus.ACTIVE, tariffsInfo.getTariffStatus());
        verify(bagRepository).findById(1);
        verify(bagRepository).delete(bag);
        verify(bagRepository).findBagsByTariffsInfoId(1L);
        verify(tariffsInfoRepository, never()).save(tariffsInfo);
    }

    @Test
    void deleteTariffServiceWhenTariffBagsListIsEmpty() {
        Bag bag = ModelUtils.getBag();
        TariffsInfo tariffsInfo = ModelUtils.getTariffInfo();
        bag.setTariffsInfo(tariffsInfo);
        TariffsInfo tariffsInfoNew = ModelUtils.getTariffsInfoWithStatusNew();
        tariffsInfoNew.setBags(Collections.emptyList());

        when(bagRepository.findById(1)).thenReturn(Optional.of(bag));
        doNothing().when(bagRepository).delete(bag);
        when(bagRepository.findBagsByTariffsInfoId(1L)).thenReturn(Collections.emptyList());
        when(tariffsInfoRepository.save(tariffsInfo)).thenReturn(tariffsInfo);
        superAdminService.deleteTariffService(1);
        assertEquals(TariffStatus.NEW, tariffsInfoNew.getTariffStatus());
        verify(bagRepository).findById(1);
        verify(bagRepository).delete(bag);
        verify(bagRepository).findBagsByTariffsInfoId(1L);
        verify(tariffsInfoRepository).save(tariffsInfo);
    }

    @Test
    void deleteTariffServiceWhenTariffBagsWithoutLimits() {
        Bag bag = ModelUtils.getBag();
        TariffsInfo tariffsInfo = ModelUtils.getTariffInfo();
        bag.setLimitIncluded(false);
        bag.setTariffsInfo(tariffsInfo);
        TariffsInfo tariffsInfoNew = ModelUtils.getTariffsInfoWithStatusNew();
        tariffsInfoNew.setBags(Collections.emptyList());

        when(bagRepository.findById(1)).thenReturn(Optional.of(bag));
        doNothing().when(bagRepository).delete(bag);
        when(bagRepository.findBagsByTariffsInfoId(1L)).thenReturn(List.of(bag));
        when(tariffsInfoRepository.save(tariffsInfo)).thenReturn(tariffsInfoNew);
        superAdminService.deleteTariffService(1);
        assertEquals(TariffStatus.NEW, tariffsInfoNew.getTariffStatus());
        verify(bagRepository).findById(1);
        verify(bagRepository).delete(bag);
        verify(bagRepository).findBagsByTariffsInfoId(1L);
        verify(tariffsInfoRepository).save(tariffsInfoNew);
    }

    @Test
    void deleteTariffServiceThrowNotFoundException() {
        when(bagRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
            () -> superAdminService.deleteTariffService(1));
        verify(bagRepository).findById(1);
        verify(bagRepository, never()).delete(any(Bag.class));
    }

    @Test
    void editTariffService() {
        Bag bag = ModelUtils.getBag();
        Employee employee = ModelUtils.getEmployee();
        TariffServiceDto dto = ModelUtils.getTariffServiceDto();
        GetTariffServiceDto editedDto = ModelUtils.getGetTariffServiceDto();
        String uuid = UUID.randomUUID().toString();

        when(employeeRepository.findByUuid(uuid)).thenReturn(Optional.of(employee));
        when(bagRepository.findById(1)).thenReturn(Optional.of(bag));
        when(bagRepository.save(bag)).thenReturn(bag);
        when(modelMapper.map(bag, GetTariffServiceDto.class)).thenReturn(editedDto);

        superAdminService.editTariffService(dto, 1, uuid);

        verify(employeeRepository).findByUuid(uuid);
        verify(bagRepository).findById(1);
        verify(bagRepository).save(bag);
        verify(modelMapper).map(bag, GetTariffServiceDto.class);
    }

    @Test
    void editTariffServiceIfEmployeeNotFoundException() {
        TariffServiceDto dto = ModelUtils.getTariffServiceDto();
        Optional<Bag> bag = ModelUtils.getOptionalBag();
        String uuid = UUID.randomUUID().toString();

        when(bagRepository.findById(1)).thenReturn(bag);
        when(employeeRepository.findByUuid(uuid)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
            () -> superAdminService.editTariffService(dto, 1, uuid));

        verify(bagRepository).findById(1);
        verify(bagRepository, never()).save(any(Bag.class));
    }

    @Test
    void editTariffServiceIfBagNotFoundException() {
        TariffServiceDto dto = ModelUtils.getTariffServiceDto();
        String uuid = UUID.randomUUID().toString();

        when(bagRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
            () -> superAdminService.editTariffService(dto, 1, uuid));

        verify(bagRepository).findById(1);
        verify(bagRepository, never()).save(any(Bag.class));
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
    void deleteService() {
        Service service = ModelUtils.getService();

        when(serviceRepository.findById(service.getId())).thenReturn(Optional.of(service));

        superAdminService.deleteService(1L);

        verify(serviceRepository).findById(1L);
        verify(serviceRepository).delete(service);
    }

    @Test
    void deleteServiceThrowNotFoundException() {
        Service service = ModelUtils.getService();

        when(serviceRepository.findById(service.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> superAdminService.deleteService(1L));

        verify(serviceRepository).findById(1L);
        verify(serviceRepository, never()).delete(service);
    }

    @Test
    void getService() {
        Service service = ModelUtils.getService();
        GetServiceDto getServiceDto = ModelUtils.getGetServiceDto();

        when(tariffsInfoRepository.existsById(1L)).thenReturn(true);
        when(serviceRepository.findServiceByTariffsInfoId(1L)).thenReturn(Optional.of(service));
        when(modelMapper.map(service, GetServiceDto.class)).thenReturn(getServiceDto);

        assertEquals(getServiceDto, superAdminService.getService(1L));

        verify(tariffsInfoRepository).existsById(1L);
        verify(serviceRepository).findServiceByTariffsInfoId(1L);
        verify(modelMapper).map(service, GetServiceDto.class);
    }

    @Test
    void getServiceIfServiceNotExists() {
        when(tariffsInfoRepository.existsById(1L)).thenReturn(true);
        when(serviceRepository.findServiceByTariffsInfoId(1L)).thenReturn(Optional.empty());

        assertNull(superAdminService.getService(1L));

        verify(tariffsInfoRepository).existsById(1L);
        verify(serviceRepository).findServiceByTariffsInfoId(1L);
        verify(modelMapper, never()).map(any(Service.class), any(GetServiceDto.class));
    }

    @Test
    void getServiceThrowTariffNotFoundException() {
        when(tariffsInfoRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class,
            () -> superAdminService.getService(1L));

        verify(tariffsInfoRepository).existsById(1L);
    }

    @Test
    void editService() {
        Service service = ModelUtils.getEditedService();
        Employee employee = ModelUtils.getEmployee();
        ServiceDto dto = ModelUtils.getServiceDto();
        GetServiceDto getServiceDto = ModelUtils.getGetServiceDto();
        String uuid = UUID.randomUUID().toString();

        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
        when(employeeRepository.findByUuid(uuid)).thenReturn(Optional.of(employee));
        when(serviceRepository.save(service)).thenReturn(service);
        when(modelMapper.map(service, GetServiceDto.class)).thenReturn(getServiceDto);

        assertEquals(getServiceDto, superAdminService.editService(1L, dto, uuid));

        verify(serviceRepository).findById(1L);
        verify(employeeRepository).findByUuid(uuid);
        verify(serviceRepository).save(service);
        verify(modelMapper).map(service, GetServiceDto.class);
    }

    @Test
    void editServiceServiceNotFoundException() {
        ServiceDto dto = ModelUtils.getServiceDto();
        String uuid = UUID.randomUUID().toString();

        when(serviceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> superAdminService.editService(1L, dto, uuid));

        verify(serviceRepository).findById(1L);
        verify(serviceRepository, never()).save(any(Service.class));
        verify(modelMapper, never()).map(any(Service.class), any(GetServiceDto.class));
    }

    @Test
    void editServiceEmployeeNotFoundException() {
        Service service = ModelUtils.getEditedService();
        ServiceDto dto = ModelUtils.getServiceDto();
        String uuid = UUID.randomUUID().toString();

        when(serviceRepository.findById(1L)).thenReturn(Optional.of(service));
        when(employeeRepository.findByUuid(uuid)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> superAdminService.editService(1L, dto, uuid));

        verify(employeeRepository).findByUuid(uuid);
        verify(serviceRepository).findById(1L);
        verify(serviceRepository, never()).save(any(Service.class));
        verify(modelMapper, never()).map(any(Service.class), any(GetServiceDto.class));
    }

    @Test
    void addService() {
        Service createdService = ModelUtils.getService();
        Service service = ModelUtils.getNewService();
        Employee employee = ModelUtils.getEmployee();
        ServiceDto serviceDto = ModelUtils.getServiceDto();
        GetServiceDto getServiceDto = ModelUtils.getGetServiceDto();
        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();
        String uuid = UUID.randomUUID().toString();

        when(tariffsInfoRepository.existsById(1L)).thenReturn(true);
        when(serviceRepository.findServiceByTariffsInfoId(1L)).thenReturn(Optional.empty());
        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffsInfo));
        when(employeeRepository.findByUuid(uuid)).thenReturn(Optional.of(employee));
        when(serviceRepository.save(service)).thenReturn(createdService);
        when(modelMapper.map(createdService, GetServiceDto.class)).thenReturn(getServiceDto);

        assertEquals(getServiceDto, superAdminService.addService(1L, serviceDto, uuid));

        verify(employeeRepository).findByUuid(uuid);
        verify(tariffsInfoRepository).existsById(1L);
        verify(tariffsInfoRepository).findById(1L);
        verify(serviceRepository).findServiceByTariffsInfoId(1L);
        verify(serviceRepository).save(service);
        verify(modelMapper).map(createdService, GetServiceDto.class);
    }

    @Test
    void addServiceThrowServiceAlreadyExistsException() {
        Service createdService = ModelUtils.getService();
        ServiceDto serviceDto = ModelUtils.getServiceDto();
        String uuid = UUID.randomUUID().toString();

        when(tariffsInfoRepository.existsById(1L)).thenReturn(true);
        when(serviceRepository.findServiceByTariffsInfoId(1L)).thenReturn(Optional.of(createdService));

        assertThrows(ServiceAlreadyExistsException.class,
            () -> superAdminService.addService(1L, serviceDto, uuid));

        verify(tariffsInfoRepository).existsById(1L);
        verify(serviceRepository).findServiceByTariffsInfoId(1L);
    }

    @Test
    void addServiceThrowEmployeeNotFoundException() {
        ServiceDto serviceDto = ModelUtils.getServiceDto();
        String uuid = UUID.randomUUID().toString();

        when(tariffsInfoRepository.existsById(1L)).thenReturn(true);
        when(serviceRepository.findServiceByTariffsInfoId(1L)).thenReturn(Optional.empty());
        when(employeeRepository.findByUuid(uuid)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> superAdminService.addService(1L, serviceDto, uuid));

        verify(tariffsInfoRepository).existsById(1L);
        verify(serviceRepository).findServiceByTariffsInfoId(1L);
        verify(employeeRepository).findByUuid(uuid);
    }

    @Test
    void addServiceThrowTariffNotFoundException() {
        ServiceDto serviceDto = ModelUtils.getServiceDto();
        String uuid = UUID.randomUUID().toString();

        when(tariffsInfoRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class,
            () -> superAdminService.addService(1L, serviceDto, uuid));

        verify(tariffsInfoRepository).existsById(1L);
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
    void deactivateCourierTest() {
        Courier courier = getCourier();
        CourierDto courierDto = getCourierDto();
        when(courierRepository.findById(anyLong())).thenReturn(Optional.of(courier));
        when(modelMapper.map(courier, CourierDto.class)).thenReturn(courierDto);

        superAdminService.deactivateCourier(anyLong());
        courier.setCourierStatus(CourierStatus.DEACTIVATED);

        assertEquals(CourierStatus.DEACTIVATED, courier.getCourierStatus());
        verify(deactivateTariffsForChosenParamRepository).deactivateTariffsByCourier(anyLong());
        verify(courierRepository, times(1)).findById(anyLong());
        verify(modelMapper, times(1)).map(courier, CourierDto.class);
    }

    @Test
    void deactivateCourierThrowBadRequestException() {
        Courier courier = getCourier();
        when(courierRepository.findById(anyLong())).thenReturn(Optional.of(courier));
        courier.setCourierStatus(CourierStatus.DEACTIVATED);
        assertThrows(BadRequestException.class,
            () -> superAdminService.deactivateCourier(1L));
        verify(courierRepository).findById(1L);
    }

    @Test
    void deactivateCourierThrowNotFoundException() {
        when(courierRepository.findById(anyLong()))
            .thenReturn(Optional.empty());

        Exception thrownNotFoundEx = assertThrows(NotFoundException.class,
            () -> superAdminService.deactivateCourier(1L));

        assertEquals(ErrorMessage.COURIER_IS_NOT_FOUND_BY_ID + 1L, thrownNotFoundEx.getMessage());
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
        when(employeeRepository.findAllByEmployeePositionId(6L)).thenReturn(ModelUtils.getEmployeeList());
        when(tariffsLocationRepository.saveAll(anySet())).thenReturn(anyList());

        superAdminService.addNewTariff(dto, "35467585763t4sfgchjfuyetf");

        verify(tariffsInfoRepository, times(2)).save(any());
        verify(tariffsLocationRepository).saveAll(anySet());
        verify(employeeRepository).findAllByEmployeePositionId(6L);
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
        verify(employeeRepository, never()).findAllByEmployeePositionId(6L);
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
        verify(employeeRepository, never()).findAllByEmployeePositionId(6L);
    }

    @Test
    void addNewTariffThrowsExceptionWhenCourierHasStatusDeactivated() {
        AddNewTariffDto addNewTariffDto = ModelUtils.getAddNewTariffDto();
        String userUUID = "35467585763t4sfgchjfuyetf";
        Courier courier = ModelUtils.getDeactivatedCourier();

        // Mock the necessary dependencies
        when(courierRepository.findById(addNewTariffDto.getCourierId())).thenReturn(Optional.of(courier));

        // Perform the test
        assertThrows(BadRequestException.class,
            () -> superAdminService.addNewTariff(addNewTariffDto, userUUID));

        // Verify the interactions
        verify(courierRepository).findById(addNewTariffDto.getCourierId());
        verifyNoMoreInteractions(courierRepository, tariffsLocationRepository, tariffsInfoRepository,
            employeeRepository);
    }

    @Test
    void addNewTariffThrowsExceptionWhenListOfReceivingStationIsEmpty() {
        AddNewTariffDto dto = ModelUtils.getAddNewTariffDto();
        dto.setReceivingStationsIdList(null);

        when(courierRepository.findById(1L)).thenReturn(Optional.of(ModelUtils.getCourier()));
        when(employeeRepository.findByUuid("35467585763t4sfgchjfuyetf")).thenReturn(Optional.ofNullable(getEmployee()));
        when(tariffsInfoRepository.save(any())).thenReturn(ModelUtils.getTariffInfo());
        when(tariffsLocationRepository.findAllByCourierIdAndLocationIds(1L, List.of(1L)))
            .thenReturn(Collections.emptyList());
        when(locationRepository.findAllByIdAndRegionId(dto.getLocationIdList(),
            dto.getRegionId())).thenReturn(Collections.emptyList());

        assertThrows(NotFoundException.class,
            () -> superAdminService.addNewTariff(dto, "35467585763t4sfgchjfuyetf"));

        verify(courierRepository).findById(1L);
        verify(employeeRepository).findByUuid("35467585763t4sfgchjfuyetf");
        verify(tariffsInfoRepository, times(1)).save(any());
        verify(tariffsLocationRepository).findAllByCourierIdAndLocationIds(1L, List.of(1L));
        verify(locationRepository).findAllByIdAndRegionId(dto.getLocationIdList(), dto.getRegionId());
        verify(employeeRepository, never()).findAllByEmployeePositionId(6L);
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
        verify(employeeRepository, never()).findAllByEmployeePositionId(6L);
    }

    @Test
    void addNewTariffThrowsException3() {
        AddNewTariffDto dto = ModelUtils.getAddNewTariffDto();
        when(courierRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
            () -> superAdminService.addNewTariff(dto, "35467585763t4sfgchjfuyetf"));
        verify(employeeRepository, never()).findAllByEmployeePositionId(6L);
    }

    @Test
    void editTariffTest() {
        EditTariffDto dto = ModelUtils.getEditTariffDto();
        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();
        TariffLocation tariffLocation = ModelUtils.getTariffLocation();
        ReceivingStation receivingStation = ModelUtils.getReceivingStation();
        Location location = ModelUtils.getLocation();
        Courier courier = getCourier();

        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));
        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffsInfo));
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(tariffsLocationRepository.findAllByCourierIdAndLocationIds(1L, List.of(1L)))
            .thenReturn(List.of(tariffLocation));
        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(receivingStation));
        when(tariffsLocationRepository.findTariffLocationByTariffsInfoAndLocation(tariffsInfo, location))
            .thenReturn(Optional.of(tariffLocation));
        when(tariffsLocationRepository.findAllByTariffsInfo(tariffsInfo)).thenReturn(List.of(tariffLocation));
        when(tariffsInfoRepository.save(tariffsInfo)).thenReturn(tariffsInfo);

        superAdminService.editTariff(1L, dto);

        verify(tariffsInfoRepository).findById(1L);
        verify(locationRepository).findById(1L);
        verify(tariffsLocationRepository).findAllByCourierIdAndLocationIds(1L, List.of(1L));
        verify(receivingStationRepository).findById(1L);
        verify(tariffsLocationRepository).findTariffLocationByTariffsInfoAndLocation(tariffsInfo, location);
        verify(tariffsLocationRepository).findAllByTariffsInfo(tariffsInfo);
        verify(tariffsInfoRepository).save(tariffsInfo);
        verify(courierRepository).findById(1L);
    }

    @Test
    void editTariffWithDeleteTariffLocation() {
        EditTariffDto dto = ModelUtils.getEditTariffDto();
        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();
        TariffLocation tariffLocation = ModelUtils.getTariffLocation();
        ReceivingStation receivingStation = ModelUtils.getReceivingStation();
        Location location = ModelUtils.getLocation();
        List<TariffLocation> tariffLocations = ModelUtils.getTariffLocationList();
        Courier courier = getCourier();

        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));
        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffsInfo));
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(tariffsLocationRepository.findAllByCourierIdAndLocationIds(1L, List.of(1L)))
            .thenReturn(List.of(tariffLocation));
        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(receivingStation));
        when(tariffsLocationRepository.findTariffLocationByTariffsInfoAndLocation(tariffsInfo, location))
            .thenReturn(Optional.of(tariffLocation));
        when(tariffsLocationRepository.findAllByTariffsInfo(tariffsInfo)).thenReturn(tariffLocations);
        doNothing().when(tariffsLocationRepository).delete(tariffLocations.get(1));
        when(tariffsInfoRepository.save(tariffsInfo)).thenReturn(tariffsInfo);

        superAdminService.editTariff(1L, dto);

        verify(tariffsInfoRepository).findById(1L);
        verify(locationRepository).findById(1L);
        verify(tariffsLocationRepository).findAllByCourierIdAndLocationIds(1L, List.of(1L));
        verify(receivingStationRepository).findById(1L);
        verify(tariffsLocationRepository).findTariffLocationByTariffsInfoAndLocation(tariffsInfo, location);
        verify(tariffsLocationRepository).findAllByTariffsInfo(tariffsInfo);
        verify(tariffsLocationRepository).delete(tariffLocations.get(1));
        verify(tariffsInfoRepository).save(tariffsInfo);
        verify(courierRepository).findById(1L);
    }

    @Test
    void editTariffThrowTariffNotFoundException() {
        EditTariffDto dto = ModelUtils.getEditTariffDto();

        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
            () -> superAdminService.editTariff(1L, dto));
        verify(tariffsInfoRepository).findById(1L);
    }

    @Test
    void editTariffThrowLocationNotFoundException() {
        EditTariffDto dto = ModelUtils.getEditTariffDto();
        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();

        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffsInfo));
        when(locationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> superAdminService.editTariff(1L, dto));

        verify(tariffsInfoRepository).findById(1L);
        verify(locationRepository).findById(1L);
    }

    @Test
    void editTariffThrowLocationBadRequestException() {
        EditTariffDto dto = ModelUtils.getEditTariffDtoWith2Locations();
        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();
        List<Location> locations = ModelUtils.getLocationList2();

        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffsInfo));
        when(locationRepository.findById(1L)).thenReturn(Optional.of(locations.get(0)));
        when(locationRepository.findById(2L)).thenReturn(Optional.of(locations.get(1)));

        assertThrows(BadRequestException.class,
            () -> superAdminService.editTariff(1L, dto));

        verify(tariffsInfoRepository).findById(1L);
        verify(locationRepository).findById(1L);
        verify(locationRepository).findById(2L);
    }

    @Test
    void editTariffThrowTariffAlreadyExistsException() {
        EditTariffDto dto = ModelUtils.getEditTariffDto();
        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();
        Location location = ModelUtils.getLocation();
        TariffLocation tariffLocation = ModelUtils.getTariffLocation2();
        Courier courier = getCourier();

        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));
        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffsInfo));
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(tariffsLocationRepository.findAllByCourierIdAndLocationIds(1L, List.of(1L)))
            .thenReturn(List.of(tariffLocation));

        assertThrows(TariffAlreadyExistsException.class,
            () -> superAdminService.editTariff(1L, dto));

        verify(tariffsInfoRepository).findById(1L);
        verify(locationRepository).findById(1L);
        verify(tariffsLocationRepository).findAllByCourierIdAndLocationIds(1L, List.of(1L));
        verify(courierRepository).findById(1L);
    }

    @Test
    void editTariffThrowReceivingStationNotFoundException() {
        EditTariffDto dto = ModelUtils.getEditTariffDto();
        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();
        Location location = ModelUtils.getLocation();
        TariffLocation tariffLocation = ModelUtils.getTariffLocation();
        Courier courier = getCourier();

        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));
        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffsInfo));
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(tariffsLocationRepository.findAllByCourierIdAndLocationIds(1L, List.of(1L)))
            .thenReturn(List.of(tariffLocation));
        when(receivingStationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> superAdminService.editTariff(1L, dto));

        verify(tariffsInfoRepository).findById(1L);
        verify(locationRepository).findById(1L);
        verify(tariffsLocationRepository).findAllByCourierIdAndLocationIds(1L, List.of(1L));
        verify(receivingStationRepository).findById(1L);
        verify(courierRepository).findById(1L);
    }

    @Test
    void editTariffThrowsCourierNotFoundException() {
        EditTariffDto dto = ModelUtils.getEditTariffDto();
        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();
        Location location = ModelUtils.getLocation();

        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffsInfo));
        when(courierRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> superAdminService.editTariff(1L, dto));

        verify(tariffsInfoRepository).findById(1L);
        verify(courierRepository).findById(1L);
        verify(locationRepository).findById(1L);
    }

    @Test
    void editTariffWithoutCourier() {
        EditTariffDto dto = ModelUtils.getEditTariffDtoWithoutCourier();
        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();
        Location location = ModelUtils.getLocation();
        TariffLocation tariffLocation = ModelUtils.getTariffLocation();

        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffsInfo));
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(tariffsLocationRepository.findAllByCourierIdAndLocationIds(1L, List.of(1L)))
            .thenReturn(List.of(tariffLocation));
        when(receivingStationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> superAdminService.editTariff(1L, dto));

        verify(tariffsInfoRepository).findById(1L);
        verify(locationRepository).findById(1L);
        verify(tariffsLocationRepository).findAllByCourierIdAndLocationIds(1L, List.of(1L));
        verify(receivingStationRepository).findById(1L);
    }

    @Test
    void editTariffThrowsCourierHasStatusDeactivatedException() {
        EditTariffDto dto = ModelUtils.getEditTariffDto();
        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();
        Location location = ModelUtils.getLocation();
        Courier courier = getDeactivatedCourier();

        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffsInfo));
        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));

        assertThrows(BadRequestException.class,
            () -> superAdminService.editTariff(1L, dto));

        verify(tariffsInfoRepository).findById(1L);
        verify(courierRepository).findById(1L);
        verify(locationRepository).findById(1L);
    }

    @Test
    void editTariffWithBuildTariffLocation() {
        EditTariffDto dto = ModelUtils.getEditTariffDto();
        TariffsInfo tariffsInfo = ModelUtils.getTariffsInfo();
        Location location = ModelUtils.getLocation();
        TariffLocation tariffLocation = ModelUtils.getTariffLocation();
        ReceivingStation receivingStation = ModelUtils.getReceivingStation();
        Courier courier = getCourier();

        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));
        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffsInfo));
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(tariffsLocationRepository.findAllByCourierIdAndLocationIds(1L, List.of(1L)))
            .thenReturn(List.of(tariffLocation));
        when(receivingStationRepository.findById(1L)).thenReturn(Optional.of(receivingStation));
        when(tariffsLocationRepository.findTariffLocationByTariffsInfoAndLocation(tariffsInfo, location))
            .thenReturn(Optional.empty());
        when(tariffsLocationRepository.findAllByTariffsInfo(tariffsInfo)).thenReturn(List.of(tariffLocation));
        when(tariffsInfoRepository.save(tariffsInfo)).thenReturn(tariffsInfo);

        superAdminService.editTariff(1L, dto);

        verify(tariffsInfoRepository).findById(1L);
        verify(locationRepository).findById(1L);
        verify(tariffsLocationRepository).findAllByCourierIdAndLocationIds(1L, List.of(1L));
        verify(receivingStationRepository).findById(1L);
        verify(tariffsLocationRepository).findTariffLocationByTariffsInfoAndLocation(tariffsInfo, location);
        verify(tariffsLocationRepository).findAllByTariffsInfo(tariffsInfo);
        verify(tariffsInfoRepository).save(tariffsInfo);
        verify(courierRepository).findById(1L);
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
    void setTariffLimitsWithAmountOfBags() {
        TariffsInfo tariffInfo = ModelUtils.getTariffsInfo();
        Bag bag = ModelUtils.getTariffBag();
        SetTariffLimitsDto dto = ModelUtils.setTariffLimitsWithAmountOfBags();

        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffInfo));
        when(bagRepository.findById(1)).thenReturn(Optional.of(bag));
        when(tariffsInfoRepository.save(tariffInfo)).thenReturn(tariffInfo);
        when(bagRepository.saveAll(List.of(bag))).thenReturn(List.of(bag));

        superAdminService.setTariffLimits(1L, dto);

        verify(tariffsInfoRepository).findById(1L);
        verify(bagRepository).findById(1);
        verify(tariffsInfoRepository).save(any());
        verify(bagRepository).saveAll(List.of(bag));
    }

    @Test
    void setTariffLimitsWithPriceOfOrder() {
        TariffsInfo tariffInfo = ModelUtils.getTariffInfo();
        Bag bag = ModelUtils.getTariffBag();
        SetTariffLimitsDto dto = ModelUtils.setTariffLimitsWithPriceOfOrder();

        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffInfo));
        when(bagRepository.findById(1)).thenReturn(Optional.of(bag));
        when(tariffsInfoRepository.save(tariffInfo)).thenReturn(tariffInfo);
        when(bagRepository.saveAll(List.of(bag))).thenReturn(List.of(bag));

        superAdminService.setTariffLimits(1L, dto);

        verify(tariffsInfoRepository).findById(1L);
        verify(bagRepository).findById(1);
        verify(tariffsInfoRepository).save(any());
        verify(bagRepository).saveAll(List.of(bag));
    }

    @Test
    void setTariffLimitsWithNullMinAndMaxAndFalseBagLimitIncluded() {
        TariffsInfo tariffInfo = ModelUtils.getTariffInfo();
        Bag bag = ModelUtils.getTariffBag();
        SetTariffLimitsDto dto = ModelUtils.setTariffLimitsWithNullMinAndMaxAndFalseBagLimit();

        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffInfo));
        when(bagRepository.findById(1)).thenReturn(Optional.of(bag));
        when(tariffsInfoRepository.save(tariffInfo)).thenReturn(tariffInfo);
        when(bagRepository.saveAll(List.of(bag))).thenReturn(List.of(bag));

        superAdminService.setTariffLimits(1L, dto);

        verify(tariffsInfoRepository).findById(1L);
        verify(bagRepository).findById(1);
        verify(tariffsInfoRepository).save(any());
        verify(bagRepository).saveAll(List.of(bag));
    }

    @Test
    void setTariffLimitsIfBagNotBelongToTariff() {
        TariffsInfo tariffInfo = ModelUtils.getTariffInfo();
        SetTariffLimitsDto dto = ModelUtils.setTariffLimitsWithAmountOfBags();
        Bag bag = ModelUtils.getTariffBag();
        tariffInfo.setId(2L);

        when(tariffsInfoRepository.findById(2L)).thenReturn(Optional.of(tariffInfo));
        when(bagRepository.findById(1)).thenReturn(Optional.of(bag));

        assertThrows(BadRequestException.class,
            () -> superAdminService.setTariffLimits(2L, dto));

        verify(tariffsInfoRepository).findById(2L);
        verify(bagRepository).findById(1);
    }

    @Test
    void setTariffLimitsWithNullMaxAndTrueBagLimitIncluded() {
        TariffsInfo tariffInfo = ModelUtils.getTariffInfo();
        SetTariffLimitsDto dto = ModelUtils.setTariffLimitsWithAmountOfBags();
        Bag bag = ModelUtils.getTariffBag();
        dto.setMax(null);

        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffInfo));
        when(bagRepository.findById(1)).thenReturn(Optional.of(bag));
        when(tariffsInfoRepository.save(tariffInfo)).thenReturn(tariffInfo);
        when(bagRepository.saveAll(List.of(bag))).thenReturn(List.of(bag));

        superAdminService.setTariffLimits(1L, dto);

        verify(tariffsInfoRepository).findById(1L);
        verify(bagRepository).findById(1);
        verify(tariffsInfoRepository).save(any());
        verify(bagRepository).saveAll(List.of(bag));
    }

    @Test
    void setTariffLimitsWithNullMinAndTrueBagLimitIncluded() {
        TariffsInfo tariffInfo = ModelUtils.getTariffInfo();
        SetTariffLimitsDto dto = ModelUtils.setTariffLimitsWithAmountOfBags();
        Bag bag = ModelUtils.getTariffBag();
        dto.setMin(null);

        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffInfo));
        when(bagRepository.findById(1)).thenReturn(Optional.of(bag));
        when(tariffsInfoRepository.save(tariffInfo)).thenReturn(tariffInfo);
        when(bagRepository.saveAll(List.of(bag))).thenReturn(List.of(bag));

        superAdminService.setTariffLimits(1L, dto);

        verify(tariffsInfoRepository).findById(1L);
        verify(bagRepository).findById(1);
        verify(tariffsInfoRepository).save(any());
        verify(bagRepository).saveAll(List.of(bag));
    }

    @Test
    void setTariffLimitsWithNullCourierLimitAndTrueBagLimitIncluded() {
        TariffsInfo tariffInfo = ModelUtils.getTariffInfo();
        SetTariffLimitsDto dto = ModelUtils.setTariffLimitsWithAmountOfBags();
        dto.setCourierLimit(null);

        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffInfo));

        assertThrows(BadRequestException.class,
            () -> superAdminService.setTariffLimits(1L, dto));

        verify(tariffsInfoRepository).findById(1L);
    }

    @Test
    void setTariffLimitsWithNullAllParamsAndTrueBagLimitIncluded() {
        TariffsInfo tariffInfo = ModelUtils.getTariffInfo();
        SetTariffLimitsDto dto = ModelUtils.setTariffLimitsWithNullAllTariffParamsAndFalseBagLimit();
        List<BagLimitDto> bagDto = List.of(ModelUtils.getBagLimitIncludedDtoTrue());
        dto.setBagLimitDtoList(bagDto);

        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffInfo));

        assertThrows(BadRequestException.class,
            () -> superAdminService.setTariffLimits(1L, dto));

        verify(tariffsInfoRepository).findById(1L);
    }

    @Test
    void setTariffLimitsWithNotNullAllParamsAndFalseBagLimitIncluded() {
        TariffsInfo tariffInfo = ModelUtils.getTariffInfo();
        SetTariffLimitsDto dto = ModelUtils.setTariffLimitsWithAmountOfBags();
        List<BagLimitDto> bagDto = List.of(ModelUtils.getBagLimitIncludedDtoFalse());
        dto.setBagLimitDtoList(bagDto);

        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffInfo));

        assertThrows(BadRequestException.class,
            () -> superAdminService.setTariffLimits(1L, dto));

        verify(tariffsInfoRepository).findById(1L);
    }

    @Test
    void setTariffsLimitWithSameMinAndMaxValue() {
        SetTariffLimitsDto dto = ModelUtils.setTariffsLimitWithSameMinAndMaxValue();
        TariffsInfo tariffInfo = ModelUtils.getTariffInfo();

        when(tariffsInfoRepository.findById(anyLong())).thenReturn(Optional.of(tariffInfo));

        assertThrows(BadRequestException.class,
            () -> superAdminService.setTariffLimits(1L, dto));

        verify(tariffsInfoRepository).findById(1L);
    }

    @Test
    void setTariffLimitsWithPriceOfOrderMaxValueIsGreaterThanMin() {
        SetTariffLimitsDto dto = ModelUtils.setTariffLimitsWithPriceOfOrderWhereMaxValueIsGreater();
        TariffsInfo tariffInfo = ModelUtils.getTariffInfo();

        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffInfo));

        assertThrows(BadRequestException.class,
            () -> superAdminService.setTariffLimits(1L, dto));

        verify(tariffsInfoRepository).findById(1L);
    }

    @Test
    void setTariffLimitsWithAmountOfBigBagMaxValueIsGreaterThanMin() {
        SetTariffLimitsDto dto = ModelUtils.setTariffLimitsWithAmountOfBigBagsWhereMaxValueIsGreater();
        TariffsInfo tariffInfo = ModelUtils.getTariffInfo();

        when(tariffsInfoRepository.findById(anyLong())).thenReturn(Optional.of(tariffInfo));

        assertThrows(BadRequestException.class,
            () -> superAdminService.setTariffLimits(1L, dto));

        verify(tariffsInfoRepository).findById(1L);
    }

    @Test
    void setTariffLimitsBagThrowTariffsInfoNotFound() {
        SetTariffLimitsDto dto = ModelUtils.setTariffLimitsWithAmountOfBags();

        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> superAdminService.setTariffLimits(1L, dto));

        verify(tariffsInfoRepository).findById(1L);
    }

    @Test
    void setTariffLimitsBagThrowBagNotFound() {
        SetTariffLimitsDto dto = ModelUtils.setTariffLimitsWithAmountOfBags();
        TariffsInfo tariffInfo = ModelUtils.getTariffInfo();

        when(tariffsInfoRepository.findById(anyLong())).thenReturn(Optional.of(tariffInfo));
        when(bagRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> superAdminService.setTariffLimits(1L, dto));

        verify(tariffsInfoRepository).findById(1L);
        verify(bagRepository).findById(1);
    }

    @Test
    void getTariffLimitsTest() {
        TariffsInfo tariffInfo = ModelUtils.getTariffsInfo();
        GetTariffLimitsDto dto = ModelUtils.getGetTariffLimitsDto();

        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffInfo));
        when(modelMapper.map(tariffInfo, GetTariffLimitsDto.class)).thenReturn(dto);

        superAdminService.getTariffLimits(1L);

        verify(tariffsInfoRepository).findById(1L);
        verify(modelMapper).map(tariffInfo, GetTariffLimitsDto.class);
    }

    @Test
    void getTariffLimitsThrowNotFoundException() {
        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> superAdminService.getTariffLimits(1L));

        verify(tariffsInfoRepository).findById(1L);
    }

    @Test
    void switchTariffStatusToDeactivated() {
        TariffsInfo tariffInfo = ModelUtils.getTariffsInfoActive();

        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffInfo));
        when(tariffsInfoRepository.save(tariffInfo)).thenReturn(tariffInfo);

        superAdminService.switchTariffStatus(1L, "Deactivated");

        verify(tariffsInfoRepository).findById(1L);
        verify(tariffsInfoRepository).save(tariffInfo);
    }

    @Test
    void switchTariffStatusToActive() {
        TariffsInfo tariffInfo = ModelUtils.getTariffsInfoDeactivated();

        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffInfo));
        when(tariffsInfoRepository.save(tariffInfo)).thenReturn(tariffInfo);

        superAdminService.switchTariffStatus(1L, "Active");

        verify(tariffsInfoRepository).findById(1L);
        verify(tariffsInfoRepository).save(tariffInfo);
    }

    @Test
    void switchTariffStatusToActiveWithMaxIsNull() {
        TariffsInfo tariffInfo = ModelUtils.getTariffsInfoDeactivated();
        tariffInfo.setMax(null);

        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffInfo));
        when(tariffsInfoRepository.save(tariffInfo)).thenReturn(tariffInfo);

        superAdminService.switchTariffStatus(1L, "Active");

        verify(tariffsInfoRepository).findById(1L);
        verify(tariffsInfoRepository).save(tariffInfo);
    }

    @Test
    void switchTariffStatusFromWhenCourierDeactivatedThrowBadRequestException() {
        TariffsInfo tariffInfo = ModelUtils.getTariffsInfoDeactivated();
        tariffInfo.setCourier(ModelUtils.getDeactivatedCourier());

        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffInfo));

        Throwable t = assertThrows(BadRequestException.class,
            () -> superAdminService.switchTariffStatus(1L, "Active"));
        assertEquals(String.format(ErrorMessage.TARIFF_ACTIVATION_RESTRICTION_DUE_TO_DEACTIVATED_COURIER +
            tariffInfo.getCourier().getId()),
            t.getMessage());

        verify(tariffsInfoRepository).findById(1L);
        verify(tariffsInfoRepository, never()).save(tariffInfo);
    }

    @Test
    void switchTariffStatusToActiveWithMinIsNull() {
        TariffsInfo tariffInfo = ModelUtils.getTariffsInfoDeactivated();
        tariffInfo.setMin(null);

        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffInfo));
        when(tariffsInfoRepository.save(tariffInfo)).thenReturn(tariffInfo);

        superAdminService.switchTariffStatus(1L, "Active");

        verify(tariffsInfoRepository).findById(1L);
        verify(tariffsInfoRepository).save(tariffInfo);
    }

    @Test
    void switchTariffStatusThrowNotFoundException() {
        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> superAdminService.switchTariffStatus(1L, "Active"));

        verify(tariffsInfoRepository).findById(1L);
        verify(tariffsInfoRepository, never()).save(any(TariffsInfo.class));
    }

    @Test
    void switchTariffStatusFromActiveToActiveThrowBadRequestException() {
        TariffsInfo tariffInfo = ModelUtils.getTariffsInfoActive();

        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffInfo));

        Throwable t = assertThrows(BadRequestException.class,
            () -> superAdminService.switchTariffStatus(1L, "Active"));
        assertEquals(String.format(ErrorMessage.TARIFF_ALREADY_HAS_THIS_STATUS, 1L, TariffStatus.ACTIVE),
            t.getMessage());

        verify(tariffsInfoRepository).findById(1L);
        verify(tariffsInfoRepository, never()).save(tariffInfo);
    }

    @Test
    void switchTariffStatusToActiveWithoutBagThrowBadRequestException() {
        TariffsInfo tariffInfo = ModelUtils.getTariffsInfoDeactivated();
        tariffInfo.setBags(Collections.emptyList());

        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffInfo));

        Throwable t = assertThrows(BadRequestException.class,
            () -> superAdminService.switchTariffStatus(1L, "Active"));
        assertEquals(ErrorMessage.TARIFF_ACTIVATION_RESTRICTION_DUE_TO_UNSPECIFIED_BAGS,
            t.getMessage());

        verify(tariffsInfoRepository).findById(1L);
        verify(tariffsInfoRepository, never()).save(tariffInfo);
    }

    @Test
    void switchTariffStatusWithUnresolvableStatusThrowBadRequestException() {
        TariffsInfo tariffInfo = ModelUtils.getTariffsInfoDeactivated();
        tariffInfo.setBags(Collections.emptyList());

        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffInfo));

        Throwable t = assertThrows(BadRequestException.class,
            () -> superAdminService.switchTariffStatus(1L, "new"));
        assertEquals(ErrorMessage.UNRESOLVABLE_TARIFF_STATUS, t.getMessage());

        verify(tariffsInfoRepository).findById(1L);
        verify(tariffsInfoRepository, never()).save(tariffInfo);
    }

    @Test
    void switchTariffStatusToActiveWithMinAndMaxNullThrowBadRequestException() {
        TariffsInfo tariffInfo = ModelUtils.getTariffsInfoDeactivated();
        tariffInfo.setMax(null);
        tariffInfo.setMin(null);

        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffInfo));

        Throwable t = assertThrows(BadRequestException.class,
            () -> superAdminService.switchTariffStatus(1L, "Active"));
        assertEquals(ErrorMessage.TARIFF_ACTIVATION_RESTRICTION_DUE_TO_UNSPECIFIED_LIMITS,
            t.getMessage());

        verify(tariffsInfoRepository).findById(1L);
        verify(tariffsInfoRepository, never()).save(tariffInfo);
    }

    @Test
    void switchTariffStatusToActiveWithoutService() {
        TariffsInfo tariffInfo = ModelUtils.getTariffsInfoDeactivated();
        tariffInfo.setService(null);

        when(tariffsInfoRepository.findById(1L)).thenReturn(Optional.of(tariffInfo));
        when(tariffsInfoRepository.save(tariffInfo)).thenReturn(tariffInfo);

        superAdminService.switchTariffStatus(1L, "Active");

        verify(tariffsInfoRepository).findById(1L);
        verify(tariffsInfoRepository).save(tariffInfo);
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
        details.setRegionsIds(Optional.of(List.of(1L, 2L)));

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
    @MethodSource("deactivateTariffByNotExistingSomeOfTreeParameters")
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

    static Stream<Arguments> deactivateTariffByNotExistingSomeOfTreeParameters() {
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

    @Test
    void deactivateTariffByCities() {
        DetailsOfDeactivateTariffsDto details = ModelUtils.getDetailsOfDeactivateTariffsDtoWithCities();

        assertThrows(BadRequestException.class,
            () -> superAdminService.deactivateTariffForChosenParam(details));
    }

    @Test
    void deactivateTariffByCitiesAndCourier() {
        DetailsOfDeactivateTariffsDto details = ModelUtils.getDetailsOfDeactivateTariffsDtoWithCitiesAndCourier();

        assertThrows(BadRequestException.class,
            () -> superAdminService.deactivateTariffForChosenParam(details));
    }

    @Test
    void deactivateTariffByCitiesAndReceivingStations() {
        DetailsOfDeactivateTariffsDto details =
            ModelUtils.getDetailsOfDeactivateTariffsDtoWithCitiesAndReceivingStations();

        assertThrows(BadRequestException.class,
            () -> superAdminService.deactivateTariffForChosenParam(details));
    }

    @Test
    void deactivateTariffByCitiesAndCourierAndReceivingStations() {
        DetailsOfDeactivateTariffsDto details =
            ModelUtils.getDetailsOfDeactivateTariffsDtoWithCitiesAndCourierAndReceivingStations();

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
                .setRegionsIds(Optional.of(List.of(1L, 2L)))),
            arguments(ModelUtils.getDetailsOfDeactivateTariffsDtoWithRegionAndCities()
                .setRegionsIds(Optional.of(List.of(1L, 2L)))),
            arguments(ModelUtils.getDetailsOfDeactivateTariffsDtoWithRegionAndCityAndStation()
                .setRegionsIds(Optional.of(List.of(1L, 2L)))),
            arguments(ModelUtils.getDetailsOfDeactivateTariffsDtoWithAllParams()
                .setRegionsIds(Optional.of(List.of(1L, 2L)))),
            arguments(ModelUtils.getDetailsOfDeactivateTariffsDtoWithRegionAndReceivingStations()
                .setRegionsIds(Optional.of(List.of(1L, 2L)))),
            arguments(ModelUtils.getDetailsOfDeactivateTariffsDtoWithCourierAndRegionAndCities()
                .setRegionsIds(Optional.of(List.of(1L, 2L)))),
            arguments(ModelUtils.getDetailsOfDeactivateTariffsDtoWithCourierAndRegionAndReceivingStations()
                .setRegionsIds(Optional.of(List.of(1L, 2L)))));
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

    @Test
    void deactivateTariffByRegionsAndReceivingStations() {
        DetailsOfDeactivateTariffsDto details =
            ModelUtils.getDetailsOfDeactivateTariffsDtoWithRegionAndReceivingStations();

        doMockForTariffWithRegionAndStation(true, true);
        superAdminService.deactivateTariffForChosenParam(details);
        verifyForTariffWithRegionAndStation(true, true);
    }

    private void doMockForTariffWithRegionAndStation(
        boolean isRegionExists,
        boolean isReceivingStationsExists) {
        when(regionRepository.existsRegionById(anyLong())).thenReturn(isRegionExists);
        if (isRegionExists) {
            when(deactivateTariffsForChosenParamRepository
                .isReceivingStationsExists(anyList())).thenReturn(isReceivingStationsExists);
        }
    }

    private void verifyForTariffWithRegionAndStation(
        boolean isRegionExists,
        boolean isReceivingStationsExists) {
        verify(regionRepository).existsRegionById(1L);
        if (isRegionExists) {
            verify(deactivateTariffsForChosenParamRepository).isReceivingStationsExists(List.of(1L, 12L));
            if (isReceivingStationsExists) {
                verify(tariffsInfoRepository)
                    .deactivateTariffsByRegionAndReceivingStations(1L, List.of(1L, 12L));
            }
        }
    }

    @Test
    void deactivateTariffByNotExistingRegionsAndReceivingStationsTrows() {
        DetailsOfDeactivateTariffsDto details =
            ModelUtils.getDetailsOfDeactivateTariffsDtoWithRegionAndReceivingStations();

        when(regionRepository.existsRegionById(anyLong())).thenReturn(false);
        assertThrows(NotFoundException.class,
            () -> superAdminService.deactivateTariffForChosenParam(details));
        verify(regionRepository).existsRegionById(1L);
        verify(tariffsInfoRepository, never()).deactivateTariffsByRegionAndReceivingStations(
            anyLong(),
            anyList());
    }

    @Test
    void deactivateTariffByRegionsAndNotExistingReceivingStationsTrows() {
        DetailsOfDeactivateTariffsDto details1 =
            ModelUtils.getDetailsOfDeactivateTariffsDtoWithRegionAndReceivingStations();

        when(regionRepository.existsRegionById(anyLong())).thenReturn(true);
        when(deactivateTariffsForChosenParamRepository.isReceivingStationsExists(anyList())).thenReturn(false);
        assertThrows(NotFoundException.class,
            () -> superAdminService.deactivateTariffForChosenParam(details1));
        verify(regionRepository).existsRegionById(1L);
        verify(deactivateTariffsForChosenParamRepository).isReceivingStationsExists(List.of(1L, 12L));
        verify(tariffsInfoRepository, never()).deactivateTariffsByRegionAndReceivingStations(
            anyLong(),
            anyList());
    }

    @Test
    void deactivateTariffByCourierAndRegionAndCities() {
        DetailsOfDeactivateTariffsDto details =
            ModelUtils.getDetailsOfDeactivateTariffsDtoWithCourierAndRegionAndCities();

        doMockForTariffWithCourierAndRegionAndCities(true, true, true);
        superAdminService.deactivateTariffForChosenParam(details);
        verifyForTariffWithCourierAndRegionAndCities(true, true, true);
    }

    private void doMockForTariffWithCourierAndRegionAndCities(
        boolean isRegionExists,
        boolean isCitiesExistsForRegion,
        boolean isCourierExists) {
        when(regionRepository.existsRegionById(anyLong())).thenReturn(isRegionExists);
        if (isRegionExists) {
            when(deactivateTariffsForChosenParamRepository
                .isCitiesExistForRegion(anyList(), anyLong())).thenReturn(isCitiesExistsForRegion);
            if (isCitiesExistsForRegion) {
                when(courierRepository.existsCourierById(anyLong())).thenReturn(isCourierExists);
            }
        }
    }

    private void verifyForTariffWithCourierAndRegionAndCities(
        boolean isRegionExists,
        boolean isCitiesExistsForRegion,
        boolean isCourierExists) {
        verify(regionRepository).existsRegionById(1L);
        if (isRegionExists) {
            verify(deactivateTariffsForChosenParamRepository).isCitiesExistForRegion(List.of(1L, 11L), 1L);
            if (isCitiesExistsForRegion) {
                verify(courierRepository).existsCourierById(1L);
                if (isCourierExists) {
                    verify(tariffsInfoRepository)
                        .deactivateTariffsByCourierAndRegionAndCities(1L, List.of(1L, 11L), 1L);
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("deactivateTariffByNotExistingSomeOfTreeParameters")
    void deactivateTariffByCourierAndRegionsAndCitiesWithNotExistingParamThrows(
        boolean isRegionExists,
        boolean isCitiesExistForRegion,
        boolean isCourierExists) {
        DetailsOfDeactivateTariffsDto details =
            ModelUtils.getDetailsOfDeactivateTariffsDtoWithCourierAndRegionAndCities();

        doMockForTariffWithCourierAndRegionAndCities(isRegionExists, isCitiesExistForRegion,
            isCourierExists);
        assertThrows(NotFoundException.class,
            () -> superAdminService.deactivateTariffForChosenParam(details));
        verifyForTariffWithCourierAndRegionAndCities(isRegionExists, isCitiesExistForRegion,
            isCourierExists);
        verify(tariffsInfoRepository, never())
            .deactivateTariffsByCourierAndRegionAndCities(anyLong(), anyList(), anyLong());
    }

    @Test
    void deactivateTariffByCourierAndRegionAndReceivingStation() {
        DetailsOfDeactivateTariffsDto details =
            ModelUtils.getDetailsOfDeactivateTariffsDtoWithCourierAndRegionAndReceivingStations();

        doMockForTariffWithCourierAndRegionAndReceivingStation(true, true, true);
        superAdminService.deactivateTariffForChosenParam(details);
        verifyForTariffWithCourierAndRegionAndReceivingStation(true, true, true);
    }

    private void doMockForTariffWithCourierAndRegionAndReceivingStation(
        boolean isRegionExists,
        boolean isCourierExists,
        boolean isReceivingStationExists) {
        when(regionRepository.existsRegionById(anyLong())).thenReturn(isRegionExists);
        if (isRegionExists) {
            when(deactivateTariffsForChosenParamRepository
                .isReceivingStationsExists(anyList())).thenReturn(isReceivingStationExists);
            if (isReceivingStationExists) {
                when(courierRepository.existsCourierById(anyLong())).thenReturn(isCourierExists);
            }
        }
    }

    private void verifyForTariffWithCourierAndRegionAndReceivingStation(
        boolean isRegionExists,
        boolean isCourierExists,
        boolean isReceivingStationExists) {
        verify(regionRepository).existsRegionById(1L);
        if (isRegionExists) {
            verify(deactivateTariffsForChosenParamRepository).isReceivingStationsExists(List.of(1L, 12L));
            if (isReceivingStationExists) {
                verify(courierRepository).existsCourierById(1L);
                if (isCourierExists) {
                    verify(tariffsInfoRepository)
                        .deactivateTariffsByCourierAndRegionAndReceivingStations(1L, List.of(1L, 12L), 1L);
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("deactivateTariffByNotExistingSomeOfTreeParameters")
    void deactivateTariffByCourierAndRegionsAndReceivingStationWithNotExistingParamThrows(
        boolean isRegionExists,
        boolean isCourierExists,
        boolean isReceivingStationExists) {
        DetailsOfDeactivateTariffsDto details =
            ModelUtils.getDetailsOfDeactivateTariffsDtoWithCourierAndRegionAndReceivingStations();

        doMockForTariffWithCourierAndRegionAndReceivingStation(isRegionExists, isReceivingStationExists,
            isCourierExists);
        assertThrows(NotFoundException.class,
            () -> superAdminService.deactivateTariffForChosenParam(details));
        verifyForTariffWithCourierAndRegionAndReceivingStation(isRegionExists, isReceivingStationExists,
            isCourierExists);
        verify(tariffsInfoRepository, never())
            .deactivateTariffsByCourierAndRegionAndCities(anyLong(), anyList(), anyLong());
    }
}