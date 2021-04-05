package greencity.repository;

import greencity.entity.telegram.TelegramBot;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TelegramBotRepository extends CrudRepository<TelegramBot, Long> {
    /**
     * The method finds telegram bot by chatId.
     *
     * @return list of {@link TelegramBot}.
     */
    @Query(value = "FROM TelegramBot t WHERE t.chatId = :chatId")
    TelegramBot findByChatId(Long chatId);
}
