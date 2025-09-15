package greencity.repository;

import greencity.entity.telegram.TelegramChat;
import greencity.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TelegramChatRepository
    extends JpaRepository<TelegramChat, Long>, JpaSpecificationExecutor<TelegramChat> {
    /**
     * The method finds telegram bot by user.
     *
     * @param user {@link User}.
     * @return {@link Optional} {@link TelegramChat}.
     *
     * @author Julia Seti
     */
    Optional<TelegramChat> findByUser(User user);

    /**
     * The method finds telegram bot by chatId.
     *
     * @param chatId {@link Long}.
     * @return {@link TelegramChat}.
     */
    Optional<TelegramChat> findByChatId(String chatId);

    /**
     * The method finds all telegram chats by specification and pageable.
     *
     * @param spec     {@link Specification}.
     * @param pageable {@link Pageable}.
     * @return {@link TelegramChat}.
     */
    @EntityGraph(attributePaths = "lastMessage")
    Page<TelegramChat> findAll(Specification<TelegramChat> spec, Pageable pageable);

    /**
     * The method finds telegram bot by userId.
     *
     * @param userId {@link Long}.
     * @return {@link TelegramChat}.
     */
    Optional<TelegramChat> findByUserId(Long userId);
}
