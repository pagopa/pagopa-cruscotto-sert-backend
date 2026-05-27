package com.nexigroup.pagopa.cruscotto.sert.service.impl;


import com.nexigroup.pagopa.cruscotto.sert.domain.*;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthFunctionRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthPermissionRepository;
import com.nexigroup.pagopa.cruscotto.sert.service.AuthFunctionService;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthFunctionDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthPermissionDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.exception.GenericServiceException;
import com.nexigroup.pagopa.cruscotto.sert.service.filter.AuthFunctionFilter;
import com.nexigroup.pagopa.cruscotto.sert.service.mapper.AuthFunctionMapper;
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
 * Service Implementation for managing {@link AuthFunction}.
 */
@Service
@Transactional
public class AuthFunctionServiceImpl implements AuthFunctionService {

    private static final String AUTH_FUNCTION = "authFunction";

    private static final String DESCRIZIONE = "descrizione";
    private static final String MODULO = "modulo";

    private final Logger log = LoggerFactory.getLogger(AuthFunctionServiceImpl.class);

    private final AuthPermissionRepository authPermissionRepository;

    private final QueryBuilder queryBuilder;

    private final AuthFunctionRepository authFunctionRepository;

    private final AuthFunctionMapper authFunctionMapper;

    public AuthFunctionServiceImpl(
        AuthPermissionRepository authPermissionRepository,
        QueryBuilder queryBuilder,
        AuthFunctionRepository authFunctionRepository,
        AuthFunctionMapper authFunctionMapper
    ) {
        this.authPermissionRepository = authPermissionRepository;
        this.queryBuilder = queryBuilder;
        this.authFunctionRepository = authFunctionRepository;
        this.authFunctionMapper = authFunctionMapper;
    }

    /**
     * Save a authFunction.
     *
     * @param authFunctionDTO the entity to save.
     * @return the persisted entity.
     */
    @Override
    public AuthFunctionDTO save(AuthFunctionDTO authFunctionDTO) {
        log.debug("Request to save AuthFunction : {}", authFunctionDTO);

        AuthFunction authFunction = new AuthFunction();
        authFunction.setNome(authFunctionDTO.getNome());
        authFunction.setModulo(authFunctionDTO.getModulo());
        authFunction.setDescrizione(authFunctionDTO.getDescrizione());

        authFunction = authFunctionRepository.save(authFunction);

        return authFunctionMapper.toDto(authFunction);
    }

    /**
     * Save a authFunction.
     *
     * @param authFunctionDTO the entity to save.
     * @return the persisted entity.
     */
    @Override
    public Optional<AuthFunctionDTO> update(AuthFunctionDTO authFunctionDTO) {
        return Optional.of(authFunctionRepository.findById(authFunctionDTO.getId()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(function -> {
                function.setNome(authFunctionDTO.getNome());
                function.setModulo(authFunctionDTO.getModulo());
                function.setDescrizione(authFunctionDTO.getDescrizione());

                log.debug("Changed AuthFunction: {}", function);
                return function;
            })
            .map(authFunctionMapper::toDto);
    }

    /**
     * Get all the authFunctions.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AuthFunctionDTO> findAll(AuthFunctionFilter filter, Pageable pageable) {
        log.debug("Request to get all AuthFunctions");

        BooleanBuilder builder = new BooleanBuilder();

        if (StringUtils.isNotBlank(filter.getNome())) {
            builder.and(QAuthFunction.authFunction.nome.containsIgnoreCase(filter.getNome()));
        }

        if (StringUtils.isNotBlank(filter.getDescrizione())) {
            builder.and(QAuthFunction.authFunction.descrizione.containsIgnoreCase(filter.getDescrizione()));
        }

        JPQLQuery<AuthFunction> jpql = queryBuilder.<AuthFunction>createQuery().from(QAuthFunction.authFunction).where(builder);

        long size = jpql.fetchCount();

        JPQLQuery<AuthFunctionDTO> jpqlSelected = jpql.select(
            Projections.fields(
                AuthFunctionDTO.class,
                QAuthFunction.authFunction.id.as("id"),
                QAuthFunction.authFunction.nome.as("nome"),
                QAuthFunction.authFunction.modulo.as(MODULO),
                QAuthFunction.authFunction.descrizione.as(DESCRIZIONE)
            )
        );

        jpqlSelected.offset(pageable.getOffset());
        jpqlSelected.limit(pageable.getPageSize());

        pageable
            .getSortOr(Sort.by(Sort.Direction.ASC, "id"))
            .forEach(order -> {
                jpqlSelected.orderBy(
                    new OrderSpecifier<>(
                        order.isAscending() ? Order.ASC : Order.DESC,
                        Expressions.stringPath(order.getProperty()),
                        QdslUtility.toQueryDslNullHandling(order.getNullHandling())
                    )
                );
            });

        List<AuthFunctionDTO> list = jpqlSelected.fetch();

        return new PageImpl<>(list, pageable, size);
    }

    /**
     * Get all the authFunctions with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<AuthFunctionDTO> findAllWithEagerRelationships(Pageable pageable) {
        return authFunctionRepository.findAllWithEagerRelationships(pageable).map(authFunctionMapper::toDto);
    }

    /**
     * Get one authFunction by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<AuthFunctionDTO> findOne(Long id) {
        log.debug("Request to get AuthFunction : {}", id);

        QAuthFunction authFunction = QAuthFunction.authFunction;

        JPQLQuery<AuthFunction> jpql = queryBuilder.<AuthFunction>createQuery().from(authFunction).where(authFunction.id.eq(id));

        JPQLQuery<AuthFunctionDTO> jpqlResponse = jpql.select(
            Projections.fields(
                AuthFunctionDTO.class,
                authFunction.id.as("id"),
                authFunction.nome.as("nome"),
                authFunction.modulo.as(MODULO),
                authFunction.descrizione.as(DESCRIZIONE)
            )
        );

        return Optional.ofNullable(jpqlResponse.fetchOne());
    }

    /**
     * Get one authFunction with permissions.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    public Optional<AuthFunctionDTO> findOneWithEagerRelationships(Long id) {
        log.debug("Request to get AuthFunction : {}", id);
        return authFunctionRepository.findOneWithEagerRelationships(id).map(authFunctionMapper::toDto);
    }

    /**
     * Delete the authFunction by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete AuthFunction : {}", id);
        authFunctionRepository.deleteById(id);
    }

    @Override
    public Page<AuthFunctionDTO> listAllFunctionSelected(long idGroup, Pageable pageable) {
        QAuthGroupAuthFunction authGroupAuthFunction = QAuthGroupAuthFunction.authGroupAuthFunction;

        JPQLQuery<AuthGroupAuthFunction> jpql = queryBuilder
            .<AuthGroupAuthFunction>createQuery()
            .from(authGroupAuthFunction)
            .join(authGroupAuthFunction.funzione, QAuthFunction.authFunction)
            .where(authGroupAuthFunction.gruppo.id.eq(idGroup));

        long size = jpql.fetchCount();

        JPQLQuery<AuthFunctionDTO> jpqlSelected = jpql.select(
            Projections.fields(
                AuthFunctionDTO.class,
                QAuthFunction.authFunction.id.as("id"),
                QAuthFunction.authFunction.modulo.as(MODULO),
                QAuthFunction.authFunction.nome.as("nome"),
                QAuthFunction.authFunction.descrizione.as(DESCRIZIONE)
            )
        );

        jpqlSelected.offset(pageable.getOffset());
        jpqlSelected.limit(pageable.getPageSize());

        Sort sort = pageable.getSortOr(Sort.by(Sort.Direction.ASC, "id"));

        sort.forEach(order -> {
            jpqlSelected.orderBy(
                new OrderSpecifier<>(
                    order.isAscending() ? Order.ASC : Order.DESC,
                    Expressions.stringPath(order.getProperty()),
                    QdslUtility.toQueryDslNullHandling(order.getNullHandling())
                )
            );
        });

        List<AuthFunctionDTO> list = jpqlSelected.fetch();

        return new PageImpl<>(list, pageable, size);
    }

    @Override
    public List<Long> listAllFunctionSelected(long idGroup) {
        QAuthGroupAuthFunction authGroupAuthFunction = QAuthGroupAuthFunction.authGroupAuthFunction;

        JPQLQuery<AuthGroupAuthFunction> jpql = queryBuilder
            .<AuthGroupAuthFunction>createQuery()
            .from(authGroupAuthFunction)
            .join(authGroupAuthFunction.funzione, QAuthFunction.authFunction)
            .where(authGroupAuthFunction.gruppo.id.eq(idGroup));

        JPQLQuery<Long> jpqlSelected = jpql.select(QAuthFunction.authFunction.id.as("id"));

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
    public Page<AuthFunctionDTO> listAllFunctionAssociabili(long idGroup, Optional<String> partialNomeFunzione, Pageable pageable) {
        String nomeFunzione = partialNomeFunzione.orElse("");

        QAuthFunction authFunction = QAuthFunction.authFunction;

        JPQLQuery<Long> subQuery = JPAExpressions.select(authFunction.id)
            .from(authFunction)
            .innerJoin(authFunction.authGroups, QAuthGroup.authGroup)
            .where(QAuthGroup.authGroup.id.eq(idGroup));

        BooleanBuilder whereCondition = new BooleanBuilder();
        whereCondition.and(authFunction.id.notIn(subQuery)).and(authFunction.nome.containsIgnoreCase(nomeFunzione));

        JPQLQuery<AuthFunction> jpql = queryBuilder.<AuthFunction>createQuery().from(authFunction).where(whereCondition);

        long size = jpql.fetchCount();

        JPQLQuery<AuthFunctionDTO> jpqlSelected = jpql.select(
            Projections.fields(
                AuthFunctionDTO.class,
                authFunction.id.as("id"),
                authFunction.nome.as("nome"),
                authFunction.modulo.as(MODULO),
                authFunction.descrizione.as(DESCRIZIONE)
            )
        );

        jpqlSelected.offset(pageable.getOffset());
        jpqlSelected.limit(pageable.getPageSize());

        Sort sort = Sort.by(Sort.Direction.ASC, "nome");

        sort.forEach(order -> {
            jpqlSelected.orderBy(
                new OrderSpecifier<>(
                    order.isAscending() ? Order.ASC : Order.DESC,
                    Expressions.stringPath(order.getProperty()),
                    QdslUtility.toQueryDslNullHandling(order.getNullHandling())
                )
            );
        });

        List<AuthFunctionDTO> list = jpqlSelected.fetch();

        return new PageImpl<>(list, pageable, size);
    }

    @Override
    public void associaPermesso(long idFunzione, AuthPermissionDTO[] permessi) {
        AuthFunction function = authFunctionRepository
            .findById(idFunzione)
            .orElseThrow(() ->
                new GenericServiceException(
                    String.format("Function not exist with id %s", idFunzione),
                    AUTH_FUNCTION,
                    "authFunction.notExists"
                )
            );

        for (AuthPermissionDTO authPermissionDTO : permessi) {
            AuthPermission authPermission = authPermissionRepository.findById(authPermissionDTO.getId()).orElse(null);

            if (authPermission != null) {
                function.getAuthPermissions().add(authPermission);

                authFunctionRepository.save(function);
            }
        }
    }

    @Override
    public void rimuoviAssociazionePermesso(Long idFunzione, Long idPermesso) {
        AuthFunction function = authFunctionRepository
            .findById(idFunzione)
            .orElseThrow(() ->
                new GenericServiceException(
                    String.format("Function not exist with id %s", idFunzione),
                    AUTH_FUNCTION,
                    "authFunction.notExists"
                )
            );

        AuthPermission authPermission = authPermissionRepository
            .findById(idPermesso)
            .orElseThrow(() ->
                new GenericServiceException(
                    String.format("Permesso not exist with id %s", idPermesso),
                    AUTH_FUNCTION,
                    "authFunction.dissociaPermesso.permessoNotExists"
                )
            );

        function.getAuthPermissions().remove(authPermission);

        authFunctionRepository.save(function);
    }
}
