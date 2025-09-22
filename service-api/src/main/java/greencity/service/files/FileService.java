package greencity.service.files;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    /**
     * Method for uploading a file.
     *
     * @param file file to save.
     * @return url of the saved file.
     */
    String uploadFile(MultipartFile file);

    /**
     * Method for deleting a file.
     *
     * @param path url of the file to delete.
     */
    void deleteFile(String path);
}
