package com.nexigroup.pagopa.cruscotto.sert.repository;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthUser;
import com.nexigroup.pagopa.cruscotto.sert.domain.enumeration.AuthenticationType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link AuthUser} entity.
 */
@Repository
public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {
    Optional<AuthUser> findOneByActivationKey(String activationKey);

    List<AuthUser> findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(Instant dateTime);

    Optional<AuthUser> findOneByResetKey(String resetKey);

    @Query("Select u from AuthUser u where lower(u.email) = lower(:email) and u.deleted = false and u.authenticationType = :type")
    Optional<AuthUser> findOneByEmailIgnoreCaseAndNotDeleted(
        @Param("email") String email,
        @Param("type") AuthenticationType authenticationType
    );

    @Query("Select u from AuthUser u where u.email = :email and u.deleted = false")
    Optional<AuthUser> findOneByEmailAndNotDeleted(@Param("email") String email);

    @Query("Select u from AuthUser u where u.login = :login and u.deleted = false and u.authenticationType = :type")
    Optional<AuthUser> findOneByLoginAndNotDeleted(@Param("login") String login, @Param("type") AuthenticationType authenticationType);

    @Query("Select u from AuthUser u JOIN FETCH u.group where u.login = :login")
    Optional<AuthUser> findOneByLogin(@Param("login") String login);

    @Query("Select u from AuthUser u JOIN FETCH u.group where u.login = :login and u.authenticationType = :type")
    Optional<AuthUser> findOneByLogin(@Param("login") String login, @Param("type") AuthenticationType authenticationType);

    @Query("Select u from AuthUser u where lower(u.login) = lower(:login) and u.deleted = false and u.authenticationType = :type")
    Optional<AuthUser> findOneByLoginIgnoreCaseAndNotDeleted(
        @Param("login") String login,
        @Param("type") AuthenticationType authenticationType
    );

    String USERS_BY_LOGIN_CACHE = "usersByLogin";

    String USERS_BY_EMAIL_CACHE = "usersByEmail";

    @EntityGraph(attributePaths = "authorities")
    @Cacheable(cacheNames = USERS_BY_LOGIN_CACHE)
    Optional<AuthUser> findOneWithAuthoritiesByLogin(String login);

    Page<AuthUser> findAllByLoginNot(Pageable pageable, String login);

    @Query("Select u from AuthUser u where u.sub = :sub and u.deleted = false")
    Optional<AuthUser> findOneBySub(@Param("sub") String sub);
}
