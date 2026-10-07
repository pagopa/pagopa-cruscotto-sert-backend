package com.nexigroup.pagopa.cruscotto.sert.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthFunction;
import com.nexigroup.pagopa.cruscotto.sert.domain.AuthGroup;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthFunctionRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthGroupRepository;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthFunctionDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.exception.GenericServiceException;
import com.nexigroup.pagopa.cruscotto.sert.service.mapper.AuthGroupMapper;
import com.nexigroup.pagopa.cruscotto.sert.service.qdsl.QueryBuilder;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AuthGroupServiceImplTest {

    private final AuthFunctionRepository functionRepository = mock(AuthFunctionRepository.class);
    private final AuthGroupRepository groupRepository = mock(AuthGroupRepository.class);
    private final AuthGroupServiceImpl service = new AuthGroupServiceImpl(
        functionRepository,
        mock(QueryBuilder.class),
        groupRepository,
        mock(AuthGroupMapper.class)
    );

    @Test
    void associatesExistingFunctionsWithGroup() {
        AuthGroup group = new AuthGroup();
        group.setId(4L);
        AuthFunction function = new AuthFunction();
        function.setId(9L);
        AuthFunctionDTO functionDto = new AuthFunctionDTO();
        functionDto.setId(9L);
        when(groupRepository.findById(4L)).thenReturn(Optional.of(group));
        when(functionRepository.findById(9L)).thenReturn(Optional.of(function));

        service.associaFunzioni(4L, new AuthFunctionDTO[] { functionDto });

        assertThat(group.getAuthFunctions()).contains(function);
        verify(groupRepository).save(group);
    }

    @Test
    void throwsDomainExceptionWhenGroupOrFunctionDoesNotExist() {
        when(groupRepository.findById(4L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.associaFunzioni(4L, new AuthFunctionDTO[0]))
            .isInstanceOf(GenericServiceException.class)
            .hasMessageContaining("Group not exist with id 4");

        AuthGroup group = new AuthGroup();
        when(groupRepository.findById(4L)).thenReturn(Optional.of(group));
        when(functionRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.rimuoviAssociazioneFunzione(4L, 9L))
            .isInstanceOf(GenericServiceException.class)
            .hasMessageContaining("Funzione not exist with id 9");
    }

    @Test
    void removesFunctionAssociationAndPersistsGroup() {
        AuthGroup group = new AuthGroup();
        AuthFunction function = new AuthFunction();
        group.getAuthFunctions().add(function);
        when(groupRepository.findById(4L)).thenReturn(Optional.of(group));
        when(functionRepository.findById(9L)).thenReturn(Optional.of(function));

        service.rimuoviAssociazioneFunzione(4L, 9L);

        assertThat(group.getAuthFunctions()).doesNotContain(function);
        verify(groupRepository).save(group);
    }
}