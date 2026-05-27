package com.nexigroup.pagopa.cruscotto.sert.security;

import com.nexigroup.pagopa.cruscotto.sert.domain.enumeration.AuthenticationType;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Utility class for Spring Security.
 */
public final class SecurityUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityUtils.class);


    public static final String AUTHORITIES_KEY = "roles";

    private SecurityUtils() {}

    /**
     * Get the login of the current user.
     *
     * @return the login of the current user.
     */
    public static Optional<String> getCurrentUserLogin() {
        return Optional.ofNullable(getPreferredUserName());
    }

    private static String extractPrincipal(Authentication authentication) {
        //        if (authentication == null) {
        //            return null;
        //        } else if (authentication.getPrincipal() instanceof UserDetails) {
        //            UserDetails springSecurityUser = (UserDetails) authentication.getPrincipal();
        //            LOGGER.info("Spring Security User : {}", springSecurityUser.getUsername());
        //            return springSecurityUser.getUsername();
        //        } else if (authentication instanceof JwtAuthenticationToken) {
        //            Object principal = ((JwtAuthenticationToken) authentication).getPrincipal();
        //
        //            LOGGER.info("Jwt Principal");
        //            ((JwtAuthenticationToken)authentication).getToken().getClaims().forEach((key, value) -> LOGGER.info("{} - {}", key, value));
        //
        //            String preferred_username = ((String) ((JwtAuthenticationToken)authentication).getToken().getClaims().get("preferred_username"));
        //            LOGGER.info("preferred_username : {}", preferred_username);
        //            return preferred_username.concat("_azure");
        //        } else if (authentication.getPrincipal() instanceof DefaultOidcUser) {
        //            Map<String, Object> attributes = ((DefaultOidcUser) authentication.getPrincipal()).getAttributes();
        //
        //            String authorizedClientRegistrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        //            LOGGER.info("authorizedClientRegistrationId : {}", authorizedClientRegistrationId);
        //            attributes.forEach((key, value) -> LOGGER.info("{} - {}", key, value));
        //            if (attributes.containsKey("preferred_username")) {
        //                return (String) attributes.get("preferred_username")+"_"+authorizedClientRegistrationId;
        //            }
        //        } else if (authentication.getPrincipal() instanceof String) {
        //            return (String) authentication.getPrincipal();
        //        }
        //        return null;
        if (authentication == null) {
            return null;
        } else if (authentication.getPrincipal() instanceof UserDetails springSecurityUser) {
            return springSecurityUser.getUsername();
        } else if (authentication.getPrincipal() instanceof Jwt jwt) {
            LOGGER.info("JWT found {}", jwt.getSubject());
            return jwt.getSubject();
        } else if (authentication.getPrincipal() instanceof String s) {
            return s;
        }
        return null;
    }

    public static Optional<AuthenticationType> getAuthenticationTypeUserLogin() {
            return Optional.of(AuthenticationType.ENTRA_ID);
    }

    private static AuthenticationType extractPrincipalAuthenticationType(Authentication authentication) {
        if (authentication == null) {
            return null;
        } else if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails springSecurityUser = (UserDetails) authentication.getPrincipal();
            return AuthenticationType.FORM_LOGIN;
        } else if (authentication instanceof JwtAuthenticationToken) {
            return AuthenticationType.FORM_LOGIN;
        }
        return null;
    }

    /**
     * Get the JWT of the current user.
     *
     * @return the JWT of the current user.
     */
    public static Optional<String> getCurrentUserJWT() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(securityContext.getAuthentication())
            .filter(authentication -> authentication.getCredentials() instanceof String)
            .map(authentication -> (String) authentication.getCredentials());
    }

    /**
     * Check if a user is authenticated.
     *
     * @return true if the user is authenticated, false otherwise.
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && getAuthorities(authentication).noneMatch(AuthoritiesConstants.ANONYMOUS::equals);
    }

    /**
     * If the current user has a specific authority (security role).
     * <p>
     * The name of this method comes from the {@code isUserInRole()} method in the Servlet API.
     *
     * @param authority the authority to check.
     * @return true if the current user has the authority, false otherwise.
     */
    public static boolean isCurrentUserInRole(String authority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && getAuthorities(authentication).anyMatch(authority::equals);
    }

    private static Stream<String> getAuthorities(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication instanceof JwtAuthenticationToken
            ? extractAuthorityFromClaims(((JwtAuthenticationToken) authentication).getToken().getClaims())
            : authentication.getAuthorities();
        return authorities.stream().map(GrantedAuthority::getAuthority);
    }

    public static List<GrantedAuthority> extractAuthorityFromClaims(Map<String, Object> claims) {
        return mapRolesToGrantedAuthorities(getRolesFromClaims(claims));
    }

    @SuppressWarnings("unchecked")
    private static Collection<String> getRolesFromClaims(Map<String, Object> claims) {
        return (Collection<String>) claims.getOrDefault("groups", claims.getOrDefault(AUTHORITIES_KEY, new ArrayList<>()));
    }

    private static List<GrantedAuthority> mapRolesToGrantedAuthorities(Collection<String> roles) {
        return roles
            .stream()
            //            .filter(role -> role.startsWith("ROLE_"))
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }

    /**
     * Checks if the current user has any of the authorities.
     *
     * @param authorities the authorities to check.
     * @return true if the current user has any of the authorities, false otherwise.
     */
    public static boolean hasCurrentUserAnyOfAuthorities(String... authorities) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (
            authentication != null && getAuthorities(authentication).anyMatch(authority -> Arrays.asList(authorities).contains(authority))
        );
    }

    /**
     * Checks if the current user has none of the authorities.
     *
     * @param authorities the authorities to check.
     * @return true if the current user has none of the authorities, false otherwise.
     */
    public static boolean hasCurrentUserNoneOfAuthorities(String... authorities) {
        return !hasCurrentUserAnyOfAuthorities(authorities);
    }

    /**
     * Checks if the current user has a specific authority.
     *
     * @param authority the authority to check.
     * @return true if the current user has the authority, false otherwise.
     */
    public static boolean hasCurrentUserThisAuthority(String authority) {
        return hasCurrentUserAnyOfAuthorities(authority);
    }

    public static List<String> getRolesFromJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        if (authentication == null) {
            return null;
        }
        return jwt.getClaimAsStringList("roles");
    }

    public static String getNameJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        if (authentication == null) {
            return null;
        }
        return jwt.getClaim("name");
    }

    public static String getSubJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getClaim("sub");
    }

    public static String getPreferredUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getClaim("preferred_username");
    }

}
