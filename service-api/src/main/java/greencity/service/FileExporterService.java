package greencity.service;

import org.springframework.core.io.Resource;

import java.util.Locale;

public interface FileExporterService {
    Resource export(Long objectId, Locale locale, String uuid);
}
