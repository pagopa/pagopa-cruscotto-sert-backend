package com.nexigroup.pagopa.cruscotto.sert.service.mapper;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthFunction;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthFunctionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for the entity {@link AuthFunction} and its DTO {@link AuthFunctionDTO}.
 */
@Mapper(componentModel = "spring", uses = { AuthPermissionMapper.class })
public interface AuthFunctionMapper extends EntityMapper<AuthFunctionDTO, AuthFunction> {
    @Mapping(target = "removeAuthPermission", ignore = true)
    @Mapping(target = "authGroups", ignore = true)
    @Mapping(target = "removeAuthGroup", ignore = true)
    AuthFunction toEntity(AuthFunctionDTO authFunctionDTO);

    @Mapping(target = "selected", ignore = true)
    @Mapping(target = "groupId", ignore = true)
    AuthFunctionDTO toDto(AuthFunction authFunction);

    default AuthFunction fromId(Long id) {
        if (id == null) {
            return null;
        }
        AuthFunction authFunction = new AuthFunction();
        authFunction.setId(id);
        return authFunction;
    }
}
