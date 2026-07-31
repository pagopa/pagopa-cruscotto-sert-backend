package com.nexigroup.pagopa.cruscotto.sert.web.rest.sertSearch.sertResourceJwtToken;

import com.nexigroup.pagopa.cruscotto.sert.security.AuthoritiesConstants;
import com.nexigroup.pagopa.cruscotto.sert.service.SertService;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.*;
import com.nexigroup.pagopa.cruscotto.sert.web.rest.sertSearch.SertResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for SERT APIs.
 */
@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class SertResourceJwtToken extends SertResource {

    private final Logger log = LoggerFactory.getLogger(SertResourceJwtToken.class);

    public SertResourceJwtToken(SertService sertService) {
        super(sertService);
    }


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
        log.info("CALL WITH SUBKEY SEARCH");
        return super.search(pa,nav,iuv,token,idCarrello,info,pageable);
    }


    @GetMapping("/position/{nav}/{pa-emittente}")
    @Operation(tags = "Visualizzazione posizione debitoria")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.SERT_POSITION_DETAIL + "\")")

    public ResponseEntity<PositionPaymentDTO> getPosition(
        @PathVariable("nav") String nav,
        @PathVariable("pa-emittente") String paEmittente,
        @Parameter(description = "Pageable", required = true) @ParameterObject Pageable pageable
    ) {
        log.info("CALL WITH SUBKEY GET POSITION");
        return super.getPosition(nav,paEmittente,pageable);
    }

    @GetMapping("/token/{token}")
    @Operation(tags = "Visualizzazione Dettagli")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.SERT_TOKEN_DETAIL + "\")")
    public ResponseEntity<TokenInfoDTO> getTokenInfo(@PathVariable("token") String token) {
        log.info("CALL WITH SUBKEY GET POSITION");
        return super.getTokenInfo(token);
    }

    @GetMapping("/transfers/{nav}/{pa-emittente}/{token}")
    @Operation(tags = "Visualizzazione Dettagli")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.SERT_TRANSFER_DETAIL + "\")")
    public ResponseEntity<TransferPaymentDTO> getTransfers(
        @PathVariable("nav") String nav,
        @PathVariable("pa-emittente") String paEmittente,
        @PathVariable("token") String token,
        @Parameter(description = "Pageable", required = true) @ParameterObject Pageable pageable
    ) {
        log.info("CALL WITH SUBKEY GET TRANSFERS");
        return super.getTransfers(nav, paEmittente, token, pageable);
    }

    @GetMapping("/workflows/{nav}/{pa-emittente}")
    @Operation(tags = "Visualizzazione Dettagli")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.SERT_WORKFLOW_DETAIL + "\")")
    public ResponseEntity<List<WorkflowObjectDTO>> getWorkflows(
        @PathVariable("nav") String nav,
        @PathVariable("pa-emittente") String paEmittente,
        @Parameter(description = "Pageable", required = true) @ParameterObject Pageable pageable
    ) {
        log.info("CALL WITH SUBKEY GET WORKFLOWS");
        return super.getWorkflows(nav,paEmittente,pageable);
    }

    @GetMapping("/extra/{token}")
    @Operation(tags = "Visualizzazione Dettagli")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.SERT_EXTRA_DETAIL + "\")")
    public ResponseEntity<ExtraInfoResponseDTO> getExtraInfo(@PathVariable("token") String token,
                                          @Parameter(description = "Pageable", required = true)
                                          @ParameterObject Pageable pageable) {
        log.info("CALL WITH SUBKEY GET EXTRA INFO");
        return super.getExtraInfo(token,pageable);
    }
}
