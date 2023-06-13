package greencity.repository;

import greencity.entity.order.OrderBag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderBagRepository extends JpaRepository<OrderBag, Long> {
}
