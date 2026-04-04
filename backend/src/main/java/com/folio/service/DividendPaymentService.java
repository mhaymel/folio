package com.folio.service;

import com.folio.domain.Isin;
import com.folio.dto.DividendPaymentDto;
import com.folio.dto.DividendPaymentFilter;
import com.folio.model.DividendPaymentEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DividendPaymentService {

    private final EntityManager em;

    public DividendPaymentService(EntityManager em) {
        this.em = em;
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<DividendPaymentDto> getDividendPayments(DividendPaymentFilter filter) {
        StringBuilder jpql = new StringBuilder(
            "SELECT dp FROM DividendPaymentEntity dp JOIN FETCH dp.isin JOIN FETCH dp.depot JOIN FETCH dp.currency WHERE 1=1");
        Map<String, Object> params = new HashMap<>();

        if (filter.isin() != null && !filter.isin().isBlank()) {
            jpql.append(" AND LOWER(dp.isin.isin) LIKE :isin");
            params.put("isin", "%" + filter.isin().toLowerCase() + "%");
        }
        if (filter.depot() != null && !filter.depot().isBlank()) {
            List<String> depotValues = Arrays.stream(filter.depot().split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toList();
            if (depotValues.size() == 1) {
                jpql.append(" AND dp.depot.name = :depot");
                params.put("depot", depotValues.get(0));
            } else if (!depotValues.isEmpty()) {
                jpql.append(" AND dp.depot.name IN :depots");
                params.put("depots", depotValues);
            }
        }
        if (filter.fromDate() != null) {
            jpql.append(" AND dp.timestamp >= :fromDate");
            params.put("fromDate", filter.fromDate().atStartOfDay());
        }
        if (filter.toDate() != null) {
            jpql.append(" AND dp.timestamp <= :toDate");
            params.put("toDate", filter.toDate().atTime(23, 59, 59));
        }
        jpql.append(" ORDER BY dp.timestamp DESC");

        var query = em.createQuery(jpql.toString(), DividendPaymentEntity.class);
        params.forEach(query::setParameter);
        List<DividendPaymentEntity> payments = query.getResultList();

        List<DividendPaymentDto> result = payments.stream().map(dp -> DividendPaymentDto.builder()
            .id(dp.getId())
            .timestamp(dp.getTimestamp())
            .isin(new Isin(dp.getIsin().getIsin()))
            .name(getFirstName(dp.getIsin().getId()))
            .depot(dp.getDepot().getName())
            .value(dp.getValue())
            .build())
            .toList();

        if (filter.name() != null && !filter.name().isBlank()) {
            String lower = filter.name().toLowerCase();
            Set<Isin> matchingIsins = result.stream()
                .filter(d -> d.getName() != null && d.getName().toLowerCase().contains(lower))
                .map(DividendPaymentDto::getIsin)
                .collect(Collectors.toSet());
            result = result.stream()
                .filter(d -> matchingIsins.contains(d.getIsin()))
                .toList();
        }

        return result;
    }

    private String getFirstName(Integer isinId) {
        try {
            return (String) em.createQuery("SELECT n.name FROM IsinNameEntity n WHERE n.isin.id = :id ORDER BY n.id ASC")
                .setParameter("id", isinId).setMaxResults(1).getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
}