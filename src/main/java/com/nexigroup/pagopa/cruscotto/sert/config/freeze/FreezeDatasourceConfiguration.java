package com.nexigroup.pagopa.cruscotto.sert.config.freeze;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class FreezeDatasourceConfiguration {

    @Bean(name = "freezeDataSourceProperties")
    @ConfigurationProperties("spring.freeze-datasource")
    public DataSourceProperties freezeDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "freezeDataSource", destroyMethod = "close")
    @ConfigurationProperties("spring.freeze-datasource.hikari")
    public HikariDataSource freezeDataSource(
        @Qualifier("freezeDataSourceProperties") DataSourceProperties properties
    ) {
        return properties.initializeDataSourceBuilder()
            .type(HikariDataSource.class)
            .build();
    }

    @Bean(name = "freezeJdbcTemplate")
    public JdbcTemplate freezeJdbcTemplate(@Qualifier("freezeDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
