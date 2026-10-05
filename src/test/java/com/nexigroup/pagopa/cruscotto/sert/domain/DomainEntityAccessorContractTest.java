package com.nexigroup.pagopa.cruscotto.sert.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class DomainEntityAccessorContractTest {

    private static final List<Class<?>> DOMAIN_TYPES = List.of(
        AnagCanale.class,
        AnagEvento.class,
        AnagFaultCode.class,
        AnagIntermediarioPa.class,
        AnagIntermediarioPsp.class,
        AnagPaEmittente.class,
        AnagPaymentMethod.class,
        AnagPsp.class,
        AnagStazione.class,
        AnagTouchpoint.class,
        AuthFunction.class,
        AuthFunctionAuthPermission.class,
        AuthFunctionAuthPermissionId.class,
        AuthGroup.class,
        AuthGroupAuthFunction.class,
        AuthGroupAuthFunctionId.class,
        AuthPermission.class,
        AuthUser.class,
        AuthUserHistory.class,
        EventsWf.class,
        ExtraInfo.class,
        Position.class,
        PositionTokens.class,
        PositionTransfers.class,
        SearchExecution.class,
        SearchExecutionStep.class,
        SearchFilter.class,
        SearchInstance.class,
        SearchPerimeterFile.class,
        SearchResult.class
    );

    @TestFactory
    Stream<DynamicTest> readableWritablePropertiesRoundTrip() {
        return DOMAIN_TYPES.stream().flatMap(type -> {
            try {
                Object instance = createInstance(type);
                return Stream.of(Introspector.getBeanInfo(type).getPropertyDescriptors())
                    .filter(property -> property.getReadMethod() != null && property.getWriteMethod() != null)
                    .map(property -> DynamicTest.dynamicTest(type.getSimpleName() + "." + property.getName(), () ->
                        assertPropertyRoundTrips(instance, property)
                    ));
            } catch (Exception exception) {
                throw new IllegalStateException("Cannot inspect domain type " + type.getName(), exception);
            }
        });
    }

    @TestFactory
    Stream<DynamicTest> entityObjectMethodsAreUsable() {
        return DOMAIN_TYPES.stream().map(type -> DynamicTest.dynamicTest(type.getSimpleName() + " object methods", () -> {
            Object first = createInstance(type);
            Object second = createInstance(type);

            assertThat(first).isEqualTo(first);
            assertThat(first.equals(null)).isFalse();
            first.equals(second);
            assertThat(first.hashCode()).isNotZero();
            assertThat(first.toString()).isNotBlank();
        }));
    }

    private void assertPropertyRoundTrips(Object instance, PropertyDescriptor property) throws Exception {
        Object value = valueFor(property.getPropertyType());
        Method setter = property.getWriteMethod();
        Method getter = property.getReadMethod();
        setter.invoke(instance, value);
        Object actual = getter.invoke(instance);

        assertThat(actual).isEqualTo(value);
    }

    private Object createInstance(Class<?> type) throws Exception {
        Constructor<?> constructor = type.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private Object valueFor(Class<?> type) {
        if (type == String.class) return "domain-test";
        if (type == Long.class || type == long.class) return 41L;
        if (type == Integer.class || type == int.class) return 41;
        if (type == Boolean.class || type == boolean.class) return true;
        if (type == Instant.class) return Instant.parse("2026-01-01T00:00:00Z");
        if (type == LocalDate.class) return LocalDate.of(2026, 1, 1);
        if (type == ZonedDateTime.class) return ZonedDateTime.parse("2026-01-01T00:00:00Z");
        if (type == UUID.class) return UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        if (type.isEnum()) return type.getEnumConstants()[0];
        if (Set.class.isAssignableFrom(type)) return new HashSet<>();
        if (List.class.isAssignableFrom(type)) return new ArrayList<>();
        if (type.isPrimitive()) return 0;
        return null;
    }
}