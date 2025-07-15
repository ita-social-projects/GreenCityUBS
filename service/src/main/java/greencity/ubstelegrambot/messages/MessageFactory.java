package greencity.ubstelegrambot.messages;

import greencity.constant.TelegramBotConstants;
import greencity.ubstelegrambot.keyboards.KeyboardFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageFactory {
    /**
     * Creates a welcome message for a Telegram chat with a help keyboard attached.
     *
     * @param chatId the Telegram chat ID to send the welcome message to
     * @return a SendMessage object containing the greeting message and help keyboard
     */
    public static SendMessage createWelcomeMessage(String chatId) {
        return buildReplyMarkUpMessage(chatId, TelegramBotConstants.GREETING_MESSAGE,
            KeyboardFactory.createHelpKeyboard());
    }

    /**
     * Creates a welcome message for a manager in a Telegram chat.
     *
     * @param chatId the Telegram chat ID to send the message to
     * @return a SendMessage configured with the manager-specific greeting
     */
    public static SendMessage createWelcomeManagerMessage(String chatId) {
        return buildMessage(chatId, TelegramBotConstants.GREETING_MANAGER_MESSAGE);
    }

    /**
     * Creates a message indicating successful logout for a manager user.
     *
     * @param chatId the Telegram chat ID to send the message to
     * @return a SendMessage configured with the successful logout message for managers
     */
    public static SendMessage createLogoutManagerMessage(String chatId) {
        return buildMessage(chatId, TelegramBotConstants.SUCCESSFUL_LOGOUT_MANAGER);
    }

    /**
     * Creates a message indicating that certain commands are forbidden for managers.
     *
     * @param chatId the Telegram chat ID to send the message to
     * @return a SendMessage configured with the forbidden commands notification for managers
     */
    public static SendMessage createForbiddenCommandsManagerMessage(String chatId) {
        return buildMessage(chatId, TelegramBotConstants.FORBIDDEN_COMMANDS_MANAGER);
    }

    /**
     * Creates a message listing the available commands for the user, including a help keyboard.
     *
     * @param chatId the Telegram chat ID to send the message to
     * @return a SendMessage object containing the list of available commands and a help keyboard
     */
    public static SendMessage createAvailableCommandsMessage(String chatId) {
        return buildReplyMarkUpMessage(chatId, TelegramBotConstants.SUPPORTED_COMMANDS,
            KeyboardFactory.createHelpKeyboard());
    }

    /**
     * Creates a message listing the available commands for managers, including a manager-specific help keyboard.
     *
     * @param chatId the Telegram chat ID to send the message to
     * @return a SendMessage containing the list of manager commands and the manager help keyboard
     */
    public static SendMessage createAvailableForManagerCommandsMessage(String chatId) {
        return buildReplyMarkUpMessage(chatId, TelegramBotConstants.SUPPORTED_COMMANDS,
            KeyboardFactory.createHelpKeyboardForManager());
    }

    /**
     * Method for creating unknown command message.
     *
     * @param chatId {@link String} is telegram chat id.
     *
     * @return {@link SendMessage} configured with the unknown command message.
     */
    public static SendMessage createUnknownCommandMessage(String chatId) {
        return buildReplyMarkUpMessage(chatId, TelegramBotConstants.UNKNOWN_COMMAND,
            KeyboardFactory.createHelpKeyboard());
    }

    /**
     * Method for creating login message.
     *
     * @param chatId {@link String} is telegram chat id.
     *
     * @return {@link SendMessage} configured with the login message.
     */
    public static SendMessage createLoginMessage(String chatId) {
        return buildReplyMarkUpMessage(chatId, TelegramBotConstants.LOGIN_MESSAGE,
            KeyboardFactory.createBackToMainMenuKeyboard());
    }

    /**
     * Method for creating fail login message.
     *
     * @param chatId       {@link String} is telegram chat id.
     * @param errorMessage {@link String} is error message.
     *
     * @return {@link SendMessage} configured with the fail login message with error
     *         message.
     */
    public static SendMessage createFailLoginMessage(String chatId, String errorMessage) {
        return buildReplyMarkUpMessage(chatId, String.format(TelegramBotConstants.LOGIN_ERROR, errorMessage),
            KeyboardFactory.createBackToMainMenuKeyboard());
    }

    /**
     * Creates a success login message for a manager, including the username and a manager-specific help keyboard.
     *
     * @param chatId the Telegram chat ID to send the message to
     * @param userName the username to include in the success message
     * @return a SendMessage object configured with the success login message and manager help keyboard
     */
    public static SendMessage createSuccessLoginMessage(String chatId, String userName) {
        return buildReplyMarkUpMessage(chatId, String.format(TelegramBotConstants.SUCCESS_LOGIN, userName),
            KeyboardFactory.createHelpKeyboardForManager());
    }

    /**
     * Creates a notification message for a manager indicating that a client wants to initiate a conversation.
     *
     * The message is formatted with HTML and includes the client's username, message text, and an internal chat ID.
     *
     * @param telegramChatId the Telegram chat ID of the manager
     * @param username the username of the client requesting to speak
     * @param messageText the message from the client
     * @param innerChatId the internal chat ID associated with the conversation
     * @return a {@link SendMessage} object configured with the notification message and HTML formatting
     */
    public static SendMessage createNotificationMessageForManager(String telegramChatId, String username,
        String messageText, Long innerChatId) {
        SendMessage message = buildMessage(telegramChatId,
            String.format(TelegramBotConstants.CLIENT_WANT_TO_SPEAK, username, messageText, innerChatId));
        message.enableHtml(true);
        message.setParseMode(ParseMode.HTML);
        return message;
    }

    /**
     * Creates a message indicating the end of support mode, including a chat feedback rating keyboard.
     *
     * @param chatId the Telegram chat ID to send the message to
     * @return a SendMessage configured with the end support mode text and feedback rating keyboard
     */
    public static SendMessage createEndSupportMessage(String chatId) {
        return buildReplyMarkUpMessage(chatId, TelegramBotConstants.CLIENT_STOP_SUPPORT_MODE,
            KeyboardFactory.createChatFeedbackRatingKeyboard());
    }

    /**
     * Creates a notification message informing the user that support mode has ended, including the specified username.
     *
     * @param chatId the Telegram chat ID to send the message to
     * @param username the username to include in the notification message
     * @return a SendMessage object configured with the end support mode notification
     */
    public static SendMessage createEndSupportModeNotification(String chatId, String username) {
        return buildMessage(chatId,
            String.format(TelegramBotConstants.CLIENT_END_SUPPORT_MODE_NOTIFICATION, username));
    }

    /**
     * Creates a SendPhoto object for sending a photo to a Telegram user.
     *
     * @param chatId   {@link String} is the telegram chat ID.
     * @param photoUrl {@link String} is the URL of the photo to send.
     * @param caption  {@link String} is the caption for the photo, can be empty.
     * @return {@link SendPhoto} configured with the specified chat ID, photo URL,
     *         and caption.
     */
    public static SendPhoto createPhotoSender(String chatId, String photoUrl, String caption) {
        return SendPhoto
            .builder()
            .chatId(chatId)
            .caption((caption == null || caption.isBlank()) ? null : caption)
            .photo(new InputFile(photoUrl))
            .build();
    }

    /**
     * Method for creating support message with call back query.
     *
     * @param chatId {@link String} is telegram chat id.
     *
     * @return {@link SendMessage} configured with the support message with call
     *         back query.
     */
    public static SendMessage createSupportMessageCallBackQuery(String chatId) {
        return SendMessage
            .builder()
            .chatId(chatId)
            .text(TelegramBotConstants.CLIENT_SUPPORT_MESSAGE_CALL_BACK_QUERY)
            .replyMarkup(KeyboardFactory.userSupportKeyboard())
            .build();
    }

    /**
     * Method for creating work schedule message.
     *
     * @param chatId {@link String} is telegram chat id.
     * @return {@link SendMessage} configured with the work schedule message.
     */
    public static SendMessage createWorkScheduleMessage(String chatId) {
        var message = buildReplyMarkUpMessage(chatId, TelegramBotConstants.WORK_SCHEDULE_MESSAGE,
            KeyboardFactory.createBackToMainMenuKeyboard());
        message.setParseMode(ParseMode.HTML);
        return message;
    }

    /**
     * Method for creating sorting prices message.
     *
     * @param chatId {@link String} is telegram chat id.
     * @return {@link SendMessage} configured with the sorting prices message.
     */
    public static SendMessage createSortingPricesMessage(String chatId) {
        var message = buildReplyMarkUpMessage(chatId, TelegramBotConstants.SORTING_RULES_PRICING_MESSAGE,
            KeyboardFactory.createBackToMainMenuKeyboard());
        message.setParseMode(ParseMode.HTML);
        return message;
    }

    /**
     * Method for creating admission rules message.
     *
     * @param chatId {@link String} is telegram chat id.
     * @return {@link SendMessage} configured with the admission rules message.
     */
    public static SendMessage createAdmissionRulesMessage(String chatId) {
        var message = buildReplyMarkUpMessage(chatId, TelegramBotConstants.ADMISSION_RULES_TEXT,
            KeyboardFactory.createBackToMainMenuKeyboard());
        message.setParseMode(ParseMode.HTML);
        return message;
    }

    /**
     * Method for creating green office message.
     *
     * @param chatId {@link String} is telegram chat id.
     * @return {@link SendMessage} configured with the green office message.
     */
    public static SendMessage createGreenOfficeMessage(String chatId) {
        return buildReplyMarkUpMessage(chatId, TelegramBotConstants.GREEN_OFFICE_TEXT,
            KeyboardFactory.createProcessOrBackToMainMenuKeyboard(TelegramBotConstants.GREEN_OFFICE_PROCESS_CALLBACK));
    }

    /**
     * Method for creating entering email message.
     *
     * @param chatId {@link String} is telegram chat id.
     * @return {@link SendMessage} configured with the entering email message.
     */
    public static SendMessage createEnteringEmailMessage(String chatId) {
        return buildReplyMarkUpMessage(chatId, TelegramBotConstants.ENTERING_EMAIL_MESSAGE,
            KeyboardFactory.createBackToMainMenuKeyboard());
    }

    /**
     * Method for creating invalid email message.
     *
     * @param chatId {@link String} is telegram chat id.
     * @return {@link SendMessage} configured with the invalid email message.
     */
    public static SendMessage createInvalidEmailMessage(String chatId) {
        return buildReplyMarkUpMessage(chatId, TelegramBotConstants.INVALID_EMAIL_MESSAGE,
            KeyboardFactory.createBackToMainMenuKeyboard());
    }

    /**
     * Method for creating green office thanks message.
     *
     * @param chatId {@link String} is telegram chat id.
     * @return {@link SendMessage} configured with the green office thanks message.
     */
    public static SendMessage createGreenOfficeThanksMessage(String chatId) {
        return buildReplyMarkUpMessage(chatId, TelegramBotConstants.GREEN_OFFICE_THANK_YOU_MESSAGE,
            KeyboardFactory.createHelpKeyboard());
    }

    /**
     * Creates a feedback prompt message with a keyboard for submitting feedback or returning to the main menu.
     *
     * @param chatId the Telegram chat ID to send the message to
     * @return a SendMessage configured with the feedback prompt and appropriate keyboard
     */
    public static SendMessage createFeedbackMessage(String chatId) {
        return buildReplyMarkUpMessage(chatId, TelegramBotConstants.FEEDBACK_MESSAGE,
            KeyboardFactory.createFeedbackOrBackToMainMenuKeyboard());
    }

    /**
     * Creates a message acknowledging receipt of great feedback.
     *
     * @param chatId the Telegram chat ID to send the message to
     * @return a SendMessage object containing the great feedback acknowledgment
     */
    public static SendMessage createGreatFeedbackMessage(String chatId) {
        return buildMessage(chatId, TelegramBotConstants.GREAT_FEEDBACK_MESSAGE);
    }

    /**
     * Creates a message acknowledging receipt of negative feedback.
     *
     * @param chatId the Telegram chat ID to send the message to
     * @return a SendMessage object containing the bad feedback acknowledgment
     */
    public static SendMessage createBadFeedbackMessage(String chatId) {
        return buildMessage(chatId, TelegramBotConstants.BAD_FEEDBACK_MESSAGE);
    }

    /**
     * Creates a message thanking the user for their feedback, including a help keyboard.
     *
     * @param chatId the Telegram chat ID to send the message to
     * @return a SendMessage object with a thank you message and help keyboard
     */
    public static SendMessage createFeedbackThanksMessage(String chatId) {
        return buildReplyMarkUpMessage(chatId, TelegramBotConstants.FEEDBACK_THANK_YOU_MESSAGE,
            KeyboardFactory.createHelpKeyboard());
    }

    /**
     * Creates a message indicating that an unknown error has occurred, with a keyboard to return to the main menu.
     *
     * @param chatId the Telegram chat ID to send the message to
     * @return a SendMessage object configured with the unknown error message and back-to-main-menu keyboard
     */
    public static SendMessage createUnknownErrorOccurredMessage(String chatId) {
        return buildReplyMarkUpMessage(chatId, TelegramBotConstants.UNKNOWN_ERROR_OCCURRED_PLEASE_TRY_AGAIN,
            KeyboardFactory.createBackToMainMenuKeyboard());
    }

    /**
     * Builds a SendMessage object with the specified chat ID and text.
     *
     * @param chatId the telegram chat ID
     * @param text   the message text to send
     * @return a SendMessage object configured with the specified chat ID and text
     */
    public static SendMessage buildMessage(String chatId, String text) {
        return SendMessage
            .builder()
            .chatId(chatId)
            .text(text)
            .build();
    }

    /**
     * Builds a SendMessage object with the specified chat ID, text, and inline
     * keyboard markup.
     *
     * @param chatId               the telegram chat ID
     * @param text                 the message text to send
     * @param inlineKeyboardMarkup the inline keyboard markup to attach
     * @return a SendMessage object configured with the specified chat ID, text, and
     *         inline keyboard markup
     */
    public static SendMessage buildReplyMarkUpMessage(String chatId, String text,
        InlineKeyboardMarkup inlineKeyboardMarkup) {
        return SendMessage
            .builder()
            .chatId(chatId)
            .replyMarkup(inlineKeyboardMarkup)
            .text(text)
            .build();
    }
}
