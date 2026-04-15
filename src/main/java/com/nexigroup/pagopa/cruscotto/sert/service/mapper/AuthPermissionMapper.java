package com.nexigroup.pagopa.cruscotto.sert.service.mapper;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthPermission;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthPermissionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for the entity {@link AuthPermission} and its DTO {@link AuthPermissionDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface AuthPermissionMapper extends EntityMapper<AuthPermissionDTO, AuthPermission> {
    @Mapping(target = "authFunctions", ignore = true)
    @Mapping(target = "removeAuthFunction", ignore = true)
    AuthPermission toEntity(AuthPermissionDTO authPermissionDTO);

    default AuthPermission fromId(Long id) {
        if (id == null) {
            return null;
        }
        AuthPermission authPermission = new AuthPermission();
        authPermission.setId(id);
        return authPermission;
    }
}
