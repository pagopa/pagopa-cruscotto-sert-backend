package com.nexigroup.pagopa.cruscotto.sert.security.jwt;


import com.nexigroup.pagopa.cruscotto.sert.config.Constants;
import com.nexigroup.pagopa.cruscotto.sert.security.GrantAuthoritiesLoad;
import com.nexigroup.pagopa.cruscotto.sert.security.oauth2.JwtInvalid;
import java.security.Key;
import java.time.Instant;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.jwt.*;

//@Component
public class TokenProvider {

    private final Logger log = LoggerFactory.getLogger(TokenProvider.class);

    private final JwtEncoder jwtEncoder;

    private final JwtDecoder jwtDecoder;


    private final GrantAuthoritiesLoad grantAuthoritiesLoad;

    private Key key;

    @Value("${jhipster.security.authentication.jwt.token-validity-in-seconds:0}")
    private long tokenValidityInSeconds;

    @Value("${jhipster.security.authentication.jwt.token-validity-in-seconds-for-remember-me:0}")
    private long tokenValidityInSecondsForRememberMe;

    public TokenProvider(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, GrantAuthoritiesLoad grantAuthoritiesLoad) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.grantAuthoritiesLoad = grantAuthoritiesLoad;
    }



    public Authentication getAuthentication(String token) {
        Map<String, Object> claims = jwtDecoder.decode(token).getClaims();

        Instant iat = Instant.ofEpochSecond(Long.parseLong(claims.get("iat").toString()));

        Collection<? extends GrantedAuthority> authorities = grantAuthoritiesLoad.load(
            claims,
            claims.get("sub").toString(),
            String.valueOf(iat.getEpochSecond()),
            Constants.FORM_LOGIN
        );

        User principal = new User(claims.get("sub").toString(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public boolean validateToken(String authToken) throws JwtInvalid {
        try {
            jwtDecoder.decode(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.info("Invalid JWT token.");
            log.trace("Invalid JWT token trace.", e);
        }
        return false;
    }
}
