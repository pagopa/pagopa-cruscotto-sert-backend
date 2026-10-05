package com.nexigroup.pagopa.cruscotto.sert.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class JacksonConfigurationTest {

    private final JacksonConfiguration configuration = new JacksonConfiguration();

    @Test
    void serializesLocalTimeUsingItsStringRepresentation() throws Exception {
        ObjectMapper mapper = new ObjectMapper().registerModule(configuration.javaTimeModule());

        assertThat(mapper.writeValueAsString(LocalTime.of(9, 5))).isEqualTo("\"09:05\"");
    }

    @Test
    void providesJdk8AndHibernateModules() {
        Jdk8Module jdk8Module = configuration.jdk8TimeModule();
        Hibernate6Module hibernate6Module = configuration.hibernate6Module();

        assertThat(jdk8Module).isNotNull();
        assertThat(hibernate6Module).isNotNull();
        assertThat(hibernate6Module.isEnabled(Hibernate6Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS)).isTrue();
    }
}