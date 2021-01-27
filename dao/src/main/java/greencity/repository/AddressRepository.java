package greencity.repository;

import greencity.entity.user.ubs.Address;
import org.springframework.data.repository.CrudRepository;

public interface AddressRepository extends CrudRepository<Address, Long> {
}
