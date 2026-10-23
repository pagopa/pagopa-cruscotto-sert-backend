package com.nexigroup.pagopa.cruscotto.sert.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManagerFactory;
import com.nexigroup.pagopa.cruscotto.sert.security.SpringSecurityAuditorAware;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class OnlineAndHistoryDatasourceConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(
                ConfigurationPropertiesAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class
            )
        )
        .withUserConfiguration(
            OnlineDatasourceConfiguration.class,
            HistoryDatasourceConfiguration.class,
            SpringSecurityAuditorAware.class
        )
        .withPropertyValues(
            "spring.datasource.online.url=jdbc:h2:mem:online;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=FALSE;INIT=CREATE SCHEMA IF NOT EXISTS sert_ingestor",
            "spring.datasource.online.driver-class-name=org.h2.Driver",
            "spring.datasource.online.username=online-user",
            "spring.datasource.online.hikari.schema=sert_ingestor",
            "spring.datasource.history.url=jdbc:h2:mem:history;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=FALSE;INIT=CREATE SCHEMA IF NOT EXISTS sert_history",
            "spring.datasource.history.driver-class-name=org.h2.Driver",
            "spring.datasource.history.username=history-user",
            "spring.datasource.history.hikari.schema=sert_history",
            "spring.jpa.hibernate.ddl-auto=none",
            "spring.jpa.properties.hibernate.default_schema=sert_ingestor"
        );

    @Test
    void configuresSeparatePrimaryJpaAndHistoryJdbcDatasources() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();

            DataSource primaryDataSource = context.getBean(DataSource.class);
            HikariDataSource onlineDataSource = context.getBean("onlineDataSource", HikariDataSource.class);
            HikariDataSource historyDataSource = context.getBean("historyDataSource", HikariDataSource.class);

            assertThat(primaryDataSource).isSameAs(onlineDataSource);
            assertThat(onlineDataSource.getJdbcUrl()).startsWith("jdbc:h2:mem:online");
            assertThat(onlineDataSource.getUsername()).isEqualTo("online-user");
            assertThat(onlineDataSource.getSchema()).isEqualTo("sert_ingestor");
            assertThat(historyDataSource.getJdbcUrl()).startsWith("jdbc:h2:mem:history");
            assertThat(historyDataSource.getUsername()).isEqualTo("history-user");
            assertThat(historyDataSource.getSchema()).isEqualTo("sert_history");
            assertThat(context).hasBean("historyEntityManagerFactory");
            assertThat(context).hasBean("historyTransactionManager");
            assertThat(context).hasBean("historyPositionRepository");
            assertThat(context).hasBean("onlineEntityManagerFactory");
            assertThat(context).hasBean("onlineTransactionManager");
            EntityManagerFactory entityManagerFactory = context.getBean("onlineEntityManagerFactory", EntityManagerFactory.class);
            assertThat(entityManagerFactory.getProperties())
                .containsEntry("hibernate.default_schema", "sert_ingestor");
            EntityManagerFactory historyEntityManagerFactory = context.getBean(
                "historyEntityManagerFactory",
                EntityManagerFactory.class
            );
            assertThat(historyEntityManagerFactory.getProperties())
                .containsEntry("hibernate.default_schema", "sert_history");
        });
    }
}
