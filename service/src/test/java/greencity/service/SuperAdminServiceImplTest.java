package greencity.service;

import greencity.ModelUtils;
import greencity.dto.AddServiceDto;
import greencity.entity.language.Language;
import greencity.entity.order.Bag;
import greencity.entity.order.BagTranslation;
import greencity.entity.user.User;
import greencity.exceptions.BagNotFoundException;
import greencity.repository.BagRepository;
import greencity.repository.BagTranslationRepository;
import greencity.repository.UserRepository;
import greencity.service.ubs.SuperAdminServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;

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
    private ModelMapper modelMapper;

    @Test
    void addServiceTest() {
        User user = ModelUtils.getUser();
        Bag bag = new Bag();
        AddServiceDto dto = ModelUtils.addServiceDto();
        Language language = new Language();
        language.setId(dto.getLanguageId());
        BagTranslation bagTranslation = new BagTranslation();

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
        BagTranslation bagTranslation = new BagTranslation();
        Bag bag = new Bag();
        bag.setId(1);
        bagTranslation.setBag(bag);
        superAdminService.deleteTariffService(1L);
        verify(bagRepository).deleteById(1);
        verify(bagTranslationRepository).delete(bagTranslation);
    }

    @Test
    void deleteTariffServiceThrowException() {
        doThrow(new BagNotFoundException("rewrewr")).when(bagRepository).deleteById(1);
        Assertions.assertThrows(BagNotFoundException.class, () -> superAdminService.deleteTariffService(1L));
    }
}
