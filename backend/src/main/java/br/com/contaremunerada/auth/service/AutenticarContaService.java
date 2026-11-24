
package br.com.contaremunerada.auth.service;

import br.com.contaremunerada.auth.exception.CredenciaisInvalidasException;
import br.com.contaremunerada.conta.domain.Conta;
import br.com.contaremunerada.conta.repository.ContaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AutenticarContaService {

  private final ContaRepository contaRepository;
  private final PasswordEncoder passwordEncoder;

  public AutenticarContaService(
      ContaRepository contaRepository,
      PasswordEncoder passwordEncoder
  ) {
    this.contaRepository = contaRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional(readOnly = true)
  public Conta autenticar(String numero, String senha) {

    Conta conta = contaRepository.findByNumero(numero)
        .orElseThrow(CredenciaisInvalidasException::new);

    boolean senhaValida = passwordEncoder.matches(
        senha,
        conta.getSenhaHash()
    );

    if (!senhaValida) {
      throw new CredenciaisInvalidasException();
    }

    return conta;
  }
}
