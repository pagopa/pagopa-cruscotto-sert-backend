package com.nexigroup.pagopa.cruscotto.sert.web.rest;

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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

import java.util.Set;
import java.util.stream.Collectors;

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
    @GetMapping("/search")
    @Operation(tags = "Ricerca delle posizioni debitorie")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.SERT_SEARCH + "\")")
    public ResponseEntity<?> search(
        @RequestParam(required = false) String pa,
        @RequestParam(required = false) String nav,
        @RequestParam(required = false) String iuv,
        @RequestParam(required = false) String token,
        @RequestParam(required = false, name = "idCarrello") String idCarrello,
        @RequestParam(required = false) String info,
        @Parameter(description = "Pageable", required = true) @ParameterObject Pageable pageable
    ) {
        log.debug("REST request to search with params - pa: {}, nav: {}, iuv: {}, token: {}, idCarrello: {}, info: {}", pa, nav, iuv, token, idCarrello, info);

        try {
            // Validate sort fields
            ResponseEntity<String> errorMessage = PaymentUtil.validatePageable(pageable, PaymentUtil.SEARCH_SORT_MAPPING);
            if (errorMessage != null) return errorMessage;
             pageable =PaymentUtil.remapSorting(pageable, null, PaymentUtil.SEARCH_SORT_MAPPING, Sort.Order.desc("paEmittente"));

            int presentGroups = 0;

            if (iuv != null) presentGroups++;
            if (token != null) presentGroups++;
            if (idCarrello != null) presentGroups++;
            if (info != null) presentGroups++;

            if (presentGroups > 1) {
                log.error("Invalid search parameters: exactly one search group must be provided.");
                return ResponseEntity.badRequest().build();
            }

            Page<PositionPaymentExtraDTO> page = null;

            if (presentGroups == 0){
                if (pa != null ||  nav != null) {
                    page = sertService.searchByNav(nav, pa, pageable);
                } else  {
                    log.error("Provide PA or NAV for PA+NAV search.");
                    return ResponseEntity.badRequest().build();
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

            return creteREsponseEntity(page);
        } catch (Exception e) {
            log.error("Error occurred during search operation. Cause: {}, Message: {}", e.getClass().getSimpleName(), e.getMessage(), e);
            return ResponseEntity.status(500).body("An error occurred while processing your request. Please try again later.");
        }
    }

    private static ResponseEntity<?> creteREsponseEntity(Page<?> page) {
        if (page == null || page.getContent()==null || page.getContent().isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /position/{nav}/{pa-emittente}} : Ricerca di un codice avviso emesso da PA.
     *
     * @param nav the Codice Avviso.
     * @param paEmittente the Codice Fiscale Ente Creditore.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the positionPayment.
     */
    @GetMapping("/position/{nav}/{pa-emittente}")
    @Operation(tags = "Visualizzazione posizione debitoria")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.SERT_POSITION_DETAIL + "\")")
    public ResponseEntity<?> getPosition(
        @PathVariable("nav") String nav,
        @PathVariable("pa-emittente") String paEmittente,
        @Parameter(description = "Pageable", required = true) @ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get Position : {}, {}", nav, paEmittente);
        try {
            ResponseEntity<String> errorMessage = PaymentUtil.validatePageable(pageable, PaymentUtil.POSITION_TOKEN_SORT_MAPPING);
            if (errorMessage != null) return errorMessage;


            Pageable remappedPageable =PaymentUtil.remapSorting(pageable,null,PaymentUtil.POSITION_TOKEN_SORT_MAPPING,
                Sort.Order.desc("tokenDateEvent"));

            Page<PositionPaymentDTO> page = sertService.getPosition(nav, paEmittente,remappedPageable);
            if (page == null || page.getContent()==null || page.getContent().isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
            return ResponseEntity.ok().headers(headers).body(page.getContent().get(0));
        } catch (Exception e) {
            log.error("Error occurred while retrieving position details. Cause: {}, Message: {}", e.getClass().getSimpleName(), e.getMessage(), e);
            return ResponseEntity.status(500).body("An error occurred while processing your request. Please try again later.");
        }
    }

    /**
     * {@code GET  /token/{token}} : Visualizza il dettaglio di un singolo tentativo di pagamento.
     *
     * @param token the Token di ricerca.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the tokenInfo.
     */
    @GetMapping("/token/{token}")
    @Operation(tags = "Visualizzazione Dettagli")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.SERT_TOKEN_DETAIL + "\")")
    public ResponseEntity<?> getTokenInfo(@PathVariable("token") String token) {
        log.debug("REST request to get Token Info : {}", token);
        try {
            TokenInfoDTO tokenInfo = sertService.getTokenInfo(token);
            if (tokenInfo == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(tokenInfo);
        } catch (Exception e) {
            log.error("Error occurred while retrieving token information. Cause: {}, Message: {}", e.getClass().getSimpleName(), e.getMessage(), e);
            return ResponseEntity.status(500).body("An error occurred while processing your request. Please try again later.");
        }
    }

    /**
     * {@code GET  /transfers/{nav}/{pa-emittente}/{token}} : Visualizza transfer di un singolo tentativo di pagamento.
     *
     * @param nav the Codice Avviso.
     * @param paEmittente the Codice Fiscale Ente Creditore.
     * @param token the token.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the transferPayment.
     */
    @GetMapping("/transfers/{nav}/{pa-emittente}/{token}")
    @Operation(tags = "Visualizzazione Dettagli")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.SERT_TRANSFER_DETAIL + "\")")
    public ResponseEntity<?> getTransfers(
        @PathVariable("nav") String nav,
        @PathVariable("pa-emittente") String paEmittente,
        @PathVariable("token") String token,
        @Parameter(description = "Pageable", required = true) @ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get Transfers : {}, {}, {}", nav, paEmittente, token);
        try {
            // Validate transfer sort fields (frontend names) and check idTransfer is not passed
            ResponseEntity<String> errorMessage = PaymentUtil.validatePageable(pageable, PaymentUtil.TRANSFER_SORT_MAPPING);
            if (errorMessage != null) return errorMessage;


            Pageable remappedPageable =PaymentUtil.remapSorting(pageable,Sort.Order.asc("idTransfer"),PaymentUtil.TRANSFER_SORT_MAPPING,
                Sort.Order.desc("paTransfer"));
            Page<TransferPaymentDTO> page = sertService.getTransfers(nav, paEmittente, token, remappedPageable);

            if (page == null || page.getContent()==null || page.getContent().isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
            return ResponseEntity.ok().headers(headers).body(page.getContent().get(0));


        } catch (Exception e) {
            log.error("Error occurred while retrieving transfer details. Cause: {}, Message: {}", e.getClass().getSimpleName(), e.getMessage(), e);
            return ResponseEntity.status(500).body("An error occurred while processing your request. Please try again later.");
        }
    }




    /**
     * {@code GET  /workflows/{nav}/{pa-emittente}} : Recupero eventi di workflow di una posizione debitoria e relativi tentativi.
     *
     * @param nav the Codice Avviso.
     * @param paEmittente the Codice Fiscale Ente Creditore.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the workflowResponse.
     */
    @GetMapping("/workflows/{nav}/{pa-emittente}")
    @Operation(tags = "Visualizzazione Dettagli")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.SERT_WORKFLOW_DETAIL + "\")")
    public ResponseEntity<?> getWorkflows(
        @PathVariable("nav") String nav,
        @PathVariable("pa-emittente") String paEmittente,
        @Parameter(description = "Pageable", required = true) @ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get Workflows : {}, {}", nav, paEmittente);
        try {

            ResponseEntity<String> errorMessage = PaymentUtil.validatePageable(pageable, PaymentUtil.WORKFLOW_QUERY_TO_DTO_MAPPING);
            if (errorMessage != null) return errorMessage;

            Pageable remappedPageable =PaymentUtil.remapSorting(pageable, null, PaymentUtil.WORKFLOW_QUERY_TO_DTO_MAPPING, Sort.Order.desc("insertedtimestamp"));
            Page<WorkflowResponseDTO> page = sertService.getWorkflows(nav, paEmittente, remappedPageable);

            if (page == null || page.getContent()==null || page.getContent().isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
            return ResponseEntity.ok().headers(headers).body(page.getContent().get(0));


        } catch (Exception e) {
            log.error("Error occurred while retrieving workflow information. Cause: {}, Message: {}", e.getClass().getSimpleName(), e.getMessage(), e);
            return ResponseEntity.status(500).body("An error occurred while processing your request. Please try again later.");
        }
    }

    /**
     * {@code GET  /extra/{token}} : Visualizza informazioni extra su un tentativo di pagamento.
     *
     * @param token the Codice Avviso (token).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the extraInfoResponse.
     */
    @GetMapping("/extra/{token}")
    @Operation(tags = "Visualizzazione Dettagli")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.SERT_EXTRA_DETAIL + "\")")
    public ResponseEntity<?> getExtraInfo(@PathVariable("token") String token,
                                          @Parameter(description = "Pageable", required = true) @ParameterObject Pageable pageable) {
        log.debug("REST request to get Extra Info : {}", token);
        try {
            ResponseEntity<String> errorMessage = PaymentUtil.validatePageable(pageable, PaymentUtil.EXTRA_INFO_SORT_MAPPING);
            if (errorMessage != null) return errorMessage;

            Pageable remappedPageable =PaymentUtil.remapSorting(pageable, null, PaymentUtil.EXTRA_INFO_SORT_MAPPING, Sort.Order.desc("infoName"));
            Page<ExtraInfoResponseDTO> page = sertService.getExtraInfo(token, remappedPageable);
            return creteREsponseEntity(page);
        } catch (Exception e) {
            log.error("Error occurred while retrieving extra information. Cause: {}, Message: {}", e.getClass().getSimpleName(), e.getMessage(), e);
            return ResponseEntity.status(500).body("An error occurred while processing your request. Please try again later.");
        }
    }
}
