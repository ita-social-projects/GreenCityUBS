package greencity.service;

import greencity.ModelUtils;
import greencity.dto.*;
import greencity.entity.enums.CourierLimit;
import greencity.entity.enums.MinAmountOfBag;
import greencity.entity.language.Language;
import greencity.entity.order.*;
import greencity.entity.user.User;
import greencity.exceptions.BagNotFoundException;
import greencity.exceptions.LanguageNotFoundException;
import greencity.exceptions.LocationNotFoundException;
import greencity.repository.*;
import greencity.service.ubs.SuperAdminServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.Arrays;
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
    private CourierLocationRepository courierLocationRepository;

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
    void getService() {
        when(serviceTranslationRepository.findAll()).thenReturn(Arrays.asList(new ServiceTranslation()));
        List<ServiceTranslation> serviceTranslations = serviceTranslationRepository.findAll();

        verify(serviceTranslationRepository, times(1)).findAll();
        assertEquals(1, serviceTranslations.size());
    }

    @Test
    void editService() {
        String uuid = "testUUid";
        Service service = new Service();
        service.setId(1L);
        service.setCourier(ModelUtils.getCourier());
        User user = new User();
        EditServiceDto dto = ModelUtils.getEditServiceDto();
        Language language = new Language();
        ServiceTranslation serviceTranslation = new ServiceTranslation();

        when(serviceRepository.findServiceById(1L)).thenReturn(Optional.of(service));
        when(userRepository.findByUuid(uuid)).thenReturn(user);
        when(languageRepository.findLanguageByLanguageCode(dto.getLanguageCode()))
            .thenReturn(Optional.ofNullable(language));
        when(serviceTranslationRepository.findServiceTranslationsByServiceAndLanguageCode(service,
            dto.getLanguageCode())).thenReturn(serviceTranslation);
        when(serviceRepository.save(service)).thenReturn(service);
        GetServiceDto getTariffServiceDto = ModelUtils.getServiceDto();

        assertEquals(superAdminService.editService(service.getId(), dto, uuid), getTariffServiceDto);

        verify(userRepository, times(1)).findByUuid(uuid);
        verify(serviceRepository, times(1)).findServiceById(service.getId());
        verify(languageRepository, times(1)).findLanguageByLanguageCode(dto.getLanguageCode());
        verify(serviceTranslationRepository, times(1)).findServiceTranslationsByServiceAndLanguageCode(service,
            dto.getLanguageCode());
        verify(serviceRepository, times(1)).save(service);
    }

    @Test
    void addService() {
        Service service = ModelUtils.getService();
        when(courierRepository.findById(any())).thenReturn(Optional.of(ModelUtils.getCourier()));
        when(userRepository.findByUuid(ModelUtils.getUser().getUuid())).thenReturn(ModelUtils.getUser());
        when(languageRepository.findById(1L)).thenReturn(Optional.of(ModelUtils.getLanguage()));
        when(serviceRepository.save(service)).thenReturn(service);
        when(serviceTranslationRepository.saveAll(service.getServiceTranslations()))
            .thenReturn(ModelUtils.getServiceTranslationList());

        superAdminService.addService(ModelUtils.getCreateServiceDto(), ModelUtils.getUser().getUuid());

        verify(courierRepository).findById(any());
        verify(userRepository).findByUuid(ModelUtils.getUser().getUuid());
        verify(languageRepository).findById(1L);
        verify(serviceRepository).save(service);
        verify(serviceTranslationRepository).saveAll(service.getServiceTranslations());
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
        CourierLocations courierLocations = ModelUtils.getCourierLocations();
        when(courierLocationRepository.findCourierLocationsLimitsByCourierIdAndLocationId(1L, dto.getLocationId()))
            .thenReturn(courierLocations);
        when(courierLocationRepository.save(courierLocations)).thenReturn(courierLocations);

        superAdminService.setCourierLimitBySumOfOrder(1L, dto);

        verify(courierLocationRepository).findCourierLocationsLimitsByCourierIdAndLocationId(1L, dto.getLocationId());
        verify(courierLocationRepository).save(courierLocations);
    }

    @Test
    void setCourierLimitByAmountOfBag() {
        EditAmountOfBagDto dto = ModelUtils.getAmountOfBagDto();
        CourierLocations courierLocations = ModelUtils.getCourierLocations();

        when(courierLocationRepository.findCourierLocationsLimitsByCourierIdAndLocationId(1L, dto.getLocationId()))
            .thenReturn(courierLocations);
        when(courierLocationRepository.save(courierLocations)).thenReturn(courierLocations);
        superAdminService.setCourierLimitByAmountOfBag(1L, dto);

        verify(courierLocationRepository).findCourierLocationsLimitsByCourierIdAndLocationId(1L, dto.getLocationId());
        verify(courierLocationRepository).save(courierLocations);
    }

    @Test
    void createCourier() {
        Courier courier = ModelUtils.getCourier();
        CreateCourierDto createCourierDto = ModelUtils.getCreateCourierDto();
        CourierLocations courierLocations = ModelUtils.getCourierLocations();

        when(locationRepository.findById(1L)).thenReturn(Optional.of(ModelUtils.getLocation()));
        when(languageRepository.findById(1L)).thenReturn(Optional.of(ModelUtils.getLanguage()));
        when(courierRepository.save(courier)).thenReturn(courier);
        when(courierTranslationRepository.saveAll(courier.getCourierTranslationList()))
            .thenReturn(ModelUtils.getCourierTranslations());
        when(courierLocationRepository.saveAll(courier.getCourierLocations())).thenReturn(List.of(courierLocations));
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

}
