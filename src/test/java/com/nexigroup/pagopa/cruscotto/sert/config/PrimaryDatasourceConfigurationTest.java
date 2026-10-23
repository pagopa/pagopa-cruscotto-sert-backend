package com.nexigroup.pagopa.cruscotto.sert.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexigroup.pagopa.cruscotto.sert.config.freeze.FreezeDatasourceConfiguration;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jdbc.core.JdbcTemplate;

class PrimaryDatasourceConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
        .withUserConfiguration(PrimaryDatasourceConfiguration.class, FreezeDatasourceConfiguration.class)
        .withPropertyValues(
            "spring.datasource.url=jdbc:postgresql://localhost:5432/cruscotto?currentSchema=sert_ingestor,public",
            "spring.datasource.driver-class-name=org.postgresql.Driver",
            "spring.datasource.username=main-user",
            "spring.datasource.hikari.initialization-fail-timeout=-1",
            "spring.freeze-datasource.url=jdbc:postgresql://localhost:5432/cruscotto?currentSchema=cruscotto,public",
            "spring.freeze-datasource.driver-class-name=org.postgresql.Driver",
            "spring.freeze-datasource.username=freeze-user",
            "spring.freeze-datasource.hikari.initialization-fail-timeout=-1"
        );

    @Test
    void makesTheMainDatasourcePrimaryAndKeepsFreezeJdbcTemplateIsolated() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();

            DataSource primaryDataSource = context.getBean(DataSource.class);
            HikariDataSource mainDataSource = context.getBean("dataSource", HikariDataSource.class);
            HikariDataSource freezeDataSource = context.getBean("freezeDataSource", HikariDataSource.class);
            JdbcTemplate freezeJdbcTemplate = context.getBean("freezeJdbcTemplate", JdbcTemplate.class);

            assertThat(primaryDataSource).isSameAs(mainDataSource);
            assertThat(mainDataSource.getJdbcUrl()).contains("currentSchema=sert_ingestor,public");
            assertThat(mainDataSource.getUsername()).isEqualTo("main-user");
            assertThat(freezeDataSource.getJdbcUrl()).contains("currentSchema=cruscotto,public");
            assertThat(freezeDataSource.getUsername()).isEqualTo("freeze-user");
            assertThat(freezeJdbcTemplate.getDataSource()).isSameAs(freezeDataSource);
        });
    }
}
