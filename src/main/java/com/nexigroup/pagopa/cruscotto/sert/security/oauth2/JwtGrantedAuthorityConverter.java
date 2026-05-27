package com.nexigroup.pagopa.cruscotto.sert.security.oauth2;

import com.nexigroup.pagopa.cruscotto.sert.config.Constants;
import com.nexigroup.pagopa.cruscotto.sert.security.GrantAuthoritiesLoad;
import com.nexigroup.pagopa.cruscotto.sert.security.SecurityUtils;
import java.util.Collection;
import java.util.Objects;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class JwtGrantedAuthorityConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final GrantAuthoritiesLoad grantAuthoritiesLoad;

    public JwtGrantedAuthorityConverter(GrantAuthoritiesLoad grantAuthoritiesLoad) {
        this.grantAuthoritiesLoad = grantAuthoritiesLoad;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt source) {
        Collection<GrantedAuthority> grantedAuthorities = SecurityUtils.extractAuthorityFromClaims(source.getClaims());

        if (grantedAuthorities != null) {
            grantedAuthorities.clear();
            grantedAuthorities.addAll(
                grantAuthoritiesLoad.load(
                    source.getClaims(),
                    source.getSubject(),
                    String.valueOf(Objects.requireNonNull(source.getIssuedAt()).getEpochSecond()),
                    Constants.FORM_LOGIN
                )
            );
        }

        return new JwtAuthenticationToken(source, grantedAuthorities);
    }
}
