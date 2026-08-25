package com.nexigroup.pagopa.cruscotto.sert.web.rest.sertSearch.sertResourceSubKey;

import com.nexigroup.pagopa.cruscotto.sert.service.SearchInstanceService;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.SearchInstanceDTO;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

@RestController
@RequestMapping("/sub/api")
@Validated
public class SearchInstanceSubKeyResource {

    private final SearchInstanceService service;

    public SearchInstanceSubKeyResource(SearchInstanceService service) {
        this.service = service;
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

}
