
package br.com.contaremunerada.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {

  @Bean
  public SecretKey jwtSecretKey(
      @Value("${security.jwt.secret-base64}")
      String secretBase64
  ) {
    byte[] chave;

    try {
      chave = Base64.getDecoder().decode(secretBase64);
    } catch (IllegalArgumentException exception) {
      throw new IllegalStateException(
          "A chave JWT deve estar codificada em Base64.",
          exception
      );
    }

    if (chave.length < 32) {
      throw new IllegalStateException(
          "A chave JWT deve possuir pelo menos 256 bits."
      );
    }

    return new SecretKeySpec(chave, "HmacSHA256");
  }

  @Bean
  public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
    return NimbusJwtEncoder
        .withSecretKey(jwtSecretKey)
        .algorithm(MacAlgorithm.HS256)
        .build();
  }

  @Bean
  public JwtDecoder jwtDecoder(
      SecretKey jwtSecretKey,
      @Value("${security.jwt.issuer}") String issuer
  ) {
    NimbusJwtDecoder decoder = NimbusJwtDecoder
        .withSecretKey(jwtSecretKey)
        .macAlgorithm(MacAlgorithm.HS256)
        .build();

    decoder.setJwtValidator(
        JwtValidators.createDefaultWithIssuer(issuer)
    );

    return decoder;
  }
}
