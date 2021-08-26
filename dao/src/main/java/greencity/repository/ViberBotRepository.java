package greencity.repository;

import greencity.entity.viber.ViberBot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ViberBotRepository extends JpaRepository<ViberBot, Long> {
    /**
     * The method finds viber bot by chatId.
     *
     * @return list of {@link ViberBot}.
     */
    Optional<ViberBot> findViberBotByChatId(String chatId);
}
