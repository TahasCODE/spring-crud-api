package com.example.CRUD.security;

import com.example.CRUD.Entity.Person;
import com.example.CRUD.config.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;

    public String generateToken(Person person, String role) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("order-api")
                .issuedAt(now)
                .expiresAt(now.plus(properties.expirationMinutes(), ChronoUnit.MINUTES))
                .subject(String.valueOf(person.getId()))   // who the token belongs to
                .claim("email", person.getEmail())
                .claim("role", role)                        // used by authorization later
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long expiresInSeconds() {
        return properties.expirationMinutes() * 60;
    }
}