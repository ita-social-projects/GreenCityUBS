package greencity.service.ubs;

import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import java.util.List;

public interface TelegramPhotoService {
    /**
     * Downloads photos from a Telegram message.
     *
     * @param message the Telegram message containing the photo
     * @return a list of URLs of the downloaded photos
     */
    List<String> downloadPhotoFromTelegram(Message message);

    /**
     * Saves photos to Azure Blob storage.
     *
     * @param photoUrls the list of photo URLs to save
     * @return a list of URLs of the saved photos in Azure
     */
    List<String> savePhotoToAzureBlob(List<String> photoUrls);

    /**
     * Saves a photo to Azure Blob storage.
     *
     * @param file the photo file to save
     * @return the URL of the saved photo in Azure
     */
    String savePhotoToAzureBlob(MultipartFile file);

    /**
     * Deletes a photo from Azure Blob storage.
     *
     * @param url the URL of the photo to delete
     */
    void deletePhotoFromAzureBlob(String url);

    /**
     * Saves photo information to the database.
     *
     * @param photoUrls the list of photo URLs
     * @param chatId    the Telegram chat ID
     * @param caption   the caption for the photos
     */
    void saveToDB(List<String> photoUrls, String chatId, String caption, boolean isManagerPhoto, Long managerId);

    /**
     * Sends a photo to a Telegram user.
     *
     * @param chatId   the Telegram chat ID
     * @param photoUrl the URL of the photo to send
     * @param caption  the caption for the photo
     */
    void sendPhotoToUser(String chatId, String photoUrl, String caption);
}
