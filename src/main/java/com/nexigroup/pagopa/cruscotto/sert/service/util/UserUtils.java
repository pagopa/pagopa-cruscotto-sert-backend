package com.nexigroup.pagopa.cruscotto.sert.service.util;

import org.springframework.stereotype.Service;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthUser;
import com.nexigroup.pagopa.cruscotto.sert.repository.online.AuthUserRepository;
import com.nexigroup.pagopa.cruscotto.sert.security.SecurityUtils;


@Service
public class UserUtils {

    private final AuthUserRepository authUserRepository;

    public UserUtils(AuthUserRepository authUserRepository) {
        this.authUserRepository = authUserRepository;
    }


    public AuthUser getLoggedUser() {
        String subJwt = SecurityUtils.getSubJwt();
        return authUserRepository.findOneBySub(subJwt).orElseThrow(() -> new RuntimeException("Current user login not found"));
    }

    public AuthUser getUserByID(Long id ) {

        return authUserRepository.getReferenceById(id);
    }


}
