package com.nexigroup.pagopa.cruscotto.sert.web.rest.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.service.AuthPermissionService;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthPermissionDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.filter.AuthPermissionFilter;
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

class AuthPermissionResourceTest {

    private final AuthPermissionService service = Mockito.mock(AuthPermissionService.class);
    private final AuthPermissionResource resource = new AuthPermissionResource(service);

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth-permissions");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void createsPermissionAndReturnsCreatedResponse() throws Exception {
        AuthPermissionDTO input = permission(null, "users.read");
        AuthPermissionDTO saved = permission(1L, "users.read");
        when(service.save(input)).thenReturn(saved);

        ResponseEntity<AuthPermissionDTO> response = resource.createAuthPermission(input);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(saved);
        assertThat(response.getHeaders().getLocation()).isEqualTo(URI.create("/api/auth-permissions/1"));
        verify(service).save(input);
    }

    @Test
    void rejectsPermissionWithAnId() {
        AuthPermissionDTO input = permission(1L, "users.read");

        assertThatThrownBy(() -> resource.createAuthPermission(input))
            .isInstanceOf(BadRequestAlertException.class)
            .satisfies(exception -> assertThat(
                ((BadRequestAlertException) exception).getProblemDetailWithCause().getTitle()
            ).isEqualTo("A new authPermission cannot already have an ID"));
    }

    @Test
    void updatesPermissionAndReturnsOkResponse() throws Exception {
        AuthPermissionDTO input = permission(2L, "users.write");
        when(service.save(input)).thenReturn(input);

        ResponseEntity<AuthPermissionDTO> response = resource.updateAuthPermission(input);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(input);
        verify(service).save(input);
    }

    @Test
    void rejectsPermissionWithoutAnId() {
        AuthPermissionDTO input = permission(null, "users.read");

        assertThatThrownBy(() -> resource.updateAuthPermission(input))
            .isInstanceOf(BadRequestAlertException.class)
            .satisfies(exception -> assertThat(
                ((BadRequestAlertException) exception).getProblemDetailWithCause().getTitle()
            ).isEqualTo("Invalid id"));
    }

    @Test
    void retrievesAllPermissionsWithPagination() {
        AuthPermissionFilter filter = new AuthPermissionFilter();
        PageImpl<AuthPermissionDTO> page = new PageImpl<>(List.of(permission(1L, "users.read")));
        when(service.findAll(filter, Pageable.unpaged())).thenReturn(page);

        ResponseEntity<List<AuthPermissionDTO>> response = resource.getAllAuthPermissions(filter, Pageable.unpaged());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(permission(1L, "users.read"));
        verify(service).findAll(filter, Pageable.unpaged());
    }

    @Test
    void returnsNotFoundForMissingPermission() {
        when(service.findOne(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resource.getAuthPermission(99L))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(exception -> assertThat(
                ((ResponseStatusException) exception).getStatusCode()
            ).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void deletesPermission() {
        ResponseEntity<Void> response = resource.deleteAuthPermission(4L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(service).delete(4L);
    }

    private AuthPermissionDTO permission(Long id, String nome) {
        AuthPermissionDTO dto = new AuthPermissionDTO();
        dto.setId(id);
        dto.setNome(nome);
        dto.setModulo("users");
        return dto;
    }
}
