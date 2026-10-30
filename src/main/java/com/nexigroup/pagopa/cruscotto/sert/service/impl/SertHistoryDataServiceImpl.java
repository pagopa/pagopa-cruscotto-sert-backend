package com.nexigroup.pagopa.cruscotto.sert.service.impl;

import com.nexigroup.pagopa.cruscotto.sert.repository.history.HistoryPositionRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.online.OnlinePositionRepository;
import com.nexigroup.pagopa.cruscotto.sert.service.SertService;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.*;
import com.nexigroup.pagopa.cruscotto.sert.service.util.PageCustomImpl;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import static com.nexigroup.pagopa.cruscotto.sert.service.util.PaymentUtil.*;

@Service
public class SertHistoryDataServiceImpl implements SertService {

    private final Logger log = LoggerFactory.getLogger(SertOnlineServiceImpl.class);


    private final HistoryPositionRepository historyPositionRepository;

    private final OnlinePositionRepository onlinePositionRepository;


    public SertHistoryDataServiceImpl(HistoryPositionRepository historyhistoryPositionRepository, OnlinePositionRepository OnlinePositionRepository) {
        this.historyPositionRepository = historyhistoryPositionRepository;
        this.onlinePositionRepository = OnlinePositionRepository;
    }

    @Override
    public Page<PositionPaymentExtraDTO> searchByNav(String nav, String pa, LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {
        log.debug("Request to search by NAV: {}, PA: {}, offset: {}, limit: {}", nav, pa, pageable.getOffset(), pageable.getPageSize());

        Page<Object[]> groupRow = historyPositionRepository.searchByNav(nav, pa, dateFrom ,dateTo, pageable);
        if (groupRow == null || groupRow.isEmpty()) {
            return Page.empty(pageable);
        }
        return groupRow.map(row -> PositionPaymentExtraDTO.builder()
            .nav(asString(row[0]))
            .paEmittente(asString(row[1]))
            .fromHistory(true)
            .build());
    }

    @Override
    public Page<PositionPaymentExtraDTO> searchByIuv(String pa, String nav, String iuv, LocalDate dateFrom, LocalDate dateTo,Pageable pageable) {
        log.debug("Request to search by IUV: {}, PA: {}, NAV: {}, offset: {}, limit: {}", iuv, pa, nav, pageable.getOffset(), pageable.getPageSize());

        List<Object[]> onlineRows = onlinePositionRepository.searchByIuv(
            iuv,
            nav,
            pa,
            Pageable.unpaged()
        );
        List<Object[]> safeOnlineRows = onlineRows == null ? List.of() : onlineRows;
        List<Integer> onlinePositionIds = safeOnlineRows.stream()
            .map(row -> ((Number) row[0]).intValue())
            .toList();
        List<Integer> onlinePositionTokenIds = safeOnlineRows.stream()
            .map(row -> ((Number) row[1]).intValue())
            .toList();

        Page<Object[]> groupRow = historyPositionRepository.searchByIuv(
            iuv,
            nav,
            pa,
            dateFrom,
            dateTo,
            onlinePositionIds,
            onlinePositionTokenIds,
            pageable
        );

        return groupRow.map(row -> PositionPaymentExtraDTO.builder()
            .nav(asString(row[0]))
            .paEmittente(asString(row[1]))
            .fromHistory(true)
            .build());
    }

    @Override
    public Page<PositionPaymentExtraDTO> searchByCart(String pa, String nav, String idCart, LocalDate dateFrom, LocalDate dateTo,Pageable pageable) {
        log.debug("Request to search by Cart: {}, offset: {}, limit: {}", idCart, pageable.getOffset(), pageable.getPageSize());

        List<Object[]> onlineRows = onlinePositionRepository.searchByCart(
            idCart,
            nav,
            pa,
            Pageable.unpaged()
        );
        List<Object[]> safeOnlineRows = onlineRows == null ? List.of() : onlineRows;
        List<Integer> onlinePositionIds = safeOnlineRows.stream()
            .map(row -> ((Number) row[0]).intValue())
            .toList();
        List<Integer> onlinePositionTokenIds = safeOnlineRows.stream()
            .map(row -> ((Number) row[1]).intValue())
            .toList();

        Page<Object[]> groupRow = historyPositionRepository.searchByCart(
            idCart,
            nav,
            pa,
            dateFrom,
            dateTo,
            onlinePositionIds,
            onlinePositionTokenIds,
            pageable
        );

        return groupRow.map(row -> PositionPaymentExtraDTO.builder()
            .nav(asString(row[0]))
            .paEmittente(asString(row[1]))
            .fromHistory(true)
            .build());
    }

    @Override
    public Page<PositionPaymentExtraDTO> searchByToken( String pa, String nav,String token, LocalDate dateFrom, LocalDate dateTo,Pageable pageable) {
        log.debug("Request to search by Token: {}, offset: {}, limit: {}", token, pageable.getOffset(), pageable.getPageSize());

        if (token == null || token.trim().isEmpty()) {
            return Page.empty(pageable);
        }

        String normalizedToken = token.trim();

        List<Object[]> onlineRows = onlinePositionRepository.searchByToken(
            normalizedToken,
            nav,
            pa,
            Pageable.unpaged()
        );
        List<Object[]> safeOnlineRows = onlineRows == null ? List.of() : onlineRows;
        List<Integer> onlinePositionIds = safeOnlineRows.stream()
            .map(row -> ((Number) row[0]).intValue())
            .toList();
        List<Integer> onlinePositionTokenIds = safeOnlineRows.stream()
            .map(row -> ((Number) row[1]).intValue())
            .toList();

        Page<Object[]> groupRow = historyPositionRepository.searchByToken(
            normalizedToken,
            nav,
            pa,
            dateFrom,
            dateTo,
            onlinePositionIds,
            onlinePositionTokenIds,
            pageable
        );

        return groupRow.map(row -> PositionPaymentExtraDTO.builder()
            .nav(asString(row[0]))
            .paEmittente(asString(row[1]))
            .fromHistory(true)
            .build());
    }

    @Override
    public Page<PositionPaymentExtraDTO> searchExtra(String pa, String nav, String searchValue, LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {
        log.debug("Request to search extra: {}, offset: {}, limit: {}", searchValue, pageable.getOffset(), pageable.getPageSize());

        if (searchValue == null) {
            return Page.empty(pageable);
        }

        List<Object[]> onlineRows = onlinePositionRepository.searchExtra(
            searchValue,
            nav,
            pa
        );
        List<Object[]> safeOnlineRows = onlineRows == null ? List.of() : onlineRows;
        String onlinePositionIds = safeOnlineRows.stream()
            .map(row -> ((Number) row[0]).intValue())
            .map(String::valueOf)
            .collect(Collectors.joining(","));
        String onlinePositionTokenIds = safeOnlineRows.stream()
            .map(row -> ((Number) row[1]).intValue())
            .map(String::valueOf)
            .collect(Collectors.joining(","));

        log.info(searchValue +" :chiamata alla query in historyPositionRepository");

        List<Object[]> groupedRows = historyPositionRepository.searchExtra(
            searchValue,
            nav,
            pa,
            dateFrom,
            dateTo,
            onlinePositionIds,
            onlinePositionTokenIds
        );
        log.info(searchValue +" :fine chiamata alla query in historyPositionRepository");

        List<PositionPaymentExtraDTO> allRows = new ArrayList<>(groupedRows.stream()
            .map(row -> PositionPaymentExtraDTO.builder()
                .nav((String) row[0])
                .paEmittente((String) row[1])
                .fromHistory(true)
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
    public Page<PositionPaymentDTO> getPosition(String nav, String paEmittente, LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {
        log.debug("Request to get position: {}, {}", nav, paEmittente);

        Page<Object[]> historyPositionRows = historyPositionRepository.findPositionDetailRows(
            nav, paEmittente, dateFrom, dateTo, Pageable.unpaged()
        );
        List<Object[]> safeHistoryRows = historyPositionRows == null || historyPositionRows.getContent() == null
            ? List.of()
            : historyPositionRows.getContent();
        String onlinePositionIds = safeHistoryRows.stream()
            .map(row -> ((Number) row[0]).intValue())
            .map(String::valueOf)
            .collect(Collectors.joining(","));
        String onlinePositionTokenIds = safeHistoryRows.stream()
            .map(row -> ((Number) row[1]).intValue())
            .map(String::valueOf)
            .collect(Collectors.joining(","));

        Page<Object[]> onlinePositionPage = onlinePositionRepository.findPositionDetailRows(
            nav, paEmittente, onlinePositionIds, onlinePositionTokenIds, Pageable.unpaged()
        );
        List<Object[]> safeOnlineRows = onlinePositionPage == null || onlinePositionPage.getContent() == null
            ? List.of()
            : onlinePositionPage.getContent();

        List<Object[]> rows = mergePositionRows(safeHistoryRows, safeOnlineRows);
        if (rows.isEmpty()) {
            return Page.empty(pageable);
        }
        sortPositionRows(rows, pageable);

        Object[] firstRow = rows.stream().filter(row -> row[2] != null).findFirst().orElse(rows.get(0));
        Object[] preferredRow = rows.stream().filter(row -> row[7] != null).findFirst().orElse(firstRow);
        Object[] payedRow = rows.stream().filter(row -> row[9] != null && row[7] != null).findFirst().orElse(null);

        List<String> allTokens = rows.stream()
            .map(row -> tokenAsHex(row[7]))
            .filter(Objects::nonNull)
            .collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new), List::copyOf));

        // multiOutcome = true se ci sono piu' token con outcome=OK per la stessa posizione (nav, pa)
        long okTokenCount = historyPositionRepository.countOkTokensByNavAndPa(nav, paEmittente);
        boolean multiOutcome = okTokenCount > 1;

        PositionPaymentDTO dto = PositionPaymentDTO.builder()
            .positionInfo(PositionPaymentInfoDTO.builder()
                .nav(asString(firstRow[2]))
                .paEmittente(asString(firstRow[3]))
                .iuv(asString(preferredRow[5]))
                .creditorReferenceId(asString(preferredRow[6]))
                .lastEvent(toInstant(firstRow[4]))
                .isCached(false)
                .build())
            .tokens((long) allTokens.size())
            .allTokens(allTokens)
            .tokenBorn(payedRow == null ? null : toInstantFromDate(payedRow[8]))
            .payed(
                payedRow == null
                    ? null
                    : PayedDTO.builder()
                    .token(tokenAsHex(payedRow[7]))
                    .payedDate(toInstant(payedRow[9]))
                    .multiOutcome(multiOutcome)
                    .build()
            )
            .actors(ActorsDTO.builder()
                .psp(asString(preferredRow[13]))
                .pspDescription(asString(preferredRow[21]))
                .ptPa(asString(preferredRow[14]))
                .ptPaDescription(asString(preferredRow[22]))
                .ptPsp(asString(preferredRow[15]))
                .ptPspDescription(asString(preferredRow[23]))
                .station(asString(preferredRow[16]))
                .channel(asString(preferredRow[17]))
                .build())
            .amount(AmountDTO.builder()
                .amount(toDouble(preferredRow[11]))
                .fee(toDouble(preferredRow[12]))
                .build())
            .paymentInfo(PaymentInfoDTO.builder()
                .touchpoint(asString(preferredRow[18]))
                .paymentMethod(asString(preferredRow[19]))
                .isCart(rows.stream().anyMatch(r -> r[20] != null))  // true se almeno un token ha idCarrello != null
                .idCarrello((rows.size() == 1 && firstRow[20] != null) ? asString(firstRow[20]) : null)
                .isGpd(false)
                .isStandin(false)
                .isDw(nav.startsWith("351"))
                .build())
            .build();

        return new PageCustomImpl<>(Collections.singletonList(dto), pageable, allTokens.size());
    }

    private List<Object[]> mergePositionRows(List<Object[]> historyRows, List<Object[]> onlineRows) {
        Map<String, Object[]> rowsByToken = new LinkedHashMap<>();
        historyRows.forEach(row -> rowsByToken.put(positionRowKey(row), row.clone()));
        onlineRows.forEach(onlineRow -> {
            String key = positionRowKey(onlineRow);
            Object[] existingRow = rowsByToken.get(key);
            if (existingRow == null) {
                rowsByToken.put(key, onlineRow.clone());
                return;
            }
            for (int i = 0; i < existingRow.length; i++) {
                if (onlineRow[i] != null) {
                    existingRow[i] = onlineRow[i];
                }
            }
        });
        return new ArrayList<>(rowsByToken.values());
    }

    private String positionRowKey(Object[] row) {
        if (row[1] != null) {
            return "token:" + row[1];
        }
        if (row[7] != null) {
            return "tokenHex:" + row[7];
        }
        return "position:" + row[0];
    }

    private void sortPositionRows(List<Object[]> rows, Pageable pageable) {
        List<Sort.Order> orders = pageable.getSort().isSorted()
            ? pageable.getSort().toList()
            : List.of(Sort.Order.desc("tokenDateEvent"));
        rows.sort((left, right) -> {
            for (Sort.Order order : orders) {
                Object leftValue = positionSortValue(left, order.getProperty());
                Object rightValue = positionSortValue(right, order.getProperty());
                int comparison;
                if (leftValue == null || rightValue == null) {
                    comparison = leftValue == rightValue ? 0 : (leftValue == null ? 1 : -1);
                } else {
                    comparison = comparePositionSortValues(leftValue, rightValue);
                    if (!order.isAscending()) {
                        comparison = -comparison;
                    }
                }
                if (comparison != 0) {
                    return comparison;
                }
            }
            long leftId = left[1] instanceof Number id ? id.longValue() : Long.MIN_VALUE;
            long rightId = right[1] instanceof Number id ? id.longValue() : Long.MIN_VALUE;
            return Long.compare(rightId, leftId);
        });
    }

    private Object positionSortValue(Object[] row, String property) {
        return switch (property) {
            case "tokenDateEvent" -> toInstantFromDate(row[8]);
            case "paymentDate" -> toInstant(row[9]);
            case "psp" -> asString(row[13]);
            case "ptPsp" -> asString(row[15]);
            case "amount" -> row[11] instanceof Number number ? number.doubleValue() : null;
            case "paymentMethod" -> asString(row[19]);
            case "touchpoint" -> asString(row[18]);
            case "ptPaDesc" -> asString(row[22]);
            case "tokenHex" -> asString(row[7]);
            default -> null;
        };
    }

    private int comparePositionSortValues(Object left, Object right) {
        if (left instanceof java.time.Instant leftInstant && right instanceof java.time.Instant rightInstant) {
            return leftInstant.compareTo(rightInstant);
        }
        if (left instanceof Number leftNumber && right instanceof Number rightNumber) {
            return Double.compare(leftNumber.doubleValue(), rightNumber.doubleValue());
        }
        return left.toString().compareTo(right.toString());
    }


    @Override
    public TokenInfoDTO getTokenInfo(String token, LocalDate dateFrom, LocalDate dateTo) {
        log.debug("Request to get token info: {}", token);

        if (token == null || token.trim().isEmpty()) {
            return null;
        }

        List<Object[]> rows = historyPositionRepository.findTokenDetailRow(token.trim());
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
            long okTokenCount = historyPositionRepository.countOkTokensByNavAndPa(rowNav, rowPa);
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
            .tokenBorn(toInstantFromDate(row[6]))
            .payed(
                !isPayed
                    ? null
                    : PayedDTO.builder()
                    .token(tokenAsHex(row[5]))
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
                .idCarrello(row[18] != null ? asString(row[18]):null)
                .isGpd(false)
                .isStandin(false)
                .isDw(rowNav != null && rowNav.startsWith("351"))
                .build())
            .build();
    }

    @Override
    public Page<TransferPaymentDTO> getTransfers(String nav, String paEmittente, String token, LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {
        log.debug("Request to get transfers for: {}, {}, {}, {}, {}", nav, paEmittente, token,pageable.getOffset(), pageable.getPageSize() );

        if (nav == null || paEmittente == null || token == null || token.trim().isEmpty()) {
            return null;
        }

        Page<Object[]> rows = historyPositionRepository.findTransferDetailRows(nav, paEmittente, token.trim(), pageable);
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
    public Page<WorkflowResponseDTO> getWorkflows(String nav, String paEmittente, LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {
        log.debug("Request to get workflows for: {}, {}", nav, paEmittente);

        Page<Object[]> workflowEvents = historyPositionRepository.findPositionWorkflows(nav, paEmittente, pageable);

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
    public Page<ExtraInfoResponseDTO> getExtraInfo(String token, LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {
        log.debug("Request to get extra info for token: {}", token);

        Page<Object[]> rows = historyPositionRepository.findExtraInfoByToken(token.trim(),pageable);
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
