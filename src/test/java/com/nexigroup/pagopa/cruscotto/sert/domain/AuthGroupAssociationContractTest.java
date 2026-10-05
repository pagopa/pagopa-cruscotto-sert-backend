package com.nexigroup.pagopa.cruscotto.sert.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AuthGroupAssociationContractTest {

    @Test
    void addingAndRemovingFunctionsKeepsBothSidesInSync() {
        AuthGroup group = new AuthGroup();
        AuthFunction function = new AuthFunction();

        assertThat(group.addAuthFunction(function)).isSameAs(group);
        assertThat(group.getAuthFunctions()).contains(function);
        assertThat(function.getAuthGroups()).contains(group);

        group.removeAuthFunction(function);
        assertThat(group.getAuthFunctions()).doesNotContain(function);
        assertThat(function.getAuthGroups()).doesNotContain(group);
    }

    @Test
    void addingAndRemovingUsersUpdatesTheirGroupReference() {
        AuthGroup group = new AuthGroup();
        AuthUser user = new AuthUser();

        assertThat(group.addAuthUser(user)).isSameAs(group);
        assertThat(group.getAuthUsers()).contains(user);
        assertThat(user.getGroup()).isSameAs(group);

        group.removeAuthUser(user);
        assertThat(group.getAuthUsers()).doesNotContain(user);
        assertThat(user.getGroup()).isNull();
    }

    @Test
    void functionPermissionAssociationRemainsBidirectional() {
        AuthFunction function = new AuthFunction();
        AuthPermission permission = new AuthPermission();

        function.addAuthPermission(permission);
        assertThat(function.getAuthPermissions()).contains(permission);
        assertThat(permission.getAuthFunctions()).contains(function);

        function.removeAuthPermission(permission);
        assertThat(function.getAuthPermissions()).doesNotContain(permission);
        assertThat(permission.getAuthFunctions()).doesNotContain(function);
    }
}