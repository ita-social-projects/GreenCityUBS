package greencity.service.ubs;

import greencity.ModelUtils;
import greencity.constant.ErrorMessage;
import greencity.dto.AddNewTariffDto;
import greencity.dto.DetailsOfDeactivateTariffsDto;
import greencity.dto.courier.*;
import greencity.dto.location.EditLocationDto;
import greencity.dto.location.LocationCreateDto;
import greencity.dto.location.LocationToCityDto;
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
import org.junit.jupiter.api.DisplayName;
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
import java.util.stream.Collectors;
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
    private DeactivateChosenEntityRepository deactivateTariffsForChosenParamRepository;;

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

        verify(locationRepository).findById(1L);
        verify(bagRepository).save(bag);
        verify(bagTranslationRepository).saveAll(bag.getBagTranslations());
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
    }

    @Test
    void editTariffService_Throw_Exception() {
        EditTariffServiceDto dto = new EditTariffServiceDto();
        assertThrows(NotFoundException.class, () -> superAdminService.editTariffService(dto, 1, "testUUid"));
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
    }

    @Test
    void getAllLocationTest() {
        List<Region> regionList = ModelUtils.getAllRegion();

        when(regionRepository.findAll()).thenReturn(regionList);

        superAdminService.getAllLocation();

        verify(regionRepository).findAll();
    }

    @Test
    void getActiveLocationsTest() {
        List<Region> regionList = ModelUtils.getAllRegion();

        when(regionRepository.findRegionsWithActiveLocations()).thenReturn(regionList);

        superAdminService.getActiveLocations();

        verify(regionRepository).findRegionsWithActiveLocations();
    }

    @Test
    void addLocationTest() {
        List<LocationCreateDto> locationCreateDtoList = ModelUtils.getLocationCreateDtoList();
        Region region = ModelUtils.getRegion();

        when(regionRepository.findRegionByEnNameAndUkrName("Kyiv region", "Київська область"))
            .thenReturn(Optional.of(region));

        superAdminService.addLocation(locationCreateDtoList);

        verify(regionRepository).findRegionByEnNameAndUkrName("Kyiv region", "Київська область");
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
    }

    @Test
    void deactivateLocation() {
        Location location = ModelUtils.getLocationDto();
        location.setLocationStatus(LocationStatus.ACTIVE);

        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));

        superAdminService.deactivateLocation(1L);

        verify(locationRepository).findById(1L);
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
    }

    @Test
    void addTariffServiceExceptionTest() {
        AddServiceDto dto = ModelUtils.addServiceDto();
        assertThrows(NotFoundException.class, () -> superAdminService.addTariffService(dto, "uuid"));
    }

    @Test
    void deleteServiceExceptionTest() {
        assertThrows(NotFoundException.class, () -> superAdminService.deleteService(1L));
    }

    @Test
    void editServiceExceptionTest() {
        EditServiceDto dto = ModelUtils.getEditServiceDto();
        assertThrows(NotFoundException.class, () -> superAdminService.editService(1L, dto, "uuid"));

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
    }

    @Test
    void addNewTariffThrowsException2() {
        AddNewTariffDto dto = ModelUtils.getAddNewTariffDto();
        when(courierRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getCourier()));
        assertThrows(EntityNotFoundException.class,
            () -> superAdminService.addNewTariff(dto, "35467585763t4sfgchjfuyetf"));
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

    @ParameterizedTest
    @DisplayName("Deactivate Tariff By Regions")
    @MethodSource("providerForDeactivateTariffForChosenParamByRegions")
    void deactivateTariffForChosenParamByRegions(List<Long> regionsId, boolean isRegionsExists) {

        DetailsOfDeactivateTariffsDto details = new DetailsOfDeactivateTariffsDto(
            Optional.of(regionsId),
            Optional.empty(),
            Optional.empty(),
            Optional.empty());

        when(deactivateTariffsForChosenParamRepository.isRegionsExists(anyList())).thenReturn(isRegionsExists);
        if (isRegionsExists) {
            superAdminService.deactivateTariffForChosenParam(details);
            verify(deactivateTariffsForChosenParamRepository).deactivateTariffsByRegions(regionsId);
        } else {
            Throwable exception = assertThrows(NotFoundException.class,
                () -> superAdminService.deactivateTariffForChosenParam(details));
            assertEquals(String.format("Current region doesn't exist: %s", regionsId), exception.getMessage());
        }
    }

    static Stream<Arguments> providerForDeactivateTariffForChosenParamByRegions() {

        List<Long> regionsId = ModelUtils.getAllRegion()
            .stream()
            .map(Region::getId)
            .limit(1L)
            .collect(Collectors.toList());

        return Stream.of(
            arguments(regionsId, true),
            arguments(List.of(2L), false));
    }

    @ParameterizedTest
    @DisplayName("Deactivate Tariff By Regions And Cities")
    @MethodSource("providerForDeactivateTariffForChosenParamByRegionsAndCities")
    void deactivateTariffForChosenParamByRegionsAndCities(List<Long> regionsId, List<Long> citiesId,
        boolean existsRegionById, boolean isCitiesExistForRegion) {

        DetailsOfDeactivateTariffsDto details = new DetailsOfDeactivateTariffsDto(
            Optional.of(regionsId),
            Optional.of(citiesId),
            Optional.empty(),
            Optional.empty());

        if (regionsId.size() == 1) {
            when(regionRepository.existsRegionById(anyLong())).thenReturn(existsRegionById);
            if (existsRegionById) {
                when(deactivateTariffsForChosenParamRepository.isCitiesExistForRegion(anyList(), anyLong()))
                    .thenReturn(isCitiesExistForRegion);

            }
            if (isCitiesExistForRegion) {
                superAdminService.deactivateTariffForChosenParam(details);
                verify(deactivateTariffsForChosenParamRepository).deactivateTariffsByRegionsAndCities(citiesId,
                    regionsId.get(0));
            } else {
                Throwable exception = assertThrows(NotFoundException.class,
                    () -> superAdminService.deactivateTariffForChosenParam(details));
                assertEquals(String.format("Current regions %s or cities %s don't exist.", regionsId, citiesId),
                    exception.getMessage());
            }
        } else {
            Throwable exception = assertThrows(BadRequestException.class,
                () -> superAdminService.deactivateTariffForChosenParam(details));
            assertEquals("Region ids size should be 1 if several params are selected", exception.getMessage());
        }
    }

    static Stream<Arguments> providerForDeactivateTariffForChosenParamByRegionsAndCities() {

        List<Long> regionsId = ModelUtils.getAllRegion()
            .stream()
            .map(Region::getId)
            .limit(1L)
            .collect(Collectors.toList());

        List<Long> regionsOverflowId = new ArrayList<>();
        regionsOverflowId.addAll(regionsId);
        regionsOverflowId.add(2L);
        regionsOverflowId.add(3L);

        List<Long> citiesInUaId = ModelUtils.getCitiesInUa()
            .stream()
            .map(LocationToCityDto::getCityId)
            .collect(Collectors.toList());

        return Stream.of(
            arguments(regionsId, citiesInUaId, true, true),
            arguments(List.of(2L), citiesInUaId, false, true),
            arguments(regionsId, List.of(2L), true, false),
            arguments(List.of(3L), List.of(2L), false, false),
            arguments(regionsOverflowId, citiesInUaId, false, false),
            arguments(new ArrayList<>(), citiesInUaId, false, false));
    }

    @ParameterizedTest
    @DisplayName("Deactivate Tariff By Courier")
    @MethodSource("providerForDeactivateTariffForChosenParamByCourier")
    void deactivateTariffForChosenParamByCourier(Long courierId, boolean existsCourierById) {

        DetailsOfDeactivateTariffsDto details = new DetailsOfDeactivateTariffsDto(
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.of(courierId));

        when(courierRepository.existsCourierById(anyLong())).thenReturn(existsCourierById);
        if (existsCourierById) {
            superAdminService.deactivateTariffForChosenParam(details);
            verify(deactivateTariffsForChosenParamRepository).deactivateTariffsByCourier(courierId);
        } else {
            Throwable exception = assertThrows(NotFoundException.class,
                () -> superAdminService.deactivateTariffForChosenParam(details));
            assertEquals(String.format("Current courier doesn't exist: %s", courierId), exception.getMessage());
        }
    }

    static Stream<Arguments> providerForDeactivateTariffForChosenParamByCourier() {
        Long courierId = ModelUtils.getCourier().getId();

        return Stream.of(
            arguments(courierId, true),
            arguments(2L, false));
    }

    @ParameterizedTest
    @DisplayName("Deactivate Tariff By Receiving Stations")
    @MethodSource("providerForDeactivateTariffForChosenParamByReceivingStations")
    void deactivateTariffForChosenParamByReceivingStations(List<Long> receivingStationsId,
        boolean isReceivingStationsExists) {

        DetailsOfDeactivateTariffsDto details = new DetailsOfDeactivateTariffsDto(
            Optional.empty(),
            Optional.empty(),
            Optional.of(receivingStationsId),
            Optional.empty());

        when(deactivateTariffsForChosenParamRepository.isReceivingStationsExists(anyList()))
            .thenReturn(isReceivingStationsExists);
        if (isReceivingStationsExists) {
            superAdminService.deactivateTariffForChosenParam(details);
            verify(deactivateTariffsForChosenParamRepository).deactivateTariffsByReceivingStations(receivingStationsId);
        } else {
            Throwable exception = assertThrows(NotFoundException.class,
                () -> superAdminService.deactivateTariffForChosenParam(details));
            assertEquals(String.format("Current receiving stations don't exist: %s", receivingStationsId),
                exception.getMessage());
        }
    }

    static Stream<Arguments> providerForDeactivateTariffForChosenParamByReceivingStations() {
        List<Long> receivingStationsId = List.of(ModelUtils.getReceivingStation().getId());

        return Stream.of(
            arguments(receivingStationsId, true),
            arguments(List.of(2L), false));
    }

    @ParameterizedTest
    @DisplayName("Deactivate Tariff By Courier And Receiving Stations")
    @MethodSource("providerForDeactivateTariffForChosenParamByCourierAndReceivingStations")
    void deactivateTariffForChosenParamByCourierAndReceivingStations(Long courierId,
        List<Long> receivingStationsId,
        boolean existsCourierById,
        boolean isReceivingStationsExists) {

        DetailsOfDeactivateTariffsDto details = new DetailsOfDeactivateTariffsDto(
            Optional.empty(),
            Optional.empty(),
            Optional.of(receivingStationsId),
            Optional.of(courierId));

        when(courierRepository.existsCourierById(anyLong())).thenReturn(existsCourierById);
        if (existsCourierById) {
            when(deactivateTariffsForChosenParamRepository.isReceivingStationsExists(anyList()))
                .thenReturn(isReceivingStationsExists);
        }
        if (existsCourierById && isReceivingStationsExists) {
            superAdminService.deactivateTariffForChosenParam(details);
            verify(deactivateTariffsForChosenParamRepository).deactivateTariffsByCourierAndReceivingStations(courierId,
                receivingStationsId);
        } else {
            Throwable exception = assertThrows(NotFoundException.class,
                () -> superAdminService.deactivateTariffForChosenParam(details));
            assertEquals(String.format("Current receiving stations: %s or courier: %s don't exist.",
                receivingStationsId, courierId), exception.getMessage());
        }
    }

    static Stream<Arguments> providerForDeactivateTariffForChosenParamByCourierAndReceivingStations() {

        Long courierId = ModelUtils.getCourier().getId();
        List<Long> receivingStationsId = List.of(ModelUtils.getReceivingStation().getId());

        return Stream.of(
            arguments(courierId, receivingStationsId, true, true),
            arguments(2L, receivingStationsId, false, true),
            arguments(courierId, List.of(3L), true, false),
            arguments(2L, List.of(3L), false, false));
    }

    @ParameterizedTest
    @DisplayName("Deactivate Tariff By Courier And Region")
    @MethodSource("providerForDeactivateTariffForChosenParamByCourierAndRegion")
    void deactivateTariffForChosenParamByCourierAndRegion(List<Long> regionsId,
        Long courierId,
        boolean existsRegionById,
        boolean existsCourierById) {

        DetailsOfDeactivateTariffsDto details = new DetailsOfDeactivateTariffsDto(
            Optional.of(regionsId),
            Optional.empty(),
            Optional.empty(),
            Optional.of(courierId));

        if (regionsId.size() == 1) {
            when(regionRepository.existsRegionById(anyLong())).thenReturn(existsRegionById);
            if (existsRegionById) {
                when(courierRepository.existsCourierById(anyLong())).thenReturn(existsCourierById);
                ;
            }
            if (existsCourierById && existsRegionById) {
                superAdminService.deactivateTariffForChosenParam(details);
                verify(deactivateTariffsForChosenParamRepository).deactivateTariffsByCourierAndRegion(regionsId.get(0),
                    courierId);
            } else {
                Throwable exception = assertThrows(NotFoundException.class,
                    () -> superAdminService.deactivateTariffForChosenParam(details));
                assertEquals(String.format("Current region: %s or courier: %s don't exist.", regionsId, courierId),
                    exception.getMessage());
            }
        } else {
            Throwable exception = assertThrows(BadRequestException.class,
                () -> superAdminService.deactivateTariffForChosenParam(details));
            assertEquals("Region ids size should be 1 if several params are selected", exception.getMessage());
        }
    }

    static Stream<Arguments> providerForDeactivateTariffForChosenParamByCourierAndRegion() {

        List<Long> regionsId = ModelUtils.getAllRegion()
            .stream()
            .map(Region::getId)
            .limit(1L)
            .collect(Collectors.toList());

        List<Long> regionsOverflowId = new ArrayList<>();
        regionsOverflowId.addAll(regionsId);
        regionsOverflowId.add(2L);
        regionsOverflowId.add(3L);

        Long courierId = ModelUtils.getCourier().getId();

        return Stream.of(
            arguments(regionsId, courierId, true, true),
            arguments(List.of(2L), courierId, false, true),
            arguments(regionsId, 3L, true, false),
            arguments(List.of(2L), 3L, false, false),
            arguments(regionsOverflowId, courierId, false, true),
            arguments(new ArrayList<>(), courierId, false, true));
    }

    @ParameterizedTest
    @DisplayName("Deactivate Tariff By Region And City And Station")
    @MethodSource("providerForDeactivateTariffForChosenParamByRegionAndCityAndStation")
    void deactivateTariffForChosenParamByRegionAndCityAndStation(List<Long> regionsId,
        List<Long> citiesId,
        List<Long> receivingStationsId,
        boolean existsRegionById,
        boolean isCitiesExistForRegion,
        boolean isReceivingStationsExists) {

        DetailsOfDeactivateTariffsDto details = new DetailsOfDeactivateTariffsDto(
            Optional.of(regionsId),
            Optional.of(citiesId),
            Optional.of(receivingStationsId),
            Optional.empty());

        if (regionsId.size() == 1) {
            when(regionRepository.existsRegionById(anyLong())).thenReturn(existsRegionById);
            if (existsRegionById) {
                when(deactivateTariffsForChosenParamRepository
                    .isCitiesExistForRegion(anyList(), anyLong())).thenReturn(isCitiesExistForRegion);
                if (isCitiesExistForRegion) {
                    when(deactivateTariffsForChosenParamRepository
                        .isReceivingStationsExists(anyList())).thenReturn(isReceivingStationsExists);
                }
            }
            if (existsRegionById && isCitiesExistForRegion && isReceivingStationsExists) {
                superAdminService.deactivateTariffForChosenParam(details);
                verify(deactivateTariffsForChosenParamRepository)
                    .deactivateTariffsByRegionAndCitiesAndStations(regionsId.get(0), citiesId, receivingStationsId);
            } else {
                Throwable exception = assertThrows(NotFoundException.class,
                    () -> superAdminService.deactivateTariffForChosenParam(details));
                assertEquals(String.format("Current region: %s or cities: %s or receiving stations: %s don't exist.",
                    regionsId, citiesId, receivingStationsId), exception.getMessage());
            }
        } else {
            Throwable exception = assertThrows(BadRequestException.class,
                () -> superAdminService.deactivateTariffForChosenParam(details));
            assertEquals("Region ids size should be 1 if several params are selected", exception.getMessage());
        }
    }

    static Stream<Arguments> providerForDeactivateTariffForChosenParamByRegionAndCityAndStation() {

        List<Long> regionsId = ModelUtils.getAllRegion()
            .stream()
            .map(Region::getId)
            .limit(1L)
            .collect(Collectors.toList());

        List<Long> regionsOverflowId = new ArrayList<>();
        regionsOverflowId.addAll(regionsId);
        regionsOverflowId.add(2L);
        regionsOverflowId.add(3L);

        List<Long> citiesInUaId = ModelUtils.getCitiesInUa()
            .stream()
            .map(LocationToCityDto::getCityId)
            .collect(Collectors.toList());

        List<Long> receivingStationsId = List.of(ModelUtils.getReceivingStation().getId());

        return Stream.of(
            arguments(regionsId, citiesInUaId, receivingStationsId, true, true, true),
            arguments(List.of(2L), citiesInUaId, receivingStationsId, false, true, true),
            arguments(List.of(2L), List.of(3L), receivingStationsId, false, false, true),
            arguments(List.of(2L), citiesInUaId, List.of(4L), false, true, false),
            arguments(regionsId, List.of(3L), receivingStationsId, true, false, true),
            arguments(regionsId, List.of(3L), List.of(4L), true, false, false),
            arguments(regionsId, citiesInUaId, List.of(4L), true, true, false),
            arguments(List.of(2L), List.of(3L), List.of(4L), false, false, false),
            arguments(regionsOverflowId, citiesInUaId, receivingStationsId, false, true, true),
            arguments(new ArrayList<>(), citiesInUaId, receivingStationsId, false, false, true));
    }

    @ParameterizedTest
    @DisplayName("Deactivate Tariff By All")
    @MethodSource("providerForDeactivateTariffForChosenParamByAll")
    void deactivateTariffForChosenParamByAll(List<Long> regionsId,
        List<Long> citiesId,
        List<Long> receivingStationsId,
        Long courierId,
        boolean existsRegionById,
        boolean isCitiesExistForRegion,
        boolean isReceivingStationsExists,
        boolean existsCourierById) {

        DetailsOfDeactivateTariffsDto details = new DetailsOfDeactivateTariffsDto(
            Optional.of(regionsId),
            Optional.of(citiesId),
            Optional.of(receivingStationsId),
            Optional.of(courierId));

        if (regionsId.size() == 1) {
            when(regionRepository.existsRegionById(anyLong())).thenReturn(existsRegionById);
            if (existsRegionById) {
                when(deactivateTariffsForChosenParamRepository
                    .isCitiesExistForRegion(anyList(), anyLong())).thenReturn(isCitiesExistForRegion);
                if (isCitiesExistForRegion) {
                    when(deactivateTariffsForChosenParamRepository
                        .isReceivingStationsExists(anyList())).thenReturn(isReceivingStationsExists);
                    if (isReceivingStationsExists) {
                        when(courierRepository.existsCourierById(courierId)).thenReturn(existsCourierById);
                    }
                }
            }
            if (existsRegionById && isCitiesExistForRegion && isReceivingStationsExists && existsCourierById) {
                superAdminService.deactivateTariffForChosenParam(details);
                verify(deactivateTariffsForChosenParamRepository)
                    .deactivateTariffsByAllParam(regionsId.get(0), citiesId, receivingStationsId, courierId);
            } else {
                Throwable exception = assertThrows(NotFoundException.class,
                    () -> superAdminService.deactivateTariffForChosenParam(details));
                assertEquals(String.format(
                    "Current region: %s or cities: %s or receiving stations: %s or courier: %s don't exist.",
                    regionsId, citiesId, receivingStationsId, courierId), exception.getMessage());
            }
        } else {
            Throwable exception = assertThrows(BadRequestException.class,
                () -> superAdminService.deactivateTariffForChosenParam(details));
            assertEquals("Region ids size should be 1 if several params are selected", exception.getMessage());
        }
    }

    static Stream<Arguments> providerForDeactivateTariffForChosenParamByAll() {

        List<Long> regionsId = ModelUtils.getAllRegion()
            .stream()
            .map(Region::getId)
            .limit(1L)
            .collect(Collectors.toList());

        List<Long> regionsOverflowId = new ArrayList<>();
        regionsOverflowId.addAll(regionsId);
        regionsOverflowId.add(2L);
        regionsOverflowId.add(3L);

        List<Long> citiesInUaId = ModelUtils.getCitiesInUa()
            .stream()
            .map(LocationToCityDto::getCityId)
            .collect(Collectors.toList());

        List<Long> receivingStationsId = List.of(ModelUtils.getReceivingStation().getId());

        Long courierId = ModelUtils.getCourier().getId();

        return Stream.of(
            arguments(regionsId, citiesInUaId, receivingStationsId, courierId, true, true, true, true),
            arguments(List.of(2L), citiesInUaId, receivingStationsId, courierId, false, true, true, true),
            arguments(regionsId, List.of(3L), receivingStationsId, courierId, true, false, true, true),
            arguments(regionsId, citiesInUaId, List.of(4L), courierId, true, true, false, true),
            arguments(regionsId, citiesInUaId, receivingStationsId, 5L, true, true, true, false),
            arguments(List.of(2L), List.of(3L), receivingStationsId, courierId, false, false, true, true),
            arguments(List.of(2L), citiesInUaId, List.of(4L), courierId, false, true, false, true),
            arguments(List.of(2L), citiesInUaId, receivingStationsId, 5L, false, true, true, false),
            arguments(regionsId, List.of(3L), List.of(4L), courierId, true, false, false, true),
            arguments(regionsId, List.of(3L), receivingStationsId, 5L, true, false, true, false),
            arguments(regionsId, citiesInUaId, List.of(4L), 5L, true, true, false, false),
            arguments(List.of(2L), List.of(3L), List.of(4L), courierId, false, false, false, true),
            arguments(List.of(2L), List.of(3L), receivingStationsId, 5L, false, false, true, false),
            arguments(regionsId, List.of(3L), List.of(4L), 5L, true, false, false, false),
            arguments(List.of(2L), List.of(3L), List.of(4L), 5L, false, false, false, false),
            arguments(regionsOverflowId, citiesInUaId, receivingStationsId, courierId, false, true, true, true),
            arguments(new ArrayList<>(), citiesInUaId, receivingStationsId, courierId, false, false, true, true));
    }

    @Test
    @DisplayName("Deactivate Tariff By All Empty")
    void deactivateTariffForChosenParamByAllEmpty() {

        DetailsOfDeactivateTariffsDto details = new DetailsOfDeactivateTariffsDto(
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty());

        Throwable exception = assertThrows(BadRequestException.class,
            () -> superAdminService.deactivateTariffForChosenParam(details));
        assertEquals("Bad request. Please choose another combination of parameters", exception.getMessage());
    }

}
