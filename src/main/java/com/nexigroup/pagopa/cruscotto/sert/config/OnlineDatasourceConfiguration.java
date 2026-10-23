package com.nexigroup.pagopa.cruscotto.sert.config;

import jakarta.persistence.EntityManagerFactory;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration(proxyBeanMethods = false)
@EnableJpaAuditing(auditorAwareRef = "springSecurityAuditorAware")
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.nexigroup.pagopa.cruscotto.sert.repository.online",
    entityManagerFactoryRef = "onlineEntityManagerFactory",
    transactionManagerRef = "onlineTransactionManager"
)
public class OnlineDatasourceConfiguration {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.online")
    public DataSourceProperties onlineDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "onlineDataSource", destroyMethod = "close")
    @Primary
    @ConfigurationProperties("spring.datasource.online.hikari")
    public HikariDataSource onlineDataSource(
        @Qualifier("onlineDataSourceProperties") DataSourceProperties properties
    ) {
        return properties.initializeDataSourceBuilder()
            .type(HikariDataSource.class)
            .build();
    }

    @Bean(name = "onlineEntityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean onlineEntityManagerFactory(
        EntityManagerFactoryBuilder builder,
        @Qualifier("onlineDataSource") DataSource dataSource
    ) {
        return builder
            .dataSource(dataSource)
            .packages("com.nexigroup.pagopa.cruscotto.sert.domain")
            .persistenceUnit("online")
            .build();
    }

    @Bean(name = "onlineTransactionManager")
    @Primary
    public PlatformTransactionManager onlineTransactionManager(
        @Qualifier("onlineEntityManagerFactory") EntityManagerFactory entityManagerFactory
    ) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
