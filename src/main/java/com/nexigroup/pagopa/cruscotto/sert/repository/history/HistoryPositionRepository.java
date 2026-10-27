package com.nexigroup.pagopa.cruscotto.sert.repository.history;

import com.nexigroup.pagopa.cruscotto.sert.domain.Position;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoryPositionRepository extends JpaRepository<Position, Integer> {

    @Query(value = "SELECT p.id, pt.id " +
        " FROM Position p, PositionTokens pt " +
        " WHERE pt.fkPosition = p.id " +
        " AND (:nav IS NULL OR p.nav = :nav) " +
        " AND (:pa IS NULL OR p.paEmittente = :pa) " +
        " AND (CAST(:dateFrom AS date) IS NULL OR CAST(:dateTo AS date) IS NULL " +
        " OR p.dateEvent BETWEEN :dateFrom AND :dateTo) " +
        " GROUP BY p.id, pt.id ")
    List<Object[]> findByNavOrPa(@Param("nav") String nav, @Param("pa") String pa);


    @Query(value = "SELECT p.nav, p.paEmittente FROM Position p " +
        "WHERE (:nav IS NULL OR p.nav = :nav) " +
        "AND (:pa IS NULL OR p.paEmittente = :pa) " +
        "AND (CAST(:dateFrom AS date) IS NULL OR CAST(:dateTo AS date) IS NULL " +
        "OR p.dateEvent BETWEEN :dateFrom AND :dateTo) " +
        "GROUP BY p.nav, p.paEmittente ")
    Page<Object[]> findByNavOrPa(
        @Param("nav") String nav,
        @Param("pa") String pa,
        @Param("dateFrom") LocalDate dateFrom,
        @Param("dateTo") LocalDate dateTo,
        Pageable pageable
    );

    @Query(
        value = "SELECT p.nav, p.paEmittente FROM Position p, PositionTokens pt " +
            "WHERE pt.fkPosition = p.id " +
            "AND ((pt.iuv = :param OR pt.creditorRefId = :param) " +
            "OR p.id IN :onlinePositionIds " +
            "OR pt.id IN :onlinePositionTokenIds) " +
            "AND (:nav IS NULL OR p.nav = :nav) " +
            "AND (:pa IS NULL OR p.paEmittente = :pa) " +
            "AND (CAST(:dateFrom AS date) IS NULL OR CAST(:dateTo AS date) IS NULL " +
            "OR p.dateEvent BETWEEN :dateFrom AND :dateTo " +
            "OR pt.dateEvent BETWEEN :dateFrom AND :dateTo) " +
            "GROUP BY p.nav, p.paEmittente",
        countQuery = "SELECT COUNT(DISTINCT CONCAT(" +
            "CONCAT(CASE WHEN p.nav IS NULL THEN 'N;' ELSE CONCAT(CONCAT('V', CAST(LENGTH(p.nav) AS string)), CONCAT(':', p.nav)) END), " +
            "CASE WHEN p.paEmittente IS NULL THEN 'N;' ELSE CONCAT(CONCAT('V', CAST(LENGTH(p.paEmittente) AS string)), CONCAT(':', p.paEmittente)) END)) " +
            "FROM Position p, PositionTokens pt " +
            "WHERE pt.fkPosition = p.id " +
            "AND ((pt.iuv = :param OR pt.creditorRefId = :param) " +
            "OR p.id IN :onlinePositionIds " +
            "OR pt.id IN :onlinePositionTokenIds) " +
            "AND (:nav IS NULL OR p.nav = :nav) " +
            "AND (:pa IS NULL OR p.paEmittente = :pa) " +
            "AND (CAST(:dateFrom AS date) IS NULL OR CAST(:dateTo AS date) IS NULL " +
            "OR p.dateEvent BETWEEN :dateFrom AND :dateTo " +
            "OR pt.dateEvent BETWEEN :dateFrom AND :dateTo)"
    )
    Page<Object[]> findByIuvAndOptionalNavAndPa(
        @Param("param") String param,
        @Param("nav") String nav,
        @Param("pa") String pa,
        @Param("dateFrom") LocalDate dateFrom,
        @Param("dateTo") LocalDate dateTo,
        @Param("onlinePositionIds") List<Integer> onlinePositionIds,
        @Param("onlinePositionTokenIds") List<Integer> onlinePositionTokenIds,
        Pageable pageable
    );

    @Query(
        value = "SELECT p.nav, p.paEmittente FROM Position p, PositionTokens pt " +
            "WHERE pt.fkPosition = p.id " +
            "AND (pt.token = FUNCTION('convert_to', :token, 'UTF8') " +
            "OR p.id IN :onlinePositionIds " +
            "OR pt.id IN :onlinePositionTokenIds) " +
            "AND (:nav IS NULL OR p.nav = :nav) " +
            "AND (:pa IS NULL OR p.paEmittente = :pa) " +
            "AND (CAST(:dateFrom AS date) IS NULL OR CAST(:dateTo AS date) IS NULL " +
            "OR p.dateEvent BETWEEN :dateFrom AND :dateTo " +
            "OR pt.dateEvent BETWEEN :dateFrom AND :dateTo) " +
            "GROUP BY p.nav, p.paEmittente",
        countQuery = "SELECT COUNT(DISTINCT CONCAT(CONCAT(COALESCE(p.nav, ''), '|'), COALESCE(p.paEmittente, ''))) " +
            "FROM Position p, PositionTokens pt " +
            "WHERE pt.fkPosition = p.id " +
            "AND (pt.token = FUNCTION('convert_to', :token, 'UTF8') " +
            "OR p.id IN :onlinePositionIds " +
            "OR pt.id IN :onlinePositionTokenIds) " +
            "AND (:nav IS NULL OR p.nav = :nav) " +
            "AND (:pa IS NULL OR p.paEmittente = :pa) " +
            "AND (CAST(:dateFrom AS date) IS NULL OR CAST(:dateTo AS date) IS NULL " +
            "OR p.dateEvent BETWEEN :dateFrom AND :dateTo " +
            "OR pt.dateEvent BETWEEN :dateFrom AND :dateTo)"
    )
    Page<Object[]> findByTokenAndOptionalNavAndPa(
        @Param("token") String token,
        @Param("nav") String nav,
        @Param("pa") String pa,
        @Param("dateFrom") LocalDate dateFrom,
        @Param("dateTo") LocalDate dateTo,
        @Param("onlinePositionIds") List<Integer> onlinePositionIds,
        @Param("onlinePositionTokenIds") List<Integer> onlinePositionTokenIds,
        Pageable pageable
    );

    @Query(
        value = "SELECT p.nav, p.paEmittente FROM Position p, PositionTokens pt " +
            "WHERE pt.fkPosition = p.id " +
            "AND (pt.idCarrello = :idCart " +
            "OR p.id IN :onlinePositionIds " +
            "OR pt.id IN :onlinePositionTokenIds) " +
            "AND (:nav IS NULL OR p.nav = :nav) " +
            "AND (:pa IS NULL OR p.paEmittente = :pa) " +
            "AND (CAST(:dateFrom AS date) IS NULL OR CAST(:dateTo AS date) IS NULL " +
            "OR p.dateEvent BETWEEN :dateFrom AND :dateTo " +
            "OR pt.dateEvent BETWEEN :dateFrom AND :dateTo) " +
            "GROUP BY p.nav, p.paEmittente",
        countQuery = "SELECT COUNT(DISTINCT CONCAT(" +
            "CONCAT(CASE WHEN p.nav IS NULL THEN 'N;' ELSE CONCAT(CONCAT('V', CAST(LENGTH(p.nav) AS string)), CONCAT(':', p.nav)) END), " +
            "CASE WHEN p.paEmittente IS NULL THEN 'N;' ELSE CONCAT(CONCAT('V', CAST(LENGTH(p.paEmittente) AS string)), CONCAT(':', p.paEmittente)) END)) " +
            "FROM Position p, PositionTokens pt " +
            "WHERE pt.fkPosition = p.id " +
            "AND (pt.idCarrello = :idCart " +
            "OR p.id IN :onlinePositionIds " +
            "OR pt.id IN :onlinePositionTokenIds) " +
            "AND (:nav IS NULL OR p.nav = :nav) " +
            "AND (:pa IS NULL OR p.paEmittente = :pa) " +
            "AND (CAST(:dateFrom AS date) IS NULL OR CAST(:dateTo AS date) IS NULL " +
            "OR p.dateEvent BETWEEN :dateFrom AND :dateTo " +
            "OR pt.dateEvent BETWEEN :dateFrom AND :dateTo)"
    )
    Page<Object[]> findByCartAndOptionalNavAndPa(
        @Param("idCart") String idCart,
        @Param("nav") String nav,
        @Param("pa") String pa,
        @Param("dateFrom") LocalDate dateFrom,
        @Param("dateTo") LocalDate dateTo,
        @Param("onlinePositionIds") List<Integer> onlinePositionIds,
        @Param("onlinePositionTokenIds") List<Integer> onlinePositionTokenIds,
        Pageable pageable
    );

    @Query(
        value = """
            SELECT DISTINCT
                p.nav AS nav,
                p.pa_emittente AS paEmittente
            FROM sert_history.position p
            JOIN sert_history.position_tokens pt
                ON pt.fk_position = p.id
            LEFT JOIN sert_history.extra_info ei
                ON ei.fk_token = pt.id
            WHERE (
                  ei.info_value = :searchValue
                  OR p.id = ANY(COALESCE(string_to_array(NULLIF(:onlinePositionIds, ''), ',')::integer[], ARRAY[]::integer[]))
                  OR pt.id = ANY(COALESCE(string_to_array(NULLIF(:onlinePositionTokenIds, ''), ',')::integer[], ARRAY[]::integer[]))
              )
              AND (:nav IS NULL OR p.nav = :nav)
              AND (:pa IS NULL OR p.pa_emittente = :pa)
              AND (
                  (CAST(:dateFrom AS date) IS NULL OR CAST(:dateTo AS date) IS NULL)
                  OR p.date_event BETWEEN :dateFrom AND :dateTo
                  OR pt.date_event BETWEEN :dateFrom AND :dateTo
                  OR ei.date_event BETWEEN :dateFrom AND :dateTo
              )
            ORDER BY p.pa_emittente DESC
        """,
        nativeQuery = true
    )
    List<Object[]> findGroupedByExtraValueAndOptionalNavAndPa(
        @Param("searchValue") String searchValue,
        @Param("nav") String nav,
        @Param("pa") String pa,
        @Param("dateFrom") LocalDate dateFrom,
        @Param("dateTo") LocalDate dateTo,
        @Param("onlinePositionIds") String onlinePositionIds,
        @Param("onlinePositionTokenIds") String onlinePositionTokenIds
    );

    @Query(value = "SELECT " +
        "p.nav AS nav, " +
        "ape.description AS paEmittente, " +
        "p.lastEvent AS lastEvent, " +
        "pt.iuv AS iuv, " +
        "pt.creditorRefId AS creditorReferenceId, " +
        "FUNCTION('ENCODE', pt.token, 'hex') AS tokenHex, " +
        "pt.dateEvent AS tokenDateEvent, " +
        "pt.paymentDate AS paymentDate, " +
        "pt.outcome AS outcome, " +
        "pt.amount AS amount, " +
        "pt.fee AS fee, " +
        "apsp.codice AS psp, " +
        "aipa.codice AS ptPa, " +
        "aipsp.codice AS ptPsp, " +
        "ast.codice AS station, " +
        "ac.codice AS channel, " +
        "pt.touchpoint AS touchpoint, " +
        "pt.paymentMethod AS paymentMethod, " +
        "pt.idCarrello AS idCarrello, " +
        "apsp.description AS pspDesc, " +
        "aipa.description AS ptPaDesc, " +
        "aipsp.description AS ptPspDesc " +
        "FROM Position p " +
        "LEFT JOIN PositionTokens pt ON pt.fkPosition = p.id " +
        "LEFT JOIN AnagPsp apsp ON apsp.id = pt.psp " +
        "LEFT JOIN AnagIntermediarioPa aipa ON aipa.id = pt.intermediarioPa " +
        "LEFT JOIN AnagIntermediarioPsp aipsp ON aipsp.id = pt.intermediarioPsp " +
        "LEFT JOIN AnagStazione ast ON ast.id = pt.stazione " +
        "LEFT JOIN AnagCanale ac ON ac.id = pt.canale " +
        "LEFT JOIN AnagPaEmittente ape ON ape.codice = p.paEmittente " +
        "WHERE p.nav = :nav " +
        "AND p.paEmittente = :paEmittente " +
        "ORDER BY  p.lastEvent DESC ",
        countQuery =
            "SELECT COUNT(pt.id) " +
                "FROM Position p " +
                "JOIN PositionTokens pt ON pt.fkPosition = p.id " +
                "WHERE p.nav = :nav " +
                "AND p.paEmittente = :paEmittente ")
    Page<Object[]> findPositionDetailRows(@Param("nav") String nav, @Param("paEmittente") String paEmittente, Pageable pageable);

    @Query(value = "SELECT " +
        "p.nav AS nav, " +
        "ape.description AS paEmittente, " +
        "p.lastEvent AS lastEvent, " +
        "pt.iuv AS iuv, " +
        "pt.creditorRefId AS creditorReferenceId, " +
        "FUNCTION('ENCODE', pt.token, 'hex') AS tokenHex, " +
        "pt.dateEvent AS tokenDateEvent, " +
        "pt.paymentDate AS paymentDate, " +
        "pt.outcome AS outcome, " +
        "pt.amount AS amount, " +
        "pt.fee AS fee, " +
        "apsp.codice AS psp, " +
        "aipa.codice AS intermediarioPa, " +
        "aipsp.codice AS intermediarioPsp, " +
        "ast.codice AS station, " +
        "ac.codice AS channel, " +
        "pt.touchpoint AS touchpoint, " +
        "pt.paymentMethod AS paymentMethod, " +
        "pt.idCarrello AS idCarrello, " +
        "apsp.description AS pspDesc, " +
        "aipa.description AS ptPaDesc, " +
        "aipsp.description AS ptPspDesc " +
        "FROM PositionTokens pt " +
        "JOIN Position p ON p.id = pt.fkPosition " +
        "LEFT JOIN AnagPsp apsp ON apsp.id = pt.psp " +
        "LEFT JOIN AnagIntermediarioPa aipa ON aipa.id = pt.intermediarioPa " +
        "LEFT JOIN AnagIntermediarioPsp aipsp ON aipsp.id = pt.intermediarioPsp " +
        "LEFT JOIN AnagStazione ast ON ast.id = pt.stazione " +
        "LEFT JOIN AnagCanale ac ON ac.id = pt.canale " +
        "LEFT JOIN AnagPaEmittente ape ON ape.codice = p.paEmittente " +
        "WHERE pt.token = FUNCTION('convert_to', :token, 'UTF8') " +
        "ORDER BY pt.paymentDate DESC, pt.dateEvent DESC, pt.id DESC")
    List<Object[]> findTokenDetailRow(@Param("token") String token);

    @Query(value = "SELECT p.nav AS nav, ape.description AS paEmittente, p.lastEvent AS lastEvent, " +
        "pt.iuv AS iuv, pt.creditorRefId AS creditorReferenceId, FUNCTION('ENCODE', pt.token, 'hex') AS tokenHex, " +
        "(SELECT COUNT(ptr2.id) FROM PositionTransfers ptr2 WHERE ptr2.fkToken = pt.id) AS transfersCount, " +
        "ptr.idTransfer AS idTransfer, ptr.isBollo AS isBollo, " +
        "ptr.ibanTransfer AS ibanTransfer, ptr.amountTransfer AS amountTransfer, ptr.paTransfer AS paTransfer, " +
        "ptr.dateEvent AS transferDateEvent " +
        "FROM Position p " +
        "JOIN PositionTokens pt ON pt.fkPosition = p.id " +
        "JOIN PositionTransfers ptr ON ptr.fkToken = pt.id " +
        "LEFT JOIN AnagPaEmittente ape ON ape.codice = p.paEmittente " +
        "WHERE p.nav = :nav AND p.paEmittente = :paEmittente " +
        "AND pt.token = FUNCTION('convert_to', :token, 'UTF8') " )
    Page<Object[]> findTransferDetailRows(@Param("nav") String nav, @Param("paEmittente") String paEmittente, @Param("token") String token, Pageable pageable);

    @Query(value =
        "SELECT insertedtimestamp, nomeevento, tipoevento, outcome, eventid, faultcode, token, reqResp, id  " +
            "FROM ( " +
            "SELECT " +
            "ew.insertedTimestampResp AS insertedtimestamp, " +
            "ae.nomeEvento AS nomeevento, " +
            "ae.tipoEvento AS tipoevento, " +
            "ew.outcomeResp AS outcome, " +
            "ew.eventIdResp AS eventid, " +
            "afc.codice AS faultcode, " +
            "FUNCTION('ENCODE', pt.token, 'hex') AS token, " +
            "'RESP' AS reqResp, " +
            "ew.id AS id " +
            "FROM Position p " +
            "LEFT JOIN EventsWf ew ON ew.fkPosition = p.id " +
            "LEFT JOIN PositionTokens pt ON ew.fkTokens = pt.id " +
            "LEFT JOIN AnagEvento ae ON ae.id = ew.tipoEvento " +
            "LEFT JOIN AnagFaultCode afc ON afc.id = ew.faultCode " +
            "WHERE p.nav = :nav AND p.paEmittente = :paEmittente " +
            "UNION ALL " +
            "SELECT " +
            "ew.insertedTimestampReq AS insertedtimestamp, " +
            "ae.nomeEvento AS nomeevento, " +
            "ae.tipoEvento AS tipoevento, " +
            "ew.outcomeReq AS outcome, " +
            "ew.eventIdReq AS eventid, " +
            "afc.codice AS faultcode, " +
            "FUNCTION('ENCODE', pt.token, 'hex') AS token, " +
            "'REQ' AS reqResp, " +
            "ew.id AS id " +
            "FROM Position p " +
            "LEFT JOIN EventsWf ew ON ew.fkPosition = p.id " +
            "LEFT JOIN PositionTokens pt ON ew.fkTokens = pt.id " +
            "LEFT JOIN AnagEvento ae ON ae.id = ew.tipoEvento " +
            "LEFT JOIN AnagFaultCode afc ON afc.id = ew.faultCode " +
            "WHERE p.nav = :nav AND p.paEmittente = :paEmittente " +
            " )"
        ,
        countQuery =
            "SELECT COUNT(*) FROM ( " +
                "SELECT ew.id AS id " +
                "FROM Position p " +
                "LEFT JOIN EventsWf ew ON ew.fkPosition = p.id " +
                "WHERE p.nav = :nav AND p.paEmittente = :paEmittente " +
                "UNION ALL " +
                "SELECT ew.id AS id " +
                "FROM Position p " +
                "LEFT JOIN EventsWf ew ON ew.fkPosition = p.id " +
                "WHERE p.nav = :nav AND p.paEmittente = :paEmittente " +
                ") "
    )
    Page<Object[]> findPositionWorkflows(
        @Param("nav") String nav,
        @Param("paEmittente") String paEmittente,
        Pageable pageable
    );

    @Query(value = "SELECT " +
        "p.nav AS nav," +
        " ape.description AS paEmittente," +
        " FUNCTION('ENCODE', pt.token, 'hex') AS token, " +
        " ei.infoName AS infoName," +
        " ei.infoValue AS infoValue, " +
        " ae.nomeEvento  AS tipoEvento " +
        "FROM PositionTokens pt " +
        "JOIN Position p ON p.id = pt.fkPosition " +
        "JOIN ExtraInfo ei ON ei.fkToken = pt.id " +
        "LEFT JOIN AnagPaEmittente ape ON ape.codice = p.paEmittente " +
        "LEFT JOIN AnagEvento ae ON ae.id = ei.tipoEvento " +
        "WHERE pt.token = FUNCTION('convert_to', :token, 'UTF8') " )
    Page<Object[]> findExtraInfoByToken(@Param("token") String token, Pageable pageable);

    @Query("SELECT COUNT(pt) FROM PositionTokens pt, Position p " +
        "WHERE p.id = pt.fkPosition " +
        "AND p.nav = :nav AND p.paEmittente = :paEmittente " +
        "AND UPPER(pt.outcome) = 'OK'")
    long countOkTokensByNavAndPa(@Param("nav") String nav, @Param("paEmittente") String paEmittente);

    @Query(
        "SELECT DISTINCT p.nav AS nav, p.paEmittente AS paEmittente " +
            "FROM Position p " +
            "JOIN PositionTokens pt ON pt.fkPosition = p.id " +
            "WHERE pt.paymentDate >= COALESCE(:paymentFrom, pt.paymentDate) " +
            "AND pt.paymentDate <= COALESCE(:paymentTo, pt.paymentDate) " +
            "AND ( " +
            "    :paymentStatuses IS NULL OR " +
            "    (COALESCE(:includeNoOutcome, false) = true AND " +
            "        (pt.outcome IS NULL OR trim(pt.outcome) = '')) OR " +
            "    pt.outcome IN (:paymentStatuses) " +
            ") " +
            "AND (:touchpoints IS NULL OR pt.touchpoint IN (:touchpoints)) " +
            "AND (:paymentMethods IS NULL OR pt.paymentMethod IN (:paymentMethods)) " +
            "AND pt.amount >= COALESCE(:amountMin, pt.amount) " +
            "AND pt.amount <= COALESCE(:amountMax, pt.amount) " +
            "AND pt.amount = COALESCE(:amountExact, pt.amount) " +
            "AND (:creditors IS NULL OR p.paEmittente IN (:creditors)) " +
            "AND (:psps IS NULL OR pt.psp IN (:psps)) " +
            "AND (:techPartners IS NULL OR " +
            "     pt.intermediarioPa IN (:techPartners) OR " +
            "     pt.intermediarioPsp IN (:techPartners)) " +
            "AND (:channels IS NULL OR pt.canale IN (:channels)) " +
            "AND (:stations IS NULL OR pt.stazione IN (:stations)) " +
            "ORDER BY p.nav, p.paEmittente"
    )
    List<Object[]> findNavPaByFilter(
        @Param("paymentFrom") LocalDateTime paymentFrom,
        @Param("paymentTo") LocalDateTime paymentTo,
        @Param("paymentStatuses") List<String> paymentStatuses,
        @Param("includeNoOutcome") Boolean includeNoOutcome,
        @Param("touchpoints") List<String> touchpoints,
        @Param("paymentMethods") List<String> paymentMethods,
        @Param("amountExact") BigDecimal amountExact,
        @Param("amountMin") BigDecimal amountMin,
        @Param("amountMax") BigDecimal amountMax,
        @Param("creditors") List<String> creditors,
        @Param("psps") List<Integer> psps,
        @Param("techPartners") List<Integer> techPartners,
        @Param("channels") List<Integer> channels,
        @Param("stations") List<Integer> stations,
        Pageable pageable
    );
}
