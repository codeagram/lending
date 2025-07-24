package com.lending.backend.crud.util;

import com.lending.backend.common.audit.BaseEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SpecificationBuilder<T extends BaseEntity> {

    public Specification<T> build(Map<String, String> filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filters != null) {
                filters.forEach((key, value) -> {
                    if (value != null && !value.trim().isEmpty()) {
                        addPredicate(predicates, root, query, criteriaBuilder, key, value);
                    }
                });
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void addPredicate(List<Predicate> predicates,
            jakarta.persistence.criteria.Root<T> root,
            jakarta.persistence.criteria.CriteriaQuery<?> query,
            jakarta.persistence.criteria.CriteriaBuilder cb,
            String key, String value) {

        // Handle different filter operations
        if (key.endsWith("_like")) {
            String field = key.substring(0, key.length() - 5);
            predicates.add(cb.like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%"));
        } else if (key.endsWith("_eq")) {
            String field = key.substring(0, key.length() - 3);
            predicates.add(cb.equal(root.get(field), value));
        } else if (key.endsWith("_ne")) {
            String field = key.substring(0, key.length() - 3);
            predicates.add(cb.notEqual(root.get(field), value));
        } else if (key.endsWith("_gt")) {
            String field = key.substring(0, key.length() - 3);
            predicates.add(cb.greaterThan(root.get(field), parseValue(value)));
        } else if (key.endsWith("_gte")) {
            String field = key.substring(0, key.length() - 4);
            predicates.add(cb.greaterThanOrEqualTo(root.get(field), parseValue(value)));
        } else if (key.endsWith("_lt")) {
            String field = key.substring(0, key.length() - 3);
            predicates.add(cb.lessThan(root.get(field), parseValue(value)));
        } else if (key.endsWith("_lte")) {
            String field = key.substring(0, key.length() - 4);
            predicates.add(cb.lessThanOrEqualTo(root.get(field), parseValue(value)));
        } else if (key.endsWith("_in")) {
            String field = key.substring(0, key.length() - 3);
            String[] values = value.split(",");
            predicates.add(root.get(field).in((Object[]) values));
        } else if (key.endsWith("_between")) {
            String field = key.substring(0, key.length() - 8);
            String[] values = value.split(",");
            if (values.length == 2) {
                predicates.add(cb.between(root.get(field),
                        parseValue(values[0]), parseValue(values[1])));
            }
        } else {
            // Default to like search for string fields
            try {
                if (root.get(key).getJavaType() == String.class) {
                    predicates.add(cb.like(cb.lower(root.get(key)), "%" + value.toLowerCase() + "%"));
                } else {
                    predicates.add(cb.equal(root.get(key), parseValue(value)));
                }
            } catch (Exception e) {
                // Skip invalid fields
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <V extends Comparable<V>> V parseValue(String value) {
        // Try to parse as different types
        try {
            // Try as LocalDateTime
            return (V) LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e1) {
            try {
                // Try as Long
                return (V) Long.valueOf(value);
            } catch (Exception e2) {
                try {
                    // Try as Double
                    return (V) Double.valueOf(value);
                } catch (Exception e3) {
                    // Return as String
                    return (V) value;
                }
            }
        }
    }
}