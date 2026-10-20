package com.nexigroup.pagopa.cruscotto.sert.repository.freeze;

import com.nexigroup.pagopa.cruscotto.sert.service.dto.PositionPaymentExtraDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.util.PaymentUtil;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class FreezePositionRepository {

    private static final Map<String, String> SORT_COLUMNS = Map.of(
        "nav", "p.nav",
        "paEmittente", "p.pa_emittente",
        "paEmittenteDesc", "p.pa_emittente"
    );

    private final JdbcTemplate jdbcTemplate;
    private final String schema;

    public FreezePositionRepository(
        @Qualifier("freezeJdbcTemplate") JdbcTemplate jdbcTemplate,
        @Value("${spring.freeze-datasource.schema}") String schema
    ) {
        if (!schema.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException("Invalid freeze datasource schema identifier");
        }
        this.jdbcTemplate = jdbcTemplate;
        this.schema = "\"" + schema + "\".";
    }

    public Page<PositionPaymentExtraDTO> search(
        String pa,
        String nav,
        String iuv,
        String token,
        String idCart,
        String info,
        LocalDate dateFrom,
        LocalDate dateTo,
        Pageable pageable
    ) {
        if (token != null && token.trim().isEmpty()) {
            return Page.empty(pageable);
        }
        boolean extraSearch = info != null;
        boolean tokenSearch = iuv != null || token != null || idCart != null || extraSearch;
        StringBuilder fromAndWhere = new StringBuilder(" FROM " + schema + "position p ");
        List<Object> parameters = new ArrayList<>();

        if (tokenSearch) {
            fromAndWhere.append(" JOIN ").append(schema).append("position_tokens pt ON pt.fk_position = p.id ");
        }
        if (extraSearch) {
            fromAndWhere.append(" JOIN ").append(schema).append("extra_info ei ON ei.fk_token = pt.id ");
        }

        String dateColumn = extraSearch ? "ei.date_event" : tokenSearch ? "pt.date_event" : "p.date_event";
        fromAndWhere.append(" WHERE ").append(dateColumn).append(" >= ? AND ").append(dateColumn).append(" <= ?");
        parameters.add(dateFrom);
        parameters.add(dateTo);
        appendFilter(fromAndWhere, parameters, "p.nav", nav);
        appendFilter(fromAndWhere, parameters, "p.pa_emittente", pa);

        if (iuv != null) {
            fromAndWhere.append(" AND (pt.iuv = ? OR pt.creditor_ref_id = ?)");
            parameters.add(iuv);
            parameters.add(iuv);
        } else if (token != null) {
            fromAndWhere.append(" AND pt.token = convert_to(?, 'UTF8')");
            parameters.add(token.trim());
        } else if (idCart != null) {
            fromAndWhere.append(" AND pt.id_carrello = ?");
            parameters.add(idCart);
        } else if (extraSearch) {
            fromAndWhere.append(" AND ei.info_value = ?");
            parameters.add(info);
        }

        String groupedQuery = "SELECT p.nav, p.pa_emittente" +
            (extraSearch ? ", string_agg(DISTINCT ei.info_name, ',')" : "") +
            fromAndWhere +
            " GROUP BY p.nav, p.pa_emittente";
        String sql = groupedQuery + buildOrderBy(pageable) + " LIMIT ? OFFSET ?";
        List<Object> queryParameters = new ArrayList<>(parameters);
        queryParameters.add(pageable.getPageSize());
        queryParameters.add(pageable.getOffset());

        RowMapper<PositionPaymentExtraDTO> rowMapper = (resultSet, rowNumber) -> {
            PositionPaymentExtraDTO.PositionPaymentExtraDTOBuilder<?, ?> builder = PositionPaymentExtraDTO.builder()
                .nav(resultSet.getString(1))
                .paEmittente(resultSet.getString(2))
                .oldPayment(true);
            if (extraSearch) {
                builder.match(PaymentUtil.parseInfoMatch(resultSet.getString(3)));
            }
            return builder.build();
        };

        List<PositionPaymentExtraDTO> results = jdbcTemplate.query(sql, rowMapper, queryParameters.toArray());
        long total = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM (SELECT p.nav, p.pa_emittente" + fromAndWhere +
                " GROUP BY p.nav, p.pa_emittente) matching_positions",
            Long.class,
            parameters.toArray()
        );
        return new PageImpl<>(results, pageable, total);
    }

    private void appendFilter(StringBuilder query, List<Object> parameters, String column, String value) {
        if (value != null) {
            query.append(" AND ").append(column).append(" = ?");
            parameters.add(value);
        }
    }

    private String buildOrderBy(Pageable pageable) {
        List<String> sortExpressions = new ArrayList<>();
        pageable.getSort().forEach(order -> {
            String column = SORT_COLUMNS.get(order.getProperty());
            if (column == null) {
                throw new IllegalArgumentException("Unsupported historical search sort field: " + order.getProperty());
            }
            sortExpressions.add(column + (order.isAscending() ? " ASC" : " DESC"));
        });
        if (sortExpressions.isEmpty()) {
            sortExpressions.add("p.pa_emittente DESC");
        }
        sortExpressions.add("p.nav ASC");
        sortExpressions.add("p.pa_emittente ASC");
        return " ORDER BY " + String.join(", ", sortExpressions);
    }
}
