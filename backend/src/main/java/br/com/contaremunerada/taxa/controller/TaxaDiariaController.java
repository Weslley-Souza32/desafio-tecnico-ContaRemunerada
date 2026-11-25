package br.com.contaremunerada.taxa.controller;

import br.com.contaremunerada.shared.dto.ErrorResponse;
import br.com.contaremunerada.taxa.dto.TaxaDiariaRequest;
import br.com.contaremunerada.taxa.dto.TaxaDiariaResponse;
import br.com.contaremunerada.taxa.service.TaxaDiariaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/taxa")
public class TaxaDiariaController {

  private final TaxaDiariaService taxaDiariaService;

  public TaxaDiariaController(TaxaDiariaService taxaDiariaService) {

    this.taxaDiariaService = taxaDiariaService;
  }

  @PutMapping
  public ResponseEntity<TaxaDiariaResponse> configurar(
      @RequestBody(required = false)TaxaDiariaRequest request
  ) {

    if (request == null) {
      throw new IllegalArgumentException("A taxa diária é obrigatória.");
    }

    TaxaDiariaResponse response = taxaDiariaService.configurar(
        request.obterTaxaComTexto()
    );

    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<?> consultarTaxaDiaria() {

    return taxaDiariaService.consultarTaxa()
        .<ResponseEntity<?>>map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(
                "TAXA_NAO_CONFIGURADA",
                "Nenhuma taxa diária foi configurada."
            )));
  }
}
