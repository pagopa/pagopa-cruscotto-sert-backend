package com.nexigroup.pagopa.cruscotto.sert.service.impl;

import static com.nexigroup.pagopa.cruscotto.sert.service.util.PaymentUtil.asString;
import static com.nexigroup.pagopa.cruscotto.sert.service.util.PaymentUtil.parseInfoMatch;
import static com.nexigroup.pagopa.cruscotto.sert.service.util.PaymentUtil.tokenAsHex;
import static com.nexigroup.pagopa.cruscotto.sert.service.util.PaymentUtil.toDouble;
import static com.nexigroup.pagopa.cruscotto.sert.service.util.PaymentUtil.toInstant;
import static com.nexigroup.pagopa.cruscotto.sert.service.util.PaymentUtil.toInstantFromDate;

import com.nexigroup.pagopa.cruscotto.sert.domain.Position;
import com.nexigroup.pagopa.cruscotto.sert.repository.PositionRepository;
import com.nexigroup.pagopa.cruscotto.sert.service.SertService;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.nexigroup.pagopa.cruscotto.sert.service.util.PageCustomImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service Implementation for SERT APIs with Mock Data.
 */
@Service
public class SertServiceImpl implements SertService {

    private final Logger log = LoggerFactory.getLogger(SertServiceImpl.class);

    /**
     * Il dettaglio del token usa una riga sola: l'ORDER BY e' nella query, quindi il taglio a 1
     * avviene sul server invece che scartando in memoria tutto cio' che segue.
     */
    private static final Pageable FIRST_ROW_ONLY = PageRequest.of(0, 1);

    private final PositionRepository positionRepository;

    public SertServiceImpl(PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }

    /**
     * Esegue una ricerca per token prima <em>potata</em> sulla partizione indicata dal registry e,
     * solo se quella non produce nulla, nella forma non potata.
     *
     * <p>Il fallback non e' difensivo, e' necessario: il registry puo' non contenere il token
     * (righe anteriori alla sua introduzione, oppure riga cancellata dalla retention del registry
     * nell'ingestor) e, per i duplicati storici, punta alla sola occorrenza piu' vecchia. Senza
     * fallback quei casi passerebbero da "trovato" a "non trovato" — un cambio di comportamento
     * silenzioso e visibile all'utente.</p>
     *
     * <p>Costo nel caso peggiore (risultato vuoto): una seek sulla PK del registry piu' una query
     * potata a una partizione, entrambe trascurabili rispetto alla scansione non potata che viene
     * eseguita comunque. Non e' quindi mai materialmente peggiore di oggi, ed e' molto meglio
     * quando il token viene trovato.</p>
     *
     * @param token  token gia' normalizzato
     * @param query  la query da eseguire, parametrizzata sulla partizione ({@code null} = non potata)
     * @param empty  come si riconosce un risultato vuoto per il tipo restituito
     */
    private <T> T pruneByTokenPartition(String token, Function<LocalDate, T> query, Predicate<T> empty) {
        LocalDate partitionDate = resolveTokenPartition(token);
        if (partitionDate != null) {
            T pruned = query.apply(partitionDate);
            if (!empty.test(pruned)) {
                return pruned;
            }
            log.debug("Registry hit but no rows in the pruned partition, falling back to the full scan");
        }
        return query.apply(null);
    }

    /**
     * Legge dal registry la partizione del token, degradando a {@code null} (= nessun pruning) su
     * qualunque problema.
     *
     * <p><strong>Il catch non e' difensivismo generico, copre un rischio concreto</strong>: questo
     * servizio legge {@code POSITION_TOKEN_REGISTRY} per la prima volta, e la tabella appartiene
     * allo schema dell'ingestor. Se al ruolo applicativo mancasse la SELECT su quella tabella —
     * oppure se la tabella non ci fosse in un ambiente non allineato — senza questo guard
     * <em>quattro</em> endpoint che oggi funzionano comincerebbero a restituire errore. Cosi'
     * invece tornano semplicemente al comportamento attuale, piu' lento ma corretto.</p>
     *
     * <p>Il valore arriva come stringa ISO: vedi {@code PositionRepository#findTokenPartitionDate}
     * per il motivo. Un valore non parsabile e' impossibile da una colonna {@code DATE}, ma anche
     * quel caso degrada invece di propagare.</p>
     */
    private LocalDate resolveTokenPartition(String token) {
        try {
            return positionRepository.findTokenPartitionDate(token).map(LocalDate::parse).orElse(null);
        } catch (RuntimeException exception) {
            // WARN e non ERROR: la ricerca non fallisce, perde solo il partition pruning. Ma va
            // visto, perche' se ricorre tutte le ricerche per token stanno girando non potate.
            log.warn("Could not read the token registry, the search runs without partition pruning", exception);
            return null;
        }
    }


    @Override
    public Page<PositionPaymentExtraDTO> searchByNav(String nav, String pa, Pageable pageable) {
        log.debug("Request to search by NAV: {}, PA: {}, offset: {}, limit: {}", nav, pa, pageable.getOffset(), pageable.getPageSize());

        Page<Object[]> groupRow = positionRepository.findByNavOrPa(nav, pa, pageable);
        if (groupRow == null || groupRow.isEmpty()) {
            return Page.empty(pageable);
        }
        return groupRow.map(row -> PositionPaymentExtraDTO.builder()
            .nav(asString(row[0]))
            .paEmittente(asString(row[1]))
            .build());
    }

    @Override
    public Page<PositionPaymentExtraDTO> searchByIuv(String pa, String nav, String iuv,Pageable pageable) {
        log.debug("Request to search by IUV: {}, PA: {}, NAV: {}, offset: {}, limit: {}", iuv, pa, nav, pageable.getOffset(), pageable.getPageSize());

        Page<Object[]> groupRow = positionRepository.findByIuvAndOptionalNavAndPa(iuv, nav, pa, pageable);

        return groupRow.map(row -> PositionPaymentExtraDTO.builder()
            .nav(asString(row[0]))
            .paEmittente(asString(row[1]))
            .build());
    }

    @Override
    public Page<PositionPaymentExtraDTO> searchByCart(String pa, String nav, String idCart, Pageable pageable) {
        log.debug("Request to search by Cart: {}, offset: {}, limit: {}", idCart, pageable.getOffset(), pageable.getPageSize());

        Page<Object[]> groupRow = positionRepository.findByCartAndOptionalNavAndPa(idCart, nav, pa, pageable);

        return groupRow.map(row -> PositionPaymentExtraDTO.builder()
            .nav(asString(row[0]))
            .paEmittente(asString(row[1]))
            .build());
    }

    @Override
    public Page<PositionPaymentExtraDTO> searchByToken( String pa, String nav,String token, Pageable pageable) {
        log.debug("Request to search by Token: {}, offset: {}, limit: {}", token, pageable.getOffset(), pageable.getPageSize());

        if (token == null || token.trim().isEmpty()) {
            return Page.empty(pageable);
        }

        String normalizedToken = token.trim();

        Page<Object[]> groupRow = pruneByTokenPartition(
            normalizedToken,
            dateEvent -> positionRepository.findByTokenAndOptionalNavAndPa(normalizedToken, nav, pa, dateEvent, pageable),
            page -> page == null || page.isEmpty());

        return groupRow.map(row -> PositionPaymentExtraDTO.builder()
            .nav(asString(row[0]))
            .paEmittente(asString(row[1]))
            .build());
    }

    @Override
    public Page<PositionPaymentExtraDTO> searchExtra(String pa, String nav, String searchValue, Pageable pageable) {
        log.debug("Request to search extra: {}, offset: {}, limit: {}", searchValue, pageable.getOffset(), pageable.getPageSize());

        if (searchValue == null) {
            return Page.empty(pageable);
        }

        long startedAtNanos = System.nanoTime();
        List<Object[]> groupedRows = positionRepository.findGroupedByExtraValueAndOptionalNavAndPa(
            searchValue,
            nav,
            pa
        );
        // Una sola milestone al posto della coppia START/END che c'era qui.
        //
        // 1) Il log di avvio non aggiungeva informazione: se la query non torna, lo si deduce
        //    dall'assenza del log finale. Durata e numero di righe dicono invece quanto costa
        //    davvero questa ricerca, che e' l'unica del servizio senza LIMIT lato database —
        //    cioe' esattamente il dato che serve per decidere se va resa asincrona.
        // 2) Il valore cercato NON viene piu' emesso a INFO. E' testo libero dell'utente, che puo'
        //    contenere qualunque cosa (un codice fiscale, per dire), e finiva in chiaro nei log di
        //    produzione due volte per ricerca. Resta nel log.debug di inizio metodo, che in
        //    produzione non e' attivo.
        log.info("Extra-info search completed: rows={}, elapsedMs={}",
            groupedRows.size(), TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAtNanos));

        List<PositionPaymentExtraDTO> allRows = new ArrayList<>(groupedRows.stream()
            .map(row -> PositionPaymentExtraDTO.builder()
                .nav((String) row[0])
                .paEmittenteDsc((String) row[1])
                .match(parseInfoMatch(row[2]))
                .paEmittente((String) row[3])
                .build())
            .toList());

        if (allRows.isEmpty() || pageable.getOffset() >= allRows.size()) {
            return new PageImpl<PositionPaymentExtraDTO>(Collections.emptyList(), pageable, allRows.size());
        }

        allRows.sort((left, right) -> {
            for (Sort.Order order : pageable.getSort()) {
                String leftValue = extraSortValue(left, order.getProperty());
                String rightValue = extraSortValue(right, order.getProperty());
                int comparison = Comparator.nullsLast(String::compareTo).compare(leftValue, rightValue);
                if (comparison != 0) {
                    return order.isAscending() ? comparison : -comparison;
                }
            }
            return 0;
        });



        int fromIndex = (int) pageable.getOffset();
        int toIndex = Math.min(fromIndex + pageable.getPageSize(), allRows.size());
        List<PositionPaymentExtraDTO> pageContent = allRows.subList(fromIndex, toIndex);

        return new PageImpl<PositionPaymentExtraDTO>(pageContent, pageable, allRows.size());
    }

    private String extraSortValue(PositionPaymentExtraDTO row, String property) {
        return switch (property) {
            case "nav" -> row.getNav();
            case "pa-emittente", "paEmittente" -> row.getPaEmittente();
            case "pa-emittente-desc", "paEmittenteDesc" -> row.getPaEmittenteDsc();
            default -> null;
        };
    }

    @Override
    public Page<PositionPaymentDTO> getPosition(String nav, String paEmittente, Pageable pageable) {
        log.debug("Request to get position: {}, {}", nav, paEmittente);

        Page<Object[]> rows = positionRepository.findPositionDetailRows(nav, paEmittente, pageable);
        if (rows == null || rows.isEmpty()) {
            return Page.empty(pageable);
        }

        Object[] firstRow = rows.getContent().get(0);
        Object[] preferredRow = rows.stream().filter(row -> row[5] != null).findFirst().orElse(firstRow);
        Object[] payedRow = rows.stream().filter(row -> row[7] != null && row[5] != null).findFirst().orElse(null);

        List<String> allTokens = rows.stream()
            .map(row -> tokenAsHex(row[5]))
            .filter(Objects::nonNull)
            .collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new), List::copyOf));

        // multiOutcome = true se ci sono piu' token con outcome=OK per la stessa posizione (nav, pa)
        long okTokenCount = positionRepository.countOkTokensByNavAndPa(nav, paEmittente);
        boolean multiOutcome = okTokenCount > 1;

        PositionPaymentDTO dto = PositionPaymentDTO.builder()
            .positionInfo(PositionPaymentInfoDTO.builder()
                .nav(asString(firstRow[0]))
                .paEmittente(asString(firstRow[1]))
                .iuv(asString(preferredRow[3]))
                .creditorReferenceId(asString(preferredRow[4]))
                .lastEvent(toInstant(firstRow[2]))
                .isCached(false)
                .build())
            .tokens(allTokens==null || allTokens.isEmpty()? 0L: rows.getTotalElements())
            .allTokens(allTokens)
            .payed(
                payedRow == null
                    ? null
                    : PayedDTO.builder()
                        .token(tokenAsHex(payedRow[5]))
                        .paymentBorn(toInstantFromDate(payedRow[6]))
                        .payedDate(toInstant(payedRow[7]))
                        .multiOutcome(multiOutcome)
                        .build()
            )
            .actors(ActorsDTO.builder()
                .psp(asString(preferredRow[11]))
                .pspDescription(asString(preferredRow[19]))
                .ptPa(asString(preferredRow[12]))
                .ptPaDescription(asString(preferredRow[20]))
                .ptPsp(asString(preferredRow[13]))
                .ptPspDescription(asString(preferredRow[21]))
                .station(asString(preferredRow[14]))
                .channel(asString(preferredRow[15]))
                .build())
            .amount(AmountDTO.builder()
                .amount(toDouble(preferredRow[9]))
                .fee(toDouble(preferredRow[10]))
                .build())
            .paymentInfo(PaymentInfoDTO.builder()
                .touchpoint(asString(preferredRow[16]))
                .paymentMethod(asString(preferredRow[17]))
                .isCart(rows.stream().anyMatch(r -> r[18] != null))  // true se almeno un token ha idCarrello != null
                .isGpd(false)
                .isStandin(false)
                .isDw(nav.startsWith("351"))
                .build())
            .build();

        return new PageCustomImpl<>(Collections.singletonList(dto), pageable, allTokens==null || allTokens.isEmpty()? 0L: rows.getTotalElements());
    }


    @Override
    public TokenInfoDTO getTokenInfo(String token) {
        log.debug("Request to get token info: {}", token);

        if (token == null || token.trim().isEmpty()) {
            return null;
        }

        String normalizedToken = token.trim();

        List<Object[]> rows = pruneByTokenPartition(
            normalizedToken,
            dateEvent -> positionRepository.findTokenDetailRow(normalizedToken, dateEvent, FIRST_ROW_ONLY),
            list -> list == null || list.isEmpty());
        if (rows == null || rows.isEmpty()) {
            return null;
        }

        Object[] row = rows.get(0);
        Boolean isPayed = row[8] != null && "OK".equalsIgnoreCase(row[8].toString());

        // multiOutcome = true se ci sono piu' token con outcome=OK per la stessa posizione (nav, pa)
        String rowNav = asString(row[0]);
        String rowPa  = asString(row[1]);
        boolean multiOutcome = false;
        if (rowNav != null && rowPa != null) {
            long okTokenCount = positionRepository.countOkTokensByNavAndPa(rowNav, rowPa);
            multiOutcome = okTokenCount > 1;
        }

        return TokenInfoDTO.builder()
            .positionInfo(PositionPaymentInfoDTO.builder()
                .nav(rowNav)
                .paEmittente(rowPa)
                .iuv(asString(row[3]))
                .creditorReferenceId(asString(row[4]))
                .lastEvent(toInstant(row[2]))
                .isCached(false)
                .build())
            .isPayedToken(isPayed)
            .payed(
                !isPayed
                    ? null
                    : PayedDTO.builder()
                        .token(tokenAsHex(row[5]))
                        .paymentBorn(toInstantFromDate(row[6]))
                        .payedDate(toInstant(row[7]))
                        .multiOutcome(multiOutcome)
                        .build()
            )
            .actors(ActorsDTO.builder()
                .psp(asString(row[11]))
                .pspDescription(asString(row[19]))
                .ptPa(asString(row[12]))
                .ptPaDescription(asString(row[20]))
                .ptPsp(asString(row[13]))
                .ptPspDescription(asString(row[21]))
                .station(asString(row[14]))
                .channel(asString(row[15]))
                .build())
            .amount(AmountDTO.builder()
                .amount(toDouble(row[9]))
                .fee(toDouble(row[10]))
                .build())
            .paymentInfo(PaymentInfoDTO.builder()
                .touchpoint(asString(row[16]))
                .paymentMethod(asString(row[17]))
                .isCart(row[18] != null)  // true se idCarrello != null
                .isGpd(false)
                .isStandin(false)
                .isDw(rowNav != null && rowNav.startsWith("351"))
                .build())
            .build();
    }

    @Override
    public Page<TransferPaymentDTO> getTransfers(String nav, String paEmittente, String token, Pageable pageable) {
        log.debug("Request to get transfers for: {}, {}, {}, {}, {}", nav, paEmittente, token,pageable.getOffset(), pageable.getPageSize() );

        if (nav == null || paEmittente == null || token == null || token.trim().isEmpty()) {
            return null;
        }

        String normalizedToken = token.trim();

        Page<Object[]> rows = pruneByTokenPartition(
            normalizedToken,
            dateEvent -> positionRepository.findTransferDetailRows(nav, paEmittente, normalizedToken, dateEvent, pageable),
            page -> page == null || page.isEmpty());
        if (rows == null || rows.isEmpty()) {
            return null;
        }

        Object[] latestRow = rows.getContent().get(0);

        List<TransferObjectDTO> transfers = rows.stream()
            .map(this::toTransferObject)
            .collect(Collectors.toList());

        TransferPaymentDTO dto = TransferPaymentDTO.builder()
            .positionInfo(PositionPaymentInfoDTO.builder()
                .nav(asString(latestRow[0]))
                .paEmittente(asString(latestRow[1]))
                .iuv(asString(latestRow[3]))
                .creditorReferenceId(asString(latestRow[4]))
                .lastEvent(toInstant(latestRow[2]))
                .isCached(false)
                .build())
            .token(tokenAsHex(latestRow[5]))
            .transfersCount(toDouble(latestRow[6]))
            .transfers(transfers)
            .build();

        return new PageCustomImpl<>(Collections.singletonList(dto), pageable, rows.getTotalElements());
    }

    private TransferObjectDTO toTransferObject(Object[] row) {
        return TransferObjectDTO.builder()
            .idTransfer(row[7] == null ? null : Integer.valueOf(asString(row[7])))
            .typeTransfer(Boolean.TRUE.equals(row[8]) ? "bollo" : "sepa")
            .iban(asString(row[9]))
            .amount(toDouble(row[10]))
            .paFiscalCode(asString(row[11]))
            .build();
    }

    @Override
    public Page<WorkflowResponseDTO> getWorkflows(String nav, String paEmittente, Pageable pageable) {
        log.debug("Request to get workflows for: {}, {}", nav, paEmittente);

        Page<Object[]> workflowEvents = positionRepository.findPositionWorkflows(nav, paEmittente, pageable);

        if (workflowEvents == null || workflowEvents.isEmpty()) {
            return null;
        }

        List<WorkflowObjectDTO> eventsPositionList = new java.util.ArrayList<>();

        List<Object[]> rows = workflowEvents.getContent();
        int offset = (int) pageable.getOffset();
        for (int i = 0; i < rows.size(); i++) {
            Object[] row = rows.get(i);
            // In case of a LEFT JOIN where there are no events at all, all event fields would be null.
            if (row[0] == null && row[4] == null) {
                continue;
            }

            String token = tokenAsHex(row[6]);
            String faultcode =  (row[5] != null ? String.valueOf(row[5]) : null);
            if ((asString(row[2]).equals("REQ/RESP"))&& asString(row[7]).equals("REQ")){
                faultcode=null;
            }

            WorkflowTokenObjectDTO tokenDTO = WorkflowTokenObjectDTO.builder()
                .insertedtimestamp(toInstant(row[0]))
                .tipoevento(asString(row[1]))
                .sottotipoevento(asString(row[2]).equals("REQ/RESP")?asString(row[7]):asString(row[2]))
                .outcome(asString(row[3]))
                .eventId(asString(row[4]))
                .faultcode( faultcode)
                .token(token)
                .id(asString(row[8]))
                .build();
            eventsPositionList.add(tokenDTO);

        }

        Long totalCount = workflowEvents.getTotalElements();

        WorkflowResponseDTO dto = WorkflowResponseDTO.builder()
            .eventsPosition(eventsPositionList)
            .build();

        return new PageCustomImpl<>(Collections.singletonList(dto), pageable, totalCount);
    }

    @Override
    public Page<ExtraInfoResponseDTO> getExtraInfo(String token, Pageable pageable) {
        log.debug("Request to get extra info for token: {}", token);

        String normalizedToken = token.trim();

        Page<Object[]> rows = pruneByTokenPartition(
            normalizedToken,
            dateEvent -> positionRepository.findExtraInfoByToken(normalizedToken, dateEvent, pageable),
            page -> page == null || page.isEmpty());
        if (rows == null || rows.isEmpty()) {
            return null;
        }

        List<ExtraInfoObjectDTO> results = rows.stream()
            .filter(row -> row[3] != null)
            .map(row -> ExtraInfoObjectDTO.builder()
                .nav(asString(row[0]))
                .paEmittente(asString(row[1]))
                .token(tokenAsHex(row[2]))
                .name(asString(row[3]))
                .value(asString(row[4]))
                .tipoevento(asString(row[5]))
                .build())
            .collect(Collectors.toList());

        ExtraInfoResponseDTO dto = ExtraInfoResponseDTO.builder()
            .count(rows.getTotalElements())
            .results(results)
            .build();
        return new PageCustomImpl<>(Collections.singletonList(dto), pageable, rows.getTotalElements());
    }
}
