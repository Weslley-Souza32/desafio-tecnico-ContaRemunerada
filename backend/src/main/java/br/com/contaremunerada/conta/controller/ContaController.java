package br.com.contaremunerada.conta.controller;

import br.com.contaremunerada.conta.domain.Conta;
import br.com.contaremunerada.conta.dto.AbrirContaRequest;
import br.com.contaremunerada.conta.dto.ContaCriadaResponse;
import br.com.contaremunerada.conta.service.AbrirContaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contas")
public class ContaController {

  private final AbrirContaService abrirContaService;

  public ContaController(AbrirContaService abrirContaService) {
    this.abrirContaService = abrirContaService;
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
}
