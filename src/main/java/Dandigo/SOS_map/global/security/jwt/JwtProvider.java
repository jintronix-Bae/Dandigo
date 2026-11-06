package Dandigo.SOS_map.global.security.jwt;

import Dandigo.SOS_map.domain.user.entity.Role;
import Dandigo.SOS_map.global.security.AuthUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JwtProvider {

    private static final String CLAIM_LOGIN_ID = "loginId";
    private static final String CLAIM_ROLE = "role";

    private final SecretKey secretKey;
    @Getter
    private final Long accessTokenExpiration;

    public JwtProvider(@Value("${jwt.secret}")String secretKey, @Value("${jwt.access-token-expiration}") Long accessTokenExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
    }

    // #1.Access Token 생성
    public String generateAccessToken(Long userId, String loginId, Role role){
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_LOGIN_ID, loginId)
                .claim(CLAIM_ROLE, role.name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenExpiration))
                .signWith(secretKey)
                .compact();
    }

    // #2.토큰 Claims 파싱 메서드
    private Claims parseClaims(String token){
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // #3.토큰에서 인증 정보 추출
    public Authentication getAuthentication(String token){
        Claims claims = parseClaims(token);
        String role = claims.get(CLAIM_ROLE, String.class);
        if (role == null || role.isEmpty()){
            throw new IllegalArgumentException("권한 정보가 없는 토큰입니다.");
        }
        AuthUser principal = new AuthUser(Long.valueOf(claims.getSubject()), claims.get(CLAIM_LOGIN_ID, String.class));
        return new UsernamePasswordAuthenticationToken(principal, null, List.of(new SimpleGrantedAuthority(role)));
    }

    // #4.토큰 유효성 검증(서명 오류, 만료, 형식 오류인 경우 false 반환)
    public boolean validateToken(String token){
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
