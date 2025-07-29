package greencity.service.ubs;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface TelegramCommandsService {
    /**
     * Processes a Telegram command message sent by the user. Recognized commands
     * include:
     * <ul>
     * <li><b>/start</b> or <b>/help</b> — Resets chat state to {@code NORMAL} and
     * shows available commands.</li>
     * <li><b>/support</b> — Switches chat state to {@code IN_SUPPORT} and sends a
     * support-related message.</li>
     * <li><b>/login</b> — Switches chat state to {@code LOGGING_AS_MANAGER} and
     * prompts for manager login.</li>
     * </ul>
     * If the command is unrecognized or the message text is {@code null}, it
     * defaults to {@code NORMAL} state and returns an unknown command message.
     *
     * @param message the Telegram {@link Message} containing the user's command
     * @return a {@link SendMessage} response appropriate to the command or fallback
     *         message
     */
    SendMessage processCommand(Message message);
}
