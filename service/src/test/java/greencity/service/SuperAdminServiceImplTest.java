package greencity.service;

import greencity.ModelUtils;
import greencity.constant.ErrorMessage;
import greencity.dto.AddNewTariffDto;
import greencity.dto.bag.EditAmountOfBagDto;
import greencity.dto.courier.*;
import greencity.dto.location.GetCourierLocationDto;
import greencity.dto.location.LocationCreateDto;
import greencity.dto.order.EditPriceOfOrder;
import greencity.dto.service.AddServiceDto;
import greencity.dto.service.CreateServiceDto;
import greencity.dto.service.EditServiceDto;
import greencity.dto.service.GetServiceDto;
import greencity.dto.tariff.EditTariffInfoDto;
import greencity.dto.tariff.EditTariffServiceDto;
import greencity.dto.tariff.GetTariffsInfoDto;
import greencity.entity.enums.CourierStatus;
import greencity.entity.enums.LocationStatus;
import greencity.entity.enums.MinAmountOfBag;
import greencity.entity.language.Language;
import greencity.entity.order.*;
import greencity.entity.user.Location;
import greencity.entity.user.Region;
import greencity.entity.user.User;
import greencity.entity.user.employee.ReceivingStation;
import greencity.exceptions.*;
import greencity.repository.*;
import greencity.service.ubs.SuperAdminServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import javax.persistence.EntityNotFoundException;
import java.util.*;

import static greencity.ModelUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
    private LanguageRepository languageRepository;
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
    private CourierTranslationRepository courierTranslationRepository;
    @Mock
    private RegionRepository regionRepository;
    @Mock
    private ReceivingStationRepository receivingStationRepository;
    @Mock
    private TariffsInfoRepository tariffsInfoRepository;

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
        when(bagTranslationRepository.findAll()).thenReturn(new ArrayList<BagTranslation>());

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
    void deleteTariffServiceThrowException() {
        assertThrows(BagNotFoundException.class, () -> superAdminService.deleteTariffService(1));
    }

    @Test
    void editTariffService_Throw_Exception() {
        EditTariffServiceDto dto = new EditTariffServiceDto();
        assertThrows(BagNotFoundException.class, () -> superAdminService.editTariffService(dto, 1, "testUUid"));
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
        Language language = ModelUtils.getLanguage();
        ServiceTranslation serviceTranslation = ModelUtils.getServiceTranslation();

        when(serviceRepository.findServiceById(1L)).thenReturn(Optional.of(service));
        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
        when(languageRepository.findLanguageByLanguageCode(dto.getLanguageCode()))
            .thenReturn(Optional.of(language));
        when(serviceTranslationRepository.findServiceTranslationsByServiceAndLanguageCode(service,
            dto.getLanguageCode())).thenReturn(serviceTranslation);
        when(serviceRepository.save(service)).thenReturn(service);
        GetServiceDto getTariffServiceDto = ModelUtils.getServiceDto();

        assertEquals(getTariffServiceDto, superAdminService.editService(service.getId(), dto, user.getUuid()));

        verify(userRepository, times(1)).findByUuid(user.getUuid());
        verify(serviceRepository, times(1)).findServiceById(service.getId());
        verify(languageRepository, times(1)).findLanguageByLanguageCode(dto.getLanguageCode());
        verify(serviceTranslationRepository, times(1)).findServiceTranslationsByServiceAndLanguageCode(service,
            dto.getLanguageCode());
        verify(serviceRepository, times(1)).save(service);
    }

    @Test
    void addService() {
        User user = ModelUtils.getUser();
        Courier courier = ModelUtils.getCourier();
        Language language = ModelUtils.getLanguage();
        CreateServiceDto createServiceDto = ModelUtils.getCreateServiceDto();
        Service service = ModelUtils.getService();
        ServiceTranslation serviceTranslation = ModelUtils.getServiceTranslation();

        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
        when(languageRepository.findById(language.getId())).thenReturn(Optional.of(language));
        when(serviceRepository.save(service)).thenReturn(service);
        when(serviceTranslationRepository.saveAll(service.getServiceTranslations()))
            .thenReturn(List.of(serviceTranslation));
        when(modelMapper.map(service, CreateServiceDto.class)).thenReturn(createServiceDto);

        assertEquals(createServiceDto, superAdminService.addService(createServiceDto, user.getUuid()));

        verify(userRepository).findByUuid(user.getUuid());
        verify(languageRepository).findById(language.getId());
        verify(serviceRepository).save(service);
        verify(serviceTranslationRepository).saveAll(service.getServiceTranslations());
        verify(modelMapper).map(service, CreateServiceDto.class);
    }

    @Test
    void createCourier() {
        Courier courier = ModelUtils.getCourier();
        courier.setId(null);
        CreateCourierDto createCourierDto = ModelUtils.getCreateCourierDto();

        when(userRepository.findByUuid(anyString())).thenReturn(ModelUtils.getUser());
        when(courierRepository.findAll()).thenReturn(List.of(getCourier(), getCourier()));
        when(languageRepository.findLanguageByCode("en")).thenReturn(ModelUtils.getEnLanguage());
        when(languageRepository.findLanguageByCode("ua")).thenReturn(ModelUtils.getLanguage());
        when(courierRepository.save(any())).thenReturn(courier);
        when(courierTranslationRepository.saveAll(any()))
            .thenReturn(ModelUtils.getCourierTranslations());
        when(modelMapper.map(any(), eq(CreateCourierDto.class))).thenReturn(createCourierDto);

        assertEquals(createCourierDto,
            superAdminService.createCourier(createCourierDto, ModelUtils.TEST_USER.getUuid()));

        verify(languageRepository).findLanguageByCode("en");
        verify(languageRepository).findLanguageByCode("ua");
        verify(courierRepository).save(any());
        verify(courierTranslationRepository).saveAll(any());
        verify(modelMapper).map(any(), eq(CreateCourierDto.class));
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
    void addLocationTest() {
        List<LocationCreateDto> locationCreateDtoList = ModelUtils.getLocationCreateDtoList();
        Language language = ModelUtils.getLanguage();
        Region region = ModelUtils.getRegion();

        when(regionRepository.findRegionByName("Kyiv region", "Київська область")).thenReturn(Optional.of(region));

        superAdminService.addLocation(locationCreateDtoList);

        verify(regionRepository).findRegionByName("Kyiv region", "Київська область");
    }

    @Test
    void addLocationCreateNewRegionTest() {
        List<LocationCreateDto> locationCreateDtoList = ModelUtils.getLocationCreateDtoList();
        Language language = ModelUtils.getLanguage();
        Region region = ModelUtils.getRegion();
        Location location = ModelUtils.getLocationForCreateRegion();
        when(regionRepository.findRegionByName("Kyiv region", "Київська область")).thenReturn(Optional.empty());
        when(regionRepository.save(any())).thenReturn(ModelUtils.getRegion());
        superAdminService.addLocation(locationCreateDtoList);
        verify(locationRepository).findLocationByName("Київ", "Kyiv", 1L);
        verify(regionRepository).findRegionByName("Kyiv region", "Київська область");
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
        assertThrows(LocationNotFoundException.class, () -> superAdminService.addTariffService(dto, "uuid"));
    }

    @Test
    void createServiceWithTranslationThrowLanguageNotFoundException() {
        CreateServiceDto createServiceDto = ModelUtils.getCreateServiceDto();
        Courier courier = ModelUtils.getCourier();
        User user = ModelUtils.getUser();

        lenient().when(courierRepository.findById(createServiceDto.getCourierId())).thenReturn(Optional.of(courier));
        when(userRepository.findByUuid("uuid")).thenReturn(user);

        assertThrows(LanguageNotFoundException.class, () -> superAdminService.addService(createServiceDto, "uuid"));
    }

    @Test
    void deleteServiceExceptionTest() {
        assertThrows(ServiceNotFoundException.class, () -> superAdminService.deleteService(1L));
    }

    @Test
    void editServiceExceptionTest() {
        EditServiceDto dto = ModelUtils.getEditServiceDto();
        assertThrows(ServiceNotFoundException.class, () -> superAdminService.editService(1L, dto, "uuid"));

    }

    @Test
    void editServiceExceptionThrowLanguageNotFoundExceptionTest() {
        EditServiceDto dto = ModelUtils.getEditServiceDto();
        Service service = Service.builder().id(1L).build();
        User user = User.builder().build();

        when(serviceRepository.findServiceById(service.getId())).thenReturn(Optional.of(service));
        when(userRepository.findByUuid("uuid")).thenReturn(user);

        assertThrows(LanguageNotFoundException.class, () -> superAdminService.editService(1L, dto, "uuid"));
    }

    @Test
    void addLocationThrowLocationAlreadyCreatedExceptionTest() {
        List<LocationCreateDto> locationCreateDtoList = ModelUtils.getLocationCreateDtoList();
        Location location = ModelUtils.getLocation();
        when(regionRepository.findRegionByName("Kyiv region", "Київська область"))
            .thenReturn(Optional.of(ModelUtils.getRegion()));
        when(locationRepository.findLocationByName("Київ", "Kyiv", 1L)).thenReturn(Optional.of(location));
        assertThrows(LocationAlreadyCreatedException.class, () -> superAdminService.addLocation(locationCreateDtoList));

        verify(locationRepository).findLocationByName("Київ", "Kyiv", 1L);
    }

    @Test
    void deactivateLocationExceptionTest() {
        assertThrows(LocationNotFoundException.class, () -> superAdminService.deactivateLocation(1L));
    }

    @Test
    void deactivateLocationException2Test() {
        Location location = ModelUtils.getLocationDto();
        location.setLocationStatus(LocationStatus.DEACTIVATED);

        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));

        assertThrows(LocationStatusAlreadyExistException.class, () -> superAdminService.deactivateLocation(1L));
    }

    @Test
    void activateLocationExceptionTest() {
        assertThrows(LocationNotFoundException.class, () -> superAdminService.activateLocation(1L));
    }

    @Test
    void activateLocationException2Test() {
        Location location = ModelUtils.getLocationDto();
        location.setLocationStatus(LocationStatus.ACTIVE);
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        assertThrows(LocationStatusAlreadyExistException.class, () -> superAdminService.activateLocation(1L));
    }

    @Test
    void setLimitDescriptionExceptiomTest() {
        assertThrows(CourierNotFoundException.class, () -> superAdminService.setLimitDescription(1L, "1"));
    }

    @Test
    void excludeBagExceptionTest() {
        assertThrows(CourierNotFoundException.class, () -> superAdminService.excludeBag(1));
    }

    @Test
    void excludeBagException2Test() {
        Optional<Bag> bag = Optional.ofNullable(ModelUtils.bagDto2());

        when(bagRepository.findById(1)).thenReturn(bag);

        assertThrows(BagWithThisStatusAlreadySetException.class, () -> superAdminService.excludeBag(1));
    }

    @Test
    void includeBagExceptionTest() {
        assertThrows(BagNotFoundException.class, () -> superAdminService.includeBag(1));
    }

    @Test
    void includeBagException2Test() {
        Optional<Bag> bag = Optional.ofNullable(ModelUtils.bagDto());

        when(bagRepository.findById(1)).thenReturn(bag);

        assertThrows(BagWithThisStatusAlreadySetException.class, () -> superAdminService.includeBag(1));
    }

    @Test
    void deleteCourierTest() {
        Courier courier = ModelUtils.getCourier();
        ;
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

        assertThrows(CourierNotFoundException.class, () -> superAdminService.deleteCourier(1L));

        verify(courierRepository).findById(1L);
    }

    @Test
    void getAllTariffsInfoTest() {
        when(tariffsInfoRepository.findAll()).thenReturn(List.of(ModelUtils.getTariffsInfo()));

        List<GetTariffsInfoDto> tariffsInfos = superAdminService.getAllTariffsInfo();

        verify(tariffsInfoRepository).findAll();
    }

    @Test
    void CreateReceivingStation() {
        AddingReceivingStationDto stationDto = AddingReceivingStationDto.builder().name("Петрівка").build();
        when(receivingStationRepository.existsReceivingStationByName(any())).thenReturn(false, true);
        lenient().when(modelMapper.map(any(ReceivingStation.class), eq(ReceivingStationDto.class)))
            .thenReturn(getReceivingStationDto());
        when(receivingStationRepository.save(any())).thenReturn(getReceivingStation(), getReceivingStation());

        superAdminService.createReceivingStation(stationDto, TEST_USER.getUuid());

        verify(receivingStationRepository, times(1)).existsReceivingStationByName(any());
        verify(receivingStationRepository, times(1)).save(any());
        verify(modelMapper, times(1))
            .map(any(ReceivingStation.class), eq(ReceivingStationDto.class));

        Exception thrown = assertThrows(ReceivingStationValidationException.class,
            () -> superAdminService.createReceivingStation(stationDto, TEST_USER.getUuid()));
        assertEquals(thrown.getMessage(), ErrorMessage.RECEIVING_STATION_ALREADY_EXISTS
            + stationDto.getName());
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

        station.setEmployees(Set.of(getEmployee()));
        Exception thrown = assertThrows(EmployeeIllegalOperationException.class,
            () -> superAdminService.deleteReceivingStation(1L));

        when(receivingStationRepository.findById(2L)).thenReturn(Optional.empty());

        Exception thrown1 = assertThrows(ReceivingStationNotFoundException.class,
            () -> superAdminService.deleteReceivingStation(2L));

        assertEquals(ErrorMessage.RECEIVING_STATION_NOT_FOUND_BY_ID + 2L, thrown1.getMessage());
        assertEquals(ErrorMessage.EMPLOYEES_ASSIGNED_STATION, thrown.getMessage());
    }

    @Test
    void updateCourierTest() {
        Courier courier = getCourier();

        List starterList = List.of(CourierTranslation.builder()
            .id(1L)
            .language(getLanguage())
            .name("Тест")
            .courier(courier)
            .build(),
            CourierTranslation.builder()
                .id(2L)
                .language(getEnLanguage())
                .name("Test")
                .courier(courier)
                .build());

        courier.setCourierTranslationList(starterList);

        List listToSave = List.of(CourierTranslation.builder()
            .id(1L)
            .language(getLanguage())
            .name("УБС")
            .courier(courier)
            .build(),
            CourierTranslation.builder()
                .id(2L)
                .language(getEnLanguage())
                .name("UBS")
                .courier(courier)
                .build());

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

        Courier courierToSave = Courier.builder()
            .id(courier.getId())
            .courierStatus(courier.getCourierStatus())
            .courierTranslationList(listToSave)
            .build();
        CourierDto courierDto = CourierDto.builder()
            .courierId(courier.getId())
            .courierStatus("Active")
            .courierTranslationDtos(dtoList)
            .build();

        when(courierRepository.findById(dto.getCourierId())).thenReturn(Optional.of(courier));
        when(courierRepository.save(courier)).thenReturn(courierToSave);
        when(courierTranslationRepository.saveAll(courier.getCourierTranslationList()))
            .thenReturn(listToSave);
        when(modelMapper.map(courierToSave, CourierDto.class)).thenReturn(courierDto);

        CourierDto actual = superAdminService.updateCourier(dto);
        CourierDto expected = CourierDto.builder()
            .courierId(getCourier().getId())
            .courierStatus("Active")
            .courierTranslationDtos(dto.getCourierTranslationDtos())
            .build();

        assertEquals(expected, actual);
    }

    @Test
    void editNewTariffSuccess() {
        AddNewTariffDto dto = ModelUtils.getAddNewTariffDto();
        when(locationRepository.findAllByIdAndRegionId(dto.getLocationIdList(), dto.getRegionId()))
            .thenReturn(ModelUtils.getLocationList());
        when(regionRepository.findById(1L)).thenReturn(Optional.of(ModelUtils.getRegion()));
        when(userRepository.findByUuid(any())).thenReturn(ModelUtils.getUser());
        when(receivingStationRepository.findAllById(List.of(1L))).thenReturn(ModelUtils.getReceivingList());
        when(courierRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getCourier()));
        superAdminService.addNewTariff(dto, "35467585763t4sfgchjfuyetf");
        verify(tariffsInfoRepository, times(1)).save(any());
    }

    @Test
    void addNewTariffThrowsException() {
        when(regionRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RegionNotFoundException.class,
            () -> superAdminService.addNewTariff(ModelUtils.getAddNewTariffDto(), "35467585763t4sfgchjfuyetf"));
    }

    @Test
    void addNewTariffThrowsException2() {
        AddNewTariffDto dto = ModelUtils.getAddNewTariffDto();
        when(courierRepository.findById(anyLong())).thenReturn(Optional.of(ModelUtils.getCourier()));
        when(regionRepository.findById(1L)).thenReturn(Optional.of(ModelUtils.getRegion()));
        when(locationRepository.findAllByIdAndRegionId(dto.getLocationIdList(), dto.getRegionId()))
            .thenReturn(Collections.emptyList());
        assertThrows(EntityNotFoundException.class,
            () -> superAdminService.addNewTariff(ModelUtils.getAddNewTariffDto(), "35467585763t4sfgchjfuyetf"));
    }

    @Test
    void addNewTariffThrowsException3() {
        AddNewTariffDto dto = ModelUtils.getAddNewTariffDto();
        when(regionRepository.findById(1L)).thenReturn(Optional.of(ModelUtils.getRegion()));
        when(courierRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(CourierNotFoundException.class,
            () -> superAdminService.addNewTariff(ModelUtils.getAddNewTariffDto(), "35467585763t4sfgchjfuyetf"));
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
    void editTariffTestThrows() {
        when(tariffsInfoRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(TariffNotFoundException.class,
            () -> superAdminService.setTariffLimitBySumOfOrder(1L, ModelUtils.getEditPriceOfOrder()));
    }

    @Test
    void deleteTariff() {
        TariffsInfo tariffsInfo = ModelUtils.getTariffInfo();
        when(tariffsInfoRepository.findById(anyLong())).thenReturn(Optional.of(tariffsInfo));
        superAdminService.deactivateTariffCard(1L);
        verify(tariffsInfoRepository).delete(tariffsInfo);
    }

    @Test
    void deleteTariff2() {
        TariffsInfo tariffsInfo = ModelUtils.getTariffInfoWithLimitOfBags();
        when(tariffsInfoRepository.findById(anyLong())).thenReturn(Optional.of(tariffsInfo));
        assertEquals("Deactivated", superAdminService.deactivateTariffCard(1L));
    }
}
