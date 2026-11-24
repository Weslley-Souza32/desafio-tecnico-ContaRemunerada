
package br.com.contaremunerada.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

  private JwtService jwtService;
  private JwtDecoder jwtDecoder;
  private JwtEncoder jwtEncoder;
  private static final String ISSUER =
      "https://conta-remunerada.local";

  @BeforeEach
  void configurar() {
    byte[] bytes = new byte[32];
    new SecureRandom().nextBytes(bytes);

    SecretKey chave = new SecretKeySpec(
        bytes,
        "HmacSHA256"
    );

    jwtEncoder = NimbusJwtEncoder
        .withSecretKey(chave)
        .algorithm(MacAlgorithm.HS256)
        .build();

    NimbusJwtDecoder decoder = NimbusJwtDecoder
        .withSecretKey(chave)
        .macAlgorithm(MacAlgorithm.HS256)
        .build();

    decoder.setJwtValidator(
        JwtValidators.createDefaultWithIssuer(ISSUER)
    );

    jwtDecoder = decoder;

    jwtService = new JwtService(jwtEncoder, ISSUER);
  }

  @Test
  void deveGerarTokenComIdentificacaoDaConta() {
    String token = jwtService.gerarToken(5L);

    Jwt jwt = jwtDecoder.decode(token);

    assertEquals("5", jwt.getSubject());
    assertEquals(ISSUER, jwt.getIssuer().toString());
    assertEquals("HS256", jwt.getHeaders().get("alg"));
  }

  @Test
  void deveGerarTokenComValidadeDeSessentaMinutos() {
    String token = jwtService.gerarToken(5L);

    Jwt jwt = jwtDecoder.decode(token);

    assertNotNull(jwt.getIssuedAt());
    assertNotNull(jwt.getExpiresAt());

    Duration validade = Duration.between(
        jwt.getIssuedAt(),
        jwt.getExpiresAt()
    );

    assertEquals(Duration.ofMinutes(60), validade);
  }

  @Test
  void deveRejeitarTokenAssinadoComOutraChave() {
    String token = jwtService.gerarToken(5L);

    byte[] outrosBytes = new byte[32];
    new SecureRandom().nextBytes(outrosBytes);

    SecretKey outraChave = new SecretKeySpec(
        outrosBytes,
        "HmacSHA256"
    );

    JwtDecoder outroDecoder = NimbusJwtDecoder
        .withSecretKey(outraChave)
        .macAlgorithm(MacAlgorithm.HS256)
        .build();

    assertThrows(
        JwtException.class,
        () -> outroDecoder.decode(token)
    );
  }


  @Test
  void deveRejeitarTokenExpirado() {
    // Arrange
    Instant agora = Instant.now();

    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer(ISSUER)
        .subject("5")
        .issuedAt(agora.minusSeconds(180))
        .expiresAt(agora.minusSeconds(120))
        .build();

    JwsHeader header = JwsHeader
        .with(MacAlgorithm.HS256)
        .build();

    String tokenExpirado = jwtEncoder.encode(
        JwtEncoderParameters.from(header, claims)
    ).getTokenValue();

    // Act + Assert
    assertThrows(
        JwtException.class,
        () -> jwtDecoder.decode(tokenExpirado)
    );
  }


  @Test
  void deveRejeitarTokenDeOutroEmissor() {
    // Arrange
    Instant agora = Instant.now();

    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer("https://outro-sistema.local")
        .subject("5")
        .issuedAt(agora)
        .expiresAt(agora.plusSeconds(3600))
        .build();

    JwsHeader header = JwsHeader
        .with(MacAlgorithm.HS256)
        .build();

    String token = jwtEncoder.encode(
        JwtEncoderParameters.from(header, claims)
    ).getTokenValue();

    // Act + Assert
    assertThrows(
        JwtException.class,
        () -> jwtDecoder.decode(token)
    );
  }
}
