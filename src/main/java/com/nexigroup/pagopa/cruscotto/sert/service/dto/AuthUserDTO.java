package com.nexigroup.pagopa.cruscotto.sert.service.dto;

import com.nexigroup.pagopa.cruscotto.sert.config.Constants;
import com.nexigroup.pagopa.cruscotto.sert.domain.AuthUser;
import com.nexigroup.pagopa.cruscotto.sert.domain.enumeration.AuthenticationType;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A DTO representing a user, with his authorities.
 */
@ToString
@EqualsAndHashCode
public class AuthUserDTO implements Serializable {

    private Long id;

    @NotBlank
    @Pattern(regexp = Constants.LOGIN_REGEX)
    @Size(min = 1, max = 50)
    private String login;

    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    @Email
    @Size(min = 5, max = 254)
    private String email;

    @Size(max = 256)
    private String imageUrl;

    private boolean activated = false;

    @Size(min = 2, max = 10)
    private String langKey;

    private String createdBy;

    private Instant createdDate;

    private String lastModifiedBy;

    private Instant lastModifiedDate;

    @NotNull
    private Long groupId;

    private String groupName;

    private Set<String> authorities;

    private LocalDate passwordExpiredDate;

    private boolean blocked = false;

    private boolean deleted = false;

    private AuthenticationType authenticationType;

    private ZonedDateTime deletedDate;

    public AuthUserDTO() {
        // Empty constructor needed for Jackson.
    }

    public AuthUserDTO(AuthUser authUser, Set<String> authorities) {
        this(authUser);
        this.authorities = authorities;
    }

    public AuthUserDTO(AuthUser authUser) {
        this.id = authUser.getId();
        this.login = authUser.getLogin();
        this.firstName = authUser.getFirstName();
        this.lastName = authUser.getLastName();
        this.email = authUser.getEmail();
        this.activated = authUser.getActivated();
        this.imageUrl = authUser.getImageUrl();
        this.langKey = authUser.getLangKey();
        this.createdBy = authUser.getCreatedBy();
        this.createdDate = authUser.getCreatedDate();
        this.lastModifiedBy = authUser.getLastModifiedBy();
        this.lastModifiedDate = authUser.getLastModifiedDate();
        this.blocked = authUser.isBlocked();
        this.deleted = authUser.isDeleted();
        this.deletedDate = authUser.getDeletedDate();
        this.authenticationType = authUser.getAuthenticationType();

        if (authUser.getGroup() != null) {
            this.groupId = authUser.getGroup().getId();
            this.groupName = authUser.getGroup().getNome();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public String getLangKey() {
        return langKey;
    }

    public void setLangKey(String langKey) {
        this.langKey = langKey;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Set<String> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<String> authorities) {
        this.authorities = authorities;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public LocalDate getPasswordExpiredDate() {
        return passwordExpiredDate;
    }

    public void setPasswordExpiredDate(LocalDate passwordExpiredDate) {
        this.passwordExpiredDate = passwordExpiredDate;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public AuthenticationType getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(AuthenticationType authenticationType) {
        this.authenticationType = authenticationType;
    }

    public ZonedDateTime getDeletedDate() {
        return deletedDate;
    }

    public void setDeletedDate(ZonedDateTime deletedDate) {
        this.deletedDate = deletedDate;
    }
}
