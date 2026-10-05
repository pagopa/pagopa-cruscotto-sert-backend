package com.nexigroup.pagopa.cruscotto.sert.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthGroup;
import com.nexigroup.pagopa.cruscotto.sert.domain.AuthPermission;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthGroupRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthPermissionRepository;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class GrantAuthoritiesResolutionTest {

    private final javax.cache.CacheManager cacheManager = mock(javax.cache.CacheManager.class);
    private final AuthGroupRepository groupRepository = mock(AuthGroupRepository.class);
    private final AuthPermissionRepository permissionRepository = mock(AuthPermissionRepository.class);
    private final GrantAuthoritiesLoad service = new GrantAuthoritiesLoad(cacheManager, groupRepository, permissionRepository);

    @Test
    void resolvesGroupPermissionsAndCachesThem() {
        javax.cache.Cache<Object, Object> cache = mock(javax.cache.Cache.class);
        when(cacheManager.getCache(AuthoritiesConstants.PRINCIPAL)).thenReturn(cache);
        when(cache.get("subject_123_form")).thenReturn(null);
        AuthGroup group = new AuthGroup();
        group.setId(5L);
        when(groupRepository.findOneByObjectId(List.of("group-a"))).thenReturn(List.of(group));
        AuthPermission permission = new AuthPermission().modulo("SERT").nome("SEARCH");
        when(permissionRepository.findAllPermissionsByGroupIds(List.of(5L))).thenReturn(List.of(permission));

        Collection<GrantedAuthority> authorities = service.load(Map.of("groups", List.of("group-a")), "subject", "123", "form");

        assertThat(authorities).containsExactly(new SimpleGrantedAuthority("SERT.SEARCH"));
        verify(cache).put(eq("subject_123_form"), any());
    }

    @Test
    void rejectsMissingAndUnknownGroups() {
        when(cacheManager.getCache(AuthoritiesConstants.PRINCIPAL)).thenReturn(null);
        assertThatThrownBy(() -> service.load(Map.of(), "subject", "123", "form"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Group is not defined");

        when(groupRepository.findOneByObjectId(List.of("unknown"))).thenReturn(List.of());
        assertThatThrownBy(() -> service.load(Map.of("roles", List.of("unknown")), "subject", "123", "form"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Group not found");
    }

    @Test
    void returnsAuthoritiesFromCacheWhenPresent() {
        javax.cache.Cache<Object, Object> cache = mock(javax.cache.Cache.class);
        when(cacheManager.getCache(AuthoritiesConstants.PRINCIPAL)).thenReturn(cache);
        when(cache.get("subject_123_form")).thenReturn(List.of(new SimpleGrantedAuthority("SERT.CACHED")));

        assertThat(service.load(Map.of("groups", List.of("group-a")), "subject", "123", "form"))
            .containsExactly(new SimpleGrantedAuthority("SERT.CACHED"));
    }
}