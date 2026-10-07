package com.nexigroup.pagopa.cruscotto.sert.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthFunction;
import com.nexigroup.pagopa.cruscotto.sert.domain.QAuthFunction;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthFunctionRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthPermissionRepository;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthFunctionDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.filter.AuthFunctionFilter;
import com.nexigroup.pagopa.cruscotto.sert.service.mapper.AuthFunctionMapper;
import com.nexigroup.pagopa.cruscotto.sert.service.qdsl.QueryBuilder;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@SuppressWarnings({ "rawtypes", "unchecked" })
class AuthFunctionServiceImplTest {

    private final AuthFunctionRepository functionRepository = mock(AuthFunctionRepository.class);
    private final AuthFunctionMapper functionMapper = mock(AuthFunctionMapper.class);
    private final QueryBuilder queryBuilder = mock(QueryBuilder.class);
    private final AuthFunctionServiceImpl service = new AuthFunctionServiceImpl(
        mock(AuthPermissionRepository.class),
        queryBuilder,
        functionRepository,
        functionMapper
    );

    @Test
    void savesFunctionMappedFromDto() {
        AuthFunctionDTO dto = functionDto(null, "SEARCH");
        AuthFunctionDTO mappedDto = functionDto(3L, "SEARCH");
        when(functionRepository.save(any(AuthFunction.class))).thenAnswer(invocation -> {
            AuthFunction saved = invocation.getArgument(0);
            saved.setId(3L);
            return saved;
        });
        when(functionMapper.toDto(any(AuthFunction.class))).thenReturn(mappedDto);

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
        AuthFunctionDTO mappedDto = functionDto(3L, "UPDATED");
        when(functionMapper.toDto(existing)).thenReturn(mappedDto);

        assertThat(service.update(functionDto(3L, "UPDATED"))).containsSame(mappedDto);
        assertThat(existing.getNome()).isEqualTo("UPDATED");
        assertThat(service.update(functionDto(99L, "missing"))).isEmpty();
    }

    @Test
    void findsFilteredPageAndSingleFunctionByQueryDsl() {
        AuthFunctionDTO dto = functionDto(7L, "SEARCH");
        JPAQuery primaryQuery = mock(JPAQuery.class);
        JPAQuery pageQuery = mock(JPAQuery.class);
        JPAQuery singleQuery = mock(JPAQuery.class);

        when(queryBuilder.createQuery()).thenReturn(primaryQuery);
        when(primaryQuery.from((EntityPath) QAuthFunction.authFunction)).thenReturn(primaryQuery);
        when(primaryQuery.where(any(Predicate.class))).thenReturn(primaryQuery);
        when(primaryQuery.fetchCount()).thenReturn(1L);
        when(primaryQuery.select((Expression) any())).thenReturn(pageQuery);
        when(pageQuery.offset(0L)).thenReturn(pageQuery);
        when(pageQuery.limit(10)).thenReturn(pageQuery);
        when(pageQuery.orderBy(any(OrderSpecifier.class))).thenReturn(pageQuery);
        when(pageQuery.fetch()).thenReturn(List.of(dto));

        Pageable pageable = PageRequest.of(0, 10);
        AuthFunctionFilter filter = new AuthFunctionFilter();
        filter.setNome("sea");
        filter.setDescrizione("desc");

        assertThat(service.findAll(filter, pageable)).hasSize(1);
        assertThat(service.findAll(filter, pageable).getContent()).singleElement().satisfies(item -> {
            assertThat(item.getId()).isEqualTo(7L);
            assertThat(item.getNome()).isEqualTo("SEARCH");
        });

        when(primaryQuery.select((Expression) any())).thenReturn(singleQuery);
        when(singleQuery.fetchOne()).thenReturn(dto);
        assertThat(service.findOne(7L)).containsSame(dto);
    }

    @Test
    void listsSelectedAndAssociableFunctionsByGroup() {
        AuthFunctionDTO selected = functionDto(5L, "POSITION");
        JPAQuery groupQuery = mock(JPAQuery.class, org.mockito.Mockito.RETURNS_SELF);
        JPAQuery selectedQuery = mock(JPAQuery.class);
        JPAQuery idQuery = mock(JPAQuery.class);
        JPAQuery availableQuery = mock(JPAQuery.class, org.mockito.Mockito.RETURNS_SELF);
        JPAQuery availableDtoQuery = mock(JPAQuery.class);

        when(queryBuilder.createQuery()).thenReturn(groupQuery, availableQuery);
        org.mockito.Mockito.doReturn(groupQuery).when(groupQuery).from((EntityPath) any());
        org.mockito.Mockito.doReturn(groupQuery).when(groupQuery).join((EntityPath) any(), any());
        when(groupQuery.where(any(Predicate.class))).thenReturn(groupQuery);
        when(groupQuery.fetchCount()).thenReturn(1L);
        org.mockito.Mockito.doReturn(selectedQuery).when(groupQuery).select((Expression) any());
        when(selectedQuery.offset(0L)).thenReturn(selectedQuery);
        when(selectedQuery.limit(10)).thenReturn(selectedQuery);
        when(selectedQuery.orderBy(any(OrderSpecifier.class))).thenReturn(selectedQuery);
        when(selectedQuery.fetch()).thenReturn(List.of(selected));
        org.mockito.Mockito.doReturn(idQuery).when(groupQuery).select((Expression) any());
        when(idQuery.orderBy(any(OrderSpecifier.class))).thenReturn(idQuery);
        when(idQuery.fetch()).thenReturn(List.of(5L));

        org.mockito.Mockito.doReturn(availableQuery).when(availableQuery).from((EntityPath) QAuthFunction.authFunction);
        when(availableQuery.where(any(Predicate.class))).thenReturn(availableQuery);
        when(availableQuery.fetchCount()).thenReturn(1L);
        org.mockito.Mockito.doReturn(availableDtoQuery).when(availableQuery).select((Expression) any());
        when(availableDtoQuery.offset(0L)).thenReturn(availableDtoQuery);
        when(availableDtoQuery.limit(10)).thenReturn(availableDtoQuery);
        when(availableDtoQuery.orderBy(any(OrderSpecifier.class))).thenReturn(availableDtoQuery);
        when(availableDtoQuery.fetch()).thenReturn(List.of(selected));

        Pageable pageable = PageRequest.of(0, 10);
        assertThat(service.listAllFunctionSelected(12L, pageable)).hasSize(1);
        assertThat(service.listAllFunctionAssociabili(12L, Optional.of("pos"), pageable)).hasSize(1);
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
