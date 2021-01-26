package greencity.dao.repository;

import greencity.dao.entity.user.ubs.Address;
import org.springframework.data.repository.CrudRepository;

public interface AddressRepository extends CrudRepository<Address, Long> {
}
