package com.folio.service;

import com.folio.domain.Isin;
import com.folio.dto.DividendPaymentDto;
import com.folio.dto.DividendPaymentFilter;
import com.folio.dto.DividendPaymentIdentity;
import com.folio.dto.DividendPaymentSource;
import com.folio.model.DividendPaymentEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Service
public class DividendPaymentService {

    private final EntityManager entityManager;

    DividendPaymentService(EntityManager entityManager) {
        this.entityManager = requireNonNull(entityManager);
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<DividendPaymentDto> getDividendPayments(DividendPaymentFilter filter) {
        StringBuilder jpql = new StringBuilder(
            "SELECT dp FROM DividendPaymentEntity dp JOIN FETCH dp.context.isin JOIN FETCH dp.context.depot JOIN FETCH dp.paymentValues.currency WHERE 1=1");
        Map<String, Object> params = new HashMap<>();

        if (filter.isinFragment() != null && !filter.isinFragment().isBlank()) {
            jpql.append(" AND LOWER(dp.context.isin.isin) LIKE :isin");
            params.put("isin", "%" + filter.isinFragment().toLowerCase() + "%");
        }
        if (filter.depotFragment() != null && !filter.depotFragment().isBlank()) {
            List<String> depotValues = Arrays.stream(filter.depotFragment().split(","))
                .map(String::trim).filter(value -> !value.isEmpty()).toList();
            if (depotValues.size() == 1) {
                jpql.append(" AND dp.context.depot.name = :depot");
                params.put("depot", depotValues.get(0));
            } else if (!depotValues.isEmpty()) {
                jpql.append(" AND dp.context.depot.name IN :depots");
                params.put("depots", depotValues);
            }
        }
        if (filter.fromDate() != null) {
            jpql.append(" AND dp.context.timestamp >= :fromDate");
            params.put("fromDate", filter.fromDate().atStartOfDay());
        }
        if (filter.toDate() != null) {
            jpql.append(" AND dp.context.timestamp <= :toDate");
            params.put("toDate", filter.toDate().atTime(23, 59, 59));
        }
        jpql.append(" ORDER BY dp.context.timestamp DESC");

        var query = entityManager.createQuery(jpql.toString(), DividendPaymentEntity.class);
        params.forEach(query::setParameter);
        List<DividendPaymentEntity> payments = query.getResultList();

        List<DividendPaymentDto> result = payments.stream().map(payment -> {
            String formattedTimestamp = payment.getTimestamp().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            return new DividendPaymentDto(
                new DividendPaymentIdentity(payment.getId(), formattedTimestamp, payment.getTimestamp()),
                new DividendPaymentSource(new Isin(payment.getIsin().getIsin()), findFirstName(payment.getIsin().getId()), payment.getDepot().getName()),
                payment.getValue());
        }).toList();

        if (filter.nameFragment() != null && !filter.nameFragment().isBlank()) {
            String lower = filter.nameFragment().toLowerCase();
            Set<Isin> matchingIsins = result.stream()
                .filter(dto -> dto.getName() != null && dto.getName().toLowerCase().contains(lower))
                .map(DividendPaymentDto::getIsin)
                .collect(Collectors.toSet());
            result = result.stream()
                .filter(dto -> matchingIsins.contains(dto.getIsin()))
                .toList();
        }

        return result;
    }

    private String findFirstName(Integer isinId) {
        try {
            return (String) entityManager.createQuery("SELECT n.name FROM IsinNameEntity n WHERE n.isin.id = :id ORDER BY n.id ASC")
                .setParameter("id", isinId).setMaxResults(1).getSingleResult();
        } catch (Exception exception) {
            return null;
        }
    }
}