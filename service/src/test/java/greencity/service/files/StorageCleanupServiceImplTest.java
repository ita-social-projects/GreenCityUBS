package greencity.service.files;

import static greencity.ModelUtils.FILE_LIST;
import static greencity.ModelUtils.TEST_OWNER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import greencity.client.config.UserRemoteWebClient;
import greencity.dto.files.CleanupFilesDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
class StorageCleanupServiceImplTest {
    @Mock
    FileStorageFacade fileStorageFacade;
    @Mock
    UserRemoteWebClient userRemoteWebClient;
    @InjectMocks
    private StorageCleanupServiceImpl storageCleanupServiceImpl;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(storageCleanupServiceImpl, "owner", TEST_OWNER);
    }

    @Test
    void cleanUpTest() {
        ArgumentCaptor<CleanupFilesDto> captor = ArgumentCaptor.forClass(CleanupFilesDto.class);

        doNothing().when(userRemoteWebClient).cleanUp(any(CleanupFilesDto.class));
        when(fileStorageFacade.getFilePaths()).thenReturn(FILE_LIST);

        storageCleanupServiceImpl.cleanUp();

        verify(userRemoteWebClient).cleanUp(captor.capture());
        CleanupFilesDto cleanupFilesDto = captor.getValue();

        assertEquals(FILE_LIST, cleanupFilesDto.getPaths());
        assertEquals(TEST_OWNER, cleanupFilesDto.getOwner());
    }
}
