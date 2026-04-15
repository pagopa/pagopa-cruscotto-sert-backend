package com.nexigroup.pagopa.cruscotto.sert.service.validation;

import com.nexigroup.pagopa.cruscotto.sert.service.impl.AuthUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserResourcePermissionValidator {

    private static final String PROBLEMI_IN_FASE_DI_VERIFICA_CREDENZIALI = "Problemi in fase di verifica credenziali ";

    private final Logger logger = LoggerFactory.getLogger(UserResourcePermissionValidator.class);

    private final AuthUserService authUserService;

    public UserResourcePermissionValidator(AuthUserService authUserService) {
        this.authUserService = authUserService;
    }

    public boolean userCanAccessIdUserResource(String userLoginLogged, Long userId) {
        return !authUserService.getAbilitazioneUtenteLoggatoManageOtherUser(userLoginLogged, userId, null);
    }

    public boolean userCanAccessUserResource(String userLoginLogged, String userLogin) {
        return !authUserService.getAbilitazioneUtenteLoggatoManageOtherUser(userLoginLogged, null, userLogin);
    }
}
