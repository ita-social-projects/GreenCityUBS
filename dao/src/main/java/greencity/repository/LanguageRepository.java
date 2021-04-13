package greencity.repository;

import greencity.entity.language.Language;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
}
