package greencity.repository;

import greencity.entity.enums.CertificateStatus;
import greencity.entity.order.Certificate;
import greencity.filters.CertificateFilterCriteria;
import greencity.filters.CertificatePage;
import lombok.ToString;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@ToString
@Repository
public class CertificateCriteriaRepo {
    private final EntityManager entityManager;
    private final CriteriaBuilder criteriaBuilder;

    /**
     * Constructor.
     * 
     * @author Sikhovskiy Rostyslav
     */
    public CertificateCriteriaRepo(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
    }

    /**
     * Method for finding certificates with some criteria.
     * 
     * @author Sikhovskiy Rostyslav
     * @return Pages of certificates with filtering and sorting data
     */

    public Page<Certificate> findAllWithFilter(CertificatePage certificatePage,
        CertificateFilterCriteria certificateFilterCriteria) {
        CriteriaQuery<Certificate> criteriaQuery = criteriaBuilder.createQuery(Certificate.class);
        Root<Certificate> certificateRoot = criteriaQuery.from(Certificate.class);
        Predicate predicate = getPredicate(certificateFilterCriteria, certificateRoot);
        criteriaQuery.where(predicate);
        setOrder(certificatePage, criteriaQuery, certificateRoot);

        TypedQuery<Certificate> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult(certificatePage.getPageNumber() * certificatePage.getPageSize());
        typedQuery.setMaxResults(certificatePage.getPageSize());

        Pageable pageable = getPageable(certificatePage);
        long certificatesCount = getCertificatesCount(predicate);

        return new PageImpl<>(typedQuery.getResultList(), pageable, certificatesCount);
    }

    private long getCertificatesCount(Predicate predicate) {
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<Certificate> countRoot = countQuery.from(Certificate.class);
        countQuery.select(criteriaBuilder.count(countRoot)).where(predicate);
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private Pageable getPageable(CertificatePage certificatePage) {
        Sort sort = Sort.by(certificatePage.getSortDirection(), certificatePage.getSortBy());
        return PageRequest.of(certificatePage.getPageNumber(), certificatePage.getPageSize(), sort);
    }

    private void setOrder(CertificatePage certificatePage,
        CriteriaQuery<Certificate> criteriaQuery,
        Root<Certificate> certificateRoot) {
        if (certificatePage.getSortDirection().equals(Sort.Direction.ASC)) {
            criteriaQuery.orderBy(criteriaBuilder.asc(certificateRoot.get(certificatePage.getSortBy())));
        } else {
            criteriaQuery.orderBy(criteriaBuilder.desc(certificateRoot.get(certificatePage.getSortBy())));
        }
    }

    private Predicate getPredicate(CertificateFilterCriteria certificateFilterCriteria,
        Root<Certificate> certificateRoot) {
        List<Predicate> predicates = new ArrayList<>();
        if (Objects.nonNull(certificateFilterCriteria.getCertificateStatus())
            && certificateFilterCriteria.getCertificateStatus().length != 0) {
            CriteriaBuilder.In<CertificateStatus> certificateStatus =
                criteriaBuilder.in(certificateRoot.get("certificateStatus"));
            Arrays.stream(certificateFilterCriteria.getCertificateStatus())
                .forEach(certificateStatus::value);
            predicates.add(certificateStatus);
        }
        if (Objects.nonNull(certificateFilterCriteria.getPoints())
            && certificateFilterCriteria.getPoints().length != 0) {
            CriteriaBuilder.In<Integer> points = criteriaBuilder.in(certificateRoot.get("points"));
            Arrays.stream(certificateFilterCriteria.getPoints())
                .forEach(points::value);
            predicates.add(points);
        }
        if (Objects.nonNull(certificateFilterCriteria.getExpirationDateFrom())
            && Objects.nonNull(certificateFilterCriteria.getExpirationDateTo())) {
            predicates.add(criteriaBuilder.between(certificateRoot.get("expirationDate").as(LocalDateTime.class),
                LocalDateTime.of(LocalDate.parse(certificateFilterCriteria.getExpirationDateFrom()), LocalTime.MIN),
                LocalDateTime.of(LocalDate.parse(certificateFilterCriteria.getExpirationDateTo()), LocalTime.MAX)));
        }
        if (Objects.nonNull(certificateFilterCriteria.getCreationDateFrom())
            && Objects.nonNull(certificateFilterCriteria.getCreationDateTo())) {
            predicates.add(criteriaBuilder.between(certificateRoot.get("creationDate").as(LocalDateTime.class),
                LocalDateTime.of(LocalDate.parse(certificateFilterCriteria.getCreationDateFrom()), LocalTime.MIN),
                LocalDateTime.of(LocalDate.parse(certificateFilterCriteria.getCreationDateTo()), LocalTime.MAX)));
        }
        if (Objects.nonNull(certificateFilterCriteria.getDateOfUseFrom())
            && Objects.nonNull(certificateFilterCriteria.getDateOfUseTo())) {
            predicates.add(criteriaBuilder.between(certificateRoot.get("dateOfUse").as(LocalDateTime.class),
                LocalDateTime.of(LocalDate.parse(certificateFilterCriteria.getDateOfUseFrom()), LocalTime.MIN),
                LocalDateTime.of(LocalDate.parse(certificateFilterCriteria.getDateOfUseTo()), LocalTime.MAX)));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
