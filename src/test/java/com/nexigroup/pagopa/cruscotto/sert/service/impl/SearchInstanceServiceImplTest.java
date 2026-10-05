package com.nexigroup.pagopa.cruscotto.sert.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexigroup.pagopa.cruscotto.sert.domain.SearchInstance;
import com.nexigroup.pagopa.cruscotto.sert.domain.SearchPerimeterFile;
import com.nexigroup.pagopa.cruscotto.sert.domain.SearchResult;
import com.nexigroup.pagopa.cruscotto.sert.domain.enumeration.PerimeterSearchType;
import com.nexigroup.pagopa.cruscotto.sert.domain.enumeration.SearchInstanceStatus;
import com.nexigroup.pagopa.cruscotto.sert.repository.SearchExecutionRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.SearchExecutionStepRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.SearchFilterRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.SearchInstanceRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.SearchPerimeterFileRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.SearchResultRepository;
import com.nexigroup.pagopa.cruscotto.sert.service.SearchInstanceAction;
import com.nexigroup.pagopa.cruscotto.sert.service.SearchLookupService;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.SearchInstanceWriteDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.CsvFromFilterGenerator;
import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.csv.CsvTemplate;
import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.validator.MassiveSearchCsvValidator;
import com.nexigroup.pagopa.cruscotto.sert.service.storage.BlobStorageService;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

class SearchInstanceServiceImplTest {

    private SearchInstanceRepository instanceRepository;
    private SearchExecutionRepository executionRepository;
    private SearchExecutionStepRepository executionStepRepository;
    private SearchResultRepository resultRepository;
    private SearchPerimeterFileRepository perimeterFileRepository;
    private SearchFilterRepository searchFilterRepository;
    private MassiveSearchCsvValidator csvValidator;
    private BlobStorageService blobStorageService;
    private SearchInstanceServiceImpl service;

    @BeforeEach
    void setUp() {
        instanceRepository = mock(SearchInstanceRepository.class);
        executionRepository = mock(SearchExecutionRepository.class);
        executionStepRepository = mock(SearchExecutionStepRepository.class);
        resultRepository = mock(SearchResultRepository.class);
        perimeterFileRepository = mock(SearchPerimeterFileRepository.class);
        searchFilterRepository = mock(SearchFilterRepository.class);
        csvValidator = mock(MassiveSearchCsvValidator.class);
        blobStorageService = mock(BlobStorageService.class);
        service = new SearchInstanceServiceImpl(
            instanceRepository,
            executionRepository,
            executionStepRepository,
            resultRepository,
            perimeterFileRepository,
            blobStorageService,
            mock(CsvFromFilterGenerator.class),
            searchFilterRepository,
            csvValidator,
            new ObjectMapper(),
            mock(SearchLookupService.class)
        );
    }

    @Test
    void createsDraftInstanceWithGeneratedIdAndTimestamps() {
        when(instanceRepository.existsByNameIgnoreCase("daily search")).thenReturn(false);
        when(searchFilterRepository.findById(any())).thenReturn(Optional.empty());

        var result = service.create(SearchInstanceWriteDTO.builder()
            .name(" daily search ")
            .inputType(PerimeterSearchType.CSV)
            .selectedReports("POSITION,TOKEN")
            .build());

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo(" daily search ");
        assertThat(result.getInputType()).isEqualTo(PerimeterSearchType.CSV);
        assertThat(result.getStatus()).isEqualTo(SearchInstanceStatus.DRAFT);
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
        verify(instanceRepository).save(any(SearchInstance.class));
    }

    @Test
    void uploadsCsvAndPersistsTemplateContentAndRowCount() throws Exception {
        UUID instanceId = UUID.randomUUID();
        SearchInstance instance = SearchInstance.builder().id(instanceId).name("batch").inputType("CSV").status("DRAFT").build();
        String content = "NAV;EC\n123456789012345678;12345678901\n\n";
        MockMultipartFile upload = new MockMultipartFile("file", "perimeter.csv", "text/csv", content.getBytes(StandardCharsets.UTF_8));
        when(instanceRepository.findById(instanceId)).thenReturn(Optional.of(instance));
        when(csvValidator.extractCsvTemplate(any(ByteArrayInputStream.class))).thenReturn(CsvTemplate.NAV_PA);
        when(perimeterFileRepository.findByInstance(instance)).thenReturn(null);

        service.uploadCsv(instanceId, upload);

        ArgumentCaptor<SearchPerimeterFile> fileCaptor = ArgumentCaptor.forClass(SearchPerimeterFile.class);
        verify(perimeterFileRepository).save(fileCaptor.capture());
        SearchPerimeterFile savedFile = fileCaptor.getValue();
        assertThat(savedFile.getTemplate()).isEqualTo("NAV_PA");
        assertThat(savedFile.getFileName()).isEqualTo("perimeter.csv");
        assertThat(savedFile.getContent()).isEqualTo(content);
        assertThat(savedFile.getRowsCount()).isEqualTo(2L);
        assertThat(savedFile.getInstance()).isSameAs(instance);
        verify(instanceRepository).save(instance);
    }

    @Test
    void archiveAndRestoreUpdateLifecycleStatus() {
        UUID id = UUID.randomUUID();
        SearchInstance instance = SearchInstance.builder().id(id).name("batch").status("DRAFT").build();
        when(instanceRepository.findById(id)).thenReturn(Optional.of(instance));

        assertThat(service.performAction(id, SearchInstanceAction.ARCHIVE)).isEmpty();
        assertThat(instance.getStatus()).isEqualTo("ARCHIVED");
        assertThat(instance.getArchivedAt()).isNotNull();
        verify(instanceRepository).save(instance);

        assertThat(service.performAction(id, SearchInstanceAction.RESTORE)).isEmpty();
        assertThat(instance.getStatus()).isEqualTo("DRAFT");
        assertThat(instance.getArchivedAt()).isNull();
        verify(instanceRepository, org.mockito.Mockito.times(2)).save(instance);
        verify(csvValidator, never()).validate(any());
    }

    @Test
    void updatesExistingInstanceAndRejectsDuplicateName() {
        UUID id = UUID.randomUUID();
        SearchInstance instance = SearchInstance.builder()
            .id(id)
            .name("old")
            .inputType("CSV")
            .status("DRAFT")
            .selectedReports("POSITION")
            .build();
        when(instanceRepository.findById(id)).thenReturn(Optional.of(instance));
        when(instanceRepository.existsByNameIgnoreCaseAndIdNot("new", id)).thenReturn(false);
        when(searchFilterRepository.findById(id)).thenReturn(Optional.empty());

        var updated = service.update(id, SearchInstanceWriteDTO.builder()
            .name("new")
            .inputType(PerimeterSearchType.FILTER)
            .selectedReports("TOKEN")
            .status(SearchInstanceStatus.READY)
            .build());

        assertThat(updated.getName()).isEqualTo("new");
        assertThat(updated.getInputType()).isEqualTo(PerimeterSearchType.FILTER);
        assertThat(updated.getSelectedReports()).isEqualTo("TOKEN");
        assertThat(updated.getStatus()).isEqualTo(SearchInstanceStatus.READY);
        verify(instanceRepository).save(instance);

        when(instanceRepository.existsByNameIgnoreCaseAndIdNot("duplicate", id)).thenReturn(true);
        assertThatThrownBy(() -> service.update(id, SearchInstanceWriteDTO.builder()
            .name("duplicate")
            .inputType(PerimeterSearchType.CSV)
            .build()))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("409 CONFLICT");
    }

    @Test
    void duplicatesInstanceAndDeletesExistingInstance() {
        UUID id = UUID.randomUUID();
        SearchInstance original = SearchInstance.builder()
            .id(id)
            .name("batch")
            .inputType("CSV")
            .selectedReports("POSITION,TOKEN")
            .status("EXECUTED")
            .build();
        when(instanceRepository.findById(id)).thenReturn(Optional.of(original));
        when(searchFilterRepository.findById(id)).thenReturn(Optional.empty());
        when(instanceRepository.existsById(id)).thenReturn(true);

        var copy = service.duplicate(id);

        assertThat(copy.getId()).isNotEqualTo(id);
        assertThat(copy.getName()).isEqualTo("batch (copy)");
        assertThat(copy.getStatus()).isEqualTo(SearchInstanceStatus.DRAFT);
        assertThat(copy.getInputType()).isEqualTo(PerimeterSearchType.CSV);
        verify(instanceRepository).save(any(SearchInstance.class));

        service.delete(id);
        verify(instanceRepository).deleteById(id);
    }

    @Test
    void getsLastResultWithFallbackFilenameAndDownloadsPerimeterCsvContent() {
        UUID id = UUID.randomUUID();
        SearchResult result = SearchResult.builder().id(id).zipFileName(" ").zipFilePath("results/archive.zip").build();
        when(resultRepository.findById(id)).thenReturn(Optional.of(result));
        byte[] archive = new byte[] { 1, 2, 3 };
        when(blobStorageService.download("results/archive.zip")).thenReturn(Optional.of(archive));
        SearchInstance instance = SearchInstance.builder().id(id).build();
        SearchPerimeterFile file = SearchPerimeterFile.builder().instance(instance).content("NAV;EC\n").build();
        when(instanceRepository.findById(id)).thenReturn(Optional.of(instance));
        when(perimeterFileRepository.findTopByInstanceOrderByCreatedAtDesc(instance)).thenReturn(Optional.of(file));

        var wrapper = service.getLastResult(id);
        var perimeter = service.downloadPerimeterCsv(id);

        assertThat(wrapper).get().satisfies(download -> {
            assertThat(download.getFileName()).isEqualTo("result.zip");
            assertThat(download.getContent()).containsExactly(1, 2, 3);
        });
        assertThat(perimeter).get().satisfies(bytes -> assertThat(bytes).containsExactly("NAV;EC\n".getBytes(StandardCharsets.UTF_8)));

        when(blobStorageService.download("results/archive.zip")).thenReturn(Optional.empty());
        when(perimeterFileRepository.findTopByInstanceOrderByCreatedAtDesc(instance)).thenReturn(Optional.empty());
        assertThat(service.getLastResult(id)).isEmpty();
        assertThat(service.downloadPerimeterCsv(id)).isEmpty();
    }
}