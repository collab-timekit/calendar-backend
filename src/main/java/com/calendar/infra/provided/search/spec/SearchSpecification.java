package com.calendar.infra.provided.search.spec;

import com.calendar.infra.provided.search.model.FilterRequest;
import com.calendar.infra.provided.search.model.SearchFilter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@RequiredArgsConstructor
public class SearchSpecification<T> implements Specification<T> {
    private final SearchFilter searchFilter;

    private final Map<Class<?>, Function<String, ?>> valueConverters = Map.of(
            Integer.class, Integer::valueOf,
            Long.class, Long::valueOf,
            BigDecimal.class, BigDecimal::new,
            LocalDate.class, LocalDate::parse,
            LocalDateTime.class, LocalDateTime::parse,
            Boolean.class, Boolean::parseBoolean,
            String.class, Function.identity(),
            Duration.class, value -> Duration.ofSeconds(Long.parseLong(value)),
            Instant.class, Instant::parse
    );

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Predicate toPredicate(@NonNull Root<T> root, CriteriaQuery<?> query, @NonNull CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (searchFilter.getFilters() == null || searchFilter.getFilters().isEmpty()) {
            return cb.conjunction();
        }

        for (FilterRequest filter : searchFilter.getFilters()) {
            var fieldName = filter.getField();
            Path<?> path = path(root, fieldName);
            var clazz = path.getJavaType();

            switch (filter.getOperator()) {
                case "eq" -> predicates.add(cb.equal(path, getValue(filter.getValue(), clazz)));
                case "like" -> predicates.add(cb.like((Expression<String>) path, "%" + filter.getValue() + "%"));
                case "gt" -> predicates.add(cb.greaterThan(path(root, fieldName), getValue(filter.getValue(), (Class<Comparable>) clazz)));
                case "lt" -> predicates.add(cb.lessThan(path(root, fieldName), getValue(filter.getValue(), (Class<Comparable>) clazz)));
                default -> throw new UnsupportedOperationException("Operator not supported: " + filter.getOperator());
            }
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <V> V getValue(String value, Class<V> javaType) {
        if (Objects.isNull(value)) {
            return null;
        } else if (javaType.isEnum()) {
            return (V) Enum.valueOf((Class<Enum>)javaType, value);
        } else if (valueConverters.containsKey(javaType)) {
            return (V) valueConverters.get(javaType).apply(value);
        }

        throw new UnsupportedOperationException("Unsupported java type: " + javaType);
    }

    @SuppressWarnings({"unchecked"})
    public static <V> Path<V> path(Root<?> root, String fieldName) {
        Path<?> currentPath = root;

        for (String field : fieldName.split("\\.")) {
            currentPath = currentPath.get(field);
        }

        return (Path<V>) currentPath;
    }
}