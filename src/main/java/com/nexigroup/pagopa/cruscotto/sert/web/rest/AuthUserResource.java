package com.nexigroup.pagopa.cruscotto.sert.web.rest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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

import com.nexigroup.pagopa.cruscotto.sert.config.Constants;
import com.nexigroup.pagopa.cruscotto.sert.domain.AuthUser;
import com.nexigroup.pagopa.cruscotto.sert.domain.enumeration.AuthenticationType;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthUserRepository;
import com.nexigroup.pagopa.cruscotto.sert.security.AuthoritiesConstants;
import com.nexigroup.pagopa.cruscotto.sert.security.SecurityUtils;
import com.nexigroup.pagopa.cruscotto.sert.service.impl.AuthUserService;
import com.nexigroup.pagopa.cruscotto.sert.service.impl.MailService;
import com.nexigroup.pagopa.cruscotto.sert.service.bean.AuthUserCreateRequestBean;
import com.nexigroup.pagopa.cruscotto.sert.service.bean.AuthUserUpdateRequestBean;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthUserDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.util.PasswordValidator;
import com.nexigroup.pagopa.cruscotto.sert.service.validation.UserResourcePermissionValidator;
import com.nexigroup.pagopa.cruscotto.sert.web.rest.errors.BadRequestAlertException;
import com.nexigroup.pagopa.cruscotto.sert.web.rest.errors.EmailAlreadyUsedException;
import com.nexigroup.pagopa.cruscotto.sert.web.rest.errors.LoginAlreadyUsedException;

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
 * REST controller for managing authUsers.
 * <p>
 * This class accesses the {@link AuthUser} entity, and needs to fetch its collection of authorities.
 * <p>
 * For a normal use-case, it would be better to have an eager relationship between User and Authority,
 * and send everything to the client side: there would be no View Model and DTO, a lot less code, and an outer-join
 * which would be good for performance.
 * <p>
 * We use a View Model and a DTO for 3 reasons:
 * <ul>
 * <li>We want to keep a lazy association between the authUser and the authorities, because people will
 * quite often do relationships with the authUser, and we don't want them to get the authorities all
 * the time for nothing (for performance reasons). This is the #1 goal: we should not impact our authUsers'
 * application because of this use-case.</li>
 * <li> Not having an outer join causes n+1 requests to the database. This is not a real issue as
 * we have by default a second-level cache. This means on the first HTTP call we do the n+1 requests,
 * but then all authorities come from the cache, so in fact it's much better than doing an outer join
 * (which will get lots of data from the database, for each HTTP call).</li>
 * <li> As this manages authUsers, for security reasons, we'd rather have a DTO layer.</li>
 * </ul>
 * <p>
 * Another option would be to have a specific JPA entity graph to handle this case.
 */
@RestController
@RequestMapping("/api")
public class AuthUserResource {

    private static final String AUTH_USER_NOT_FOUND = "AuthUser not found";

    private static final String L_UTENTE_SELEZIONATO_COINCIDE_CON_L_UTENTE_LOGGATO = "L'utente selezionato coincide con l'utente loggato";

    private static final String L_UTENTE_COINCIDE_CON_L_UTENTE_LOGGATO_OPERAZIONE_NON_AMMESSA =
        "L'utente {} coincide con l'utente loggato {}: operazione non ammessa";

    private static final String STATO_UTENTE_INCOMPATIBILE_CON_L_OPERAZIONE_RICHIESTA =
        "Stato utente incompatibile con l'operazione richiesta";

    private static final String CURRENT_USER_LOGIN_NOT_FOUND = "Current user login not found";

    private static final String RESET_PASSWORD_NOT_PERMITTED = "Non è possibile inviare il reset password per l'utente selezionato";

    private final Logger log = LoggerFactory.getLogger(AuthUserResource.class);

    private static final String USER_IS_THE_SAME_LOGGED_USER_ERROR = "userManagement.isTheSameLoggedUser";

    private static final String STATO_USER_INCOMPATIBILE_ERROR = "userManagement.statoNotValid";

    private static final String PERMISSION_DENIED = "userManagement.permissionDenied";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AuthUserService authUserService;

    private final AuthUserRepository authUserRepository;

    private final MailService mailService;

    private final PasswordValidator passwordValidator;

    private final UserResourcePermissionValidator userResourcePermissionValidator;
    

    public AuthUserResource(
        AuthUserService authUserService,
        AuthUserRepository authUserRepository,
        MailService mailService,
        PasswordValidator passwordValidator,
        UserResourcePermissionValidator userResourcePermissionValidator
    ) {
        this.authUserService = authUserService;
        this.authUserRepository = authUserRepository;
        this.mailService = mailService;
        this.passwordValidator = passwordValidator;
        this.userResourcePermissionValidator = userResourcePermissionValidator;
    }

    /**
     * {@code POST  /auth-users}  : Creates a new authUser.
     * <p>
     * Creates a new authUser if the login and email are not already used, and sends an
     * mail with an activation link.
     * The authUser needs to be activated on creation.
     *
     * @param authUserCreateRequestBean the authUser to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new authUser, or with status {@code 400 (Bad Request)} if the login or email is already in use.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     * @throws BadRequestAlertException {@code 400 (Bad Request)} if the login or email is already in use.
     */
    @PostMapping("/auth-users")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.USER_CREATION + "\")")
    public ResponseEntity<AuthUser> createAuthUser(@Valid @RequestBody AuthUserCreateRequestBean authUserCreateRequestBean)
        throws URISyntaxException {
        log.info("REST request to save AuthUser : {}", authUserCreateRequestBean);

        if (
            authUserRepository
                .findOneByLoginIgnoreCaseAndNotDeleted(authUserCreateRequestBean.getLogin(), AuthenticationType.FORM_LOGIN)
                .isPresent()
        ) {
            throw new LoginAlreadyUsedException();
        } else if (
            authUserRepository
                .findOneByEmailIgnoreCaseAndNotDeleted(authUserCreateRequestBean.getEmail(), AuthenticationType.FORM_LOGIN)
                .isPresent()
        ) {
            throw new EmailAlreadyUsedException();
        } else {
            passwordValidator.check(
                authUserCreateRequestBean.getLogin(),
                authUserCreateRequestBean.getPassword(),
                authUserCreateRequestBean.getFirstName(),
                authUserCreateRequestBean.getLastName()
            );

            AuthUser newAuthUser = authUserService.createUser(authUserCreateRequestBean);

            return ResponseEntity.created(new URI("/api/auth-users/" + newAuthUser.getLogin()))
                .headers(HeaderUtil.createAlert(applicationName, "userManagement.created", newAuthUser.getLogin()))
                .body(newAuthUser);
        }
    }

    /**
     * {@code PUT /auth-users} : Updates an existing AuthUser.
     *
     * @param authUserUpdateRequestBean the authUser to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated authUser.
     * @throws EmailAlreadyUsedException {@code 400 (Bad Request)} if the email is already in use.
     * @throws LoginAlreadyUsedException {@code 400 (Bad Request)} if the login is already in use.
     */
    @PutMapping("/auth-users")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.USER_MODIFICATION + "\")")
    public ResponseEntity<AuthUserDTO> updateAuthUser(@Valid @RequestBody AuthUserUpdateRequestBean authUserUpdateRequestBean) {
        log.info("REST request to update AuthUser : {}", authUserUpdateRequestBean);

        String loginUtenteLoggato = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new RuntimeException(CURRENT_USER_LOGIN_NOT_FOUND));

        // ----------------------------------------------------------------------------------------------
        // BEGIN Check user permission on resource
        if (
            userResourcePermissionValidator.userCanAccessIdUserResource(loginUtenteLoggato, authUserUpdateRequestBean.getId())
        ) return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        // END Check user permission on resource
        // ----------------------------------------------------------------------------------------------

        Optional<AuthUser> existingAuthUserOptional = authUserRepository.findOneByEmailIgnoreCaseAndNotDeleted(
            authUserUpdateRequestBean.getEmail(),
            AuthenticationType.FORM_LOGIN
        );

        AuthUser existingAuthUser = existingAuthUserOptional.orElseThrow(() -> {
            log.error("Stato utente {} incompatibile con l'operazione richiesta", authUserUpdateRequestBean.getEmail());
            return new BadRequestAlertException(STATO_UTENTE_INCOMPATIBILE_CON_L_OPERAZIONE_RICHIESTA, STATO_USER_INCOMPATIBILE_ERROR);
        });

        if (loginUtenteLoggato.equals(existingAuthUser.getLogin())) {
            log.error(L_UTENTE_COINCIDE_CON_L_UTENTE_LOGGATO_OPERAZIONE_NON_AMMESSA, existingAuthUser.getLogin(), loginUtenteLoggato);
            throw new BadRequestAlertException(L_UTENTE_SELEZIONATO_COINCIDE_CON_L_UTENTE_LOGGATO, USER_IS_THE_SAME_LOGGED_USER_ERROR);
        }

        if (!existingAuthUser.getId().equals(authUserUpdateRequestBean.getId())) {
            throw new EmailAlreadyUsedException();
        }

        if (StringUtils.isNotBlank(authUserUpdateRequestBean.getPassword())) passwordValidator.check(
            existingAuthUser.getLogin(),
            authUserUpdateRequestBean.getPassword(),
            authUserUpdateRequestBean.getFirstName(),
            authUserUpdateRequestBean.getLastName()
        );

        Optional<AuthUserDTO> updatedAuthUser = authUserService.updateUser(authUserUpdateRequestBean);

        return ResponseUtil.wrapOrNotFound(
            updatedAuthUser,
            HeaderUtil.createAlert(applicationName, "userManagement.updated", existingAuthUser.getLogin())
        );
    }

    /**
     * {@code PUT /auth-users/changeState/{id}} : Updates an existing AuthUser.
     *
     * @param id the authUser to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated authUser.
     * @throws EmailAlreadyUsedException {@code 400 (Bad Request)} if the email is already in use.
     * @throws LoginAlreadyUsedException {@code 400 (Bad Request)} if the login is already in use.
     */
    @PutMapping("/auth-users/changeState/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.USER_MODIFICATION + "\")")
    public ResponseEntity<Void> changeState(@PathVariable Long id) {
        log.info("REST request to update state AuthUser : {}", id);

        String loginUtenteLoggato = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new RuntimeException(CURRENT_USER_LOGIN_NOT_FOUND));

        // ----------------------------------------------------------------------------------------------
        // BEGIN Check user permission on resource
        if (userResourcePermissionValidator.userCanAccessIdUserResource(loginUtenteLoggato, id)) return new ResponseEntity<>(
            HttpStatus.FORBIDDEN
        );
        // END Check user permission on resource
        // ----------------------------------------------------------------------------------------------

        AuthUserDTO existingAuthUser = authUserService.getUserById(id).orElseThrow(() -> new RuntimeException(AUTH_USER_NOT_FOUND));

        if (existingAuthUser.getAuthenticationType().compareTo(AuthenticationType.OAUHT2) == 0) throw new BadRequestAlertException(
            RESET_PASSWORD_NOT_PERMITTED,
            PERMISSION_DENIED
        );

        if (existingAuthUser.isDeleted()) {
            log.error("Utente con id {} CANCELLATO: operazione non ammessa", id);
            throw new BadRequestAlertException(STATO_UTENTE_INCOMPATIBILE_CON_L_OPERAZIONE_RICHIESTA, STATO_USER_INCOMPATIBILE_ERROR);
        }

        if (loginUtenteLoggato.equals(existingAuthUser.getLogin())) {
            log.error(L_UTENTE_COINCIDE_CON_L_UTENTE_LOGGATO_OPERAZIONE_NON_AMMESSA, existingAuthUser.getLogin(), loginUtenteLoggato);
            throw new BadRequestAlertException(L_UTENTE_SELEZIONATO_COINCIDE_CON_L_UTENTE_LOGGATO, USER_IS_THE_SAME_LOGGED_USER_ERROR);
        }

        authUserService.changeState(id, !existingAuthUser.isActivated());

        return ResponseEntity.ok()
            .headers(HeaderUtil.createAlert(applicationName, "userManagement.updated", existingAuthUser.getLogin()))
            .body(null);
    }

    /**
     * {@code GET /users/reset} : reset user password.
     *
     * @param login the mail of the user to reset password.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the "login" user, or with status {@code 404 (Not Found)}.
     */
    @PostMapping("/auth-users/reset")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.USER_MODIFICATION + "\")")
    public ResponseEntity<Void> resetPasswordUser(@RequestBody String login) {
        log.info("Reset password User : {}", login);

        //        Optional<AuthenticationType> authenticationType = SecurityUtils.getAuthenticationTypeUserLogin();
        //
        //        if(authenticationType.isPresent() && authenticationType.get().compareTo(AuthenticationType.OAUHT2) == 0)
        //            throw new BadRequestAlertException("Non è possibile inviare il reset password per l'utente selezionato", PERMISSION_DENIED);

        String loginUtenteLoggato = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new RuntimeException(CURRENT_USER_LOGIN_NOT_FOUND));

        // ----------------------------------------------------------------------------------------------
        // BEGIN Check user permission on resource
        if (userResourcePermissionValidator.userCanAccessUserResource(loginUtenteLoggato, login)) return new ResponseEntity<>(
            HttpStatus.FORBIDDEN
        );
        // END Check user permission on resource
        // ----------------------------------------------------------------------------------------------

        if (loginUtenteLoggato.equals(login)) {
            log.error(L_UTENTE_COINCIDE_CON_L_UTENTE_LOGGATO_OPERAZIONE_NON_AMMESSA, login, loginUtenteLoggato);
            throw new BadRequestAlertException(L_UTENTE_SELEZIONATO_COINCIDE_CON_L_UTENTE_LOGGATO, USER_IS_THE_SAME_LOGGED_USER_ERROR);
        }

        mailService.sendPasswordResetMail(
            authUserService
                .requestPasswordResetByLogin(login)
                .orElseThrow(() ->
                    new BadRequestAlertException(STATO_UTENTE_INCOMPATIBILE_CON_L_OPERAZIONE_RICHIESTA, STATO_USER_INCOMPATIBILE_ERROR)
                )
        );

        return ResponseEntity.noContent().headers(HeaderUtil.createAlert(applicationName, "userManagement.reset", login)).build();
    }

    /**
     * {@code GET /auth-users} : get all authUsers.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body all authUsers.
     */
    @GetMapping("/auth-users")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.USER_LIST + "\")")
    public ResponseEntity<List<AuthUserDTO>> getAllAuthUsers(
        @Parameter(description = "Pageable", required = true) @ParameterObject Pageable pageable
    ) {
        final Page<AuthUserDTO> page = authUserService.getAllManagedUsers(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * {@code GET /auth-users/:login} : get the "login" authUser.
     *
     * @param login the login of the authUser to find.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the "login" authUser, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/auth-users/{login:" + Constants.LOGIN_REGEX + "}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.USER_DETAIL + "\")")
    public ResponseEntity<AuthUserDTO> getAuthUser(@PathVariable String login) {
        log.info("REST request to get AuthUser : {}", login);

        String loginUtenteLoggato = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new RuntimeException(CURRENT_USER_LOGIN_NOT_FOUND));

        // ----------------------------------------------------------------------------------------------
        // BEGIN Check user permission on resource
        if (userResourcePermissionValidator.userCanAccessUserResource(loginUtenteLoggato, login)) return new ResponseEntity<>(
            HttpStatus.FORBIDDEN
        );
        // END Check user permission on resource
        // ----------------------------------------------------------------------------------------------

        return ResponseUtil.wrapOrNotFound(authUserService.userDetailByLogin(login));
    }

    /**
     * {@code DELETE /auth-users/:login} : delete the "login" AuthUser.
     *
     * @param login the login of the authUser to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/auth-users/{login:" + Constants.LOGIN_REGEX + "}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.USER_DELETION + "\")")
    public ResponseEntity<Void> deleteAuthUser(@PathVariable String login) {
        log.info("REST request to delete AuthUser: {}", login);

        String loginUtenteLoggato = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new RuntimeException(CURRENT_USER_LOGIN_NOT_FOUND));

        // ----------------------------------------------------------------------------------------------
        // BEGIN Check user permission on resource
        if (userResourcePermissionValidator.userCanAccessUserResource(loginUtenteLoggato, login)) return new ResponseEntity<>(
            HttpStatus.FORBIDDEN
        );
        // END Check user permission on resource
        // ----------------------------------------------------------------------------------------------

        if (loginUtenteLoggato.equals(login)) {
            log.error(L_UTENTE_COINCIDE_CON_L_UTENTE_LOGGATO_OPERAZIONE_NON_AMMESSA, login, loginUtenteLoggato);
            throw new BadRequestAlertException(L_UTENTE_SELEZIONATO_COINCIDE_CON_L_UTENTE_LOGGATO, USER_IS_THE_SAME_LOGGED_USER_ERROR);
        }

        if (!authUserService.deleteUser(login)) throw new BadRequestAlertException(
            STATO_UTENTE_INCOMPATIBILE_CON_L_OPERAZIONE_RICHIESTA,
            STATO_USER_INCOMPATIBILE_ERROR
        );

        return ResponseEntity.noContent().headers(HeaderUtil.createAlert(applicationName, "userManagement.deleted", login)).build();
    }

    @GetMapping("/auth-users/{id}/view")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.USER_DETAIL + "\")")
    public ResponseEntity<AuthUserDTO> getAuthUserById(@PathVariable Long id) {
        log.info("REST request to get AuthUser by id: {}", id);

        String loginUtenteLoggato = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new RuntimeException(CURRENT_USER_LOGIN_NOT_FOUND));

        // ----------------------------------------------------------------------------------------------
        // BEGIN Check user permission on resource
        if (userResourcePermissionValidator.userCanAccessIdUserResource(loginUtenteLoggato, id)) return new ResponseEntity<>(
            HttpStatus.FORBIDDEN
        );
        // END Check user permission on resource
        // ----------------------------------------------------------------------------------------------

        return ResponseUtil.wrapOrNotFound(authUserService.getUserById(id));
    }
}
