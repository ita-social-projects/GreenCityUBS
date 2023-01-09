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
import greencity.entity.user.employee.ReceivingStation;
import greencity.enums.CourierStatus;
import greencity.enums.LocationStatus;
import greencity.enums.MinAmountOfBag;
import greencity.enums.StationStatus;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.UnprocessableEntityException;
import greencity.exceptions.courier.CourierAlreadyExists;
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

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
    private BagRepository bagRepository;
    @Mock
    private BagTranslationRepository bagTranslationRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private ServiceRepository serviceRepository;
    @Mock
    private ServiceTranslationRepository serviceTranslationRepository;
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
            bagRepository,
            bagTranslationRepository,
            locationRepository,
            modelMapper,
            serviceRepository,
            serviceTranslationRepository,
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
        when(bagTranslationRepository.saveAll(bag.getBagTranslations())).thenReturn(ModelUtils.getBagTransaltion());

        superAdminService.addTariffService(dto, "123233");

        verify(userRepository).findByUuid("123233");
        verify(locationRepository).findById(1L);
        verify(bagRepository).save(bag);
        verify(bagTranslationRepository).saveAll(bag.getBagTranslations());
        verify(modelMapper).map(bag, AddServiceDto.class);
    }

    @Test
    void getTariffServiceTest() {
        when(bagTranslationRepository.findAll()).thenReturn(new ArrayList<>());

        superAdminService.getTariffService();

        verify(bagTranslationRepository).findAll();
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
        BagTranslation bagTranslation = ModelUtils.getBagTranslationForEditMethod();
        EditTariffServiceDto dto = ModelUtils.getEditTariffServiceDto();
        Bag bag = bagTranslation.getBag();
        User user = new User();
        user.setRecipientName("John");
        user.setRecipientSurname("Doe");

        when(userRepository.findByUuid(uuid)).thenReturn(user);
        when(bagRepository.findById(1)).thenReturn(Optional.of(bag));
        when(bagTranslationRepository.findBagTranslationByBag(bag))
            .thenReturn(bagTranslation);
        when(bagTranslationRepository.save(bagTranslation)).thenReturn(bagTranslation);

        superAdminService.editTariffService(dto, 1, uuid);

        verify(bagRepository).findById(1);
        verify(bagRepository).save(bag);
        verify(bagTranslationRepository).findBagTranslationByBag(bag);
        verify(bagTranslationRepository).save(bagTranslation);
    }

    @Test
    void deleteService() {
        Service service = new Service();
        service.setId(1L);

        when(serviceRepository.findById(service.getId())).thenReturn(Optional.of(service));
        superAdminService.deleteService(service.getId());
        verify(serviceRepository, times(1)).findById(service.getId());
        verify(serviceRepository, times(1)).delete(service);
    }

    @Test
    void getServiceTest() {
        List<ServiceTranslation> serviceTranslations = ModelUtils.getServiceTranslationList();
        List<GetServiceDto> getServiceDtos = List.of(ModelUtils.getAllInfoAboutService());
        when(serviceTranslationRepository.findAll()).thenReturn(serviceTranslations);

        assertEquals(getServiceDtos, superAdminService.getService());

        verify(serviceTranslationRepository).findAll();
    }

    @Test
    void editService() {
        Service service = ModelUtils.getEditedService();
        User user = ModelUtils.getUser();
        EditServiceDto dto = ModelUtils.getEditServiceDto();
        ServiceTranslation serviceTranslation = ModelUtils.getServiceTranslation();

        when(serviceRepository.findServiceById(1L)).thenReturn(Optional.of(service));
        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
        when(serviceTranslationRepository.findServiceTranslationsByService(service)).thenReturn(serviceTranslation);
        when(serviceRepository.save(service)).thenReturn(service);
        GetServiceDto getTariffServiceDto = ModelUtils.getServiceDto();

        assertEquals(getTariffServiceDto, superAdminService.editService(service.getId(), dto, user.getUuid()));

        verify(userRepository, times(1)).findByUuid(user.getUuid());
        verify(serviceRepository, times(1)).findServiceById(service.getId());
        verify(serviceTranslationRepository, times(1)).findServiceTranslationsByService(service);
        verify(serviceRepository, times(1)).save(service);
    }

    @Test
    void addService() {
        User user = ModelUtils.getUser();
        Courier courier = ModelUtils.getCourier();
        CreateServiceDto createServiceDto = ModelUtils.getCreateServiceDto();
        Service service = ModelUtils.getService();
        ServiceTranslation serviceTranslation = ModelUtils.getServiceTranslation();

        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
        when(serviceRepository.save(service)).thenReturn(service);
        when(serviceTranslationRepository.saveAll(service.getServiceTranslations()))
            .thenReturn(List.of(serviceTranslation));
        when(courierRepository.findById(anyLong())).thenReturn(Optional.of(courier));
        when(modelMapper.map(service, CreateServiceDto.class)).thenReturn(createServiceDto);

        assertEquals(createServiceDto, superAdminService.addService(createServiceDto, user.getUuid()));

        verify(userRepository).findByUuid(user.getUuid());
        verify(serviceRepository).save(service);
        verify(serviceTranslationRepository).saveAll(service.getServiceTranslations());
        verify(modelMapper).map(service, CreateServiceDto.class);
    }

    @Test
    void includeBag() {
        BagTranslation bagTranslationTest = BagTranslation.builder()
            .id(2L)
            .bag(Bag.builder().id(2).capacity(120).price(350).build())
            .name("Useless paper")
            .description("Description")
            .build();
        bagTranslationTest.getBag().setMinAmountOfBags(MinAmountOfBag.EXCLUDE);
        bagTranslationTest.getBag().setLocation(ModelUtils.getLocation());
        when(bagRepository.findById(10)).thenReturn(Optional.of(bagTranslationTest.getBag()));

        when(bagTranslationRepository.findBagTranslationByBag(bagTranslationTest.getBag()))
            .thenReturn(bagTranslationTest);

        assertEquals(MinAmountOfBag.INCLUDE.toString(), superAdminService.includeBag(10).getMinAmountOfBag());
        verify(bagRepository).save(any(Bag.class));
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
        BagTranslation bagTranslation = ModelUtils.bagTranslationDto();

        when(bagRepository.findById(1)).thenReturn(bag);
        when(bagTranslationRepository.findBagTranslationByBag(ModelUtils.bagDto2())).thenReturn(bagTranslation);

        superAdminService.excludeBag(1);

        verify(bagRepository).findById(1);
        verify(bagTranslationRepository).findBagTranslationByBag(ModelUtils.bagDto2());
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
    void deleteServiceExceptionTest() {
        assertThrows(NotFoundException.class, () -> superAdminService.deleteService(1L));
        verify(serviceRepository).findById(anyLong());
    }

    @Test
    void editServiceExceptionTest() {
        EditServiceDto dto = ModelUtils.getEditServiceDto();
        assertThrows(NotFoundException.class, () -> superAdminService.editService(1L, dto, "uuid"));
        verify(serviceRepository).findServiceById(anyLong());

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

        when(userRepository.findByUuid(anyString())).thenReturn(ModelUtils.getUser());
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

        when(userRepository.findByUuid(anyString())).thenReturn(ModelUtils.getUser());
        when(courierRepository.findAll()).thenReturn(List.of(getCourier(), getCourier()));

        assertThrows(CourierAlreadyExists.class, () -> superAdminService.createCourier(createCourierDto, uuid));
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

        superAdminService.createReceivingStation(stationDto, test);

        verify(receivingStationRepository, times(1)).existsReceivingStationByName(any());
        verify(receivingStationRepository, times(1)).save(any());
        verify(modelMapper, times(1))
            .map(any(ReceivingStation.class), eq(ReceivingStationDto.class));

        Exception thrown = assertThrows(UnprocessableEntityException.class,
            () -> superAdminService.createReceivingStation(stationDto, test));
        assertEquals(thrown.getMessage(), ErrorMessage.RECEIVING_STATION_ALREADY_EXISTS
            + stationDto.getName());
        verify(userRepository).findByUuid(any());
    }

    @Test
    void createReceivingStationSaveCorrectValue() {
        String receivingStationName = "Петрівка";
        User user = TEST_USER;
        AddingReceivingStationDto addingReceivingStationDto =
            AddingReceivingStationDto.builder().name(receivingStationName).build();
        ReceivingStation activatedReceivingStation = ReceivingStation.builder()
            .name(receivingStationName)
            .createdBy(user)
            .createDate(LocalDate.now())
            .stationStatus(StationStatus.ACTIVE)
            .build();

        ReceivingStationDto receivingStationDto = getReceivingStationDto();

        when(userRepository.findByUuid(any())).thenReturn(user);
        when(receivingStationRepository.existsReceivingStationByName(any())).thenReturn(false);
        when(receivingStationRepository.save(any())).thenReturn(activatedReceivingStation);
        when(modelMapper.map(any(), eq(ReceivingStationDto.class)))
            .thenReturn(receivingStationDto);

        superAdminService.createReceivingStation(addingReceivingStationDto, user.getUuid());

        verify(userRepository).findByUuid(any());
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
        when(userRepository.findByUuid(any())).thenReturn(ModelUtils.getUser());
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
    void addNewTariffThrowsException2() {
        AddNewTariffDto dto = ModelUtils.getAddNewTariffDto();
        when(courierRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getCourier()));
        assertThrows(EntityNotFoundException.class,
            () -> superAdminService.addNewTariff(dto, "35467585763t4sfgchjfuyetf"));
        verify(courierRepository).findById(anyLong());
        verify(receivingStationRepository).findAllById(any());
        verify(tariffsLocationRepository).findAllByCourierIdAndLocationIds(anyLong(), anyList());
    }

    @Test
    void addNewTariffThrowsException3() {
        AddNewTariffDto dto = ModelUtils.getAddNewTariffDto();
        when(courierRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
            () -> superAdminService.addNewTariff(dto, "35467585763t4sfgchjfuyetf"));
    }

    @Test
    void checkIfTariffExistsTest() {
        AddNewTariffDto dto = ModelUtils.getAddNewTariffDto();
        when(tariffsLocationRepository.findAllByCourierIdAndLocationIds(anyLong(), any()))
            .thenReturn(Collections.emptyList());
        boolean actual = superAdminService.checkIfTariffExists(dto);
        assertFalse(actual);
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