package com.nexigroup.pagopa.cruscotto.sert.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthFunction;
import com.nexigroup.pagopa.cruscotto.sert.domain.AuthPermission;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthFunctionRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthPermissionRepository;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthPermissionDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.exception.GenericServiceException;
import com.nexigroup.pagopa.cruscotto.sert.service.mapper.AuthFunctionMapper;
import com.nexigroup.pagopa.cruscotto.sert.service.qdsl.QueryBuilder;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AuthFunctionAssociationServiceTest {

    private final AuthFunctionRepository functionRepository = mock(AuthFunctionRepository.class);
    private final AuthPermissionRepository permissionRepository = mock(AuthPermissionRepository.class);
    private final AuthFunctionServiceImpl service = new AuthFunctionServiceImpl(
        permissionRepository,
        mock(QueryBuilder.class),
        functionRepository,
        mock(AuthFunctionMapper.class)
    );

    @Test
    void associatesFoundPermissionsAndIgnoresMissingOnes() {
        AuthFunction function = new AuthFunction();
        AuthPermission permission = new AuthPermission();
        AuthPermissionDTO found = new AuthPermissionDTO();
        found.setId(3L);
        AuthPermissionDTO missing = new AuthPermissionDTO();
        missing.setId(4L);
        when(functionRepository.findById(2L)).thenReturn(Optional.of(function));
        when(permissionRepository.findById(3L)).thenReturn(Optional.of(permission));
        when(permissionRepository.findById(4L)).thenReturn(Optional.empty());

        service.associaPermesso(2L, new AuthPermissionDTO[] { found, missing });

        assertThat(function.getAuthPermissions()).contains(permission);
        verify(functionRepository).save(function);
    }

    @Test
    void throwsWhenFunctionOrPermissionDoesNotExistAndRemovesExistingAssociation() {
        when(functionRepository.findById(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.associaPermesso(2L, new AuthPermissionDTO[0]))
            .isInstanceOf(GenericServiceException.class)
            .hasMessageContaining("Function not exist with id 2");

        AuthFunction function = new AuthFunction();
        AuthPermission permission = new AuthPermission();
        function.getAuthPermissions().add(permission);
        when(functionRepository.findById(2L)).thenReturn(Optional.of(function));
        when(permissionRepository.findById(3L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.rimuoviAssociazionePermesso(2L, 3L))
            .isInstanceOf(GenericServiceException.class)
            .hasMessageContaining("Permesso not exist with id 3");

        when(permissionRepository.findById(3L)).thenReturn(Optional.of(permission));
        service.rimuoviAssociazionePermesso(2L, 3L);
        assertThat(function.getAuthPermissions()).doesNotContain(permission);
        verify(functionRepository).save(function);
    }
}