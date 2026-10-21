package com.nexigroup.pagopa.cruscotto.sert.web.rest.sertSearch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.service.SertService;
import com.nexigroup.pagopa.cruscotto.sert.service.SertFreezeDataService;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.ExtraInfoResponseDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.PositionPaymentDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.PositionPaymentExtraDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.TokenInfoDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.TransferPaymentDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.WorkflowObjectDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.WorkflowResponseDTO;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

class SertSearchCommonResourceTest {

    private final SertService service = mock(SertService.class);
    private final SertFreezeDataService freezeDataService = mock(SertFreezeDataService.class);
    private final SertSearchCommonResource resource = new SertSearchCommonResource(service, freezeDataService);

    @BeforeEach
    void setUpRequestContext() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
    }

    @Test
    void searchesByNavAndReturnsPageContent() {
        PageImpl<PositionPaymentExtraDTO> page = new PageImpl<>(List.of(positionExtra("nav-1")));
        when(service.searchByNav(eq("nav-1"), eq("pa-1"), any(Pageable.class))).thenReturn(page);

        ResponseEntity<List<PositionPaymentExtraDTO>> response = resource.search(
            "pa-1", "nav-1", null, null, null, null, Pageable.ofSize(10)
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(positionExtra("nav-1"));
        verify(service).searchByNav(eq("nav-1"), eq("pa-1"), any(Pageable.class));
    }

    @Test
    void rejectsSearchWithMoreThanOneSearchGroup() {
        assertThatThrownBy(() -> resource.search(
            "pa-1", "nav-1", "iuv-1", "token-1", null, null, Pageable.ofSize(10)
        )).isInstanceOf(ResponseStatusException.class)
            .satisfies(exception -> assertThat(((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void returnsNotFoundWhenSearchHasNoResults() {
        PageImpl<PositionPaymentExtraDTO> page = new PageImpl<>(List.of());
        when(service.searchByNav(eq("nav-1"), eq("pa-1"), any(Pageable.class))).thenReturn(page);

        assertThatThrownBy(() -> resource.search(
            "pa-1", "nav-1", null, null, null, null, Pageable.ofSize(10)
        )).isInstanceOf(ResponseStatusException.class)
            .satisfies(exception -> assertThat(((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void skipsOnlineSearchWhenFromOnlineIsFalse() {
        assertThatThrownBy(() -> resource.search(
            "pa-1", "nav-1", null, null, null, null, false, false, null, null, Pageable.ofSize(10)
        )).isInstanceOf(ResponseStatusException.class)
            .satisfies(exception -> assertThat(((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND));

        verifyNoInteractions(service);
    }

    @Test
    void returnsHistoricalResultsWithoutOnlineSearchWhenFromOnlineIsFalse() {
        var historical = positionExtra("nav-historical");
        historical.setFromHistory(true);
        when(freezeDataService.searchFromHistory(
            eq(null), eq("nav-historical"), eq(null), eq(null), eq(null), eq(null),
            eq(LocalDate.parse("2025-01-01")), eq(LocalDate.parse("2025-12-31")), any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(historical), PageRequest.of(0, 10), 1));

        ResponseEntity<List<PositionPaymentExtraDTO>> response = resource.search(
            null, "nav-historical", null, null, null, null, true, false,
            LocalDate.parse("2025-01-01"), LocalDate.parse("2025-12-31"), Pageable.ofSize(10)
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(historical);
        assertThat(historical.getFromHistory()).isTrue();
        verifyNoInteractions(service);
    }

    @Test
    void mergesCurrentAndHistoricalResultsAndMarksTheirSource() {
        var pageable = PageRequest.of(0, 2, Sort.by(Sort.Order.desc("paEmittente")));
        var current = positionExtra("nav-current");
        current.setPaEmittente("PA-2");
        var historical = positionExtra("nav-historical");
        historical.setPaEmittente("PA-1");
        when(service.searchByNav(eq("nav-current"), eq(null), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(current), PageRequest.of(0, 2), 1));
        when(freezeDataService.searchFromHistory(
            eq(null), eq("nav-current"), eq(null), eq(null), eq(null), eq(null),
            eq(LocalDate.parse("2025-01-01")), eq(LocalDate.parse("2025-12-31")), any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(historical), PageRequest.of(0, 2), 1));

        ResponseEntity<List<PositionPaymentExtraDTO>> response = resource.search(
            null, "nav-current", null, null, null, null, true,
            LocalDate.parse("2025-01-01"), LocalDate.parse("2025-12-31"), pageable
        );

        assertThat(response.getBody()).containsExactly(current, historical);
        assertThat(response.getBody()).extracting(PositionPaymentExtraDTO::getFromHistory).containsExactly(false, true);
    }

    @Test
    void requiresDateRangeWhenHistoricalSearchIsEnabled() {
        assertThatThrownBy(() -> resource.search(
            null, "nav-1", null, null, null, null, true, null, null, Pageable.ofSize(10)
        )).isInstanceOf(ResponseStatusException.class)
            .satisfies(exception -> assertThat(((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void returnsPositionAndTokenDetails() {
        PositionPaymentDTO position = PositionPaymentDTO.builder().build();
        TokenInfoDTO tokenInfo = TokenInfoDTO.builder().build();
        when(service.getPosition(eq("nav-1"), eq("pa-1"), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(position)));
        when(service.getTokenInfo(eq("token-1"))).thenReturn(tokenInfo);

        ResponseEntity<PositionPaymentDTO> positionResponse = resource.getPosition("nav-1", "pa-1", Pageable.ofSize(10));
        ResponseEntity<TokenInfoDTO> tokenResponse = resource.getTokenInfo("token-1");

        assertThat(positionResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(positionResponse.getBody()).isSameAs(position);
        assertThat(tokenResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(tokenResponse.getBody()).isSameAs(tokenInfo);
    }

    @Test
    void returnsTransfersWorkflowsAndExtraInfo() {
        TransferPaymentDTO transfer = TransferPaymentDTO.builder().build();
        WorkflowObjectDTO workflow = WorkflowObjectDTO.builder().build();
        ExtraInfoResponseDTO extraInfo = ExtraInfoResponseDTO.builder().build();
        when(service.getTransfers(eq("nav-1"), eq("pa-1"), eq("token-1"), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(transfer)));
        when(service.getWorkflows(eq("nav-1"), eq("pa-1"), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(WorkflowResponseDTO.builder().eventsPosition(List.of(workflow)).build())));
        when(service.getExtraInfo(eq("token-1"), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(extraInfo)));

        ResponseEntity<TransferPaymentDTO> transferResponse = resource.getTransfers(
            "nav-1", "pa-1", "token-1", Pageable.ofSize(10)
        );
        ResponseEntity<List<WorkflowObjectDTO>> workflowResponse = resource.getWorkflows(
            "nav-1", "pa-1", Pageable.ofSize(10)
        );
        ResponseEntity<ExtraInfoResponseDTO> extraResponse = resource.getExtraInfo("token-1", Pageable.ofSize(10));

        assertThat(transferResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(transferResponse.getBody()).isSameAs(transfer);
        assertThat(workflowResponse.getBody()).containsExactly(workflow);
        assertThat(extraResponse.getBody()).isSameAs(extraInfo);
    }

    @Test
    void readsAllDetailEndpointsFromFreezeWhenHistoryIsRequested() {
        PositionPaymentDTO position = PositionPaymentDTO.builder().build();
        TokenInfoDTO tokenInfo = TokenInfoDTO.builder().build();
        TransferPaymentDTO transfer = TransferPaymentDTO.builder().build();
        WorkflowObjectDTO workflow = WorkflowObjectDTO.builder().build();
        ExtraInfoResponseDTO extraInfo = ExtraInfoResponseDTO.builder().build();
        when(freezeDataService.getPositionFromHistory(eq("nav-1"), eq("pa-1"), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(position)));
        when(freezeDataService.getTokenInfoFromHistory("token-1")).thenReturn(tokenInfo);
        when(freezeDataService.getTransfersFromHistory(eq("nav-1"), eq("pa-1"), eq("token-1"), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(transfer)));
        when(freezeDataService.getWorkflowsFromHistory(eq("nav-1"), eq("pa-1"), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(WorkflowResponseDTO.builder().eventsPosition(List.of(workflow)).build())));
        when(freezeDataService.getExtraInfoFromHistory(eq("token-1"), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(extraInfo)));

        assertThat(resource.getPosition("nav-1", "pa-1", true, Pageable.ofSize(10)).getBody()).isSameAs(position);
        assertThat(resource.getTokenInfo("token-1", true).getBody()).isSameAs(tokenInfo);
        assertThat(resource.getTransfers("nav-1", "pa-1", "token-1", true, Pageable.ofSize(10)).getBody())
            .isSameAs(transfer);
        assertThat(resource.getWorkflows("nav-1", "pa-1", true, Pageable.ofSize(10)).getBody())
            .containsExactly(workflow);
        assertThat(resource.getExtraInfo("token-1", true, Pageable.ofSize(10)).getBody()).isSameAs(extraInfo);

        verifyNoInteractions(service);
    }

    private PositionPaymentExtraDTO positionExtra(String nav) {
        PositionPaymentExtraDTO dto = new PositionPaymentExtraDTO();
        dto.setMatch(List.of(nav));
        return dto;
    }
}
