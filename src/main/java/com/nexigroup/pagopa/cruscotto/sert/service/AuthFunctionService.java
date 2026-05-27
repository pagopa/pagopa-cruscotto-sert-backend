package com.nexigroup.pagopa.cruscotto.sert.service;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthFunction;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthFunctionDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthPermissionDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.filter.AuthFunctionFilter;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link AuthFunction}.
 */
public interface AuthFunctionService {
    /**
     * Save a authFunction.
     *
     * @param authFunctionDTO the entity to save.
     * @return the persisted entity.
     */
    AuthFunctionDTO save(AuthFunctionDTO authFunctionDTO);

    /**
     * Save a authGroup.
     *
     * @param authFunctionDTO the entity to save.
     * @return the persisted entity.
     */
    Optional<AuthFunctionDTO> update(AuthFunctionDTO authFunctionDTO);

    /**
     * Get all the authFunctions.
     *
     * @param filter the pagination filter.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<AuthFunctionDTO> findAll(AuthFunctionFilter filter, Pageable pageable);

    /**
     * Get all the authFunctions with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    Page<AuthFunctionDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" authFunction.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AuthFunctionDTO> findOne(Long id);

    /**
     * Get the "id" authFunction with permission.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AuthFunctionDTO> findOneWithEagerRelationships(Long id);

    /**
     * Delete the "id" authFunction.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    Page<AuthFunctionDTO> listAllFunctionSelected(long idGroup, Pageable pageable);

    List<Long> listAllFunctionSelected(long idGroup);

    Page<AuthFunctionDTO> listAllFunctionAssociabili(long idGroup, Optional<String> partialNomeFunzione, Pageable pageable);

    public void associaPermesso(long idFunzione, AuthPermissionDTO[] permessi);

    public void rimuoviAssociazionePermesso(Long idFunzione, Long idPermesso);
}
