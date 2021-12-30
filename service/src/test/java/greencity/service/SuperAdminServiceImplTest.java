package greencity.service;

import greencity.ModelUtils;
import greencity.dto.*;
import greencity.entity.enums.CourierLimit;
import greencity.entity.enums.CourierStatus;
import greencity.entity.enums.LocationStatus;
import greencity.entity.enums.MinAmountOfBag;
import greencity.entity.language.Language;
import greencity.entity.order.*;
import greencity.entity.user.Location;
import greencity.entity.user.LocationTranslation;
import greencity.entity.user.Region;
import greencity.entity.user.User;
import greencity.exceptions.*;
import greencity.repository.*;
import greencity.service.ubs.SuperAdminServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private LocationTranslationRepository locationTranslationRepository;
    @Mock
    private CourierLocationRepository courierLocationRepository;
    @Mock
    private RegionRepository regionRepository;
    @Mock
    private RegionTranslationRepository regionTranslationRepository;

    @Test
    void addTariffServiceTest() {
        User user = ModelUtils.getUser();
        Bag bag = ModelUtils.getTariffBag();
        AddServiceDto dto = ModelUtils.addServiceDto();

        when(userRepository.findByUuid("123233")).thenReturn(user);
        when(locationRepository.findById(1L)).thenReturn(Optional.of(ModelUtils.getLocation()));
        when(languageRepository.findById(1L)).thenReturn(Optional.of(ModelUtils.getLanguage()));
        when(bagRepository.save(bag)).thenReturn(bag);
        when(bagTranslationRepository.saveAll(bag.getBagTranslations())).thenReturn(ModelUtils.getBagTransaltion());

        superAdminService.addTariffService(dto, "123233");

        verify(locationRepository).findById(1L);
        verify(languageRepository).findById(1L);
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
        when(bagTranslationRepository.findBagTranslationByBagAndLanguageCode(bag, dto.getLangCode()))
            .thenReturn(bagTranslation);
        when(bagTranslationRepository.save(bagTranslation)).thenReturn(bagTranslation);

        superAdminService.editTariffService(dto, 1, uuid);

        verify(bagRepository).findById(1);
        verify(bagRepository).save(bag);
        verify(bagTranslationRepository).findBagTranslationByBagAndLanguageCode(bag, dto.getLangCode());
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

        when(courierRepository.findById(courier.getId())).thenReturn(Optional.of(courier));
        when(userRepository.findByUuid(user.getUuid())).thenReturn(user);
        when(languageRepository.findById(language.getId())).thenReturn(Optional.of(language));
        when(serviceRepository.save(service)).thenReturn(service);
        when(serviceTranslationRepository.saveAll(service.getServiceTranslations()))
            .thenReturn(List.of(serviceTranslation));
        when(modelMapper.map(service, CreateServiceDto.class)).thenReturn(createServiceDto);

        assertEquals(createServiceDto, superAdminService.addService(createServiceDto, user.getUuid()));

        verify(courierRepository).findById(courier.getId());
        verify(userRepository).findByUuid(user.getUuid());
        verify(languageRepository).findById(language.getId());
        verify(serviceRepository).save(service);
        verify(serviceTranslationRepository).saveAll(service.getServiceTranslations());
        verify(modelMapper).map(service, CreateServiceDto.class);
    }

    @Test
    void createCourierThrowException_LocationNotFoundException() {
        CreateCourierDto createCourierDto = ModelUtils.createCourier();
        Language language = ModelUtils.getLanguage();
        when(languageRepository.findById(any())).thenReturn(Optional.of(language));
        assertThrows(LocationNotFoundException.class, () -> superAdminService.createCourier(createCourierDto));
    }

    @Test
    void createCourierThrowException_LanguageNotFoundException() {
        CreateCourierDto createCourierDto = ModelUtils.createCourier();
        assertThrows(LanguageNotFoundException.class, () -> superAdminService.createCourier(createCourierDto));
    }

    @Test
    void getAllCouriers() {
        when(courierLocationRepository.findAllInfoAboutCourier()).thenReturn(List.of(ModelUtils.getCourierLocations()));
        when(modelMapper.map(ModelUtils.getCourierLocations(), GetCourierLocationDto.class))
            .thenReturn(ModelUtils.getCourierLocationsDto());

        assertEquals(List.of(ModelUtils.getCourierLocationsDto()), superAdminService.getAllCouriers());
    }

    @Test
    void setCourierLimitBySumOfOrder() {
        EditPriceOfOrder dto = ModelUtils.getEditPriceOfOrder();
        CourierLocation courierLocation = ModelUtils.getCourierLocations();
        when(courierLocationRepository.findCourierLocationsLimitsByCourierIdAndLocationId(1L, dto.getLocationId()))
            .thenReturn(courierLocation);
        when(courierLocationRepository.save(courierLocation)).thenReturn(courierLocation);

        superAdminService.setCourierLimitBySumOfOrder(1L, dto);

        verify(courierLocationRepository).findCourierLocationsLimitsByCourierIdAndLocationId(1L, dto.getLocationId());
        verify(courierLocationRepository).save(courierLocation);
    }

    @Test
    void setCourierLimitByAmountOfBag() {
        EditAmountOfBagDto dto = ModelUtils.getAmountOfBagDto();
        CourierLocation courierLocation = ModelUtils.getCourierLocations();

        when(courierLocationRepository.findCourierLocationsLimitsByCourierIdAndLocationId(1L, dto.getLocationId()))
            .thenReturn(courierLocation);
        when(courierLocationRepository.save(courierLocation)).thenReturn(courierLocation);
        superAdminService.setCourierLimitByAmountOfBag(1L, dto);

        verify(courierLocationRepository).findCourierLocationsLimitsByCourierIdAndLocationId(1L, dto.getLocationId());
        verify(courierLocationRepository).save(courierLocation);
    }

    @Test
    void createCourier() {
        Courier courier = ModelUtils.getCourier();
        courier.setId(null);
        CreateCourierDto createCourierDto = ModelUtils.getCreateCourierDto();
        CourierLocation courierLocation = ModelUtils.getCourierLocations();

        when(locationRepository.findById(1L)).thenReturn(Optional.of(ModelUtils.getLocation()));
        when(languageRepository.findById(1L)).thenReturn(Optional.of(ModelUtils.getLanguage()));
        when(courierRepository.save(courier)).thenReturn(courier);
        when(courierTranslationRepository.saveAll(courier.getCourierTranslationList()))
            .thenReturn(ModelUtils.getCourierTranslations());
        when(courierLocationRepository.saveAll(courier.getCourierLocations())).thenReturn(List.of(courierLocation));
        when(modelMapper.map(courier, CreateCourierDto.class)).thenReturn(createCourierDto);

        assertEquals(createCourierDto, superAdminService.createCourier(createCourierDto));

        verify(locationRepository).findById(1L);
        verify(languageRepository).findById(1L);
        verify(courierRepository).save(courier);
        verify(courierTranslationRepository).saveAll(courier.getCourierTranslationList());
        verify(courierLocationRepository).saveAll(courier.getCourierLocations());
        verify(modelMapper).map(courier, CreateCourierDto.class);
    }

    @Test
    void setLimitDescription() {
        CourierTranslation courierTranslationTest =
            ModelUtils.getCourierTranslation(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG);
        when(courierRepository.findById(10L)).thenReturn(Optional.of(courierTranslationTest.getCourier()));
        when(courierTranslationRepository.findCourierTranslationByCourierAndLanguageId(
            courierTranslationTest.getCourier(), courierTranslationTest.getLanguage().getId()))
                .thenReturn(courierTranslationTest);
        assertEquals("LimitDescription",
            superAdminService.setLimitDescription(10L, "LimitDescription", courierTranslationTest.getLanguage().getId())
                .getLimitDescription());
    }

    @Test
    void includeBag() {
        BagTranslation bagTranslationTest = BagTranslation.builder()
            .id(2L)
            .bag(Bag.builder().id(2).capacity(120).price(350).build())
            .language(Language.builder().id(1L).code("en").build())
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

        when(languageRepository.findLanguageByLanguageCode("ua")).thenReturn(Optional.of(language));
        when(regionRepository.findRegionByName("Київська область")).thenReturn(Optional.of(region));

        superAdminService.addLocation(locationCreateDtoList);

        verify(languageRepository).findLanguageByLanguageCode("ua");
        verify(regionRepository).findRegionByName("Київська область");
    }

    @Test
    void addLocationCreateNewRegionTest() {
        List<LocationCreateDto> locationCreateDtoList = ModelUtils.getLocationCreateDtoList();
        Language language = ModelUtils.getLanguage();
        Region region = ModelUtils.getRegion();
        Location location = ModelUtils.getLocationForCreateRegion();
        List<LocationTranslation> locationTranslations = List.of(ModelUtils.getLocationTranslation());

        when(locationRepository.findLocationByName("Київ")).thenReturn(Optional.empty());
        when(languageRepository.findLanguageByLanguageCode("ua")).thenReturn(Optional.of(language));
        when(regionRepository.findRegionByName("Київська область")).thenReturn(Optional.empty());
        when(regionTranslationRepository.saveAll(region.getRegionTranslations()))
            .thenReturn(ModelUtils.getRegionTranslationsList());
        when(locationRepository.save(location)).thenReturn(location);
        when(locationTranslationRepository.saveAll(location.getLocationTranslations()))
            .thenReturn(locationTranslations);

        superAdminService.addLocation(locationCreateDtoList);

        verify(locationRepository).findLocationByName("Київ");
        verify(languageRepository, times(2)).findLanguageByLanguageCode("ua");
        verify(regionRepository).findRegionByName("Київська область");
        verify(regionTranslationRepository).saveAll(region.getRegionTranslations());
        verify(locationRepository).save(location);
        verify(locationTranslationRepository).saveAll(location.getLocationTranslations());
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
        LocationTranslation locationTranslation = ModelUtils.getLocationTranslation();

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
    void editInfoInTariff() {
        EditTariffInfoDto editTariffInfoDto = ModelUtils.editTariffInfoDto();
        Bag bag = ModelUtils.bagDto();
        CourierTranslation courierTranslation = CourierTranslation.builder().id(1L).build();
        CourierLocation courierLocation = ModelUtils.getCourierLocations();
        Language language = ModelUtils.getLanguage();

        when(bagRepository.findById(1)).thenReturn(Optional.of(bag));
        when(courierLocationRepository.findCourierLocationsLimitsByCourierIdAndLocationId(
            editTariffInfoDto.getCourierId(), editTariffInfoDto.getLocationId()))
                .thenReturn(ModelUtils.getCourierLocations());
        when(
            courierTranslationRepository.findCourierTranslationByCourierAndLanguageId(courierLocation.getCourier(), 1L))
                .thenReturn(courierTranslation);
        when(languageRepository.findById(1L)).thenReturn(Optional.ofNullable(language));

        Assertions.assertEquals(editTariffInfoDto, superAdminService.editInfoInTariff(editTariffInfoDto));

        verify(bagRepository).findById(1);
        verify(courierTranslationRepository).findCourierTranslationByCourierAndLanguageId(courierLocation.getCourier(),
            1L);
        verify(languageRepository).findById(1L);
        verify(courierLocationRepository).findCourierLocationsLimitsByCourierIdAndLocationId(
            editTariffInfoDto.getCourierId(), editTariffInfoDto.getLocationId());
    }

    @Test
    void addTariffServiceExceptionTest() {
        AddServiceDto dto = ModelUtils.addServiceDto();
        assertThrows(LocationNotFoundException.class, () -> superAdminService.addTariffService(dto, "uuid"));
    }

    @Test
    void addTariffServiceException2Test() {
        User user = ModelUtils.getUser();
        AddServiceDto dto = ModelUtils.addServiceDto();

        when(userRepository.findByUuid("123233")).thenReturn(user);
        when(locationRepository.findById(1L)).thenReturn(Optional.of(ModelUtils.getLocation()));
        when(locationRepository.findById(1L)).thenReturn(Optional.ofNullable(ModelUtils.getLocation()));

        assertThrows(LanguageNotFoundException.class, () -> superAdminService.addTariffService(dto, "123233"));
    }

    @Test
    void createServiceWithTranslationThrowLanguageNotFoundException() {
        CreateServiceDto createServiceDto = ModelUtils.getCreateServiceDto();
        Courier courier = ModelUtils.getCourier();
        User user = ModelUtils.getUser();

        when(courierRepository.findById(createServiceDto.getCourierId())).thenReturn(Optional.of(courier));
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
    void addLocationExceptionTest() {
        List<LocationCreateDto> locationCreateDtoList = ModelUtils.getLocationCreateDtoList();

        when(locationRepository.findLocationByName("Київ")).thenReturn(Optional.empty());
        when(languageRepository.findLanguageByLanguageCode("ua")).thenReturn(Optional.empty());

        assertThrows(LanguageNotFoundException.class, () -> superAdminService.addLocation(locationCreateDtoList));

        verify(languageRepository).findLanguageByLanguageCode("ua");
    }

    @Test
    void addLocationThrowLocationAlreadyCreatedExceptionTest() {
        List<LocationCreateDto> locationCreateDtoList = ModelUtils.getLocationCreateDtoList();
        Location location = ModelUtils.getLocation();

        when(locationRepository.findLocationByName("Київ")).thenReturn(Optional.of(location));
        assertThrows(LocationAlreadyCreatedException.class, () -> superAdminService.addLocation(locationCreateDtoList));

        verify(locationRepository).findLocationByName("Київ");
    }

    @Test
    void deactivateLocationExceptionTest() {
        assertThrows(LocationNotFoundException.class, () -> superAdminService.deactivateLocation(1L));
    }

    @Test
    void deactivateLocationException2Test() {
        Location location = ModelUtils.getLocationDto();
        location.setLocationStatus(LocationStatus.DEACTIVATED);
        LocationTranslation locationTranslation = ModelUtils.getLocationTranslation();

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
        LocationTranslation locationTranslation = ModelUtils.getLocationTranslation();

        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));

        assertThrows(LocationStatusAlreadyExistException.class, () -> superAdminService.activateLocation(1L));
    }

    @Test
    void setLimitDescriptionExceptiomTest() {
        assertThrows(CourierNotFoundException.class, () -> superAdminService.setLimitDescription(1L, "1", 1l));
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
    void editInfoInTariffExceptionTest() {
        EditTariffInfoDto editTariffInfoDto = ModelUtils.editTariffInfoDto();

        assertThrows(CourierNotFoundException.class, () -> superAdminService.editInfoInTariff(editTariffInfoDto));
    }

    @Test
    void editInfoInTariffThrowLanguageNotFoundExceptionTest() {
        EditTariffInfoDto editTariffInfoDto = ModelUtils.editTariffInfoDto();
        Optional<Bag> bag = Optional.ofNullable(ModelUtils.bagDto());
        CourierLocation courierLocation = ModelUtils.getCourierLocations();

        when(courierLocationRepository.findCourierLocationsLimitsByCourierIdAndLocationId(
            editTariffInfoDto.getCourierId(), editTariffInfoDto.getLocationId())).thenReturn(courierLocation);
        when(bagRepository.findById(1)).thenReturn(bag);

        assertThrows(LanguageNotFoundException.class, () -> superAdminService.editInfoInTariff(editTariffInfoDto));
    }

    @Test
    void addServiceThrowCourierNotFoundException() {
        CreateServiceDto dto = ModelUtils.getCreateServiceDto();
        when(courierRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CourierNotFoundException.class, () -> superAdminService.addService(dto, "123321"));

        verify(courierRepository).findById(1L);
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
    void addLocationToCourierTest() {
        NewLocationForCourierDto dto = ModelUtils.newLocationForCourierDto();
        CourierLocation courierLocation = ModelUtils.getCourierLocations();
        Courier courier = ModelUtils.getCourier();
        Location location = ModelUtils.getLocation();
        courierLocation.setCourier(courier);
        courierLocation.setLocation(location);

        when(modelMapper.map(dto, CourierLocation.class)).thenReturn(courierLocation);
        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(courierLocationRepository.save(courierLocation)).thenReturn(courierLocation);

        superAdminService.addLocationToCourier(dto);

        verify(modelMapper).map(dto, CourierLocation.class);
        verify(courierRepository).findById(1L);
        verify(locationRepository).findById(1L);
        verify(courierLocationRepository).save(courierLocation);
    }

    @Test
    void addLocationToCourierThrowCourierNotFoundExceptionTest() {
        NewLocationForCourierDto dto = ModelUtils.newLocationForCourierDto();
        CourierLocation courierLocation = ModelUtils.getCourierLocations();
        Courier courier = ModelUtils.getCourier();
        courierLocation.setCourier(courier);

        when(modelMapper.map(dto, CourierLocation.class)).thenReturn(courierLocation);
        when(courierRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CourierNotFoundException.class, () -> superAdminService.addLocationToCourier(dto));

        verify(modelMapper).map(dto, CourierLocation.class);
        verify(courierRepository).findById(1L);
    }

    @Test
    void addLocationToCourierThrowLocationNotFoundExceptionTest() {
        NewLocationForCourierDto dto = ModelUtils.newLocationForCourierDto();
        CourierLocation courierLocation = ModelUtils.getCourierLocations();
        Courier courier = ModelUtils.getCourier();
        Location location = ModelUtils.getLocation();
        courierLocation.setCourier(courier);
        courierLocation.setLocation(location);

        when(modelMapper.map(dto, CourierLocation.class)).thenReturn(courierLocation);
        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));
        when(locationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(LocationNotFoundException.class, () -> superAdminService.addLocationToCourier(dto));

        verify(modelMapper).map(dto, CourierLocation.class);
        verify(courierRepository).findById(1L);
        verify(locationRepository).findById(1L);
    }
}
