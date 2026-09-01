package com.nexigroup.pagopa.cruscotto.sert.web.rest.sertSearch.sertResourceSubKey;

import com.nexigroup.pagopa.cruscotto.sert.service.SearchInstanceAction;
import com.nexigroup.pagopa.cruscotto.sert.service.SearchInstanceService;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.SearchInstanceDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.csv.CsvValidationResult;
import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.validator.MassiveSearchCsvValidator;
import io.swagger.v3.oas.annotations.Operation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

@RestController
@RequestMapping("/sub/api")
@Validated
public class SearchInstanceSubKeyResource {

    private final SearchInstanceService service;
    private final MassiveSearchCsvValidator csvValidator;

    public SearchInstanceSubKeyResource(SearchInstanceService service, MassiveSearchCsvValidator csvValidator) {
        this.service = service;
        this.csvValidator = csvValidator;
    }

    @PostMapping(value = "/bulk/search-instances", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a new Search Instance (public /sub)")
    public ResponseEntity<SearchInstanceDTO> create(@RequestBody SearchInstanceDTO dto) {
        SearchInstanceDTO result = service.create(dto);
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/bulk/search-instances", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List Search Instances (public /sub)")
    public ResponseEntity<List<SearchInstanceDTO>> list( Pageable pageable) {
            Page<SearchInstanceDTO> page = service.findAll(pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
            return ResponseEntity.ok().headers(headers).body(page.getContent());
        }

    @GetMapping(value = "/bulk/search-instances/{id}")
    @Operation(summary = "Get Search Instance by id (public /sub)")
    public ResponseEntity<SearchInstanceDTO> get(@PathVariable("id") UUID id) {
        Optional<SearchInstanceDTO> dto = service.findOne(id);
        return dto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping(value = "/bulk/search-instances/{id}")
    @Operation(summary = "Update Search Instance (public /sub)")
    public ResponseEntity<SearchInstanceDTO> update(@PathVariable("id") UUID id, @RequestBody SearchInstanceDTO dto) {
        SearchInstanceDTO updated = service.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    // CSV upload (public)
    @PostMapping(value = "/bulk/search-instances/{id}/csv")
    @Operation(summary = "Upload CSV for Search Instance (public /sub)")
    public ResponseEntity<Void> uploadCsv(@PathVariable("id") UUID id, @RequestParam("file") MultipartFile file) {
        service.uploadCsv(id, file);
        return ResponseEntity.accepted().build();
    }

    // New endpoint: validate uploaded CSV file directly (pre-creation, no id)
    @PostMapping(value = "/bulk/search-instances/csv/validate-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Validate uploaded CSV file for Search Instance (pre-creation)")
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
    @PostMapping(value = "/bulk/search-instances/{id}/{action}")
    @Operation(summary = "Perform lifecycle action (restore|archive|duplicate)")
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
}
