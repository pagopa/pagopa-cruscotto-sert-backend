package com.nexigroup.pagopa.cruscotto.sert.web.rest.vm;

import com.nexigroup.pagopa.cruscotto.sert.service.dto.AuthUserDTO;

/**
 * View Model extending the AdminUserDTO, which is meant to be used in the user management UI.
 */
public class ManagedUserVM extends AuthUserDTO {

    public ManagedUserVM() {
        // Empty constructor needed for Jackson.
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ManagedUserVM{" + super.toString() + "} ";
    }
}
