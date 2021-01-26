package greencity.dao.repository;

import greencity.dao.entity.order.Bag;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BagRepository extends CrudRepository<Bag, Integer> {
}
