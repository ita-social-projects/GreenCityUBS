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
            Join<TelegramChat, User> userJoin = root.join("user");

            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("recipientName")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(userJoin.get("recipientSurname")), pattern)
            );
        };
    }
}
