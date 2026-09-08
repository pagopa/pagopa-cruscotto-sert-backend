package com.nexigroup.pagopa.cruscotto.sert.web.rest.sertSearch.sertResourceJwtToken;

import com.nexigroup.pagopa.cruscotto.sert.service.SearchInstanceAction;
import com.nexigroup.pagopa.cruscotto.sert.service.SearchInstanceService;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.SearchInstanceDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.csv.CsvValidationResult;
import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.validator.MassiveSearchCsvValidator;

import io.swagger.v3.oas.annotations.Operation;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.server.ResponseStatusException;
import tech.jhipster.web.util.PaginationUtil;

import java.io.InputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api")
@Validated
public class SearchInstanceJwtTokenResource {

    private final SearchInstanceService service;
    private final MassiveSearchCsvValidator csvValidator;

    public SearchInstanceJwtTokenResource(SearchInstanceService service, MassiveSearchCsvValidator csvValidator) {
        this.service = service;
        this.csvValidator = csvValidator;
    }

    @PostMapping(value = "/bulk/search-instances", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a new Search Instance")
    @PreAuthorize("hasAuthority('GTW.SERT_MASS_SEARCH')")
    public ResponseEntity<SearchInstanceDTO> create(@RequestBody SearchInstanceDTO dto) throws URISyntaxException {
        SearchInstanceDTO result = service.create(dto);
        URI location = new URI("/api/bulk/search-instances/" + (result != null && result.getId() != null ? result.getId() : ""));
        HttpHeaders headers = new HttpHeaders();
        return ResponseEntity.created(location).headers(headers).body(result);
    }

    @GetMapping(value = "/bulk/search-instances", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List Search Instances")
    @PreAuthorize("hasAuthority('GTW.SERT_MASS_SEARCH')")
    public ResponseEntity<List<SearchInstanceDTO>> list( Pageable pageable) {
        Page<SearchInstanceDTO> page = service.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping(value = "/bulk/search-instances/{id}")
    @Operation(summary = "Get Search Instance by id")
    @PreAuthorize("hasAuthority('GTW.SERT_MASS_SEARCH')")
    public ResponseEntity<SearchInstanceDTO> get(@PathVariable("id") UUID id) {
        Optional<SearchInstanceDTO> dto = service.findOne(id);
        return dto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping(value = "/bulk/search-instances/{id}")
    @Operation(summary = "Update Search Instance")
    @PreAuthorize("hasAuthority('GTW.SERT_MASS_SEARCH')")
    public ResponseEntity<SearchInstanceDTO> update(@PathVariable("id") UUID id, @RequestBody SearchInstanceDTO dto) {
        SearchInstanceDTO updated = service.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping(value = "/bulk/search-instances/{id}")
    @Operation(summary = "Delete Search Instance")
    @PreAuthorize("hasAuthority('GTW.SERT_MASS_SEARCH')")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }


    // CSV
    @PostMapping(value = "/bulk/search-instances/{id}/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload CSV for Search Instance")
    @PreAuthorize("hasAuthority('GTW.SERT_MASS_SEARCH')")
    public ResponseEntity<Void> uploadCsv(@PathVariable("id") UUID id, @RequestParam("file") MultipartFile file) {
        service.uploadCsv(id, file);
        return ResponseEntity.accepted().build();
    }



    // New endpoint: validate uploaded CSV file directly (pre-creation, no id)
    @PostMapping(value = "/bulk/search-instances/csv/validate-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Validate uploaded CSV file for Search Instance (pre-creation)")
    @PreAuthorize("hasAuthority('GTW.SERT_MASS_SEARCH')")
    public ResponseEntity<?> validateUploadedCsv(
        @RequestParam("file") MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("No file uploaded");
        }
        try (InputStream is = file.getInputStream()) {
            CsvValidationResult result = csvValidator.validate(is);
            if (result.valid()) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to read uploaded file", e);
        }
    }

    // Unified lifecycle endpoint: action in path (restore | archive | duplicate)
    @PostMapping(value = "/bulk/search-instances/{id}/{action}")
    @Operation(summary = "Perform lifecycle action (restore|archive|duplicate)")
    @PreAuthorize("hasAuthority('GTW.SERT_MASS_SEARCH')")
    public ResponseEntity<?> lifecycleAction(@PathVariable("id") UUID id, @PathVariable("action") SearchInstanceAction act) {

        Optional<SearchInstanceDTO> maybe = service.performAction(id, act);
        if (act == com.nexigroup.pagopa.cruscotto.sert.service.SearchInstanceAction.DUPLICATE) {
            return maybe.map(dto -> {
                try {
                    URI location = new URI("/api/bulk/search-instances/" + (dto.getId() != null ? dto.getId() : ""));
                    return ResponseEntity.status(HttpStatus.CREATED).location(location).body(dto);
                } catch (URISyntaxException e) {
                    return ResponseEntity.status(HttpStatus.CREATED).body(dto);
                }
            }).orElseGet(() -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        }

        return ResponseEntity.noContent().build();
    }


    // Execute / rerun
    @PostMapping(value = "/bulk/search-instances/{id}/execute")
    @Operation(summary = "Execute Search Instance (set READY)")
    @PreAuthorize("hasAuthority('GTW.SERT_MASS_SEARCH')")
    public ResponseEntity<Void> execute(@PathVariable("id") UUID id) {
        service.execute(id);
        return ResponseEntity.accepted().build();
    }

    @PostMapping(value = "/bulk/search-instances/{id}/rerun")
    @Operation(summary = "Rerun Search Instance (set READY)")
    @PreAuthorize("hasAuthority('GTW.SERT_MASS_SEARCH')")
    public ResponseEntity<Void> rerun(@PathVariable("id") UUID id) {
        service.rerun(id);
        return ResponseEntity.accepted().build();
    }


    @GetMapping(value = "/bulk/search-instances/{id}/download")
    @Operation(summary = "Download last ZIP result")
    @PreAuthorize("hasAuthority('GTW.SERT_MASS_SEARCH')")
    public ResponseEntity<byte[]> download(@PathVariable("id") UUID id) {
        Optional<byte[]> maybe = service.getLastResult(id);
        return maybe.map(bytes -> ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(bytes))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/bulk/search-instances/{id}/perimeter/download")
    @Operation(summary = "Download perimeter CSV for Search Instance")
    @PreAuthorize("hasAuthority('GTW.SERT_MASS_SEARCH')")
    public ResponseEntity<byte[]> downloadPerimeterCsv(@PathVariable("id") UUID id) {
        Optional<byte[]> maybe = service.downloadPerimeterCsv(id);
        return maybe.map(bytes -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
            ContentDisposition cd = ContentDisposition.builder("attachment")
                .filename("generated-perimeter-" + id + ".csv")
                .build();
            headers.setContentDisposition(cd);
            return ResponseEntity.ok().headers(headers).body(bytes);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

}
