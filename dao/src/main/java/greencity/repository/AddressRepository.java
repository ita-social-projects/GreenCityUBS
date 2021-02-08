package greencity.repository;

import greencity.entity.coords.Coordinates;
import greencity.entity.user.ubs.Address;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends CrudRepository<Address, Long> {
    @Query("select a.coordinates from Address a inner join UBSuser u on a = u.userAddress "
        + "inner join Order o on u = o.ubsUser "
        + "where o.orderStatus = 'NEW' and a.coordinates is not null")
    List<Coordinates> undeliveredOrdersCoords();
}
