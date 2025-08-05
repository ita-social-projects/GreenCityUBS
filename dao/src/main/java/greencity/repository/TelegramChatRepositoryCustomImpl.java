package greencity.repository;

import greencity.entity.telegram.TelegramChat;
import greencity.entity.telegram.TelegramMessage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

public class TelegramChatRepositoryCustomImpl implements TelegramChatRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<TelegramChat> findAllSortedByLastMessage(Specification<TelegramChat> spec, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<TelegramChat> cq = cb.createQuery(TelegramChat.class);
        Root<TelegramChat> chatRoot = cq.from(TelegramChat.class);

        Join<TelegramChat, TelegramMessage> messageJoin = chatRoot.join("messages", JoinType.LEFT);

        Predicate predicate = spec != null ? spec.toPredicate(chatRoot, cq, cb) : cb.conjunction();
        cq.where(predicate);

        cq.groupBy(chatRoot.get("id"));

        Path<LocalDateTime> sendAtPath = messageJoin.get("sendAt");
        Expression<LocalDateTime> maxSendAt = cb.greatest(sendAtPath);

        cq.orderBy(cb.desc(maxSendAt));

        cq.select(chatRoot);

        TypedQuery<TelegramChat> query = entityManager.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<TelegramChat> result = query.getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<TelegramChat> countRoot = countQuery.from(TelegramChat.class);
        countQuery.select(cb.count(countRoot)).where(predicate);
        Long count = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(result, pageable, count);
    }
}
