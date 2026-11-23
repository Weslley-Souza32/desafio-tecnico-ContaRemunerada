package br.com.contaremunerada.conta.service;

import br.com.contaremunerada.conta.domain.Conta;
import br.com.contaremunerada.conta.repository.ContaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AbrirContaService {

  private final ContaRepository contaRepository;
  private final PasswordEncoder passwordEncoder;

  public AbrirContaService(ContaRepository contaRepository, PasswordEncoder passwordEncoder) {
    this.contaRepository = contaRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional
  public Conta abrirConta(String titular, String senha) {
    if (titular == null || titular.isBlank()){
      throw new IllegalArgumentException("O titular é obrigatório.");
    }

    if (senha == null || senha.length() < 6){
      throw new IllegalArgumentException("A senha deve possuir no mínimo 6 caracteres.");
    }

    String numero = gerarNumero();
    String senhaHash = passwordEncoder.encode(senha);

    Conta conta = new Conta(numero, titular.trim(), senhaHash);

    return contaRepository.save(conta);
  }

  private String gerarNumero() {
    String identificador = UUID.randomUUID()
        .toString()
        .replace("-", "")
        .substring(0, 20);

    return "CR" + identificador.toUpperCase();
  }
}
