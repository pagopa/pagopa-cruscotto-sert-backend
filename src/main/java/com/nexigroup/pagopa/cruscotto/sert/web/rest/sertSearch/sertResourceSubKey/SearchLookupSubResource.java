package com.nexigroup.pagopa.cruscotto.sert.web.rest.sertSearch.sertResourceSubKey;

import com.nexigroup.pagopa.cruscotto.sert.service.SearchLookupService;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.SearchLookupDTO;
import io.swagger.v3.oas.annotations.Operation;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import tech.jhipster.web.util.PaginationUtil;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/sub/api/bulk/lookups")
public class SearchLookupSubResource {

    private final SearchLookupService service;

    public SearchLookupSubResource(SearchLookupService service) {
        this.service = service;
    }

    @GetMapping("/creditor-institutions")
    @Operation(summary = "Lookup creditor institutions (paged) - sub key public")
    public ResponseEntity<List<SearchLookupDTO>> creditorInstitutions(
        @RequestParam(name = "search", required = false) String search,
        @ParameterObject Pageable pageable
    ) {
        Page<SearchLookupDTO> page = service.findPaEmittente(search, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/psp")
    @Operation(summary = "Lookup PSP (paged) - sub key public")
    public ResponseEntity<List<SearchLookupDTO>> psp(
        @RequestParam(name = "search", required = false) String search,
        @ParameterObject Pageable pageable
    ) {
        Page<SearchLookupDTO> page = service.findPsp(search, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/intermediaries")
    @Operation(summary = "Lookup intermediaries (paged) - sub key public")
    public ResponseEntity<List<SearchLookupDTO>> intermediaries(
        @RequestParam(name = "search", required = false) String search,
        @ParameterObject Pageable pageable
    ) {
        Page<SearchLookupDTO> page = service.findIntermediaries(search, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/intermediaries-psp")
    @Operation(summary = "Lookup intermediaries PSP (paged) - sub key public")
    public ResponseEntity<List<SearchLookupDTO>> intermediariesPsp(
        @RequestParam(name = "search", required = false) String search,
        @ParameterObject Pageable pageable
    ) {
        Page<SearchLookupDTO> page = service.findIntermediariesPsp(search, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/stations")
    @Operation(summary = "Lookup stations (paged) - sub key public")
    public ResponseEntity<List<SearchLookupDTO>> stations(
        @RequestParam(name = "search", required = false) String search,
        @ParameterObject Pageable pageable
    ) {
        Page<SearchLookupDTO> page = service.findStations(search, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/channels")
    @Operation(summary = "Lookup channels (paged) - sub key public")
    public ResponseEntity<List<SearchLookupDTO>> channels(
        @RequestParam(name = "search", required = false) String search,
        @ParameterObject Pageable pageable
    ) {
        Page<SearchLookupDTO> page = service.findChannels(search, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/touchpoints")
    @Operation(summary = "Lookup touchpoints (paged) - sub key public")
    public ResponseEntity<List<SearchLookupDTO>> touchpoints(
        @RequestParam(name = "search", required = false) String search,
        @ParameterObject Pageable pageable
    ) {
        Page<SearchLookupDTO> page = service.findTouchpoints(search, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/payment-methods")
    @Operation(summary = "Lookup payment methods (paged) - sub key public")
    public ResponseEntity<List<SearchLookupDTO>> paymentMethods(
        @RequestParam(name = "search", required = false) String search,
        @ParameterObject Pageable pageable
    ) {
        Page<SearchLookupDTO> page = service.findPaymentMethods(search, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/creditor-institutions/{id}")
    @Operation(summary = "Get creditor institution by id - sub key public")
    public ResponseEntity<SearchLookupDTO> creditorInstitutionById(@PathVariable Long id) {
        Optional<SearchLookupDTO> result = service.findPaEmittenteById(id);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/psp/{id}")
    @Operation(summary = "Get PSP by id - sub key public")
    public ResponseEntity<SearchLookupDTO> pspById(@PathVariable Long id) {
        Optional<SearchLookupDTO> result = service.findPspById(id);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/intermediaries/{id}")
    @Operation(summary = "Get intermediary by id - sub key public")
    public ResponseEntity<SearchLookupDTO> intermediaryById(@PathVariable Long id) {
        Optional<SearchLookupDTO> result = service.findIntermediaryById(id);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/intermediaries-psp/{id}")
    @Operation(summary = "Get PSP intermediary by id - sub key public")
    public ResponseEntity<SearchLookupDTO> intermediaryPspById(@PathVariable Long id) {
        Optional<SearchLookupDTO> result = service.findIntermediaryPspById(id);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/stations/{id}")
    @Operation(summary = "Get station by id - sub key public")
    public ResponseEntity<SearchLookupDTO> stationById(@PathVariable Long id) {
        Optional<SearchLookupDTO> result = service.findStationsById(id);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/channels/{id}")
    @Operation(summary = "Get channel by id - sub key public")
    public ResponseEntity<SearchLookupDTO> channelById(@PathVariable Long id) {
        Optional<SearchLookupDTO> result = service.findChannelById(id);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/touchpoints/{id}")
    @Operation(summary = "Get touchpoint by id - sub key public")
    public ResponseEntity<SearchLookupDTO> touchpointById(@PathVariable Long id) {
        Optional<SearchLookupDTO> result = service.findTouchpointById(id);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/payment-methods/{id}")
    @Operation(summary = "Get payment method by id - sub key public")
    public ResponseEntity<SearchLookupDTO> paymentMethodById(@PathVariable Long id) {
        Optional<SearchLookupDTO> result = service.findPaymentMethodById(id);
        return result.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
