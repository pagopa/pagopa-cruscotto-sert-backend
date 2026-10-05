package com.nexigroup.pagopa.cruscotto.sert.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthFunction;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthFunctionRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthPermissionRepository;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthFunctionDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.mapper.AuthFunctionMapper;
import com.nexigroup.pagopa.cruscotto.sert.service.qdsl.QueryBuilder;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AuthFunctionServiceImplTest {

    private final AuthFunctionRepository functionRepository = mock(AuthFunctionRepository.class);
    private final AuthFunctionMapper functionMapper = mock(AuthFunctionMapper.class);
    private final AuthFunctionServiceImpl service = new AuthFunctionServiceImpl(
        mock(AuthPermissionRepository.class),
        mock(QueryBuilder.class),
        functionRepository,
        functionMapper
    );

    @Test
    void savesFunctionMappedFromDto() {
        AuthFunctionDTO dto = functionDto(null, "SEARCH");
        AuthFunctionDTO mappedDto = functionDto(3L, "SEARCH");
        when(functionRepository.save(org.mockito.ArgumentMatchers.any(AuthFunction.class))).thenAnswer(invocation -> {
            AuthFunction saved = invocation.getArgument(0);
            saved.setId(3L);
            return saved;
        });
        when(functionMapper.toDto(org.mockito.ArgumentMatchers.any(AuthFunction.class))).thenReturn(mappedDto);

        assertThat(service.save(dto)).isSameAs(mappedDto);
        verify(functionRepository).save(org.mockito.ArgumentMatchers.argThat(function ->
            "SEARCH".equals(function.getNome()) && "SERT".equals(function.getModulo())
        ));
    }

    @Test
    void updatesExistingFunctionAndReturnsEmptyForMissingId() {
        AuthFunction existing = new AuthFunction();
        existing.setId(3L);
        when(functionRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(functionRepository.findById(99L)).thenReturn(Optional.empty());
        AuthFunctionDTO dto = functionDto(3L, "UPDATED");
        AuthFunctionDTO mappedDto = functionDto(3L, "UPDATED");
        when(functionMapper.toDto(existing)).thenReturn(mappedDto);

        assertThat(service.update(dto)).containsSame(mappedDto);
        assertThat(existing.getNome()).isEqualTo("UPDATED");
        assertThat(existing.getModulo()).isEqualTo("SERT");
        assertThat(service.update(functionDto(99L, "missing"))).isEmpty();
    }

    @Test
    void deletesFunctionById() {
        service.delete(3L);

        verify(functionRepository).deleteById(3L);
    }

    private AuthFunctionDTO functionDto(Long id, String name) {
        AuthFunctionDTO dto = new AuthFunctionDTO();
        dto.setId(id);
        dto.setNome(name);
        dto.setModulo("SERT");
        dto.setDescrizione("Search permissions");
        return dto;
    }
}