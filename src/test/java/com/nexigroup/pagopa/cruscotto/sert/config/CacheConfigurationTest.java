package com.nexigroup.pagopa.cruscotto.sert.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthUser;
import java.util.HashMap;
import java.util.Map;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import org.hibernate.cache.jcache.ConfigSettings;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import tech.jhipster.config.JHipsterProperties;

class CacheConfigurationTest {

    private final CacheConfiguration configuration = new CacheConfiguration(new JHipsterProperties());

    @Test
    void createsAllApplicationCachesWhenTheyDoNotExist() {
        CacheManager cacheManager = mock(CacheManager.class);
        when(cacheManager.getCache(anyString())).thenReturn(null);

        JCacheManagerCustomizer customizer = configuration.cacheManagerCustomizer();
        customizer.customize(cacheManager);

        verify(cacheManager, times(5)).createCache(anyString(), any(Configuration.class));
    }

    @Test
    void clearsAnExistingCacheInsteadOfRecreatingIt() {
        Cache<Object, Object> existingCache = mock(Cache.class);
        CacheManager cacheManager = mock(CacheManager.class);
        when(cacheManager.getCache(anyString())).thenReturn(null);
        when(cacheManager.getCache(AuthUser.class.getName())).thenReturn(existingCache);

        configuration.cacheManagerCustomizer().customize(cacheManager);

        verify(existingCache).clear();
        verify(cacheManager, times(4)).createCache(anyString(), any(Configuration.class));
    }

    @Test
    void registersCacheManagerWithHibernate() {
        CacheManager cacheManager = mock(CacheManager.class);
        Map<String, Object> hibernateProperties = new HashMap<>();
        HibernatePropertiesCustomizer customizer = configuration.hibernatePropertiesCustomizer(cacheManager);

        customizer.customize(hibernateProperties);

        assertThat(hibernateProperties).containsEntry(ConfigSettings.CACHE_MANAGER, cacheManager);
    }
}