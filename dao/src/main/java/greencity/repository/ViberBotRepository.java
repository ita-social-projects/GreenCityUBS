package greencity.repository;

import greencity.entity.telegram.TelegramBot;
import greencity.entity.viber.ViberBot;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ViberBotRepository extends CrudRepository<ViberBot, Long> {
    /**
     * The method finds viber bot by chatId.
     *
     * @return list of {@link ViberBot}.
     */
    @Query(value = "FROM ViberBot t WHERE t.chatId = :chatId")
    Optional<ViberBot> findByChatId(String chatId);
}
