package com.futureprograms.MyIkea.Security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class JwtService {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    @Value("${app.security.jwt.access.secret:${JWT_SECRET:change-this-secret-in-production-change-this-secret}}")
    private String accessJwtSecret;

    @Value("${app.security.jwt.refresh.secret:${app.security.jwt.access.secret:${JWT_SECRET:change-this-secret-in-production-change-this-secret}}}")
    private String refreshJwtSecret;

    @Value("${app.security.jwt.access.expiration-seconds:${JWT_EXPIRATION_SECONDS:900}}")
    private long accessJwtExpirationSeconds;

    @Value("${app.security.jwt.refresh.expiration-seconds:604800}")
    private long refreshJwtExpirationSeconds;

    @Value("${app.security.jwt.access.cookie-name:${JWT_COOKIE_NAME:MYIKEA_AUTH_TOKEN}}")
    private String accessCookieName;

    @Value("${app.security.jwt.refresh.cookie-name:MYIKEA_REFRESH_TOKEN}")
    private String refreshCookieName;

    @Value("${app.security.jwt.cookie-secure:false}")
    private boolean jwtCookieSecure;

    @Value("${app.security.jwt.cookie-same-site:Lax}")
    private String jwtCookieSameSite;

    public String generateAccessToken(Authentication authentication) {
        return generateToken(authentication, TOKEN_TYPE_ACCESS, accessJwtExpirationSeconds, accessJwtSecret);
    }

    public String generateRefreshToken(Authentication authentication) {
        return generateToken(authentication, TOKEN_TYPE_REFRESH, refreshJwtExpirationSeconds, refreshJwtSecret);
    }

    private String generateToken(Authentication authentication, String tokenType, long expirationSeconds, String secret) {
        try {
            Instant now = Instant.now();
            Instant exp = now.plusSeconds(expirationSeconds);

            Map<String, Object> header = new LinkedHashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sub", authentication.getName());
            payload.put("iat", now.getEpochSecond());
            payload.put("exp", exp.getEpochSecond());
            payload.put("token_type", tokenType);
            payload.put("jti", UUID.randomUUID().toString());
            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            payload.put("roles", roles);

            String encodedHeader = base64UrlEncode(OBJECT_MAPPER.writeValueAsBytes(header));
            String encodedPayload = base64UrlEncode(OBJECT_MAPPER.writeValueAsBytes(payload));
            String unsignedToken = encodedHeader + "." + encodedPayload;
            String signature = sign(unsignedToken, secret);

            return unsignedToken + "." + signature;
        } catch (Exception ex) {
            throw new IllegalStateException("Error creating JWT token", ex);
        }
    }

    public void attachAuthCookies(HttpServletResponse response, Authentication authentication) {
        String accessToken = generateAccessToken(authentication);
        String refreshToken = generateRefreshToken(authentication);

        addCookie(response, accessCookieName, accessToken, accessJwtExpirationSeconds);
        addCookie(response, refreshCookieName, refreshToken, refreshJwtExpirationSeconds);
    }

    public void clearAuthCookies(HttpServletResponse response) {
        addCookie(response, accessCookieName, "", 0);
        addCookie(response, refreshCookieName, "", 0);
    }

    public Optional<String> extractUsernameFromAccessToken(String token) {
        Map<String, Object> payload = parseAndValidate(token, TOKEN_TYPE_ACCESS, accessJwtSecret);
        Object sub = payload.get("sub");
        return sub == null ? Optional.empty() : Optional.of(sub.toString());
    }

    public Optional<String> extractUsernameFromRefreshToken(String token) {
        Map<String, Object> payload = parseAndValidate(token, TOKEN_TYPE_REFRESH, refreshJwtSecret);
        Object sub = payload.get("sub");
        return sub == null ? Optional.empty() : Optional.of(sub.toString());
    }

    public boolean isAccessTokenValid(String token, String expectedUsername) {
        Optional<String> username = extractUsernameFromAccessToken(token);
        return username.isPresent() && Objects.equals(username.get(), expectedUsername);
    }

    public boolean isRefreshTokenValid(String token, String expectedUsername) {
        Optional<String> username = extractUsernameFromRefreshToken(token);
        return username.isPresent() && Objects.equals(username.get(), expectedUsername);
    }

    public String getAccessCookieName() {
        return accessCookieName;
    }

    public Optional<String> resolveCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return Optional.of(cookie.getValue());
            }
        }
        return Optional.empty();
    }

    public Optional<String> resolveAccessToken(HttpServletRequest request) {
        return resolveCookieValue(request, accessCookieName);
    }

    public Optional<String> resolveRefreshToken(HttpServletRequest request) {
        return resolveCookieValue(request, refreshCookieName);
    }

    private Map<String, Object> parseAndValidate(String token, String expectedType, String secret) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT format");
            }

            String unsignedToken = parts[0] + "." + parts[1];
            String expectedSignature = sign(unsignedToken, secret);
            if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8),
                    parts[2].getBytes(StandardCharsets.UTF_8))) {
                throw new IllegalArgumentException("Invalid JWT signature");
            }

            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            Map<String, Object> payload = OBJECT_MAPPER.readValue(payloadBytes, new TypeReference<>() {});

            long exp = longValue(payload.get("exp"));
            if (Instant.now().getEpochSecond() >= exp) {
                throw new IllegalArgumentException("JWT expired");
            }

            String tokenType = String.valueOf(payload.get("token_type"));
            if (!expectedType.equals(tokenType)) {
                throw new IllegalArgumentException("Unexpected token type");
            }

            return payload;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid JWT token", ex);
        }
    }

    private String sign(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKey);
        byte[] signature = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return base64UrlEncode(signature);
    }

    private void addCookie(HttpServletResponse response, String name, String value, long maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(jwtCookieSecure)
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite(jwtCookieSameSite)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private long longValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }
}
