package greencity.repository;

import greencity.enums.CertificateStatus;
import greencity.entity.order.Certificate;
import greencity.filters.CertificateFilterCriteria;
import greencity.filters.CertificatePage;
import lombok.ToString;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static java.util.Objects.nonNull;

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
        long certificatesCount = getCertificatesCount(certificateFilterCriteria);

        return new PageImpl<>(typedQuery.getResultList(), pageable, certificatesCount);
    }

    private long getCertificatesCount(CertificateFilterCriteria certificateFilterCriteria) {
        var countQuery = criteriaBuilder.createQuery(Long.class);
        var countRoot = countQuery.from(Certificate.class);
        var countPredicate = getPredicate(certificateFilterCriteria, countRoot);
        countQuery.select(criteriaBuilder.count(countRoot)).where(countPredicate);
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
        if (nonNull(certificateFilterCriteria.getCertificateStatus())
            && certificateFilterCriteria.getCertificateStatus().length != 0) {
            CriteriaBuilder.In<CertificateStatus> certificateStatus =
                criteriaBuilder.in(certificateRoot.get("certificateStatus"));
            Arrays.stream(certificateFilterCriteria.getCertificateStatus())
                .forEach(certificateStatus::value);
            predicates.add(certificateStatus);
        }
        if (nonNull(certificateFilterCriteria.getPoints())
            && certificateFilterCriteria.getPoints().length != 0) {
            CriteriaBuilder.In<Integer> points = criteriaBuilder.in(certificateRoot.get("points"));
            Arrays.stream(certificateFilterCriteria.getPoints())
                .forEach(points::value);
            predicates.add(points);
        }
        if (nonNull(certificateFilterCriteria.getExpirationDateFrom())
            && nonNull(certificateFilterCriteria.getExpirationDateTo())) {
            predicates.add(criteriaBuilder.between(certificateRoot.get("expirationDate").as(LocalDateTime.class),
                LocalDateTime.of(LocalDate.parse(certificateFilterCriteria.getExpirationDateFrom()), LocalTime.MIN),
                LocalDateTime.of(LocalDate.parse(certificateFilterCriteria.getExpirationDateTo()), LocalTime.MAX)));
        }
        if (nonNull(certificateFilterCriteria.getCreationDateFrom())
            && nonNull(certificateFilterCriteria.getCreationDateTo())) {
            predicates.add(criteriaBuilder.between(certificateRoot.get("creationDate").as(LocalDateTime.class),
                LocalDateTime.of(LocalDate.parse(certificateFilterCriteria.getCreationDateFrom()), LocalTime.MIN),
                LocalDateTime.of(LocalDate.parse(certificateFilterCriteria.getCreationDateTo()), LocalTime.MAX)));
        }
        if (nonNull(certificateFilterCriteria.getDateOfUseFrom())
            && nonNull(certificateFilterCriteria.getDateOfUseTo())) {
            predicates.add(criteriaBuilder.between(certificateRoot.get("dateOfUse").as(LocalDateTime.class),
                LocalDateTime.of(LocalDate.parse(certificateFilterCriteria.getDateOfUseFrom()), LocalTime.MIN),
                LocalDateTime.of(LocalDate.parse(certificateFilterCriteria.getDateOfUseTo()), LocalTime.MAX)));
        }
        if (nonNull(certificateFilterCriteria.getSearch())) {
            searchOnCertificates(certificateFilterCriteria, certificateRoot, predicates);
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private void searchOnCertificates(CertificateFilterCriteria certificateFilterCriteria,
        Root<Certificate> certificateRoot,
        List<Predicate> predicates) {
        Predicate predicate = criteriaBuilder.or(
            criteriaBuilder.like((certificateRoot.get("code")),
                "%" + certificateFilterCriteria.getSearch().toUpperCase() + "%"),
            criteriaBuilder.like(criteriaBuilder.upper(certificateRoot.get("certificateStatus").as(String.class)),
                "%" + certificateFilterCriteria.getSearch().toUpperCase() + "%"),
            criteriaBuilder.like((certificateRoot.get("points").as(String.class)),
                "%" + certificateFilterCriteria.getSearch().toUpperCase() + "%"),
            criteriaBuilder.like((certificateRoot.get("order")).get("id").as(String.class),
                "%" + certificateFilterCriteria.getSearch() + "%"),
            criteriaBuilder.like((certificateRoot.get("expirationDate")).as(String.class),
                "%" + certificateFilterCriteria.getSearch().toUpperCase() + "%"),
            criteriaBuilder.like((certificateRoot.get("creationDate")).as(String.class),
                "%" + certificateFilterCriteria.getSearch().toUpperCase() + "%"),
            criteriaBuilder.like((certificateRoot.get("dateOfUse")).as(String.class),
                "%" + certificateFilterCriteria.getSearch().toUpperCase() + "%"));
        predicates.add(predicate);
    }
}
