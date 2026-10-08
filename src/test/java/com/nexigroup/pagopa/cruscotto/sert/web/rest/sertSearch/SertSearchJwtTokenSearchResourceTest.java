package com.nexigroup.pagopa.cruscotto.sert.web.rest.sertSearch.sertResourceJwtToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.service.SertService;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.PositionPaymentExtraDTO;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class SertSearchJwtTokenSearchResourceTest {

    private final SertService service = mock(SertService.class);
    private final SertSearchJwtTokenSearchResource resource = new SertSearchJwtTokenSearchResource(service);

    @Test
    void delegatesSearchToCommonResource() {
        PageImpl<PositionPaymentExtraDTO> page = new PageImpl<>(List.of(new PositionPaymentExtraDTO()));
        when(service.searchByNav(eq("nav-1"), eq("pa-1"), any(Pageable.class))).thenReturn(page);

        ResponseEntity<List<PositionPaymentExtraDTO>> response = resource.search(
            "pa-1", "nav-1", null, null, null, null, Pageable.ofSize(10)
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(page.getContent());
    }
}
