package greencity.ubstelegrambot.service;

import greencity.dto.pageble.PageableDto;
import greencity.dto.telegram.FeedbackDto;
import greencity.entity.telegram.ChatFeedback;
import greencity.entity.telegram.TelegramChat;
import greencity.enums.ChatState;
import greencity.enums.FeedbackState;
import greencity.repository.ChatFeedbackRepository;
import greencity.repository.TelegramChatRepository;
import greencity.service.ubs.TelegramFeedbackService;
import greencity.ubstelegrambot.messages.MessageFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TelegramFeedbackServiceImpl implements TelegramFeedbackService {
    private final TelegramChatRepository telegramChatRepository;
    private final ChatFeedbackRepository chatFeedbackRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public SendMessage processInputCommentRequest(Message message) {
        Optional<TelegramChat> telegramChat = telegramChatRepository.findByChatId(message.getChatId().toString());

        if (telegramChat.isEmpty()) {
            return MessageFactory.createUnknownErrorOccurredMessage(message.getChatId().toString());
        }

        Optional<ChatFeedback> chatFeedback = chatFeedbackRepository
            .findByChatIdAndFeedbackState(
                telegramChat.get().getId(),
                FeedbackState.IN_PROGRESS);

        if (chatFeedback.isEmpty()) {
            return MessageFactory.createUnknownErrorOccurredMessage(message.getChatId().toString());
        }

        chatFeedback.get().setComment(message.getText());
        chatFeedback.get().setFeedbackState(FeedbackState.CLOSED);
        chatFeedbackRepository.save(chatFeedback.get());
        telegramChat.get().setChatState(ChatState.NORMAL);
        telegramChat.get().setChatStateUpdatedAt(Instant.now());
        telegramChatRepository.save(telegramChat.get());
        return MessageFactory.createFeedbackThanksMessage(message.getChatId().toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SendMessage processRatingFeedbackRequest(String chatId, int rating) {
        Optional<TelegramChat> chat = telegramChatRepository.findByChatId(chatId);

        if (chat.isEmpty()) {
            return MessageFactory.createUnknownErrorOccurredMessage(chatId);
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

        if (rating >= 4) {
            return MessageFactory.createGreatFeedbackMessage(chatId);
        }

        return MessageFactory.createBadFeedbackMessage(chatId);
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
    public PageableDto<FeedbackDto> getAllFeedbacksByChatId(String chatId, Pageable pageable) {
        Page<ChatFeedback> chatFeedbacks = chatFeedbackRepository.findByChatIdPageable(chatId, pageable);
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
