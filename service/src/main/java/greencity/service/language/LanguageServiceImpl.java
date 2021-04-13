package greencity.service.language;

import greencity.repository.LanguageRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link LanguageService}.
 *
 * @author Veremchuk Zahar
 */
@Service
public class LanguageServiceImpl implements LanguageService {
    private final LanguageRepository languageRepo;

    /**
     * Constructor with parameters.
     *
     * @author Veremchuk Zahar
     */
    public LanguageServiceImpl(LanguageRepository languageRepo) {
        this.languageRepo = languageRepo;
    }

    /**
     * method, that returns codes of all {@link greencity.entity.language.Language}.
     *
     * @return {@link List} of language code strings.
     */
    @Override
    public List<String> findAllLanguageCodes() {
        return languageRepo.findAllLanguageCodes();
    }
}
