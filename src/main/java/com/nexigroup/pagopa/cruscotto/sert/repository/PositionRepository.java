package com.nexigroup.pagopa.cruscotto.sert.repository;

import com.nexigroup.pagopa.cruscotto.sert.domain.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PositionRepository extends JpaRepository<Position, Integer> {

    @Query(value = "SELECT * FROM POSITION WHERE (:nav IS NOT NULL AND NAV = :nav) OR (:pa IS NOT NULL AND PA_EMITTENTE = :pa)", nativeQuery = true)
    List<Position> findByNavOrPa(@Param("nav") String nav, @Param("pa") String pa);

    @Query(value = "SELECT p.* FROM POSITION p JOIN POSITION_TOKENS pt ON p.ID = pt.FK_POSITION " +
                   "WHERE pt.IUV = :iuv " +
                   "AND (:nav IS NULL OR p.NAV = :nav) " +
                   "AND (:pa IS NULL OR p.PA_EMITTENTE = :pa)", nativeQuery = true)
    List<Position> findByIuvAndOptionalNavAndPa(@Param("iuv") String iuv, @Param("nav") String nav, @Param("pa") String pa);

    @Query(value = "SELECT p.* FROM POSITION p JOIN POSITION_TOKENS pt ON p.ID = pt.FK_POSITION " +
                   "WHERE pt.TOKEN = :token " +
                   "AND (:nav IS NULL OR p.NAV = :nav) " +
                   "AND (:pa IS NULL OR p.PA_EMITTENTE = :pa)", nativeQuery = true)
    List<Position> findByTokenAndOptionalNavAndPa(@Param("token") String token, @Param("nav") String nav, @Param("pa") String pa);

    @Query(value = "SELECT p.* FROM POSITION p JOIN POSITION_TOKENS pt ON p.ID = pt.FK_POSITION " +
                   "WHERE pt.ID_CARRELLO = :idCart " +
                   "AND (:nav IS NULL OR p.NAV = :nav) " +
                   "AND (:pa IS NULL OR p.PA_EMITTENTE = :pa)", nativeQuery = true)
    List<Position> findByCartAndOptionalNavAndPa(@Param("idCart") String idCart, @Param("nav") String nav, @Param("pa") String pa);

    @Query(value = "SELECT p.* FROM POSITION p " +
                   "JOIN POSITION_TOKENS pt ON p.ID = pt.FK_POSITION " +
                   "JOIN EXTRA_INFO ei ON pt.ID = ei.FK_TOKEN " +
                   "WHERE ei.INFO_NAME = :infoName AND ei.INFO_VALUE = :infoValue " +
                   "AND (:nav IS NULL OR p.NAV = :nav) " +
                   "AND (:pa IS NULL OR p.PA_EMITTENTE = :pa)", nativeQuery = true)
    List<Position> findByExtraAndOptionalNavAndPa(@Param("infoName") String infoName, @Param("infoValue") String infoValue, @Param("nav") String nav, @Param("pa") String pa);

    @Query(value = "SELECT p.NAV AS nav, p.PA_EMITTENTE AS paEmittente, " +
                   "STRING_AGG(DISTINCT ei.INFO_NAME, ',' ORDER BY ei.INFO_NAME) AS infoMatch " +
                   "FROM POSITION p " +
                   "JOIN POSITION_TOKENS pt ON p.ID = pt.FK_POSITION " +
                   "JOIN EXTRA_INFO ei ON pt.ID = ei.FK_TOKEN " +
                   "WHERE ei.INFO_VALUE = :searchValue " +
                   "AND (:nav IS NULL OR p.NAV = :nav) " +
                   "AND (:pa IS NULL OR p.PA_EMITTENTE = :pa) " +
                   "GROUP BY p.NAV, p.PA_EMITTENTE", nativeQuery = true)
    List<Object[]> findGroupedByExtraValueAndOptionalNavAndPa(@Param("searchValue") String searchValue, @Param("nav") String nav, @Param("pa") String pa);

    @Query(value = "SELECT p.NAV AS nav, p.PA_EMITTENTE AS paEmittente, p.LAST_EVENT AS lastEvent, " +
                   "pt.IUV AS iuv, pt.CREDITOR_REF_ID AS creditorReferenceId, ENCODE(pt.TOKEN, 'hex') AS tokenHex, " +
                   "pt.DATE_EVENT AS tokenDateEvent, pt.PAYMENT_DATE AS paymentDate, pt.OUTCOME AS outcome, " +
                   "pt.AMOUNT AS amount, pt.FEE AS fee, pt.PSP AS psp, pt.INTERMEDIARIO_PA AS ptPa, " +
                   "pt.INTERMEDIARIO_PSP AS ptPsp, pt.STAZIONE AS station, pt.CANALE AS channel, " +
                   "pt.TOUCHPOINT AS touchpoint, pt.PAYMENT_METHOD AS paymentMethod, pt.ID_CARRELLO AS idCarrello " +
                   "FROM POSITION p LEFT JOIN POSITION_TOKENS pt ON p.ID = pt.FK_POSITION " +
                   "WHERE p.NAV = :nav AND p.PA_EMITTENTE = :paEmittente " +
                   "ORDER BY pt.PAYMENT_DATE DESC NULLS LAST, pt.DATE_EVENT DESC NULLS LAST, pt.ID DESC", nativeQuery = true)
    List<Object[]> findPositionDetailRows(@Param("nav") String nav, @Param("paEmittente") String paEmittente);

    @Query(value = "SELECT p.NAV AS nav, p.PA_EMITTENTE AS paEmittente, p.LAST_EVENT AS lastEvent, " +
                   "pt.IUV AS iuv, pt.CREDITOR_REF_ID AS creditorReferenceId, ENCODE(pt.TOKEN, 'hex') AS tokenHex, " +
                   "pt.DATE_EVENT AS tokenDateEvent, pt.PAYMENT_DATE AS paymentDate, pt.OUTCOME AS outcome, " +
                   "pt.AMOUNT AS amount, pt.FEE AS fee, pt.PSP AS psp, pt.INTERMEDIARIO_PA AS ptPa, " +
                   "pt.INTERMEDIARIO_PSP AS ptPsp, pt.STAZIONE AS station, pt.CANALE AS channel, " +
                   "pt.TOUCHPOINT AS touchpoint, pt.PAYMENT_METHOD AS paymentMethod, pt.ID_CARRELLO AS idCarrello " +
                   "FROM POSITION_TOKENS pt " +
                   "JOIN POSITION p ON p.ID = pt.FK_POSITION " +
                   "WHERE LOWER(ENCODE(pt.TOKEN, 'hex')) = LOWER(:token) " +
                   "ORDER BY pt.PAYMENT_DATE DESC NULLS LAST, pt.DATE_EVENT DESC NULLS LAST, pt.ID DESC " +
                   "LIMIT 1", nativeQuery = true)
    List<Object[]> findTokenDetailRow(@Param("token") String token);

    @Query(value = "SELECT p.NAV AS nav, p.PA_EMITTENTE AS paEmittente, p.LAST_EVENT AS lastEvent, " +
                    "pt.IUV AS iuv, pt.CREDITOR_REF_ID AS creditorReferenceId, ENCODE(pt.TOKEN, 'hex') AS tokenHex, " +
                    "COUNT(ptr.ID) OVER() AS transfersCount, ptr.ID_TRANSFER AS idTransfer, ptr.IS_BOLLO AS isBollo, " +
                    "ptr.IBAN_TRANSFER AS ibanTransfer, ptr.AMOUNT_TRANSFER AS amountTransfer, ptr.PA_TRANSFER AS paTransfer, " +
                    "ptr.DATE_EVENT AS transferDateEvent " +
                    "FROM POSITION p " +
                    "JOIN POSITION_TOKENS pt ON p.ID = pt.FK_POSITION " +
                    "JOIN POSITION_TRANSFERS ptr ON pt.ID = ptr.FK_TOKEN " +
                    "WHERE p.NAV = :nav AND p.PA_EMITTENTE = :paEmittente " +
                    "AND LOWER(ENCODE(pt.TOKEN, 'hex')) = LOWER(:token) " +
                    "ORDER BY ptr.DATE_EVENT DESC NULLS LAST, ptr.ID DESC", nativeQuery = true)
     List<Object[]> findTransferDetailRows(@Param("nav") String nav, @Param("paEmittente") String paEmittente, @Param("token") String token);

    @Query(value = "SELECT ew.INSERTED_TIMESTAMP_REQ, ew.TIPO_EVENTO, ew.OUTCOME_REQ, ew.EVENT_ID_REQ, ew.FAULT_CODE, " +
                    "ew.FK_TOKENS, CAST(NULL AS VARCHAR) AS token " +
                    "FROM EVENTS_WF ew " +
                    "JOIN POSITION p ON ew.FK_POSITION = p.ID " +
                    "WHERE p.NAV = :nav AND p.PA_EMITTENTE = :paEmittente AND ew.FK_TOKENS IS NULL " +
                    "ORDER BY ew.INSERTED_TIMESTAMP_REQ DESC NULLS LAST", nativeQuery = true)
    List<Object[]> findEventsPositionByNavAndPa(@Param("nav") String nav, @Param("paEmittente") String paEmittente);

    @Query(value = "SELECT ew.INSERTED_TIMESTAMP_REQ, ew.TIPO_EVENTO, ew.OUTCOME_REQ, ew.EVENT_ID_REQ, ew.FAULT_CODE, " +
                    "ENCODE(pt.TOKEN, 'hex') AS token " +
                    "FROM EVENTS_WF ew " +
                    "JOIN POSITION_TOKENS pt ON ew.FK_TOKENS = pt.ID " +
                    "JOIN POSITION p ON ew.FK_POSITION = p.ID " +
                    "WHERE p.NAV = :nav AND p.PA_EMITTENTE = :paEmittente AND ew.FK_TOKENS IS NOT NULL " +
                    "ORDER BY ew.INSERTED_TIMESTAMP_REQ DESC NULLS LAST", nativeQuery = true)
    List<Object[]> findEventsTokenByNavAndPa(@Param("nav") String nav, @Param("paEmittente") String paEmittente);

    @Query(value = "SELECT p.NAV AS nav, p.PA_EMITTENTE AS paEmittente, ENCODE(pt.TOKEN, 'hex') AS token, " +
                    "ei.INFO_NAME AS infoName, ei.INFO_VALUE AS infoValue, ew.TIPO_EVENTO AS tipoEvento " +
                    "FROM POSITION_TOKENS pt " +
                    "JOIN POSITION p ON p.ID = pt.FK_POSITION " +
                    "LEFT JOIN EXTRA_INFO ei ON pt.ID = ei.FK_TOKEN " +
                    "LEFT JOIN EVENTS_WF ew ON pt.ID = ew.FK_TOKENS AND ew.FK_TOKENS IS NOT NULL " +
                    "WHERE LOWER(ENCODE(pt.TOKEN, 'hex')) = LOWER(:token) " +
                    "ORDER BY ei.INFO_NAME", nativeQuery = true)
    List<Object[]> findExtraInfoByToken(@Param("token") String token);
}
