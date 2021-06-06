package greencity.exceptions;

/**
 * Exception that the user is trying to use the certificate is not activated.
 *
 * @author Volodymyr Hutei
 */
public class CertificateIsNotActivated extends RuntimeException {
    /**
     * Constructor.
     */
    public CertificateIsNotActivated(String message) {
        super(message);
    }
}
