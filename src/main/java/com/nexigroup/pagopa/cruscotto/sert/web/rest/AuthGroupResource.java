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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthGroup;
import com.nexigroup.pagopa.cruscotto.sert.security.AuthoritiesConstants;
import com.nexigroup.pagopa.cruscotto.sert.service.AuthFunctionService;
import com.nexigroup.pagopa.cruscotto.sert.service.AuthGroupService;
import com.nexigroup.pagopa.cruscotto.sert.service.bean.AuthGroupUpdateRequestBean;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthFunctionDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthGroupDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.filter.AuthGroupFilter;
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
 * REST controller for managing {@link AuthGroup}.
 */
@RestController
@RequestMapping("/api")
public class AuthGroupResource {

    private final Logger log = LoggerFactory.getLogger(AuthGroupResource.class);

    private static final String ENTITY_NAME = "authGroup";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AuthFunctionService authFunctionService;

    private final AuthGroupService authGroupService;

    public AuthGroupResource(AuthFunctionService authFunctionService, AuthGroupService authGroupService) {
        this.authFunctionService = authFunctionService;
        this.authGroupService = authGroupService;
    }

    /**
     * {@code POST  /auth-groups} : Create a new authGroup.
     *
     * @param authGroupDTO the authGroupDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new authGroupDTO, or with status {@code 400 (Bad Request)} if the authGroup has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/auth-groups")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.GROUP_CREATION + "\")")
    public ResponseEntity<AuthGroupDTO> createAuthGroup(@Valid @RequestBody AuthGroupDTO authGroupDTO) throws URISyntaxException {
        log.info("REST request to save AuthGroup : {}", authGroupDTO);
        if (authGroupDTO.getId() != null) {
            throw new BadRequestAlertException("A new authGroup cannot already have an ID", ENTITY_NAME, "idexists");
        }
        AuthGroupDTO result = authGroupService.save(authGroupDTO);
        return ResponseEntity.created(new URI("/api/auth-groups/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /auth-groups} : Updates an existing authGroup.
     *
     * @param authGroupDTO the authGroupDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated authGroupDTO,
     * or with status {@code 400 (Bad Request)} if the authGroupDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the authGroupDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/auth-groups")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.GROUP_MODIFICATION + "\")")
    public ResponseEntity<AuthGroupDTO> updateAuthGroup(@Valid @RequestBody AuthGroupDTO authGroupDTO) throws URISyntaxException {
        log.info("REST request to update AuthGroup : {}", authGroupDTO);
        if (authGroupDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        AuthGroupDTO result = authGroupService.update(authGroupDTO).orElseGet(() -> null);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, authGroupDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /auth-groups} : get all the authGroups.
     *

     * @param pageable the pagination information.
     * @param filter the filter which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of authGroups in body.
     */
    @GetMapping("/auth-groups")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.GROUP_LIST + "\")")
    public ResponseEntity<List<AuthGroupDTO>> getAllAuthGroups(
        @Parameter(description = "Filter", required = false) @Valid @ParameterObject AuthGroupFilter filter,
        @Parameter(description = "Pageable", required = true) @ParameterObject Pageable pageable
    ) {
        log.info("REST request to get AuthGroups by filter: {}", filter);
        Page<AuthGroupDTO> page = authGroupService.findAll(filter, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /auth-groups/:id} : get the "id" authGroup.
     *
     * @param id the id of the authGroupDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the authGroupDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/auth-groups/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.GROUP_DETAIL + "\")")
    public ResponseEntity<AuthGroupDTO> getAuthGroup(@PathVariable Long id) {
        log.info("REST request to get AuthGroup : {}", id);
        Optional<AuthGroupDTO> authGroupDTO = authGroupService.findOne(id);
        return ResponseUtil.wrapOrNotFound(authGroupDTO);
    }

    /**
     * {@code GET  /auth-groups/detail/:id} : get the "id" authGroup.
     *
     * @param id the id of the authGroupDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the authGroupDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/auth-groups/detail/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.GROUP_DETAIL + "\")")
    public ResponseEntity<AuthGroupDTO> getAuthGroupWithFunction(@PathVariable Long id) {
        log.info("REST request to get AuthGroup : {}", id);
        Optional<AuthGroupDTO> authGroupDTO = authGroupService.findOneWithEagerRelationships(id);
        return ResponseUtil.wrapOrNotFound(authGroupDTO);
    }

    /**
     * {@code DELETE  /auth-groups/:id} : delete the "id" authGroup.
     *
     * @param id the id of the authGroupDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/auth-groups/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.GROUP_DELETION + "\")")
    public ResponseEntity<Void> deleteAuthGroup(@PathVariable Long id) {
        log.info("REST request to delete AuthGroup : {}", id);
        authGroupService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    @PostMapping("/auth-groups/{idGroup}/associa-funzioni")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.GROUP_MODIFICATION_FUNCTION_ASSOCIATION + "\")")
    public ResponseEntity<Void> aggiungiAssociazioneFunzione(@PathVariable Long idGroup, @Valid @RequestBody AuthFunctionDTO[] funzioni)
        throws URISyntaxException {
        log.info("REST request to save function da associare al gruppo");

        if (funzioni == null || funzioni.length == 0) throw new BadRequestAlertException(
            "Selezionare almeno una funzione da associare al gruppo",
            "authGroup.associaFunzioni.funzioniDaAssociare.notEmpty"
        );

        //verifico che la funzione selezionata non sia già stato associata

        List<AuthFunctionDTO> funzioniDaAssociare = Arrays.asList(funzioni);

        List<Long> ufficiAssociati = authFunctionService.listAllFunctionSelected(idGroup);

        Boolean check = ufficiAssociati
            .stream()
            .anyMatch(funzioniDaAssociare.stream().map(AuthFunctionDTO::getId).collect(Collectors.toSet())::contains);

        if (check) {
            throw new BadRequestAlertException("Funzione già associata al gruppo", "authGroup.associaFunzioni.funzioneAlreadyAssociated");
        }

        // Associa Funzioni
        authGroupService.associaFunzioni(idGroup, funzioni);

        return ResponseEntity.created(new URI("api/auth-groups/" + idGroup.toString() + "/associa-funzioni"))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, "authGroup.message.associaFunzioni", ""))
            .body(null);
    }

    @GetMapping("/auth-groups/{idGroup}/rimuovi-funzione/{funzioneId}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.GROUP_MODIFICATION_FUNCTION_DISASSOCIATION + "\")")
    public ResponseEntity<Void> rimuoviAssociazioneFunzione(@PathVariable Long idGroup, @PathVariable Long funzioneId)
        throws URISyntaxException {
        log.info("Rimuovi associazione funzione (id) {} al gruppo (id) {}", funzioneId, idGroup);

        // Dissocia funzione
        authGroupService.rimuoviAssociazioneFunzione(idGroup, funzioneId);

        return ResponseEntity.created(new URI("api/auth-groups/" + idGroup.toString() + "/rimuovi-funzione/" + funzioneId.toString()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, "authGroup.message.dissociaFunzione", ""))
            .body(null);
    }

    @PutMapping("/auth-groups/aggiorna-livello-visibilita")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.GROUP_CREATION + "\", \"" + AuthoritiesConstants.GROUP_MODIFICATION + "\")")
    public ResponseEntity<Void> aggiornaLivelloVisibilitaGruppi(@Valid @RequestBody AuthGroupUpdateRequestBean[] authGroups)
        throws URISyntaxException {
        log.info("REST request to update groups visibility level");

        authGroupService.aggiornaLivelloVisibilitaGruppi(authGroups);

        return ResponseEntity.created(new URI("api/auth-groups/aggiorna-livello-visibilita"))
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, "authGroup.message.aggiornamentoLivelloVisibilita", ""))
            .body(null);
    }
}
