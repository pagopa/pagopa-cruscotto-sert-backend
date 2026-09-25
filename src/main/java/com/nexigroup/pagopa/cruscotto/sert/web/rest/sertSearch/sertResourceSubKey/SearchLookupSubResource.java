package com.nexigroup.pagopa.cruscotto.sert.web.rest.sertSearch.sertResourceSubKey;

import com.nexigroup.pagopa.cruscotto.sert.domain.AnagCanale;
import com.nexigroup.pagopa.cruscotto.sert.domain.AnagIntermediarioPa;
import com.nexigroup.pagopa.cruscotto.sert.domain.AnagIntermediarioPsp;
import com.nexigroup.pagopa.cruscotto.sert.domain.AnagPaEmittente;
import com.nexigroup.pagopa.cruscotto.sert.domain.AnagPsp;
import com.nexigroup.pagopa.cruscotto.sert.domain.AnagStazione;
import com.nexigroup.pagopa.cruscotto.sert.service.SearchLookupService;
import io.swagger.v3.oas.annotations.Operation;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import tech.jhipster.web.util.PaginationUtil;

import java.util.List;

@RestController
@RequestMapping("/sub/api/bulk/lookups")
public class SearchLookupSubResource {

    private final SearchLookupService service;

    public SearchLookupSubResource(SearchLookupService service) {
        this.service = service;
    }

    @GetMapping("/creditor-institutions")
    @Operation(summary = "Lookup creditor institutions (paged) - sub key public")
    public ResponseEntity<List<AnagPaEmittente>> creditorInstitutions(
        @RequestParam(name = "search", required = false) String search,
        @ParameterObject Pageable pageable
    ) {
        Page<AnagPaEmittente> page = service.findPaEmittente(search, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/psp")
    @Operation(summary = "Lookup PSP (paged) - sub key public")
    public ResponseEntity<List<AnagPsp>> psp(
        @RequestParam(name = "search", required = false) String search,
        @ParameterObject Pageable pageable
    ) {
        Page<AnagPsp> page = service.findPsp(search, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/intermediaries")
    @Operation(summary = "Lookup intermediaries (paged) - sub key public")
    public ResponseEntity<List<AnagIntermediarioPa>> intermediaries(
        @RequestParam(name = "search", required = false) String search,
        @ParameterObject Pageable pageable
    ) {
        Page<AnagIntermediarioPa> page = service.findIntermediaries(search, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/intermediaries-psp")
    @Operation(summary = "Lookup intermediaries PSP (paged) - sub key public")
    public ResponseEntity<List<AnagIntermediarioPsp>> intermediariesPsp(
        @RequestParam(name = "search", required = false) String search,
        @ParameterObject Pageable pageable
    ) {
        Page<AnagIntermediarioPsp> page = service.findIntermediariesPsp(search, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/stations")
    @Operation(summary = "Lookup stations (paged) - sub key public")
    public ResponseEntity<List<AnagStazione>> stations(
        @RequestParam(name = "search", required = false) String search,
        @ParameterObject Pageable pageable
    ) {
        Page<AnagStazione> page = service.findStations(search, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/channels")
    @Operation(summary = "Lookup channels (paged) - sub key public")
    public ResponseEntity<List<AnagCanale>> channels(
        @RequestParam(name = "search", required = false) String search,
        @ParameterObject Pageable pageable
    ) {
        Page<AnagCanale> page = service.findChannels(search, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/touchpoints")
    @Operation(summary = "Lookup touchpoints (paged) - sub key public")
    public ResponseEntity<List<String>> touchpoints(
        @RequestParam(name = "search", required = false) String search,
        @ParameterObject Pageable pageable
    ) {
        Page<String> page = service.findTouchpoints(search, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/payment-methods")
    @Operation(summary = "Lookup payment methods (paged) - sub key public")
    public ResponseEntity<List<String>> paymentMethods(
        @RequestParam(name = "search", required = false) String search,
        @ParameterObject Pageable pageable
    ) {
        Page<String> page = service.findPaymentMethods(search, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
