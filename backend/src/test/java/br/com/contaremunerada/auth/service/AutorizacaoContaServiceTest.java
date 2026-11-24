package br.com.contaremunerada.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.junit.jupiter.api.Assertions.*;

public class AutorizacaoContaServiceTest {

  private AutorizacaoContaService autorizacaoContaService;

  @BeforeEach
  public void configurar() {
    autorizacaoContaService = new AutorizacaoContaService();
  }

  @Test
  void devePermitirAcessoAPropriaConta(){
    Jwt jwt = criarJwt("2");

    assertDoesNotThrow(
        () -> autorizacaoContaService.validarAcesso(2L, jwt)
    );
  }

  @Test
  void deveNegarAcessoAOutraConta() {
    Jwt jwt = criarJwt("2");

    AccessDeniedException exception = assertThrows(
        AccessDeniedException.class,
        () -> autorizacaoContaService.validarAcesso(3L, jwt)
    );

    assertEquals(
        "Você não possui permissão para acessar esta conta.",
        exception.getMessage()
    );
  }

  @Test
  void deveNegarAcessoMesmoQuandoIdSolicitadoNaoExiste() {
    Jwt jwt = criarJwt("2");

    assertThrows(
        AccessDeniedException.class,
        () -> autorizacaoContaService.validarAcesso(999L, jwt)
    );
  }

  private Jwt criarJwt(String contaId){
    return Jwt.withTokenValue("token-teste")
        .header("alg", "HS256")
        .subject(contaId)
        .build();
  }
}
