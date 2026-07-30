package com.nexigroup.pagopa.cruscotto.sert.repository;

import com.nexigroup.pagopa.cruscotto.sert.domain.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface PositionRepository extends JpaRepository<Position, Integer> {

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

    @Query(value = "SELECT p.nav AS nav, ape.description AS paEmittente, " +
                   "FUNCTION('STRING_AGG', ei.infoName, ',') AS infoMatch " +
                   "FROM Position p " +
                   "JOIN PositionTokens pt ON pt.fkPosition = p.id " +
                   "JOIN ExtraInfo ei ON ei.fkToken = pt.id " +
                   "LEFT JOIN AnagPaEmittente ape ON ape.codice = p.paEmittente " +
                   "WHERE ei.infoValue = :searchValue " +
                   "AND (:nav IS NULL OR p.nav = :nav) " +
                   "AND (:pa IS NULL OR p.paEmittente = :pa) " +
                   "GROUP BY p.nav, p.paEmittente, ape.description",
           countQuery = "SELECT count(distinct p.id) FROM Position p " +
                        "JOIN PositionTokens pt ON pt.fkPosition = p.id " +
                        "JOIN ExtraInfo ei ON ei.fkToken = pt.id " +
                        "WHERE ei.infoValue = :searchValue AND (:nav IS NULL OR p.nav = :nav) " +
                        "AND (:pa IS NULL OR p.paEmittente = :pa)")
    Page<Object[]> findGroupedByExtraValueAndOptionalNavAndPa(@Param("searchValue") String searchValue, @Param("nav") String nav, @Param("pa") String pa, Pageable pageable);

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
                "LEFT JOIN PositionTokens pt ON pt.fkPosition = p.id " +
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
        countQuery = "SELECT COUNT(ew.id) * 2 FROM Position p " +
                     "LEFT JOIN EventsWf ew ON ew.fkPosition = p.id " +
                     "WHERE p.nav = :nav AND p.paEmittente = :paEmittente" )
    Page<Object[]> findPositionWorkflows(
        @Param("nav") String nav,
        @Param("paEmittente") String paEmittente,
        Pageable pageable
    );

    @Query(value = "SELECT p.nav AS nav, ape.description AS paEmittente, FUNCTION('ENCODE', pt.token, 'hex') AS token, " +
                    "ei.infoName AS infoName, ei.infoValue AS infoValue, " +
                    "(SELECT MAX(ae2.nomeEvento) FROM EventsWf ew2 LEFT JOIN AnagEvento ae2 ON ae2.id = ew2.tipoEvento WHERE ew2.fkTokens = pt.id) AS tipoEvento " +
                    "FROM PositionTokens pt " +
                    "JOIN Position p ON p.id = pt.fkPosition " +
                    "JOIN ExtraInfo ei ON ei.fkToken = pt.id " +
                    "LEFT JOIN AnagPaEmittente ape ON ape.codice = p.paEmittente " +
                    "WHERE pt.token = FUNCTION('convert_to', :token, 'UTF8') " )
    Page<Object[]> findExtraInfoByToken(@Param("token") String token, Pageable pageable);

    @Query("SELECT COUNT(pt) FROM PositionTokens pt, Position p " +
           "WHERE p.id = pt.fkPosition " +
           "AND p.nav = :nav AND p.paEmittente = :paEmittente " +
           "AND UPPER(pt.outcome) = 'OK'")
    long countOkTokensByNavAndPa(@Param("nav") String nav, @Param("paEmittente") String paEmittente);
}
