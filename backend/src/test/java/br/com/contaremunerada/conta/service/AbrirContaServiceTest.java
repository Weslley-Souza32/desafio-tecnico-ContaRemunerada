package br.com.contaremunerada.conta.service;

import br.com.contaremunerada.conta.domain.Conta;
import br.com.contaremunerada.conta.repository.ContaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AbrirContaServiceTest {

  private ContaRepository contaRepository;
  private PasswordEncoder passwordEncoder;
  private AbrirContaService abrirContaService;

  @BeforeEach
  void configurar(){
    contaRepository = mock(ContaRepository.class);
    passwordEncoder = mock(PasswordEncoder.class);

    abrirContaService = new AbrirContaService(
        contaRepository,
        passwordEncoder
    );
  }

  @Test
  void deveRejeitarTitularEmBranco(){
    assertThrows(
        IllegalArgumentException.class,
        () -> abrirContaService.abrirConta("   ", "senha123")
    );

    verifyNoInteractions(contaRepository, passwordEncoder);
  }
  @Test
  void deveRejeitarSenhaComMenosDeSeisCaracteres() {
    assertThrows(
        IllegalArgumentException.class,
        () -> abrirContaService.abrirConta("Maria Silva", "12345")
    );

    verifyNoInteractions(contaRepository, passwordEncoder);
  }


  @Test
  void deveCriarContaComNumeroGeradoSenhaCodificadaESaldoZero() {
    // Arrange
    String titular = "Maria Silva";
    String senha = "senha123";
    String hashSimulado = "hash-bcrypt-simulado";

    when(passwordEncoder.encode(senha))
        .thenReturn(hashSimulado);

    when(contaRepository.save(any(Conta.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    Conta resultado = abrirContaService.abrirConta(titular, senha);

    // Assert
    ArgumentCaptor<Conta> captor =
        ArgumentCaptor.forClass(Conta.class);

    verify(passwordEncoder, times(1)).encode(senha);
    verify(contaRepository, times(1)).save(captor.capture());

    Conta contaPersistida = captor.getValue();

    assertNotNull(resultado);
    assertEquals(contaPersistida, resultado);

    assertNotNull(contaPersistida.getNumero());
    assertTrue(contaPersistida.getNumero().startsWith("CR"));
    assertEquals(22, contaPersistida.getNumero().length());

    assertEquals(titular, contaPersistida.getTitular());
    assertEquals(hashSimulado, contaPersistida.getSenhaHash());
    assertNotEquals(senha, contaPersistida.getSenhaHash());

    assertEquals(
        0,
        BigDecimal.ZERO.compareTo(contaPersistida.getSaldo())
    );
  }

}
