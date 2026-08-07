package com.nexigroup.pagopa.cruscotto.sert.web.rest.searchinstance;

import com.nexigroup.pagopa.cruscotto.sert.service.SearchInstanceAction;
import com.nexigroup.pagopa.cruscotto.sert.service.SearchInstanceService;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.SearchInstanceDTO;
import io.swagger.v3.oas.annotations.Operation;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@Validated
public class SearchInstanceApiResource {

    private final SearchInstanceService service;

    public SearchInstanceApiResource(SearchInstanceService service) {
        this.service = service;
    }

    @PostMapping(value = "/v1/search-instances", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a new Search Instance")
    @PreAuthorize("hasAuthority('GTW.SERT_MASS_SEARCH')")
    public ResponseEntity<SearchInstanceDTO> create(@RequestBody SearchInstanceDTO dto) throws URISyntaxException {
        SearchInstanceDTO result = service.create(dto);
        URI location = new URI("/api/v1/search-instances/" + (result != null && result.getId() != null ? result.getId() : ""));
        HttpHeaders headers = new HttpHeaders();
        return ResponseEntity.created(location).headers(headers).body(result);
    }

    @GetMapping(value = "/v1/search-instances", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List Search Instances")
    @PreAuthorize("hasAuthority('GTW.SERT_MASS_SEARCH')")
    public ResponseEntity<List<SearchInstanceDTO>> list() {
        List<SearchInstanceDTO> list = service.findAll();
        return ResponseEntity.ok(list);
    }

    @GetMapping(value = "/v1/search-instances/{id}")
    @Operation(summary = "Get Search Instance by id")
    @PreAuthorize("hasAuthority('GTW.SERT_MASS_SEARCH')")
    public ResponseEntity<SearchInstanceDTO> get(@PathVariable("id") UUID id) {
        Optional<SearchInstanceDTO> dto = service.findOne(id);
        return dto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping(value = "/v1/search-instances/{id}")
    @Operation(summary = "Update Search Instance")
    @PreAuthorize("hasAuthority('GTW.SERT_MASS_SEARCH')")
    public ResponseEntity<SearchInstanceDTO> update(@PathVariable("id") UUID id, @RequestBody SearchInstanceDTO dto) {
        SearchInstanceDTO updated = service.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping(value = "/v1/search-instances/{id}")
    @Operation(summary = "Delete Search Instance")
    @PreAuthorize("hasAuthority('GTW.SERT_MASS_SEARCH')")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Unified lifecycle endpoint: action in path (restore | archive | duplicate)
    @PostMapping(value = "/v1/search-instances/{id}/{action}")
    @Operation(summary = "Perform lifecycle action (restore|archive|duplicate)")
    @PreAuthorize("hasAuthority('GTW.SERT_MASS_SEARCH')")
    public ResponseEntity<?> lifecycleAction(@PathVariable("id") UUID id, @PathVariable("action") String action) {
        SearchInstanceAction act;
        try {
            act = SearchInstanceAction.valueOf(action.toUpperCase());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid action: " + action);
        }

        Optional<SearchInstanceDTO> maybe = service.performAction(id, act);
        if (act == SearchInstanceAction.DUPLICATE) {
            return maybe.map(dto -> {
                try {
                    URI location = new URI("/api/v1/search-instances/" + (dto.getId() != null ? dto.getId() : ""));
                    return ResponseEntity.status(HttpStatus.CREATED).location(location).body(dto);
                } catch (URISyntaxException e) {
                    return ResponseEntity.status(HttpStatus.CREATED).body(dto);
                }
            }).orElseGet(() -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        }

        return ResponseEntity.noContent().build();
    }

    // CSV
    @PostMapping(value = "/v1/search-instances/{id}/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload CSV for Search Instance")
    @PreAuthorize("hasAuthority('GTW.SERT_MASS_SEARCH')")
    public ResponseEntity<Void> uploadCsv(@PathVariable("id") UUID id, @RequestParam("file") MultipartFile file) {
        service.uploadCsv(id, file);
        return ResponseEntity.accepted().build();
    }

    @PostMapping(value = "/v1/search-instances/{id}/csv/validate")
    @Operation(summary = "Validate CSV for Search Instance")
    @PreAuthorize("hasAuthority('GTW.SERT_MASS_SEARCH')")
    public ResponseEntity<Boolean> validateCsv(@PathVariable("id") UUID id) {
        boolean ok = service.validateCsv(id);
        return ResponseEntity.ok(ok);
    }

    // Execute / rerun
    @PostMapping(value = "/v1/search-instances/{id}/execute")
    @Operation(summary = "Execute Search Instance (set READY)")
    @PreAuthorize("hasAuthority('GTW.SERT_MASS_SEARCH')")
    public ResponseEntity<Void> execute(@PathVariable("id") UUID id) {
        service.execute(id);
        return ResponseEntity.accepted().build();
    }

    @PostMapping(value = "/v1/search-instances/{id}/rerun")
    @Operation(summary = "Rerun Search Instance (set READY)")
    @PreAuthorize("hasAuthority('GTW.SERT_MASS_SEARCH')")
    public ResponseEntity<Void> rerun(@PathVariable("id") UUID id) {
        service.rerun(id);
        return ResponseEntity.accepted().build();
    }

    // Results
    @GetMapping(value = "/v1/search-instances/{id}/last-result")
    @Operation(summary = "Get last result metadata / availability")
    @PreAuthorize("hasAuthority('GTW.SERT_MASS_SEARCH')")
    public ResponseEntity<Void> getLastResult(@PathVariable("id") UUID id) {
        // TODO: return metadata – for now just 204 if present
        Optional<byte[]> maybe = service.getLastResult(id);
        return maybe.isPresent() ? ResponseEntity.ok().build() : ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/v1/search-instances/{id}/download")
    @Operation(summary = "Download last ZIP result")
    @PreAuthorize("hasAuthority('GTW.SERT_MASS_SEARCH')")
    public ResponseEntity<byte[]> download(@PathVariable("id") UUID id) {
        Optional<byte[]> maybe = service.getLastResult(id);
        return maybe.map(bytes -> ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(bytes))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
