
package br.com.contaremunerada.auth.service;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.time.Instant;

@Service
public class JwtService {

  private final String issuer;
  private static final Duration VALIDADE = Duration.ofMinutes(60);

  private final JwtEncoder jwtEncoder;

  public JwtService(JwtEncoder jwtEncoder, @Value("${security.jwt.issuer}") String issuer) {
    this.jwtEncoder = jwtEncoder;
    this.issuer = issuer;
  }

  public String gerarToken(Long contaId) {
    Instant agora = Instant.now();

    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer(issuer)
        .subject(contaId.toString())
        .issuedAt(agora)
        .expiresAt(agora.plus(VALIDADE))
        .build();

    JwsHeader header = JwsHeader.with(MacAlgorithm.HS256)
        .build();

    return jwtEncoder.encode(
        JwtEncoderParameters.from(header, claims)
    ).getTokenValue();
  }
}
