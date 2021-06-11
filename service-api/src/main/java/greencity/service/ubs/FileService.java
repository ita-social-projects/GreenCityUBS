package greencity.service.ubs;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    /**
     * Upload file to Azure Cloud Storage.
     *
     * @param multipartFile image file to save.
     * @return public image url.
     **/
    String upload(MultipartFile multipartFile);
}
