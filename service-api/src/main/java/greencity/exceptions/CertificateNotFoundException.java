package greencity.exceptions;

/**
 * Exception that user enters unexisting certificate.
 *
 * @author Oleh Bilonizhka
 */
public class CertificateNotFoundException extends RuntimeException {
    /**
     * Constructor.
     */
    public CertificateNotFoundException(String message) {
        super(message);
    }
}
