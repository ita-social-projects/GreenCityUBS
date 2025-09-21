package greencity.service.files;

import greencity.client.config.UserRemoteWebClient;
import greencity.dto.files.CleanupFilesDto;
import greencity.dto.files.DeleteFileDto;
import greencity.dto.files.UploadFileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    private final FileStorageFacade fileStorageFacade;
    private final UserRemoteWebClient userRemoteWebClient;
    @Value("UBS")
    private String owner;

    @Override
    public String uploadFile(MultipartFile file) {
        UploadFileDto fileDto = UploadFileDto.builder()
            .file(file)
            .owner(owner)
            .build();

        return userRemoteWebClient.uploadFile(fileDto);
    }

    @Override
    public void deleteFile(String path) {
        DeleteFileDto deleteFileDto = DeleteFileDto.builder()
            .path(path)
            .owner(owner)
            .build();

        userRemoteWebClient.deleteFile(deleteFileDto);
    }

    @Override
    public void cleanUp() {
        List<String> filePaths = fileStorageFacade.getFilePaths();
        CleanupFilesDto cleanupFilesDto = CleanupFilesDto.builder()
            .paths(filePaths)
            .owner(owner)
            .build();

        userRemoteWebClient.cleanUp(cleanupFilesDto);
    }
}
