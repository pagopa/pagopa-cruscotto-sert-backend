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
    @GetMapping("/search")
    @Operation(tags = "Ricerca delle posizioni debitorie")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.SERT_SEARCH + "\")")
    public ResponseEntity<List<PositionPaymentExtraDTO>> search(
        @RequestParam(required = false) String pa,
        @RequestParam(required = false) String nav,
        @RequestParam(required = false) String iuv,
        @RequestParam(required = false) String token,
        @RequestParam(required = false, name = "idCarrello") String idCarrello,
        @RequestParam(required = false) String info,
        @Parameter(description = "Pageable", required = true) @ParameterObject Pageable pageable
    ) {
        log.info("START REST request to search with params - pa: {}, nav: {}, iuv: {}, token: {}, idCarrello: {}, info: {}", pa, nav, iuv, token, idCarrello, info);

        try {
            // Validate sort fields
            PaymentUtil.validatePageable(pageable, PaymentUtil.SEARCH_SORT_MAPPING);

            pageable =PaymentUtil.remapSorting(pageable, null, PaymentUtil.SEARCH_SORT_MAPPING, Sort.Order.desc("paEmittente"));

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
            log.error("Error occurred during search operation. Cause: {}, Message: {}", e.getClass().getSimpleName(), e.getMessage(), e);
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An error occurred while processing your request. Please try again later."
            );
        }
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
    public ResponseEntity<PositionPaymentDTO> getPosition(
        @PathVariable("nav") String nav,
        @PathVariable("pa-emittente") String paEmittente,
        @Parameter(description = "Pageable", required = true) @ParameterObject Pageable pageable
    ) {
        log.info ("START REST request to get Position : {}, {}", nav, paEmittente);
        try {
            PaymentUtil.validatePageable(pageable, PaymentUtil.POSITION_TOKEN_SORT_MAPPING);

            Pageable remappedPageable =PaymentUtil.remapSorting(pageable,null,PaymentUtil.POSITION_TOKEN_SORT_MAPPING,
                Sort.Order.desc("tokenDateEvent"));

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
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An error occurred while processing your request. Please try again later.");
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
    public ResponseEntity<TokenInfoDTO> getTokenInfo(@PathVariable("token") String token) {
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
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An error occurred while processing your request. Please try again later.");
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
    public ResponseEntity<TransferPaymentDTO> getTransfers(
        @PathVariable("nav") String nav,
        @PathVariable("pa-emittente") String paEmittente,
        @PathVariable("token") String token,
        @Parameter(description = "Pageable", required = true) @ParameterObject Pageable pageable
    ) {
        log.info("START REST request to get Transfers : {}, {}, {}", nav, paEmittente, token);
        try {
            // Validate transfer sort fields (frontend names) and check idTransfer is not passed
            PaymentUtil.validatePageable(pageable, PaymentUtil.TRANSFER_SORT_MAPPING);


            Pageable remappedPageable =PaymentUtil.remapSorting(pageable,Sort.Order.asc("idTransfer"),PaymentUtil.TRANSFER_SORT_MAPPING,
                Sort.Order.desc("paTransfer"));
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
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An error occurred while processing your request. Please try again later.");
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
    public ResponseEntity<WorkflowResponseDTO> getWorkflows(
        @PathVariable("nav") String nav,
        @PathVariable("pa-emittente") String paEmittente,
        @Parameter(description = "Pageable", required = true) @ParameterObject Pageable pageable
    ) {
        log.info("START REST request to get Workflows : {}, {}", nav, paEmittente);
        try {

           PaymentUtil.validatePageable(pageable, PaymentUtil.WORKFLOW_QUERY_TO_DTO_MAPPING);


            Pageable remappedPageable =PaymentUtil.remapSorting(pageable, null,
                PaymentUtil.WORKFLOW_QUERY_TO_DTO_MAPPING,
                Sort.Order.desc("insertedtimestamp"));

            Page<WorkflowResponseDTO> page = sertService.getWorkflows(nav, paEmittente, remappedPageable);

            if (page == null || page.getContent()==null || page.getContent().isEmpty()) {
                throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    ""
                );
            }

            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
            log.info("END REST request to get Workflows ");

            return ResponseEntity.ok().headers(headers).body(page.getContent().get(0));


        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An error occurred while processing your request. Please try again later.");
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
    public ResponseEntity<ExtraInfoResponseDTO> getExtraInfo(@PathVariable("token") String token,
                                          @Parameter(description = "Pageable", required = true) @ParameterObject Pageable pageable) {
        log.info("START REST request to get Extra Info : {}", token);
        try {
            PaymentUtil.validatePageable(pageable, PaymentUtil.EXTRA_INFO_SORT_MAPPING);


            Pageable remappedPageable =PaymentUtil.remapSorting(pageable, null, PaymentUtil.EXTRA_INFO_SORT_MAPPING, Sort.Order.desc("infoName"));
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
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An error occurred while processing your request. Please try again later.");
        }
    }
}
