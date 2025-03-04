package greencity.exceptions.exporting.pdf;

public class PdfFileExportingException extends RuntimeException {
    public PdfFileExportingException() {
    }

    public PdfFileExportingException(String message) {
        super(message);
    }

    public PdfFileExportingException(String message, Throwable cause) {
        super(message, cause);
    }
}