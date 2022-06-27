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
    TelegramBot findByChatId(Long chatId);
}
