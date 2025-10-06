package greencity.service.ubs.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
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
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
class CertificateCalculatorServiceImplTest {
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private CertificateRepository certificateRepository;
    @InjectMocks
    private CertificateCalculatorServiceImpl service;

    @Test
    void getCertificateSumToPayInCoins_ShouldReduceCorrectly() {
        Order order = new Order();
        order.setPointsToUse(2);

        Certificate cert = new Certificate();
        cert.setPoints(3);
        order.setCertificates(Set.of(cert));

        CertificateDto dto = new CertificateDto();
        dto.setPoints(3);

        when(modelMapper.map(cert, CertificateDto.class)).thenReturn(dto);

        long result = service.getCertificateSumToPayInCoins(order, 1000L);

        assertEquals(500L, result);
    }

    @Test
    void applyCertificatesForClientOrder_ShouldReturnSame_WhenNoCertificates() {
        Order order = new Order();
        OrderWayForPayClientDto dto = new OrderWayForPayClientDto();
        dto.setCertificates(null);

        long result = service.applyCertificatesForClientOrder(dto, order, 100L);

        assertEquals(100L, result);
    }

    @Test
    void applyCertificatesForClientOrder_ShouldThrow_WhenNotFound() {
        Order order = new Order();
        OrderWayForPayClientDto dto = new OrderWayForPayClientDto();
        dto.setCertificates(Set.of("ABC"));

        when(certificateRepository.findByCodeInAndCertificateStatus(anyList(), any()))
            .thenReturn(Set.of());

        assertThrows(NotFoundException.class,
            () -> service.applyCertificatesForClientOrder(dto, order, 100L));
    }

    @Test
    void applyCertificatesForClientOrder_ShouldThrow_WhenSomeInvalid() {
        Order order = new Order();
        OrderWayForPayClientDto dto = new OrderWayForPayClientDto();
        dto.setCertificates(Set.of("A", "B"));

        Certificate cert = new Certificate();
        cert.setCode("A");
        cert.setPoints(1);
        cert.setCertificateStatus(CertificateStatus.ACTIVE);

        when(certificateRepository.findByCodeInAndCertificateStatus(anyList(), any()))
            .thenReturn(Set.of(cert));

        assertThrows(NotFoundException.class,
            () -> service.applyCertificatesForClientOrder(dto, order, 100L));
    }

    @Test
    void applyCertificatesForClientOrder_ShouldReduce_WhenValid() {
        Order order = new Order();
        OrderWayForPayClientDto dto = new OrderWayForPayClientDto();
        dto.setCertificates(Set.of("A"));

        Certificate cert = new Certificate();
        cert.setCode("A");
        cert.setPoints(2);
        cert.setCertificateStatus(CertificateStatus.ACTIVE);

        when(certificateRepository.findByCodeInAndCertificateStatus(anyList(), any()))
            .thenReturn(Set.of(cert));

        long result = service.applyCertificatesForClientOrder(dto, order, 500L);

        assertEquals(300L, result);
        assertEquals(CertificateStatus.USED, cert.getCertificateStatus());
        assertEquals(order, cert.getOrder());
    }

    @Test
    void applyCertificatesToOrder_ShouldThrowNotFoundException_WhenTooManyCertificates() {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setCertificates(Set.of("A", "B", "C", "D"));
        Order order = new Order();

        assertThrows(NotFoundException.class,
            () -> service.applyCertificatesToOrder(dto, new HashSet<>(), order, 100L));
    }

    @Test
    void applyCertificatesToOrder_ShouldThrow_WhenCertificateNew() {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setCertificates(Set.of("A"));
        Order order = new Order();

        Certificate cert = new Certificate();
        cert.setCode("A");
        cert.setCertificateStatus(CertificateStatus.NEW);

        when(certificateRepository.findById("A")).thenReturn(Optional.of(cert));

        assertThrows(CertificateIsNotActivated.class,
            () -> service.applyCertificatesToOrder(dto, new HashSet<>(), order, 100L));
    }

    @Test
    void applyCertificatesToOrder_ShouldReduce_WhenValid() {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setCertificates(Set.of("A"));
        Order order = new Order();
        Set<Certificate> orderCerts = new HashSet<>();

        Certificate cert = new Certificate();
        cert.setCode("A");
        cert.setCertificateStatus(CertificateStatus.ACTIVE);
        cert.setExpirationDate(LocalDate.now().plusDays(1));
        cert.setPoints(1);

        when(certificateRepository.findById("A")).thenReturn(Optional.of(cert));

        long result = service.applyCertificatesToOrder(dto, orderCerts, order, 200L);

        assertEquals(100L, result);
        assertTrue(orderCerts.contains(cert));
        assertEquals(CertificateStatus.USED, cert.getCertificateStatus());
    }

    @Test
    void countCertificatesBonuses_ShouldSumPoints() {
        CertificateDto c1 = new CertificateDto();
        c1.setPoints(2);
        CertificateDto c2 = new CertificateDto();
        c2.setPoints(3);

        int result = service.countCertificatesBonuses(List.of(c1, c2));

        assertEquals(5, result);
    }

    @Test
    void applyCertificatesForClientOrder_ShouldReturnZero_WhenSumLessThanCertificate() {
        Order order = new Order();
        OrderWayForPayClientDto dto = new OrderWayForPayClientDto();
        dto.setCertificates(Set.of("A"));

        Certificate cert = new Certificate();
        cert.setCode("A");
        cert.setPoints(5);
        cert.setCertificateStatus(CertificateStatus.ACTIVE);

        when(certificateRepository.findByCodeInAndCertificateStatus(anyList(), any()))
            .thenReturn(Set.of(cert));

        long result = service.applyCertificatesForClientOrder(dto, order, 400L);

        assertEquals(0L, result);
        assertEquals(CertificateStatus.USED, cert.getCertificateStatus());
        assertEquals(order, cert.getOrder());
        assertEquals(4, cert.getPoints());
    }

    @Test
    void applyCertificatesToOrder_ShouldThrow_WhenCertificateExpired() {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setCertificates(Set.of("A"));
        Order order = new Order();

        Certificate cert = new Certificate();
        cert.setCode("A");
        cert.setCertificateStatus(CertificateStatus.ACTIVE);
        cert.setExpirationDate(LocalDate.now().minusDays(1));

        when(certificateRepository.findById("A")).thenReturn(Optional.of(cert));

        BadRequestException ex = assertThrows(BadRequestException.class,
            () -> service.applyCertificatesToOrder(dto, new HashSet<>(), order, 100L));

        assertTrue(ex.getMessage().contains(cert.getCode()));
        assertTrue(ex.getMessage().contains("expired"));
    }

    @Test
    void applyCertificatesToOrder_ShouldThrow_WhenCertificateUsed() {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setCertificates(Set.of("A"));
        Order order = new Order();

        Certificate cert = new Certificate();
        cert.setCode("A");
        cert.setCertificateStatus(CertificateStatus.USED);
        cert.setExpirationDate(LocalDate.now().plusDays(1));

        when(certificateRepository.findById("A")).thenReturn(Optional.of(cert));

        BadRequestException ex = assertThrows(BadRequestException.class,
            () -> service.applyCertificatesToOrder(dto, new HashSet<>(), order, 100L));

        assertTrue(ex.getMessage().contains(cert.getCode()));
        assertTrue(ex.getMessage().contains("used"));
    }

    @Test
    void applyCertificatesToOrder_ShouldReturnSame_WhenSumIsZero() {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setCertificates(Set.of("A"));
        Order order = new Order();

        long result = service.applyCertificatesToOrder(dto, new HashSet<>(), order, 0L);

        assertEquals(0L, result);
    }

    @Test
    void applyCertificatesToOrder_ShouldReturnSame_WhenCertificatesNull() {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setCertificates(null);
        Order order = new Order();

        long result = service.applyCertificatesToOrder(dto, new HashSet<>(), order, 100L);

        assertEquals(100L, result);
    }

    @Test
    void applyCertificatesToOrder_ShouldThrowBadRequestException_WhenTooManyCertificates() {
        OrderResponseDto dto = new OrderResponseDto();
        Set<String> certs = new HashSet<>();
        for (int i = 0; i < AppConstant.MAX_CERTIFICATES_PER_ORDER + 1; i++) {
            certs.add("CERT" + i);
        }
        dto.setCertificates(certs);
        Order order = new Order();

        BadRequestException ex = assertThrows(BadRequestException.class,
            () -> service.applyCertificatesToOrder(dto, new HashSet<>(), order, 100L));

        assertTrue(ex.getMessage().contains(ErrorMessage.TOO_MANY_CERTIFICATES));
    }
}