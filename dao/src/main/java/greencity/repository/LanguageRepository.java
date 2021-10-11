package greencity.repository;

import greencity.entity.language.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Provides an interface to manage {@link Language} entity.
 *
 * @author Veremchuk Zahar
 */
@Repository
public interface LanguageRepository extends JpaRepository<Language, Long> {
    /**
     * method, that returns {@link List}of{@link String} of codes.
     *
     * 
     * @return {@link List}of{@link String} with all codes.
     * @author Veremchuk Zahar
     */
    @Query("SELECT code FROM Language")
    List<String> findAllLanguageCodes();

    /**
     * Find language by it's code.
     */
    Language findLanguageByCode(String code);

    /**
     * Methods, that returns {@link Language} by language code.
     * 
     * @param langCode - lenguage code.
     * @return {@link Language}
     * @author Vadym Makitra
     */
    @Query(value = "SELECT * FROM Languages  where code = :langCode", nativeQuery = true)
    Optional<Language> findLanguageByLanguageCode(String langCode);
}
