package com.nexigroup.pagopa.cruscotto.sert.web.rest;

import com.nexigroup.pagopa.cruscotto.sert.service.SertService;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for SERT APIs.
 */
@RestController
@RequestMapping("/api")
public class SertResource {

    private final Logger log = LoggerFactory.getLogger(SertResource.class);

    private final SertService sertService;

    public SertResource(SertService sertService) {
        this.sertService = sertService;
    }

    /**
     * {@code GET  /search/nav/{nav}} : Ricerca per Codice Avviso.
     *
     * @param nav the Codice Avviso.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the searchResultsResponse.
     */
    @GetMapping("/search/nav/{nav}")
    public ResponseEntity<SearchResultsResponseDTO> searchByNav(@PathVariable("nav") String nav) {
        log.debug("REST request to search by NAV : {}", nav);
        return ResponseEntity.ok(sertService.searchByNav(nav));
    }

    /**
     * {@code GET  /search/iuv/{iuv}} : Ricerca per IUV o Creditor Reference ID.
     *
     * @param iuv the IUV o Creditor Reference ID.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the searchResultsResponse.
     */
    @GetMapping("/search/iuv/{iuv}")
    public ResponseEntity<SearchResultsResponseDTO> searchByIuv(@PathVariable("iuv") String iuv) {
        log.debug("REST request to search by IUV : {}", iuv);
        return ResponseEntity.ok(sertService.searchByIuv(iuv));
    }

    /**
     * {@code GET  /search/cart/{id_cart}} : Ricerca per ID CARRELLO.
     *
     * @param idCart the ID Carrello.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the searchResultsResponse.
     */
    @GetMapping("/search/cart/{id_cart}")
    public ResponseEntity<SearchResultsResponseDTO> searchByCart(@PathVariable("id_cart") String idCart) {
        log.debug("REST request to search by Cart : {}", idCart);
        return ResponseEntity.ok(sertService.searchByCart(idCart));
    }

    /**
     * {@code GET  /search/token/{token}} : Ricerca per Token.
     *
     * @param token the Token di pagamento.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the searchResultsResponse.
     */
    @GetMapping("/search/token/{token}")
    public ResponseEntity<SearchResultsResponseDTO> searchByToken(@PathVariable("token") String token) {
        log.debug("REST request to search by Token : {}", token);
        return ResponseEntity.ok(sertService.searchByToken(token));
    }

    /**
     * {@code GET  /search/extra/{searchValue}} : Ricerca per rrn/pspTransactionId ed altro.
     *
     * @param searchValue the Valore da ricercare.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the searchResultsExtraResponse.
     */
    @GetMapping("/search/extra/{searchValue}")
    public ResponseEntity<SearchResultsExtraResponseDTO> searchExtra(@PathVariable("searchValue") String searchValue) {
        log.debug("REST request to search extra : {}", searchValue);
        return ResponseEntity.ok(sertService.searchExtra(searchValue));
    }

    /**
     * {@code GET  /position/{nav}/{pa-emittente}} : Ricerca di un codice avviso emesso da PA.
     *
     * @param nav the Codice Avviso.
     * @param paEmittente the Codice Fiscale Ente Creditore.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the positionPayment.
     */
    @GetMapping("/position/{nav}/{pa-emittente}")
    public ResponseEntity<PositionPaymentDTO> getPosition(
        @PathVariable("nav") String nav,
        @PathVariable("pa-emittente") String paEmittente
    ) {
        log.debug("REST request to get Position : {}, {}", nav, paEmittente);
        return ResponseEntity.ok(sertService.getPosition(nav, paEmittente));
    }

    /**
     * {@code GET  /token/{token}} : Visualizza il dettaglio di un singolo tentativo di pagamento.
     *
     * @param token the Token di ricerca.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the tokenInfo.
     */
    @GetMapping("/token/{token}")
    public ResponseEntity<TokenInfoDTO> getTokenInfo(@PathVariable("token") String token) {
        log.debug("REST request to get Token Info : {}", token);
        return ResponseEntity.ok(sertService.getTokenInfo(token));
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
    public ResponseEntity<TransferPaymentDTO> getTransfers(
        @PathVariable("nav") String nav,
        @PathVariable("pa-emittente") String paEmittente,
        @PathVariable("token") String token
    ) {
        log.debug("REST request to get Transfers : {}, {}, {}", nav, paEmittente, token);
        return ResponseEntity.ok(sertService.getTransfers(nav, paEmittente, token));
    }

    /**
     * {@code GET  /workflows/{nav}/{pa-emittente}} : Recupero eventi di workflow di una posizione debitoria e relativi tentativi.
     *
     * @param nav the Codice Avviso.
     * @param paEmittente the Codice Fiscale Ente Creditore.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the workflowResponse.
     */
    @GetMapping("/workflows/{nav}/{pa-emittente}")
    public ResponseEntity<WorkflowResponseDTO> getWorkflows(
        @PathVariable("nav") String nav,
        @PathVariable("pa-emittente") String paEmittente
    ) {
        log.debug("REST request to get Workflows : {}, {}", nav, paEmittente);
        return ResponseEntity.ok(sertService.getWorkflows(nav, paEmittente));
    }

    /**
     * {@code GET  /extra/{token}} : Visualizza informazioni extra su un tentativo di pagamento.
     *
     * @param token the Codice Avviso (token).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the extraInfoResponse.
     */
    @GetMapping("/extra/{token}")
    public ResponseEntity<ExtraInfoResponseDTO> getExtraInfo(@PathVariable("token") String token) {
        log.debug("REST request to get Extra Info : {}", token);
        return ResponseEntity.ok(sertService.getExtraInfo(token));
    }
}
