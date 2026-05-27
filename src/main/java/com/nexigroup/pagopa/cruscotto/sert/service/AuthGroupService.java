package com.nexigroup.pagopa.cruscotto.sert.service;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthGroup;
import com.nexigroup.pagopa.cruscotto.sert.domain.enumeration.AuthenticationType;
import com.nexigroup.pagopa.cruscotto.sert.service.bean.AuthGroupUpdateRequestBean;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthFunctionDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthGroupDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.filter.AuthGroupFilter;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link AuthGroup}.
 */
public interface AuthGroupService {
    /**
     * Save a authGroup.
     *
     * @param authGroupDTO the entity to save.
     * @return the persisted entity.
     */
    AuthGroupDTO save(AuthGroupDTO authGroupDTO);

    /**
     * Save a authGroup.
     *
     * @param authGroupDTO the entity to save.
     * @return the persisted entity.
     */
    Optional<AuthGroupDTO> update(AuthGroupDTO authGroupDTO);

    /**
     * Get all the authGroups.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<AuthGroupDTO> findAll(AuthGroupFilter filter, Pageable pageable);

    /**
     * Get all the authGroups with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    Page<AuthGroupDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" authGroup.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AuthGroupDTO> findOne(Long id);

    /**
     * Get the "id" authGroup with function.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AuthGroupDTO> findOneWithEagerRelationships(Long id);

    /**
     * Delete the "id" authGroup.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    public void associaFunzioni(long idGruppo, AuthFunctionDTO[] funzioni);

    public void rimuoviAssociazioneFunzione(Long idGroup, Long funzioneId);

    void aggiornaLivelloVisibilitaGruppi(AuthGroupUpdateRequestBean[] authGroups);

    boolean isAuthGroupValid(String userLoginUtenteLoggato, AuthenticationType authenticationType, Long authGroupId);
}
