package com.nexigroup.pagopa.cruscotto.sert.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthGroup;
import com.nexigroup.pagopa.cruscotto.sert.domain.AuthPermission;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthGroupRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthPermissionRepository;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.cache.Cache;
import javax.cache.CacheManager;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

class GrantAuthoritiesLoadTest {

    private final CacheManager cacheManager = mock(CacheManager.class);
    private final Cache<Object, Object> cache = mock(Cache.class);
    private final AuthGroupRepository groupRepository = mock(AuthGroupRepository.class);
    private final AuthPermissionRepository permissionRepository = mock(AuthPermissionRepository.class);
    private final GrantAuthoritiesLoad loader = new GrantAuthoritiesLoad(cacheManager, groupRepository, permissionRepository);
    private final Map<String, Object> claims = Map.of("groups", List.of("object-group"));

    @Test
    void resolvesPermissionsAndCachesThemOnCacheMiss() {
        AuthGroup group = new AuthGroup();
        group.setId(42L);
        AuthPermission permission = new AuthPermission().modulo("SERT").nome("SEARCH");
        when(cacheManager.getCache(AuthoritiesConstants.PRINCIPAL)).thenReturn(cache);
        when(cache.get("subject_100_form")).thenReturn(null);
        when(groupRepository.findOneByObjectId(List.of("object-group"))).thenReturn(List.of(group));
        when(permissionRepository.findAllPermissionsByGroupIds(List.of(42L))).thenReturn(List.of(permission));

        Collection<GrantedAuthority> authorities = loader.load(claims, "subject", "100", "form");

        assertThat(authorities).extracting(GrantedAuthority::getAuthority).containsExactly("SERT.SEARCH");
        verify(cache).put("subject_100_form", authorities);
    }

    @Test
    void returnsCachedAuthoritiesWithoutRepositoryCalls() {
        Collection<GrantedAuthority> cached = List.of(() -> "SERT.CACHED");
        when(cacheManager.getCache(AuthoritiesConstants.PRINCIPAL)).thenReturn(cache);
        when(cache.get(anyString())).thenReturn(cached);

        assertThat(loader.load(claims, "subject", "100", "form")).containsExactlyElementsOf(cached);
        verify(groupRepository, never()).findOneByObjectId(anyList());
        verify(permissionRepository, never()).findAllPermissionsByGroupIds(anyList());
    }

    @Test
    void rejectsMissingGroupClaimsAndUnknownGroups() {
        when(cacheManager.getCache(AuthoritiesConstants.PRINCIPAL)).thenReturn(null);
        assertThatThrownBy(() -> loader.load(Map.of(), "subject", "100", "form"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Group is not defined");

        when(groupRepository.findOneByObjectId(List.of("object-group"))).thenReturn(List.of());
        assertThatThrownBy(() -> loader.load(claims, "subject", "100", "form"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Group not found");
    }
}