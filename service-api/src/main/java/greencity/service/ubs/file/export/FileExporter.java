package greencity.service.ubs.file.export;

import java.util.Locale;

public interface FileExporter<T> {
    /**
     * Exports requested object to file.
     *
     * @param objectToWrite an object to export.
     * @return byte representation of file with requested data
     *
     */
    byte[] export(T objectToWrite, Locale locale);
}