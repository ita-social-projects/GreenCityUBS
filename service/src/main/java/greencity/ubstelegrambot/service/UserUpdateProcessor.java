package greencity.ubstelegrambot.service;

import greencity.constant.TelegramBotConstants;
import greencity.entity.telegram.*;
import greencity.enums.ChatState;
import greencity.repository.*;
import greencity.service.ubs.*;
import greencity.ubstelegrambot.messages.MessageFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import java.util.Optional;

@Slf4j
@Service("userUpdateProcessor")
@RequiredArgsConstructor
public class UserUpdateProcessor implements TelegramUpdateProcessor {
    private final TelegramChatRepository telegramChatRepository;
    private final TelegramUtils telegramUtils;
    private final TelegramLoginService telegramLoginService;
    private final TelegramFeedbackService telegramFeedbackService;
    private final TelegramSupportService telegramSupportService;
    private final TelegramGreenOfficeService telegramGreenOfficeService;
    private final TelegramCommandsService telegramCommandsService;
    private final TelegramLanguageService telegramLanguageService;

    /**
     * Handles incoming updates related to user interactions in Telegram.
     * {@inheritDoc}
     */
    @Override
    public SendMessage process(Update update) {
        if (update.hasCallbackQuery()) {
            CallbackQuery callBackQuery = update.getCallbackQuery();
            String chatId = callBackQuery.getMessage().getChatId().toString();
            String lang = telegramLanguageService.getChatLanguage(chatId);
            switch (callBackQuery.getData()) {
                case TelegramBotConstants.CLIENT_SUPPORT_CALLBACK -> {
                    return telegramUtils.updateChatStateAndRespond(chatId, ChatState.IN_SUPPORT,
                        MessageFactory.createSupportMessageCallBackQuery(chatId, lang));
                }
                case TelegramBotConstants.SORTING_PRICES_CALLBACK -> {
                    return telegramUtils.updateChatStateAndRespond(chatId, ChatState.NORMAL,
                        MessageFactory.createSortingPricesMessage(chatId, lang));
                }
                case TelegramBotConstants.WORK_SCHEDULE_CALLBACK -> {
                    return telegramUtils.updateChatStateAndRespond(chatId, ChatState.NORMAL,
                        MessageFactory.createWorkScheduleMessage(chatId, lang));
                }
                case TelegramBotConstants.ADMISSION_RULES_CALLBACK -> {
                    return telegramUtils.updateChatStateAndRespond(chatId, ChatState.NORMAL,
                        MessageFactory.createAdmissionRulesMessage(chatId, lang));
                }
                case TelegramBotConstants.GREEN_OFFICE_CALLBACK -> {
                    return telegramUtils.updateChatStateAndRespond(chatId, ChatState.NORMAL,
                        MessageFactory.createGreenOfficeMessage(chatId, lang));
                }
                case TelegramBotConstants.GREEN_OFFICE_PROCESS_CALLBACK -> {
                    return telegramUtils.updateChatStateAndRespond(chatId, ChatState.ENTERING_GREEN_OFFICE_EMAIL,
                        MessageFactory.createEnteringEmailMessage(chatId, lang));
                }
                case TelegramBotConstants.FEEDBACK_CALLBACK -> {
                    return telegramUtils.updateChatStateAndRespond(chatId, ChatState.NORMAL,
                        MessageFactory.createFeedbackMessage(chatId, lang));
                }
                case TelegramBotConstants.RATING_TERRIBLY_CALLBACK -> {
                    return telegramFeedbackService.processRatingFeedbackRequest(chatId, 1);
                }
                case TelegramBotConstants.RATING_BADLY_CALLBACK -> {
                    return telegramFeedbackService.processRatingFeedbackRequest(chatId, 2);
                }
                case TelegramBotConstants.RATING_SATISFACTORILY_CALLBACK -> {
                    return telegramFeedbackService.processRatingFeedbackRequest(chatId, 3);
                }
                case TelegramBotConstants.RATING_GOOD_CALLBACK -> {
                    return telegramFeedbackService.processRatingFeedbackRequest(chatId, 4);
                }
                case TelegramBotConstants.RATING_PERFECTLY_CALLBACK -> {
                    return telegramFeedbackService.processRatingFeedbackRequest(chatId, 5);
                }
                case TelegramBotConstants.LOGIN_CALLBACK -> {
                    return telegramUtils.updateChatStateAndRespond(chatId, ChatState.LOGGING_AS_MANAGER,
                        MessageFactory.createLoginMessage(chatId, lang));
                }
                default -> {
                    return telegramUtils.updateChatStateAndRespond(chatId, ChatState.NORMAL,
                        MessageFactory.createAvailableCommandsMessage(chatId, lang));
                }
            }
        } else {
            var message = update.getMessage();

            Optional<TelegramChat> chatOpt = telegramChatRepository.findByChatId(message.getChatId().toString());

            if (chatOpt.isEmpty()) {
                return MessageFactory.createUnknownErrorOccurredMessage(message.getChatId().toString(),
                    TelegramBotConstants.UA);
            }
            String lang = chatOpt.get().getLanguageCode();
            TelegramChat chat = chatOpt.get();
            switch (chat.getChatState()) {
                case IN_SUPPORT -> {
                    return telegramSupportService.processSupportMessage(message, lang);
                }
                case ENTERING_GREEN_OFFICE_EMAIL -> {
                    return telegramGreenOfficeService.processGreenOfficeEmail(message, lang);
                }
                case MAKING_FEEDBACK -> {
                    return telegramFeedbackService.processInputCommentRequest(message, lang);
                }
                case LOGGING_AS_MANAGER -> {
                    return telegramLoginService.processInputManagerCredentialsRequest(message, lang);
                }
                default -> {
                    return telegramCommandsService.processCommand(message, lang);
                }
            }
        }
    }
}
