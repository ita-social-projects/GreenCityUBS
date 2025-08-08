package greencity.ubstelegrambot.service;

import greencity.entity.telegram.TelegramChat;
import greencity.repository.TelegramChatRepository;
import greencity.service.ubs.TelegramUpdateProcessor;
import lombok.RequiredArgsConstructor;
import org.jvnet.hk2.annotations.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@RequiredArgsConstructor
public class LanguageSwitcherProcessor implements TelegramUpdateProcessor {

    private final TelegramChatRepository chatRepository;
    //private final MessageLocalizer localizer;


    @Override
    public SendMessage process(Update update) {
        String chatId = update.getCallbackQuery().getFrom().getId().toString();
        String callback = update.getCallbackQuery().getData();

        TelegramChat chat = chatRepository.findByChatId(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        String newLang = callback.substring("SET_LANGUAGE_".length());
        chat.setLanguageCode(newLang);
        chatRepository.save(chat);

  //      String messageText = localizer.get("menu.language_changed", newLang);

        SendMessage response = new SendMessage(chatId, "");
   //     response.setReplyMarkup(mainMenuWithLangButtons(newLang));

        return response;
    }
}
