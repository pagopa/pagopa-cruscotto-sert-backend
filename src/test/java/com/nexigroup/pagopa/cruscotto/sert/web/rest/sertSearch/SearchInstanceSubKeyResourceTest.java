package com.nexigroup.pagopa.cruscotto.sert.web.rest.sertSearch.sertResourceSubKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.domain.enumeration.PerimeterSearchType;
import com.nexigroup.pagopa.cruscotto.sert.service.SearchInstanceAction;
import com.nexigroup.pagopa.cruscotto.sert.service.SearchInstanceService;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.SearchInstanceDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.SearchInstanceWriteDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.SearchResultDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.WrapperFileResultDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.csv.CsvValidationResult;
import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.csv.CsvTemplate;
import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.csv.WrapperInstanceCsv;
import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.validator.MassiveSearchCsvValidator;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

class SearchInstanceSubKeyResourceTest {

    private final SearchInstanceService service = mock(SearchInstanceService.class);
    private final MassiveSearchCsvValidator csvValidator = mock(MassiveSearchCsvValidator.class);
    private final SearchInstanceSubKeyResource resource = new SearchInstanceSubKeyResource(service, csvValidator);
    private final UUID instanceId = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @BeforeEach
    void setUpRequestContext() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
    }

    @Test
    void createsSearchInstance() {
        SearchInstanceWriteDTO request = request("instance");
        SearchInstanceDTO created = instance(instanceId, "instance");
        when(service.create(request)).thenReturn(created);

        ResponseEntity<SearchInstanceDTO> response = resource.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(created);
    }

    @Test
    void listsInstancesAndReturnsNotFound() {
        PageImpl<SearchInstanceDTO> page = new PageImpl<>(List.of(instance(instanceId, "instance")));
        when(service.findAll(null, null, null, null, Pageable.ofSize(10))).thenReturn(page);
        assertThat(resource.list(null, null, null, null, Pageable.ofSize(10)).getStatusCode()).isEqualTo(HttpStatus.OK);

        when(service.findOne(instanceId)).thenReturn(Optional.empty());
        assertThat(resource.get(instanceId).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void returnsSearchResult() {
        SearchResultDTO result = new SearchResultDTO();
        when(service.findResult(instanceId)).thenReturn(Optional.of(result));

        ResponseEntity<SearchResultDTO> response = resource.result(instanceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(result);
    }

    @Test
    void deletesAndExecutesSearchInstance() {
        resource.delete(instanceId);
        resource.execute(instanceId);
        resource.rerun(instanceId);

        verify(service).delete(instanceId);
        verify(service).execute(instanceId);
        verify(service).rerun(instanceId);
        assertThat(resource.delete(instanceId).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void rejectsEmptyCsv() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.csv", "text/csv", new byte[0]);

        ResponseEntity<?> response = resource.uploadCsv(instanceId, file);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("No file uploaded");
    }

    @Test
    void uploadsValidCsvAndReturnsAccepted() throws IOException {
        MockMultipartFile file = file("position.csv");
        when(csvValidator.validate(any(java.io.InputStream.class))).thenReturn(validResult());

        ResponseEntity<?> response = resource.uploadCsv(instanceId, file);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        verify(service).uploadCsv(instanceId, file);
    }

    @Test
    void createsInstanceFromValidCsv() throws IOException {
        MockMultipartFile file = file("position.csv");
        SearchInstanceDTO created = instance(instanceId, "instance");
        when(csvValidator.validate(any(java.io.InputStream.class))).thenReturn(validResult());
        when(service.create(any(SearchInstanceWriteDTO.class))).thenReturn(created);

        ResponseEntity<WrapperInstanceCsv> response = resource.saveInstanceCsv("instance", "POSITION", file);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody().getSearchInstanceDTO()).isSameAs(created);
        verify(service).uploadCsv(instanceId, file);
    }

    @Test
    void validatesCsvWithoutCreatingInstance() throws IOException {
        MockMultipartFile file = file("position.csv");
        when(csvValidator.validate(any(java.io.InputStream.class))).thenReturn(validResult());

        ResponseEntity<?> response = resource.validateUploadedCsv(file);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void performsLifecycleActionsAndDuplicatesInstance() {
        SearchInstanceDTO duplicate = instance(UUID.fromString("33333333-3333-3333-3333-333333333333"), "duplicate");
        when(service.performAction(instanceId, SearchInstanceAction.ARCHIVE)).thenReturn(Optional.of(instance(instanceId, "instance")));
        when(service.performAction(instanceId, SearchInstanceAction.DUPLICATE)).thenReturn(Optional.of(duplicate));

        assertThat(resource.lifecycleAction(instanceId, SearchInstanceAction.ARCHIVE).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        ResponseEntity<?> duplicateResponse = resource.lifecycleAction(instanceId, SearchInstanceAction.DUPLICATE);
        assertThat(duplicateResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(duplicateResponse.getHeaders().getLocation()).hasToString("/api/bulk/search-instances/" + duplicate.getId());
    }

    @Test
    void downloadsResultAndPerimeterCsv() {
        WrapperFileResultDTO result = WrapperFileResultDTO.builder().fileName("result.zip").content("content".getBytes(StandardCharsets.UTF_8)).build();
        when(service.getLastResult(instanceId)).thenReturn(Optional.of(result));
        when(service.downloadPerimeterCsv(instanceId)).thenReturn(Optional.of("csv".getBytes(StandardCharsets.UTF_8)));

        ResponseEntity<byte[]> resultResponse = resource.download(instanceId);
        ResponseEntity<byte[]> perimeterResponse = resource.downloadPerimeterCsv(instanceId);

        assertThat(resultResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resultResponse.getHeaders().getContentDisposition().toString()).contains("result.zip");
        assertThat(resultResponse.getBody()).isEqualTo(result.getContent());
        assertThat(perimeterResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(perimeterResponse.getBody()).isEqualTo("csv".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void returnsNotFoundForMissingDownloads() {
        when(service.getLastResult(instanceId)).thenReturn(Optional.empty());
        when(service.downloadPerimeterCsv(instanceId)).thenReturn(Optional.empty());

        assertThat(resource.download(instanceId).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(resource.downloadPerimeterCsv(instanceId).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private SearchInstanceWriteDTO request(String name) {
        return SearchInstanceWriteDTO.builder()
            .name(name)
            .inputType(PerimeterSearchType.CSV)
            .selectedReports("POSITION")
            .build();
    }

    private SearchInstanceDTO instance(UUID id, String name) {
        return SearchInstanceDTO.builder().id(id).name(name).build();
    }

    private MockMultipartFile file(String filename) {
        return new MockMultipartFile("file", filename, "text/csv", "POSITION\n".getBytes(StandardCharsets.UTF_8));
    }

    private CsvValidationResult validResult() {
        return new CsvValidationResult(true, CsvTemplate.NAV_PA, 1, 1, 0, List.of());
    }
}
