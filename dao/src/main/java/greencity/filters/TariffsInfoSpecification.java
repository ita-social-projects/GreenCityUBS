package greencity.filters;

import greencity.enums.TariffStatus;
import greencity.entity.order.Courier;
import greencity.entity.order.TariffsInfo;
import greencity.entity.user.Location;
import greencity.entity.user.Region;
import greencity.entity.user.employee.ReceivingStation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;

@RequiredArgsConstructor
public class TariffsInfoSpecification implements Specification<TariffsInfo> {
    private final TariffsInfoFilterCriteria criteria;

    @Override
    public Predicate toPredicate(Root<TariffsInfo> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        Path<Location> location = root.join("tariffLocations").get("location");
        Path<Region> region = location.get("region");
        Path<Courier> courier = root.get("courier");
        Path<ReceivingStation> receivingStation = root.join("receivingStationList");
        Path<TariffStatus> status = root.get("tariffStatus");

        List<Predicate> predicates = new ArrayList<>();

        if (nonNull(criteria.getRegion())) {
            predicates.add(criteriaBuilder.equal(region, criteria.getRegion()));
        }
        if (nonNull(criteria.getLocation())) {
            predicates.add(location.in((Object[]) criteria.getLocation()));
        }
        if (nonNull(criteria.getCourier())) {
            predicates.add(criteriaBuilder.equal(courier, criteria.getCourier()));
        }
        if (nonNull(criteria.getReceivingStation())) {
            predicates.add(receivingStation.in((Object[]) criteria.getReceivingStation()));
        }
        if (nonNull(criteria.getStatus())) {
            predicates.add(criteriaBuilder.equal(status, criteria.getStatus()));
        }

        query.distinct(true);

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
