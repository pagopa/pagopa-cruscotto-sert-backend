package com.nexigroup.pagopa.cruscotto.sert.web.rest.sertSearch.sertResourceSubKey;

import com.nexigroup.pagopa.cruscotto.sert.service.SertService;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.*;
import com.nexigroup.pagopa.cruscotto.sert.web.rest.sertSearch.SertSearchCommonResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for SERT APIs.
 */
@RestController
@RequestMapping(value = "/sub/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class SertSearchSubKeySearchResource extends SertSearchCommonResource {

    private final Logger log = LoggerFactory.getLogger(SertSearchSubKeySearchResource.class);

    public SertSearchSubKeySearchResource(SertService sertService) {
        super(sertService);
    }


    @GetMapping("/search")
    @Operation(tags = "Ricerca delle posizioni debitorie")
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
    public ResponseEntity<TokenInfoDTO> getTokenInfo(@PathVariable("token") String token) {
        log.info("CALL WITH SUBKEY GET POSITION");
        return super.getTokenInfo(token);
    }

    @GetMapping("/transfers/{nav}/{pa-emittente}/{token}")
    @Operation(tags = "Visualizzazione Dettagli")
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
    public ResponseEntity<ExtraInfoResponseDTO> getExtraInfo(@PathVariable("token") String token,
                                          @Parameter(description = "Pageable", required = true) @ParameterObject Pageable pageable) {
        log.info("CALL WITH SUBKEY GET EXTRA INFO");
        return super.getExtraInfo(token,pageable);
    }
}
