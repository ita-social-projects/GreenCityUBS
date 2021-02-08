package greencity.repository;

import greencity.entity.coords.Coordinates;
import greencity.entity.lang.Language;
import greencity.entity.user.ubs.Address;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends CrudRepository<Address, Long> {
    /**
     * Method returns {@link Coordinates} of undelivered orders.
     *
     * @return list of {@link Coordinates}.
     */
    @Query("select a.coordinates from Address a inner join UBSuser u on a = u.userAddress "
        + "inner join Order o on u = o.ubsUser "
        + "where o.orderStatus = 'NEW' and a.coordinates is not null")
    List<Coordinates> undeliveredOrdersCoords();
}
