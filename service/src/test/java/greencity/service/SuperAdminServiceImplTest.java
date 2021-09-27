package greencity.service;

import greencity.ModelUtils;
import greencity.dto.AddServiceDto;
import greencity.entity.language.Language;
import greencity.entity.order.Bag;
import greencity.entity.order.BagTranslation;
import greencity.entity.user.User;
import greencity.repository.BagRepository;
import greencity.repository.BagTranslationRepository;
import greencity.repository.UserRepository;
import greencity.service.ubs.SuperAdminServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SuperAdminServiceImplTest {
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

        superAdminService.addService(dto, "123233");

        verify(modelMapper).map(dto, Bag.class);
        verify(modelMapper).map(dto, BagTranslation.class);
        verify(bagRepository).save(bag);
        verify(bagTranslationRepository).save(bagTranslation);

    }
}
