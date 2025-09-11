package greencity.ubstelegrambot.messages;

import greencity.constant.TelegramBotConstants;
import greencity.exceptions.bots.TelegramBotExecutionException;
import greencity.ubstelegrambot.keyboards.KeyboardFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageFactory {
    /**
     * Method for creating welcome SendMessage for TelegramLongPollingBot.
     *
     * @param chatId {@link String} is telegram chat id.
     * @param lang   {@link String} is a language code of the telegram chat.
     *
     * @return {@link SendMessage} configured with the welcome message.
     */
    public static SendMessage createWelcomeMessage(String chatId, String lang) {
        return buildReplyMarkUpMessage(chatId, MessageProvider.get(lang, "greeting.message"),
            KeyboardFactory.createHelpKeyboard(lang));
    }

    /**
     * Method for creating welcome manager SendMessage.
     *
     * @param chatId {@link String} is telegram chat id.
     * @param lang   {@link String} is a language code of the telegram chat.
     *
     * @return {@link SendMessage} configured with the welcome manager message.
     */
    public static SendMessage createWelcomeManagerMessage(String chatId, String lang) {
        return buildMessage(chatId, MessageProvider.get(lang, "greeting.manager.message"));
    }

    /**
     * Method for creating successful logout manager SendMessage for
     * TelegramLongPollingBot.
     *
     * @param chatId {@link String} is telegram chat id.
     * @param lang   {@link String} is a language code of the telegram chat.
     *
     * @return {@link SendMessage} configured with the successful logout manager
     *         message.
     */
    public static SendMessage createLogoutManagerMessage(String chatId, String lang) {
        return buildMessage(chatId, MessageProvider.get(lang, "successful.logout.manager"));
    }

    /**
     * Method for creating forbidden manager SendMessage for TelegramLongPollingBot.
     *
     * @param chatId {@link String} is telegram chat id.
     * @param lang   {@link String} is a language code of the telegram chat.
     *
     * @return {@link SendMessage} configured with the forbidden manager messages.
     */
    public static SendMessage createForbiddenCommandsManagerMessage(String chatId, String lang) {
        return buildReplyMarkUpMessage(chatId,
            MessageProvider.get(lang, "forbidden.commands.manager") + "\n"
                + MessageProvider.get(lang, "supported.commands"),
            KeyboardFactory.createHelpKeyboardForManager(lang));
    }

    /**
     * Method for creating list of available commands message.
     *
     * @param chatId {@link String} is telegram chat id.
     * @param lang   {@link String} is a language code of the telegram chat.
     *
     * @return {@link SendMessage} configured with the list of available commands
     *         message.
     */
    public static SendMessage createAvailableCommandsMessage(String chatId, String lang) {
        return buildReplyMarkUpMessage(chatId, MessageProvider.get(lang, "supported.commands"),
            KeyboardFactory.createHelpKeyboard(lang));
    }

    /**
     * Method for creating list of supported manager commands for
     * TelegramLongPollingBot.
     *
     * @param chatId {@link String} is telegram chat id.
     * @param lang   {@link String} is a language code of the telegram chat.
     *
     * @return {@link SendMessage} configured with the list of supported manager
     *         commands message.
     */
    public static SendMessage createAvailableForManagerCommandsMessage(String chatId, String lang) {
        return buildReplyMarkUpMessage(chatId, MessageProvider.get(lang, "supported.commands"),
            KeyboardFactory.createHelpKeyboardForManager(lang));
    }

    /**
     * Method for creating unknown command message.
     *
     * @param chatId {@link String} is telegram chat id.
     * @param lang   {@link String} is a language code of the telegram chat.
     *
     * @return {@link SendMessage} configured with the unknown command message.
     */
    public static SendMessage createUnknownCommandMessage(String chatId, String lang) {
        return buildReplyMarkUpMessage(chatId, MessageProvider.get(lang, "unknown.command"),
            KeyboardFactory.createHelpKeyboard(lang));
    }

    /**
     * Method for creating login message.
     *
     * @param chatId {@link String} is telegram chat id.
     * @param lang   {@link String} is a language code of the telegram chat.
     *
     * @return {@link SendMessage} configured with the login message.
     */
    public static SendMessage createLoginMessage(String chatId, String lang) {
        return buildReplyMarkUpMessage(chatId, MessageProvider.get(lang, "login.message"),
            KeyboardFactory.createBackToMainMenuKeyboard(lang));
    }

    /**
     * Method for creating fail login message.
     *
     * @param chatId       {@link String} is telegram chat id.
     * @param errorMessage {@link String} is error message.
     * @param lang         {@link String} is a language code of the telegram chat.
     *
     * @return {@link SendMessage} configured with the fail login message with error
     *         message.
     */
    public static SendMessage createFailLoginMessage(String chatId, String errorMessage, String lang) {
        return buildReplyMarkUpMessage(chatId, String.format(MessageProvider.get(lang, "login.error"), errorMessage),
            KeyboardFactory.createBackToMainMenuKeyboard(lang));
    }

    /**
     * Method for creating success login message.
     *
     * @param chatId   {@link String} is telegram chat id.
     * @param userName {@link String} is username.
     * @param lang     {@link String} is a language code of the telegram chat.
     *
     * @return {@link SendMessage} configured with the success login message with
     *         username.
     */
    public static SendMessage createSuccessLoginMessage(String chatId, String userName, String lang) {
        return buildReplyMarkUpMessage(chatId, String.format(MessageProvider.get(lang, "login.success"), userName),
            KeyboardFactory.createHelpKeyboardForManager(lang));
    }

    /**
     * Method for creating manager notification message.
     *
     * @param telegramChatId {@link String} is telegram chat id.
     * @param username       {@link String} is username in telegram.
     * @param messageText    {@link String} is message.
     * @param innerChatId    {@link Long} is id for link.
     * @param lang           {@link String} is a language code of the telegram chat.
     *
     * @return {@link SendMessage} configured with the manager notification message.
     */
    public static SendMessage createNotificationMessageForManager(String telegramChatId, String username,
        String messageText, Long innerChatId, String lang) {
        SendMessage message = buildMessage(telegramChatId,
            String.format(MessageProvider.get(lang, "client.want.to.speak"), username, messageText, innerChatId));
        message.enableHtml(true);
        message.setParseMode(ParseMode.HTML);
        return message;
    }

    /**
     * Method for creating end support mode message.
     *
     * @param chatId {@link String} is telegram chat id.
     * @param lang   {@link String} is a language code of the telegram chat.
     * @return {@link SendMessage} configured with the end support mode message.
     */
    public static SendMessage createEndSupportMessage(String chatId, String lang) {
        return buildReplyMarkUpMessage(chatId, MessageProvider.get(lang, "client.stop.support.mode"),
            KeyboardFactory.createChatFeedbackRatingKeyboard());
    }

    /**
     * Method for creating user notification end support mode message.
     *
     * @param chatId   {@link String} is telegram chat id.
     * @param username {@link String} is username in telegram.
     * @param lang     {@link String} is a language code of the telegram chat.
     * @return {@link SendMessage} configured with the user notification end support
     *         mode message.
     */
    public static SendMessage createEndSupportModeNotification(String chatId, String username, String lang) {
        return buildMessage(chatId,
            String.format(MessageProvider.get(lang, "client.end.support.notification"), username));
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
     * @param lang   {@link String} is a language code of the telegram chat.
     * @return {@link SendMessage} configured with the support message with call
     *         back query.
     */
    public static SendMessage createSupportMessageCallBackQuery(String chatId, String lang) {
        return SendMessage
            .builder()
            .chatId(chatId)
            .text(MessageProvider.get(lang, "client.support.message.callback.query"))
            .replyMarkup(KeyboardFactory.userSupportKeyboard(lang))
            .build();
    }

    /**
     * Method for creating support message.
     *
     * @param chatId {@link String} is telegram chat id.
     * @param lang   {@link String} is a language code of the telegram chat.
     * @return {@link SendMessage} configured with the support message.
     */
    public static SendMessage createSupportReplyMarkup(String chatId, String lang) {
        return SendMessage
            .builder()
            .chatId(chatId)
            .text(MessageProvider.get(lang, "client.support.message.change.language"))
            .replyMarkup(KeyboardFactory.userSupportKeyboard(lang))
            .build();
    }

    /**
     * Method for creating work schedule message.
     *
     * @param chatId {@link String} is telegram chat id.
     * @param lang   {@link String} is a language code of the telegram chat.
     * @return {@link SendMessage} configured with the work schedule message.
     */
    public static SendMessage createWorkScheduleMessage(String chatId, String lang) {
        var message = buildReplyMarkUpMessage(chatId, MessageProvider.get(lang, "work.schedule.message"),
            KeyboardFactory.createBackToMainMenuKeyboard(lang));
        message.setParseMode(ParseMode.HTML);
        return message;
    }

    /**
     * Method for creating sorting prices message.
     *
     * @param chatId {@link String} is telegram chat id.
     * @param lang   {@link String} is a language code of the telegram chat.
     * @return {@link SendMessage} configured with the sorting prices message.
     */
    public static SendMessage createSortingPricesMessage(String chatId, String lang) {
        var message = buildReplyMarkUpMessage(chatId, MessageProvider.get(lang, "sorting.rules.pricing.message"),
            KeyboardFactory.createBackToMainMenuKeyboard(lang));
        message.setParseMode(ParseMode.HTML);
        return message;
    }

    /**
     * Method for creating admission rules message.
     *
     * @param chatId {@link String} is telegram chat id.
     * @param lang   {@link String} is a language code of the telegram chat.
     * @return {@link SendMessage} configured with the admission rules message.
     */
    public static SendMessage createAdmissionRulesMessage(String chatId, String lang) {
        var message = buildReplyMarkUpMessage(chatId, MessageProvider.get(lang, "admission.rules.text"),
            KeyboardFactory.createBackToMainMenuKeyboard(lang));
        message.setParseMode(ParseMode.HTML);
        return message;
    }

    /**
     * Method for creating green office message.
     *
     * @param chatId {@link String} is telegram chat id.
     * @param lang   {@link String} is a language code of the telegram chat.
     * @return {@link SendMessage} configured with the green office message.
     */
    public static SendMessage createGreenOfficeMessage(String chatId, String lang) {
        return buildReplyMarkUpMessage(chatId, MessageProvider.get(lang, "green.office.text"),
            KeyboardFactory.createProcessOrBackToMainMenuKeyboard(TelegramBotConstants.GREEN_OFFICE_PROCESS_CALLBACK,
                lang));
    }

    /**
     * Method for creating entering email message.
     *
     * @param chatId {@link String} is telegram chat id.
     * @param lang   {@link String} is a language code of the telegram chat.
     * @return {@link SendMessage} configured with the entering email message.
     */
    public static SendMessage createEnteringEmailMessage(String chatId, String lang) {
        return buildReplyMarkUpMessage(chatId, MessageProvider.get(lang, "entering.email.message"),
            KeyboardFactory.createBackToMainMenuKeyboard(lang));
    }

    /**
     * Method for creating invalid email message.
     *
     * @param chatId {@link String} is telegram chat id.
     * @param lang   {@link String} is a language code of the telegram chat.
     * @return {@link SendMessage} configured with the invalid email message.
     */
    public static SendMessage createInvalidEmailMessage(String chatId, String lang) {
        return buildReplyMarkUpMessage(chatId, MessageProvider.get(lang, "invalid.email.message"),
            KeyboardFactory.createBackToMainMenuKeyboard(lang));
    }

    /**
     * Method for creating green office thanks message.
     *
     * @param chatId {@link String} is telegram chat id.
     * @return {@link SendMessage} configured with the green office thanks message.
     */
    public static SendMessage createGreenOfficeThanksMessage(String chatId, String lang) {
        return buildReplyMarkUpMessage(chatId, MessageProvider.get(lang, "green.office.thank.you.message"),
            KeyboardFactory.createHelpKeyboard(lang));
    }

    /**
     * Method for creating feedback message.
     *
     * @param chatId {@link String} is telegram chat id.
     * @param lang   {@link String} is a language code of the telegram chat.
     * @return {@link SendMessage} configured with the feedback message.
     */
    public static SendMessage createFeedbackMessage(String chatId, String lang) {
        return buildReplyMarkUpMessage(chatId, MessageProvider.get(lang, "feedback.message"),
            KeyboardFactory.createFeedbackOrBackToMainMenuKeyboard(lang));
    }

    /**
     * Method for creating great feedback message.
     *
     * @param chatId {@link String} is telegram chat id.
     * @param lang   {@link String} is a language code of the telegram chat.
     * @return {@link SendMessage} configured with the great feedback message.
     */
    public static SendMessage createGreatFeedbackMessage(String chatId, String lang) {
        return buildMessage(chatId, MessageProvider.get(lang, "great.feedback.message"));
    }

    /**
     * Method for creating bad feedback message.
     *
     * @param chatId {@link String} is telegram chat id.
     * @param lang   {@link String} is a language code of the telegram chat.
     * @return {@link SendMessage} configured with the bad feedback message.
     */
    public static SendMessage createBadFeedbackMessage(String chatId, String lang) {
        return buildMessage(chatId, MessageProvider.get(lang, "bad.feedback.message"));
    }

    /**
     * Method for creating thanks feedback message.
     *
     * @param chatId {@link String} is telegram chat id.
     * @param lang   {@link String} is a language code of the telegram chat.
     * @return {@link SendMessage} configured with the thanks feedback message.
     */
    public static SendMessage createFeedbackThanksMessage(String chatId, String lang) {
        return buildReplyMarkUpMessage(chatId, MessageProvider.get(lang, "feedback.thank.you.message"),
            KeyboardFactory.createHelpKeyboard(lang));
    }

    /**
     * Method for creating unknown error occurred message.
     *
     * @param chatId {@link String} is telegram chat id.
     * @param lang   {@link String} is a language code of the telegram chat.
     * @return {@link SendMessage} configured with the unknown error occurred
     *         message.
     */
    public static SendMessage createUnknownErrorOccurredMessage(String chatId, String lang) {
        return buildReplyMarkUpMessage(chatId, MessageProvider.get(lang, "unknown.error"),
            KeyboardFactory.createBackToMainMenuKeyboard(lang));
    }

    /**
     * Method for creating a message for the user when the chat with the manager is
     * already open.
     *
     * @param chatId {@link String} is the Telegram chat ID.
     * @param lang   {@link String} is the language code of the Telegram chat.
     * @return {@link SendMessage} configured with the chat already open message.
     */
    public static SendMessage createChatAlreadyOpenMessage(String chatId, String lang) {
        return buildMessage(chatId, MessageProvider.get(lang, "manager.chat.already.open.message"));
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
     * Creates a {@link SendPhoto} object to send a photo from a
     * {@link MultipartFile} to a Telegram chat.
     *
     * @param chatId {@link String} the ID of the target chat
     * @param file   {@link MultipartFile} the file to be sent as a photo
     * @return a configured {@link SendPhoto} object
     * @throws IOException if reading the file input stream fails
     */
    public static SendPhoto createSendPhoto(String chatId, MultipartFile file, String caption) throws IOException {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(new InputFile(file.getInputStream(), file.getOriginalFilename()));
        if (caption != null && !caption.isBlank()) {
            sendPhoto.setCaption(caption.length() > 1024 ? caption.substring(0, 1024) : caption);
        }
        return sendPhoto;
    }

    /**
     * Creates a {@link SendDocument} object to send a file from a
     * {@link MultipartFile} to a Telegram chat.
     *
     * @param chatId {@link String} the ID of the target chat
     * @param file   {@link MultipartFile} the file to be sent as a file
     * @return a configured {@link SendDocument} object
     * @throws IOException if reading the file input stream fails
     */
    public static SendDocument createSendDocument(String chatId, String caption, MultipartFile file)
        throws IOException {
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(chatId);
        if (caption != null && !caption.isBlank()) {
            sendDocument.setCaption(caption.length() > 1024 ? caption.substring(0, 1024) : caption);
        }
        sendDocument.setDocument(new InputFile(file.getInputStream(), file.getOriginalFilename()));
        return sendDocument;
    }

    /**
     * Creates a Telegram {@link SendMessage} that removes the custom keyboard after
     * the support mode ends.
     *
     * <p>
     * This message includes a predefined text notifying the user that support mode
     * has ended, and attaches a {@link ReplyKeyboardRemove} to hide the keyboard.
     * </p>
     *
     * @param chatId the ID of the chat to send the message to
     * @param lang   {@link String} is a language code of the telegram chat.
     * @return a {@link SendMessage} object configured to remove the keyboard
     */
    public static SendMessage deleteEndSupportKeyboardMessage(String chatId, String lang) {
        SendMessage removeKeyboardMsg = new SendMessage();
        removeKeyboardMsg.setChatId(chatId);
        removeKeyboardMsg.setText(MessageProvider.get(lang, "client.stop.support.mode"));
        removeKeyboardMsg.setReplyMarkup(new ReplyKeyboardRemove(true));
        return removeKeyboardMsg;
    }

    /**
     * Builds a EditMessageText object with the specified chat ID and text to edit
     * message.
     *
     * @param chatId            {@link String} the telegram chat ID
     * @param telegramMessageId {@link Integer} the ID from telegram API
     * @param text              {@link String} the message text to edit
     * @return a EditMessageText object configured with the specified chat ID, text
     *         and telegram message ID
     */
    public static EditMessageText buildEditMessageText(String chatId, Integer telegramMessageId, String text) {
        validationChatId(chatId);
        validationText(text);
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(telegramMessageId);
        editMessage.setText(text);
        return editMessage;
    }

    /**
     * Builds a SendMediaGroup object with the specified chat ID and images.
     *
     * @param chatId {@link String} the telegram chat ID
     * @param images {@link List} the ID from telegram API
     * @param text   {@link String} the message text to edit
     * @return a SendMediaGroup object configured with the specified chat ID, images
     *         and text
     */
    public static SendMediaGroup buildSendMediaGroup(String chatId, List<MultipartFile> images, String text) {
        List<InputMedia> media = new ArrayList<>();
        try {
            for (MultipartFile file : images) {
                InputMediaPhoto photo = new InputMediaPhoto();
                photo.setMedia(file.getInputStream(), file.getOriginalFilename());
                media.add(photo);
            }
        } catch (IOException e) {
            throw new TelegramBotExecutionException("Failed to read image file for media group", e);
        }
        if (text != null && !text.isBlank()) {
            media.getFirst().setCaption(text);
        }
        SendMediaGroup sendMediaGroup = new SendMediaGroup();
        sendMediaGroup.setChatId(chatId);
        sendMediaGroup.setMedias(media);
        return sendMediaGroup;
    }

    /**
     * Builds a EditMessageText object with the specified chat ID and caption to
     * edit message with photo.
     *
     * @param chatId            {@link String} the telegram chat ID
     * @param telegramMessageId {@link Integer} the ID from telegram API
     * @param text              {@link String} the message text to edit
     * @return a EditMessageText object configured with the specified chat ID,
     *         caption and telegram message ID
     */
    public static EditMessageCaption buildEditMessageCaption(String chatId, Integer telegramMessageId, String text) {
        validationChatId(chatId);
        validationText(text);
        return EditMessageCaption.builder()
            .chatId(chatId)
            .messageId(telegramMessageId)
            .caption(text)
            .build();
    }

    private static void validationChatId(String chatId) {
        if (chatId == null || chatId.isBlank()) {
            throw new IllegalArgumentException("chatId cannot be null or blank");
        }
    }

    private static void validationText(String text) {
        if (text == null || text.isBlank() || text.length() >= 1000) {
            throw new IllegalArgumentException("text cannot be null or blank");
        }
    }
}
