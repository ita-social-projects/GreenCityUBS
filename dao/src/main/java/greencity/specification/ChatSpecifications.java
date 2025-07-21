package greencity.specification;

import greencity.entity.telegram.TelegramChat;
import greencity.entity.user.User;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class ChatSpecifications {
    public static Specification<TelegramChat> hasNameLike(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            String pattern = "%" + searchTerm.toLowerCase() + "%";

            return criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), pattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), pattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), pattern));
        };
    }
}
