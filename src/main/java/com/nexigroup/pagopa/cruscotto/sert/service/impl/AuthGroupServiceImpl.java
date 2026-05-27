package com.nexigroup.pagopa.cruscotto.sert.service.impl;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthFunction;
import com.nexigroup.pagopa.cruscotto.sert.domain.AuthGroup;
import com.nexigroup.pagopa.cruscotto.sert.domain.QAuthGroup;
import com.nexigroup.pagopa.cruscotto.sert.domain.QAuthUser;
import com.nexigroup.pagopa.cruscotto.sert.domain.enumeration.AuthenticationType;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthFunctionRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthGroupRepository;
import com.nexigroup.pagopa.cruscotto.sert.security.SecurityUtils;
import com.nexigroup.pagopa.cruscotto.sert.service.AuthGroupService;
import com.nexigroup.pagopa.cruscotto.sert.service.bean.AuthGroupUpdateRequestBean;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthFunctionDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthGroupDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.exception.GenericServiceException;
import com.nexigroup.pagopa.cruscotto.sert.service.filter.AuthGroupFilter;
import com.nexigroup.pagopa.cruscotto.sert.service.mapper.AuthGroupMapper;
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
 * Service Implementation for managing {@link AuthGroup}.
 */
@Service
@Transactional
public class AuthGroupServiceImpl implements AuthGroupService {

    private static final String AUTH_GROUP = "authGroup";

    private final Logger log = LoggerFactory.getLogger(AuthGroupServiceImpl.class);

    private final AuthFunctionRepository authFunctionRepository;

    private final QueryBuilder queryBuilder;

    private final AuthGroupRepository authGroupRepository;

    private final AuthGroupMapper authGroupMapper;

    public AuthGroupServiceImpl(
        AuthFunctionRepository authFunctionRepository,
        QueryBuilder queryBuilder,
        AuthGroupRepository authGroupRepository,
        AuthGroupMapper authGroupMapper
    ) {
        this.authFunctionRepository = authFunctionRepository;
        this.queryBuilder = queryBuilder;
        this.authGroupRepository = authGroupRepository;
        this.authGroupMapper = authGroupMapper;
    }
    /**
     * Save a authGroup.
     *
     * @param authGroupDTO the entity to save.
     * @return the persisted entity.
     */
    @Override
    public AuthGroupDTO save(AuthGroupDTO authGroupDTO) {
        log.debug("Service to save AuthGroup : {}", authGroupDTO);

        AuthGroup authGroup = new AuthGroup();
        authGroup.setNome(authGroupDTO.getNome());
        authGroup.setDescrizione(authGroupDTO.getDescrizione());
        authGroup.setLivelloVisibilita(authGroupRepository.getMaxLivelloVisibilita() + 1);

        authGroup = authGroupRepository.save(authGroup);
        return authGroupMapper.toDto(authGroup);
    }

    /**
     * Update a authGroup.
     *
     * @param authGroupDTO the entity to save.
     * @return the persisted entity.
     */
    @Override
    public Optional<AuthGroupDTO> update(AuthGroupDTO authGroupDTO) {
        return Optional.of(authGroupRepository.findById(authGroupDTO.getId()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(group -> {
                group.setNome(authGroupDTO.getNome());
                group.setDescrizione(authGroupDTO.getDescrizione());

                log.debug("Changed AuthGroup: {}", group);
                return group;
            })
            .map(authGroupMapper::toDto);
    }

    /**
     * Get all the authGroups.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AuthGroupDTO> findAll(AuthGroupFilter filter, Pageable pageable) {
        log.debug("Service to get all AuthGroups");

        QAuthGroup qAuthGroup = QAuthGroup.authGroup;
        BooleanBuilder builder = new BooleanBuilder();

        List<String> roles = SecurityUtils.getRolesFromJwt();

        builder.and(qAuthGroup.objectId.in(roles));

        if (StringUtils.isNotBlank(filter.getNome())) {
            builder.and(qAuthGroup.nome.containsIgnoreCase(filter.getNome()));
        }

        if (StringUtils.isNotBlank(filter.getDescrizione())) {
            builder.and(qAuthGroup.descrizione.containsIgnoreCase(filter.getDescrizione()));
        }

        JPQLQuery<AuthGroup> jpql = queryBuilder.<AuthGroup>createQuery()
            .from(qAuthGroup)
            .where(builder);
        long size = jpql.fetchCount();

        JPQLQuery<AuthGroupDTO> jpqlSelected = jpql.select(
            Projections.fields(
                AuthGroupDTO.class,
                qAuthGroup.id.as("id"),
                qAuthGroup.nome.as("nome"),
                qAuthGroup.descrizione.as("descrizione"),
                qAuthGroup.livelloVisibilita.as("livelloVisibilita")
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

        List<AuthGroupDTO> list = jpqlSelected.fetch();

        return new PageImpl<>(list, pageable, size);
    }

    /**
     * Get all the authGroups with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<AuthGroupDTO> findAllWithEagerRelationships(Pageable pageable) {
        return authGroupRepository.findAllWithEagerRelationships(pageable).map(authGroupMapper::toDto);
    }

    /**
     * Get one authGroup by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<AuthGroupDTO> findOne(Long id) {
        log.debug("Service to get AuthGroup : {}", id);
        QAuthGroup qAuthGroup = QAuthGroup.authGroup;

        JPQLQuery<AuthGroup> jpql = queryBuilder.<AuthGroup>createQuery().from(qAuthGroup).where(qAuthGroup.id.eq(id));

        JPQLQuery<AuthGroupDTO> jpqlResponse = jpql.select(
            Projections.fields(
                AuthGroupDTO.class, //
                qAuthGroup.id.as("id"),
                qAuthGroup.nome.as("nome"),
                qAuthGroup.descrizione.as("descrizione"),
                qAuthGroup.livelloVisibilita.as("livelloVisibilita")
            )
        );
        return Optional.ofNullable(jpqlResponse.fetchOne());
    }

    /**
     * Get one authGroup with functions.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<AuthGroupDTO> findOneWithEagerRelationships(Long id) {
        return authGroupRepository.findOneWithEagerRelationships(id).map(authGroupMapper::toDto);
    }

    /**
     * Delete the authGroup by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Service to delete AuthGroup : {}", id);
        authGroupRepository.deleteById(id);
    }

    @Override
    public void associaFunzioni(long idGruppo, AuthFunctionDTO[] funzioni) {
        AuthGroup group = authGroupRepository
            .findById(idGruppo)
            .orElseThrow(() ->
                new GenericServiceException(String.format("Group not exist with id %s", idGruppo), AUTH_GROUP, "authGroup.notExists")
            );

        for (AuthFunctionDTO authFunctionDTO : funzioni) {
            AuthFunction authFunction = authFunctionRepository.findById(authFunctionDTO.getId()).orElse(null);

            if (authFunction != null) {
                group.getAuthFunctions().add(authFunction);

                authGroupRepository.save(group);
            }
        }
    }

    @Override
    public void rimuoviAssociazioneFunzione(Long idGroup, Long funzioneId) {
        AuthGroup group = authGroupRepository
            .findById(idGroup)
            .orElseThrow(() ->
                new GenericServiceException(String.format("Group not exist with id %s", idGroup), AUTH_GROUP, "authGroup.notExists")
            );

        AuthFunction authFunction = authFunctionRepository
            .findById(funzioneId)
            .orElseThrow(() ->
                new GenericServiceException(
                    String.format("Funzione not exist with id %s", funzioneId),
                    AUTH_GROUP,
                    "authGroup.dissociaFunzione.funzioneNotExists"
                )
            );

        group.getAuthFunctions().remove(authFunction);

        authGroupRepository.save(group);
    }

    @Override
    public void aggiornaLivelloVisibilitaGruppi(AuthGroupUpdateRequestBean[] authGroups) {
        for (AuthGroupUpdateRequestBean authGroup : authGroups) {
            log.info("Service to update visibility level of group {}", authGroup);

            queryBuilder
                .createQueryFactory()
                .update(QAuthGroup.authGroup)
                .set(QAuthGroup.authGroup.livelloVisibilita, Integer.valueOf(authGroup.getLivelloVisibilita()))
                .where(QAuthGroup.authGroup.id.eq(Long.valueOf(authGroup.getId())))
                .execute();
        }
    }

    @Override
    public boolean isAuthGroupValid(String userLoginUtenteLoggato, AuthenticationType authenticationType, Long authGroupId) {
        log.info("Service - controllo abilitazione utente {} al gruppo {}", userLoginUtenteLoggato, authGroupId);

        QAuthGroup qAuthGroup = QAuthGroup.authGroup;

        BooleanBuilder builder = new BooleanBuilder();

        builder.and(qAuthGroup.id.eq(authGroupId));

        //utenti appartenenti a gruppi con livello di visibilità superiore o uguale al livello di visibilità del gruppo dell'utente loggato
        JPQLQuery<Integer> livelloVisibilitaGruppoUtenteLoggato = JPAExpressions.select(QAuthGroup.authGroup.livelloVisibilita)
            .from(QAuthGroup.authGroup)
            .innerJoin(QAuthGroup.authGroup.authUsers, QAuthUser.authUser)
            .where(QAuthUser.authUser.login.eq(userLoginUtenteLoggato).and(QAuthUser.authUser.authenticationType.eq(authenticationType)));

        builder.and(qAuthGroup.livelloVisibilita.goe(livelloVisibilitaGruppoUtenteLoggato));

        JPQLQuery<Long> jpql = queryBuilder.createQuery().from(qAuthGroup).select(qAuthGroup.id).where(builder);

        return Optional.ofNullable(jpql.fetchFirst()).isPresent();
    }
}
