package com.nexigroup.pagopa.cruscotto.sert.web.rest.sertSearch.sertResourceSubKey;

import com.nexigroup.pagopa.cruscotto.sert.config.ApplicationProperties;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.*;
import com.nexigroup.pagopa.cruscotto.sert.service.impl.SertHistoryDataServiceImpl;
import com.nexigroup.pagopa.cruscotto.sert.service.impl.SertOnlineServiceImpl;
import com.nexigroup.pagopa.cruscotto.sert.web.rest.sertSearch.SertSearchCommonResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.LocalDate;

/**
 * REST controller for SERT APIs.
 */
@RestController
@RequestMapping(value = "/sub/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class SertSearchSubKeyResource extends SertSearchCommonResource {

    public SertSearchSubKeyResource(
        SertOnlineServiceImpl sertService,
        SertHistoryDataServiceImpl sertHistoryDataService,
        ApplicationProperties properties
    ) {
        super(sertService, sertHistoryDataService, properties.getSearch().getDateRangeDeltaDays());
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
        @RequestParam(required = false, name = "fromHistory", defaultValue = "false") boolean searchFromHistory,
        @RequestParam(required = false, name = "fromOnline", defaultValue = "true") boolean searchFromOnline,
        @RequestParam(required = false, name = "dateFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
        @RequestParam(required = false, name = "dateTo") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
        @Parameter(description = "Pageable", required = true) @ParameterObject Pageable pageable
    ) {
        return super.search(pa,nav,iuv,token,idCarrello,info, searchFromHistory,searchFromOnline,dateFrom,dateTo,pageable);
    }


    @GetMapping("/position/{nav}/{pa-emittente}")
    @Operation(tags = "Visualizzazione posizione debitoria")
    public ResponseEntity<PositionPaymentDTO> getPosition(
        @PathVariable("nav") String nav,
        @PathVariable("pa-emittente") String paEmittente,
        @RequestParam(required = false, name = "fromHistory", defaultValue = "false") boolean searchFromHistory,
        @RequestParam(required = false, name = "dateFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
        @RequestParam(required = false, name = "dateTo") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
        @Parameter(description = "Pageable", required = true) @ParameterObject Pageable pageable
    ) {
        return super.getPosition(nav, paEmittente, searchFromHistory, dateFrom, dateTo, pageable);
    }

    @GetMapping("/token/{token}")
    @Operation(tags = "Visualizzazione Dettagli")
    public ResponseEntity<TokenInfoDTO> getTokenInfo(
        @PathVariable("token") String token,
        @RequestParam(required = false, name = "fromHistory", defaultValue = "false") boolean searchFromHistory,
        @RequestParam(required = false, name = "dateFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
        @RequestParam(required = false, name = "dateTo") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        return super.getTokenInfo(token, searchFromHistory,dateFrom,dateTo);
    }

    @GetMapping("/transfers/{nav}/{pa-emittente}/{token}")
    @Operation(tags = "Visualizzazione Dettagli")
    public ResponseEntity<TransferPaymentDTO> getTransfers(
        @PathVariable("nav") String nav,
        @PathVariable("pa-emittente") String paEmittente,
        @PathVariable("token") String token,
        @RequestParam(required = false, name = "fromHistory", defaultValue = "false") boolean searchFromHistory,
        @RequestParam(required = false, name = "dateFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
        @RequestParam(required = false, name = "dateTo") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
        @Parameter(description = "Pageable", required = true) @ParameterObject Pageable pageable
    ) {
        return super.getTransfers(nav, paEmittente, token, searchFromHistory,dateFrom,dateTo, pageable);
    }

    @GetMapping("/workflows/{nav}/{pa-emittente}")
    @Operation(tags = "Visualizzazione Dettagli")
    public ResponseEntity<List<WorkflowObjectDTO>> getWorkflows(
        @PathVariable("nav") String nav,
        @PathVariable("pa-emittente") String paEmittente,
        @RequestParam(required = false, name = "fromHistory", defaultValue = "false") boolean searchFromHistory,
        @RequestParam(required = false, name = "dateFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
        @RequestParam(required = false, name = "dateTo") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
        @Parameter(description = "Pageable", required = true) @ParameterObject Pageable pageable
    ) {
        return super.getWorkflows(nav, paEmittente, searchFromHistory,dateFrom,dateTo, pageable);
    }

    @GetMapping("/extra/{token}")
    @Operation(tags = "Visualizzazione Dettagli")
    public ResponseEntity<ExtraInfoResponseDTO> getExtraInfo(
        @PathVariable("token") String token,
        @RequestParam(required = false, name = "fromHistory", defaultValue = "false") boolean searchFromHistory,
        @RequestParam(required = false, name = "dateFrom") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
        @RequestParam(required = false, name = "dateTo") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
        @Parameter(description = "Pageable", required = true) @ParameterObject Pageable pageable
    ) {
        return super.getExtraInfo(token, searchFromHistory,dateFrom,dateTo, pageable);
    }
}
