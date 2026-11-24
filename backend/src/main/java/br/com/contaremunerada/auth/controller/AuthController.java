package br.com.contaremunerada.auth.controller;

import br.com.contaremunerada.auth.dto.LoginRequest;
import br.com.contaremunerada.auth.dto.LoginResponse;
import br.com.contaremunerada.auth.service.AutenticarContaService;
import br.com.contaremunerada.auth.service.JwtService;
import br.com.contaremunerada.conta.domain.Conta;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final AutenticarContaService autenticarContaService;
  private final JwtService jwtService;

  public AuthController(AutenticarContaService autenticarContaService, JwtService jwtService) {
    this.autenticarContaService = autenticarContaService;
    this.jwtService = jwtService;
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {

    Conta conta = autenticarContaService.autenticar(loginRequest.numero(), loginRequest.senha());

    String token = jwtService.gerarToken(conta.getId());

    LoginResponse response = new LoginResponse(
        token,
        conta.getId()
    );

    return ResponseEntity.ok(response);
  }
}
