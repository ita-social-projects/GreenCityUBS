package greencity.ubstelegrambot.service;

import greencity.client.UserRemoteClient;
import greencity.constant.TelegramBotConstants;
import greencity.dto.pageble.PageableDto;
import greencity.dto.telegram.FeedbackDto;
import greencity.dto.telegram.UserTelegramFeedbackDto;
import greencity.entity.telegram.ChatFeedback;
import greencity.entity.telegram.TelegramChat;
import greencity.enums.ChatState;
import greencity.enums.FeedbackState;
import greencity.repository.ChatFeedbackRepository;
import greencity.repository.TelegramChatRepository;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public SendMessage processInputCommentRequest(Message message, String lang) {
        Optional<TelegramChat> telegramChat = telegramChatRepository.findByChatId(message.getChatId().toString());

        if (telegramChat.isEmpty()) {
            return MessageFactory.createUnknownErrorOccurredMessage(message.getChatId().toString(),
                TelegramBotConstants.UK);
        }

        Optional<ChatFeedback> chatFeedback = chatFeedbackRepository
            .findByChatIdAndFeedbackState(
                telegramChat.get().getId(),
                FeedbackState.IN_PROGRESS);

        if (chatFeedback.isEmpty()) {
            return MessageFactory.createUnknownErrorOccurredMessage(message.getChatId().toString(), lang);
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
        return MessageFactory.createFeedbackThanksMessage(message.getChatId().toString(), lang);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SendMessage processRatingFeedbackRequest(String chatId, int rating) {
        Optional<TelegramChat> chat = telegramChatRepository.findByChatId(chatId);

        if (chat.isEmpty()) {
            return MessageFactory.createUnknownErrorOccurredMessage(chatId, TelegramBotConstants.UK);
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
            return MessageFactory.createGreatFeedbackMessage(chatId, lang);
        }

        return MessageFactory.createBadFeedbackMessage(chatId, lang);
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
