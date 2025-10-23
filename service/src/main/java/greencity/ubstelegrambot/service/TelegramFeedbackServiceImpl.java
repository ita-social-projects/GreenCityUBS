package greencity.ubstelegrambot.service;

import greencity.client.UserRemoteClient;
import greencity.constant.constant.TelegramBotConstants;
import greencity.dto.pageble.PageableDto;
import greencity.dto.telegram.FeedbackDto;
import greencity.dto.telegram.UserTelegramFeedbackDto;
import greencity.entity.telegram.ChatFeedback;
import greencity.entity.telegram.TelegramChat;
import greencity.enums.ChatState;
import greencity.enums.FeedbackState;
import greencity.enums.MessageType;
import greencity.repository.ChatFeedbackRepository;
import greencity.repository.TelegramChatRepository;
import greencity.service.ubs.TelegramBotResponseService;
import greencity.service.ubs.TelegramFeedbackService;
import greencity.service.ubs.TelegramLanguageService;
import greencity.ubstelegrambot.messages.MessageFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TelegramFeedbackServiceImpl implements TelegramFeedbackService {
    private final TelegramChatRepository telegramChatRepository;
    private final ChatFeedbackRepository chatFeedbackRepository;
    private final TelegramLanguageService telegramLanguageService;
    private final UserRemoteClient userRemoteClient;
    private final TelegramBotResponseService telegramBotResponseService;

    /**
     * {@inheritDoc}
     */
    @Override
    public SendMessage processInputCommentRequest(Message message, String lang) {
        Optional<TelegramChat> telegramChat = telegramChatRepository.findByChatId(message.getChatId().toString());

        if (telegramChat.isEmpty()) {
            String text = telegramBotResponseService.getResponseByLangAndMessageType(
                TelegramBotConstants.UK, MessageType.UNKNOWN_ERROR);
            return MessageFactory.createUnknownErrorOccurredMessage(message.getChatId().toString(),
                TelegramBotConstants.UK, text);
        }

        Optional<ChatFeedback> chatFeedback = chatFeedbackRepository
            .findByChatIdAndFeedbackState(
                telegramChat.get().getId(),
                FeedbackState.IN_PROGRESS);

        if (chatFeedback.isEmpty()) {
            String text = telegramBotResponseService.getResponseByLangAndMessageType(lang, MessageType.UNKNOWN_ERROR);
            return MessageFactory.createUnknownErrorOccurredMessage(message.getChatId().toString(), lang, text);
        }

        ChatFeedback feedback = chatFeedback.get();
        feedback.setComment(message.getText());
        feedback.setFeedbackState(FeedbackState.CLOSED);
        chatFeedbackRepository.save(feedback);

        TelegramChat chat = telegramChat.get();
        chat.setChatState(ChatState.NORMAL);
        chat.setChatStateUpdatedAt(Instant.now());
        telegramChatRepository.save(chat);

        UserTelegramFeedbackDto feedbackDto = UserTelegramFeedbackDto.builder()
            .chatId(chat.getChatId())
            .rating(feedback.getRating())
            .comment(feedback.getComment())
            .name(chat.getUsername())
            .subject("Новий відгук з Telegram")
            .build();

        try {
            userRemoteClient.sendTelegramFeedback(feedbackDto);
        } catch (RuntimeException ex) {
            log.warn("Failed to send Telegram feedback email for chatId={}, continuing", chat.getChatId(), ex);
        }
        String text = telegramBotResponseService.getResponseByLangAndMessageType(
            lang, MessageType.FEEDBACK_THANK_YOU_MESSAGE);
        return MessageFactory.createFeedbackThanksMessage(message.getChatId().toString(), lang, text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SendMessage processRatingFeedbackRequest(String chatId, int rating) {
        Optional<TelegramChat> chat = telegramChatRepository.findByChatId(chatId);

        if (chat.isEmpty()) {
            String text = telegramBotResponseService.getResponseByLangAndMessageType(
                TelegramBotConstants.UK, MessageType.UNKNOWN_ERROR);
            return MessageFactory.createUnknownErrorOccurredMessage(chatId, TelegramBotConstants.UK, text);
        }

        chat.get().setChatState(ChatState.MAKING_FEEDBACK);
        chat.get().setChatStateUpdatedAt(Instant.now());
        telegramChatRepository.save(chat.get());

        Optional<ChatFeedback> inProgressFeedback = chatFeedbackRepository
            .findByChatIdAndFeedbackState(chat.get().getId(), FeedbackState.IN_PROGRESS);

        if (inProgressFeedback.isPresent()) {
            inProgressFeedback.get().setFeedbackState(FeedbackState.CLOSED);
            chatFeedbackRepository.save(inProgressFeedback.get());
        }

        ChatFeedback chatFeedback = ChatFeedback.builder()
            .rating(rating)
            .feedbackState(FeedbackState.IN_PROGRESS)
            .chat(chat.get())
            .build();

        chatFeedbackRepository.save(chatFeedback);
        String lang = telegramLanguageService.getChatLanguage(chat.get().getChatId());
        if (rating >= 4) {
            String text = telegramBotResponseService.getResponseByLangAndMessageType(
                lang, MessageType.GREAT_FEEDBACK_MESSAGE);
            return MessageFactory.createGreatFeedbackMessage(chatId, text);
        }

        String text = telegramBotResponseService.getResponseByLangAndMessageType(
            lang, MessageType.BAD_FEEDBACK_MESSAGE);
        return MessageFactory.createBadFeedbackMessage(chatId, text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableDto<FeedbackDto> getAllFeedbacks(Pageable pageable) {
        Page<ChatFeedback> chatFeedbacks = chatFeedbackRepository.findAll(pageable);
        List<FeedbackDto> feedbackDtos = chatFeedbacks
            .getContent()
            .stream()
            .map(feedback -> new FeedbackDto(
                feedback.getId(),
                feedback.getChat().getId().toString(),
                feedback.getRating(),
                feedback.getComment()))
            .toList();

        return new PageableDto<>(
            feedbackDtos,
            chatFeedbacks.getTotalElements(),
            chatFeedbacks.getNumber(),
            chatFeedbacks.getTotalPages());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableDto<FeedbackDto> getAllFeedbacksByChatId(Long chatId, Pageable pageable) {
        Page<ChatFeedback> chatFeedbacks = chatFeedbackRepository.findByChatId(chatId, pageable);
        List<FeedbackDto> feedbackDtos = chatFeedbacks
            .getContent()
            .stream()
            .map(feedback -> new FeedbackDto(
                feedback.getId(),
                feedback.getChat().getChatId(),
                feedback.getRating(),
                feedback.getComment()))
            .toList();

        return new PageableDto<>(
            feedbackDtos,
            chatFeedbacks.getTotalElements(),
            chatFeedbacks.getNumber(),
            chatFeedbacks.getTotalPages());
    }
}
