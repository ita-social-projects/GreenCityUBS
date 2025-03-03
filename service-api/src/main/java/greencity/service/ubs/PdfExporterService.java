package greencity.service.ubs;

import org.springframework.core.io.Resource;
import java.util.Locale;

public interface PdfExporterService {
    Resource exportById(long orderId, Locale locale, String userUuid);
}
