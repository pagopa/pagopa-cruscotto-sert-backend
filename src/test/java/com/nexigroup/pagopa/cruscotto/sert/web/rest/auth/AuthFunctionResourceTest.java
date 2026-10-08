package com.nexigroup.pagopa.cruscotto.sert.web.rest.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.service.AuthFunctionService;
import com.nexigroup.pagopa.cruscotto.sert.service.AuthPermissionService;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthFunctionDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthPermissionDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.filter.AuthFunctionFilter;
import com.nexigroup.pagopa.cruscotto.sert.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

class AuthFunctionResourceTest {

    private final AuthFunctionService functionService = Mockito.mock(AuthFunctionService.class);
    private final AuthPermissionService permissionService = Mockito.mock(AuthPermissionService.class);
    private final AuthFunctionResource resource = new AuthFunctionResource(functionService, permissionService);

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth-functions");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void createsFunctionAndReturnsCreatedResponse() throws Exception {
        AuthFunctionDTO input = function(null, "read");
        AuthFunctionDTO saved = function(1L, "read");
        when(functionService.save(input)).thenReturn(saved);

        ResponseEntity<AuthFunctionDTO> response = resource.createAuthFunction(input);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(saved);
        assertThat(response.getHeaders().getLocation()).isEqualTo(URI.create("/api/auth-functions/1"));
        verify(functionService).save(input);
    }

    @Test
    void rejectsFunctionWithAnId() {
        assertThatThrownBy(() -> resource.createAuthFunction(function(1L, "read")))
            .isInstanceOf(BadRequestAlertException.class)
            .satisfies(exception -> assertThat(
                ((BadRequestAlertException) exception).getProblemDetailWithCause().getTitle()
            ).isEqualTo("A new authFunction cannot already have an ID"));
    }

    @Test
    void retrievesFunctionsWithPagination() {
        AuthFunctionFilter filter = new AuthFunctionFilter();
        PageImpl<AuthFunctionDTO> page = new PageImpl<>(List.of(function(1L, "read")));
        when(functionService.findAll(filter, Pageable.unpaged())).thenReturn(page);

        ResponseEntity<List<AuthFunctionDTO>> response = resource.getAllAuthFunctions(filter, Pageable.unpaged());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(function(1L, "read"));
    }

    @Test
    void returnsNotFoundForMissingFunction() {
        when(functionService.findOne(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resource.getAuthFunction(99L))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(exception -> assertThat(
                ((ResponseStatusException) exception).getStatusCode()
            ).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void rejectsEmptyPermissionAssociation() {
        assertThatThrownBy(() -> resource.aggiungiAssociazioneFunzione(1L, new AuthPermissionDTO[0]))
            .isInstanceOf(BadRequestAlertException.class)
            .satisfies(exception -> assertThat(
                ((BadRequestAlertException) exception).getProblemDetailWithCause().getTitle()
            ).isEqualTo("Selezionare almeno un permesso da associare alla funzione"));
    }

    @Test
    void rejectsDuplicatePermissionAssociation() {
        AuthPermissionDTO permission = permission(10L);
        when(permissionService.listAllPermissionSelected(1L)).thenReturn(List.of(10L));

        assertThatThrownBy(() -> resource.aggiungiAssociazioneFunzione(1L, new AuthPermissionDTO[] {permission}))
            .isInstanceOf(BadRequestAlertException.class)
            .satisfies(exception -> assertThat(
                ((BadRequestAlertException) exception).getProblemDetailWithCause().getTitle()
            ).isEqualTo("Permesso già associata alla funzione"));
    }

    @Test
    void associatesNewPermissions() throws Exception {
        AuthPermissionDTO permission = permission(10L);
        when(permissionService.listAllPermissionSelected(1L)).thenReturn(List.of());

        ResponseEntity<Void> response = resource.aggiungiAssociazioneFunzione(1L, new AuthPermissionDTO[] {permission});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(functionService).associaPermesso(1L, new AuthPermissionDTO[] {permission});
    }

    @Test
    void deletesFunction() {
        ResponseEntity<Void> response = resource.deleteAuthFunction(3L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(functionService).delete(3L);
    }

    private AuthFunctionDTO function(Long id, String nome) {
        AuthFunctionDTO dto = new AuthFunctionDTO();
        dto.setId(id);
        dto.setNome(nome);
        dto.setModulo("functions");
        dto.setDescrizione("Description");
        return dto;
    }

    private AuthPermissionDTO permission(Long id) {
        AuthPermissionDTO dto = new AuthPermissionDTO();
        dto.setId(id);
        dto.setNome("permission");
        dto.setModulo("permissions");
        return dto;
    }
}
