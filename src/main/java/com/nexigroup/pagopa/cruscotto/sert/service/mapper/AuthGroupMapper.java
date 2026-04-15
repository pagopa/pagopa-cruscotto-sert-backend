package com.nexigroup.pagopa.cruscotto.sert.service.mapper;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthGroup;
import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthGroupDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for the entity {@link AuthGroup} and its DTO {@link AuthGroupDTO}.
 */
@Mapper(componentModel = "spring", uses = { AuthFunctionMapper.class, AuthUserMapper.class })
public interface AuthGroupMapper extends EntityMapper<AuthGroupDTO, AuthGroup> {
    @Mapping(target = "removeAuthUser", ignore = true)
    @Mapping(target = "authUsers", ignore = true)
    @Mapping(target = "removeAuthFunction", ignore = true)
    AuthGroup toEntity(AuthGroupDTO authGroupDTO);

    AuthGroupDTO toDto(AuthGroup authGroup);

    default AuthGroup fromId(Long id) {
        if (id == null) {
            return null;
        }
        AuthGroup authGroup = new AuthGroup();
        authGroup.setId(id);
        return authGroup;
    }
}
