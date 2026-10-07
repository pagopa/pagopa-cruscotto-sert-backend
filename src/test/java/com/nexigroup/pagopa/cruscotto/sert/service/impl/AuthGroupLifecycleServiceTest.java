package com.nexigroup.pagopa.cruscotto.sert.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthGroup;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthFunctionRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthGroupRepository;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthGroupDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.mapper.AuthGroupMapper;
import com.nexigroup.pagopa.cruscotto.sert.service.qdsl.QueryBuilder;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class AuthGroupLifecycleServiceTest {

    private final AuthGroupRepository repository = mock(AuthGroupRepository.class);
    private final AuthGroupMapper mapper = mock(AuthGroupMapper.class);
    private final AuthGroupServiceImpl service = new AuthGroupServiceImpl(
        mock(AuthFunctionRepository.class), mock(QueryBuilder.class), repository, mapper
    );

    @Test
    void savesGroupWithNextVisibilityLevel() {
        when(repository.getMaxLivelloVisibilita()).thenReturn(4);
        when(repository.save(any(AuthGroup.class))).thenAnswer(invocation -> invocation.getArgument(0));
        AuthGroupDTO response = new AuthGroupDTO();
        response.setNome("OPERATORS");
        when(mapper.toDto(any(AuthGroup.class))).thenReturn(response);
        AuthGroupDTO request = new AuthGroupDTO();
        request.setNome("OPERATORS");
        request.setDescrizione("Operations");

        assertThat(service.save(request)).isSameAs(response);
        verify(repository).save(org.mockito.ArgumentMatchers.argThat(group ->
            group.getLivelloVisibilita() == 5 && "OPERATORS".equals(group.getNome())
        ));
    }

    @Test
    void updatesExistingGroupAndReturnsEmptyForMissingOne() {
        AuthGroup entity = new AuthGroup();
        entity.setId(10L);
        AuthGroupDTO response = new AuthGroupDTO();
        when(repository.findById(10L)).thenReturn(Optional.of(entity));
        when(repository.findById(11L)).thenReturn(Optional.empty());
        when(mapper.toDto(entity)).thenReturn(response);
        AuthGroupDTO update = new AuthGroupDTO();
        update.setId(10L);
        update.setNome("UPDATED");
        update.setDescrizione("Updated group");
        AuthGroupDTO missing = new AuthGroupDTO();
        missing.setId(11L);

        assertThat(service.update(update)).containsSame(response);
        assertThat(entity.getNome()).isEqualTo("UPDATED");
        assertThat(entity.getDescrizione()).isEqualTo("Updated group");
        assertThat(service.update(missing)).isEmpty();
    }

    @Test
    void mapsEagerRepositoryPageAndDeletesGroup() {
        AuthGroup entity = new AuthGroup();
        AuthGroupDTO dto = new AuthGroupDTO();
        var pageable = PageRequest.of(0, 10);
        when(repository.findAllWithEagerRelationships(pageable)).thenReturn(new PageImpl<>(java.util.List.of(entity), pageable, 1));
        when(mapper.toDto(entity)).thenReturn(dto);

        assertThat(service.findAllWithEagerRelationships(pageable).getContent()).containsExactly(dto);
        service.delete(10L);
        verify(repository).deleteById(10L);
    }
}