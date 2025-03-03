package greencity.service.ubs;

import java.util.Locale;

public interface FileExporter<T> {
    byte[] export(T objectToWrite, Locale locale);
}
