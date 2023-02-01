package greencity.repository;

import greencity.entity.order.TariffLocation;
import greencity.entity.order.TariffsInfo;
import greencity.enums.LocationStatus;
import greencity.enums.TariffStatus;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
public class DeactivateChosenEntityRepository {
    private final EntityManager entityManager;
    private static final String REGION_ID = "regionId";
    private static final String REGIONS_ID = "regionsId";
    private static final String CITIES_ID = "citiesId";
    private static final String COURIER_ID = "courierId";
    private static final String STATIONS_ID = "stationsId";
    private static final String TARIFFS_ID = "tariffsId";

    /**
     * Constructor to initialize EntityManager.
     */
    public DeactivateChosenEntityRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Method that deactivate tariffs and cities for list of regions id.
     *
     * @param regionsId - list of regions id.
     * @author Nikita Korzh.
     */
    public void deactivateTariffsByRegions(List<Long> regionsId) {
        entityManager.createQuery("update Location l set l.locationStatus = 'DEACTIVATED' "
            + "where l.region.id in (:regionsId)")
            .setParameter(REGIONS_ID, regionsId)
            .executeUpdate();

        entityManager.createQuery("select tl from TariffLocation tl left join tl.location l "
            + "where l.region.id in (:regionsId)", TariffLocation.class)
            .setParameter(REGIONS_ID, regionsId)
            .getResultList()
            .forEach(locationStatus -> locationStatus.setLocationStatus(LocationStatus.DEACTIVATED));

        List<Long> tariffsId = entityManager.createQuery("select t1.tariffsInfo.id from TariffLocation t1"
            + " group by t1.tariffsInfo.id having count(t1) ="
            + " sum (case when t1.locationStatus = 'DEACTIVATED' then 1 else 0 end)", Long.class).getResultList();

        if (!tariffsId.isEmpty()) {
            entityManager.createQuery("update TariffsInfo ti set ti.tariffStatus = 'DEACTIVATED'"
                + " where ti.id in(:tariffsId)")
                .setParameter(TARIFFS_ID, tariffsId)
                .executeUpdate();
        }
    }

    /**
     * Method that deactivate tariffs and cities for list of cities id and region
     * id.
     *
     * @param citiesId - list of cities id.
     * @param regionId - region id.
     * @author Nikita Korzh.
     */
    public void deactivateTariffsByRegionsAndCities(List<Long> citiesId, Long regionId) {
        entityManager.createQuery("update Location l set l.locationStatus = 'DEACTIVATED'"
            + " where l.region.id =: regionId and l.id in (:citiesId)")
            .setParameter(REGION_ID, regionId)
            .setParameter(CITIES_ID, citiesId)
            .executeUpdate();

        entityManager.createQuery("select tl from TariffLocation tl left join tl.location l "
            + "where l.region.id =: regionId and tl.location.id in (:citiesId)", TariffLocation.class)
            .setParameter(REGION_ID, regionId)
            .setParameter(CITIES_ID, citiesId)
            .getResultList()
            .forEach(tariffLocation -> tariffLocation.setLocationStatus(LocationStatus.DEACTIVATED));

        List<Long> tariffsId = entityManager.createQuery("select t1.tariffsInfo.id from TariffLocation t1"
            + " group by t1.tariffsInfo.id having count(t1) ="
            + " sum (case when t1.locationStatus = 'DEACTIVATED' then 1 else 0 end)", Long.class).getResultList();

        if (!tariffsId.isEmpty()) {
            entityManager.createQuery("update TariffsInfo ti set"
                + " ti.tariffStatus = 'DEACTIVATED' where ti.id in(:tariffsId)")
                .setParameter(TARIFFS_ID, tariffsId)
                .executeUpdate();
        }
    }

    /**
     * Method that deactivate tariffs and courier for courier id.
     *
     * @param courierId - courier id.
     * @author Nikita Korzh.
     */
    public void deactivateTariffsByCourier(Long courierId) {
        entityManager.createQuery("update Courier c set c.courierStatus = 'DEACTIVATED' where c.id =:courierId")
            .setParameter(COURIER_ID, courierId)
            .executeUpdate();

        entityManager.createQuery("update TariffsInfo ti set ti.tariffStatus = 'DEACTIVATED'"
            + "where ti.courier.id =: courierId")
            .setParameter(COURIER_ID, courierId)
            .executeUpdate();
    }

    /**
     * Method that deactivate tariffs and receiving stations for list of receiving
     * stations id.
     *
     * @param stationsId - list of receiving stations id.
     * @author Nikita Korzh.
     */
    public void deactivateTariffsByReceivingStations(List<Long> stationsId) {
        entityManager.createQuery("update ReceivingStation rc set rc.stationStatus = 'DEACTIVATED' "
            + "where rc.id in (:stationsId)")
            .setParameter(STATIONS_ID, stationsId)
            .executeUpdate();

        List<Long> tariffsId = entityManager.createQuery("select ti.id from TariffsInfo ti "
            + "left join ti.receivingStationList rs group by ti.id "
            + "having count(ti) = sum(case when rs.stationStatus = 'DEACTIVATED' then 1 else 0 end)",
            Long.class).getResultList();

        if (!tariffsId.isEmpty()) {
            entityManager.createQuery("update TariffsInfo ti set"
                + " ti.tariffStatus = 'DEACTIVATED' where ti.id in(:tariffsId)")
                .setParameter(TARIFFS_ID, tariffsId)
                .executeUpdate();
        }
    }

    /**
     * Method that deactivate tariffs for courier id and list of receiving stations
     * id.
     *
     * @param courierId  - courier id.
     * @param stationsId - list of receiving stations id.
     * @author Nikita Korzh.
     */
    public void deactivateTariffsByCourierAndReceivingStations(Long courierId, List<Long> stationsId) {
        entityManager.createQuery("select ti from TariffsInfo ti left join ti.receivingStationList rc"
            + " where rc.id in(:stationsId) and ti.courier.id =: courierId", TariffsInfo.class)
            .setParameter(STATIONS_ID, stationsId)
            .setParameter(COURIER_ID, courierId)
            .getResultList()
            .forEach(tariffsInfo -> tariffsInfo.setTariffStatus(TariffStatus.DEACTIVATED));
    }

    /**
     * Method that deactivate tariffs for region id and courier id.
     *
     * @param regionId  - region id.
     * @param courierId - courier id.
     * @author Nikita Korzh.
     */
    public void deactivateTariffsByCourierAndRegion(Long regionId, Long courierId) {
        entityManager.createQuery("select ti from TariffsInfo ti left join ti.tariffLocations tl "
            + "left join tl.location l "
            + "where ti.courier.id =: courierId and l.region.id =: regionId",
            TariffsInfo.class)
            .setParameter(COURIER_ID, courierId)
            .setParameter(REGION_ID, regionId)
            .getResultList()
            .forEach(tariffsInfo -> tariffsInfo.setTariffStatus(TariffStatus.DEACTIVATED));
    }

    /**
     * Method that deactivate tariffs for region id, list of cities id and list of
     * station id.
     *
     * @param regionId   - region id.
     * @param citiesId   - list of cities id
     * @param stationsId - list of receiving stations id.
     * @author Nikita Korzh.
     */
    public void deactivateTariffsByRegionAndCitiesAndStations(Long regionId, List<Long> citiesId,
        List<Long> stationsId) {
        entityManager.createQuery("select ti from TariffsInfo ti left join ti.receivingStationList rs "
            + "left join ti.tariffLocations tl  left join tl.location l where l.region.id =: regionId and "
            + "tl.location.id in (:citiesId) and rs.id in(:stationsId)", TariffsInfo.class)
            .setParameter(REGION_ID, regionId)
            .setParameter(CITIES_ID, citiesId)
            .setParameter(STATIONS_ID, stationsId)
            .getResultList()
            .forEach(tariffsInfo -> tariffsInfo.setTariffStatus(TariffStatus.DEACTIVATED));
    }

    /**
     * Method that deactivate tariffs for region id, list of cities id , list of
     * station id and courier id.
     *
     * @param regionId   - region id.
     * @param citiesId   - list of cities id
     * @param stationsId - list of receiving stations id.
     * @param courierId  - courier id.
     * @author Nikita Korzh.
     */
    public void deactivateTariffsByAllParam(Long regionId, List<Long> citiesId,
        List<Long> stationsId, Long courierId) {
        entityManager.createQuery("select ti from TariffsInfo ti left join ti.receivingStationList rs "
            + "left join ti.tariffLocations tl  left join tl.location l"
            + " where l.region.id =: regionId and "
            + "tl.location.id in (:citiesId) and "
            + "rs.id in(:stationsId) and ti.courier.id =: courierId",
            TariffsInfo.class)
            .setParameter(REGION_ID, regionId)
            .setParameter(CITIES_ID, citiesId)
            .setParameter(STATIONS_ID, stationsId)
            .setParameter(COURIER_ID, courierId)
            .getResultList()
            .forEach(tariffsInfo -> tariffsInfo.setTariffStatus(TariffStatus.DEACTIVATED));
    }

    /**
     * Method to check if the cities exist by regionId.
     *
     * @param citiesId - list of cities id.
     * @param regionId - region id.
     * @return return true if cities exist for region and false if not.
     * @author Nikita Korzh.
     */
    public boolean isCitiesExistForRegion(List<Long> citiesId, Long regionId) {
        Long size = entityManager.createQuery("select count(l) from Location l where l.id in(:citiesId)"
            + " and l.region.id = :regionId", Long.class)
            .setParameter(CITIES_ID, citiesId)
            .setParameter(REGION_ID, regionId)
            .getSingleResult();
        return size == citiesId.size();
    }

    /**
     * Method to check if the regions exist.
     *
     * @param regionsId - list of regions id.
     * @return return true if region exists and false if not.
     * @author Nikita Korzh.
     */
    public boolean isRegionsExists(List<Long> regionsId) {
        Long size = entityManager.createQuery("select count(r) from Region r where r.id in(:regionsId)",
            Long.class)
            .setParameter(REGIONS_ID, regionsId)
            .getSingleResult();
        return size == regionsId.size();
    }

    /**
     * Method to check if the receiving stations exist.
     *
     * @param stationsId - list of receiving stations.
     * @return return true if receiving stations exist and false if not.
     * @author Nikita Korzh.
     */
    public boolean isReceivingStationsExists(List<Long> stationsId) {
        Long size = entityManager.createQuery("select count(rs) from ReceivingStation rs"
            + " where rs.id in(:stationsId)", Long.class)
            .setParameter(STATIONS_ID, stationsId)
            .getSingleResult();
        return size == stationsId.size();
    }
}
