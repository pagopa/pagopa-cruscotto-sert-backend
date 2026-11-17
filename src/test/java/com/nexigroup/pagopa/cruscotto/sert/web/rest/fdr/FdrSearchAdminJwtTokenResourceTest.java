package com.nexigroup.pagopa.cruscotto.sert.web.rest.fdr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

import com.nexigroup.pagopa.cruscotto.sert.security.AuthoritiesConstants;
import com.nexigroup.pagopa.cruscotto.sert.service.FdrService;
import com.nexigroup.pagopa.cruscotto.sert.service.impl.FdrServiceImpl;
import com.nexigroup.pagopa.cruscotto.sert.web.rest.sertSearch.sertResourceJwtToken.FdrSearchAdminJwtTokenResource;
import java.time.LocalDate;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.nexigroup.pagopa.cruscotto.sert.web.rest.sertSearch.sertResourceSubKey.FdrSearchAdminSubKeyResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class FdrSearchAdminJwtTokenResourceTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        FdrService service = new FdrServiceImpl();
        mockMvc = MockMvcBuilders.standaloneSetup(
            new FdrSearchAdminJwtTokenResource(service),
            new FdrSearchAdminSubKeyResource(service)
        )
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    @Test
    void servesContractShapedSearchResponsesOnBothAuthenticationPaths() throws Exception {
        mockMvc.perform(get("/api/fdr/flows").param("flowId", "FLOW-42").param("scope", "ALL"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].flowId").value("FLOW-42"))
            .andExpect(header().string("X-Total-Count", "2"));

        mockMvc.perform(
            get("/api/fdr/flows")
                .param("flowId", "FLOW-42")
                .param("scope", "ALL")
                .param("page", "1")
                .param("size", "1")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].revisionOrdinal").value(1))
            .andExpect(header().string("X-Total-Count", "2"));

        mockMvc.perform(get("/sub/api/fdr/occurrences/by-iuv").param("iuv", "IUV-42").param("flowId", "FLOW-42").param("scope", "ALL"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].searchedBy").value("IUV_AND_FLOW_ID"))
            .andExpect(jsonPath("$[0].searchedValue").value("IUV-42"))
            .andExpect(jsonPath("$[0].revisionNumber").value(2))
            .andExpect(jsonPath("$[0].totalFlowRevisions").value(2))
            .andExpect(header().string("X-Total-Count", "2"));
    }

    @Test
    void acceptsReingestionMockAndReturnsAcceptedBatch() throws Exception {
        mockMvc.perform(
            post("/api/admin/fdr/reingestions")
                .header("Idempotency-Key", "mock-key-001")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"arrivalDates\":[\"2026-11-15\"],\"reason\":\"Mock request\"}")
        )
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.status").value("ACCEPTED"))
            .andExpect(jsonPath("$.items[0].sequence").value(1));
    }

    @Test
    void exposesAllJwtApiOperationsOnSubscriptionKeyResource() {
        var jwtOperations = Arrays.stream(FdrSearchAdminJwtTokenResource.class.getDeclaredMethods())
            .filter(method -> Modifier.isPublic(method.getModifiers()))
            .map(java.lang.reflect.Method::getName)
            .collect(Collectors.toSet());
        var subKeyOperations = Arrays.stream(FdrSearchAdminSubKeyResource.class.getDeclaredMethods())
            .filter(method -> Modifier.isPublic(method.getModifiers()))
            .map(java.lang.reflect.Method::getName)
            .collect(Collectors.toSet());

        assertThat(subKeyOperations).containsExactlyInAnyOrderElementsOf(jwtOperations);
    }

    @Test
    void servesAdministrativeOperationsOnSubscriptionKeyResource() throws Exception {
        mockMvc.perform(
            post("/sub/api/admin/fdr/reingestions")
                .header("Idempotency-Key", "sub-key-001")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"arrivalDates\":[\"2026-11-15\"],\"reason\":\"SubKey request\"}")
        )
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.status").value("ACCEPTED"));

        mockMvc.perform(get("/sub/api/admin/fdr/reingestions").param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].requestId").exists())
            .andExpect(header().exists("X-Total-Count"));

        mockMvc.perform(get("/sub/api/admin/fdr/ingestion-days").param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].arrivalDate[0]").value(2026));

        mockMvc.perform(get("/sub/api/admin/fdr/ingestion-days/2026-11-15"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.arrivalDate[0]").value(2026));

        mockMvc.perform(get("/sub/api/admin/fdr/ingestion-events").param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].eventCode").value("INGESTION_COMPLETED"))
            .andExpect(header().exists("X-Total-Count"));

        mockMvc.perform(get("/sub/api/admin/fdr/reingestions/9b70cfa7-299b-42da-b5c8-000000000003"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.requestId").value("9b70cfa7-299b-42da-b5c8-000000000003"));
    }

    @Test
    void streamsRevisionCsvOnSubscriptionKeyResource() throws Exception {
        var result = mockMvc.perform(get("/sub/api/fdr/flow-revisions/9b70cfa7-299b-42da-b5c8-000000000001/export"))
            .andExpect(status().isOk())
            .andReturn();

        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("text/csv"))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("\"recordId\",\"revisionId\"")));
    }

    @Test
    void streamsRevisionCsvWithJwtOnlyApiAuthority() throws Exception {
        var result = mockMvc.perform(
            get("/api/fdr/flow-revisions/9b70cfa7-299b-42da-b5c8-000000000001/export")
        ).andExpect(status().isOk()).andReturn();

        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("text/csv"))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("\"recordId\",\"revisionId\"")));
    }

    @Test
    void protectsEveryJwtEndpointWithAnApiSpecificAuthority() throws NoSuchMethodException {
        assertAuthority("searchFlows", String.class, LocalDate.class, LocalDate.class, String.class, String.class, Pageable.class);
        assertAuthority("getRevision", java.util.UUID.class);
        assertAuthority(
            "searchPayments",
            java.util.UUID.class,
            String.class,
            String.class,
            BigDecimal.class,
            String.class,
            String.class,
            Pageable.class
        );
        assertAuthority("searchByIuv", String.class, String.class, String.class, LocalDate.class, LocalDate.class, String.class, Pageable.class);
        assertAuthority("searchByIur", String.class, LocalDate.class, LocalDate.class, String.class, String.class, Pageable.class);
        assertAuthority("exportRevision", java.util.UUID.class);
        assertAuthority("requestReingestion", String.class, FdrSearchAdminJwtTokenResource.ReingestionRequest.class);
        assertAuthority("listReingestions", String.class, Pageable.class);
        assertAuthority("getReingestion", java.util.UUID.class);
        assertAuthority("listIngestionDays", LocalDate.class, LocalDate.class, String.class, Pageable.class);
        assertAuthority("getIngestionDay", LocalDate.class);
        assertAuthority("searchIngestionEvents", LocalDate.class, java.util.UUID.class, String.class, String.class, Pageable.class);
    }

    @Test
    void subscriptionKeyEndpointsDoNotUsePreAuthorize() throws NoSuchMethodException {
        Class<?> resource = FdrSearchAdminSubKeyResource.class;
        assertThat(resource.getDeclaredMethods()).allSatisfy(method -> assertThat(method.isAnnotationPresent(PreAuthorize.class)).isFalse());
    }

    private void assertAuthority(String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        PreAuthorize annotation = FdrSearchAdminJwtTokenResource.class.getDeclaredMethod(methodName, parameterTypes).getAnnotation(PreAuthorize.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).contains("GTW.FDR_");
        assertThat(annotation.value()).doesNotContain(AuthoritiesConstants.SERT_SEARCH);
    }
}
