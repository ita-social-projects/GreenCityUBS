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
     * Method for creating welcome SendMessage for TelegramLongPollingBot.
     *
     * @param chatId {@link String} is telegram chat id.
     *
     * @return {@link SendMessage} configured with the welcome message.
     */
    public static SendMessage createWelcomeMessage(String chatId) {
        return buildMessage(chatId, TelegramBotConstants.GREETING_MESSAGE);
    }

    public static SendMessage createAvailableCommandOption(String chatId) {
        return buildReplyMarkUpMessage(chatId, TelegramBotConstants.SUPPORTED_COMMANDS,
            KeyboardFactory.createHelpKeyboard());
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

    /**
     * Method for creating client support message.
     *
     * @param chatId {@link String} is telegram chat id.
     *
     * @return {@link SendMessage} configured with the client support message.
     */
    public static SendMessage createClientSupportMessage(String chatId) {
        return buildMessage(chatId, TelegramBotConstants.CLIENT_SUPPORT_MESSAGE);
    }

    /**
     * Method for creating success client support message.
     *
     * @param chatId {@link String} is telegram chat id.
     *
     * @return {@link SendMessage} configured with the success client support
     *         message.
     */
    public static SendMessage createSuccessClientSupportMessageSend(String chatId) {
        return buildMessage(chatId, TelegramBotConstants.CLIENT_SUPPORT_MESSAGE_SEND);
    }

    /**
     * Method for creating help message.
     *
     * @param chatId {@link String} is telegram chat id.
     *
     * @return {@link SendMessage} configured with the help message and help
     *         keyboard.
     */
    public static SendMessage createHelpMessage(String chatId) {
        return buildReplyMarkUpMessage(chatId, TelegramBotConstants.SUPPORTED_COMMANDS,
            KeyboardFactory.createHelpKeyboard());
    }

    /**
     * Method for creating unknown command message.
     *
     * @param chatId {@link String} is telegram chat id.
     *
     * @return {@link SendMessage} configured with the unknown command message.
     */
    public static SendMessage createUnknownCommandMessage(String chatId) {
        return buildMessage(chatId, TelegramBotConstants.UNKNOWN_COMMAND);
    }

    /**
     * Method for creating login message.
     *
     * @param chatId {@link String} is telegram chat id.
     *
     * @return {@link SendMessage} configured with the login message.
     */
    public static SendMessage createLoginMessage(String chatId) {
        return buildMessage(chatId, TelegramBotConstants.LOGIN_MESSAGE);
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
        return buildMessage(chatId, String.format(TelegramBotConstants.LOGIN_ERROR, errorMessage));
    }

    /**
     * Method for creating success login message.
     *
     * @param chatId   {@link String} is telegram chat id.
     * @param userName {@link String} is username.
     *
     * @return {@link SendMessage} configured with the success login message with
     *         username.
     */
    public static SendMessage createSuccessLoginMessage(String chatId, String userName) {
        return buildMessage(chatId, String.format(TelegramBotConstants.SUCCESS_LOGIN, userName));
    }

    /**
     * Method for creating notification message for manager.
     *
     * @param chatId    {@link String} is telegram chat id of the user.
     * @param managerId {@link String} is telegram chat id of the manager.
     *
     * @return {@link SendMessage} configured with the notification message for
     *         manager with user chat id.
     */
    public static SendMessage createNotificationMessageForManager(String chatId, String managerId, int messageCount) {
        return buildMessage(managerId,
            String.format(TelegramBotConstants.NEW_SUPPORT_MESSAGE_NOTIFICATION, messageCount, chatId));
    }

    /**
     * Method for creating stop support mode message.
     *
     * @param chatId {@link String} is telegram chat id.
     *
     * @return {@link SendMessage} configured with the stop support mode message.
     */
    public static SendMessage createStopSupportModeMessage(String chatId) {
        return buildMessage(chatId, TelegramBotConstants.CLIENT_END_SUPPORT_MODE);
    }

    /**
     * Method for creating end support mode message.
     *
     * @param chatId {@link String} is telegram chat id.
     *
     * @return {@link SendMessage} configured with the end support mode message.
     */
    public static SendMessage createEndSupportMessage(String chatId) {
        return buildReplyMarkUpMessage(chatId, TelegramBotConstants.CLIENT_STOP_SUPPORT_MODE,
            KeyboardFactory.createChatFeedbackRatingKeyboard());
    }

    /**
     * Method for creating end support mode notification message.
     *
     * @param chatId    {@link String} is telegram chat id of the user.
     * @param managerId {@link String} is telegram chat id of the manager.
     *
     * @return {@link SendMessage} configured with the end support mode notification
     *         message with user chat id.
     */
    public static SendMessage createEndSupportModeNotification(String chatId, String managerId) {
        return buildMessage(managerId,
            String.format(TelegramBotConstants.CLIENT_END_SUPPORT_MODE_NOTIFICATION, chatId));
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
            .caption(caption.isEmpty() ? null : caption)
            .photo(new InputFile(photoUrl))
            .build();
    }

    /**
     * Method for creating message after user feedback.
     *
     * @param chatId {@link String} is telegram chat id.
     *
     * @return {@link SendMessage} configured with the message after user feedback.
     */
    public static SendMessage createMessageAfterUserFeedback(String chatId) {
        return buildMessage(chatId, TelegramBotConstants.CLIENT_MESSAGE_AFTER_FEEDBACK);
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
            KeyboardFactory.createBackToMainManuButton());
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
            KeyboardFactory.createProcessOrBackToMainMenuKeyboard(TelegramBotConstants.SORTING_PRICES_CALLBACK));
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
            KeyboardFactory.createBackToMainManuButton());
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
            KeyboardFactory.createBackToMainManuButton());
    }

    /**
     * Method for creating invalid email message.
     *
     * @param chatId {@link String} is telegram chat id.
     * @return {@link SendMessage} configured with the invalid email message.
     */
    public static SendMessage createInvalidEmailMessage(String chatId) {
        return buildMessage(chatId, TelegramBotConstants.INVALID_EMAIL_MESSAGE);
    }

    /**
     * Method for creating green office thanks message.
     *
     * @param chatId {@link String} is telegram chat id.
     * @return {@link SendMessage} configured with the green office thanks message.
     */
    public static SendMessage createGreenOfficeThanksMessage(String chatId) {
        return buildMessage(chatId, TelegramBotConstants.GREEN_OFFICE_THANK_YOU_MESSAGE);
    }

    /**
     * Method for creating feedback message.
     *
     * @param chatId {@link String} is telegram chat id.
     *
     * @return {@link SendMessage} configured with the feedback message.
     */
    public static SendMessage createFeedbackMessage(String chatId) {
        return buildReplyMarkUpMessage(chatId, TelegramBotConstants.FEEDBACK_MESSAGE,
            KeyboardFactory.createFeedbackOrBackToMainMenuKeyboard());
    }

    public static SendMessage createEnteringFeedbackMessage(String chatId, String feedback) {
        if (feedback != null && !feedback.isEmpty() && feedback.equals(TelegramBotConstants.GREAT_FEEDBACK_CALLBACK)) {
            return buildMessage(chatId, TelegramBotConstants.GREAT_FEEDBACK_MESSAGE);
        } else {
            return buildMessage(chatId, TelegramBotConstants.BAD_FEEDBACK_MESSAGE);
        }
    }

    public static SendMessage createFeedbackThanksMessage(String chatId) {
        return buildMessage(chatId, TelegramBotConstants.FEEDBACK_THANK_YOU_MESSAGE);
    }
}
