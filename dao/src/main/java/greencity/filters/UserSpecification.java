package greencity.filters;

import greencity.entity.order.Order_;
import greencity.entity.user.User;
import greencity.entity.user.User_;
import greencity.enums.UserCategory;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@AllArgsConstructor
public class UserSpecification implements Specification<User> {
    private UserCategory userCategory;

    @Override
    public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query,
        CriteriaBuilder criteriaBuilder) {
        Predicate predicate = criteriaBuilder.conjunction();

        if (Objects.isNull(userCategory)) {
            return predicate;
        }

        predicate = createCriteriaForUsersWithOrdersMadeLessThanThreeMountsAgo(root, criteriaBuilder, predicate);
        predicate = createCriteriaForUsersWithOrdersMadeWithinThreeMonthsToYear(root, criteriaBuilder, predicate);
        predicate = createCriteriaFoUserWithOrdersMadeMoreThanYearAgo(root, criteriaBuilder, predicate);
        predicate = createCriteriaForUsersWithoutOrders(root, criteriaBuilder, predicate);

        return predicate;
    }

    private Predicate createCriteriaForUsersWithOrdersMadeLessThanThreeMountsAgo(
        Root<User> root, CriteriaBuilder criteriaBuilder, Predicate predicate) {
        if (userCategory.equals(UserCategory.USERS_WITH_ORDERS_MADE_LESS_THAN_3_MONTHS)) {
            var orderJoin = root.join(User_.ORDERS, JoinType.LEFT);
            var criteria = criteriaBuilder
                .greaterThanOrEqualTo(orderJoin.get(Order_.ORDER_DATE), LocalDateTime.now().minusMonths(3));
            predicate = criteriaBuilder.and(predicate, criteria);
        }
        return predicate;
    }

    private Predicate createCriteriaForUsersWithOrdersMadeWithinThreeMonthsToYear(
        Root<User> root, CriteriaBuilder criteriaBuilder, Predicate predicate) {
        if (userCategory.equals(UserCategory.USERS_WITH_ORDERS_MADE_WITHIN_3_MONTHS_TO_1_YEAR)) {
            var orderJoin = root.join(User_.ORDERS, JoinType.LEFT);
            var lessThanOrEqualToCriteria = criteriaBuilder
                .lessThanOrEqualTo(orderJoin.get(Order_.ORDER_DATE), LocalDateTime.now().minusMonths(3));
            var greaterThanOrEqualToCriteria = criteriaBuilder
                .greaterThanOrEqualTo(orderJoin.get(Order_.ORDER_DATE), LocalDateTime.now().minusYears(1));

            predicate = criteriaBuilder.and(predicate, lessThanOrEqualToCriteria, greaterThanOrEqualToCriteria);
        }
        return predicate;
    }

    private Predicate createCriteriaFoUserWithOrdersMadeMoreThanYearAgo(
        Root<User> root, CriteriaBuilder criteriaBuilder, Predicate predicate) {
        if (userCategory.equals(UserCategory.USERS_WITH_ORDERS_MADE_MORE_THAN_1_YEAR)) {
            var orderJoin = root.join(User_.ORDERS, JoinType.LEFT);
            var criteria =
                criteriaBuilder.lessThan(orderJoin.get(Order_.ORDER_DATE), LocalDateTime.now().minusYears(1));
            predicate = criteriaBuilder.and(predicate, criteria);
        }
        return predicate;
    }

    private Predicate createCriteriaForUsersWithoutOrders(
        Root<User> root, CriteriaBuilder criteriaBuilder, Predicate predicate) {
        if (userCategory.equals(UserCategory.USERS_WITHOUT_ORDERS)) {
            var orderJoin = root.join(User_.ORDERS, JoinType.LEFT);
            var criteria = criteriaBuilder.isNull(orderJoin.get(Order_.ID));
            predicate = criteriaBuilder.and(predicate, criteria);
        }
        return predicate;
    }
}
