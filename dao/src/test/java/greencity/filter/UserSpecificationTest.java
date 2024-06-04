package greencity.filter;

import greencity.entity.user.User;
import greencity.entity.user.User_;
import greencity.enums.UserCategory;
import greencity.filters.UserSpecification;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import java.time.LocalDateTime;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSpecificationTest {

    @Mock
    private Root<User> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Predicate predicate;

    @Mock
    private Join<Object, Object> orderJoin;

    @Test
    void toPredicateWithUsersWithOrdersMadeLessThanThreeMonthsTest() {
        UserSpecification spec = new UserSpecification(UserCategory.USERS_WITH_ORDERS_MADE_LESS_THAN_3_MONTHS);
        when(criteriaBuilder.conjunction()).thenReturn(predicate);
        when(root.join(User_.ORDERS, JoinType.LEFT)).thenReturn(orderJoin);

        assertDoesNotThrow(() -> spec.toPredicate(root, query, criteriaBuilder));
        verify(criteriaBuilder).greaterThanOrEqualTo(any(), any(LocalDateTime.class));
    }

    @Test
    void toPredicateWithUsersWithOrdersMadeWithinThreeMonthsToYearTest() {
        UserSpecification spec = new UserSpecification(UserCategory.USERS_WITH_ORDERS_MADE_WITHIN_3_MONTHS_TO_1_YEAR);
        when(criteriaBuilder.conjunction()).thenReturn(predicate);
        when(root.join(User_.ORDERS, JoinType.LEFT)).thenReturn(orderJoin);

        assertDoesNotThrow(() -> spec.toPredicate(root, query, criteriaBuilder));
        verify(criteriaBuilder).lessThanOrEqualTo(any(), any(LocalDateTime.class));
        verify(criteriaBuilder).greaterThanOrEqualTo(any(), any(LocalDateTime.class));
    }

    @Test
    void toPredicateWithUsersWithOrdersMadeMoreThanYearAgoTest() {
        UserSpecification spec = new UserSpecification(UserCategory.USERS_WITH_ORDERS_MADE_MORE_THAN_1_YEAR);
        when(criteriaBuilder.conjunction()).thenReturn(predicate);
        when(root.join(User_.ORDERS, JoinType.LEFT)).thenReturn(orderJoin);

        assertDoesNotThrow(() -> spec.toPredicate(root, query, criteriaBuilder));
        verify(criteriaBuilder).lessThan(any(), any(LocalDateTime.class));
    }

    @Test
    void toPredicateWithUsersWithoutOrdersTest() {
        UserSpecification spec = new UserSpecification(UserCategory.USERS_WITHOUT_ORDERS);
        when(criteriaBuilder.conjunction()).thenReturn(predicate);
        when(root.join(User_.ORDERS, JoinType.LEFT)).thenReturn(orderJoin);

        assertDoesNotThrow(() -> spec.toPredicate(root, query, criteriaBuilder));
        verify(criteriaBuilder).isNull(any());
    }

    @Test
    void toPredicateWithNullUserCategoryTest() {
        UserSpecification spec = new UserSpecification(null);
        when(criteriaBuilder.conjunction()).thenReturn(predicate);

        assertDoesNotThrow(() -> spec.toPredicate(root, query, criteriaBuilder));
        verify(criteriaBuilder, never()).greaterThanOrEqualTo(any(), any(LocalDateTime.class));
        verify(criteriaBuilder, never()).lessThanOrEqualTo(any(), any(LocalDateTime.class));
        verify(criteriaBuilder, never()).lessThan(any(), any(LocalDateTime.class));
        verify(criteriaBuilder, never()).isNull(any());
    }
}
