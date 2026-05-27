package com.nexigroup.pagopa.cruscotto.sert.service.mapper;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthGroup;
import com.nexigroup.pagopa.cruscotto.sert.domain.AuthUser;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthUserDTO;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Mapper for the entity {@link AuthUser} and its DTO called {@link AuthUserDTO}.
 *
 * Normal mappers are generated using MapStruct, this one is hand-coded as MapStruct
 * support is still in beta, and requires a manual step with an IDE.
 */
@Service
public class AuthUserMapper {

    public List<AuthUserDTO> authUsersToAuthUserDTOs(List<AuthUser> authUsers) {
        return authUsers.stream().filter(Objects::nonNull).map(this::authUserToAuthUserDTO).collect(Collectors.toList());
    }

    public AuthUserDTO authUserToAuthUserDTO(AuthUser authUser) {
        return new AuthUserDTO(authUser);
    }

    public List<AuthUser> authUserDTOsToAuthUsers(List<AuthUserDTO> authUserDTOs) {
        return authUserDTOs.stream().filter(Objects::nonNull).map(this::authUserDTOToAuthUser).collect(Collectors.toList());
    }

    public AuthUser authUserDTOToAuthUser(AuthUserDTO authUserDTO) {
        if (authUserDTO == null) {
            return null;
        } else {
            AuthUser authUser = new AuthUser();
            authUser.setId(authUserDTO.getId());
            authUser.setLogin(authUserDTO.getLogin());
            authUser.setFirstName(authUserDTO.getFirstName());
            authUser.setLastName(authUserDTO.getLastName());
            authUser.setEmail(authUserDTO.getEmail());
            authUser.setImageUrl(authUserDTO.getImageUrl());
            authUser.setActivated(authUserDTO.isActivated());
            authUser.setLangKey(authUserDTO.getLangKey());

            AuthGroup authGroup = new AuthGroup();
            authGroup.setId(authUserDTO.getGroupId());
            authUser.setGroup(authGroup);

            return authUser;
        }
    }

    public AuthUser authUserFromId(Long id) {
        if (id == null) {
            return null;
        }
        AuthUser authUser = new AuthUser();
        authUser.setId(id);
        return authUser;
    }
}
