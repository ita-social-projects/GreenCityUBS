package greencity.repository;

import greencity.entity.lang.Language;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LanguageRepo extends CrudRepository<Language, Long> {
    /**
     * method, that returns {@link Language} by it's code.
     *
     * @param languageCode code of the language.
     * @return {@link Language} by it's code.
     */
    Optional<Language> findByCode(String languageCode);

    /**
     * method, that returns codes of all {@link Language}s.
     *
     * @return {@link List} of language code strings.
     */
    @Query("SELECT code FROM Language")
    List<String> findAllLanguageCodes();
}
