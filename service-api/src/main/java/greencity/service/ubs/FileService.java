package greencity.service.ubs;

import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

public interface FileService {
    /**
     * Upload file to Azure Cloud Storage.
     *
     * @param multipartFile image file to save.
     * @return public image url.
     **/
    String upload(MultipartFile multipartFile);

    /**
     * Upload file to Azure Cloud Storage.
     *
     * @param inputStream      {@link InputStream} input stream
     * @param originalFileName {@link String} image name to save.
     * @param fileSize         {@link Long} size of file
     * @return public image url.
     **/
    String upload(InputStream inputStream, String originalFileName, long fileSize);

    /**
     * Delete file from Azure Cloud Storage.
     *
     * @param path {@link String}
     */
    void delete(String path);
}
