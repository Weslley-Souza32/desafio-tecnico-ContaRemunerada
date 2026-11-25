package br.com.contaremunerada.conta.controller;

import br.com.contaremunerada.auth.service.AutorizacaoContaService;
import br.com.contaremunerada.conta.domain.Conta;
import br.com.contaremunerada.conta.dto.AbrirContaRequest;
import br.com.contaremunerada.conta.dto.ContaCriadaResponse;
import br.com.contaremunerada.conta.dto.MovimentacaoRequest;
import br.com.contaremunerada.conta.dto.SaldoResponse;
import br.com.contaremunerada.conta.exception.ValorInvalidoException;
import br.com.contaremunerada.conta.service.AbrirContaService;
import br.com.contaremunerada.conta.service.ConsultarSaldoService;
import br.com.contaremunerada.conta.service.DepositarService;
import br.com.contaremunerada.conta.service.SacarService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contas")
public class ContaController {

  private final AbrirContaService abrirContaService;
  private final AutorizacaoContaService autorizacaoContaService;
  private final ConsultarSaldoService consultarSaldoService;
  private final DepositarService depositarService;
  private final SacarService sacarService;

  public ContaController(AbrirContaService abrirContaService, AutorizacaoContaService autorizacaoContaService, ConsultarSaldoService consultarSaldoService, DepositarService depositarService, SacarService sacarService) {
    this.abrirContaService = abrirContaService;
    this.autorizacaoContaService = autorizacaoContaService;
    this.consultarSaldoService = consultarSaldoService;
    this.depositarService = depositarService;
    this.sacarService = sacarService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ContaCriadaResponse abrirConta(@Valid @RequestBody AbrirContaRequest request) {

    Conta conta = abrirContaService.abrirConta(request.titular(), request.senha());

    return new ContaCriadaResponse(
        conta.getId(),
        conta.getNumero()
    );
  }

  @GetMapping("/{id}/saldo")
  public ResponseEntity<SaldoResponse> consultarSaldo(
      @PathVariable("id") Long contaId,
      @AuthenticationPrincipal Jwt jwt
  ) {
      autorizacaoContaService.validarAcesso(contaId, jwt);

      SaldoResponse saldoResponse = consultarSaldoService.consultarSaldo(contaId);

      return ResponseEntity.ok(saldoResponse);
  }

  @PostMapping("/{id}/depositos")
  public ResponseEntity<Void> depositar(
      @PathVariable("id") Long contaId,
      @AuthenticationPrincipal Jwt jwt,
      @RequestBody(required = false) MovimentacaoRequest request
  ) {
    autorizacaoContaService.validarAcesso(contaId, jwt);

    if (request == null) {
      throw new ValorInvalidoException();
    }

    depositarService.depositar(contaId, request.obterValorComoTexto());

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .build();
  }

  @PostMapping("/{id}/saques")
  public ResponseEntity<Void> sacar(
      @PathVariable("id") Long contaId,
      @AuthenticationPrincipal Jwt jwt,
      @RequestBody(required = false) MovimentacaoRequest request
  ) {
    autorizacaoContaService.validarAcesso(contaId, jwt);

    if (request == null) {
      throw new ValorInvalidoException();
    }

    sacarService.sacar(contaId, request.obterValorComoTexto());

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .build();
  }
}
