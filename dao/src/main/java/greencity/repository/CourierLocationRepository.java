package greencity.repository;

import greencity.entity.order.CourierLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourierLocationRepository extends JpaRepository<CourierLocation, Long> {
    /**
     * Method get info about limits of courier and courier translation in all
     * locations.
     *
     * @param courierId    - selected courier
     * @param languageCode - selected language
     * @return {@link CourierLocation}
     * @author Vadym Makitra
     */
    @Query(nativeQuery = true, value = "select * from courier_locations cl "
        + "join courier c on cl.courier_id = c.id "
        + "join languages lang on lang.code = :languageCode "
        + "join courier_translations ct on c.id = ct.courier_id "
        + "join locations l on cl.location_id = l.id "
        + "join location_translations lt on l.id = lt.location_id "
        + "where  cl.courier_id = :courierId and c.courier_status = 'ACTIVE' and lt.language_id = lang.id "
        + "and ct.language_id = lang.id  and l.location_status != 'DEACTIVATED'")
    List<CourierLocation> findCourierLocationsByCourierIdAndLanguageCode(@Param("courierId") Long courierId,
        @Param("languageCode") String languageCode);

    /**
     * Method find info about courier and courier limit in selected location.
     *
     * @param courierId  - id of selected courier
     * @param locationId - if of selected location
     * @return {@link CourierLocation}
     * @author Vadym Makitra
     */
    @Query(nativeQuery = true,
        value = "select * from courier_locations cl where cl.courier_id = :courierId and cl.location_id = :locationId")
    CourierLocation findCourierLocationsLimitsByCourierIdAndLocationId(@Param("courierId") Long courierId,
        @Param("locationId") Long locationId);

    /**
     * Method get info about all {@link greencity.entity.order.Courier}.
     *
     * @return {@link CourierLocation}
     * @author Vadym Makitra
     */
    @Query(nativeQuery = true, value = "select * from courier_locations cl "
        + "join courier c2 on cl.courier_id = c2.id "
        + "join locations l on cl.location_id = l.id "
        + "where c2.courier_status != 'DELETED' and l.location_status != 'DEACTIVATED' ")
    List<CourierLocation> findAllInfoAboutCourier();
}
