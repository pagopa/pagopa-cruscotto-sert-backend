package com.nexigroup.pagopa.cruscotto.sert.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexigroup.pagopa.cruscotto.sert.service.massivesearch.filter.SearchBulkFilterDTO;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SearchDomainEntityTest {

    @Test
    void searchInstanceBuilderAndValueEqualityIncludeConfiguredFields() {
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        SearchInstance first = SearchInstance.builder()
            .id(id)
            .name("daily search")
            .inputType("CSV")
            .status("DRAFT")
            .createdAt(createdAt)
            .updatedAt(createdAt)
            .selectedReports("payments")
            .build();
        SearchInstance same = SearchInstance.builder()
            .id(id)
            .name("daily search")
            .inputType("CSV")
            .status("DRAFT")
            .createdAt(createdAt)
            .updatedAt(createdAt)
            .selectedReports("payments")
            .build();
        SearchInstance different = SearchInstance.builder().id(UUID.randomUUID()).name("other").build();

        assertThat(first).isEqualTo(same).hasSameHashCodeAs(same).isNotEqualTo(different);
        assertThat(first.toString()).contains("daily search", "payments");
    }

    @Test
    void positionBuilderSupportsItsDateAndEventFields() {
        LocalDate eventDate = LocalDate.of(2026, 10, 5);
        LocalDateTime eventTime = LocalDateTime.of(2026, 10, 5, 11, 30);
        Position position = Position.builder()
            .id(7)
            .dateEvent(eventDate)
            .insertedTimestamp(eventTime)
            .nav("nav-1")
            .paEmittente("pa-1")
            .lastEvent(eventTime)
            .dateEvents("[]")
            .build();

        assertThat(position.getId()).isEqualTo(7);
        assertThat(position.getDateEvent()).isEqualTo(eventDate);
        assertThat(position.getInsertedTimestamp()).isEqualTo(eventTime);
        assertThat(position.getLastEvent()).isEqualTo(eventTime);
        assertThat(position.getNav()).isEqualTo("nav-1");
        assertThat(position.getPaEmittente()).isEqualTo("pa-1");
        assertThat(position.getDateEvents()).isEqualTo("[]");
    }

    @Test
    void compositeAuthGroupFunctionIdentityComparesBothRelations() {
        AuthFunction function = new AuthFunction();
        AuthGroup group = new AuthGroup();
        AuthGroupAuthFunction first = new AuthGroupAuthFunction();
        first.setFunzione(function);
        first.setGruppo(group);
        AuthGroupAuthFunction same = new AuthGroupAuthFunction();
        same.setFunzione(function);
        same.setGruppo(group);
        AuthGroupAuthFunction otherGroup = new AuthGroupAuthFunction();
        otherGroup.setFunzione(function);
        otherGroup.setGruppo(new AuthGroup());

        assertThat(first).isEqualTo(first).isEqualTo(same).hasSameHashCodeAs(same).isNotEqualTo(otherGroup);
        assertThat(first).isNotEqualTo(null).isNotEqualTo("different type");
    }

    @Test
    void compositeFunctionPermissionIdentityComparesBothRelations() {
        AuthFunction function = new AuthFunction();
        AuthPermission permission = new AuthPermission();
        AuthFunctionAuthPermission first = new AuthFunctionAuthPermission();
        first.setFunzione(function);
        first.setPermesso(permission);
        AuthFunctionAuthPermission same = new AuthFunctionAuthPermission();
        same.setFunzione(function);
        same.setPermesso(permission);
        AuthFunctionAuthPermission otherPermission = new AuthFunctionAuthPermission();
        otherPermission.setFunzione(function);
        otherPermission.setPermesso(new AuthPermission());

        assertThat(first).isEqualTo(first).isEqualTo(same).hasSameHashCodeAs(same).isNotEqualTo(otherPermission);
        assertThat(first).isNotEqualTo(null).isNotEqualTo("different type");
    }

    @Test
    void compositeIdExposesBothKeyParts() {
        AuthGroupAuthFunctionId id = new AuthGroupAuthFunctionId();
        id.setGruppo(10L);
        id.setFunzione(20L);

        assertThat(id.getGruppo()).isEqualTo(10L);
        assertThat(id.getFunzione()).isEqualTo(20L);
    }

    @Test
    void authGroupMaintainsBothSidesOfUserAndFunctionRelationships() {
        AuthGroup group = new AuthGroup().nome("operators").descrizione("Operations");
        AuthFunction function = new AuthFunction();
        AuthUser user = new AuthUser();

        group.addAuthFunction(function).addAuthUser(user);
        assertThat(group.getAuthFunctions()).contains(function);
        assertThat(function.getAuthGroups()).contains(group);
        assertThat(group.getAuthUsers()).contains(user);
        assertThat(user.getGroup()).isSameAs(group);

        group.removeAuthFunction(function).removeAuthUser(user);
        assertThat(group.getAuthFunctions()).doesNotContain(function);
        assertThat(function.getAuthGroups()).doesNotContain(group);
        assertThat(group.getAuthUsers()).doesNotContain(user);
        assertThat(user.getGroup()).isNull();
    }

    @Test
    void authFunctionMaintainsBothSidesOfPermissionRelationship() {
        AuthFunction function = new AuthFunction().nome("search").modulo("SERT").descrizione("Search positions");
        AuthPermission permission = new AuthPermission().nome("POSITION_LIST").modulo("SERT");

        function.addAuthPermission(permission);
        assertThat(function.getAuthPermissions()).contains(permission);
        assertThat(permission.getAuthFunctions()).contains(function);

        function.removeAuthPermission(permission);
        assertThat(function.getAuthPermissions()).doesNotContain(permission);
        assertThat(permission.getAuthFunctions()).doesNotContain(function);
        assertThat(function.toString()).contains("search", "SERT", "Search positions");
        assertThat(permission.toString()).contains("POSITION_LIST", "SERT");
    }

    @Test
    void authUserEqualityUsesPersistedIdAndToStringDoesNotRequirePersistence() {
        AuthUser first = new AuthUser();
        AuthUser noId = new AuthUser();
        AuthUser sameId = new AuthUser();
        first.setId(5L);
        sameId.setId(5L);
        first.setLogin("test@example.test");
        first.setPassword("password-hash");

        assertThat(first).isEqualTo(first).isEqualTo(sameId).isNotEqualTo(noId).hasSameHashCodeAs(sameId);
        assertThat(first.toString()).contains("test@example.test", "password-hash");
    }

    @Test
    void searchFilterSerializesAndDeserializesFilterDataAndHandlesInvalidJson() {
        SearchFilter filter = SearchFilter.builder().instanceId(UUID.randomUUID()).build();
        SearchBulkFilterDTO dto = new SearchBulkFilterDTO();
        SearchBulkFilterDTO.AmountFilter amount = new SearchBulkFilterDTO.AmountFilter();
        amount.setMin(new BigDecimal("10.50"));
        amount.setMax(new BigDecimal("75.00"));
        dto.setAmount(amount);
        dto.setTouchpoints(java.util.List.of("WEB", "APP"));

        filter.setFilterObject(dto);
        SearchBulkFilterDTO restored = filter.getFilterObject();

        assertThat(restored.getAmount().getMin()).isEqualByComparingTo("10.50");
        assertThat(restored.getAmount().getMax()).isEqualByComparingTo("75.00");
        assertThat(restored.getTouchpoints()).containsExactly("WEB", "APP");

        filter.setFilterObject(null);
        assertThat(filter.getFilterJson()).isNull();
        assertThat(filter.getFilterObject()).isNull();
        filter.setFilterJson("{");
        assertThat(filter.getFilterObject()).isNull();
    }

    @Test
    void builderDefaultsAttemptNumberForExecutionStep() {
        SearchExecutionStep step = SearchExecutionStep.builder().phase("IMPORT").build();

        assertThat(step.getAttemptNo()).isEqualTo(1);
    }

    @Test
    void auditingEntityInitializesDatesAndExposesAuditFields() {
        AuthUser user = new AuthUser();
        Instant createdDate = Instant.parse("2026-01-01T00:00:00Z");
        Instant modifiedDate = Instant.parse("2026-01-02T00:00:00Z");
        user.setCreatedBy("creator");
        user.setCreatedDate(createdDate);
        user.setLastModifiedBy("modifier");
        user.setLastModifiedDate(modifiedDate);

        assertThat(user.getCreatedBy()).isEqualTo("creator");
        assertThat(user.getCreatedDate()).isEqualTo(createdDate);
        assertThat(user.getLastModifiedBy()).isEqualTo("modifier");
        assertThat(user.getLastModifiedDate()).isEqualTo(modifiedDate);
    }

    @Test
    void searchExecutionAndStepBuildersPreserveLifecycleFields() {
        UUID instanceId = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();
        Instant now = Instant.parse("2026-10-05T10:00:00Z");
        SearchInstance instance = SearchInstance.builder().id(instanceId).build();
        SearchExecution execution = SearchExecution.builder()
            .id(executionId)
            .instance(instance)
            .status("COMPLETED")
            .startedAt(now)
            .completedAt(now.plusSeconds(15))
            .totalInputRows(12L)
            .processedRows(12L)
            .generatedFiles(2)
            .errorCode(null)
            .errorMessage(null)
            .createdAt(now)
            .updatedAt(now)
            .build();
        SearchExecutionStep step = SearchExecutionStep.builder()
            .id(UUID.randomUUID())
            .executionId(executionId)
            .instanceId(instanceId)
            .phase("DOWNLOAD")
            .attemptNo(2)
            .status("COMPLETED")
            .windowFrom(LocalDateTime.of(2026, 10, 1, 0, 0))
            .windowTo(LocalDateTime.of(2026, 10, 5, 0, 0))
            .rowsProcessed(12L)
            .startedAt(now)
            .endedAt(now.plusSeconds(3))
            .durationMs(3000L)
            .errorCode(null)
            .errorMessage(null)
            .createdAt(now)
            .build();

        assertThat(execution.getInstance()).isSameAs(instance);
        assertThat(execution.getCompletedAt()).isEqualTo(now.plusSeconds(15));
        assertThat(execution.getTotalInputRows()).isEqualTo(12L);
        assertThat(execution.getProcessedRows()).isEqualTo(12L);
        assertThat(execution.getGeneratedFiles()).isEqualTo(2);
        assertThat(step.getAttemptNo()).isEqualTo(2);
        assertThat(step.getExecutionId()).isEqualTo(executionId);
        assertThat(step.getRowsProcessed()).isEqualTo(12L);
        assertThat(step.getDurationMs()).isEqualTo(3000L);
        assertThat(step.getWindowTo()).isEqualTo(LocalDateTime.of(2026, 10, 5, 0, 0));
    }

    @Test
    void searchResultAndPerimeterFileBuildersPreserveMetadata() {
        UUID instanceId = UUID.randomUUID();
        UUID executionId = UUID.randomUUID();
        Instant now = Instant.parse("2026-10-05T10:00:00Z");
        SearchInstance instance = SearchInstance.builder().id(instanceId).build();
        SearchResult result = SearchResult.builder()
            .id(instanceId)
            .instance(instance)
            .executionId(executionId)
            .zipFileName("result.zip")
            .zipFilePath("/exports/result.zip")
            .zipSizeBytes(1024L)
            .positionRows(20L)
            .attemptRows(3L)
            .transferRows(6L)
            .generatedAt(now)
            .updatedAt(now)
            .build();
        SearchPerimeterFile perimeterFile = SearchPerimeterFile.builder()
            .id(UUID.randomUUID())
            .instance(instance)
            .executionId(executionId)
            .source("UPLOAD")
            .template("NAV_EC")
            .fileName("perimeter.csv")
            .filePath("/uploads/perimeter.csv")
            .rowsCount(20L)
            .validationStatus("VALID")
            .createdAt(now)
            .content("NAV;EC")
            .build();

        assertThat(result.getInstance()).isSameAs(instance);
        assertThat(result.getZipSizeBytes()).isEqualTo(1024L);
        assertThat(result.getPositionRows()).isEqualTo(20L);
        assertThat(result.getAttemptRows()).isEqualTo(3L);
        assertThat(result.getTransferRows()).isEqualTo(6L);
        assertThat(perimeterFile.getInstance()).isSameAs(instance);
        assertThat(perimeterFile.getExecutionId()).isEqualTo(executionId);
        assertThat(perimeterFile.getTemplate()).isEqualTo("NAV_EC");
        assertThat(perimeterFile.getRowsCount()).isEqualTo(20L);
        assertThat(perimeterFile.getContent()).isEqualTo("NAV;EC");
    }

    @Test
    void positionPaymentEntitiesPreserveAmountsAndIdentifiers() {
        PositionTokens token = PositionTokens.builder()
            .id(1)
            .fkPosition(2)
            .token("token-1")
            .amount(new BigDecimal("12.34"))
            .fee(new BigDecimal("0.50"))
            .iuv("iuv-1")
            .creditorRefId("creditor-1")
            .outcome("OK")
            .idCarrello("cart-1")
            .stazione((short) 1)
            .canale((short) 2)
            .intermediarioPa((short) 3)
            .intermediarioPsp((short) 4)
            .psp((short) 5)
            .touchpoint("WEB")
            .paymentMethod("CARD")
            .paymentDate(LocalDateTime.of(2026, 10, 5, 10, 0))
            .build();
        PositionTransfers transfer = PositionTransfers.builder()
            .id(9)
            .fkToken(1)
            .paTransfer("pa-1")
            .idTransfer((short) 7)
            .ibanTransfer("IT000")
            .amountTransfer(new BigDecimal("10.00"))
            .isBollo(Boolean.TRUE)
            .build();
        ExtraInfo extraInfo = ExtraInfo.builder().id(3).fkToken(1).infoName("key").infoValue("value").tipoEvento((short) 4).build();

        assertThat(token.getAmount()).isEqualByComparingTo("12.34");
        assertThat(token.getFee()).isEqualByComparingTo("0.50");
        assertThat(token.getPaymentMethod()).isEqualTo("CARD");
        assertThat(token.getPsp()).isEqualTo((short) 5);
        assertThat(transfer.getAmountTransfer()).isEqualByComparingTo("10.00");
        assertThat(transfer.getIbanTransfer()).isEqualTo("IT000");
        assertThat(transfer.getIsBollo()).isTrue();
        assertThat(extraInfo.getInfoName()).isEqualTo("key");
        assertThat(extraInfo.getInfoValue()).isEqualTo("value");
        assertThat(extraInfo.getTipoEvento()).isEqualTo((short) 4);
    }
}