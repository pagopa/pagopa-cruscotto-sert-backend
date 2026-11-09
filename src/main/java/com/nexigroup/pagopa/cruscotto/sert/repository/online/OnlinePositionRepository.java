package com.nexigroup.pagopa.cruscotto.sert.repository.online;

import com.nexigroup.pagopa.cruscotto.sert.domain.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface OnlinePositionRepository extends JpaRepository<Position, Integer> {

    @Query(value = "SELECT p.nav, p.paEmittente  FROM Position p WHERE (:nav IS  NULL OR p.nav = :nav) AND (:pa IS  NULL OR p.paEmittente = :pa) GROUP BY p.nav, p.paEmittente ")
    Page<Object[]> findByNavOrPa(@Param("nav") String nav, @Param("pa") String pa, Pageable pageable);

    @Query(value = "SELECT p.nav, p.paEmittente FROM Position p, PositionTokens pt " +
        "WHERE pt.fkPosition = p.id " +
        "AND (pt.iuv = :param OR pt.creditorRefId = :param) " +
        "AND (:nav IS NULL OR p.nav = :nav) " +
        "AND (:pa IS NULL OR p.paEmittente = :pa) " +
        "GROUP BY p.nav, p.paEmittente ",
        countQuery = "SELECT COUNT(DISTINCT p.id) FROM Position p, PositionTokens pt " +
            "WHERE pt.fkPosition = p.id " +
            "AND (pt.iuv = :param OR pt.creditorRefId = :param) " +
            "AND (:nav IS NULL OR p.nav = :nav) " +
            "AND (:pa IS NULL OR p.paEmittente = :pa)")
    Page<Object[]> findByIuvAndOptionalNavAndPa(@Param("param") String param, @Param("nav") String nav, @Param("pa") String pa, Pageable pageable);

    @Query(value = "SELECT p.nav, p.paEmittente FROM Position p, PositionTokens pt " +
        "WHERE pt.fkPosition = p.id " +
        "AND pt.token = FUNCTION('convert_to', :token, 'UTF8') " +
        "AND (:nav IS NULL OR p.nav = :nav) " +
        "AND (:pa IS NULL OR p.paEmittente = :pa) " +
        "GROUP BY p.nav, p.paEmittente ",
        countQuery = "SELECT COUNT(DISTINCT p.id) FROM Position p, PositionTokens pt " +
            "WHERE pt.fkPosition = p.id " +
            "AND pt.token = FUNCTION('convert_to', :token, 'UTF8') " +
            "AND (:nav IS NULL OR p.nav = :nav) " +
            "AND (:pa IS NULL OR p.paEmittente = :pa)")
    Page<Object[]> findByTokenAndOptionalNavAndPa(@Param("token") String token, @Param("nav") String nav, @Param("pa") String pa, Pageable pageable);

    @Query(value = "SELECT p.nav, p.paEmittente FROM Position p, PositionTokens pt " +
        "WHERE pt.fkPosition = p.id " +
        "AND pt.idCarrello = :idCart " +
        "AND (:nav IS NULL OR p.nav = :nav) " +
        "AND (:pa IS NULL OR p.paEmittente = :pa) " +
        "GROUP BY p.nav, p.paEmittente ",
        countQuery = "SELECT COUNT(DISTINCT p.id) FROM Position p, PositionTokens pt " +
            "WHERE pt.fkPosition = p.id " +
            "AND pt.idCarrello = :idCart " +
            "AND (:nav IS NULL OR p.nav = :nav) " +
            "AND (:pa IS NULL OR p.paEmittente = :pa)")
    Page<Object[]> findByCartAndOptionalNavAndPa(@Param("idCart") String idCart, @Param("nav") String nav, @Param("pa") String pa, Pageable pageable);

    @Query(value = "SELECT DISTINCT p FROM Position p, PositionTokens pt, ExtraInfo ei " +
        "WHERE pt.fkPosition = p.id " +
        "AND ei.fkToken = pt.id " +
        "AND ei.infoName = :infoName " +
        "AND ei.infoValue = :infoValue " +
        "AND (:nav IS NULL OR p.nav = :nav) " +
        "AND (:pa IS NULL OR p.paEmittente = :pa)")
    Page<Position> findByExtraAndOptionalNavAndPa(@Param("infoName") String infoName, @Param("infoValue") String infoValue, @Param("nav") String nav, @Param("pa") String pa, Pageable pageable);

    @Query(
        value = """
            SELECT DISTINCT
                p.nav AS nav,
                p.pa_emittente AS paEmittente
            FROM sert_ingestor.extra_info ei
            JOIN sert_ingestor.position_tokens pt
                ON pt.id = ei.fk_token
            JOIN sert_ingestor.position p
                ON p.id = pt.fk_position
            WHERE ei.info_value = :searchValue
              AND (:nav IS NULL OR p.nav = :nav)
              AND (:pa IS NULL OR p.pa_emittente = :pa)
            ORDER BY p.pa_emittente DESC
        """,
        nativeQuery = true
    )
    List<Object[]> findGroupedByExtraValueAndOptionalNavAndPa(
        @Param("searchValue") String searchValue,
        @Param("nav") String nav,
        @Param("pa") String pa
    );

    @Query(value = "SELECT " +
        "p.id AS positionId, " +
        "pt.id AS positionTokenId, " +
        "p.nav AS nav, " +
        "ape.description AS paEmittente, " +
        "p.lastEvent AS lastEvent, " +
        "pt.iuv AS iuv, " +
        "pt.creditorRefId AS creditorReferenceId, " +
        "FUNCTION('ENCODE', pt.token, 'hex') AS tokenHex, " +
        "pt.insertedTimestamp AS tokenDateEvent, " +
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
        "aipsp.description AS ptPspDesc, " +
        "pt.isCart AS isCart " +
        "FROM PositionTokens pt  " +
        "LEFT JOIN Position p  ON pt.fkPosition = p.id " +
        "LEFT JOIN AnagPsp apsp ON apsp.id = pt.psp " +
        "LEFT JOIN AnagIntermediarioPa aipa ON aipa.id = pt.intermediarioPa " +
        "LEFT JOIN AnagIntermediarioPsp aipsp ON aipsp.id = pt.intermediarioPsp " +
        "LEFT JOIN AnagStazione ast ON ast.id = pt.stazione " +
        "LEFT JOIN AnagCanale ac ON ac.id = pt.canale " +
        "LEFT JOIN AnagPaEmittente ape ON ape.codice = p.paEmittente " +
        "WHERE ((p.nav = :nav AND p.paEmittente = :paEmittente) " +
        "OR pt.fkPosition IN (:onlinePositionIds))"+
        "ORDER BY  p.lastEvent DESC ",
        countQuery =
            "SELECT COUNT(pt.id) " +
                "FROM Position p " +
                "JOIN PositionTokens pt ON pt.fkPosition = p.id " +
                "WHERE ((p.nav = :nav AND p.paEmittente = :paEmittente) " +
                "OR pt.fkPosition IN (:onlinePositionIds))")
    Page<Object[]> findPositionDetailRows(
        @Param("nav") String nav,
        @Param("paEmittente") String paEmittente,
        @Param("onlinePositionIds") List<Integer> onlinePositionIds,
        @Param("onlinePositionTokenIds") List<Integer> onlinePositionTokenIds,
        Pageable pageable
    );

    @Query(value = "SELECT " +
        "pt.fkPosition AS positionId, " +
        "pt.id AS positionTokenId, " +
        "p.nav AS nav, " +
        "ape.description AS paEmittente, " +
        "p.lastEvent AS lastEvent, " +
        "pt.iuv AS iuv, " +
        "pt.creditorRefId AS creditorReferenceId, " +
        "FUNCTION('ENCODE', pt.token, 'hex') AS tokenHex, " +
        "pt.insertedTimestamp AS tokenDateEvent, " +
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
        "aipsp.description AS ptPspDesc, " +
        "pt.isCart AS isCart " +
        "FROM PositionTokens pt " +
        "LEFT JOIN Position p ON p.id = pt.fkPosition " +
        "LEFT JOIN AnagPsp apsp ON apsp.id = pt.psp " +
        "LEFT JOIN AnagIntermediarioPa aipa ON aipa.id = pt.intermediarioPa " +
        "LEFT JOIN AnagIntermediarioPsp aipsp ON aipsp.id = pt.intermediarioPsp " +
        "LEFT JOIN AnagStazione ast ON ast.id = pt.stazione " +
        "LEFT JOIN AnagCanale ac ON ac.id = pt.canale " +
        "LEFT JOIN AnagPaEmittente ape ON ape.codice = p.paEmittente " +
        "WHERE (pt.token = FUNCTION('convert_to', :token, 'UTF8')) " +
        "ORDER BY pt.paymentDate DESC, pt.dateEvent DESC, pt.id DESC")
    List<Object[]> findTokenDetailRow(@Param("token") String token);

    @Query(value = "SELECT " +
        "p,p.id as positionId, " +
        "pt.id as positionTokensId, "+
        "p.nav AS nav, " +
        "ape.description AS paEmittente, " +
        "p.lastEvent AS lastEvent, " +
        "pt.iuv AS iuv, " +
        "pt.creditorRefId AS creditorReferenceId, " +
        "FUNCTION('ENCODE', pt.token, 'hex') AS tokenHex, " +
        "(SELECT COUNT(ptr2.id) FROM PositionTransfers ptr2 WHERE ptr2.fkToken = pt.id) AS transfersCount, " +
        "ptr.idTransfer AS idTransfer, " +
        "ptr.isBollo AS isBollo, " +
        "ptr.ibanTransfer AS ibanTransfer, " +
        "ptr.amountTransfer AS amountTransfer, " +
        "ptr.paTransfer AS paTransfer, " +
        "ptr.dateEvent AS transferDateEvent " +
        "FROM PositionTransfers ptr " +
        "LEFT JOIN PositionTokens pt ON ptr.fkToken = pt.id " +
        "LEFT JOIN Position p ON pt.fkPosition = p.id " +
        "LEFT JOIN AnagPaEmittente ape ON ape.codice = p.paEmittente " +
        "WHERE  (pt.token = FUNCTION('convert_to', :token, 'UTF8') " +
        "OR ptr.fkToken IN (:onlinePositionTokenIds))")
    List<Object[]> findTransferDetailRows(
        @Param("token") String token,
        @Param("onlinePositionTokenIds") List<Integer> onlinePositionTokenIds);

    @Query(value =
        "SELECT insertedtimestamp, nomeevento, tipoevento, outcome, eventid, faultcode, token, reqResp, id, positionTokenId  " +
            "FROM ( " +
            "SELECT " +
            "ew.insertedTimestampResp AS insertedtimestamp, " +
            "ae.nomeEvento AS nomeevento, " +
            "ae.tipoEvento AS tipoevento, " +
            "ew.outcomeResp AS outcome, " +
            "ew.eventIdResp AS eventid, " +
            "afc.codice AS faultcode, " +
            "ew.fkTokens  AS token, " +
            "'RESP' AS reqResp, " +
            "ew.id AS id, " +
            "pt.id AS positionTokenId " +
            "FROM EventsWf ew " +
            "LEFT JOIN PositionTokens pt ON pt.id = ew.fkTokens " +
            "LEFT JOIN AnagEvento ae ON ae.id = ew.tipoEvento " +
            "LEFT JOIN AnagFaultCode afc ON afc.id = ew.faultCode " +
            "WHERE ew.fkTokens IN :positionTokenIds " +
            "UNION ALL " +
            "SELECT " +
            "ew.insertedTimestampReq AS insertedtimestamp, " +
            "ae.nomeEvento AS nomeevento, " +
            "ae.tipoEvento AS tipoevento, " +
            "ew.outcomeReq AS outcome, " +
            "ew.eventIdReq AS eventid, " +
            "afc.codice AS faultcode, " +
            "ew.fkTokens  AS token, " +
            "'REQ' AS reqResp, " +
            "ew.id AS id, " +
            "pt.id AS positionTokenId " +
            "FROM EventsWf ew " +
            "LEFT JOIN PositionTokens pt ON pt.id = ew.fkTokens " +
            "LEFT JOIN AnagEvento ae ON ae.id = ew.tipoEvento " +
            "LEFT JOIN AnagFaultCode afc ON afc.id = ew.faultCode " +
            "WHERE ew.fkTokens IN :positionTokenIds " +
            " )"

    )
    List<Object[]> findPositionWorkflows(
        @Param("positionTokenIds") List<Integer> positionTokenIds
    );

    @Query("SELECT pt.id, FUNCTION('ENCODE', pt.token, 'hex') FROM PositionTokens pt WHERE pt.fkPosition IN :positionIds")
    List<Object[]> findTokenByPositionIds(@Param("positionIds") List<Integer> positionIds);

    @Query(value = "SELECT " +
        "ei.id AS extraInfoId, " +
        "ei.infoName AS infoName, " +
        "ei.infoValue AS infoValue, " +
        "ae.nomeEvento AS tipoEvento " +
        "FROM ExtraInfo ei " +
        "LEFT JOIN AnagEvento ae ON ae.id = ei.tipoEvento " +
        "WHERE ei.fkToken IN (:offlinePositionTokenIds) ")
    List<Object[]> findExtraInfoByToken(
        @Param("offlinePositionTokenIds") List<Integer> offlinePositionTokenIds
    );

    @Query("SELECT COUNT(pt) FROM PositionTokens pt, Position p " +
        "WHERE p.id = pt.fkPosition " +
        "AND (p.nav = :nav AND p.paEmittente = :paEmittente " +
        "OR p.id IN (:offlinePositionIds) " +
        "OR pt.id IN (:offlinePositionTokenIds)) " +
        "AND UPPER(pt.outcome) = 'OK'")
    long countOkTokensByNavAndPa(
        @Param("nav") String nav,
        @Param("paEmittente") String paEmittente,
        @Param("offlinePositionIds") List<Integer> offlinePositionIds,
        @Param("offlinePositionTokenIds") List<Integer> offlinePositionTokenIds
    );

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

    @Query(value = "SELECT p.id, pt.id FROM Position p, PositionTokens pt " +
        "WHERE pt.fkPosition = p.id " +
        "AND (pt.iuv = :param OR pt.creditorRefId = :param) " +
        "AND (:nav IS NULL OR p.nav = :nav) " +
        "AND (:pa IS NULL OR p.paEmittente = :pa) " +
        "GROUP BY p.id, pt.id ")
    List<Object[]> searchByIuv(@Param("param") String param, @Param("nav") String nav, @Param("pa") String pa, Pageable pageable);

    @Query(value = "SELECT p.id, pt.id FROM Position p, PositionTokens pt " +
        "WHERE pt.fkPosition = p.id " +
        "AND pt.token = FUNCTION('convert_to', :token, 'UTF8') " +
        "AND (:nav IS NULL OR p.nav = :nav) " +
        "AND (:pa IS NULL OR p.paEmittente = :pa) " +
        "GROUP BY p.id, pt.id ")
    List<Object[]> searchByToken(@Param("token") String token, @Param("nav") String nav, @Param("pa") String pa, Pageable pageable);

    @Query(value = "SELECT p.id, pt.id FROM Position p, PositionTokens pt " +
        "WHERE pt.fkPosition = p.id " +
        "AND pt.idCarrello = :idCart " +
        "AND (:nav IS NULL OR p.nav = :nav) " +
        "AND (:pa IS NULL OR p.paEmittente = :pa) " +
        "GROUP BY p.id, pt.id ")
    List<Object[]> searchByCart(@Param("idCart") String idCart, @Param("nav") String nav, @Param("pa") String pa, Pageable pageable);




    @Query(
        value = """
            SELECT DISTINCT
                p.id AS positionId,
                pt.id AS positionTokenId
            FROM sert_ingestor.extra_info ei
            JOIN sert_ingestor.position_tokens pt
                ON pt.id = ei.fk_token
            JOIN sert_ingestor.position p
                ON p.id = pt.fk_position
            WHERE ei.info_value = :searchValue
              AND (:nav IS NULL OR p.nav = :nav)
              AND (:pa IS NULL OR p.pa_emittente = :pa)
        """,
        nativeQuery = true
    )
    List<Object[]> searchExtra(
        @Param("searchValue") String searchValue,
        @Param("nav") String nav,
        @Param("pa") String pa
    );


}
