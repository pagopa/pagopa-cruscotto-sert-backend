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
                .fromHistory(true);
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

    public Page<Object[]> findPositionDetailRows(String nav, String paEmittente, Pageable pageable) {
        String fromAndWhere = " FROM " + schema + "position p " +
            "LEFT JOIN " + schema + "position_tokens pt ON pt.fk_position = p.id " +
            "LEFT JOIN " + schema + "anag_psp apsp ON apsp.id = pt.psp " +
            "LEFT JOIN " + schema + "anag_intermediario_pa aipa ON aipa.id = pt.intermediario_pa " +
            "LEFT JOIN " + schema + "anag_intermediario_psp aipsp ON aipsp.id = pt.intermediario_psp " +
            "LEFT JOIN " + schema + "anag_stazione ast ON ast.id = pt.stazione " +
            "LEFT JOIN " + schema + "anag_canale ac ON ac.id = pt.canale " +
            "LEFT JOIN " + schema + "anag_pa_emittente ape ON ape.codice = p.pa_emittente " +
            "WHERE p.nav = ? AND p.pa_emittente = ?";
        String sql = "SELECT p.nav, ape.description, p.last_event, pt.iuv, pt.creditor_ref_id, " +
            "encode(pt.token, 'hex'), pt.date_event, pt.payment_date, pt.outcome, pt.amount, pt.fee, " +
            "apsp.codice, aipa.codice, aipsp.codice, ast.codice, ac.codice, pt.touchpoint, " +
            "pt.payment_method, pt.id_carrello, apsp.description, aipa.description, aipsp.description" +
            fromAndWhere + buildOrderBy(pageable,             Map.of(
                "tokenDateEvent", "pt.date_event",
                "psp", "apsp.codice",
                "ptPsp", "aipsp.codice",
                "amount", "pt.amount",
                "paymentMethod", "pt.payment_method",
                "paymentDate", "pt.payment_date",
                "touchpoint", "pt.touchpoint",
                "ptPaDesc", "aipa.description",
                "tokenHex", "pt.token"
            ), "p.last_event DESC") + " LIMIT ? OFFSET ?";
        return queryPage(
            sql,
            "SELECT COUNT(pt.id)" + fromAndWhere,
            List.of(nav, paEmittente),
            pageable
        );
    }

    public List<Object[]> findTokenDetailRows(String token) {
        String sql = "SELECT p.nav, p.pa_emittente, p.last_event, pt.iuv, pt.creditor_ref_id, " +
            "encode(pt.token, 'hex'), pt.date_event, pt.payment_date, pt.outcome, pt.amount, pt.fee, " +
            "apsp.codice, aipa.codice, aipsp.codice, ast.codice, ac.codice, pt.touchpoint, " +
            "pt.payment_method, pt.id_carrello, apsp.description, aipa.description, aipsp.description " +
            "FROM " + schema + "position_tokens pt " +
            "JOIN " + schema + "position p ON p.id = pt.fk_position " +
            "LEFT JOIN " + schema + "anag_psp apsp ON apsp.id = pt.psp " +
            "LEFT JOIN " + schema + "anag_intermediario_pa aipa ON aipa.id = pt.intermediario_pa " +
            "LEFT JOIN " + schema + "anag_intermediario_psp aipsp ON aipsp.id = pt.intermediario_psp " +
            "LEFT JOIN " + schema + "anag_stazione ast ON ast.id = pt.stazione " +
            "LEFT JOIN " + schema + "anag_canale ac ON ac.id = pt.canale " +
            "WHERE pt.token = convert_to(?, 'UTF8') " +
            "ORDER BY pt.payment_date DESC, pt.date_event DESC, pt.id DESC";
        return queryRows(sql, List.of(token));
    }

    public long countOkTokensByNavAndPa(String nav, String paEmittente) {
        Long count = jdbcTemplate.queryForObject(
            "SELECT COUNT(pt.id) FROM " + schema + "position_tokens pt " +
                "JOIN " + schema + "position p ON p.id = pt.fk_position " +
                "WHERE p.nav = ? AND p.pa_emittente = ? AND UPPER(pt.outcome) = 'OK'",
            Long.class,
            nav,
            paEmittente
        );
        return count == null ? 0 : count;
    }

    public Page<Object[]> findTransferDetailRows(
        String nav,
        String paEmittente,
        String token,
        Pageable pageable
    ) {
        String fromAndWhere = " FROM " + schema + "position p " +
            "JOIN " + schema + "position_tokens pt ON pt.fk_position = p.id " +
            "JOIN " + schema + "position_transfers ptr ON ptr.fk_token = pt.id " +
            "LEFT JOIN " + schema + "anag_pa_emittente ape ON ape.codice = p.pa_emittente " +
            "WHERE p.nav = ? AND p.pa_emittente = ? AND pt.token = convert_to(?, 'UTF8')";
        String sql = "SELECT p.nav, ape.description, p.last_event, pt.iuv, pt.creditor_ref_id, " +
            "encode(pt.token, 'hex'), (SELECT COUNT(ptr2.id) FROM " + schema + "position_transfers ptr2 " +
            "WHERE ptr2.fk_token = pt.id), ptr.id_transfer, ptr.is_bollo, ptr.iban_transfer, " +
            "ptr.amount_transfer, ptr.pa_transfer, ptr.date_event" + fromAndWhere +
            buildOrderBy(pageable, Map.of(
                "idTransfer", "ptr.id_transfer",
                "isBollo", "ptr.is_bollo",
                "ibanTransfer", "ptr.iban_transfer",
                "amountTransfer", "ptr.amount_transfer",
                "paTransfer", "ptr.pa_transfer"
            ), "ptr.id_transfer ASC, ptr.pa_transfer DESC") + " LIMIT ? OFFSET ?";
        return queryPage(
            sql,
            "SELECT COUNT(*)" + fromAndWhere,
            List.of(nav, paEmittente, token),
            pageable
        );
    }

    public Page<Object[]> findPositionWorkflows(String nav, String paEmittente, Pageable pageable) {
        String query = "SELECT * FROM ( " +
            "SELECT ew.inserted_timestamp_resp AS insertedtimestamp, ae.nome_evento AS nomeevento, " +
            "ae.tipo_evento AS tipoevento, ew.outcome_resp AS outcome, ew.event_id_resp AS eventid, " +
            "afc.codice AS faultcode, encode(pt.token, 'hex') AS token, 'RESP' AS reqresp, ew.id " +
            "FROM " + schema + "position p " +
            "LEFT JOIN " + schema + "events_wf ew ON ew.fk_position = p.id " +
            "LEFT JOIN " + schema + "position_tokens pt ON ew.fk_tokens = pt.id " +
            "LEFT JOIN " + schema + "anag_evento ae ON ae.id = ew.tipo_evento " +
            "LEFT JOIN " + schema + "anag_fault_code afc ON afc.id = ew.fault_code " +
            "WHERE p.nav = ? AND p.pa_emittente = ? " +
            "UNION ALL " +
            "SELECT ew.inserted_timestamp_req AS insertedtimestamp, ae.nome_evento AS nomeevento, " +
            "ae.tipo_evento AS tipoevento, ew.outcome_req AS outcome, ew.event_id_req AS eventid, " +
            "afc.codice AS faultcode, encode(pt.token, 'hex') AS token, 'REQ' AS reqresp, ew.id " +
            "FROM " + schema + "position p " +
            "LEFT JOIN " + schema + "events_wf ew ON ew.fk_position = p.id " +
            "LEFT JOIN " + schema + "position_tokens pt ON ew.fk_tokens = pt.id " +
            "LEFT JOIN " + schema + "anag_evento ae ON ae.id = ew.tipo_evento " +
            "LEFT JOIN " + schema + "anag_fault_code afc ON afc.id = ew.fault_code " +
            "WHERE p.nav = ? AND p.pa_emittente = ? " +
            ") workflow_events";
        String orderBy = buildOrderBy(pageable, Map.of(
            "insertedtimestamp", "insertedtimestamp",
            "nomeevento", "nomeevento",
            "tipoevento", "tipoevento",
            "outcome", "outcome",
            "eventid", "eventid",
            "faultcode", "faultcode",
            "token", "token",
            "id", "id",
            "reqResp", "reqresp"
        ), "insertedtimestamp DESC");
        List<Object> params = List.of(nav, paEmittente, nav, paEmittente);
        return queryPage(
            query + orderBy + " LIMIT ? OFFSET ?",
            "SELECT COUNT(*) FROM (" + query + ") workflow_events",
            params,
            pageable
        );
    }

    public Page<Object[]> findExtraInfoByToken(String token, Pageable pageable) {
        String fromAndWhere = " FROM " + schema + "position_tokens pt " +
            "JOIN " + schema + "position p ON p.id = pt.fk_position " +
            "JOIN " + schema + "extra_info ei ON ei.fk_token = pt.id " +
            "LEFT JOIN " + schema + "anag_pa_emittente ape ON ape.codice = p.pa_emittente " +
            "LEFT JOIN " + schema + "anag_evento ae ON ae.id = ei.tipo_evento " +
            "WHERE pt.token = convert_to(?, 'UTF8')";
        String sql = "SELECT p.nav, ape.description, encode(pt.token, 'hex'), ei.info_name, " +
            "ei.info_value, ae.nome_evento" + fromAndWhere +
            buildOrderBy(pageable, Map.of(
                "nav", "p.nav",
                "paEmittente", "ape.description",
                "token", "pt.token",
                "infoName", "ei.info_name",
                "infoValue", "ei.info_value",
                "tipoEvento", "ae.nome_evento"
            ), "ei.info_name DESC") + " LIMIT ? OFFSET ?";
        return queryPage(
            sql,
            "SELECT COUNT(*)" + fromAndWhere,
            List.of(token),
            pageable
        );
    }

    private Page<Object[]> queryPage(String sql, String countSql, List<?> parameters, Pageable pageable) {
        List<Object> queryParameters = new ArrayList<>(parameters);
        queryParameters.add(pageable.getPageSize());
        queryParameters.add(pageable.getOffset());
        List<Object[]> rows = queryRows(sql, queryParameters);
        Long total = jdbcTemplate.queryForObject(countSql, Long.class, parameters.toArray());
        return new PageImpl<>(rows, pageable, total == null ? 0 : total);
    }

    private List<Object[]> queryRows(String sql, List<?> parameters) {
        return jdbcTemplate.query(sql, (resultSet, rowNumber) -> {
            int columnCount = resultSet.getMetaData().getColumnCount();
            Object[] row = new Object[columnCount];
            for (int column = 0; column < columnCount; column++) {
                row[column] = resultSet.getObject(column + 1);
            }
            return row;
        }, parameters.toArray());
    }

    private String buildOrderBy(Pageable pageable, Map<String, String> sortColumns, String fallback) {
        List<String> sortExpressions = new ArrayList<>();
        pageable.getSort().forEach(order -> {
            String column = sortColumns.get(order.getProperty());
            if (column == null) {
                throw new IllegalArgumentException("Unsupported historical detail sort field: " + order.getProperty());
            }
            sortExpressions.add(column + (order.isAscending() ? " ASC" : " DESC"));
        });
        if (sortExpressions.isEmpty()) {
            sortExpressions.add(fallback);
        }
        return " ORDER BY " + String.join(", ", sortExpressions);
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
