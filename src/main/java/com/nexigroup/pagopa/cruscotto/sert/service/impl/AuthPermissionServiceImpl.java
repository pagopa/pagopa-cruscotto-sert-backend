package com.nexigroup.pagopa.cruscotto.sert.service.impl;

import com.nexigroup.pagopa.cruscotto.sert.domain.*;
import com.nexigroup.pagopa.cruscotto.sert.domain.AuthFunctionAuthPermission;
import com.nexigroup.pagopa.cruscotto.sert.domain.AuthPermission;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthPermissionRepository;
import com.nexigroup.pagopa.cruscotto.sert.service.AuthPermissionService;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthPermissionDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.filter.AuthPermissionFilter;
import com.nexigroup.pagopa.cruscotto.sert.service.mapper.AuthPermissionMapper;
import com.nexigroup.pagopa.cruscotto.sert.service.qdsl.QdslUtility;
import com.nexigroup.pagopa.cruscotto.sert.service.qdsl.QueryBuilder;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link AuthPermission}.
 */
@Service
@Transactional
public class AuthPermissionServiceImpl implements AuthPermissionService {

    private static final String MODULO = "modulo";

    private final Logger log = LoggerFactory.getLogger(AuthPermissionServiceImpl.class);

    private final QueryBuilder queryBuilder;

    private final AuthPermissionRepository authPermissionRepository;

    private final AuthPermissionMapper authPermissionMapper;

    public AuthPermissionServiceImpl(
        QueryBuilder queryBuilder,
        AuthPermissionRepository authPermissionRepository,
        AuthPermissionMapper authPermissionMapper
    ) {
        this.queryBuilder = queryBuilder;
        this.authPermissionRepository = authPermissionRepository;
        this.authPermissionMapper = authPermissionMapper;
    }

    /**
     * Save a authPermission.
     *
     * @param authPermissionDTO the entity to save.
     * @return the persisted entity.
     */
    @Override
    public AuthPermissionDTO save(AuthPermissionDTO authPermissionDTO) {
        log.debug("Request to save AuthPermission : {}", authPermissionDTO);
        AuthPermission authPermission = authPermissionMapper.toEntity(authPermissionDTO);
        authPermission = authPermissionRepository.save(authPermission);
        return authPermissionMapper.toDto(authPermission);
    }

    /**
     * Get all the authPermissions.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AuthPermissionDTO> findAll(AuthPermissionFilter filter, Pageable pageable) {
        log.debug("Request to get all AuthPermissions");
        BooleanBuilder builder = new BooleanBuilder();

        if (StringUtils.isNotBlank(filter.getNome())) {
            builder.and(QAuthPermission.authPermission.nome.containsIgnoreCase(filter.getNome()));
        }

        if (StringUtils.isNotBlank(filter.getModulo())) {
            builder.and(QAuthPermission.authPermission.modulo.containsIgnoreCase(filter.getModulo()));
        }

        JPQLQuery<AuthPermission> jpql = queryBuilder.<AuthPermission>createQuery().from(QAuthPermission.authPermission).where(builder);

        long size = jpql.fetchCount();

        JPQLQuery<AuthPermissionDTO> jpqlSelected = jpql.select(
            Projections.fields(
                AuthPermissionDTO.class,
                QAuthPermission.authPermission.id.as("id"),
                QAuthPermission.authPermission.nome.as("nome"),
                QAuthPermission.authPermission.modulo.as(MODULO)
            )
        );

        jpqlSelected.offset(pageable.getOffset());
        jpqlSelected.limit(pageable.getPageSize());

        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        if (pageable.getSort() != null) sort = pageable.getSort();

        if (sort != null) {
            sort.forEach(order -> {
                jpqlSelected.orderBy(
                    new OrderSpecifier<>(
                        order.isAscending() ? Order.ASC : Order.DESC,
                        Expressions.stringPath(order.getProperty()),
                        QdslUtility.toQueryDslNullHandling(order.getNullHandling())
                    )
                );
            });
        }

        List<AuthPermissionDTO> list = jpqlSelected.fetch();

        return new PageImpl<>(list, pageable, size);
    }

    /**
     * Get one authPermission by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<AuthPermissionDTO> findOne(Long id) {
        log.debug("Request to get AuthPermission : {}", id);
        return authPermissionRepository.findById(id).map(authPermissionMapper::toDto);
    }

    /**
     * Delete the authPermission by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete AuthPermission : {}", id);
        authPermissionRepository.deleteById(id);
    }

    @Override
    public List<Long> listAllPermissionSelected(long idFunction) {
        QAuthFunctionAuthPermission authFunctionAuthPermission = QAuthFunctionAuthPermission.authFunctionAuthPermission;

        JPQLQuery<AuthFunctionAuthPermission> jpql = queryBuilder
            .<AuthFunctionAuthPermission>createQuery()
            .from(authFunctionAuthPermission)
            .join(authFunctionAuthPermission.permesso, QAuthPermission.authPermission)
            .where(authFunctionAuthPermission.funzione.id.eq(idFunction));

        JPQLQuery<Long> jpqlSelected = jpql.select(QAuthPermission.authPermission.id.as("id"));

        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        sort.forEach(order -> {
            jpqlSelected.orderBy(
                new OrderSpecifier<>(
                    order.isAscending() ? Order.ASC : Order.DESC,
                    Expressions.stringPath(order.getProperty()),
                    QdslUtility.toQueryDslNullHandling(order.getNullHandling())
                )
            );
        });

        return jpqlSelected.fetch();
    }

    @Override
    public Page<AuthPermissionDTO> listAllPermissionSelected(long idFunction, Pageable pageable) {
        QAuthFunctionAuthPermission authFunctionAuthPermission = QAuthFunctionAuthPermission.authFunctionAuthPermission;

        JPQLQuery<AuthFunctionAuthPermission> jpql = queryBuilder
            .<AuthFunctionAuthPermission>createQuery()
            .from(authFunctionAuthPermission)
            .join(authFunctionAuthPermission.permesso, QAuthPermission.authPermission)
            .where(authFunctionAuthPermission.funzione.id.eq(idFunction));

        long size = jpql.fetchCount();

        JPQLQuery<AuthPermissionDTO> jpqlSelected = jpql.select(
            Projections.fields(
                AuthPermissionDTO.class,
                QAuthPermission.authPermission.id.as("id"),
                QAuthPermission.authPermission.modulo.as(MODULO),
                QAuthPermission.authPermission.nome.as("nome")
            )
        );

        jpqlSelected.offset(pageable.getOffset());
        jpqlSelected.limit(pageable.getPageSize());

        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        if (pageable.getSort() != null) sort = pageable.getSort();

        if (sort != null) {
            sort.forEach(order -> {
                jpqlSelected.orderBy(
                    new OrderSpecifier<>(
                        order.isAscending() ? Order.ASC : Order.DESC,
                        Expressions.stringPath(order.getProperty()),
                        QdslUtility.toQueryDslNullHandling(order.getNullHandling())
                    )
                );
            });
        }

        List<AuthPermissionDTO> list = jpqlSelected.fetch();

        return new PageImpl<>(list, pageable, size);
    }

    @Override
    public Page<AuthPermissionDTO> listAllPermissionAssociabili(long idFunction, Optional<String> optionalNomePermesso, Pageable pageable) {
        String nomePermesso = optionalNomePermesso.orElse("");
        QAuthPermission authPermission = QAuthPermission.authPermission;

        JPQLQuery<Long> subQuery = JPAExpressions.select(authPermission.id)
            .from(authPermission)
            .innerJoin(authPermission.authFunctions, QAuthFunction.authFunction)
            .where(QAuthFunction.authFunction.id.eq(idFunction));

        BooleanBuilder whereCondition = new BooleanBuilder();
        whereCondition.and(authPermission.id.notIn(subQuery)).and(authPermission.nome.containsIgnoreCase(nomePermesso));

        JPQLQuery<AuthPermission> jpql = queryBuilder.<AuthPermission>createQuery().from(authPermission).where(whereCondition);

        long size = jpql.fetchCount();

        JPQLQuery<AuthPermissionDTO> jpqlSelected = jpql.select(
            Projections.fields(
                AuthPermissionDTO.class,
                QAuthPermission.authPermission.id.as("id"),
                QAuthPermission.authPermission.nome.as("nome"),
                QAuthPermission.authPermission.modulo.as(MODULO)
            )
        );

        jpqlSelected.offset(pageable.getOffset());
        jpqlSelected.limit(pageable.getPageSize());

        Sort sort = pageable.getSortOr(Sort.by(Sort.Direction.ASC, "nome"));

        sort.forEach(order -> {
            jpqlSelected.orderBy(
                new OrderSpecifier<>(
                    order.isAscending() ? Order.ASC : Order.DESC,
                    Expressions.stringPath(order.getProperty()),
                    QdslUtility.toQueryDslNullHandling(order.getNullHandling())
                )
            );
        });

        List<AuthPermissionDTO> list = jpqlSelected.fetch();

        return new PageImpl<>(list, pageable, size);
    }
}
