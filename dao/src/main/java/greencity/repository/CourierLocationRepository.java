package greencity.repository;

import greencity.entity.order.CourierLocations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourierLocationRepository extends JpaRepository<CourierLocations, Long> {
    @Query(nativeQuery = true, value = "select * from courier_locations cl "
        + "join courier c on cl.courier_id = c.id "
        + "join languages lang on lang.code = :languageCode "
        + "join courier_translations ct on c.id = ct.courier_id "
        + "join locations l on cl.location_id = l.id "
        + "join location_translations lt on l.id = lt.location_id and lang.id = lt.language_id "
        + "where c.id = :courierId and lt.language_id = lang.id and ct.language_id = lang.id and  c.courier_status != 'DELETED' and l.location_status != 'DEACTIVATED' ")
    List<CourierLocations> findCourierLocationsByCourierIdAndLanguageCode(@Param("courierId") Long courierId,
        @Param("languageCode") String languageCode);

    @Query(nativeQuery = true,
        value = "select * from courier_locations cl where cl.courier_id = :courierId and cl.location_id = :locationId")
    CourierLocations findCourierLocationsLimitsByCourierIdAndLocationId(@Param("courierId") Long courierId,
        @Param("locationId") Long locationId);

    @Query(nativeQuery = true, value = "select * from courier_locations cl "
        + "join courier c2 on cl.courier_id = c2.id "
        + "join locations l on cl.location_id = l.id "
        + "where c2.courier_status != 'DELETED' and l.location_status != 'DEACTIVATED' ")
    List<CourierLocations> findAllInfoAboutCourier();

}
