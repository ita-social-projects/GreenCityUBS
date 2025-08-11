package greencity.specification;

import greencity.entity.telegram.TelegramChat;
import greencity.entity.telegram.TelegramMessage;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
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

    public static Specification<TelegramChat> withSearchAndSort(String searchTerm) {
        return Specification.where(hasNameLike(searchTerm))
            .and(sortByLastMessageOrCreatedAt());
    }

    public static Specification<TelegramChat> sortByLastMessageOrCreatedAt() {
        return (root, query, cb) -> {
            Join<TelegramChat, TelegramMessage> lastMessageJoin = root.join("lastMessage", JoinType.LEFT);

            Expression<Integer> messageNullOrder = cb.<Integer>selectCase()
                .when(cb.isNull(lastMessageJoin.get("sendAt")), 1)
                .otherwise(0);

            query.orderBy(
                cb.asc(messageNullOrder),
                cb.desc(lastMessageJoin.get("sendAt")),
                cb.desc(root.get("createdAt")));
            return cb.conjunction();
        };
    }
}
