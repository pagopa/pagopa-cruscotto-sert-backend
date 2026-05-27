package com.nexigroup.pagopa.cruscotto.sert.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthGroup;
import com.nexigroup.pagopa.cruscotto.sert.domain.AuthPermission;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthGroupRepository;
import com.nexigroup.pagopa.cruscotto.sert.repository.AuthPermissionRepository;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.cache.Cache;
import javax.cache.CacheManager;

@Component
public class GrantAuthoritiesLoad {

    private final CacheManager cacheManager;

    private final AuthGroupRepository authGroupRepository;

    private final AuthPermissionRepository authPermissionRepository;

    public GrantAuthoritiesLoad(
        CacheManager cacheManager,
        AuthGroupRepository authGroupRepository,
        AuthPermissionRepository authPermissionRepository
    ) {
        this.cacheManager = cacheManager;
        this.authGroupRepository = authGroupRepository;
        this.authPermissionRepository = authPermissionRepository;
    }

    public Collection<GrantedAuthority> load(Map<String, Object> claims, String subject, String iat, String typeLogin) {
        Collection<GrantedAuthority> grantedAuthorities = SecurityUtils.extractAuthorityFromClaims(claims);
        Cache<Object, Object> cache = cacheManager.getCache(AuthoritiesConstants.PRINCIPAL);

        String key = subject.concat("_").concat(iat).concat("_").concat(typeLogin);

        Set<GrantedAuthority> grantedAuthoritiesConverted = new HashSet<>();

        if (cache != null && cache.get(key) != null) {
            grantedAuthoritiesConverted.addAll((Collection)cache.get(key));
        }

        if (grantedAuthoritiesConverted.isEmpty()) {
            //carico in cache le Authorities dell'utente
            if (grantedAuthorities.isEmpty()){
                throw new IllegalArgumentException("Group is not defined");
            }

            List<String> groupAuthority = grantedAuthorities
                .stream().map(grantedAuthority -> grantedAuthority.getAuthority()).toList();


            List<AuthGroup> group = authGroupRepository.findOneByObjectId(groupAuthority);
            if (group.isEmpty()){
                throw  new IllegalArgumentException("Group not found");
            }

            List<AuthPermission> functions = authPermissionRepository.findAllPermissionsByGroupIds(group.stream().map(AuthGroup::getId).toList());

            grantedAuthoritiesConverted = functions
                .stream()
                .map(function -> new SimpleGrantedAuthority(function.getModulo() + "." + function.getNome()))
                .collect(Collectors.toSet());

            cache.put(key, grantedAuthoritiesConverted);
    }

        return grantedAuthoritiesConverted;
    }
}
