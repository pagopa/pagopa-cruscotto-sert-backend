package com.nexigroup.pagopa.cruscotto.sert.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import java.util.Map;
import javax.sql.DataSource;
import com.nexigroup.pagopa.cruscotto.sert.repository.history.entity.HistoryPosition;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration(proxyBeanMethods = false)
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.nexigroup.pagopa.cruscotto.sert.repository.history",
    entityManagerFactoryRef = "historyEntityManagerFactory",
    transactionManagerRef = "historyTransactionManager"
)
public class HistoryDatasourceConfiguration {

    @Bean
    @ConfigurationProperties("spring.datasource.history")
    public DataSourceProperties historyDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "historyDataSource", destroyMethod = "close")
    @ConfigurationProperties("spring.datasource.history.hikari")
    public HikariDataSource historyDataSource(
        @Qualifier("historyDataSourceProperties") DataSourceProperties properties
    ) {
        return properties.initializeDataSourceBuilder()
            .type(HikariDataSource.class)
            .build();
    }

    @Bean(name = "historyEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean historyEntityManagerFactory(
        EntityManagerFactoryBuilder builder,
        @Qualifier("historyDataSource") DataSource dataSource,
        @Value("${spring.datasource.history.hikari.schema}") String schema
    ) {
        return builder
            .dataSource(dataSource)
            .packages("com.nexigroup.pagopa.cruscotto.sert.domain")
            .persistenceUnit("history")
            .properties(Map.of("hibernate.default_schema", schema))
            .build();
    }

    @Bean(name = "historyTransactionManager")
    public PlatformTransactionManager historyTransactionManager(
        @Qualifier("historyEntityManagerFactory") EntityManagerFactory entityManagerFactory
    ) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
