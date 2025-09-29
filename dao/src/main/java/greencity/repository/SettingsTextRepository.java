package greencity.repository;

import greencity.entity.admin.SettingsText;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SettingsTextRepository extends JpaRepository<SettingsText, Long> {
    List<SettingsText> findAllBySectionIgnoreCase(String section);

    Optional<SettingsText> findBySectionAndField(String section, String field);
}
