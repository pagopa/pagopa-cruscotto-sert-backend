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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import tech.jhipster.web.util.PaginationUtil;

@RestController
@RequestMapping("/sub/api/bulk/lookups")
public class SearchLookupSubResource {

    private final SearchLookupService service;

    public SearchLookupSubResource(SearchLookupService service) {
        this.service = service;
    }

    @GetMapping("/creditor-institutions")
    @Operation(summary = "Lookup creditor institutions (paged) - sub key public")
    public ResponseEntity<Page<AnagPaEmittente>> creditorInstitutions(@ParameterObject Pageable pageable) {
        Page<AnagPaEmittente> page = service.findPaEmittente(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page);
    }

    @GetMapping("/psp")
    @Operation(summary = "Lookup PSP (paged) - sub key public")
    public ResponseEntity<Page<AnagPsp>> psp(@ParameterObject Pageable pageable) {
        Page<AnagPsp> page = service.findPsp(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page);
    }

    @GetMapping("/intermediaries")
    @Operation(summary = "Lookup intermediaries (paged) - sub key public")
    public ResponseEntity<Page<AnagIntermediarioPa>> intermediaries(@ParameterObject Pageable pageable) {
        Page<AnagIntermediarioPa> page = service.findIntermediaries(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page);
    }

    @GetMapping("/intermediaries-psp")
    @Operation(summary = "Lookup intermediaries PSP (paged) - sub key public")
    public ResponseEntity<Page<AnagIntermediarioPsp>> intermediariesPsp(@ParameterObject Pageable pageable) {
        Page<AnagIntermediarioPsp> page = service.findIntermediariesPsp(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page);
    }

    @GetMapping("/stations")
    @Operation(summary = "Lookup stations (paged) - sub key public")
    public ResponseEntity<Page<AnagStazione>> stations(@ParameterObject Pageable pageable) {
        Page<AnagStazione> page = service.findStations(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page);
    }

    @GetMapping("/channels")
    @Operation(summary = "Lookup channels (paged) - sub key public")
    public ResponseEntity<Page<AnagCanale>> channels(@ParameterObject Pageable pageable) {
        Page<AnagCanale> page = service.findChannels(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page);
    }

    @GetMapping("/touchpoints")
    @Operation(summary = "Lookup touchpoints (paged) - sub key public")
    public ResponseEntity<Page<String>> touchpoints(@ParameterObject Pageable pageable) {
        Page<String> page = service.findTouchpoints(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page);
    }

    @GetMapping("/payment-methods")
    @Operation(summary = "Lookup payment methods (paged) - sub key public")
    public ResponseEntity<Page<String>> paymentMethods(@ParameterObject Pageable pageable) {
        Page<String> page = service.findPaymentMethods(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page);
    }
}
