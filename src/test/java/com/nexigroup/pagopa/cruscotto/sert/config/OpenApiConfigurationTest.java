package com.nexigroup.pagopa.cruscotto.sert.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;

class OpenApiConfigurationTest {

    private final OpenApiConfiguration configuration = new OpenApiConfiguration();

    @Test
    void addsPagoPaHostVariableAndSupportedEnvironments() {
        OpenAPI openApi = new OpenAPI();
        OpenApiCustomizer customizer = configuration.pagopaServerOpenApiCustomizer();

        customizer.customise(openApi);

        assertThat(openApi.getServers()).hasSize(1);
        assertThat(openApi.getServers().get(0).getUrl()).isEqualTo("https://{host}");
        assertThat(openApi.getServers().get(0).getVariables().get("host").getDefault())
            .isEqualTo("api.dev.platform.pagopa.it");
        assertThat(openApi.getServers().get(0).getVariables().get("host").getEnum())
            .containsExactly("api.dev.platform.pagopa.it", "api.uat.platform.pagopa.it", "api.platform.pagopa.it");
    }

    @Test
    void createsGroupedApiDefinitions() {
        GroupedOpenApi sertApi = configuration.sertApi();
        GroupedOpenApi subKeyApi = configuration.subKeySertApi();

        assertThat(sertApi.getGroup()).isEqualTo("sertSearch");
        assertThat(subKeyApi.getGroup()).isEqualTo("subKeySertSearchApi");
        assertThat(subKeyApi.getPackagesToScan())
            .containsExactly("com.nexigroup.pagopa.cruscotto.sert.web.rest.sertSearch.sertResourceSubKey");

        OpenAPI sertOpenApi = new OpenAPI();
        sertApi.getOpenApiCustomizers().forEach(customizer -> customizer.customise(sertOpenApi));
        assertThat(sertOpenApi.getInfo().getTitle()).isEqualTo("Cruscotto Sert pagoPA backend service API");

        OpenAPI subKeyOpenApi = new OpenAPI();
        subKeyApi.getOpenApiCustomizers().forEach(customizer -> customizer.customise(subKeyOpenApi));
        assertThat(subKeyOpenApi.getInfo().getTitle())
            .isEqualTo("Cruscotto Sert pagoPA backend service API - Subscription Key");
    }
}