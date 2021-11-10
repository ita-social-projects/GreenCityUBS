package greencity.service;

import greencity.ModelUtils;
import greencity.constant.ErrorMessage;
import greencity.dto.*;
import greencity.entity.enums.CourierLimit;
import greencity.entity.enums.MinAmountOfBag;
import greencity.entity.language.Language;
import greencity.entity.order.*;
import greencity.entity.user.Location;
import greencity.entity.user.User;
import greencity.exceptions.BagNotFoundException;
import greencity.exceptions.CourierNotFoundException;
import greencity.exceptions.LanguageNotFoundException;
import greencity.exceptions.LocationNotFoundException;
import greencity.repository.*;
import greencity.service.ubs.SuperAdminServiceImpl;
import org.junit.Assert;
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
    private  BagTranslationRepository translationRepository;

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
        Bag bag = ModelUtils.getBag().get();
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
        User user = new User();
        EditServiceDto dto = ModelUtils.getEditServiceDto();
        Location location = ModelUtils.getLocation();
        Language language = new Language();
        ServiceTranslation serviceTranslation = new ServiceTranslation();

        when(serviceRepository.findById(service.getId())).thenReturn(Optional.of(service));

        when(userRepository.findByUuid(uuid)).thenReturn(user);

        when(locationRepository.findById(dto.getLocationId())).thenReturn(Optional.ofNullable(location));

        when(languageRepository.findLanguageByLanguageCode(dto.getLanguageCode()))
            .thenReturn(Optional.ofNullable(language));

        when(serviceTranslationRepository.findServiceTranslationsByServiceAndLanguageCode(service,
            dto.getLanguageCode())).thenReturn(serviceTranslation);

        when(serviceRepository.save(service)).thenReturn(service);

        superAdminService.editService(service.getId(), dto, uuid);

        verify(userRepository, times(1)).findByUuid(uuid);
        verify(serviceRepository, times(1)).findById(service.getId());
        verify(locationRepository, times(1)).findById(dto.getLocationId());
        verify(languageRepository, times(1)).findLanguageByLanguageCode(dto.getLanguageCode());
        verify(serviceTranslationRepository, times(1)).findServiceTranslationsByServiceAndLanguageCode(service,
            dto.getLanguageCode());
        verify(serviceRepository, times(1)).save(service);
    }

    @Test
    void addService() {

        User user = ModelUtils.getUser();
        Service service = ModelUtils.getService();
        CreateServiceDto dto = ModelUtils.getCreateServiceDto();

        when(userRepository.findByUuid("123233")).thenReturn(user);
        when(locationRepository.findById(1L)).thenReturn(Optional.of(ModelUtils.getLocation()));
        when(languageRepository.findById(1L)).thenReturn(Optional.of(ModelUtils.getLanguage()));
        when(serviceRepository.save(service)).thenReturn(service);
        when(serviceTranslationRepository.saveAll(service.getServiceTranslations()))
            .thenReturn(ModelUtils.getServiceTranslation());

        superAdminService.addService(dto, "123233");

        verify(locationRepository).findById(1L);
        verify(languageRepository).findById(1L);
//        verify(bagRepository).save(bag);
//        verify(bagTranslationRepository).saveAll(bag.getBagTranslations());
    }

    @Test
    void createCourierThrowException_LocationNotFoundException() {
        assertThrows(LocationNotFoundException.class, () -> superAdminService.createCourier(new CreateCourierDto()));
    }

    @Test
    void createCourierThrowException_LanguageNotFoundException() {

        Location location = new Location();
        when(locationRepository.findById(any())).thenReturn(Optional.of(location));
        when(languageRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(LanguageNotFoundException.class,
            () -> superAdminService.createCourier(ModelUtils.getCreateCourierDto()));
    }

    @Test
    void setCourierLimitBySumOfOrderThrowException() {
        when(courierRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(CourierNotFoundException.class,
            () -> superAdminService.setCourierLimitBySumOfOrder(1L, new EditPriceOfOrder()));
    }

    @Test
    void setCourierLimitByAmountOfBagThrowException() {
        when(courierRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(CourierNotFoundException.class,
            () -> superAdminService.setCourierLimitByAmountOfBag(1L, new EditAmountOfBagDto()));
    }

    @Test
    void getAllCouriers() {
        when(courierTranslationRepository.findAll()).thenReturn(new ArrayList<CourierTranslation>());
        superAdminService.getAllCouriers();
        verify(courierTranslationRepository).findAll();
    }

    @Test
    void setCourierLimitBySumOfOrder() {
        Courier courier = ModelUtils.getCourier(CourierLimit.LIMIT_BY_SUM_OF_ORDER);
        CourierTranslation courierTranslation = ModelUtils.getCourierTranslation(CourierLimit.LIMIT_BY_SUM_OF_ORDER);
        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));
        when(courierTranslationRepository.findCourierTranslationByCourierAndLanguageId(courier, null))
            .thenReturn(courierTranslation);
        superAdminService.setCourierLimitBySumOfOrder(1L, new EditPriceOfOrder());
        verify(courierTranslationRepository).findCourierTranslationByCourierAndLanguageId(courier, null);
        verify(courierRepository).save(courier);
    }

    @Test
    void setCourierLimitByAmountOfBag() {
        Courier courier = ModelUtils.getCourier(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG);
        CourierTranslation courierTranslation = ModelUtils.getCourierTranslation(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG);
        when(courierRepository.findById(1L)).thenReturn(Optional.of(courier));
        when(courierTranslationRepository.findCourierTranslationByCourierAndLanguageId(courier, null))
            .thenReturn(courierTranslation);
        superAdminService.setCourierLimitByAmountOfBag(1L, new EditAmountOfBagDto());
        verify(courierTranslationRepository).findCourierTranslationByCourierAndLanguageId(courier, null);
        verify(courierRepository).save(courier);
    }

    @Test
    void createCourier() {
        Courier courier = ModelUtils.getCourier(CourierLimit.LIMIT_BY_SUM_OF_ORDER);
        CourierTranslation courierTranslation = ModelUtils.getCourierTranslation(
            CourierLimit.LIMIT_BY_SUM_OF_ORDER);
        CreateCourierDto createCourierDto = ModelUtils.getCreateCourierDto();

        Service service = ModelUtils.getService();

        when(locationRepository.findById(1L)).thenReturn(Optional.of(ModelUtils.getLocation()));
        when(languageRepository.findById(1L)).thenReturn(Optional.of(ModelUtils.getLanguage()));
        when(courierRepository.save(courier)).thenReturn(courier);
        when(courierTranslationRepository.saveAll(courier.getCourierTranslationList()))
            .thenReturn(ModelUtils.getCourierTranslations());

        superAdminService.createCourier(createCourierDto);

        verify(locationRepository).findById(1L);
        verify(languageRepository).findById(1L);
    }

    @Test
    void setLimitDescription(){
        CourierTranslation courierTranslationTest = ModelUtils.getCourierTranslation(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG);
        when(courierRepository.findById(10L)).thenReturn(Optional.of(courierTranslationTest.getCourier()));
        when(courierTranslationRepository.findCourierTranslationByCourierAndLanguageId(courierTranslationTest.getCourier(), courierTranslationTest.getLanguage().getId())).thenReturn(courierTranslationTest);
        Assert.assertEquals("LimitDescription", superAdminService.setLimitDescription(10L, "LimitDescription", courierTranslationTest.getLanguage().getId()).getLimitDescription());
    }

    @Test
    void includeBag(){
        BagTranslation bagTranslationTest = ModelUtils.getBagTranslation();
        bagTranslationTest.getBag().setMinAmountOfBags(MinAmountOfBag.EXCLUDE);
        bagTranslationTest.getBag().setLocation(ModelUtils.getLocation());
        when(bagRepository.findById(10)).thenReturn(Optional.of(bagTranslationTest.getBag()));
        when(translationRepository.findBagTranslationByBag(bagTranslationTest.getBag())).thenReturn(bagTranslationTest);
        Assert.assertEquals(MinAmountOfBag.INCLUDE.toString(), superAdminService.includeBag(10).getMinAmountOfBag());
    }
}
