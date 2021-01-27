package greencity.service;

import greencity.dto.LanguageDto;

import java.util.List;

/**
 * LanguageService interface.
 *
 * @author Vitaliy Dzen
 */
public interface LanguageService {
    /**
     * Method finds all {@link LanguageDto}.
     *
     * @return List of all {@link LanguageDto}
     * @author Vitaliy Dzen
     */
    List<greencity.dto.LanguageDto> getAllLanguages();

    /**
     * Method for extracting language code from request param.
     *
     * @return language code
     */
    String extractLanguageCodeFromRequest();

    /**
     * Method for getting {@link LanguageDto} by code.
     *
     * @param code code of language.
     * @return {@link LanguageDto} by language code.
     */
    LanguageDto findByCode(String code);

    /**
     * method, that returns codes of all languages.
     *
     * @return {@link List} of language code strings.
     */
    List<String> findAllLanguageCodes();
}
