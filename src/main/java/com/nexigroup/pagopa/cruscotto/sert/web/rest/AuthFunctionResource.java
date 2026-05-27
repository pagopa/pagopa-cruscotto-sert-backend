package com.nexigroup.pagopa.cruscotto.sert.web.rest;

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

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthFunction;
import com.nexigroup.pagopa.cruscotto.sert.security.AuthoritiesConstants;
import com.nexigroup.pagopa.cruscotto.sert.service.AuthFunctionService;
import com.nexigroup.pagopa.cruscotto.sert.service.AuthPermissionService;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthFunctionDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthPermissionDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.filter.AuthFunctionFilter;
import com.nexigroup.pagopa.cruscotto.sert.web.rest.errors.BadRequestAlertException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link AuthFunction}.
 */
@RestController
@RequestMapping("/api")
public class AuthFunctionResource {

    private final Logger log = LoggerFactory.getLogger(AuthFunctionResource.class);

    private static final String ENTITY_NAME = "authFunction";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AuthPermissionService authPermissionService;

    private final AuthFunctionService authFunctionService;

    public AuthFunctionResource(AuthFunctionService authFunctionService, AuthPermissionService authPermissionService) {
        this.authPermissionService = authPermissionService;
        this.authFunctionService = authFunctionService;
    }

    /**
     * {@code POST  /auth-functions} : Create a new authFunction.
     *
     * @param authFunctionDTO the authFunctionDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new authFunctionDTO, or with status {@code 400 (Bad Request)} if the authFunction has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/auth-functions")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.FUNCTION_CREATION + "\")")
    public ResponseEntity<AuthFunctionDTO> createAuthFunction(@Valid @RequestBody AuthFunctionDTO authFunctionDTO)
        throws URISyntaxException {
        log.debug("REST request to save AuthFunction : {}", authFunctionDTO);
        if (authFunctionDTO.getId() != null) {
            throw new BadRequestAlertException("A new authFunction cannot already have an ID", ENTITY_NAME, "idexists");
        }
        AuthFunctionDTO result = authFunctionService.save(authFunctionDTO);
        return ResponseEntity.created(new URI("/api/auth-functions/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /auth-functions} : Updates an existing authFunction.
     *
     * @param authFunctionDTO the authFunctionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated authFunctionDTO,
     * or with status {@code 400 (Bad Request)} if the authFunctionDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the authFunctionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/auth-functions")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.FUNCTION_MODIFICATION + "\")")
    public ResponseEntity<AuthFunctionDTO> updateAuthFunction(@Valid @RequestBody AuthFunctionDTO authFunctionDTO)
        throws URISyntaxException {
        log.debug("REST request to update AuthFunction : {}", authFunctionDTO);
        if (authFunctionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        AuthFunctionDTO result = authFunctionService.update(authFunctionDTO).orElseGet(() -> null);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, authFunctionDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /auth-functions} : get all the authFunctions.
     *

     * @param pageable the pagination information.

     * @param filter the filter which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of authFunctions in body.
     */
    @GetMapping("/auth-functions")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.FUNCTION_LIST + "\")")
    public ResponseEntity<List<AuthFunctionDTO>> getAllAuthFunctions(
        @Parameter(description = "Filter", required = false) @Valid @ParameterObject AuthFunctionFilter filter,
        @Parameter(description = "Pageable", required = true) @ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get AuthFunctions by filter: {}", filter);
        Page<AuthFunctionDTO> page = authFunctionService.findAll(filter, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /auth-functions/:id} : get the "id" authFunction.
     *
     * @param id the id of the authFunctionDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the authFunctionDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/auth-functions/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.FUNCTION_DETAIL + "\")")
    public ResponseEntity<AuthFunctionDTO> getAuthFunction(@PathVariable Long id) {
        log.debug("REST request to get AuthFunction : {}", id);
        Optional<AuthFunctionDTO> authFunctionDTO = authFunctionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(authFunctionDTO);
    }

    /**
     * {@code GET  /auth-functions/detail/:id} : get the "id" authFunction.
     *
     * @param id the id of the authFunctionDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the authFunctionDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/auth-functions/detail/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.FUNCTION_DETAIL + "\")")
    public ResponseEntity<AuthFunctionDTO> getAuthFunctionWithPermission(@PathVariable Long id) {
        log.debug("REST request to get AuthGroup : {}", id);
        Optional<AuthFunctionDTO> authFunctionDTO = authFunctionService.findOneWithEagerRelationships(id);
        return ResponseUtil.wrapOrNotFound(authFunctionDTO);
    }

    /**
     * {@code DELETE  /auth-functions/:id} : delete the "id" authFunction.
     *
     * @param id the id of the authFunctionDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/auth-functions/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.FUNCTION_DELETION + "\")")
    public ResponseEntity<Void> deleteAuthFunction(@PathVariable Long id) {
        log.debug("REST request to delete AuthFunction : {}", id);
        authFunctionService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    @GetMapping("/auth-functions/auth-group/{idGroup}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.FUNCTION_LIST_ASSOCIATED_WITH_GROUP + "\")")
    public ResponseEntity<List<AuthFunctionDTO>> getAllAuthFunctions(
        @PathVariable Long idGroup,
        @Parameter(description = "Pageable", required = true) @ParameterObject Pageable pageable
    ) {
        Page<AuthFunctionDTO> page = authFunctionService.listAllFunctionSelected(idGroup, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/auth-functions/auth-group/{idGroup}/associabili")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.FUNCTION_LIST_ASSOCIABLE_WITH_GROUP + "\")")
    public ResponseEntity<List<AuthFunctionDTO>> getAllAuthFunctionsAssociabili(
        @PathVariable Long idGroup,
        @RequestParam Optional<String> nameFilter,
        @Parameter(description = "Pageable", required = true) @ParameterObject Pageable pageable
    ) {
        Page<AuthFunctionDTO> page = authFunctionService.listAllFunctionAssociabili(idGroup, nameFilter, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @PostMapping("/auth-functions/{idFunction}/associa-permessi")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.FUNCTION_MODIFICATION_PERMISSION_ASSOCIATION + "\")")
    public ResponseEntity<Void> aggiungiAssociazioneFunzione(
        @PathVariable Long idFunction,
        @Valid @RequestBody AuthPermissionDTO[] permessi
    ) throws URISyntaxException {
        log.debug("REST request to save permission da associare alla funzione");

        if (permessi == null || permessi.length == 0) throw new BadRequestAlertException(
            "Selezionare almeno un permesso da associare alla funzione",
            "authFunction.associaPermessi.permessiDaAssociare.notEmpty"
        );

        //verifico che il permesso selezionato non sia già stato associato

        List<AuthPermissionDTO> permessiDaAssociare = Arrays.asList(permessi);

        List<Long> permessiAssociati = authPermissionService.listAllPermissionSelected(idFunction);

        Boolean check = permessiAssociati
            .stream()
            .anyMatch(permessiDaAssociare.stream().map(AuthPermissionDTO::getId).collect(Collectors.toSet())::contains);

        if (check) {
            throw new BadRequestAlertException(
                "Permesso già associata alla funzione",
                "authFunction.associaPermessi.permessoAlreadyAssociated"
            );
        }

        // Associa Funzioni
        authFunctionService.associaPermesso(idFunction, permessi);

        return ResponseEntity.created(new URI("api/auth-functions/" + idFunction.toString() + "/associa-permessi"))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, "authFunction.message.associaPermessi", ""))
            .body(null);
    }

    @GetMapping("/auth-functions/{idFunction}/rimuovi-permesso/{idPermesso}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.FUNCTION_MODIFICATION_PERMISSION_DISASSOCIATION + "\")")
    public ResponseEntity<Void> rimuoviAssociazioneFunzione(@PathVariable Long idFunction, @PathVariable Long idPermesso)
        throws URISyntaxException {
        log.debug("Rimuovi associazione permesso (id) {} alla funzione (id) {}", idFunction, idPermesso);

        // Dissocia funzione
        authFunctionService.rimuoviAssociazionePermesso(idFunction, idPermesso);

        return ResponseEntity.created(new URI("api/auth-functions/" + idFunction.toString() + "/rimuovi-permessi/" + idPermesso.toString()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, "authFunction.message.dissociaPermesso", ""))
            .body(null);
    }
}
