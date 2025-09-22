package greencity.service.files;

import greencity.client.config.UserRemoteWebClient;
import greencity.dto.files.CleanupFilesDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageCleanupServiceImpl implements StorageCleanupService {
    private final FileStorageFacade fileStorageFacade;
    private final UserRemoteWebClient userRemoteWebClient;
    @Value("UBS")
    private String owner;

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
