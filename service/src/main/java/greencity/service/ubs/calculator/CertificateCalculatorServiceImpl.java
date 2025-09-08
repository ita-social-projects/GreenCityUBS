package greencity.service.ubs.calculator;

import static greencity.constant.ErrorMessage.CERTIFICATE_EXPIRED;
import static greencity.constant.ErrorMessage.CERTIFICATE_IS_NOT_ACTIVATED;
import static greencity.constant.ErrorMessage.CERTIFICATE_IS_USED;
import static greencity.constant.ErrorMessage.CERTIFICATE_NOT_FOUND;
import static greencity.constant.ErrorMessage.CERTIFICATE_NOT_FOUND_BY_CODE;
import static greencity.constant.ErrorMessage.SOME_CERTIFICATES_ARE_INVALID;
import static greencity.constant.ErrorMessage.TOO_MANY_CERTIFICATES;
import static java.util.stream.Collectors.joining;
import greencity.constant.AppConstant;
import greencity.dto.certificate.CertificateDto;
import greencity.dto.order.OrderResponseDto;
import greencity.dto.order.OrderWayForPayClientDto;
import greencity.entity.order.Certificate;
import greencity.entity.order.Order;
import greencity.enums.CertificateStatus;
import greencity.exceptions.BadRequestException;
import greencity.exceptions.NotFoundException;
import greencity.exceptions.certificate.CertificateIsNotActivated;
import greencity.repository.CertificateRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CertificateCalculatorServiceImpl implements CertificateCalculatorService {
    private final ModelMapper modelMapper;
    private final CertificateRepository certificateRepository;

    @Override
    public long getCertificateSumToPayInCoins(Order order, long sumToPayInCoins) {
        List<CertificateDto> certificateDtos = order.getCertificates().stream()
            .map(certificate -> modelMapper.map(certificate, CertificateDto.class))
            .toList();

        sumToPayInCoins = sumToPayInCoins - (long) AppConstant.CURRENCY_CONVERSION_RATE * (order.getPointsToUse()
            + countCertificatesBonuses(certificateDtos));
        return sumToPayInCoins;
    }

    @Override
    @Transactional
    public long applyCertificatesForClientOrder(OrderWayForPayClientDto dto,
                                                Order order,
                                                long sumToPayInCoins) {
        if (sumToPayInCoins == 0 || dto.getCertificates() == null) {
            return sumToPayInCoins;
        }

        Set<Certificate> certificates = certificateRepository.findByCodeInAndCertificateStatus(
                new ArrayList<>(dto.getCertificates()), CertificateStatus.ACTIVE);

        if (certificates.isEmpty()) {
            throw new NotFoundException(CERTIFICATE_NOT_FOUND);
        }

        checkValidationCertificates(certificates, dto);

        for (Certificate certificate : certificates) {
            sumToPayInCoins = applyCertificate(order, sumToPayInCoins, certificate);
        }
        return sumToPayInCoins;
    }

    @Override
    @Transactional
    public long applyCertificatesToOrder(OrderResponseDto dto,
                                         Set<Certificate> orderCertificates,
                                         Order order,
                                         long sumToPayInCoins) {
        if (sumToPayInCoins == 0 || dto.getCertificates() == null) {
            return sumToPayInCoins;
        }
        if (dto.getCertificates().size() > AppConstant.MAX_CERTIFICATES_PER_ORDER) {
            throw new BadRequestException(TOO_MANY_CERTIFICATES);
        }
        for (String temp : dto.getCertificates()) {
            Certificate certificate = certificateRepository.findById(temp)
                .orElseThrow(() -> new NotFoundException(CERTIFICATE_NOT_FOUND_BY_CODE + temp));

            validateCertificate(certificate);

            sumToPayInCoins = applyCertificate(order, sumToPayInCoins, certificate);

            orderCertificates.add(certificate);
        }
        return sumToPayInCoins;
    }

    @Override
    public Integer countCertificatesBonuses(List<CertificateDto> certificateDtos) {
        return certificateDtos.stream()
            .map(CertificateDto::getPoints)
            .reduce(0, Integer::sum);
    }

    private void checkValidationCertificates(Set<Certificate> certificates, OrderWayForPayClientDto dto) {
        if (certificates.size() != dto.getCertificates().size()) {
            String validCertification = certificates.stream()
                .map(Certificate::getCode)
                .collect(joining(", "));
            throw new NotFoundException(SOME_CERTIFICATES_ARE_INVALID + validCertification);
        }
    }

    private void validateCertificate(Certificate certificate) {
        if (certificate.getCertificateStatus() == CertificateStatus.NEW) {
            throw new CertificateIsNotActivated(CERTIFICATE_IS_NOT_ACTIVATED + certificate.getCode());
        } else if (certificate.getCertificateStatus() == CertificateStatus.USED) {
            throw new BadRequestException(CERTIFICATE_IS_USED + certificate.getCode());
        } else {
            if (LocalDate.now().isAfter(certificate.getExpirationDate())) {
                throw new BadRequestException(CERTIFICATE_EXPIRED + certificate.getCode());
            }
        }
    }


    private long applyCertificate(Order order,
                                  long sumToPayInCoins,
                                  Certificate certificate) {
        certificate.setOrder(order);
        certificate.setCertificateStatus(CertificateStatus.USED);
        certificate.setDateOfUse(LocalDate.now());

        sumToPayInCoins -= certificate.getPoints() * AppConstant.CURRENCY_CONVERSION_RATE;

        if (sumToPayInCoins <= 0) {
            adjustCertificateBalance(sumToPayInCoins, certificate);
            sumToPayInCoins = 0L;
        }
        return sumToPayInCoins;
    }

    private static void adjustCertificateBalance(long sumToPayInCoins, Certificate certificate) {
        certificate.setCertificateStatus(CertificateStatus.USED);
        certificate.setPoints(certificate.getPoints()
            + BigDecimal.valueOf(sumToPayInCoins)
            .movePointLeft(AppConstant.TWO_DECIMALS_AFTER_POINT_IN_CURRENCY)
            .setScale(0, RoundingMode.UP).intValue());
    }
}
