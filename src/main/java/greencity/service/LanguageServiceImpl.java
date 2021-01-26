package greencity.service;

import greencity.dao.entity.lang.Language;
import greencity.dao.repository.LanguageRepo;
import greencity.dto.LanguageDTO;
import greencity.exceptions.LanguageNotFoundException;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Implementation of {@link LanguageService}.
 *
 * @author Oleh Kopylchak
 * @author Vitaliy Dzen
 */
@Service
public class LanguageServiceImpl implements LanguageService {
    private final LanguageRepo languageRepo;
    private final ModelMapper modelMapper;
    private HttpServletRequest request;

    /**
     * Constructor with parameters.
     *
     * @author Vitaliy Dzen
     */
    @Autowired
    public LanguageServiceImpl(
            LanguageRepo languageRepo,
            @Lazy ModelMapper modelMapper, HttpServletRequest request) {
        this.languageRepo = languageRepo;
        this.modelMapper = modelMapper;
        this.request = request;
    }

    /**
     * Method finds all {@link Language}.
     *
     * @return List of all {@link LanguageDTO}
     * @author Vitaliy Dzen
     */
    @Override
    public List<LanguageDTO> getAllLanguages() {
        return modelMapper.map(languageRepo.findAll(), new TypeToken<List<LanguageDTO>>() {
        }.getType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String extractLanguageCodeFromRequest() {
        String languageCode = request.getParameter("language");

        if (languageCode == null) {
            return "en";
        }

        return languageCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LanguageDTO findByCode(String code) {
        Language language = languageRepo.findByCode(code)
                .orElseThrow(() -> new LanguageNotFoundException("Given language code is not supported."));
        return modelMapper.map(language, LanguageDTO.class);
    }

    /**
     * method, that returns codes of all {@link Language}s.
     *
     * @return {@link List} of language code strings.
     */
    @Override
    public List<String> findAllLanguageCodes() {
        return languageRepo.findAllLanguageCodes();
    }
}
