package com.nexigroup.pagopa.cruscotto.sert.web.rest.sertSearch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.service.dto.ExtraInfoResponseDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.PositionPaymentDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.PositionPaymentExtraDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.TokenInfoDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.TransferPaymentDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.WorkflowObjectDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.WorkflowResponseDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.impl.SertHistoryDataServiceImpl;
import com.nexigroup.pagopa.cruscotto.sert.service.impl.SertOnlineServiceImpl;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

class SertSearchCommonResourceTest {

    private static final LocalDate VALID_DATE_TO = LocalDate.now().minusMonths(6);
    private static final LocalDate VALID_DATE_FROM = VALID_DATE_TO.minusMonths(5);

    private final SertOnlineServiceImpl service = mock(SertOnlineServiceImpl.class);
    private final SertHistoryDataServiceImpl freezeDataService = mock(SertHistoryDataServiceImpl.class);
    private final SertSearchCommonResource resource = new SertSearchCommonResource(service, freezeDataService, 3);

    @BeforeEach
    void setUpRequestContext() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
    }

    @Test
    void searchesByNavAndReturnsPageContent() {
        PageImpl<PositionPaymentExtraDTO> page = new PageImpl<>(List.of(positionExtra("nav-1")));
        when(service.searchByNav(eq("nav-1"), eq("pa-1"), isNull(), isNull(), any(Pageable.class))).thenReturn(page);

        ResponseEntity<List<PositionPaymentExtraDTO>> response = resource.search(
            "pa-1", "nav-1", null, null, null, null, Pageable.ofSize(10)
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(positionExtra("nav-1"));
        verify(service).searchByNav(eq("nav-1"), eq("pa-1"), isNull(), isNull(), any(Pageable.class));
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
        when(service.searchByNav(eq("nav-1"), eq("pa-1"), isNull(), isNull(), any(Pageable.class))).thenReturn(page);

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
    void requiresDateRangeWhenHistoricalSearchIsEnabled() {
        assertThatThrownBy(() -> resource.search(
            null, "nav-1", null, null, null, null, true, null, null, Pageable.ofSize(10)
        )).isInstanceOf(ResponseStatusException.class)
            .satisfies(exception -> assertThat(((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void requiresDateFromToBeBeforeDateTo() {
        assertBadDateRange(VALID_DATE_FROM, VALID_DATE_FROM);
        assertBadDateRange(VALID_DATE_TO, VALID_DATE_FROM);
    }

    @Test
    void rejectsDateToLaterThanSixMonthsAndConfiguredDelta() {
        LocalDate tooRecentDateTo = LocalDate.now().minusMonths(6).plusDays(4);

        assertBadDateRange(tooRecentDateTo.minusMonths(1), tooRecentDateTo);
    }


    @Test
    void rejectsDateRangesLongerThanSixMonths() {
        LocalDate dateTo = LocalDate.now().minusMonths(6);
        assertBadDateRange(dateTo.minusMonths(6).minusDays(1), dateTo);
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
        when(freezeDataService.getPosition(eq("nav-1"), eq("pa-1"), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(position)));
        when(freezeDataService.getTokenInfo("token-1")).thenReturn(tokenInfo);
        when(freezeDataService.getTransfers(eq("nav-1"), eq("pa-1"), eq("token-1"), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(transfer)));
        when(freezeDataService.getWorkflows(eq("nav-1"), eq("pa-1"), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(WorkflowResponseDTO.builder().eventsPosition(List.of(workflow)).build())));
        when(freezeDataService.getExtraInfo(eq("token-1"), any(Pageable.class)))
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

    private void assertBadDateRange(LocalDate dateFrom, LocalDate dateTo) {
        assertThatThrownBy(() -> resource.search(
            null, "nav-1", null, null, null, null, true, false, dateFrom, dateTo, Pageable.ofSize(10)
        )).isInstanceOf(ResponseStatusException.class)
            .satisfies(exception -> assertThat(((ResponseStatusException) exception).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST));
    }
}
