package greencity.repository;

import greencity.entity.telegram.TelegramChat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface TelegramChatRepositoryCustom {
    Page<TelegramChat> findAllSortedByLastMessage(Specification<TelegramChat> spec, Pageable pageable);
}
