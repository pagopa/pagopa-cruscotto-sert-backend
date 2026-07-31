package com.nexigroup.pagopa.cruscotto.sert.web.rest.auth;

import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexigroup.pagopa.cruscotto.sert.config.ApplicationProperties;
import com.nexigroup.pagopa.cruscotto.sert.domain.AuthUser;
import com.nexigroup.pagopa.cruscotto.sert.domain.enumeration.AuthenticationType;
import com.nexigroup.pagopa.cruscotto.sert.domain.enumeration.Language;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthUserRepository;
import com.nexigroup.pagopa.cruscotto.sert.security.AuthoritiesConstants;
import com.nexigroup.pagopa.cruscotto.sert.security.SecurityUtils;
import com.nexigroup.pagopa.cruscotto.sert.service.impl.AuthUserService;
import com.nexigroup.pagopa.cruscotto.sert.service.impl.MailService;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthUserAccountDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthUserDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.util.CookieTranslateUtil;
import com.nexigroup.pagopa.cruscotto.sert.service.util.PasswordValidator;
import com.nexigroup.pagopa.cruscotto.sert.web.rest.errors.BadRequestAlertException;
import com.nexigroup.pagopa.cruscotto.sert.web.rest.errors.EmailAlreadyUsedException;
import com.nexigroup.pagopa.cruscotto.sert.web.rest.errors.InvalidPasswordException;
import com.nexigroup.pagopa.cruscotto.sert.web.rest.vm.KeyAndPasswordVM;

import java.security.Principal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/api")
public class AccountResource {

    private static final String USER_COULD_NOT_BE_FOUND = "User could not be found";

    private static final String CURRENT_USER_LOGIN_NOT_FOUND = "Current user login not found";

    private static class AccountResourceException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private AccountResourceException(String message) {
            super(message);
        }
    }

    private final Logger log = LoggerFactory.getLogger(AccountResource.class);

    private final AuthUserService authUserService;

    private final AuthUserRepository authUserRepository;

    private final PasswordValidator passwordValidator;

    private final MailService mailService;

    private final ApplicationProperties properties;


    public AccountResource(
        AuthUserService authUserService,
        AuthUserRepository authUserRepository,
        PasswordValidator passwordValidator,
        MailService mailService,
        ApplicationProperties properties
    ) {
        this.authUserService = authUserService;
        this.authUserRepository = authUserRepository;
        this.passwordValidator = passwordValidator;
        this.mailService = mailService;
        this.properties = properties;
    }

    /**
     * {@code GET  /account} : get the current user.
     *
     * @param principal the current user; resolves to {@code null} if not authenticated.
     * @return the current user.
     * @throws AccountResourceException {@code 500 (Internal Server Error)} if the user couldn't be returned.
     */
    @GetMapping("/account")
    @SuppressWarnings("unchecked")
    public ResponseEntity<AuthUserDTO> getAccount(
        HttpServletRequest request,
        HttpServletResponse response,
        Principal principal
    ) {

        if (!(principal instanceof JwtAuthenticationToken jwtAuth)) {
            throw new AccountResourceException("JWT principal not found");
        }

        Jwt jwt = jwtAuth.getToken();


        List<String> groupIds =
            jwt.getClaimAsStringList("roles")
                .stream()
                .toList();

        List<String> roles =
            jwt.getClaimAsStringList("roles");

        Set<String> dto =
            authUserService.getUserWithAuthoritiesFromJwt(
                groupIds,
                roles
            );

        return ResponseEntity.ok(authUserService.buildFromJwt(jwt,dto));
    }

    /**
     * {@code POST  /account} : update the current authUser information.
     *
     * @param authUserDTO the current authUser information.
     * @throws EmailAlreadyUsedException {@code 400 (Bad Request)} if the email is already used.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if the authUser login wasn't found.
     */
    @PostMapping("/account")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ACCOUNT_MODIFICATION + "\")")
    public void saveAccount(@Valid @RequestBody AuthUserAccountDTO authUserDTO) {
        AuthenticationType authenticationType = SecurityUtils.getAuthenticationTypeUserLogin()
            .orElseThrow(() -> new RuntimeException("Authentication Type not found"));

        if (authenticationType.compareTo(AuthenticationType.OAUHT2) == 0) throw new AccountResourceException(
            "Current user could not have permission to change settings account"
        );

        String userLogin = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new AccountResourceException(CURRENT_USER_LOGIN_NOT_FOUND));
        AuthUser existingUser = authUserRepository
            .findOneByEmailIgnoreCaseAndNotDeleted(authUserDTO.getEmail(), AuthenticationType.FORM_LOGIN)
            .orElse(null);

        if (existingUser != null && (!existingUser.getLogin().equalsIgnoreCase(userLogin))) {
            throw new EmailAlreadyUsedException();
        }
        AuthUser authUser = authUserRepository
            .findOneByLoginAndNotDeleted(userLogin, AuthenticationType.FORM_LOGIN)
            .orElseThrow(() -> new AccountResourceException(USER_COULD_NOT_BE_FOUND));

        authUserService.updateUser(
            authUserDTO.getFirstName(),
            authUserDTO.getLastName(),
            authUserDTO.getEmail(),
            authUserDTO.getLangKey(),
            authUser.getGroup().getId(),
            AuthenticationType.FORM_LOGIN
        );
    }


    /**
     * {@code GET  /language/:language} : send language to save in cookie
     *
     * @param language the HTTP request.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @GetMapping("/language/{language}")
    public ResponseEntity<Void> setLanguageCookie(
        HttpServletRequest request,
        HttpServletResponse response,
        @PathVariable("language") String language
    ) {
        log.debug("REST request to save language in cookie");

        Language lg = EnumUtils.getEnum(Language.class, language);

        if (lg == null) {
            return ResponseEntity.noContent().build();
        }

        CookieTranslateUtil.create(request, response, language);

        return ResponseEntity.noContent().build();
    }

    /**
     * {@code POST   /account/reset-password/init} : Send an email to reset the password of the authUser.
     *
     * @param mail the mail of the authUser.
     */
    @PostMapping(path = "/account/reset-password/init")
    public void requestPasswordReset(@RequestBody String mail) {
        Optional<AuthUser> user = authUserService.requestPasswordResetByMail(mail, AuthenticationType.FORM_LOGIN);
        if (user.isPresent()) {
            mailService.sendPasswordResetMail(user.orElseThrow(() -> new AccountResourceException(USER_COULD_NOT_BE_FOUND)));
        } else {
            // Pretend the request has been successful to prevent checking which emails really exist
            // but log that an invalid attempt has been made
            log.warn("Password reset requested for non existing mail");
        }
    }

    /**
     * {@code POST   /account/reset-password/finish} : Finish to reset the password of the authUser.
     *
     * @param keyAndPassword the generated key and the new password.
     * @throws InvalidPasswordException {@code 400 (Bad Request)} if the password is incorrect.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if the password could not be reset.
     */
    @PostMapping(path = "/account/reset-password/finish")
    public void finishPasswordReset(@RequestBody KeyAndPasswordVM keyAndPassword) {
        Optional<AuthUser> authUserOptional = authUserRepository.findOneByResetKey(keyAndPassword.getKey());

        AuthUser authUser = authUserOptional.orElseThrow(() -> new AccountResourceException("No user was found for this reset key"));

        //validity token
        if (
            properties.getPassword().getHoursKeyResetPasswordExpired() != null &&
            properties.getPassword().getHoursKeyResetPasswordExpired() > 0
        ) {
            ZonedDateTime dateStartReset = ZonedDateTime.now().minusHours(properties.getPassword().getHoursKeyResetPasswordExpired());

            if (authUser.getResetDate().isBefore(dateStartReset.toInstant())) throw new BadRequestAlertException(
                "Reset key is not valid",
                "Account",
                "keyResetNotValid"
            );
        }

        passwordValidator.check(authUser.getLogin(), keyAndPassword.getNewPassword(), authUser.getFirstName(), authUser.getLastName());

        authUserService.completePasswordReset(keyAndPassword.getNewPassword(), keyAndPassword.getKey());
    }
}
