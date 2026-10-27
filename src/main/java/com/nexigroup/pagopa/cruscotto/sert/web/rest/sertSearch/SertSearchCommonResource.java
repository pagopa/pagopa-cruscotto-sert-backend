package com.nexigroup.pagopa.cruscotto.sert.web.rest.sertSearch;

import com.nexigroup.pagopa.cruscotto.sert.service.SertService;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.*;
import com.nexigroup.pagopa.cruscotto.sert.service.impl.SertHistoryDataServiceImpl;
import com.nexigroup.pagopa.cruscotto.sert.service.impl.SertOnlineServiceImpl;
import com.nexigroup.pagopa.cruscotto.sert.service.util.PaymentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

import java.util.List;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;


public class SertSearchCommonResource {

    private final Logger log = LoggerFactory.getLogger(SertSearchCommonResource.class);

    private final SertService sertOnlineService;
    private final SertService sertHistoryDataService;
    private final int dateRangeDeltaDays;

    // Valid sort fields for search API



    public SertSearchCommonResource(
        SertOnlineServiceImpl sertOnlineService,
        SertHistoryDataServiceImpl sertHistoryDataService,
        int dateRangeDeltaDays
    ) {
        this.sertOnlineService = sertOnlineService;
        this.sertHistoryDataService = sertHistoryDataService;
        this.dateRangeDeltaDays = dateRangeDeltaDays;
    }

    /**
     * Validates that the sort fields in Pageable are allowed.
     * Only "nav" and "paEmittente" are allowed for sorting.
     */


    /**
     * {@code GET  /search} : Unified search API.
     *
     * @param pa the Codice Fiscale Ente Creditore.
     * @param nav the Codice Avviso.
     * @param iuv the IUV o Creditor Reference ID.
     * @param token the Token di pagamento.
     * @param idCarrello the ID Carrello.
     * @param info the Valore per ricerca extra.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the search results.
     */
    public ResponseEntity<List<PositionPaymentExtraDTO>> search(
        String pa,
        String nav,
        String iuv,
        String token,
        String idCarrello,
        String info,
        Pageable pageable
    ){
        return search(pa, nav, iuv, token, idCarrello, info, false, null, null, pageable);
    }

    public ResponseEntity<List<PositionPaymentExtraDTO>> search(
        String pa,
        String nav,
        String iuv,
        String token,
        String idCarrello,
        String info,
        boolean searchFromHistory,
        LocalDate dateFrom,
        LocalDate dateTo,
        Pageable pageable
    ){
        return search(pa, nav, iuv, token, idCarrello, info, searchFromHistory, true, dateFrom, dateTo, pageable);
    }

    public ResponseEntity<List<PositionPaymentExtraDTO>> search(
        String pa,
        String nav,
        String iuv,
        String token,
        String idCarrello,
        String info,
        boolean searchFromHistory,
        boolean searchFromOnline,
        LocalDate dateFrom,
        LocalDate dateTo,
        Pageable pageable
    ){
        try {
            if (searchFromHistory) {
                cheackSearchHistory(dateFrom, dateTo);
            }
            // Validate sort fields
            PaymentUtil.validatePageable(pageable, PaymentUtil.SEARCH_SORT_MAPPING);

            pageable =PaymentUtil.remapSorting(pageable, null, PaymentUtil.SEARCH_SORT_MAPPING, List.of(Sort.Order.desc("paEmittente")));
            Pageable searchPageable = pageable;
            if (searchFromHistory) {
                long requestedEnd = pageable.getOffset() + pageable.getPageSize();
                if (requestedEnd > Integer.MAX_VALUE) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requested page is too large.");
                }
                int fetchSize = (int) requestedEnd;
                searchPageable = PageRequest.of(0, Math.max(fetchSize, 1), pageable.getSort());
            }

            int presentGroups = 0;

            if (iuv != null) presentGroups++;
            if (token != null) presentGroups++;
            if (idCarrello != null) presentGroups++;
            if (info != null) presentGroups++;

            if (presentGroups > 1) {
                log.error("Invalid search parameters: exactly one search group must be provided.");
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid search parameters: exactly one search group must be provided."
                );
            }

            Page<PositionPaymentExtraDTO> page = null;

            if (searchFromOnline) {
                page = searchPositionPayment(pa, nav, iuv, token, idCarrello, info, presentGroups, page, searchPageable,sertOnlineService, null,null);
            }

            if (searchFromHistory) {
                Page<PositionPaymentExtraDTO> oldPage = searchPositionPayment(pa, nav, iuv, token, idCarrello, info, presentGroups, page, searchPageable,sertHistoryDataService, dateFrom, dateTo);
                if (page == null) {
                    page = new PageImpl<>(List.of(), searchPageable, 0);
                }
                page = mergeSearchResults(page, oldPage, pageable);
            }

            if (page == null || page.getContent()==null || page.getContent().isEmpty()) {
                throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    ""
                );
            }

            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
            return ResponseEntity.ok().headers(headers).body(page.getContent());
        } catch (ResponseStatusException e) {
                throw e;
        }catch (Exception e) {
            log.error("Error occurred during search operation. ", e);
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An error occurred while processing your request. Please try again later."
            );
        }
    }

    private Page<PositionPaymentExtraDTO> searchPositionPayment(String pa, String nav, String iuv, String token, String idCarrello, String info, int presentGroups, Page<PositionPaymentExtraDTO> page, Pageable searchPageable,
                                                                SertService sertService,
                                                                LocalDate dateFrom,
                                                                LocalDate dateTo) {
        if (presentGroups == 0){
            if (pa != null ||  nav != null) {
                page = sertService.searchByNav(nav, pa,  dateFrom, dateTo,searchPageable);
            } else  {
                log.error("Provide PA or NAV for PA+NAV search.");
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Provide PA or NAV for PA+NAV search."
                );
            }
        } else if (iuv != null) {
            page = sertService.searchByIuv(pa, nav, iuv,  dateFrom, dateTo,searchPageable);
        } else if (token != null) {
            page = sertService.searchByToken(pa, nav, token,  dateFrom, dateTo,searchPageable);
        } else if (idCarrello != null) {
            page = sertService.searchByCart(pa, nav, idCarrello,  dateFrom, dateTo,searchPageable);
        } else if (info != null) {
            page = sertService.searchExtra(pa, nav, info,  dateFrom, dateTo,searchPageable);
        }
        return page;
    }

    private void cheackSearchHistory(LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom == null || dateTo == null || !dateFrom.isBefore(dateTo)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "dateFrom and dateTo are required and dateFrom must be before dateTo when fromHistory is enabled."
            );
        }
        if (dateTo.isAfter(LocalDate.now().minusMonths(6).plusDays(dateRangeDeltaDays))) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "dateTo must  be earlier than six months ago plus the configured date range delta."
            );
        }
        if (dateFrom.plusMonths(6).isBefore(dateTo)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "The date range between dateFrom and dateTo must not exceed six months."
            );
        }
    }

    private Page<PositionPaymentExtraDTO> mergeSearchResults(
        Page<PositionPaymentExtraDTO> currentPage,
        Page<PositionPaymentExtraDTO> oldPage,
        Pageable pageable
    ) {
        List<PositionPaymentExtraDTO> merged = new ArrayList<>(currentPage.getContent());
        Map<SearchResultKey, PositionPaymentExtraDTO> currentByPosition = new LinkedHashMap<>();
        currentPage.getContent().forEach(item -> {
            if (item.getFromHistory() == null) {
                item.setFromHistory(false);
            }
            currentByPosition.put(new SearchResultKey(item.getNav(), item.getPaEmittente()), item);
        });
        oldPage.getContent().forEach(item -> {
            item.setFromHistory(true);
            SearchResultKey key = new SearchResultKey(item.getNav(), item.getPaEmittente());
            PositionPaymentExtraDTO existing = currentByPosition.get(key);
            if (existing == null) {
                merged.add(item);
            } else {
                mergeHistoryFlag(existing, item);
            }
        });

        merged.sort(searchComparator(pageable.getSort()));

        int fromIndex = (int) Math.min(pageable.getOffset(), merged.size());
        int toIndex = Math.min(fromIndex + pageable.getPageSize(), merged.size());
        return new PageImpl<>(
            merged.subList(fromIndex, toIndex),
            pageable,
            currentPage.getTotalElements() + oldPage.getTotalElements()
        );
    }

    private record SearchResultKey(String nav, String paEmittente) {}

    private void mergeHistoryFlag(PositionPaymentExtraDTO existing, PositionPaymentExtraDTO candidate) {
        existing.setFromHistory(
            Boolean.TRUE.equals(existing.getFromHistory()) || Boolean.TRUE.equals(candidate.getFromHistory())
        );
    }

    private Comparator<PositionPaymentExtraDTO> searchComparator(Sort sort) {
        Comparator<PositionPaymentExtraDTO> comparator = (left, right) -> 0;
        for (Sort.Order order : sort) {
            Comparator<String> valueComparator = order.isAscending()
                ? Comparator.nullsLast(String::compareTo)
                : Comparator.nullsLast((left, right) -> right.compareTo(left));
            Comparator<String> finalValueComparator = valueComparator;
            comparator = comparator.thenComparing(
                row -> searchSortValue(row, order.getProperty()),
                finalValueComparator
            );
        }
        return comparator;
    }

    private String searchSortValue(PositionPaymentExtraDTO row, String property) {
        return switch (property) {
            case "nav" -> row.getNav();
            case "paEmittente", "paEmittenteDesc" -> row.getPaEmittente();
            default -> null;
        };
    }



    public ResponseEntity<PositionPaymentDTO> getPosition(
        String nav,
        String paEmittente,
        boolean searchFromHistory,
        Pageable pageable
    ) {
        try {
            PaymentUtil.validatePageable(pageable, PaymentUtil.POSITION_TOKEN_SORT_MAPPING);

            Pageable remappedPageable =PaymentUtil.remapSorting(pageable,null,PaymentUtil.POSITION_TOKEN_SORT_MAPPING,
                List.of(Sort.Order.desc("tokenDateEvent")));

            Page<PositionPaymentDTO> page = searchFromHistory
                ? sertHistoryDataService.getPosition(nav, paEmittente, remappedPageable)
                : sertOnlineService.getPosition(nav, paEmittente,remappedPageable);
            if (page == null || page.getContent()==null || page.getContent().isEmpty()) {
                throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    ""
                );
            }

            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
            return ResponseEntity.ok().headers(headers).body(page.getContent().get(0));

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error in getPosition ", e);
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An error occurred while processing your request. Please try again later.");
        }
    }

    public ResponseEntity<TokenInfoDTO> getTokenInfo( String token) {
        return getTokenInfo(token, false);
    }

    public ResponseEntity<TokenInfoDTO> getTokenInfo(String token, boolean searchFromHistory) {
        try {
            TokenInfoDTO tokenInfo = searchFromHistory
                ? sertHistoryDataService.getTokenInfo(token)
                : sertOnlineService.getTokenInfo(token);
            if (tokenInfo == null) {
                throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    ""
                );
            }
            return ResponseEntity.ok(tokenInfo);
        } catch (ResponseStatusException e) {
            throw e;
        }catch (Exception e) {
            log.error("Error in getTokenInfo ", e);
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An error occurred while processing your request. Please try again later.");
        }
    }

    public ResponseEntity<TransferPaymentDTO> getTransfers(
        String nav,
        String paEmittente,
        String token,
        Pageable pageable
    ) {
        return getTransfers(nav, paEmittente, token, false, pageable);
    }

    public ResponseEntity<TransferPaymentDTO> getTransfers(
        String nav,
        String paEmittente,
        String token,
        boolean searchFromHistory,
        Pageable pageable
    ) {
        try {
            // Validate transfer sort fields (frontend names) and check idTransfer is not passed
            PaymentUtil.validatePageable(pageable, PaymentUtil.TRANSFER_SORT_MAPPING);


            Pageable remappedPageable =PaymentUtil.remapSorting(pageable,List.of(Sort.Order.asc("idTransfer")),PaymentUtil.TRANSFER_SORT_MAPPING,
                List.of(Sort.Order.asc("idTransfer"),Sort.Order.desc("paTransfer")));
            Page<TransferPaymentDTO> page = searchFromHistory
                ? sertHistoryDataService.getTransfers(nav, paEmittente, token, remappedPageable)
                : sertOnlineService.getTransfers(nav, paEmittente, token, remappedPageable);

            if (page == null || page.getContent()==null || page.getContent().isEmpty()) {
                throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    ""
                );
            }

            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
            return ResponseEntity.ok().headers(headers).body(page.getContent().get(0));

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error in getTransfers ", e);

            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An error occurred while processing your request. Please try again later.");
        }
    }

    public ResponseEntity<List<WorkflowObjectDTO>> getWorkflows(
        String nav,
        String paEmittente,
        Pageable pageable
    ) {
        return getWorkflows(nav, paEmittente, false, pageable);
    }

    public ResponseEntity<List<WorkflowObjectDTO>> getWorkflows(
        String nav,
        String paEmittente,
        boolean searchFromHistory,
        Pageable pageable
    ) {
        try {

           PaymentUtil.validatePageable(pageable, PaymentUtil.WORKFLOW_QUERY_TO_DTO_MAPPING);

            List<Sort.Order> orderSecondColumn = List.of(Sort.Order.asc("insertedtimestamp"));

            Sort.Order sortSottotipoEvento = pageable.getSort()
                .getOrderFor("sottotipoevento");

            if (sortSottotipoEvento != null) {
                orderSecondColumn = List.of(
                    new Sort.Order(sortSottotipoEvento.getDirection(), "reqResp"),
                    Sort.Order.asc("insertedtimestamp")
                );
            }


            Pageable remappedPageable =PaymentUtil.remapSorting(pageable, orderSecondColumn,
                PaymentUtil.WORKFLOW_QUERY_TO_DTO_MAPPING,
                List.of(Sort.Order.desc("insertedtimestamp") ));

            Page<WorkflowResponseDTO> page = searchFromHistory
                ? sertHistoryDataService.getWorkflows(nav, paEmittente, remappedPageable)
                : sertOnlineService.getWorkflows(nav, paEmittente, remappedPageable);

            if (page == null || page.getContent()==null || page.getContent().isEmpty()) {
                throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    ""
                );
            }

            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
            return ResponseEntity.ok().headers(headers).body(page.getContent().get(0).getEventsPosition());


        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error in getWorkflows ", e);

            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An error occurred while processing your request. Please try again later.");
        }
    }
    public ResponseEntity<ExtraInfoResponseDTO> getExtraInfo(
        String token,
        Pageable pageable) {
        return getExtraInfo(token, false, pageable);
    }

    public ResponseEntity<ExtraInfoResponseDTO> getExtraInfo(
        String token,
        boolean searchFromHistory,
        Pageable pageable) {

        try {
            PaymentUtil.validatePageable(pageable, PaymentUtil.EXTRA_INFO_SORT_MAPPING);


            Pageable remappedPageable =PaymentUtil.remapSorting(pageable, null, PaymentUtil.EXTRA_INFO_SORT_MAPPING,
                List.of(Sort.Order.desc("infoName")));
            Page<ExtraInfoResponseDTO> page = searchFromHistory
                ? sertHistoryDataService.getExtraInfo(token, remappedPageable)
                : sertOnlineService.getExtraInfo(token, remappedPageable);

            if (page == null || page.getContent()==null || page.getContent().isEmpty()) {
                throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    ""
                );
            }
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);

            return ResponseEntity.ok().headers(headers).body(page.getContent().get(0));

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error in getExtraInfo ", e);

            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An error occurred while processing your request. Please try again later.");
        }
    }
}
