package com.nexigroup.pagopa.cruscotto.sert.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthPermission;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthPermissionRepository;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthPermissionDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.mapper.AuthPermissionMapper;
import com.nexigroup.pagopa.cruscotto.sert.service.qdsl.QueryBuilder;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AuthPermissionServiceImplTest {

    private final AuthPermissionRepository permissionRepository = mock(AuthPermissionRepository.class);
    private final AuthPermissionMapper permissionMapper = mock(AuthPermissionMapper.class);
    private final AuthPermissionServiceImpl service = new AuthPermissionServiceImpl(
        mock(QueryBuilder.class),
        permissionRepository,
        permissionMapper
    );

    @Test
    void savesPermissionUsingMapperAndRepository() {
        AuthPermissionDTO dto = new AuthPermissionDTO();
        dto.setNome("SEARCH");
        dto.setModulo("SERT");
        AuthPermission entity = new AuthPermission().nome("SEARCH").modulo("SERT");
        AuthPermissionDTO mapped = new AuthPermissionDTO();
        mapped.setNome("SEARCH");
        mapped.setModulo("SERT");
        when(permissionMapper.toEntity(dto)).thenReturn(entity);
        when(permissionRepository.save(entity)).thenReturn(entity);
        when(permissionMapper.toDto(entity)).thenReturn(mapped);

        assertThat(service.save(dto)).isSameAs(mapped);
        verify(permissionRepository).save(entity);
    }

    @Test
    void findsPermissionAndReturnsEmptyWhenMissing() {
        AuthPermission entity = new AuthPermission().nome("SEARCH").modulo("SERT");
        AuthPermissionDTO dto = new AuthPermissionDTO();
        dto.setNome("SEARCH");
        when(permissionRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(permissionRepository.findById(2L)).thenReturn(Optional.empty());
        when(permissionMapper.toDto(entity)).thenReturn(dto);

        assertThat(service.findOne(1L)).containsSame(dto);
        assertThat(service.findOne(2L)).isEmpty();
    }

    @Test
    void deletesPermissionById() {
        service.delete(1L);

        verify(permissionRepository).deleteById(1L);
    }
}