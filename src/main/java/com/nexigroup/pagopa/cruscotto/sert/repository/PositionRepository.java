package com.nexigroup.pagopa.cruscotto.sert.repository;

import com.nexigroup.pagopa.cruscotto.sert.domain.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface PositionRepository extends JpaRepository<Position, Integer> {

    /**
     * Partizione in cui vive la riga canonica di {@code POSITION_TOKENS} per il token indicato.
     *
     * <p><strong>Perche' serve.</strong> {@code POSITION_TOKENS} e' RANGE-partizionata per mese su
     * {@code DATE_EVENT}, e in PostgreSQL <em>non esistono indici globali</em> su tabella
     * partizionata: {@code CREATE INDEX} sul parent crea N indici locali, uno per partizione. Una
     * ricerca per token senza predicato sulla chiave di partizionamento non fa quindi <em>una</em>
     * discesa b-tree, ne fa <em>una per partizione</em> — 6 sui 6 mesi online, 36 su 36 mesi.
     *
     * <p>{@code POSITION_TOKEN_REGISTRY} e' <strong>non partizionata</strong> e ha la PK su
     * {@code TOKEN}: leggerla per prima trasforma la scansione di tutte le partizioni in una PK seek
     * piu' una seek potata. E' lo stesso meccanismo gia' in esercizio nell'ingestion
     * ({@code CanonicalTokenResolver}), su cui l'invariante si regge: l'INSERT in
     * {@code POSITION_TOKENS} e' condizionato all'inserimento nel registry nella stessa istruzione
     * ({@code WHERE EXISTS (SELECT 1 FROM token_registry_insert)}), quindi esiste <em>una sola</em>
     * riga per valore di token, nella partizione di {@code FIRST_DATE_EVENT}.
     *
     * <p><strong>Il risultato non e' garantito completo e il chiamante DEVE prevedere il
     * fallback.</strong> Due casi noti: (1) le righe anteriori all'introduzione del registry, il cui
     * backfill ha preso {@code MIN(DATE_EVENT)} per token — eventuali duplicati storici in
     * partizioni successive non sarebbero raggiunti; (2) {@code TokenRegistryCleanupService}
     * nell'ingestor cancella le righe di registry piu' vecchie di una soglia, quindi su uno storico
     * lungo un token legittimo puo' non esserci piu'. In entrambi i casi si ricade sulla query non
     * potata, che e' il comportamento odierno.
     *
     * <p>Il {@code TOKEN} e' {@code BYTEA} ma il contenuto e' testo: l'ingestion scrive
     * {@code getBytes(UTF_8)}, quindi il confronto usa {@code convert_to(..., 'UTF8')} come tutte le
     * altre query di questo repository.
     *
     * <p><strong>Perche' torna una String e non una {@code LocalDate}.</strong> E' una native query
     * senza result mapping: per una colonna {@code DATE} Hibernate restituisce il tipo JDBC
     * ({@code java.sql.Date}) e la conversione verso {@code LocalDate} finirebbe nel
     * ConversionService di Spring, che per {@code java.util.Date} passa da {@code toInstant()} —
     * metodo che {@code java.sql.Date} <strong>sovrascrive lanciando
     * UnsupportedOperationException</strong>. A seconda della versione si otterrebbe un valore
     * corretto o un'eccezione a runtime sulla prima ricerca per token. Il {@code to_char} elimina
     * l'ambiguita': il driver restituisce {@code text}, il mapping e' su {@code String} e il
     * parsing e' esplicito e testabile ({@code SertServiceImpl#resolveTokenPartition}).</p>
     *
     * @param token il token in forma testuale, gia' trimmato
     * @return la {@code DATE_EVENT} della partizione in formato ISO {@code yyyy-MM-dd},
     *         vuoto se il token non e' nel registry
     */
    @Query(value = "SELECT to_char(r.first_date_event, 'YYYY-MM-DD') "
        + "FROM sert_ingestor.position_token_registry r "
        + "WHERE r.token = convert_to(:token, 'UTF8')", nativeQuery = true)
    Optional<String> findTokenPartitionDate(@Param("token") String token);

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
        "AND (:dateEvent IS NULL OR pt.dateEvent = :dateEvent) " +
        "GROUP BY p.nav, p.paEmittente ",
        countQuery = "SELECT COUNT(DISTINCT p.id) FROM Position p, PositionTokens pt " +
            "WHERE pt.fkPosition = p.id " +
            "AND pt.token = FUNCTION('convert_to', :token, 'UTF8') " +
            "AND (:nav IS NULL OR p.nav = :nav) " +
            "AND (:pa IS NULL OR p.paEmittente = :pa) " +
            "AND (:dateEvent IS NULL OR pt.dateEvent = :dateEvent)")
    Page<Object[]> findByTokenAndOptionalNavAndPa(@Param("token") String token, @Param("nav") String nav,
                                                  @Param("pa") String pa, @Param("dateEvent") LocalDate dateEvent,
                                                  Pageable pageable);

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

    /**
     * Ricerca per valore di EXTRA_INFO (RRN, transactionId, TID, ...).
     *
     * <p>Le due CTE sono referenziate <strong>una sola volta</strong>, quindi da PostgreSQL 12 il
     * default e' l'inlining. L'{@code AS MATERIALIZED} che era qui lo sopprimeva esplicitamente,
     * alzando un recinto che impediva al planner di spingere {@code nav} / {@code pa} verso le
     * scansioni: i due filtri si applicavano <em>dopo</em> la materializzazione dell'intero join
     * {@code extra_info x position_tokens}. Rimuoverlo non cambia il result set — l'inlining di una
     * CTE e' una trasformazione definita come equivalente — e restituisce al planner la scelta.</p>
     *
     * <p>NOTA: {@code info_name} non e' filtrato, quindi il valore cercato viene confrontato con
     * ogni {@code info_value} di ogni partizione. Nessun predicato su {@code date_event}: la query
     * non pota. Entrambe le cose sono fuori dal perimetro di questa modifica.</p>
     */
    @Query(
        value = """
            WITH filtered AS (
                          SELECT
                              ei.fk_token,
                              ei.info_name
                          FROM sert_ingestor.extra_info ei
                          WHERE ei.info_value = :searchValue
                      ),
                      token_position AS (
                          SELECT
                              pt.fk_position,
                              f.info_name
                          FROM filtered f
                          JOIN sert_ingestor.position_tokens pt
                              ON pt.id = f.fk_token
                      )
                      SELECT
                          p.nav AS nav,
                          ape.description AS paEmittenteDesc,
                          string_agg(tp.info_name, ',') AS infoMatch,
                          p.pa_emittente AS paEmittente
                      FROM token_position tp
                      JOIN sert_ingestor.position p
                          ON p.id = tp.fk_position
                      LEFT JOIN sert_ingestor.anag_pa_emittente ape
                          ON ape.codice = p.pa_emittente
                      WHERE (:nav IS NULL OR p.nav = :nav)
                        AND (:pa IS NULL OR p.pa_emittente = :pa)
                      GROUP BY
                          p.nav,
                          p.pa_emittente,
                          ape.description,
                          p.pa_emittente
                      ORDER BY ape.description DESC;
        """,
        nativeQuery = true
    )
    List<Object[]> findGroupedByExtraValueAndOptionalNavAndPa(
        @Param("searchValue") String searchValue,
        @Param("nav") String nav,
        @Param("pa") String pa
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

    /**
     * Riga di dettaglio del token, la piu' recente per {@code paymentDate}.
     *
     * <p><strong>{@code pageable}</strong>: il chiamante usa solo la prima riga, ma la query girava
     * senza alcun {@code LIMIT} e il driver materializzava l'intero result set. Passando
     * {@code PageRequest.of(0, 1)} il taglio avviene sul server. L'{@code ORDER BY} e' gia' nella
     * query, quindi la riga selezionata e' la stessa di prima: tutto cio' che veniva scartato lato
     * applicativo ora non viene nemmeno letto.</p>
     *
     * <p><strong>{@code dateEvent}</strong>: quando valorizzato pota {@code POSITION_TOKENS} a una
     * sola partizione mensile. Il valore arriva da {@code POSITION_TOKEN_REGISTRY}, che e' la mappa
     * TOKEN -&gt; partizione. Quando e' {@code null} il predicato sparisce dal piano e il
     * comportamento e' identico a prima: {@code prepareThreshold=0} forza un custom plan, quindi
     * {@code :dateEvent IS NULL} viene valutato come costante in fase di planning.</p>
     */
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
        "AND (:dateEvent IS NULL OR pt.dateEvent = :dateEvent) " +
        "ORDER BY pt.paymentDate DESC, pt.dateEvent DESC, pt.id DESC")
    List<Object[]> findTokenDetailRow(@Param("token") String token,
                                      @Param("dateEvent") LocalDate dateEvent,
                                      Pageable pageable);

    /**
     * Versamenti del token, con il totale dei versamenti dello stesso token su ogni riga.
     *
     * <p><strong>{@code transfersCount}</strong> era una subquery scalare correlata
     * ({@code SELECT COUNT(ptr2.id) ... WHERE ptr2.fkToken = pt.id}) rieseguita <em>per ogni riga
     * emessa</em>, e priva di predicato su {@code date_event}: ogni esecuzione sondava tutte le
     * partizioni mensili di {@code POSITION_TRANSFERS}. La window function calcola lo stesso numero
     * con una sola scansione.</p>
     *
     * <p>I due conteggi coincidono perche' {@code JOIN PositionTransfers ptr ON ptr.fkToken = pt.id}
     * non porta predicati aggiuntivi — le righe del result set per un dato {@code pt.id} sono
     * esattamente i versamenti di quel token — e perche' le window function sono valutate
     * <strong>prima</strong> di {@code LIMIT}, quindi la paginazione non le altera.</p>
     *
     * <p><strong>NON COPERTA DA TEST.</strong> La suite di questo repo e' interamente a mock, quindi
     * {@code transfersCount} arriva dallo stub e non dal database: nessun test puo' accorgersi di una
     * regressione su questo SQL. Se un domani si aggiungesse un filtro su {@code ptr}, i due valori
     * divergerebbero in silenzio. Verifica manuale: su un token con piu' versamenti, confrontare
     * {@code transfersCount} con il numero di righe restituite chiedendo una pagina abbastanza
     * grande da contenerle tutte.</p>
     *
     * <p>Il {@code countQuery} diventa esplicito: con una window function nella proiezione la
     * derivazione automatica di Spring Data non e' affidabile. Conta le stesse righe del
     * {@code count(p)} derivato — {@code ptr.id} non e' mai null su una inner join.</p>
     */
    @Query(value = "SELECT p.nav AS nav, ape.description AS paEmittente, p.lastEvent AS lastEvent, " +
        "pt.iuv AS iuv, pt.creditorRefId AS creditorReferenceId, FUNCTION('ENCODE', pt.token, 'hex') AS tokenHex, " +
        "COUNT(ptr.id) OVER (PARTITION BY pt.id) AS transfersCount, " +
        "ptr.idTransfer AS idTransfer, ptr.isBollo AS isBollo, " +
        "ptr.ibanTransfer AS ibanTransfer, ptr.amountTransfer AS amountTransfer, ptr.paTransfer AS paTransfer, " +
        "ptr.dateEvent AS transferDateEvent " +
        "FROM Position p " +
        "JOIN PositionTokens pt ON pt.fkPosition = p.id " +
        "JOIN PositionTransfers ptr ON ptr.fkToken = pt.id " +
        "LEFT JOIN AnagPaEmittente ape ON ape.codice = p.paEmittente " +
        "WHERE p.nav = :nav AND p.paEmittente = :paEmittente " +
        "AND pt.token = FUNCTION('convert_to', :token, 'UTF8') " +
        "AND (:dateEvent IS NULL OR pt.dateEvent = :dateEvent) ",
        countQuery = "SELECT COUNT(ptr.id) " +
            "FROM Position p " +
            "JOIN PositionTokens pt ON pt.fkPosition = p.id " +
            "JOIN PositionTransfers ptr ON ptr.fkToken = pt.id " +
            "WHERE p.nav = :nav AND p.paEmittente = :paEmittente " +
            "AND pt.token = FUNCTION('convert_to', :token, 'UTF8') " +
            "AND (:dateEvent IS NULL OR pt.dateEvent = :dateEvent) ")
    Page<Object[]> findTransferDetailRows(@Param("nav") String nav, @Param("paEmittente") String paEmittente,
                                          @Param("token") String token, @Param("dateEvent") LocalDate dateEvent,
                                          Pageable pageable);

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
        // Il countQuery era una UNION ALL di due rami IDENTICI: il risultato e' per costruzione il
        // doppio di un ramo solo, ottenuto scandendo due volte EVENTS_WF — che e' partizionata PER
        // GIORNO, quindi ~183 partizioni su 6 mesi e ~1.096 su 36. Una scansione sola moltiplicata
        // per 2 da' lo stesso numero.
        //
        // COUNT(p.id) e non COUNT(ew.id): il LEFT JOIN produce una riga anche per le posizioni
        // senza eventi, e il COUNT(*) sulla derivata la contava. p e' la tabella guida, quindi
        // p.id non e' mai null e il conteggio resta identico.
        //
        // NON COPERTO DA TEST: la suite di questo repo e' interamente a mock, quindi il count
        // arriva dallo stub e non dal database. Verifica manuale obbligatoria prima del rilascio:
        // confrontare il totale di /workflows prima e dopo, su una posizione con eventi noti E su
        // una posizione senza eventi (e' li' che COUNT(*) e COUNT(ew.id) divergerebbero).
        countQuery =
            "SELECT COUNT(p.id) * 2 " +
                "FROM Position p " +
                "LEFT JOIN EventsWf ew ON ew.fkPosition = p.id " +
                "WHERE p.nav = :nav AND p.paEmittente = :paEmittente "
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
        "WHERE pt.token = FUNCTION('convert_to', :token, 'UTF8') " +
        "AND (:dateEvent IS NULL OR pt.dateEvent = :dateEvent) ")
    Page<Object[]> findExtraInfoByToken(@Param("token") String token, @Param("dateEvent") LocalDate dateEvent,
                                        Pageable pageable);

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
