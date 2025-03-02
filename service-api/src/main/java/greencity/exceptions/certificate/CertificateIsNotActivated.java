package greencity.exceptions.certificate;

import lombok.experimental.StandardException;

/**
 * Exception that the user is trying to use the certificate is not activated.
 *
 * @author Volodymyr Hutei
 */
@StandardException
public class CertificateIsNotActivated extends RuntimeException {
}
