package greencity.repository;

import greencity.entity.order.BigOrderTableViews;
import greencity.filters.DateFilter;
import greencity.filters.OrderPage;
import greencity.filters.OrderSearchCriteria;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.lang.reflect.Field;
import java.time.LocalDate;

import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.Objects.nonNull;

@Repository
public class BigOrderTableRepository {
    private final EntityManager entityManager;
    private final CriteriaBuilder criteriaBuilder;

    /**
     * Constructor to initialize EntityManager and CriteriaBuilder.
     */
    public BigOrderTableRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
    }

    /**
     * Method returns Page of BigOrderTableViews with orders and additional info
     * related to order.
     * 
     * @return Page
     * @author Kuzbyt Maksym
     */
    public Page<BigOrderTableViews> findAll(OrderPage orderPage, OrderSearchCriteria searchCriteria) {
        var criteriaQuery = criteriaBuilder.createQuery(BigOrderTableViews.class);
        var orderRoot = criteriaQuery.from(BigOrderTableViews.class);

        var predicate = getPredicate(searchCriteria, orderRoot);

        sort(orderPage, criteriaQuery, orderRoot);
        criteriaQuery.select(orderRoot).where(predicate);

        var typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult(orderPage.getPageNumber() * orderPage.getPageSize());
        typedQuery.setMaxResults(orderPage.getPageSize());

        var resultList = typedQuery.getResultList();

        var sort = Sort.by(orderPage.getSortDirection(), orderPage.getSortBy());
        var pageable = PageRequest.of(orderPage.getPageNumber(), orderPage.getPageSize(), sort);
        var ordersCount = getOrdersCount(predicate);

        return new PageImpl<>(resultList, pageable, ordersCount);
    }

    private Predicate getPredicate(OrderSearchCriteria sc, Root<BigOrderTableViews> orderRoot) {
        var filtersEnum = new HashMap<String, Enum<?>[]>();
        filtersEnum.put("orderStatus", sc.getOrderStatus());
        filtersEnum.put("orderPaymentStatus", sc.getOrderPaymentStatus());

        var filtersString = new HashMap<String, String[]>();
        filtersString.put("region", sc.getRegion());
        filtersString.put("settlement", sc.getCity());
        filtersString.put("district", sc.getDistricts());

        var dateFilter = new HashMap<String, DateFilter>();
        dateFilter.put("orderDate", sc.getOrderDate());
        dateFilter.put("dateOfExport", sc.getDeliveryDate());
        dateFilter.put("paymentDate", sc.getPaymentDate());

        var filtersLong = new HashMap<String, Long[]>();
        filtersLong.put("receivingStationId", sc.getReceivingStation());
        filtersLong.put("responsibleCallerId", sc.getResponsibleCallerId());
        filtersLong.put("responsibleLogicManId", sc.getResponsibleLogicManId());
        filtersLong.put("responsibleNavigatorId", sc.getResponsibleNavigatorId());
        filtersLong.put("responsibleDriverId", sc.getResponsibleDriverId());

        var predicates = new ArrayList<Predicate>();

        filtersEnum.entrySet().stream()
            .filter(e -> e.getValue() != null)
            .map(e -> filter(e.getValue(), orderRoot, e.getKey()))
            .forEach(predicates::add);

        filtersString.entrySet().stream()
            .filter(e -> e.getValue() != null)
            .map(e -> filter(e.getValue(), orderRoot, e.getKey()))
            .forEach(predicates::add);

        dateFilter.entrySet().stream()
            .filter(e -> e.getValue() != null)
            .map(e -> filter(e.getValue(), orderRoot, e.getKey()))
            .forEach(predicates::add);

        filtersLong.entrySet().stream()
            .filter(e -> e.getValue() != null)
            .map(e -> filter(e.getValue(), orderRoot, e.getKey()))
            .forEach(predicates::add);

        if (nonNull(sc.getSearch())) {
            searchOnBigTable(sc.getSearch(), orderRoot, predicates);
        }

        return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
    }

    private Predicate filter(Enum<?>[] filter, Root<?> root, String columnName) {
        var predicate = criteriaBuilder.in(root.get(columnName).as(String.class));
        Arrays.stream(filter)
            .map(Enum::name)
            .forEach(predicate::value);
        return predicate;
    }

    private Predicate filter(String[] filter, Root<?> root, String columnName) {
        var predicate = criteriaBuilder.in(criteriaBuilder.upper(root.get(columnName)));
        Arrays.stream(filter)
            .map(String::toUpperCase)
            .forEach(predicate::value);
        return predicate;
    }

    private Predicate filter(DateFilter df, Root<?> root, String columnName) {
        var column = root.<LocalDate>get(columnName).as(LocalDate.class);
        var to = df.getTo();
        var from = df.getFrom();
        var format = DateTimeFormatter.ofPattern("yyy-MM-d");

        if (from == null && to != null) {
            return criteriaBuilder.lessThanOrEqualTo(column, LocalDate.parse(to, format));
        }
        if (from != null && to == null) {
            return criteriaBuilder.greaterThanOrEqualTo(column, LocalDate.parse(from, format));
        }
        return criteriaBuilder.between(column, LocalDate.parse(Objects.requireNonNull(from), format),
            LocalDate.parse(Objects.requireNonNull(to), format));
    }

    private Predicate filter(Long[] id, Root<?> root, String nameColumn) {
        var predicate = criteriaBuilder.in(root.<Long>get(nameColumn));
        Arrays.stream(id)
            .forEach(predicate::value);
        return predicate;
    }

    private void searchOnBigTable(String[] searchWords, Root<?> root, List<Predicate> predicates) {
        var fields = BigOrderTableViews.class.getDeclaredFields();
        var listPredicates = new ArrayList<Predicate>();

        for (String searchWord : searchWords) {
            Arrays.stream(fields)
                .map(Field::getName)
                .map(root::get)
                .map(path -> criteriaBuilder.upper(path.as(String.class)))
                .map(expression -> criteriaBuilder.like(expression, "%" + searchWord.toUpperCase() + "%"))
                .forEach(listPredicates::add);
        }
        predicates.add(criteriaBuilder.or(listPredicates.toArray(Predicate[]::new)));
    }

    private void sort(OrderPage orderPage, CriteriaQuery<BigOrderTableViews> cq, Root<BigOrderTableViews> root) {
        if (orderPage.getSortDirection().equals(Sort.Direction.ASC)) {
            cq.orderBy(criteriaBuilder.asc(root.get(orderPage.getSortBy())));
        } else {
            cq.orderBy(criteriaBuilder.desc(root.get(orderPage.getSortBy())));
        }
    }

    private long getOrdersCount(Predicate predicate) {
        var countQuery = criteriaBuilder.createQuery(Long.class);
        var countOrderRoot = countQuery.from(BigOrderTableViews.class);
        countQuery.select(criteriaBuilder.count(countOrderRoot)).where(predicate);
        return entityManager.createQuery(countQuery).getSingleResult();
    }
}
