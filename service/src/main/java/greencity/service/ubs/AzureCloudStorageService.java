package greencity.service.ubs;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.specialized.BlockBlobClient;
import greencity.constant.ErrorMessage;
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
        BlockBlobClient blockBlobClient = containerClient()
            .getBlobClient(blob + multipartFile.getOriginalFilename()).getBlockBlobClient();
        try {
            blockBlobClient.upload(multipartFile.getInputStream(), multipartFile.getSize());
        } catch (IOException e) {
            throw new FileNotSavedException(ErrorMessage.FILE_NOT_SAVED);
        }
        return blockBlobClient.getBlobUrl();
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
        }
    }

    private BlobServiceClient blobServiceClient(){

        return new BlobServiceClientBuilder()
                .endpoint("https://csb10032000a548f571.blob.core.windows.net/?sv=2020-08-04&ss=bfqt&srt=sco&sp=rwdlacuptfx&se=2021-08-25T21:29:37Z&st=2021-08-25T13:29:37Z&spr=https&sig=rVOqWEvdSvpOzY%2BRAr6FmBi%2BW5vLDFbOP1HccF5A%2BX8%3D")
                /*.sasToken("?sv=2020-08-04&ss=bfqt&srt=sco&sp=rwdlacuptfx&se=2021-08-25T19:20:48Z&st=2021-08-25T11:20:48Z&spr=" +
                        "https&sig=V1e21IwYYaAFtzGF2epsjko7mDfjtD%2BKuzRp6DpeG0E%3D")*/
                .buildClient();
    }

    private BlobContainerClient containerClient() {
        return blobServiceClient().getBlobContainerClient(containerName);
    }


    /*private BlobClient blobClient(){
        return containerClient().getBlobClient();
    }
*/


}
