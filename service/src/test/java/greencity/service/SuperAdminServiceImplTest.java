package greencity.service;

import greencity.ModelUtils;
import greencity.dto.*;
import greencity.entity.enums.CourierLimit;
import greencity.entity.language.Language;
import greencity.entity.order.Bag;
import greencity.entity.order.BagTranslation;
import greencity.entity.order.Courier;
import greencity.entity.order.CourierTranslation;
import greencity.entity.user.Location;
import greencity.entity.user.User;
import greencity.exceptions.BagNotFoundException;
import greencity.exceptions.CourierNotFoundException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private CourierRepository courierRepository;
    @Mock
    private CourierTranslationRepository courierTranslationRepository;

    @Test
    void addServiceTest() {
        User user = ModelUtils.getUser();
        Bag bag = new Bag();
        AddServiceDto dto = ModelUtils.addServiceDto();
        Language language = new Language();
        BagTranslation bagTranslation = new BagTranslation();
        Location location = new Location();

        when(languageRepository.findById(1L)).thenReturn(Optional.of(language));
        when(locationRepository.findById(any())).thenReturn(Optional.of(location));
        when(modelMapper.map(dto, Bag.class)).thenReturn(bag);
        when(modelMapper.map(dto, BagTranslation.class)).thenReturn(bagTranslation);
        when(userRepository.findByUuid("123233")).thenReturn(user);
        when(bagRepository.save(bag)).thenReturn(bag);
        when(bagTranslationRepository.save(bagTranslation)).thenReturn(bagTranslation);

        superAdminService.addTariffService(dto, "123233");

        verify(modelMapper).map(dto, Bag.class);
        verify(modelMapper).map(dto, BagTranslation.class);
        verify(bagRepository).save(bag);
        verify(bagTranslationRepository).save(bagTranslation);
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
    void createCourierThrowException_LocationNotFoundException(){
        assertThrows(LocationNotFoundException.class, () ->
                superAdminService.createCourier(new CreateCourierDto()));
    }

    @Test
    void createCourierThrowException_LanguageNotFoundException(){

        Location location = new Location();
        when(locationRepository.findById(any())).thenReturn(Optional.of(location));
        assertThrows(LanguageNotFoundException.class, () ->
                superAdminService.createCourier(new CreateCourierDto()));
    }

    @Test
    void setCourierLimitBySumOfOrderThrowException(){
        when(courierRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(CourierNotFoundException.class, () ->
                superAdminService.setCourierLimitBySumOfOrder(1L, new EditPriceOfOrder()));
    }

    @Test
    void setCourierLimitByAmountOfBagThrowException(){
        when(courierRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(CourierNotFoundException.class, () ->
                superAdminService.setCourierLimitByAmountOfBag(1L, new EditAmountOfBagDto()));
    }
    @Test
    void getAllCouriers(){
        when(courierTranslationRepository.findAll()).thenReturn(new ArrayList<CourierTranslation>());
        superAdminService.getAllCouriers();
        verify(courierTranslationRepository).findAll();
    }

    @Test
    void setCourierLimitBySumOfOrder(){
        Courier courier = ModelUtils.getCourier(CourierLimit.LIMIT_BY_SUM_OF_ORDER);
        CourierTranslation courierTranslation = ModelUtils.getCourierTranslation(CourierLimit.LIMIT_BY_SUM_OF_ORDER);
        when(courierRepository.findById(any())).thenReturn(Optional.of(courier));
        when(courierTranslationRepository.findCourierTranslationByCourier(courier))
                .thenReturn(courierTranslation);
        superAdminService.setCourierLimitBySumOfOrder(1L, new EditPriceOfOrder());
        verify(courierTranslationRepository).findCourierTranslationByCourier(courier);
        verify(courierRepository).save(courier);
    }

    @Test
    void setCourierLimitByAmountOfBag(){
        Courier courier = ModelUtils.getCourier(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG);
        CourierTranslation courierTranslation = ModelUtils.getCourierTranslation(CourierLimit.LIMIT_BY_AMOUNT_OF_BAG);
        when(courierRepository.findById(any())).thenReturn(Optional.of(courier));
        when(courierTranslationRepository.findCourierTranslationByCourier(courier))
                .thenReturn(courierTranslation);
        superAdminService.setCourierLimitByAmountOfBag(1L, new EditAmountOfBagDto());
        verify(courierTranslationRepository).findCourierTranslationByCourier(courier);
        verify(courierRepository).save(courier);
    }

    @Test
    void createCourier(){
        Courier courier = ModelUtils.getCourier(CourierLimit.LIMIT_BY_SUM_OF_ORDER);
        CourierTranslation courierTranslation = ModelUtils.getCourierTranslation(
                CourierLimit.LIMIT_BY_SUM_OF_ORDER);
        CreateCourierDto createCourierDto = ModelUtils.getCreateCourierDto();
        Location location = ModelUtils.getLocation();
        Language language = ModelUtils.getLanguage();
        when(locationRepository.findById(any())).thenReturn(Optional.of(location));
        when(languageRepository.findLanguageByLanguageCode(any())).thenReturn(Optional.of(language));
        when(modelMapper.map(createCourierDto, Courier.class)).thenReturn(courier);
        when(modelMapper.map(createCourierDto, CourierTranslation.class)).thenReturn(courierTranslation);
        superAdminService.createCourier(createCourierDto);
        verify(locationRepository).findById(1L);
        verify(languageRepository).findLanguageByLanguageCode(any());
        verify(modelMapper).map(createCourierDto, Courier.class);
        verify(modelMapper).map(createCourierDto, CourierTranslation.class);
        verify(courierRepository).save(courier);
        verify(courierTranslationRepository).save(courierTranslation);
    }
}
