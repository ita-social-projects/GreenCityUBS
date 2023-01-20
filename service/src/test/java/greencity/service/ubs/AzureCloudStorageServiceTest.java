package greencity.service.ubs;

import com.azure.storage.blob.*;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.image.FileIsNullException;
import greencity.exceptions.image.FileNotSavedException;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.PropertyResolver;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AzureCloudStorageServiceTest {

    @BeforeEach
    void setUp() {
        MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setProperty("azure.connection.string", " ");
        mockEnvironment.setProperty("azure.container.name", " ");
        propertyResolver = mockEnvironment;
        azureCloudStorageService = spy(new AzureCloudStorageService(propertyResolver));
    }

    @Mock
    private PropertyResolver propertyResolver;

    private AzureCloudStorageService azureCloudStorageService;

    @Mock
    BlobContainerClient containerClient;

    @Mock
    BlobClient blobClient;

    @Test
    void upload() {
        MultipartFile multipartFile = new MockMultipartFile("Image", "Image".getBytes(StandardCharsets.UTF_8));
        doReturn(containerClient).when(azureCloudStorageService).containerClient();
        when(containerClient.getBlobClient(anyString())).thenReturn(blobClient);
        doReturn("blobUrl").when(blobClient).getBlobUrl();
        azureCloudStorageService.upload(multipartFile);
        assertNotNull(multipartFile);
        assertNotNull(azureCloudStorageService.getConnectionString());
        assertNotNull(azureCloudStorageService.getContainerName());
        verify(containerClient).getBlobClient(anyString());
        verify(blobClient).upload(any(InputStream.class), anyLong());
        verify(blobClient).getBlobUrl();
    }

    @Test
    void delete() {
        doReturn(containerClient).when(azureCloudStorageService).containerClient();
        when(containerClient.getBlobClient(anyString())).thenReturn(blobClient);
        when(blobClient.exists()).thenReturn(true);
        doNothing().when(blobClient).delete();
        azureCloudStorageService.delete("url/somepath/somefile.txt");
        assertNotNull(azureCloudStorageService.getConnectionString());
        assertNotNull(azureCloudStorageService.getContainerName());
        verify(containerClient).getBlobClient(anyString());
        assertEquals(true, blobClient.exists());
        verify(blobClient).delete();
    }

    @Test
    void checkUploadThrowsException() {
        doReturn(containerClient).when(azureCloudStorageService).containerClient();
        when(containerClient.getBlobClient(anyString())).thenReturn(blobClient);
        doAnswer(invocation -> {
            throw new IOException();
        }).when(blobClient).upload(any(InputStream.class), anyLong());
        MultipartFile multipartFile = new MockMultipartFile("Image", "Image".getBytes(StandardCharsets.UTF_8));
        assertThrows(FileNotSavedException.class, () -> azureCloudStorageService.upload(multipartFile));
    }

    @Test
    void checkUploadNullImage() {
        MultipartFile multipartFile = null;
        assertThrows(FileIsNullException.class, () -> azureCloudStorageService.upload(multipartFile));
    }

    @Test
    void checkDeleteThrowsException() {
        assertThrows(BadRequestException.class, () -> azureCloudStorageService.delete("aa#26#1"));
    }
}