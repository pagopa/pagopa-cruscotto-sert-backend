package com.nexigroup.pagopa.cruscotto.sert.web.rest.searchinstance;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bulk/lookups")
public class SearchLookupResource {

    @GetMapping("/creditor-institutions")
    @Operation(summary = "Lookup creditor institutions")
    @PreAuthorize("hasAuthority('GTW.SERT_MASS_SEARCH')")
    public ResponseEntity<List<String>> creditorInstitutions() {
        // TODO: implement lookup
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/psp")
    @Operation(summary = "Lookup PSP")
    @PreAuthorize("hasAuthority('GTW.SERT_MASS_SEARCH')")
    public ResponseEntity<List<String>> psp() {
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/intermediaries")
    @Operation(summary = "Lookup intermediaries")
    @PreAuthorize("hasAuthority('GTW.SERT_MASS_SEARCH')")
    public ResponseEntity<List<String>> intermediaries() {
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/stations")
    @Operation(summary = "Lookup stations")
    @PreAuthorize("hasAuthority('GTW.SERT_MASS_SEARCH')")
    public ResponseEntity<List<String>> stations() {
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/channels")
    @Operation(summary = "Lookup channels")
    @PreAuthorize("hasAuthority('GTW.SERT_MASS_SEARCH')")
    public ResponseEntity<List<String>> channels() {
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/touchpoints")
    @Operation(summary = "Lookup touchpoints")
    @PreAuthorize("hasAuthority('GTW.SERT_MASS_SEARCH')")
    public ResponseEntity<List<String>> touchpoints() {
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/payment-methods")
    @Operation(summary = "Lookup payment methods")
    @PreAuthorize("hasAuthority('GTW.SERT_MASS_SEARCH')")
    public ResponseEntity<List<String>> paymentMethods() {
        return ResponseEntity.ok(List.of());
    }
}
