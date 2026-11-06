package com.nexigroup.pagopa.cruscotto.sert.service.impl;

import com.nexigroup.pagopa.cruscotto.sert.domain.Position;
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
        String onlinePositionIds = extraIds(safeOnlineRows, 0);
        String onlinePositionTokenIds = extraIds(safeOnlineRows, 1);

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
        List<Integer> offlinePositionIds = extraIdsAsIntegers(safeHistoryRows, 0);
        List<Integer> offlinePositionTokenIds = extraIdsAsIntegers(safeHistoryRows, 1);

        Page<Object[]> onlinePositionPage = onlinePositionRepository.findPositionDetailRows(
            nav, paEmittente, offlinePositionIds, offlinePositionTokenIds, Pageable.unpaged()
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

        long okTokenCount = rows.stream()
            .filter(row -> "OK".equalsIgnoreCase(asString(row[10])))
            .count();
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
                .isCart(rows.stream().anyMatch(r -> Boolean.TRUE.equals(r[24])))
                .idCarrello((rows.size() == 1 && firstRow[20] != null) ? asString(firstRow[20]) : null)
                .isGpd(false)
                .isStandin(false)
                .isDw(nav.startsWith("351"))
                .build())
            .build();

        return new PageCustomImpl<>(Collections.singletonList(dto), pageable, allTokens.size());
    }

    private List<Object[]> mergePositionRows(List<Object[]> historyRows, List<Object[]> onlineRows) {
        List<Object[]> rows = new ArrayList<>(historyRows.size() + onlineRows.size());
        rows.addAll(historyRows);
        rows.addAll(onlineRows);
        return rows;
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
        List<Object[]> onlineRows = onlinePositionRepository.findTokenDetailRow(token.trim());
        List<Object[]> safeOnlineRows = onlineRows == null ? List.of() : onlineRows;
        List<Integer> offlinePositionIds = extraIdsAsIntegers(safeOnlineRows, 0);

        List<Object[]> historyPositionRows = historyPositionRepository.findTokenDetailRow(token.trim(), dateFrom, dateTo,offlinePositionIds);
        List<Object[]> safeHistoryRows = historyPositionRows == null ? List.of() : historyPositionRows;


        List<Object[]> rows = mergePositionRows(safeHistoryRows, safeOnlineRows);
        if (rows.isEmpty()) {
            return null;
        }

        Object[] tokenRow = rows.stream()
            .filter(candidate -> token.trim().equalsIgnoreCase(tokenAsHex(candidate[7])))
            .findFirst()
            .orElse(null);
        if (tokenRow == null) {
            return null;
        }
        Object[] positionRow = rows.stream()
            .filter(candidate -> hasPositionDetails(candidate) && samePositionId(candidate[0], tokenRow[0]))
            .findFirst()
            .orElse(tokenRow);
        Boolean isPayed = tokenRow[10] != null && "OK".equalsIgnoreCase(tokenRow[10].toString());

        // multiOutcome = true se ci sono piu' token con outcome=OK per la stessa posizione (nav, pa)
        String rowNav = asString(positionRow[2]);
        String rowPa  = asString(positionRow[3]);
        String outcomeToken = tokenRow[10]!=null ? asString(tokenRow[10]):"KO";
        boolean multiOutcome = outcomeToken.equalsIgnoreCase("OK")?true:false;

        return TokenInfoDTO.builder()
            .positionInfo(PositionPaymentInfoDTO.builder()
                .nav(rowNav)
                .paEmittente(rowPa)
                .iuv(asString(tokenRow[5]))
                .creditorReferenceId(asString(tokenRow[6]))
                .lastEvent(toInstant(positionRow[4]))
                .isCached(false)
                .build())
            .isPayedToken(isPayed)
            .tokenBorn(toInstantFromDate(tokenRow[8]))
            .payed(
                !isPayed
                    ? null
                    : PayedDTO.builder()
                    .token(tokenAsHex(tokenRow[7]))

                    .payedDate(toInstant(tokenRow[9]))
                    .multiOutcome(multiOutcome)
                    .build()
            )
            .actors(ActorsDTO.builder()
                .psp(asString(tokenRow[13]))
                .pspDescription(asString(tokenRow[21]))
                .ptPa(asString(tokenRow[14]))
                .ptPaDescription(asString(tokenRow[22]))
                .ptPsp(asString(tokenRow[15]))
                .ptPspDescription(asString(tokenRow[23]))
                .station(asString(tokenRow[16]))
                .channel(asString(tokenRow[17]))
                .build())
            .amount(AmountDTO.builder()
                .amount(toDouble(tokenRow[11]))
                .fee(toDouble(tokenRow[12]))
                .build())
            .paymentInfo(PaymentInfoDTO.builder()
                .touchpoint(asString(tokenRow[18]))
                .paymentMethod(asString(tokenRow[19]))
                .isCart(Boolean.TRUE.equals(tokenRow[24]))
                .idCarrello(tokenRow[20] != null ? asString(tokenRow[20]):null)
                .isGpd(false)
                .isStandin(false)
                .isDw(rowNav != null && rowNav.startsWith("351"))
                .build())
            .build();
    }

    private static String extraIds(List<Object[]> safeHistoryRows, int x) {
        return safeHistoryRows.stream()
            .map(row -> ((Number) row[x]).intValue())
            .map(String::valueOf)
            .collect(Collectors.joining(","));
    }

    private static List<Integer> extraIdsAsIntegers(List<Object[]> rows, int column) {
        return rows.stream()
            .map(row -> row[column])
            .filter(Objects::nonNull)
            .map(id -> ((Number) id).intValue())
            .toList();
    }

    private boolean hasPositionDetails(Object[] row) {
        return row[2] != null || row[3] != null || row[4] != null;
    }

    private boolean samePositionId(Object left, Object right) {
        if (left instanceof Number leftId && right instanceof Number rightId) {
            return leftId.longValue() == rightId.longValue();
        }
        return Objects.equals(left, right);
    }

    @Override
    public Page<TransferPaymentDTO> getTransfers(String nav, String paEmittente, String token, LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {
        log.debug(
            "Request to get transfers for: {}, {}, {}, {}, {}",
            nav,
            paEmittente,
            token,
            pageable.getOffset(),
            pageable.isUnpaged() ? "unpaged" : pageable.getPageSize()
        );

        if (nav == null || paEmittente == null || token == null || token.trim().isEmpty()) {
            return null;
        }

        List<Object[]> byTokenNoPage = historyPositionRepository.findByTokenNoPage(token.trim(), nav, paEmittente, dateFrom, dateTo);
        List<Object[]> safeTokenRows = byTokenNoPage == null ? List.of() : byTokenNoPage;
        List<Integer> positionTokenIds = extraIdsAsIntegers(safeTokenRows, 1);
        List<Object[]> onlineRows = onlinePositionRepository.findTransferDetailRows(token.trim(), positionTokenIds);
        List<Object[]> historyRows = historyPositionRepository.findTransferDetailRows(nav, paEmittente, token.trim(), dateFrom, dateTo);

        List<Object[]> rows = new ArrayList<>();
        if (onlineRows != null) {
            rows.addAll(onlineRows);
        }
        if (historyRows != null) {
            rows.addAll(historyRows);
        }
        if (rows.isEmpty()) {
            return null;
        }

        Object[] tokenRow = rows.stream()
            .filter(row -> hasRequestedToken(row, token))
            .filter(this::hasTokenDetails)
            .findFirst()
            .orElseGet(() -> rows.stream()
                .filter(row -> hasPositionTokenId(row) && positionTokenIds.stream()
                    .anyMatch(id -> samePositionId(id, row[2])))
                .findFirst()
                .orElseGet(() -> rows.stream()
                    .filter(row -> hasPositionTokenId(row) && (row[3] != null || row[6] != null || row[8] != null))
                    .findFirst()
                    .orElse(rows.get(0))));
        Object[] positionRow = rows.stream()
            .filter(row -> row.length > 5 && row[0] instanceof Position position
                && Objects.equals(nav, position.getNav())
                && Objects.equals(paEmittente, position.getPaEmittente()))
            .findFirst()
            .orElse(tokenRow);
        List<Object[]> transferRows = rows.stream()
            .filter(this::hasTransferDetails)
            .filter(row -> hasRequestedToken(row, token) || (hasPositionTokenId(row) && positionTokenIds.stream()
                .anyMatch(id -> samePositionId(id, row[2]))))
            .collect(Collectors.toCollection(ArrayList::new));
        sortTransferRows(transferRows, pageable);
        long totalTransfers = transferRows.size();
        List<Object[]> pageTransferRows = paginateTransferRows(transferRows, pageable);

        List<TransferObjectDTO> transfers = pageTransferRows.stream()
            .map(this::toTransferObject)
            .collect(Collectors.toList());

        TransferPaymentDTO dto = TransferPaymentDTO.builder()
            .positionInfo(PositionPaymentInfoDTO.builder()
                .nav(asString(positionRow[3]))
                .paEmittente(asString(positionRow[4]))
                .iuv(asString(tokenRow[6]))
                .creditorReferenceId(asString(tokenRow[7]))
                .lastEvent(toInstant(positionRow[5]))
                .isCached(false)
                .build())
            .token(tokenAsHex(tokenRow[8]))
            .transfersCount(toDouble(tokenRow[9]))
            .transfers(transfers)
            .build();

        return new PageCustomImpl<>(Collections.singletonList(dto), pageable, totalTransfers);
    }

    private boolean hasTransferDetails(Object[] row) {
        return row.length > 10 && (row[10] != null || row[11] != null || row[12] != null || row[13] != null || row[14] != null);
    }

    private boolean hasPositionTokenId(Object[] row) {
        return row.length > 2 && row[2] != null;
    }

    private boolean hasRequestedToken(Object[] row, String token) {
        return row.length > 8 && Objects.equals(token.trim(), tokenAsHex(row[8]));
    }

    private boolean hasTokenDetails(Object[] row) {
        return hasPositionTokenId(row) && (row[3] != null || row[6] != null || row[8] != null);
    }

    private void sortTransferRows(List<Object[]> rows, Pageable pageable) {
        if (!pageable.getSort().isSorted()) {
            return;
        }
        rows.sort((left, right) -> {
            for (Sort.Order order : pageable.getSort()) {
                Object leftValue = transferSortValue(left, order.getProperty());
                Object rightValue = transferSortValue(right, order.getProperty());
                int comparison;
                if (leftValue == null || rightValue == null) {
                    comparison = leftValue == rightValue ? 0 : (leftValue == null ? 1 : -1);
                } else {
                    comparison = compareTransferSortValues(leftValue, rightValue);
                    if (!order.isAscending()) {
                        comparison = -comparison;
                    }
                }
                if (comparison != 0) {
                    return comparison;
                }
            }
            return 0;
        });
    }

    private Object transferSortValue(Object[] row, String property) {
        return switch (property) {
            case "idTransfer" -> row[10];
            case "typeTransfer" -> Boolean.TRUE.equals(row[11]) ? "bollo" : "sepa";
            case "isBollo" -> row[11];
            case "iban", "ibanTransfer" -> row[12];
            case "amount", "amountTransfer" -> row[13];
            case "paFiscalCode", "paTransfer" -> row[14];
            case "transferDateEvent", "dateEvent" -> row.length > 15 ? row[15] : null;
            default -> null;
        };
    }

    private int compareTransferSortValues(Object left, Object right) {
        if (left instanceof Number leftNumber && right instanceof Number rightNumber) {
            return Double.compare(leftNumber.doubleValue(), rightNumber.doubleValue());
        }
        if (left instanceof LocalDate leftDate && right instanceof LocalDate rightDate) {
            return leftDate.compareTo(rightDate);
        }
        if (left instanceof java.time.temporal.TemporalAccessor && right instanceof java.time.temporal.TemporalAccessor) {
            return left.toString().compareTo(right.toString());
        }
        return left.toString().compareTo(right.toString());
    }

    private List<Object[]> paginateTransferRows(List<Object[]> rows, Pageable pageable) {
        if (pageable.isUnpaged()) {
            return rows;
        }
        int fromIndex = (int) Math.min(pageable.getOffset(), rows.size());
        int toIndex = Math.min(fromIndex + pageable.getPageSize(), rows.size());
        return rows.subList(fromIndex, toIndex);
    }

    private TransferObjectDTO toTransferObject(Object[] row) {
        return TransferObjectDTO.builder()
            .idTransfer(row[10] == null ? null : Integer.valueOf(asString(row[10])))
            .typeTransfer(Boolean.TRUE.equals(row[11]) ? "bollo" : "sepa")
            .iban(asString(row[12]))
            .amount(toDouble(row[13]))
            .paFiscalCode(asString(row[14]))
            .build();
    }

    @Override
    public Page<WorkflowResponseDTO> getWorkflows(String nav, String paEmittente, LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {
        log.debug("Request to get workflows for: {}, {}", nav, paEmittente);

        List<Object[]> historyPositionRows = historyPositionRepository.findByTokenNoPage(null, nav, paEmittente, dateFrom, dateTo);
        List<Object[]> safeHistoryPositionRows = historyPositionRows == null ? List.of() : historyPositionRows;
        List<Integer> positionIds = extraIdsAsIntegers(safeHistoryPositionRows, 0);
        List<Integer> positionTokenIds = extraIdsAsIntegers(safeHistoryPositionRows, 1);
        List<Integer> queryPositionIds = positionIds.isEmpty() ? List.of(-1) : positionIds;
        List<Integer> queryPositionTokenIds = positionTokenIds.isEmpty() ? List.of(-1) : positionTokenIds;

        List<Object[]> historyWorkflowEvents = historyPositionRepository.findPositionWorkflows(
            nav,
            paEmittente,
            dateFrom,
            dateTo
        );
        List<Object[]> onlineWorkflowEvents = onlinePositionRepository.findPositionWorkflows(
            nav,
            paEmittente,
            queryPositionIds,
            queryPositionTokenIds
        );
        List<Object[]> workflowRows = new ArrayList<>();
        if (historyWorkflowEvents != null) {
            workflowRows.addAll(historyWorkflowEvents.stream().filter(this::hasWorkflowEvent).toList());
        }
        if (onlineWorkflowEvents != null) {
            workflowRows.addAll(onlineWorkflowEvents.stream().filter(this::hasWorkflowEvent).toList());
        }
        if (workflowRows.isEmpty()) {
            return null;
        }

        sortWorkflowRows(workflowRows, pageable);
        long totalCount = workflowRows.size();
        int fromIndex = pageable.isUnpaged() ? 0 : (int) Math.min(pageable.getOffset(), totalCount);
        int toIndex = pageable.isUnpaged() ? workflowRows.size() : Math.min(fromIndex + pageable.getPageSize(), workflowRows.size());
        List<Object[]> pageRows = workflowRows.subList(fromIndex, toIndex);
        List<WorkflowObjectDTO> eventsPositionList = new java.util.ArrayList<>();

        for (Object[] row : pageRows) {
            // In case of a LEFT JOIN where there are no events at all, all event fields would be null.
            if (row[0] == null && row[4] == null) {
                continue;
            }

            String token = tokenAsHex(row[6]);
            String faultcode =  (row[5] != null ? String.valueOf(row[5]) : null);
            if ("REQ/RESP".equals(asString(row[2])) && "REQ".equals(asString(row[7]))) {
                faultcode=null;
            }

            WorkflowTokenObjectDTO tokenDTO = WorkflowTokenObjectDTO.builder()
                .insertedtimestamp(toInstant(row[0]))
                .tipoevento(asString(row[1]))
                .sottotipoevento("REQ/RESP".equals(asString(row[2])) ? asString(row[7]) : asString(row[2]))
                .outcome(asString(row[3]))
                .eventId(asString(row[4]))
                .faultcode( faultcode)
                .token(token)
                .id(asString(row[8]))
                .build();
            eventsPositionList.add(tokenDTO);

        }

        WorkflowResponseDTO dto = WorkflowResponseDTO.builder()
            .eventsPosition(eventsPositionList)
            .build();

        return new PageCustomImpl<>(Collections.singletonList(dto), pageable, totalCount);
    }

    private boolean hasWorkflowEvent(Object[] row) {
        return row != null && row.length > 8 && row[8] != null;
    }

    private void sortWorkflowRows(List<Object[]> rows, Pageable pageable) {
        if (!pageable.getSort().isSorted()) {
            return;
        }
        rows.sort((left, right) -> {
            for (Sort.Order order : pageable.getSort()) {
                Object leftValue = workflowSortValue(left, order.getProperty());
                Object rightValue = workflowSortValue(right, order.getProperty());
                int comparison;
                if (leftValue == null || rightValue == null) {
                    comparison = leftValue == rightValue ? 0 : (leftValue == null ? 1 : -1);
                } else {
                    comparison = compareTransferSortValues(leftValue, rightValue);
                    if (!order.isAscending()) {
                        comparison = -comparison;
                    }
                }
                if (comparison != 0) {
                    return comparison;
                }
            }
            return 0;
        });
    }

    private Object workflowSortValue(Object[] row, String property) {
        return switch (property) {
            case "insertedtimestamp", "insertedTimestamp" -> row[0];
            case "tipoevento", "tipoEvento" -> row[1];
            case "sottotipoevento", "sottotipoEvento" -> row[2];
            case "outcome" -> row[3];
            case "eventId", "event-id" -> row[4];
            case "faultcode", "faultCode" -> row[5];
            case "token" -> row[6];
            case "id" -> row[8];
            default -> null;
        };
    }

    @Override
    public Page<ExtraInfoResponseDTO> getExtraInfo(String token, LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {
        log.debug("Request to get extra info for token: {}", token);

        if (token == null || token.trim().isEmpty()) {
            return Page.empty(pageable);
        }

        String normalizedToken = token.trim();
        List<Object[]> historyPositionRows = historyPositionRepository.findByTokenNoPage(normalizedToken, null, null, dateFrom, dateTo);
        List<Object[]> safeHistoryPositionRows = historyPositionRows == null ? List.of() : historyPositionRows;
        List<Integer> positionIds = extraIdsAsIntegers(safeHistoryPositionRows, 0);
        List<Integer> positionTokenIds = extraIdsAsIntegers(safeHistoryPositionRows, 1);
        List<Integer> queryPositionIds = positionIds.isEmpty() ? List.of(-1) : positionIds;
        List<Integer> queryPositionTokenIds = positionTokenIds.isEmpty() ? List.of(-1) : positionTokenIds;

        List<Object[]> onlineRows = onlinePositionRepository.findExtraInfoByToken(
            normalizedToken,
            queryPositionIds,
            queryPositionTokenIds
        );
        List<Object[]> safeOnlineRows = onlineRows == null ? List.of() : onlineRows;

        Set<Integer> allPositionIds = new LinkedHashSet<>(positionIds);
        allPositionIds.addAll(extraIdsAsIntegers(safeOnlineRows, 0));
        List<Integer> historyQueryPositionIds = allPositionIds.isEmpty() ? List.of(-1) : new ArrayList<>(allPositionIds);
        List<Object[]> historyRows = historyPositionRepository.findExtraInfoByToken(
            normalizedToken,
            historyQueryPositionIds,
            dateFrom,
            dateTo
        );
        List<Object[]> safeHistoryRows = historyRows == null ? List.of() : historyRows;

        List<Object[]> rows = new ArrayList<>(safeOnlineRows);
        rows.addAll(safeHistoryRows);

        List<Object[]> positionRows = rows.stream()
            .filter(row -> row[0] != null && row[1] == null)
            .toList();
        List<Object[]> positionTokenRows = rows.stream()
            .filter(row -> row[1] != null && row[2] == null)
            .toList();
        List<Object[]> extraInfoRows = rows.stream()
            .filter(row -> row[2] != null)
            .toList();


        String positionNav = positionRows != null && !positionRows.isEmpty()? asString(positionRows.get(0)[3]) : null ;
        String positionPa = positionRows != null && !positionRows.isEmpty()? asString(positionRows.get(0)[4]) : null ;
        String positionToken = positionTokenRows != null && !positionTokenRows.isEmpty()?   tokenAsHex(positionTokenRows.get(0)[5]) :null ;

        List<ExtraInfoObjectDTO> allResults = extraInfoRows.stream()
            .map(row -> ExtraInfoObjectDTO.builder()
                .nav(asString(row[3]) == null ? positionNav : asString(row[3]))
                .paEmittente(asString(row[4]) == null ? positionPa : asString(row[4]))
                .token(tokenAsHex(row[5]) == null ? positionToken : tokenAsHex(row[5]))
                .name(asString(row[6]))
                .value(asString(row[7]))
                .tipoevento(asString(row[8]))
                .build())
            .collect(Collectors.toCollection(ArrayList::new));

        sortExtraInfoResults(allResults, pageable.getSort());
        long totalCount = allResults.size();
        int fromIndex = pageable.isUnpaged() ? 0 : (int) Math.min(pageable.getOffset(), totalCount);
        int toIndex = pageable.isUnpaged() ? allResults.size() : Math.min(fromIndex + pageable.getPageSize(), allResults.size());
        List<ExtraInfoObjectDTO> results = allResults.subList(fromIndex, toIndex);

        ExtraInfoResponseDTO dto = ExtraInfoResponseDTO.builder()
            .count(totalCount)
            .results(results)
            .build();
        return new PageCustomImpl<>(Collections.singletonList(dto), pageable, totalCount);
    }

    private boolean hasExtraInfoProjection(Object[] row) {
        return row != null && row.length >= 9;
    }

    private boolean isRequestedExtraInfoToken(Object[] row, String token) {
        if (row == null || row.length <= 5) {
            return false;
        }
        return token.equals(tokenAsHex(row[5]));
    }

    private void sortExtraInfoResults(List<ExtraInfoObjectDTO> results, Sort sort) {
        if (!sort.isSorted()) {
            return;
        }
        results.sort((left, right) -> {
            for (Sort.Order order : sort) {
                String leftValue = extraInfoSortValue(left, order.getProperty());
                String rightValue = extraInfoSortValue(right, order.getProperty());
                int comparison;
                if (leftValue == null || rightValue == null) {
                    comparison = leftValue == rightValue ? 0 : (leftValue == null ? 1 : -1);
                } else {
                    comparison = leftValue.compareTo(rightValue);
                    if (!order.isAscending()) {
                        comparison = -comparison;
                    }
                }
                if (comparison != 0) {
                    return comparison;
                }
            }
            return 0;
        });
    }

    private String extraInfoSortValue(ExtraInfoObjectDTO result, String property) {
        return switch (property) {
            case "nav" -> result.getNav();
            case "paEmittente", "pa-emittente" -> result.getPaEmittente();
            case "token" -> result.getToken();
            case "name", "infoName" -> result.getName();
            case "value", "infoValue" -> result.getValue();
            case "tipoevento", "tipoEvento" -> result.getTipoevento();
            default -> null;
        };
    }
}
