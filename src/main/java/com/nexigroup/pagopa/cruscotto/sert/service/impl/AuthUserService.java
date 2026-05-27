package com.nexigroup.pagopa.cruscotto.sert.service.impl;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthGroup;
import com.nexigroup.pagopa.cruscotto.sert.service.exception.GenericServiceException;
import com.nexigroup.pagopa.cruscotto.sert.service.exception.InvalidPasswordException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexigroup.pagopa.cruscotto.sert.config.ApplicationProperties;
import com.nexigroup.pagopa.cruscotto.sert.config.Constants;
import com.nexigroup.pagopa.cruscotto.sert.domain.AuthPermission;
import com.nexigroup.pagopa.cruscotto.sert.domain.AuthUser;
import com.nexigroup.pagopa.cruscotto.sert.domain.AuthUserHistory;
import com.nexigroup.pagopa.cruscotto.sert.domain.QAuthFunction;
import com.nexigroup.pagopa.cruscotto.sert.domain.QAuthGroup;
import com.nexigroup.pagopa.cruscotto.sert.domain.QAuthUser;
import com.nexigroup.pagopa.cruscotto.sert.domain.enumeration.AuthenticationType;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthGroupRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthPermissionRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthUserHistoryRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthUserRepository;
import com.nexigroup.pagopa.cruscotto.sert.security.SecurityUtils;
import com.nexigroup.pagopa.cruscotto.sert.service.bean.AuthUserCreateRequestBean;
import com.nexigroup.pagopa.cruscotto.sert.service.bean.AuthUserUpdateRequestBean;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthFunctionDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthUserDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.qdsl.QdslUtility;
import com.nexigroup.pagopa.cruscotto.sert.service.qdsl.QueryBuilder;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import tech.jhipster.security.RandomUtil;

/**
 * Service class for managing authUsers.
 */
@Service
@Transactional
public class AuthUserService {

    private static final String USER_MANAGEMENT = "userManagement";

    private final Logger log = LoggerFactory.getLogger(AuthUserService.class);

    private final AuthUserRepository authUserRepository;

    private final AuthUserHistoryRepository authUserHistoryRepository;

    private final AuthGroupRepository authGroupRepository;

    private final AuthPermissionRepository authPermissionRepository;

    private final ApplicationProperties properties;

    private final QueryBuilder queryBuilder;

    private final CacheManager cacheManager;

    private final PasswordEncoder passwordEncoder;

    public AuthUserService(
        AuthUserRepository authUserRepository,
        AuthUserHistoryRepository authUserHistoryRepository,
        ApplicationProperties properties,
        QueryBuilder queryBuilder,
        AuthGroupRepository authGroupRepository,
        AuthPermissionRepository authPermissionRepository,
        CacheManager cacheManager,
        PasswordEncoder passwordEncoder
    ) {
        this.authUserRepository = authUserRepository;
        this.authUserHistoryRepository = authUserHistoryRepository;
        this.authGroupRepository = authGroupRepository;
        this.properties = properties;
        this.queryBuilder = queryBuilder;
        this.authPermissionRepository = authPermissionRepository;
        this.cacheManager = cacheManager;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<AuthUser> activateRegistration(String key) {
        log.debug("Activating authUser for activation key {}", key);
        return authUserRepository
            .findOneByActivationKey(key)
            .map(authUser -> {
                // activate given authUser for the registration key.
                authUser.setActivated(true);
                authUser.setActivationKey(null);
                log.debug("Activated authUser: {}", authUser);
                return authUser;
            });
    }

    public Optional<AuthUser> requestPasswordResetByMail(String mail, AuthenticationType authenticationType) {
        return authUserRepository
            .findOneByEmailIgnoreCaseAndNotDeleted(mail, authenticationType)
            .filter(AuthUser::getActivated)
            .map(authUser -> {
                authUser.setResetKey(RandomUtil.generateResetKey());
                authUser.setResetDate(Instant.now());
                return authUser;
            });
    }

    public Optional<AuthUser> requestPasswordResetByLogin(String login) {
        return authUserRepository
            .findOneByLoginAndNotDeleted(login, AuthenticationType.FORM_LOGIN)
            .filter(AuthUser::getActivated)
            .map(authUser -> {
                authUser.setResetKey(RandomUtil.generateResetKey());
                authUser.setFailedLoginAttempts(0);
                authUser.setBlocked(Boolean.FALSE);
                authUser.setResetDate(Instant.now());
                return authUser;
            });
    }

    public AuthUser createUser(AuthUserCreateRequestBean authUserCreateRequestBean) {
        AuthUser authUser = new AuthUser();
        authUser.setLogin(authUserCreateRequestBean.getLogin());
        authUser.setFirstName(authUserCreateRequestBean.getFirstName());
        authUser.setLastName(authUserCreateRequestBean.getLastName());
        authUser.setEmail(authUserCreateRequestBean.getEmail().toLowerCase());

        if (authUserCreateRequestBean.getLangKey() == null) {
            authUser.setLangKey(Constants.DEFAULT_LANGUAGE); // default language
        } else {
            authUser.setLangKey(authUserCreateRequestBean.getLangKey());
        }

        String encryptedPassword = passwordEncoder.encode(authUserCreateRequestBean.getPassword());
        authUser.setPassword(encryptedPassword);
        authUser.setResetKey(null);
        authUser.setResetDate(null);
        authUser.setActivated(true);
        authUser.setFailedLoginAttempts(0);
        authUser.setLastPasswordChangeDate(ZonedDateTime.now());
        authUser.setPasswordExpiredDay(0);
        authUser.setAuthenticationType(AuthenticationType.FORM_LOGIN);

        authGroupRepository.findById(authUserCreateRequestBean.getGroupId()).ifPresent(authUser::setGroup);

        //se utente loggato ha un solo cliente associato viene associato automaticamente all'utente creato -------------------
        String loginUtenteLoggato = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new RuntimeException("Current user login not found"));

        AuthenticationType authenticationType = SecurityUtils.getAuthenticationTypeUserLogin()
            .orElseThrow(() -> new RuntimeException("Authentication Type not found"));

        AuthUser utenteLoggato = authUserRepository
            .findOneByLoginAndNotDeleted(loginUtenteLoggato, authenticationType)
            .orElseThrow(() -> new RuntimeException("Current logged user with login " + loginUtenteLoggato + " not found"));

        authUserRepository.save(authUser);

        AuthUserHistory authUserHistory = new AuthUserHistory();
        authUserHistory.setPassword(encryptedPassword);
        authUserHistory.setAuthUser(authUser);
        authUserHistory.setDataModifica(ZonedDateTime.now());
        authUserHistoryRepository.save(authUserHistory);

        log.debug("Created Information for authUser: {}", authUser);

        return authUser;
    }

    /**
     * Update basic information (first name, last name, email, language) for the current authUser.
     *
     * @param firstName first name of authUser.
     * @param lastName  last name of authUser.
     * @param email     email id of authUser.
     * @param langKey   language key.
     * @param groupId   group id.
     */
    public void updateUser(
        String firstName,
        String lastName,
        String email,
        String langKey,
        Long groupId,
        AuthenticationType authenticationType
    ) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap(login -> authUserRepository.findOneByLoginAndNotDeleted(login, authenticationType))
            .ifPresent(authUser -> {
                authUser.setFirstName(firstName);
                authUser.setLastName(lastName);
                authUser.setEmail(email.toLowerCase());
                authUser.setLangKey(langKey);
                authGroupRepository.findById(groupId).ifPresent(authUser::setGroup);
                authUserRepository.save(authUser);
                log.debug("Changed Information for authUser: {}", authUser);
            });
    }

    /**
     * Update all information for a specific authUser, and return the modified authUser.
     *
     * @param authUserUpdateRequestBean authUser to update.
     * @return updated authUser.
     */
    public Optional<AuthUserDTO> updateUser(AuthUserUpdateRequestBean authUserUpdateRequestBean) {
        return Optional.of(authUserRepository.findById(authUserUpdateRequestBean.getId()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(authUser -> {
                if (StringUtils.isNotBlank(authUserUpdateRequestBean.getPassword())) {
                    String encryptedPassword = passwordEncoder.encode(authUserUpdateRequestBean.getPassword());
                    authUser.setPassword(encryptedPassword);
                    authUser.setLastPasswordChangeDate(ZonedDateTime.now());
                    authUser.setFailedLoginAttempts(0);
                    authUser.setPasswordExpiredDay(0);
                    authUser.setBlocked(Boolean.FALSE);

                    AuthUserHistory authUserHistory = new AuthUserHistory();
                    authUserHistory.setPassword(encryptedPassword);
                    authUserHistory.setAuthUser(authUser);
                    authUserHistory.setDataModifica(ZonedDateTime.now());
                    authUserHistoryRepository.save(authUserHistory);
                }

                authUser.setFirstName(authUserUpdateRequestBean.getFirstName());
                authUser.setLastName(authUserUpdateRequestBean.getLastName());
                authUser.setEmail(authUserUpdateRequestBean.getEmail().toLowerCase());
                authUser.setLangKey(authUserUpdateRequestBean.getLangKey());
                authGroupRepository.findById(authUserUpdateRequestBean.getGroupId()).ifPresent(authUser::setGroup);
                log.debug("Changed Information for authUser: {}", authUser);
                return authUser;
            })
            .map(AuthUserDTO::new);
    }

    public void changeState(Long authUserId, Boolean activated) {
        queryBuilder
            .createQueryFactory()
            .update(QAuthUser.authUser)
            .set(QAuthUser.authUser.activated, activated)
            .where(QAuthUser.authUser.id.eq(authUserId))
            .execute();
    }

    public boolean deleteUser(String login) {
        Optional<AuthUser> authUserOptional = authUserRepository.findOneByLoginAndNotDeleted(login, AuthenticationType.FORM_LOGIN);

        AuthUser authUser = authUserOptional.orElse(null);

        if (authUser != null) {
            authUser.setDeleted(Boolean.TRUE);
            authUser.setDeletedDate(ZonedDateTime.now());
            authUser.setActivated(false);

            authUserRepository.save(authUser);

            log.debug("Deleted authUser: {}", authUser);

            return true;
        }

        return false;
    }

    @Transactional(readOnly = true)
    public Page<AuthUserDTO> getAllManagedUsers(Pageable pageable) {
        String loginUtente = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new RuntimeException("Current user login not found"));

        AuthenticationType authenticationType = SecurityUtils.getAuthenticationTypeUserLogin()
            .orElseThrow(() -> new RuntimeException("Authentication Type not found"));

        AuthUser loggedUser = authUserRepository
            .findOneByLoginAndNotDeleted(loginUtente, authenticationType)
            .orElseThrow(() ->
                new RuntimeException("Current logged user with login " + loginUtente + " and type " + authenticationType + " not found")
            );

        boolean result;

        QAuthUser qAuthUser = QAuthUser.authUser;

        QAuthGroup qAuthGroup = QAuthGroup.authGroup;
        JPQLQuery<Long> jpql = queryBuilder
            .createQuery()
            .from(qAuthGroup)
            .leftJoin(qAuthGroup.authUsers, qAuthUser)
            .select(qAuthGroup.id)
            .where(qAuthUser.login.eq(loginUtente).and(qAuthGroup.nome.eq(properties.getAuthGroup().getSuperAdmin())));

        result = jpql.fetchCount() > 0;

        JPQLQuery<AuthUser> jpqlAuthUser = queryBuilder
            .<AuthUser>createQuery()
            .from(QAuthUser.authUser)
            .leftJoin(QAuthUser.authUser.group, QAuthGroup.authGroup);

        if (!result) {
            BooleanBuilder builder = new BooleanBuilder();
            BooleanBuilder builder2 = new BooleanBuilder();
            BooleanBuilder builder3 = new BooleanBuilder();

            builder.and(QAuthUser.authUser.group.livelloVisibilita.goe(loggedUser.getGroup().getLivelloVisibilita()));

            builder2.and(QAuthUser.authUser.deleted.eq(Boolean.FALSE)).and(qAuthGroup.nome.notEqualsIgnoreCase(properties.getAuthGroup().getSuperAdmin()));
            builder3.and(QAuthUser.authUser.createdBy.eq(loginUtente));

            builder.and(builder2.or(builder3));

            jpqlAuthUser.where(builder);
        }

        long size = jpqlAuthUser.fetchCount();

        JPQLQuery<AuthUserDTO> jpqlAuthUserDto = jpqlAuthUser
            .select(
                Projections.fields(
                    AuthUserDTO.class,
                    QAuthUser.authUser.id.as("id"),
                    QAuthUser.authUser.login.as("login"),
                    QAuthUser.authUser.firstName.as("firstName"),
                    QAuthUser.authUser.lastName.as("lastName"), //
                    QAuthUser.authUser.email.as("email"),
                    QAuthUser.authUser.imageUrl.as("imageUrl"),
                    QAuthUser.authUser.activated.as("activated"),
                    QAuthUser.authUser.langKey.as("langKey"),
                    QAuthUser.authUser.group.nome.as("groupName"),
                    QAuthUser.authUser.group.id.as("groupId"),
                    QAuthUser.authUser.createdBy.as("createdBy"),
                    QAuthUser.authUser.createdDate.as("createdDate"),
                    QAuthUser.authUser.lastModifiedBy.as("lastModifiedBy"),
                    QAuthUser.authUser.lastModifiedDate.as("lastModifiedDate"),
                    QAuthUser.authUser.blocked.as("blocked"),
                    QAuthUser.authUser.deletedDate.as("deletedDate"),
                    QAuthUser.authUser.deleted.as("deleted"),
                    QAuthUser.authUser.authenticationType.as("authenticationType")
                )
            )
            .distinct();

        jpqlAuthUserDto.offset(pageable.getOffset());
        jpqlAuthUserDto.limit(pageable.getPageSize());

        pageable.getSortOr(Sort.by(Sort.Direction.DESC, "id"));

        pageable
            .getSort()
            .forEach(order -> {
                jpqlAuthUserDto.orderBy(
                    new OrderSpecifier<>(
                        order.isAscending() ? Order.ASC : Order.DESC,
                        Expressions.stringPath(order.getProperty()),
                        QdslUtility.toQueryDslNullHandling(order.getNullHandling())
                    )
                );
            });

        List<AuthUserDTO> list = jpqlAuthUserDto.fetch();

        return new PageImpl<>(list, pageable, size);
    }

    @Transactional(readOnly = true)
    public Optional<AuthUserDTO> getAuthUserWithAuthoritiesByLogin(String login) {
        Optional<AuthUser> optionalAuthUser = authUserRepository.findOneByLoginAndNotDeleted(login, AuthenticationType.FORM_LOGIN);

        return optionalAuthUser.map(authUser -> {
            List<AuthPermission> permissions = authPermissionRepository.findAllPermissionsByGroupId(authUser.getGroup().getId());

            Set<String> grantedAuthorities = permissions
                .stream()
                .map(permission -> permission.getModulo() + "." + permission.getNome())
                .collect(Collectors.toSet());

            return new AuthUserDTO(authUser, grantedAuthorities);
        });
    }

    @Transactional(readOnly = true)
    public Optional<AuthUserDTO> getUserByLogin(String login) {
        Optional<AuthUser> optionalAuthUser = authUserRepository.findOneByLoginAndNotDeleted(login, AuthenticationType.FORM_LOGIN);
        return optionalAuthUser.map(AuthUserDTO::new);
    }

    @Transactional(readOnly = true)
    public Optional<AuthUser> findUserByLogin(String login) {
        return authUserRepository.findOneByLoginAndNotDeleted(login, AuthenticationType.FORM_LOGIN);
    }

    @Transactional(readOnly = true)
    public Optional<AuthUserDTO> getUserWithAuthorities(AuthenticationType authenticationType) {
        Optional<AuthUser> optionalAuthUser = SecurityUtils.getCurrentUserLogin()
            .flatMap(login -> authUserRepository.findOneByLoginAndNotDeleted(login, authenticationType));
        List<String> roles = SecurityUtils.getRolesFromJwt();
        return optionalAuthUser.map(authUser -> {
            JPQLQuery<AuthFunctionDTO> jpql = queryBuilder
                .createQuery()
                .from(QAuthGroup.authGroup)
                .join(QAuthGroup.authGroup.authFunctions, QAuthFunction.authFunction)
                .select(
                    Projections.fields(
                        AuthFunctionDTO.class,
                        QAuthFunction.authFunction.nome.as("nome"),
                        QAuthFunction.authFunction.modulo.as("modulo")
                    )
                )
                .where(QAuthGroup.authGroup.objectId.in(roles))
                .distinct();

            List<AuthFunctionDTO> functions = jpql.fetch();

            Set<String> grantedAuthorities = functions
                .stream()
                .map(function -> new String(function.getModulo() + "." + function.getNome()))
                .collect(Collectors.toSet());

            return new AuthUserDTO(authUser, grantedAuthorities);
        });
    }

    public AuthUserDTO buildFromJwt(
        Jwt jwt,
        Set<String> authoritiesFromDb
    ) {

        AuthUser user = syncUserFromJwt(jwt);

        AuthUserDTO dto = new AuthUserDTO();

        dto.setLogin(user.getLogin());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());

        dto.setId(user.getId());

        dto.setActivated(true);
        dto.setLangKey("it");

        dto.setCreatedBy(user.getCreatedBy());
        dto.setLastModifiedBy(user.getLastModifiedBy());

        Instant issuedAt = jwt.getIssuedAt();
        dto.setCreatedDate(issuedAt);
        dto.setLastModifiedDate(issuedAt);

        dto.setAuthenticationType(user.getAuthenticationType());

        dto.setBlocked(false);
        dto.setDeleted(false);


        List<String> roles = jwt.getClaimAsStringList("roles");

        if (roles != null && !roles.isEmpty()) {
            List<AuthGroup> groups = authGroupRepository.findOneByObjectId(roles);
            dto.setGroupName( groups.stream()
                .map(AuthGroup::getNome)
                .collect(Collectors.joining(",")));
        }

        dto.setAuthorities(authoritiesFromDb);

        return dto;
    }

    @Transactional
    public AuthUser syncUserFromJwt(Jwt jwt) {

        String sub = jwt.getClaimAsString("sub");

        Optional<AuthUser> userOpt = authUserRepository.findOneBySub(sub);

        Instant now = Instant.now();
        AuthUser user = new AuthUser();
        if (userOpt.isPresent()) {
            user = userOpt.orElseThrow();
            user.setLastLoginAt(now);
        }

        user.setSub(sub);
        user.setOid(jwt.getClaimAsString("oid"));

        String username = jwt.getClaimAsString("preferred_username");

        user.setLogin(username);
        user.setPassword("***************");
        user.setEmail(username);
        String oid = jwt.getClaimAsString("oid");
        if (oid != null) {
            user.setOid(oid);
        }

        String fullName = jwt.getClaimAsString("name");

        if (fullName != null) {
            String cleaned = fullName.replaceAll("\\(.*\\)", "").trim();
            String[] parts = cleaned.split(" ", 2);

            user.setFirstName(parts[0]);

            if (parts.length > 1) {
                user.setLastName(parts[1]);
            }
        }


        user.setSource("AZURE_AD");
        user.setLastLoginAt(now);

        user.setActivated(true);
        user.setBlocked(false);
        user.setDeleted(false);

        List<String> roles = jwt.getClaimAsStringList("roles");

        if (roles != null && !roles.isEmpty()) {
            List<AuthGroup> groups = authGroupRepository.findOneByObjectId(roles);
            user.setGroup(groups.stream()
                .min(Comparator.comparing(AuthGroup::getLivelloVisibilita))
                .orElse(null));
        }

        user.setAuthenticationType(AuthenticationType.ENTRA_ID);

        return authUserRepository.save(user);
    }


    @Transactional(readOnly = true)
    public Set<String>  getUserWithAuthoritiesFromJwt(
        List<String> groupIds,
        List<String> tokenRoles
    ) {

        if (groupIds == null || groupIds.isEmpty()) {
            return  Collections.emptySet();
        }

        JPQLQuery<AuthFunctionDTO> jpql = queryBuilder
            .createQuery()
            .from(QAuthGroup.authGroup)
            .join(QAuthGroup.authGroup.authFunctions, QAuthFunction.authFunction)
            .select(
                Projections.fields(
                    AuthFunctionDTO.class,
                    QAuthFunction.authFunction.nome.as("nome"),
                    QAuthFunction.authFunction.modulo.as("modulo")
                )
            )
            .where(QAuthGroup.authGroup.objectId.in(groupIds))
            .distinct();

        List<AuthFunctionDTO> functions = jpql.fetch();

        Set<String> grantedAuthorities = functions.stream()
            .map(f -> f.getModulo() + "." + f.getNome())
            .collect(Collectors.toSet());

        return  grantedAuthorities;
    }

    public void increaseFailedLoginAttempts(String username) {
        log.debug("Incremento il flag dei tentativi di accesso");

        AuthUser authUser = authUserRepository.findOneByLoginAndNotDeleted(username, AuthenticationType.FORM_LOGIN).orElse(null);

        if (authUser != null) {
            if (
                properties.getPassword().getFailedLoginAttempts() != null &&
                    authUser.getFailedLoginAttempts() != null &&
                    authUser.getFailedLoginAttempts() >= properties.getPassword().getFailedLoginAttempts()
            ) {
                authUser.setBlocked(Boolean.TRUE);
            }

            authUser.setLastModifiedBy(Constants.SYSTEM);
            authUser.setFailedLoginAttempts(authUser.getFailedLoginAttempts() == null ? 1 : (authUser.getFailedLoginAttempts() + 1));

            authUserRepository.save(authUser);
        }
    }

    public void resetFailedLoginAttempts(String username) {
        log.debug("Reset flag dei tentativi di accesso");

        AuthUser authUser = authUserRepository.findOneByLoginAndNotDeleted(username, AuthenticationType.FORM_LOGIN).orElse(null);

        if (authUser != null) {
            authUser.setLastModifiedBy(Constants.SYSTEM);
            authUser.setFailedLoginAttempts(0);

            authUserRepository.save(authUser);
        }
    }

    @Transactional(readOnly = true)
    public Optional<AuthUserDTO> getUserById(Long idAuthUser) {
        Optional<AuthUser> optionalAuthUser = authUserRepository.findById(idAuthUser);
        return optionalAuthUser.map(AuthUserDTO::new);
    }

    @Transactional(readOnly = true)
    public Optional<AuthUserDTO> userDetailByLogin(String login) {
        Optional<AuthUser> optionalAuthUser = authUserRepository.findOneByLogin(login);
        return optionalAuthUser.map(AuthUserDTO::new);
    }

    public boolean getAbilitazioneUtenteLoggatoManageOtherUser(String userLoginUtenteLoggato, Long userId, String userLogin) {
        log.info(
            "Service - controllo abilitazione utente {} alla gestione dell'utente {}",
            userLoginUtenteLoggato,
            userId != null ? userId : userLogin
        );

        boolean result;

        QAuthUser qAuthUser = QAuthUser.authUser;

        QAuthGroup qAuthGroup = QAuthGroup.authGroup;
        JPQLQuery<Long> jpql = queryBuilder
            .createQuery()
            .from(qAuthGroup)
            .leftJoin(qAuthGroup.authUsers, qAuthUser)
            .select(qAuthGroup.id)
            .where(qAuthUser.login.eq(userLoginUtenteLoggato).and(qAuthGroup.nome.eq(properties.getAuthGroup().getSuperAdmin())));

        result = jpql.fetchCount() > 0;

        if (!result) {
            BooleanBuilder builder = new BooleanBuilder();
            //  BooleanBuilder builder3 = new BooleanBuilder();

            if (userId != null) builder.and(qAuthUser.id.eq(userId));
            else if (StringUtils.isNotBlank(userLogin)) builder.and(qAuthUser.login.eq(userLogin));
            else return false;

            //utenti appartenenti a gruppi con livello di visibilità superiore o uguale al livello di visibilità del gruppo dell'utente loggato
            JPQLQuery<Integer> livelloVisibilitaGruppoUtenteLoggato = JPAExpressions.select(QAuthGroup.authGroup.livelloVisibilita)
                .from(QAuthGroup.authGroup)
                .innerJoin(QAuthGroup.authGroup.authUsers, QAuthUser.authUser)
                .where(QAuthUser.authUser.login.eq(userLoginUtenteLoggato));

            builder.and(qAuthUser.group.livelloVisibilita.goe(livelloVisibilitaGruppoUtenteLoggato));

            //  builder3.and(qAuthUser.createdBy.eq(userLoginUtenteLoggato));

            //    builder.and(builder3);

            jpql = queryBuilder
                .createQuery()
                .from(qAuthUser)
                .leftJoin(qAuthUser.group, QAuthGroup.authGroup)
                .select(qAuthUser.id)
                .where(builder);

            result = Optional.ofNullable(jpql.fetchFirst()).isPresent();
        }

        return result;
    }

    @Transactional
    public AuthUser syncUserWithIdP(Map<String, Object> details, AuthUser user) {
        // save account in to sync users between IdP and local database
        AuthUser existingUser = authUserRepository.findOneByLogin(user.getLogin()).orElse(null);
        if (existingUser != null) {
            if (existingUser.getAuthenticationType().compareTo(AuthenticationType.FORM_LOGIN) == 0) throw new GenericServiceException(
                String.format("User already exist"),
                USER_MANAGEMENT,
                "userManagement.userAlreadyExist"
            );

            // if IdP sends last updated information, use it to determine if an update should happen
            if (details.get("updated_at") != null) {
                Instant dbModifiedDate = existingUser.getLastModifiedDate();
                Instant idpModifiedDate = (Instant) details.get("updated_at");
                if (idpModifiedDate.isAfter(dbModifiedDate)) {
                    log.debug("Updating user '{}' in local database", user.getLogin());
                    updateUser(
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getLangKey(),
                        user.getGroup().getId(),
                        user.getAuthenticationType()
                    );
                }
                // no last updated info, blindly update
            } else {
                log.debug("Updating user '{}' in local database", user.getLogin());
                updateUser(
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getLangKey(),
                    user.getGroup().getId(),
                    user.getAuthenticationType()
                );
            }
        } else {
            log.debug("Saving user '{}' in local database", user.getLogin());
            authUserRepository.save(user);
            this.clearUserCaches(user);
        }

        return user;
    }

    private void clearUserCaches(AuthUser user) {
        Objects.requireNonNull(cacheManager.getCache(AuthUserRepository.USERS_BY_LOGIN_CACHE)).evict(user.getLogin());
        if (user.getEmail() != null) {
            Objects.requireNonNull(cacheManager.getCache(AuthUserRepository.USERS_BY_EMAIL_CACHE)).evict(user.getEmail());
        }
    }

    public void changePassword(String currentClearTextPassword, String newPassword) {
        SecurityUtils.getCurrentUserLogin()
            .flatMap(login -> authUserRepository.findOneByLoginAndNotDeleted(login, AuthenticationType.FORM_LOGIN))
            .ifPresent(authUser -> {
                String currentEncryptedPassword = authUser.getPassword();
                if (!passwordEncoder.matches(currentClearTextPassword, currentEncryptedPassword)) {
                    throw new InvalidPasswordException();
                }

                String encryptedPassword = passwordEncoder.encode(newPassword);
                authUser.setPassword(encryptedPassword);
                authUser.setFailedLoginAttempts(0);
                authUser.setLastPasswordChangeDate(ZonedDateTime.now());

                if (properties.getPassword().getDayPasswordExpired() != null && properties.getPassword().getDayPasswordExpired() > 0) {
                    authUser.setPasswordExpiredDay(properties.getPassword().getDayPasswordExpired());
                }

                authUserRepository.save(authUser);

                AuthUserHistory authUserHistory = new AuthUserHistory();
                authUserHistory.setPassword(encryptedPassword);
                authUserHistory.setAuthUser(authUser);
                authUserHistory.setDataModifica(ZonedDateTime.now());
                authUserHistoryRepository.save(authUserHistory);

                log.debug("Changed password for authUser: {}", authUser);
            });
    }

    public Optional<AuthUser> completePasswordReset(String newPassword, String key) {
        log.debug("Reset authUser password for reset key {}", key);
        return authUserRepository
            .findOneByResetKey(key)
            .filter(authUser -> authUser.getResetDate().isAfter(Instant.now().minusSeconds(86400)))
            .map(authUser -> {
                String newPasswordEncoded = passwordEncoder.encode(newPassword);
                authUser.setPassword(newPasswordEncoded);
                authUser.setResetKey(null);
                authUser.setResetDate(null);
                authUser.setFailedLoginAttempts(0);
                authUser.setLastPasswordChangeDate(ZonedDateTime.now());
                authUser.setBlocked(Boolean.FALSE);

                if (properties.getPassword().getDayPasswordExpired() != null && properties.getPassword().getDayPasswordExpired() > 0) {
                    authUser.setPasswordExpiredDay(properties.getPassword().getDayPasswordExpired());
                }

                AuthUserHistory authUserHistory = new AuthUserHistory();
                authUserHistory.setPassword(newPasswordEncoded);
                authUserHistory.setAuthUser(authUser);
                authUserHistory.setDataModifica(ZonedDateTime.now());
                authUserHistoryRepository.save(authUserHistory);
                return authUser;
            });
    }
}
