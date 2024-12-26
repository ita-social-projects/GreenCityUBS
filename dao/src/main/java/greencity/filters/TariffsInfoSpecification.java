package greencity.filters;

import greencity.entity.order.Courier_;
import greencity.entity.order.TariffLocation_;
import greencity.entity.order.TariffsInfo;
import greencity.entity.order.TariffsInfo_;
import greencity.entity.order.Courier;
import greencity.entity.user.Location_;
import greencity.entity.user.Region_;
import greencity.entity.user.employee.ReceivingStation_;
import greencity.enums.TariffStatus;
import greencity.entity.user.Location;
import greencity.entity.user.Region;
import greencity.entity.user.employee.ReceivingStation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.nonNull;

@RequiredArgsConstructor
public class TariffsInfoSpecification implements Specification<TariffsInfo> {
    private final TariffsInfoFilterCriteria criteria;

    @Override
    public Predicate toPredicate(Root<TariffsInfo> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        Path<Location> location = root.join(TariffsInfo_.TARIFF_LOCATIONS).get(TariffLocation_.LOCATION);
        Path<Region> region = location.get(Location_.REGION);
        Path<Courier> courier = root.get(TariffsInfo_.COURIER);
        Path<ReceivingStation> receivingStation = root.join(TariffsInfo_.RECEIVING_STATION_LIST);
        Path<TariffStatus> status = root.get(TariffsInfo_.TARIFF_STATUS);

        List<Predicate> predicates = new ArrayList<>();

        if (nonNull(criteria.getRegion())) {
            predicates.add(criteriaBuilder.equal(region.get(Region_.ID), criteria.getRegion()));
        }
        if (nonNull(criteria.getLocation())) {
            predicates.add(location.get(Location_.ID).in((Object[]) criteria.getLocation()));
        }
        if (nonNull(criteria.getCourier())) {
            predicates.add(criteriaBuilder.equal(courier.get(Courier_.ID), criteria.getCourier()));
        }
        if (nonNull(criteria.getReceivingStation())) {
            predicates.add(receivingStation.get(ReceivingStation_.ID).in((Object[]) criteria.getReceivingStation()));
        }
        if (nonNull(criteria.getStatus())) {
            predicates.add(criteriaBuilder.equal(status, criteria.getStatus()));
        }

        query.distinct(true);

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
