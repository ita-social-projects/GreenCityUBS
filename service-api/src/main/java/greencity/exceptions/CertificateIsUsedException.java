package greencity.exceptions;

/**
 * Exception that the user is trying to use the certificate used.
 *
 * @author Marian Diakiv
 */
public class CertificateIsUsedException extends RuntimeException {
    /**
     * Constructor.
     */
    public CertificateIsUsedException(String message) {
        super(message);
    }
}
