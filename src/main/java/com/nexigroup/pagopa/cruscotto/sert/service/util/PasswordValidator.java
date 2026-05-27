package com.nexigroup.pagopa.cruscotto.sert.service.util;

import com.nexigroup.pagopa.cruscotto.sert.domain.AuthUser;
import com.nexigroup.pagopa.cruscotto.sert.service.impl.AuthUserHistoryService;
import com.nexigroup.pagopa.cruscotto.sert.service.impl.AuthUserService;
import com.nexigroup.pagopa.cruscotto.sert.service.exception.InvalidPasswordException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.passay.*;
import org.passay.dictionary.ArrayWordList;
import org.passay.dictionary.WordListDictionary;
import org.passay.dictionary.sort.ArraysSort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public final class PasswordValidator {

    private final Logger log = LoggerFactory.getLogger(PasswordValidator.class);

    private final PasswordEncoder passwordEncoder;

    private final AuthUserService authUserService;

    private final AuthUserHistoryService authUserHistoryService;

    private static final Integer MIN_LENGTH = 8;

    private static final Integer MAX_LENGTH = 100;

    private static final Integer PREV_PASSWORDS = 6;

    private final LengthRule ruleLenght = new LengthRule(MIN_LENGTH, MAX_LENGTH);

    private final CharacterCharacteristicsRule charRules = new CharacterCharacteristicsRule();

    private final CharacterCharacteristicsRule charRules2 = new CharacterCharacteristicsRule();

    private final WhitespaceRule noWhite = new WhitespaceRule();

    public PasswordValidator(
        PasswordEncoder passwordEncoder,
        AuthUserService authUserService,
        AuthUserHistoryService authUserHistoryService
    ) {
        this.passwordEncoder = passwordEncoder;
        this.authUserService = authUserService;
        this.authUserHistoryService = authUserHistoryService;
        init();
    }

    private void init() {
        charRules.setNumberOfCharacteristics(3);
        charRules.getRules().add(new CharacterRule(EnglishCharacterData.UpperCase, 1));
        charRules.getRules().add(new CharacterRule(EnglishCharacterData.LowerCase, 1));
        charRules.getRules().add(new CharacterRule(EnglishCharacterData.Digit, 1));
        charRules.getRules().add(new CharacterRule(EnglishCharacterData.Special, 1));

        charRules2.setNumberOfCharacteristics(2);
        charRules2.getRules().add(new CharacterRule(EnglishCharacterData.Alphabetical, 1));
        charRules2.getRules().add(new CharacterRule(EnglishCharacterData.Digit, 1));
    }

    public void check(String username, String password, String firstName, String lastName) {
        List<String> oldPasswords = new ArrayList<>();

        AuthUser authUser = authUserService.findUserByLogin(username).orElse(null);

        // aggiungo il controllo sulle vecchie password e sull'ultima in uso
        if (authUser != null) {
            int pwdLoad = PREV_PASSWORDS;

            //recupero le vecchie eventuali password
            if (pwdLoad > 0) {
                String[] historyPassword = authUserHistoryService.getOldPassword(authUser.getId(), pwdLoad);
                if (!ArrayUtils.isEmpty(historyPassword)) oldPasswords.addAll(Arrays.asList(historyPassword));
            }

            oldPasswords.add(authUser.getPassword());
        }

        String[] words = new String[] { username, firstName, lastName };

        DictionarySubstringRule rulesDictionary = new DictionarySubstringRule(
            new WordListDictionary(new ArrayWordList(words, false, new ArraysSort()))
        );

        //controllo che la password non sia stata utilizzata in precedenza
        for (String oldPassword : oldPasswords) {
            if (passwordEncoder.matches(password, oldPassword)) {
                log.info("Password utilizzata in precedenza");

                throw new InvalidPasswordException();
            }
        }

        org.passay.PasswordValidator validator = new org.passay.PasswordValidator(
            ruleLenght,
            charRules,
            noWhite,
            charRules2,
            rulesDictionary
        );

        PasswordData passwordData = new PasswordData(password);

        RuleResult result = validator.validate(passwordData);

        if (!result.isValid()) {
            log.info("Invalid password:");
            for (String msg : validator.getMessages(result)) {
                log.error(msg);
            }

            throw new InvalidPasswordException();
        }
    }
}
