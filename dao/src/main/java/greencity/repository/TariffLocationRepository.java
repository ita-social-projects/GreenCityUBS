package greencity.repository;

import greencity.entity.order.TariffLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TariffLocationRepository extends JpaRepository<TariffLocation, Long> {
}
