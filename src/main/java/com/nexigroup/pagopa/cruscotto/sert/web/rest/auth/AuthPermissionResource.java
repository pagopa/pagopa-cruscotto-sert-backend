package com.nexigroup.pagopa.cruscotto.sert.web.rest.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthPermission;
import com.nexigroup.pagopa.cruscotto.sert.security.AuthoritiesConstants;
import com.nexigroup.pagopa.cruscotto.sert.service.AuthPermissionService;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthPermissionDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.filter.AuthPermissionFilter;
import com.nexigroup.pagopa.cruscotto.sert.web.rest.errors.BadRequestAlertException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link AuthPermission}.
 */
@RestController
@RequestMapping("/api")
public class AuthPermissionResource {

    private final Logger log = LoggerFactory.getLogger(AuthPermissionResource.class);

    private static final String ENTITY_NAME = "authPermission";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AuthPermissionService authPermissionService;

    public AuthPermissionResource(AuthPermissionService authPermissionService) {
        this.authPermissionService = authPermissionService;
    }

    /**
     * {@code POST  /auth-permissions} : Create a new authPermission.
     *
     * @param authPermissionDTO the authPermissionDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new authPermissionDTO, or with status {@code 400 (Bad Request)} if the authPermission has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/auth-permissions")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.PERMISSION_CREATION + "\")")
    public ResponseEntity<AuthPermissionDTO> createAuthPermission(@Valid @RequestBody AuthPermissionDTO authPermissionDTO)
        throws URISyntaxException {
        log.debug("REST request to save AuthPermission : {}", authPermissionDTO);
        if (authPermissionDTO.getId() != null) {
            throw new BadRequestAlertException("A new authPermission cannot already have an ID", ENTITY_NAME, "idexists");
        }
        AuthPermissionDTO result = authPermissionService.save(authPermissionDTO);
        return ResponseEntity.created(new URI("/api/auth-permissions/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /auth-permissions} : Updates an existing authPermission.
     *
     * @param authPermissionDTO the authPermissionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated authPermissionDTO,
     * or with status {@code 400 (Bad Request)} if the authPermissionDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the authPermissionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/auth-permissions")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.PERMISSION_MODIFICATION + "\")")
    public ResponseEntity<AuthPermissionDTO> updateAuthPermission(@Valid @RequestBody AuthPermissionDTO authPermissionDTO)
        throws URISyntaxException {
        log.debug("REST request to update AuthPermission : {}", authPermissionDTO);
        if (authPermissionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        AuthPermissionDTO result = authPermissionService.save(authPermissionDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, authPermissionDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /auth-permissions} : get all the authPermissions.
     *

     * @param pageable the pagination information.

     * @param filter the filter which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of authPermissions in body.
     */
    @GetMapping("/auth-permissions")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.PERMISSION_LIST + "\")")
    public ResponseEntity<List<AuthPermissionDTO>> getAllAuthPermissions(
        @Parameter(description = "Filtro", required = false) @Valid @ParameterObject AuthPermissionFilter filter,
        @Parameter(description = "Pageable", required = true) @ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get AuthPermissions by criteria: {}", filter);
        Page<AuthPermissionDTO> page = authPermissionService.findAll(filter, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /auth-permissions/auth-function/:idFunction} : get all the authPermissions.
     *
     * @param idFunction the id function which the requested entities should match.
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of authPermissions in body.
     */
    @GetMapping("/auth-permissions/auth-function/{idFunction}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.PERMISSION_LIST_ASSOCIATED_WITH_FUNCTION + "\")")
    public ResponseEntity<List<AuthPermissionDTO>> getAllAuthFunctionsSelected(
        @PathVariable Long idFunction,
        @Parameter(description = "Pageable", required = true) @ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get AuthPermissions by id function: {}", idFunction);
        Page<AuthPermissionDTO> page = authPermissionService.listAllPermissionSelected(idFunction, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /auth-permissions/auth-function/:idFunction}/associabili : get all the authPermissions.
     *
     * @param idFunction the id function which the requested entities should match.
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of authPermissions in body.
     */
    @GetMapping("/auth-permissions/auth-function/{idFunction}/associabili")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.PERMISSION_LIST_ASSOCIABLE_WITH_FUNCTION + "\")")
    public ResponseEntity<List<AuthPermissionDTO>> getAllAuthFunctionsAssociabili(
        @PathVariable Long idFunction,
        @RequestParam Optional<String> nameFilter,
        @Parameter(description = "Pageable", required = true) @ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get AuthPermissions associabili by id function: {}", idFunction);
        Page<AuthPermissionDTO> page = authPermissionService.listAllPermissionAssociabili(idFunction, nameFilter, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /auth-permissions/:id} : get the "id" authPermission.
     *
     * @param id the id of the authPermissionDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the authPermissionDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/auth-permissions/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.PERMISSION_DETAIL + "\")")
    public ResponseEntity<AuthPermissionDTO> getAuthPermission(@PathVariable Long id) {
        log.debug("REST request to get AuthPermission : {}", id);
        Optional<AuthPermissionDTO> authPermissionDTO = authPermissionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(authPermissionDTO);
    }

    /**
     * {@code DELETE  /auth-permissions/:id} : delete the "id" authPermission.
     *
     * @param id the id of the authPermissionDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/auth-permissions/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.PERMISSION_DELETION + "\")")
    public ResponseEntity<Void> deleteAuthPermission(@PathVariable Long id) {
        log.debug("REST request to delete AuthPermission : {}", id);
        authPermissionService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
