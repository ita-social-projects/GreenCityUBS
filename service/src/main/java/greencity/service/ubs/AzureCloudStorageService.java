package greencity.service.ubs;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import greencity.constant.ErrorMessage;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.image.FileNotSavedException;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.PropertyResolver;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Data
public class AzureCloudStorageService implements FileService {
    private final String connectionString;
    private final String containerName;

    /**
     * Constructor with parameters.
     */
    @Autowired
    public AzureCloudStorageService(@Autowired PropertyResolver propertyResolver) {
        this.connectionString = propertyResolver.getProperty("azure.connection.string");
        this.containerName = propertyResolver.getProperty("azure.container.name");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String upload(MultipartFile multipartFile) {
        final String blob = UUID.randomUUID().toString();
        BlobClient client = containerClient()
            .getBlobClient(blob + multipartFile.getOriginalFilename());
        try {
            client.upload(new BufferedInputStream(multipartFile.getInputStream()), multipartFile.getSize(), true);
        } catch (IOException e) {
            throw new FileNotSavedException(ErrorMessage.FILE_NOT_SAVED);
        }
        return client.getBlobUrl();
    }

    /**
     * Uploads a file to Azure Blob Storage from an InputStream.
     * This method is intended for uploading files obtained, for example, from a URL (as in the case of Telegram).
     *
     *  @param inputStream The file data stream.
     *  @param originalFileName The desired file name with an extension (for example, "image.jpg").
     *   You can get it from Telegram's file_path.
     *  @param fileSize The size of the file in bytes. You can get it using Telegram's PhotoSize.getFileSize() .
     *  @return The URL of the uploaded file to Azure Blob Storage.
     *  @throws FileNotSavedException if the file could not be saved.
     */
    @Override
    public String upload(InputStream inputStream, String originalFileName, long fileSize) {
        final String blobName = UUID.randomUUID() + "_" + originalFileName;

        BlobClient client = containerClient().getBlobClient(blobName);

        try {
            client.upload(new BufferedInputStream(inputStream), fileSize, true);
        } catch (Exception e) {
            throw new FileNotSavedException(ErrorMessage.FILE_NOT_SAVED, e);
        }
        return client.getBlobUrl();
    }

    @Override
    public void delete(String url) {
        String fileName;
        try {
            fileName = Paths.get(new URI(url).getPath()).getFileName().toString();
        } catch (URISyntaxException e) {
            throw new BadRequestException(ErrorMessage.PARSING_URL_FAILED + url);
        }
        BlobClient client = containerClient().getBlobClient(fileName);
        if (client.exists() != null && client.exists()) {
            client.delete();
        }
    }

    private BlobContainerClient containerClient() {
        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
            .connectionString(connectionString).buildClient();
        return serviceClient.getBlobContainerClient(containerName);
    }
}
