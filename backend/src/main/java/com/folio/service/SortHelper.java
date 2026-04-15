package com.folio.service;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Provides reusable sorting for in-memory lists.
 * Comparator factory methods remain static because they are pure functions
 * used in static field initialisers of controllers.
 */
@Component
public final class SortHelper {

    public <T> List<T> sort(List<T> items, SortRequest sort, Map<String, Comparator<T>> fieldMap) {
        if (sort.sortField() == null || sort.sortField().isBlank()) return items;
        Comparator<T> cmp = fieldMap.get(sort.sortField());
        if (cmp == null) return items;
        if ("desc".equalsIgnoreCase(sort.sortDir())) cmp = cmp.reversed();
        return items.stream().sorted(cmp).toList();
    }

    public static <T> Comparator<T> text(Function<T, String> accessor) {
        return Comparator.comparing(accessor, Comparator.nullsLast(String::compareToIgnoreCase));
    }

    public static <T> Comparator<T> number(Function<T, Double> accessor) {
        return Comparator.comparing(accessor, Comparator.nullsLast(Double::compareTo));
    }

    public static <T, U extends Comparable<U>> Comparator<T> comparing(Function<T, U> accessor) {
        return Comparator.comparing(accessor, Comparator.nullsLast(Comparator.naturalOrder()));
    }
}