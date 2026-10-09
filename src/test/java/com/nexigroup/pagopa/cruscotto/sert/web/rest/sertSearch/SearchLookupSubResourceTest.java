package com.nexigroup.pagopa.cruscotto.sert.web.rest.sertSearch.sertResourceSubKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.service.SearchLookupService;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.SearchLookupDTO;
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

class SearchLookupSubResourceTest {

    private final SearchLookupService service = mock(SearchLookupService.class);
    private final SearchLookupSubResource resource = new SearchLookupSubResource(service);

    @BeforeEach
    void setUpRequestContext() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
    }

    @Test
    void returnsPagedPaymentMethods() {
        SearchLookupDTO dto = new SearchLookupDTO();
        PageImpl<SearchLookupDTO> page = new PageImpl<>(List.of(dto));
        when(service.findPaymentMethods("card", Pageable.ofSize(10))).thenReturn(page);

        ResponseEntity<List<SearchLookupDTO>> response = resource.paymentMethods("card", Pageable.ofSize(10));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(dto);
    }

    @Test
    void returnsNotFoundForMissingPaymentMethod() {
        when(service.findPaymentMethodById(42L)).thenReturn(java.util.Optional.empty());

        ResponseEntity<SearchLookupDTO> response = resource.paymentMethodById(42L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }
}
