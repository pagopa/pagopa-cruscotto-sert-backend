package com.nexigroup.pagopa.cruscotto.sert.service;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthPermission;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthPermissionDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.filter.AuthPermissionFilter;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link AuthPermission}.
 */
public interface AuthPermissionService {
    /**
     * Save a authPermission.
     *
     * @param authPermissionDTO the entity to save.
     * @return the persisted entity.
     */
    AuthPermissionDTO save(AuthPermissionDTO authPermissionDTO);

    /**
     * Get all the authPermissions.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<AuthPermissionDTO> findAll(AuthPermissionFilter filter, Pageable pageable);

    /**
     * Get the "id" authPermission.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AuthPermissionDTO> findOne(Long id);

    /**
     * Delete the "id" authPermission.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    List<Long> listAllPermissionSelected(long idFunction);

    Page<AuthPermissionDTO> listAllPermissionSelected(long idFunction, Pageable pageable);

    Page<AuthPermissionDTO> listAllPermissionAssociabili(long idFunction, Optional<String> optionalNomePermesso, Pageable pageable);
}
