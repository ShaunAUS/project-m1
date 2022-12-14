package com.example.testSecurity.jwt;

import com.example.testSecurity.Enum.RoleType;
import com.example.testSecurity.config.AppProperties;
import com.example.testSecurity.entity.Member;
import com.example.testSecurity.exception.enums.ServiceMessage;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtSigningKeyResolver signingKeyResolver;
    private final AppProperties properties;


    public static Credential getCredential(Authentication authentication) {
        if (Optional.ofNullable(authentication).isEmpty()) {
            throw new AuthenticationServiceException(ServiceMessage.NOT_AUTHORIZED.getMessage());
        }
        // cast authentication.getCredentials() as Credential
        Credential credentials = (Credential) authentication.getCredentials();
        if (Optional.ofNullable(credentials).isEmpty()) {
            throw new AuthenticationCredentialsNotFoundException(
                ServiceMessage.NOT_AUTHORIZED.getMessage());
        }
        return credentials;
    }


    public String getJwtToken(Member member, Long memberAccessId) {

        return Jwts.builder()
            //header
            .setHeaderParam("typ", "JWT")
            .setHeaderParam("alg", "HS256")

            //payload
            .setSubject(AppProperties.LOGIN_SERVICE)
            .setAudience(member.getUserName())
            .claim(AppProperties.ACCESS_ID, memberAccessId)  // accessId
            .claim("name", member.getUserName())
            .claim("password", member.getPassword())
            //.claim("roles", Arrays.asList(RoleType.valueOf(member.getRoleType())))
            .claim("roles", Arrays.asList(RoleType.valueOf(member.getRoleType())))
            .setExpiration(new Date(
                System.currentTimeMillis() + 60 * properties.getAccessHoldTimeMillis()))  // 15???

            //SecretKey or privateKey ??? ?????? ???????????? ??????
            //publicKey ??? ???????????? x
            .signWith(signingKeyResolver.getMemberAuthKey())
            .compact();
    }

    //????????????
    public Jws<Claims> parsingToken(@NonNull String token, long allowedClockSkewSeconds) {
        return Jwts.parserBuilder()
            .setSigningKeyResolver(signingKeyResolver) // SecretKey or privateKey ????????????
            .setAllowedClockSkewSeconds(allowedClockSkewSeconds)
            .build()
            .parseClaimsJws(token);
    }


    //JWT ?????? ?????? -> claim ?????? (MemberAccessId)
    public long getAccessId(String token) {
        final Jws<Claims> claimsJws = parsingToken(token,
            (120 - properties.getAccessHoldTime()) * 60); //?????? ??????
        return claimsJws.getBody()
            .get(properties.SESSION_ID, Long.class); // claim?????? ???????????? ?????? ?????? ???????????? ??????
    }


    //???????????? ????????? ?????? ??????
    //DB?????? ??????????????? ??????????????? UserDetails ???????????? ????????? UsernamePasswordAuthenticationToken ??????(??????,??????,??????)
    // => Authentication???????????? -> SecurityContextHolder??? ?????? ->????????? ?????? =>????????????
    public Authentication getAuthentication(String token) {

        Jws<Claims> claimsJws = null;

        try {
            claimsJws = parsingToken(token, properties.getAccessHoldTime() * 60l);
        } catch (ExpiredJwtException e) {
            //?????? ?????? ????????? ?????? ??????
            Claims claims = e.getClaims();
            UserDetails expiredUser = User.withUsername(claims.getAudience())
                .accountExpired(true)
                .disabled(true)
                .password("")
                .roles()
                .build();

            //??????,?????? ??????
            return new UsernamePasswordAuthenticationToken(expiredUser, null,
                Collections.emptyList());

        } catch (JwtException e) {

            //????????? ?????? ??????, ?????? ????????????
            UserDetails invalidUser = User.withUsername("invalidUser")
                .disabled(true)
                .password("")
                .roles()
                .build();
            //??????,?????? ??????
            return new UsernamePasswordAuthenticationToken(invalidUser, null,
                Collections.emptyList());
        }

        //JWT ?????? ?????????
        //15??? ???????????? ????????? ?????? ???????????? role??? ?????? ????????????
        final Long sessionId = claimsJws.getBody()
            .get(AppProperties.ACCESS_ID, Long.class);   //claim ??? ????????? ?????? ?????? ???????????? ??????
        String audience = claimsJws.getBody().getAudience();

        switch (claimsJws.getBody().getSubject()) {

            //subject
            case AppProperties.LOGIN_SERVICE: {
                ArrayList<String> roles = claimsJws.getBody()
                    .get("roles", ArrayList.class);  // jwt - ??????claim ??????

                UserDetails manager = User.withUsername(audience + ":" + sessionId)
                    //TODO roles.toArray(new String[roles.size()]) - entity ??????????????? ??????
                    .roles(roles.toArray(new String[roles.size()]))
                    .password("")
                    .build();

                //??????,?????? ??????
                return new UsernamePasswordAuthenticationToken(manager, null,
                    manager.getAuthorities());
            }

            default:
                throw new IllegalStateException();
        }
    }

    @Builder
    public static class Credential {

        public String token;
        public AuthenticationUser user;
        public Jws<Claims> claims;
    }


}
