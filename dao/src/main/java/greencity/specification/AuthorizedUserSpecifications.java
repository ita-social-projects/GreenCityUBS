package greencity.specification;

import greencity.entity.telegram.AuthorizedUser;
import greencity.entity.user.User;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class AuthorizedUserSpecifications {
    public static Specification<AuthorizedUser> hasNameLike(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            String pattern = "%" + searchTerm.toLowerCase() + "%";
            Join<AuthorizedUser, User> userJoin = root.join("user");

            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("recipientName")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("recipientSurname")), pattern)
            );
        };
    }
}
