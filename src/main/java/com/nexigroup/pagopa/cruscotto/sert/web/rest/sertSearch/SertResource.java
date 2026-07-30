package com.nexigroup.pagopa.cruscotto.sert.web.rest.sertSearch;

import com.nexigroup.pagopa.cruscotto.sert.security.AuthoritiesConstants;
import com.nexigroup.pagopa.cruscotto.sert.service.SertService;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.*;
import com.nexigroup.pagopa.cruscotto.sert.service.util.PaymentUtil;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

import java.util.List;

/**
 * REST controller for SERT APIs.
 */
@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class SertResource {

    private final Logger log = LoggerFactory.getLogger(SertResource.class);

    private final SertService sertService;

    // Valid sort fields for search API



    public SertResource(SertService sertService) {
        this.sertService = sertService;
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
    ) {
        log.info("START REST request to search with params - pa: {}, nav: {}, iuv: {}, token: {}, idCarrello: {}, info: {}", pa, nav, iuv, token, idCarrello, info);

        try {
            // Validate sort fields
            PaymentUtil.validatePageable(pageable, PaymentUtil.SEARCH_SORT_MAPPING);

            pageable =PaymentUtil.remapSorting(pageable, null, PaymentUtil.SEARCH_SORT_MAPPING, List.of(Sort.Order.desc("paEmittente")));

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

            if (presentGroups == 0){
                if (pa != null ||  nav != null) {
                    page = sertService.searchByNav(nav, pa, pageable);
                } else  {
                    log.error("Provide PA or NAV for PA+NAV search.");
                    throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Provide PA or NAV for PA+NAV search."
                    );

                }
            } else if (iuv != null) {
                page = sertService.searchByIuv(pa, nav, iuv, pageable);
            } else if (token != null) {
                page = sertService.searchByToken(pa, nav, token, pageable);
            } else if (idCarrello != null) {
                page = sertService.searchByCart(pa, nav, idCarrello, pageable);
            } else if (info != null) {
                page = sertService.searchExtra(pa, nav, info, pageable);
            }

            if (page == null || page.getContent()==null || page.getContent().isEmpty()) {
                throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    ""
                );
            }

            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
            log.info("END REST request to search with params ");
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


    public ResponseEntity<PositionPaymentDTO> getPosition(
         String nav,
         String paEmittente,
         Pageable pageable
    ) {
        log.info ("START REST request to get Position : {}, {}", nav, paEmittente);
        try {
            PaymentUtil.validatePageable(pageable, PaymentUtil.POSITION_TOKEN_SORT_MAPPING);

            Pageable remappedPageable =PaymentUtil.remapSorting(pageable,null,PaymentUtil.POSITION_TOKEN_SORT_MAPPING,
                List.of(Sort.Order.desc("tokenDateEvent")));

            Page<PositionPaymentDTO> page = sertService.getPosition(nav, paEmittente,remappedPageable);
            if (page == null || page.getContent()==null || page.getContent().isEmpty()) {
                throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    ""
                );
            }

            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
            log.info ("END REST request to get Position" );

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
        log.info("START REST request to get Token Info : {}", token);
        try {
            TokenInfoDTO tokenInfo = sertService.getTokenInfo(token);
            if (tokenInfo == null) {
                throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    ""
                );
            }
            log.info("END REST request to get Token Info ");
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
        log.info("START REST request to get Transfers : {}, {}, {}", nav, paEmittente, token);
        try {
            // Validate transfer sort fields (frontend names) and check idTransfer is not passed
            PaymentUtil.validatePageable(pageable, PaymentUtil.TRANSFER_SORT_MAPPING);


            Pageable remappedPageable =PaymentUtil.remapSorting(pageable,List.of(Sort.Order.asc("idTransfer")),PaymentUtil.TRANSFER_SORT_MAPPING,
                List.of(Sort.Order.asc("idTransfer"),Sort.Order.desc("paTransfer")));
            Page<TransferPaymentDTO> page = sertService.getTransfers(nav, paEmittente, token, remappedPageable);

            if (page == null || page.getContent()==null || page.getContent().isEmpty()) {
                throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    ""
                );
            }

            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
            log.info("END REST request to get Transfers ");
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
        log.info("START REST request to get Workflows : {}, {}", nav, paEmittente);
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

            Page<WorkflowResponseDTO> page = sertService.getWorkflows(nav, paEmittente, remappedPageable);

            if (page == null || page.getContent()==null || page.getContent().isEmpty()) {
                throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    ""
                );
            }

            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
            log.info("END REST request to get Workflows ");

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

        log.info("START REST request to get Extra Info : {}", token);
        try {
            PaymentUtil.validatePageable(pageable, PaymentUtil.EXTRA_INFO_SORT_MAPPING);


            Pageable remappedPageable =PaymentUtil.remapSorting(pageable, null, PaymentUtil.EXTRA_INFO_SORT_MAPPING,
                List.of(Sort.Order.desc("infoName")));
            Page<ExtraInfoResponseDTO> page = sertService.getExtraInfo(token, remappedPageable);

            if (page == null || page.getContent()==null || page.getContent().isEmpty()) {
                throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    ""
                );
            }
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);

            log.info("END REST request to get Extra Info");

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
