package greencity.exceptions.files;

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
