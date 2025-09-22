package greencity.repository;

import greencity.entity.telegram.MessageAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface MessageAssetRepository extends JpaRepository<MessageAsset, Long> {
    /**
     * Returns all distinct non-null image paths.
     *
     * @return {@link List} of distinct image paths.
     */
    @Query(value = "SELECT DISTINCT ma.url FROM MessageAsset ma WHERE ma.url IS NOT NULL")
    List<String> findDistinctImagePaths();
}
