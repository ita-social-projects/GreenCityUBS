package greencity.repository;

import greencity.entity.order.BigOrderTableViews;
import greencity.filters.DateFilter;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
public class CustomCriteriaPredicate {
    private final CriteriaBuilder criteriaBuilder;

    /**
     * Constructor to initialize CriteriaBuilder.
     */
    public CustomCriteriaPredicate(EntityManager entityManager) {
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
    }

    Predicate filter(Enum<?>[] filter, Root<?> root, String columnName) {
        var predicate = criteriaBuilder.in(root.get(columnName).as(String.class));
        Arrays.stream(filter)
            .map(Enum::name)
            .forEach(predicate::value);
        return predicate;
    }

    Predicate filter(String[] filter, Root<?> root, String columnName) {
        var predicate = criteriaBuilder.in(criteriaBuilder.upper(root.get(columnName)));
        Arrays.stream(filter)
            .map(String::toUpperCase)
            .forEach(predicate::value);
        return predicate;
    }

    Predicate filter(DateFilter df, Root<?> root, String columnName) {
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

    Predicate filter(Long[] id, Root<?> root, String nameColumn) {
        var predicate = criteriaBuilder.in(root.<Long>get(nameColumn));
        Arrays.stream(id)
            .forEach(predicate::value);
        return predicate;
    }

    Predicate filter(Root<?> root, String nameColumn) {
        return criteriaBuilder.isNull(root.<Long>get(nameColumn));
    }

    Predicate filter(List<Long> ids, Root<?> root, String nameColumn) {
        List<Predicate> predicateList = new ArrayList<>();
        for (Long id : ids) {
            predicateList.add(criteriaBuilder.equal(root.<Long>get(nameColumn), id));
        }
        return criteriaBuilder.or(predicateList.toArray(new Predicate[0]));
    }

    Predicate search(String[] searchWords, Root<?> root) {
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
        return criteriaBuilder.or(listPredicates.toArray(Predicate[]::new));
    }
}
