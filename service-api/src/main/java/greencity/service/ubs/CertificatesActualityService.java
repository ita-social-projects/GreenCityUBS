package greencity.service.ubs;

public interface CertificatesActualityService {
    /**
     * Method checks expiration date for all certificates and changes status to
     * expired in case date has passed.
     *
     * @author Sikhovskiy Rostyslav
     */
    void checkCertificatesForActuality();
}
