package greencity.service;

import greencity.ModelUtils;
import greencity.dto.LanguageDto;
import greencity.entity.lang.Language;
import greencity.exceptions.LanguageNotFoundException;
import greencity.repository.LanguageRepo;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
class LanguageServiceImplTest {
    @Mock
    private ModelMapper modelMapper;

    @Mock
    private LanguageRepo languageRepo;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private LanguageServiceImpl languageService;

    private Language language = ModelUtils.getLanguage();

    @Test
    void extractExistingLanguageCodeFromRequest() {
        String expectedLanguageCode = "ua";

        when(request.getParameter("language")).thenReturn(expectedLanguageCode);
        assertEquals(expectedLanguageCode, languageService.extractLanguageCodeFromRequest());
    }

    @Test
    void findAllLanguageCodes() {
        List<String> code = Collections.singletonList(language.getCode());
        when(languageRepo.findAllLanguageCodes()).thenReturn(code);
        assertEquals(code, languageService.findAllLanguageCodes());
    }

    @Test
    void extractNotExistingLanguageCodeFromRequest() {
        when(request.getParameter("language")).thenReturn(null);
        Assertions.assertEquals("en", languageService.extractLanguageCodeFromRequest());
    }

    @Test
    void findByCode() {
        LanguageDto dto = new LanguageDto(1L, "en");
        when(languageRepo.findByCode(language.getCode())).thenReturn(Optional.of(language));
        when(modelMapper.map(language, LanguageDto.class)).thenReturn(dto);
        assertEquals(dto, languageService.findByCode(language.getCode()));
    }

    @Test
    void findCodeByIdFailed() {
        Assertions
            .assertThrows(LanguageNotFoundException.class,
                () -> languageService.findByCode("ua"));
    }

}
