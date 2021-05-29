package greencity.exceptions;

/**
 * Exception that the user is trying to use the certificate expired.
 *
 * @author Marian Diakiv
 */
public class CertificateExpiredException extends RuntimeException {
    /**
     * Constructor.
     */
    public CertificateExpiredException(String message) {
        super(message);
    }
}
