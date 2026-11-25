package br.com.contaremunerada.fechamento.controller;

import br.com.contaremunerada.fechamento.dto.FechamentoResponse;
import br.com.contaremunerada.fechamento.service.ConsultarFechamentoService;
import br.com.contaremunerada.fechamento.service.ExecutarFechamentoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("fechamentos")
public class FechamentoController {

  private final ConsultarFechamentoService consultarFechamentoService;
  private final ExecutarFechamentoService executarFechamentoService;

  public FechamentoController(ConsultarFechamentoService consultarFechamentoService, ExecutarFechamentoService executarFechamentoService) {
    this.consultarFechamentoService = consultarFechamentoService;
    this.executarFechamentoService = executarFechamentoService;
  }

  @GetMapping("/{data}")
  public ResponseEntity<FechamentoResponse> consultar(
      @PathVariable("data")
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      LocalDate dataReferencia
  ) {
    return consultarFechamentoService.consultar(dataReferencia)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping
  public ResponseEntity<FechamentoResponse> executar(
      @RequestParam("data")
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      LocalDate dataReferencia
  ) {
    FechamentoResponse response = executarFechamentoService.executar(dataReferencia);

    return ResponseEntity.ok(response);
  }
}
