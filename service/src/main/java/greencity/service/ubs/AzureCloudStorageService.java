package greencity.service.ubs;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import greencity.constant.ErrorMessage;
import greencity.exceptions.BlobNotFoundException;
import greencity.exceptions.FileNotSavedException;
import greencity.exceptions.ImageUrlParseException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.PropertyResolver;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
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
            client.upload(multipartFile.getInputStream(), multipartFile.getSize());
        } catch (IOException e) {
            throw new FileNotSavedException(ErrorMessage.FILE_NOT_SAVED);
        }
        return client.getBlobUrl();
    }

    @Override
    public void delete(String url) {
        String fileName;
        try {
            fileName = Paths.get(new URI(url).getPath()).getFileName().toString();
        } catch (URISyntaxException e) {
            throw new ImageUrlParseException(ErrorMessage.PARSING_URL_FAILED + url);
        }
        BlobClient client = containerClient().getBlobClient(fileName);
        if (client.exists()) {
            client.delete();
        } else {
            throw new BlobNotFoundException(ErrorMessage.BLOB_DOES_NOT_EXIST);
        }
    }

    private BlobContainerClient containerClient() {
        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
            .connectionString(connectionString).buildClient();
        return serviceClient.getBlobContainerClient(containerName);
    }
}
