package greencity.service;

import greencity.dto.CertificateDto;
import greencity.dto.OrderResponseDto;
import greencity.dto.PersonalDataDto;
import greencity.dto.UserPointsAndAllBagsDto;

import java.util.List;

public interface UBS_service {

    UserPointsAndAllBagsDto getFirstPageData(Long userId);

    List<PersonalDataDto> getSecondPageData(Long userId);

    CertificateDto checkCertificate(String code);

    void processOrder(OrderResponseDto dto, Long userId);
}
