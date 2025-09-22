package greencity.client.config;

import greencity.dto.files.CleanupFilesDto;
import greencity.dto.files.DeleteFileDto;
import greencity.dto.files.UploadFileDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRemoteWebClient {
    private final WebClient webClient;

    /**
     * Method for uploading a file.
     *
     * @param fileDto {@link UploadFileDto} file to save.
     * @return url of the saved file.
     */
    public String uploadFile(UploadFileDto fileDto) {
        return webClient.post()
            .uri("/files")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(multipartInserter(fileDto))
            .retrieve()
            .bodyToMono(String.class)
            .block();
    }

    /**
     * Method for deleting a file.
     *
     * @param fileDto {@link DeleteFileDto} file to delete.
     */
    public void deleteFile(DeleteFileDto fileDto) {
        webClient.method(HttpMethod.DELETE)
            .uri("/files")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(fileDto)
            .retrieve()
            .bodyToMono(Void.class)
            .block();
    }

    public void cleanUp(CleanupFilesDto cleanupFilesDto) {
        webClient.post()
            .uri("/cleanup")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(cleanupFilesDto)
            .retrieve()
            .bodyToMono(Void.class)
            .block();
    }

    private BodyInserters.MultipartInserter multipartInserter(UploadFileDto fileDto) {
        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("file", fileDto.getFile().getResource());
        multipartBodyBuilder.part("owner", fileDto.getOwner());

        return BodyInserters.fromMultipartData(multipartBodyBuilder.build());
    }
}
