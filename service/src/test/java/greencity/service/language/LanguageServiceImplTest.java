package greencity.service.language;

import greencity.repository.LanguageRepository;
import greencity.service.language.LanguageServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static greencity.ModelUtils.TEST_ALL_LANGUAGE_CODE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LanguageServiceImplTest {

    @Mock
    private LanguageRepository languageRepo;

    @InjectMocks
    private LanguageServiceImpl languageService;

    @Test
    void findAllLanguageCodesTest() {
        when(languageRepo.findAllLanguageCodes()).thenReturn(TEST_ALL_LANGUAGE_CODE);

        List<String> actual = languageService.findAllLanguageCodes();

        assertEquals(TEST_ALL_LANGUAGE_CODE, actual);

        verify(languageRepo).findAllLanguageCodes();
    }

}