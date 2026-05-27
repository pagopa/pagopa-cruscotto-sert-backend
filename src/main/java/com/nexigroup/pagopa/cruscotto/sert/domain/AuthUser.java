package com.nexigroup.pagopa.cruscotto.sert.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.nexigroup.pagopa.cruscotto.sert.config.Constants;
import com.nexigroup.pagopa.cruscotto.sert.domain.enumeration.AuthenticationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A user.
 */
@Entity
@Table(name = "AUTH_USER")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class AuthUser extends AbstractAuditingEntity<Long> implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int DEFAULT_DAY_PASSWORD_EXPIRED = Integer.MAX_VALUE;

    @Id
    @Column(name = "CO_ID")
    @SequenceGenerator(name = "SQCRUSC8_AUTHUSER", sequenceName = "SQCRUSC8_AUTHUSER", allocationSize = 1)
    @GeneratedValue(generator = "SQCRUSC8_AUTHUSER", strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotNull
    @Pattern(regexp = Constants.LOGIN_REGEX)
    @Size(min = 1, max = 50)
    @Column(name = "TE_LOGIN", length = 50, unique = true, nullable = false)
    private String login;

    @JsonIgnore
    @NotNull
    @Size(min = 10, max = 60)
    @Column(name = "TE_PASSWORD_HASH", length = 60)
    private String password;

    @Size(max = 50)
    @Column(name = "NM_FIRST_NAME", length = 50)
    private String firstName;

    @Size(max = 50)
    @Column(name = "NM_LAST_NAME", length = 50)
    private String lastName;

    @Email
    @Size(min = 5, max = 254)
    @Column(name = "TE_EMAIL", length = 254, unique = true)
    private String email;

    @NotNull
    @Column(name = "FL_ACTIVATED", nullable = false)
    private boolean activated = false;

    @Size(min = 2, max = 10)
    @Column(name = "TE_LANG_KEY", length = 10)
    private String langKey;

    @Size(max = 256)
    @Column(name = "TE_IMAGE_URL", length = 256)
    private String imageUrl;

    @Size(max = 20)
    @Column(name = "TE_ACTIVATION_KEY", length = 20)
    @JsonIgnore
    private String activationKey;

    @Size(max = 20)
    @Column(name = "TE_RESET_KEY", length = 20)
    @JsonIgnore
    private String resetKey;

    @Column(name = "DT_RESET_DATE")
    private Instant resetDate = null;

    @Column(name = "CO_FAILED_LOGIN_ATTEMPTS")
    private Integer failedLoginAttempts;

    @Column(name = "DT_LAST_PWD_CHANGE_DATE")
    private ZonedDateTime lastPasswordChangeDate = null;

    @Column(name = "CO_PWD_EXPIRED_DAY")
    private Integer passwordExpiredDay = AuthUser.DEFAULT_DAY_PASSWORD_EXPIRED;

    @NotNull
    @Column(name = "FL_BLOCKED", nullable = false)
    private boolean blocked = false;

    @Column(name = "FL_DELETED", nullable = false)
    private boolean deleted;

    @Column(name = "DT_DELETED_DATE")
    private ZonedDateTime deletedDate = null;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "TE_AUTHENTICATION_TYPE")
    private AuthenticationType authenticationType;

    @ManyToOne
    @JoinColumn(name = "CO_AUTH_GROUP_ID")
    @JsonIgnoreProperties("users")
    private AuthGroup group;

    @OneToMany(mappedBy = "authUser", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<AuthUserHistory> authUserHistory = new HashSet<>();

    @Size(max = 100)
    @Column(name = "TE_SUB", length = 100, unique = true)
    private String sub;

    @Size(max = 100)
    @Column(name = "TE_OID", length = 100)
    private String oid;

    @Size(max = 50)
    @Column(name = "TE_SOURCE", length = 50)
    private String source;

    @Column(name = "DT_LAST_LOGIN_AT")
    private Instant lastLoginAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    // Lowercase the login before saving it in database
    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public boolean getActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public String getActivationKey() {
        return activationKey;
    }

    public void setActivationKey(String activationKey) {
        this.activationKey = activationKey;
    }

    public String getResetKey() {
        return resetKey;
    }

    public void setResetKey(String resetKey) {
        this.resetKey = resetKey;
    }

    public Instant getResetDate() {
        return resetDate;
    }

    public void setResetDate(Instant resetDate) {
        this.resetDate = resetDate;
    }

    public String getLangKey() {
        return langKey;
    }

    public void setLangKey(String langKey) {
        this.langKey = langKey;
    }

    public AuthGroup getGroup() {
        return group;
    }

    public void setGroup(AuthGroup group) {
        this.group = group;
    }

    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(Integer failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public ZonedDateTime getLastPasswordChangeDate() {
        return lastPasswordChangeDate;
    }

    public void setLastPasswordChangeDate(ZonedDateTime lastPasswordChangeDate) {
        this.lastPasswordChangeDate = lastPasswordChangeDate;
    }

    public Integer getPasswordExpiredDay() {
        return passwordExpiredDay;
    }

    public void setPasswordExpiredDay(Integer passwordExpiredDay) {
        this.passwordExpiredDay = passwordExpiredDay;
    }

    public ZonedDateTime getDeletedDate() {
        return deletedDate;
    }

    public void setDeletedDate(ZonedDateTime deletedDate) {
        this.deletedDate = deletedDate;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public Set<AuthUserHistory> getAuthUserHistory() {
        return authUserHistory;
    }

    public AuthenticationType getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(AuthenticationType authenticationType) {
        this.authenticationType = authenticationType;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public void setAuthUserHistory(Set<AuthUserHistory> authUserHistory) {
        this.authUserHistory = authUserHistory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuthUser)) {
            return false;
        }
        return id != null && id.equals(((AuthUser) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "AuthUser{" +
            "id=" + id +
            ", login='" + login + '\'' +
            ", password='" + password + '\'' +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", email='" + email + '\'' +
            ", activated=" + activated +
            ", langKey='" + langKey + '\'' +
            ", imageUrl='" + imageUrl + '\'' +
            ", activationKey='" + activationKey + '\'' +
            ", resetKey='" + resetKey + '\'' +
            ", resetDate=" + resetDate +
            ", failedLoginAttempts=" + failedLoginAttempts +
            ", lastPasswordChangeDate=" + lastPasswordChangeDate +
            ", passwordExpiredDay=" + passwordExpiredDay +
            ", blocked=" + blocked +
            ", deleted=" + deleted +
            ", deletedDate=" + deletedDate +
            ", authenticationType=" + authenticationType +
            ", group=" + group +
            ", authUserHistory=" + authUserHistory +
            ", sub='" + sub + '\'' +
            ", oid='" + oid + '\'' +
            ", source='" + source + '\'' +
            ", lastLoginAt=" + lastLoginAt +
            '}';
    }
}
