package com.folio.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class SortHelper {

    private SortHelper() {}

    public static <T> List<T> sort(List<T> items, String sortField, String sortDir,
                                   Map<String, Comparator<T>> fieldMap) {
        if (sortField == null || sortField.isBlank()) return items;
        Comparator<T> cmp = fieldMap.get(sortField);
        if (cmp == null) return items;
        if ("desc".equalsIgnoreCase(sortDir)) cmp = cmp.reversed();
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