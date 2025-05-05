package greencity.service.ubs;

import org.springframework.core.io.Resource;
import java.util.Locale;

public interface PdfExporterService {
    /**
     * Generates a PDF file based on the provided object ID.
     *
     * @param objectId - id of the required object to be exported
     * @param locale   - language of a generated file
     * @param userUuid - user's ID
     * @return Resource representation of a PDF-file
     */
    Resource generatePdfFileByObjectId(long objectId, Locale locale, String userUuid);
}