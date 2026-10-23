package com.nexigroup.pagopa.cruscotto.sert.web.rest.sertSearch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;

import com.nexigroup.pagopa.cruscotto.sert.config.ApplicationProperties;
import static org.mockito.Mockito.when;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.PositionPaymentExtraDTO;

import java.util.List;

import com.nexigroup.pagopa.cruscotto.sert.service.impl.SertHistoryDataServiceImpl;
import com.nexigroup.pagopa.cruscotto.sert.service.impl.SertOnlineServiceImpl;
import com.nexigroup.pagopa.cruscotto.sert.web.rest.sertSearch.sertResourceJwtToken.SertSearchJwtTokenSearchResource;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class SertSearchJwtTokenSearchResourceTest {

    private final SertOnlineServiceImpl service = mock(SertOnlineServiceImpl.class);
    private final SertHistoryDataServiceImpl freezeDataService = mock(SertHistoryDataServiceImpl.class);
    private final SertSearchJwtTokenSearchResource resource = new SertSearchJwtTokenSearchResource(
        service, freezeDataService, new ApplicationProperties()
    );

    @Test
    void delegatesSearchToCommonResource() {
        PageImpl<PositionPaymentExtraDTO> page = new PageImpl<>(List.of(new PositionPaymentExtraDTO()));
        when(service.searchByNav(eq("nav-1"), eq("pa-1"), isNull(), isNull(), any(Pageable.class))).thenReturn(page);

        ResponseEntity<List<PositionPaymentExtraDTO>> response = resource.search(
            "pa-1", "nav-1", null, null, null, null, false, true, null, null, Pageable.ofSize(10)
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(page.getContent());
    }
}
