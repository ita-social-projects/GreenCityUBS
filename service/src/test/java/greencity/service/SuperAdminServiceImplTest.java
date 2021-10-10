package greencity.service;

import greencity.ModelUtils;
import greencity.dto.AddServiceDto;
import greencity.dto.EditTariffServiceDto;
import greencity.entity.language.Language;
import greencity.entity.order.Bag;
import greencity.entity.order.BagTranslation;
import greencity.entity.user.Location;
import greencity.entity.user.User;
import greencity.exceptions.BagNotFoundException;
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
        Location location = new Location();
        Language language = new Language();

        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(languageRepository.findById(any())).thenReturn(Optional.of(language));
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

}
