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

            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("firstName"), "%" + searchTerm + "%"),
                    criteriaBuilder.like(root.get("lastName"), "%" + searchTerm + "%"),
                    criteriaBuilder.like(root.get("username"), "%" + searchTerm + "%")
            );
        };
    }
}
