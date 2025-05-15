package greencity.ubstelegrambot.service;

import greencity.entity.telegram.Image;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.image.FileNotSavedException;
import greencity.mapping.telegrammessage.ImageConverter;
import greencity.repository.AuthorizedUserRepository;
import greencity.repository.TelegramImageRepository;
import greencity.service.ubs.TelegramPhotoService;
import greencity.service.ubs.AzureCloudStorageService;
import greencity.service.ubs.BASE64DecodedMultipartFile;
import greencity.service.ubs.TelegramStreamingService;
import greencity.ubstelegrambot.UBSTelegramBot;
import greencity.ubstelegrambot.messages.MessageFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TelegramPhotoServiceImpl implements TelegramPhotoService {
    private final TelegramExecutor executor;
    private final AzureCloudStorageService azureCloudStorageService;
    private final TelegramImageRepository telegramImageRepository;
    private final TelegramExecutor telegramExecutor;
    private final ApplicationContext applicationContext;
    private final AuthorizedUserRepository telegramBotRepository;
    private final ImageConverter imageConverter;
    private final TelegramStreamingService telegramStrimingService;
    private static final String PHOTO_NOT_FOUND = "Photo not found in message";
    private static final String LARGEST_PHOTO_NOT_FOUND = "Cannot determine largest photo";
    private static final String CHAT_NOT_FOUND = "Chat with id %s not found";
    private static final String IMAGE_CONTENT_TYPE = "image/jpeg";
    private static final String FAILED_TO_SAVE_PHOTO_TO_AZURE = "Error saving file to Azure";

    @Override
    public List<String> downloadPhotoFromTelegram(Message message) {
        var bot = applicationContext.getBean(UBSTelegramBot.class);
        List<PhotoSize> photos = message.getPhoto();
        File file = getFile(photos, bot);
        return List.of(file.getFileUrl(bot.getBotToken()));
    }

    @Override
    public List<String> savePhotoToAzureBlob(List<String> photoUrls) {
        List<String> savedPhotoUrls = new ArrayList<>();
        for (String urlString : photoUrls) {
            try {
                URI uri = URI.create(urlString);
                try (InputStream inputStream = uri.toURL().openStream()) {
                    byte[] content = inputStream.readAllBytes();

                    MultipartFile file = BASE64DecodedMultipartFile.builder()
                        .name(getFileNameFromUrl(urlString))
                        .content(content)
                        .contentType(IMAGE_CONTENT_TYPE)
                        .build();

                    savedPhotoUrls.add(azureCloudStorageService.upload(file));
                }
            } catch (IOException | IllegalArgumentException e) {
                throw new FileNotSavedException(FAILED_TO_SAVE_PHOTO_TO_AZURE);
            }
        }
        return savedPhotoUrls;
    }

    @Override
    public String savePhotoToAzureBlob(MultipartFile file) {
        return azureCloudStorageService.upload(file);
    }

    @Override
    public void deletePhotoFromAzureBlob(String url) {
        azureCloudStorageService.delete(url);
    }

    @Override
    public void saveToDB(List<String> photoUrl, String chatId, String caption) {
        for (String url : photoUrl) {
            Image telegramUserPhotos = new Image(
                chatId,
                url,
                caption);
            telegramImageRepository.save(telegramUserPhotos);
            telegramStrimingService.streamMessages(chatId, imageConverter.map(telegramUserPhotos));
        }
    }

    @Override
    public void sendPhotoToUser(String chatId, String photoUrl, String caption) {
        var chat = telegramBotRepository.findByChatId(chatId);
        if (chat.isEmpty()) {
            throw new NotFoundException(String.format(CHAT_NOT_FOUND, chatId));
        }
        var message = MessageFactory.createPhotoSender(chatId, photoUrl, caption);
        Message returnMessage =
            telegramExecutor.executeSendPhoto(applicationContext.getBean(UBSTelegramBot.class), message);
        List<PhotoSize> photos = returnMessage.getPhoto();
        var bot = applicationContext.getBean(UBSTelegramBot.class);

        File file = getFile(photos, bot);

        var variable = file.getFileUrl(bot.getBotToken());

        saveToDB(List.of(variable), chatId, caption);
    }

    private String getFileNameFromUrl(String fileUrl) {
        String[] parts = fileUrl.split("/");
        return parts[parts.length - 1];
    }

    private File getFile(List<PhotoSize> photos, TelegramLongPollingBot bot) {
        if (photos.isEmpty()) {
            throw new BadRequestException(PHOTO_NOT_FOUND);
        }
        PhotoSize largestPhoto = photos.stream()
            .max(Comparator.comparing(PhotoSize::getFileSize))
            .orElseThrow(() -> new RuntimeException(LARGEST_PHOTO_NOT_FOUND));

        String fileId = largestPhoto.getFileId();
        GetFile getFileRequest = new GetFile();
        getFileRequest.setFileId(fileId);

        return executor.executeGetFile(bot, getFileRequest);
    }
}
