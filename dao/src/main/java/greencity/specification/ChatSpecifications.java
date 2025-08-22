package greencity.specification;

import greencity.entity.telegram.TelegramChat;
import greencity.entity.telegram.TelegramMessage;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public class ChatSpecifications {
    public static Specification<TelegramChat> hasNameLike(String searchTerm) {
        return (root, query, cb) -> {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return cb.conjunction();
            }

            String pattern = "%" + searchTerm.toLowerCase() + "%";

            Expression<String> chatId = cb.lower(root.get("chatId"));
            Expression<String> firstName = cb.lower(cb.coalesce(root.get("firstName"), ""));
            Expression<String> lastName = cb.lower(cb.coalesce(root.get("lastName"), ""));
            Expression<String> username = cb.lower(cb.coalesce(root.get("username"), ""));
            Expression<String> fullNameFirstAndLast = cb.concat(cb.concat(firstName, " "), lastName);
            Expression<String> fullNameLastAndFirst = cb.concat(cb.concat(lastName, " "), firstName);

            return cb.or(
                cb.like(chatId, pattern),
                cb.like(firstName, pattern),
                cb.like(lastName, pattern),
                cb.like(username, pattern),
                cb.like(fullNameFirstAndLast, pattern),
                cb.like(fullNameLastAndFirst, pattern));
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
