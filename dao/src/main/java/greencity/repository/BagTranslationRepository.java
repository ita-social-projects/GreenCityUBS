package greencity.repository;

import greencity.entity.order.BagTranslation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Provides an interface to manage {@link BagTranslation} entity.
 *
 * @author Veremchuk Zahar
 */
public interface BagTranslationRepository extends JpaRepository<BagTranslation, Long> {
    /**
     * method, that returns {@link List}of{@link BagTranslation} that have language.
     *
     * @param language code of the language.
     * @return {@link List}of{@link BagTranslation} by it's language.
     * @author Veremchuk Zahar
     */
    @Query(nativeQuery = true,
        value = "SELECT * FROM bag_translations b LEFT JOIN languages l ON b.language_id = l.id "
            + "WHERE l.code = :language")
    List<BagTranslation> findAllByLanguage(String language);
}
