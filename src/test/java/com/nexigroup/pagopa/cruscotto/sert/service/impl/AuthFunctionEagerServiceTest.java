package com.nexigroup.pagopa.cruscotto.sert.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthFunction;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthFunctionRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthPermissionRepository;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthFunctionDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.mapper.AuthFunctionMapper;
import com.nexigroup.pagopa.cruscotto.sert.service.qdsl.QueryBuilder;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class AuthFunctionEagerServiceTest {

    @Test
    void mapsEagerFunctionPageAndOptionalRelationshipLookup() {
        AuthFunctionRepository repository = mock(AuthFunctionRepository.class);
        AuthFunctionMapper mapper = mock(AuthFunctionMapper.class);
        AuthFunctionServiceImpl service = new AuthFunctionServiceImpl(
            mock(AuthPermissionRepository.class), mock(QueryBuilder.class), repository, mapper
        );
        AuthFunction entity = new AuthFunction();
        entity.setId(5L);
        AuthFunctionDTO dto = new AuthFunctionDTO();
        dto.setId(5L);
        var pageable = PageRequest.of(0, 5);
        when(repository.findAllWithEagerRelationships(pageable)).thenReturn(new PageImpl<>(java.util.List.of(entity), pageable, 1));
        when(repository.findOneWithEagerRelationships(5L)).thenReturn(Optional.of(entity));
        when(repository.findOneWithEagerRelationships(6L)).thenReturn(Optional.empty());
        when(mapper.toDto(entity)).thenReturn(dto);

        assertThat(service.findAllWithEagerRelationships(pageable).getContent()).containsExactly(dto);
        assertThat(service.findOneWithEagerRelationships(5L)).containsSame(dto);
        assertThat(service.findOneWithEagerRelationships(6L)).isEmpty();
    }
}