package com.nexigroup.pagopa.cruscotto.sert.repository;

import com.nexigroup.pagopa.cruscotto.sert.domain.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PositionRepository extends JpaRepository<Position, Integer> {

    @Query(value = "SELECT p FROM Position p WHERE (:nav IS NOT NULL AND p.nav = :nav) OR (:pa IS NOT NULL AND p.paEmittente = :pa)")
    List<Position> findByNavOrPa(@Param("nav") String nav, @Param("pa") String pa);

    @Query(value = "SELECT DISTINCT p FROM Position p, PositionTokens pt " +
                   "WHERE pt.fkPosition = p.id " +
                   "AND pt.iuv = :iuv " +
                   "AND (:nav IS NULL OR p.nav = :nav) " +
                   "AND (:pa IS NULL OR p.paEmittente = :pa)")
    List<Position> findByIuvAndOptionalNavAndPa(@Param("iuv") String iuv, @Param("nav") String nav, @Param("pa") String pa);

    @Query(value = "SELECT DISTINCT p FROM Position p, PositionTokens pt " +
                   "WHERE pt.fkPosition = p.id " +
                   "AND pt.token = FUNCTION('convert_to', :token, 'UTF8') " +
                   "AND (:nav IS NULL OR p.nav = :nav) " +
                   "AND (:pa IS NULL OR p.paEmittente = :pa)")
    List<Position> findByTokenAndOptionalNavAndPa(@Param("token") String token, @Param("nav") String nav, @Param("pa") String pa);

    @Query(value = "SELECT DISTINCT p FROM Position p, PositionTokens pt " +
                   "WHERE pt.fkPosition = p.id " +
                   "AND pt.idCarrello = :idCart " +
                   "AND (:nav IS NULL OR p.nav = :nav) " +
                   "AND (:pa IS NULL OR p.paEmittente = :pa)")
    List<Position> findByCartAndOptionalNavAndPa(@Param("idCart") String idCart, @Param("nav") String nav, @Param("pa") String pa);

    @Query(value = "SELECT DISTINCT p FROM Position p, PositionTokens pt, ExtraInfo ei " +
                   "WHERE pt.fkPosition = p.id " +
                   "AND ei.fkToken = pt.id " +
                   "AND ei.infoName = :infoName " +
                   "AND ei.infoValue = :infoValue " +
                   "AND (:nav IS NULL OR p.nav = :nav) " +
                   "AND (:pa IS NULL OR p.paEmittente = :pa)")
    List<Position> findByExtraAndOptionalNavAndPa(@Param("infoName") String infoName, @Param("infoValue") String infoValue, @Param("nav") String nav, @Param("pa") String pa);

    @Query(value = "SELECT p.nav AS nav, p.paEmittente AS paEmittente, " +
                   "FUNCTION('STRING_AGG', ei.infoName, ',') AS infoMatch " +
                   "FROM Position p, PositionTokens pt, ExtraInfo ei " +
                   "WHERE pt.fkPosition = p.id " +
                   "AND ei.fkToken = pt.id " +
                   "AND ei.infoValue = :searchValue " +
                   "AND (:nav IS NULL OR p.nav = :nav) " +
                   "AND (:pa IS NULL OR p.paEmittente = :pa) " +
                   "GROUP BY p.nav, p.paEmittente")
    List<Object[]> findGroupedByExtraValueAndOptionalNavAndPa(@Param("searchValue") String searchValue, @Param("nav") String nav, @Param("pa") String pa);

    @Query(value = "SELECT p.nav AS nav, p.paEmittente AS paEmittente, p.lastEvent AS lastEvent, " +
                   "pt.iuv AS iuv, pt.creditorRefId AS creditorReferenceId, FUNCTION('ENCODE', pt.token, 'hex') AS tokenHex, " +
                   "pt.dateEvent AS tokenDateEvent, pt.paymentDate AS paymentDate, pt.outcome AS outcome, " +
                   "pt.amount AS amount, pt.fee AS fee, pt.psp AS psp, pt.intermediarioPa AS ptPa, " +
                   "pt.intermediarioPsp AS ptPsp, pt.stazione AS station, pt.canale AS channel, " +
                   "pt.touchpoint AS touchpoint, pt.paymentMethod AS paymentMethod, pt.idCarrello AS idCarrello " +
                   "FROM Position p LEFT JOIN PositionTokens pt ON pt.fkPosition = p.id " +
                   "WHERE p.nav = :nav AND p.paEmittente = :paEmittente " +
                   "ORDER BY pt.paymentDate DESC, pt.dateEvent DESC, pt.id DESC")
    List<Object[]> findPositionDetailRows(@Param("nav") String nav, @Param("paEmittente") String paEmittente);

    @Query(value = "SELECT p.nav AS nav, p.paEmittente AS paEmittente, p.lastEvent AS lastEvent, " +
                   "pt.iuv AS iuv, pt.creditorRefId AS creditorReferenceId, FUNCTION('ENCODE', pt.token, 'hex') AS tokenHex, " +
                   "pt.dateEvent AS tokenDateEvent, pt.paymentDate AS paymentDate, pt.outcome AS outcome, " +
                   "pt.amount AS amount, pt.fee AS fee, pt.psp AS psp, pt.intermediarioPa AS ptPa, " +
                   "pt.intermediarioPsp AS ptPsp, pt.stazione AS station, pt.canale AS channel, " +
                   "pt.touchpoint AS touchpoint, pt.paymentMethod AS paymentMethod, pt.idCarrello AS idCarrello " +
                   "FROM PositionTokens pt, Position p " +
                   "WHERE p.id = pt.fkPosition " +
                   "AND pt.token = FUNCTION('convert_to', :token, 'UTF8') " +
                   "ORDER BY pt.paymentDate DESC, pt.dateEvent DESC, pt.id DESC")
    List<Object[]> findTokenDetailRow(@Param("token") String token);

    @Query(value = "SELECT p.nav AS nav, p.paEmittente AS paEmittente, p.lastEvent AS lastEvent, " +
                    "pt.iuv AS iuv, pt.creditorRefId AS creditorReferenceId, FUNCTION('ENCODE', pt.token, 'hex') AS tokenHex, " +
                    "(SELECT COUNT(ptr2.id) FROM PositionTransfers ptr2 WHERE ptr2.fkToken = pt.id) AS transfersCount, " +
                    "ptr.idTransfer AS idTransfer, ptr.isBollo AS isBollo, " +
                    "ptr.ibanTransfer AS ibanTransfer, ptr.amountTransfer AS amountTransfer, ptr.paTransfer AS paTransfer, " +
                    "ptr.dateEvent AS transferDateEvent " +
                    "FROM Position p, PositionTokens pt, PositionTransfers ptr " +
                    "WHERE p.id = pt.fkPosition " +
                    "AND pt.id = ptr.fkToken " +
                    "AND p.nav = :nav AND p.paEmittente = :paEmittente " +
                    "AND pt.token = FUNCTION('convert_to', :token, 'UTF8') " +
                    "ORDER BY ptr.dateEvent DESC, ptr.id DESC")
     List<Object[]> findTransferDetailRows(@Param("nav") String nav, @Param("paEmittente") String paEmittente, @Param("token") String token);

    @Query(value = "SELECT ew.insertedTimestampReq, ew.tipoEvento, ew.outcomeReq, ew.eventIdReq, ew.faultCode, " +
                    "ew.fkTokens, NULL " +
                    "FROM EventsWf ew, Position p " +
                    "WHERE ew.fkPosition = p.id " +
                    "AND p.nav = :nav AND p.paEmittente = :paEmittente AND ew.fkTokens IS NULL " +
                    "ORDER BY ew.insertedTimestampReq DESC")
    List<Object[]> findEventsPositionByNavAndPa(@Param("nav") String nav, @Param("paEmittente") String paEmittente);

    @Query(value = "SELECT ew.insertedTimestampReq, ew.tipoEvento, ew.outcomeReq, ew.eventIdReq, ew.faultCode, " +
                    "FUNCTION('ENCODE', pt.token, 'hex') AS token " +
                    "FROM EventsWf ew, PositionTokens pt, Position p " +
                    "WHERE ew.fkTokens = pt.id " +
                    "AND ew.fkPosition = p.id " +
                    "AND p.nav = :nav AND p.paEmittente = :paEmittente AND ew.fkTokens IS NOT NULL " +
                    "ORDER BY ew.insertedTimestampReq DESC")
    List<Object[]> findEventsTokenByNavAndPa(@Param("nav") String nav, @Param("paEmittente") String paEmittente);

    @Query(value = "SELECT p.nav AS nav, p.paEmittente AS paEmittente, FUNCTION('ENCODE', pt.token, 'hex') AS token, " +
                    "ei.infoName AS infoName, ei.infoValue AS infoValue, " +
                    "(SELECT MAX(ew2.tipoEvento) FROM EventsWf ew2 WHERE ew2.fkTokens = pt.id) AS tipoEvento " +
                    "FROM PositionTokens pt, Position p, ExtraInfo ei " +
                    "WHERE p.id = pt.fkPosition " +
                    "AND ei.fkToken = pt.id " +
                    "AND pt.token = FUNCTION('convert_to', :token, 'UTF8') " +
                    "ORDER BY ei.infoName")
    List<Object[]> findExtraInfoByToken(@Param("token") String token);
}
