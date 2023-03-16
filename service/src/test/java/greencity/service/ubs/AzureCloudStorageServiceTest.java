package greencity.service.ubs;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import greencity.constant.ErrorMessage;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.image.FileNotSavedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.PropertyResolver;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class AzureCloudStorageServiceTest {

    private final String connectionString =
        "DefaultEndpointsProtocol=https;AccountName=2fdsgd;AccountKey=qV2VLads==;EndpointSuffix=core.windows.net";

    private final String containerName = "container";

    @Mock
    private PropertyResolver propertyResolver;

    private AzureCloudStorageService azureCloudStorageService;

    @Mock
    private BlobContainerClient containerClient;

    @Mock
    private BlobClient blobClient;

    @BeforeEach
    void setUp() {
        MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setProperty("azure.connection.string", connectionString);
        mockEnvironment.setProperty("azure.container.name", containerName);
        propertyResolver = mockEnvironment;
        azureCloudStorageService = spy(new AzureCloudStorageService(propertyResolver));
    }

    @Test
    void checkUpload() {
        MultipartFile multipartFile = new MockMultipartFile("Image", "Image".getBytes(StandardCharsets.UTF_8));
        doReturn(containerClient).when(azureCloudStorageService).containerClient();
        when(containerClient.getBlobClient(anyString())).thenReturn(blobClient);
        doReturn("blobUrl").when(blobClient).getBlobUrl();
        azureCloudStorageService.upload(multipartFile);
        assertNotNull(azureCloudStorageService.getConnectionString());
        assertNotNull(azureCloudStorageService.getContainerName());
        assertEquals(connectionString, azureCloudStorageService.getConnectionString());
        assertEquals(containerName, azureCloudStorageService.getContainerName());
        verify(containerClient).getBlobClient(anyString());
        verify(blobClient).upload(any(InputStream.class), anyLong());
        verify(blobClient).getBlobUrl();
    }

    @Test
    void checkDelete() {
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
    void checkDeleteIfClientNull() {
        doReturn(containerClient).when(azureCloudStorageService).containerClient();
        when(containerClient.getBlobClient(anyString())).thenReturn(blobClient);
        when(blobClient.exists()).thenReturn(null);
        azureCloudStorageService.delete("url/somepath/somefile.txt");
        verify(blobClient, never()).delete();
    }

    @Test
    void checkDeleteIfClientFalse() {
        doReturn(containerClient).when(azureCloudStorageService).containerClient();
        when(containerClient.getBlobClient(anyString())).thenReturn(blobClient);
        when(blobClient.exists()).thenReturn(false);
        azureCloudStorageService.delete("url/somepath/somefile.txt");
        verify(blobClient, never()).delete();
    }

    @Test
    void checkUploadThrowsException() {
        doReturn(containerClient).when(azureCloudStorageService).containerClient();
        when(containerClient.getBlobClient(anyString())).thenReturn(blobClient);
        doAnswer(invocation -> {
            throw new IOException();
        }).when(blobClient).upload(any(InputStream.class), anyLong());
        MultipartFile multipartFile = new MockMultipartFile("Image", "Image".getBytes(StandardCharsets.UTF_8));
        FileNotSavedException ex =
            assertThrows(FileNotSavedException.class, () -> azureCloudStorageService.upload(multipartFile));
        assertEquals(ErrorMessage.FILE_NOT_SAVED, ex.getMessage());
    }

    @Test
    void checkUploadNullImage() {
        MultipartFile multipartFile = null;
        IllegalArgumentException ex =
            assertThrows(IllegalArgumentException.class, () -> azureCloudStorageService.upload(multipartFile));
        assertEquals(ErrorMessage.FILE_IS_NULL, ex.getMessage());
    }

    @Test
    void checkInvalidConnectionString() {
        azureCloudStorageService = new AzureCloudStorageService(new MockEnvironment());
        assertThrows(IllegalArgumentException.class, () -> azureCloudStorageService.containerClient());
    }

    @Test
    void checkContainerClient() {
        BlobContainerClient client = azureCloudStorageService.containerClient();
        assertEquals(containerName, client.getBlobContainerName());
    }

    @Test
    void checkDeleteThrowsException() {
        String url = "aa#26#1";
        BadRequestException ex = assertThrows(BadRequestException.class, () -> azureCloudStorageService.delete(url));
        assertEquals(ErrorMessage.PARSING_URL_FAILED + url, ex.getMessage());
    }
}