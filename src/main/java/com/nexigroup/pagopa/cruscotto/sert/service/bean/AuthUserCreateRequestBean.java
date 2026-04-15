package com.nexigroup.pagopa.cruscotto.sert.service.bean;

import com.nexigroup.pagopa.cruscotto.sert.config.Constants;
import com.nexigroup.pagopa.cruscotto.sert.service.validation.ValidAuthGroup;
import jakarta.validation.constraints.*;

public class AuthUserCreateRequestBean {

    @NotBlank
    @Pattern(regexp = Constants.LOGIN_REGEX)
    @Size(max = 50)
    private String login;

    @NotBlank
    @Size(max = 50)
    private String firstName;

    @NotBlank
    @Size(max = 50)
    private String lastName;

    @Email
    @Size(min = 5, max = 254)
    private String email;

    private String password;

    @Size(min = 2, max = 10)
    private String langKey;

    @NotNull
    @Digits(integer = 19, fraction = 0)
    @ValidAuthGroup
    private Long groupId;

    public AuthUserCreateRequestBean() {
        // Empty constructor needed for Jackson.
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLangKey() {
        return langKey;
    }

    public void setLangKey(String langKey) {
        this.langKey = langKey;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    @Override
    public String toString() {
        return (
            "UserDTO{" +
            "login='" +
            login +
            '\'' +
            ", firstName='" +
            firstName +
            '\'' +
            ", lastName='" +
            lastName +
            '\'' +
            ", email='" +
            email +
            '\'' +
            ", langKey='" +
            langKey +
            '\'' +
            ", groupId=" +
            groupId +
            "}"
        );
    }
}
