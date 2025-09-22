package greencity.service.files;

import static greencity.ModelUtils.TEST_FILE;
import static greencity.ModelUtils.TEST_OWNER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import greencity.client.config.UserRemoteWebClient;
import greencity.dto.files.DeleteFileDto;
import greencity.dto.files.UploadFileDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {
    @Mock
    UserRemoteWebClient userRemoteWebClient;
    @InjectMocks
    private FileServiceImpl fileService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileService, "owner", TEST_OWNER);
    }

    @Test
    void uploadFileTest() {
        MultipartFile file = mock(MultipartFile.class);
        ArgumentCaptor<UploadFileDto> captor = ArgumentCaptor.forClass(UploadFileDto.class);

        when(userRemoteWebClient.uploadFile(any(UploadFileDto.class)))
            .thenReturn(TEST_FILE);

        String result = fileService.uploadFile(file);

        assertEquals(TEST_FILE, result);

        verify(userRemoteWebClient).uploadFile(captor.capture());
        UploadFileDto uploadFileDto = captor.getValue();

        assertEquals(file, uploadFileDto.getFile());
        assertEquals(TEST_OWNER, uploadFileDto.getOwner());
    }

    @Test
    void deleteFileTest() {
        ArgumentCaptor<DeleteFileDto> captor = ArgumentCaptor.forClass(DeleteFileDto.class);

        doNothing().when(userRemoteWebClient).deleteFile(any(DeleteFileDto.class));

        fileService.deleteFile(TEST_FILE);

        verify(userRemoteWebClient).deleteFile(captor.capture());
        DeleteFileDto deleteFileDto = captor.getValue();

        assertEquals(TEST_FILE, deleteFileDto.getPath());
        assertEquals(TEST_OWNER, deleteFileDto.getOwner());
    }
}
