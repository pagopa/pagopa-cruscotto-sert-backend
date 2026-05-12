package com.nexigroup.pagopa.cruscotto.sert.web.rest;

import com.nexigroup.pagopa.cruscotto.sert.service.SertService;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;

/**
 * REST controller for SERT APIs.
 */
@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class SertResource {

    private final Logger log = LoggerFactory.getLogger(SertResource.class);

    private final SertService sertService;

    public SertResource(SertService sertService) {
        this.sertService = sertService;
    }

    /**
     * {@code GET  /search} : Unified search API.
     *
     * @param pa the Codice Fiscale Ente Creditore.
     * @param nav the Codice Avviso.
     * @param iuv the IUV o Creditor Reference ID.
     * @param token the Token di pagamento.
     * @param idCarrello the ID Carrello.
     * @param info the Valore per ricerca extra.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the unifiedSearchResponse.
     */
    @GetMapping("/search")
    @Operation(tags = "Ricerca delle posizioni debitorie")
    public ResponseEntity<UnifiedSearchResponseDTO> search(
        @RequestParam(required = false) String pa,
        @RequestParam(required = false) String nav,
        @RequestParam(required = false) String iuv,
        @RequestParam(required = false) String token,
        @RequestParam(required = false, name = "idCarrello") String idCarrello,
        @RequestParam(required = false) String info
    ) {
        log.debug("REST request to search with params - pa: {}, nav: {}, iuv: {}, token: {}, idCarrello: {}, info: {}", pa, nav, iuv, token, idCarrello, info);

        int presentGroups = 0;

        if (iuv != null) presentGroups++;
        if (token != null) presentGroups++;
        if (idCarrello != null) presentGroups++;
        if (info != null) presentGroups++;

        if (presentGroups > 1) {
            log.error("Invalid search parameters: exactly one search group must be provided.");
            return ResponseEntity.badRequest().build();
        }


        if (presentGroups == 0){
            if (pa != null ||  nav != null) {
                return ResponseEntity.ok(sertService.searchByNav(nav, pa));
            } else  {
                log.error("Provide PA or NAV for PA+NAV search.");
                return ResponseEntity.badRequest().build();
            }
        }

        if (iuv != null) {
            return ResponseEntity.ok(sertService.searchByIuv(pa, nav, iuv));
        }

        if (token != null) {
            return ResponseEntity.ok(sertService.searchByToken(pa, nav, token));
        }

        if (idCarrello != null) {
            return ResponseEntity.ok(sertService.searchByCart(pa, nav, idCarrello));
        }

        if (info != null) {
            return ResponseEntity.ok(sertService.searchExtra(pa, nav, info));
        }

        return ResponseEntity.badRequest().build();
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
    public ResponseEntity<PositionPaymentDTO> getPosition(
        @PathVariable("nav") String nav,
        @PathVariable("pa-emittente") String paEmittente
    ) {
        log.debug("REST request to get Position : {}, {}", nav, paEmittente);
        PositionPaymentDTO positionPayment = sertService.getPosition(nav, paEmittente);
        if (positionPayment == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(positionPayment);
    }

    /**
     * {@code GET  /token/{token}} : Visualizza il dettaglio di un singolo tentativo di pagamento.
     *
     * @param token the Token di ricerca.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the tokenInfo.
     */
    @GetMapping("/token/{token}")
    @Operation(tags = "Visualizzazione Dettagli")
    public ResponseEntity<TokenInfoDTO> getTokenInfo(@PathVariable("token") String token) {
        log.debug("REST request to get Token Info : {}", token);
        TokenInfoDTO tokenInfo = sertService.getTokenInfo(token);
        if (tokenInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tokenInfo);
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
    public ResponseEntity<TransferPaymentDTO> getTransfers(
        @PathVariable("nav") String nav,
        @PathVariable("pa-emittente") String paEmittente,
        @PathVariable("token") String token
    ) {
        log.debug("REST request to get Transfers : {}, {}, {}", nav, paEmittente, token);
        TransferPaymentDTO transferPayment = sertService.getTransfers(nav, paEmittente, token);
        if (transferPayment == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(transferPayment);
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
    @Operation(tags = "Visualizzazione Dettagli")
    public ResponseEntity<ExtraInfoResponseDTO> getExtraInfo(@PathVariable("token") String token) {
        log.debug("REST request to get Extra Info : {}", token);
        return ResponseEntity.ok(sertService.getExtraInfo(token));
    }
}
