package greencity.service.language;

import java.util.List;

public interface LanguageService {
    /**
     * Method that return all language codes.
     *
     *
     * @return {@link List} of {@link String}.
     *
     */
    List<String> findAllLanguageCodes();
}
