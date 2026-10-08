package com.nexigroup.pagopa.cruscotto.sert.web.rest.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nexigroup.pagopa.cruscotto.sert.service.AuthFunctionService;
import com.nexigroup.pagopa.cruscotto.sert.service.AuthGroupService;
import com.nexigroup.pagopa.cruscotto.sert.service.bean.AuthGroupUpdateRequestBean;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthFunctionDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthGroupDTO;
import com.nexigroup.pagopa.cruscotto.sert.service.filter.AuthGroupFilter;
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

class AuthGroupResourceTest {

    private final AuthFunctionService functionService = Mockito.mock(AuthFunctionService.class);
    private final AuthGroupService groupService = Mockito.mock(AuthGroupService.class);
    private final AuthGroupResource resource = new AuthGroupResource(functionService, groupService);

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth-groups");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void createsGroupAndReturnsCreatedResponse() throws Exception {
        AuthGroupDTO input = group(null, "Administrators");
        AuthGroupDTO saved = group(1L, "Administrators");
        when(groupService.save(input)).thenReturn(saved);

        ResponseEntity<AuthGroupDTO> response = resource.createAuthGroup(input);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(saved);
        assertThat(response.getHeaders().getLocation()).isEqualTo(URI.create("/api/auth-groups/1"));
        verify(groupService).save(input);
    }

    @Test
    void rejectsGroupWithoutAnIdOnUpdate() {
        AuthGroupDTO input = group(null, "Administrators");

        assertThatThrownBy(() -> resource.updateAuthGroup(input))
            .isInstanceOf(BadRequestAlertException.class)
            .satisfies(exception -> assertThat(
                ((BadRequestAlertException) exception).getProblemDetailWithCause().getTitle()
            ).isEqualTo("Invalid id"));
    }

    @Test
    void retrievesGroupsWithPagination() {
        AuthGroupFilter filter = new AuthGroupFilter();
        PageImpl<AuthGroupDTO> page = new PageImpl<>(List.of(group(1L, "Administrators")));
        when(groupService.findAll(filter, Pageable.unpaged())).thenReturn(page);

        ResponseEntity<List<AuthGroupDTO>> response = resource.getAllAuthGroups(filter, Pageable.unpaged());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(group(1L, "Administrators"));
    }

    @Test
    void returnsNotFoundForMissingGroup() {
        when(groupService.findOne(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resource.getAuthGroup(99L))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(exception -> assertThat(
                ((ResponseStatusException) exception).getStatusCode()
            ).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void rejectsEmptyFunctionAssociation() {
        assertThatThrownBy(() -> resource.aggiungiAssociazioneFunzione(1L, new AuthFunctionDTO[0]))
            .isInstanceOf(BadRequestAlertException.class)
            .satisfies(exception -> assertThat(
                ((BadRequestAlertException) exception).getProblemDetailWithCause().getTitle()
            ).isEqualTo("Selezionare almeno una funzione da associare al gruppo"));
    }

    @Test
    void rejectsDuplicateFunctionAssociation() {
        AuthFunctionDTO function = function(10L);
        when(functionService.listAllFunctionSelected(1L)).thenReturn(List.of(10L));

        assertThatThrownBy(() -> resource.aggiungiAssociazioneFunzione(1L, new AuthFunctionDTO[] {function}))
            .isInstanceOf(BadRequestAlertException.class)
            .satisfies(exception -> assertThat(
                ((BadRequestAlertException) exception).getProblemDetailWithCause().getTitle()
            ).isEqualTo("Funzione già associata al gruppo"));
    }

    @Test
    void associatesNewFunctions() throws Exception {
        AuthFunctionDTO function = function(10L);
        when(functionService.listAllFunctionSelected(1L)).thenReturn(List.of());

        ResponseEntity<Void> response = resource.aggiungiAssociazioneFunzione(1L, new AuthFunctionDTO[] {function});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(groupService).associaFunzioni(1L, new AuthFunctionDTO[] {function});
    }

    @Test
    void updatesVisibilityLevel() throws Exception {
        AuthGroupUpdateRequestBean[] updates = {new AuthGroupUpdateRequestBean()};

        ResponseEntity<Void> response = resource.aggiornaLivelloVisibilitaGruppi(updates);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(groupService).aggiornaLivelloVisibilitaGruppi(updates);
    }

    private AuthGroupDTO group(Long id, String nome) {
        AuthGroupDTO dto = new AuthGroupDTO();
        dto.setId(id);
        dto.setNome(nome);
        dto.setDescrizione("Description");
        dto.setLivelloVisibilita(1);
        return dto;
    }

    private AuthFunctionDTO function(Long id) {
        AuthFunctionDTO dto = new AuthFunctionDTO();
        dto.setId(id);
        dto.setNome("read");
        dto.setModulo("functions");
        dto.setDescrizione("Description");
        return dto;
    }
}
