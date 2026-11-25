
package br.com.contaremunerada.fechamento.service;

import br.com.contaremunerada.conta.repository.ContaRepository;
import br.com.contaremunerada.fechamento.domain.Fechamento;
import br.com.contaremunerada.fechamento.repository.FechamentoRepository;
import br.com.contaremunerada.taxa.domain.TaxaDiaria;
import br.com.contaremunerada.taxa.repository.TaxaDiariaRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExecutarFechamentoServiceTest {

  @Mock
  private TaxaDiariaRepository taxaDiariaRepository;

  @Mock
  private FechamentoEstadoService fechamentoEstadoService;

  @Mock
  private ContaRepository contaRepository;

  @Mock
  private ProcessarContaService processarContaService;

  @Mock
  private FechamentoRepository fechamentoRepository;

  @InjectMocks
  private ExecutarFechamentoService service;

  @Test
  void deveProcessar101ContasEmDoisLotes() {
    LocalDate data = LocalDate.now();
    BigDecimal taxa = new BigDecimal("0.0003");

    List<Long> ids = LongStream.rangeClosed(1, 101)
        .boxed()
        .toList();

    var pageable = PageRequest.of(0, 100);

    Slice<Long> primeiroLote = new SliceImpl<>(
        ids.subList(0, 100), pageable, true
    );

    Slice<Long> segundoLote = new SliceImpl<>(
        ids.subList(100, 101), pageable, false
    );

    when(taxaDiariaRepository.findById((short) 1))
        .thenReturn(Optional.of(new TaxaDiaria(taxa)));

    when(contaRepository.buscarMaiorId())
        .thenReturn(101L);

    when(contaRepository.listarIdsParaFechamento(
        anyLong(), eq(101L), any()
    )).thenReturn(primeiroLote, segundoLote);

    Fechamento fechamento = new Fechamento(data);
    fechamento.atualizarProgresso(101);
    fechamento.concluir();

    when(fechamentoRepository.findById(data))
        .thenReturn(Optional.of(fechamento));

    var resposta = service.executar(data);

    assertEquals(101, resposta.contasProcessadas());
    assertEquals("CONCLUIDO", resposta.status().name());

    verify(processarContaService, times(101))
        .processar(anyLong(), eq(data), eq(taxa));

    verify(fechamentoEstadoService)
        .atualizarProgresso(data, 100);

    verify(fechamentoEstadoService)
        .atualizarProgresso(data, 101);

    verify(fechamentoEstadoService).concluir(data);

    verify(contaRepository, times(2))
        .listarIdsParaFechamento(
            anyLong(), eq(101L), any()
        );
  }


  @Test
  void deveRegistrarErroQuandoProcessamentoDeContaFalhar() {

    LocalDate data = LocalDate.now();

    var pageable = PageRequest.of(0, 100);

    Slice<Long> lote = new SliceImpl<>(
        List.of(1L, 2L, 3L),
        pageable,
        false
    );

    when(contaRepository.buscarMaiorId())
        .thenReturn(3L);

    when(contaRepository.listarIdsParaFechamento(
        eq(0L),
        eq(3L),
        any()
    )).thenReturn(lote);


    doNothing()
        .when(processarContaService)
        .processar(
            eq(1L),
            eq(data),
            isNull()
        );

    doThrow(new IllegalStateException("Falha simulada"))
        .when(processarContaService)
        .processar(
            eq(2L),
            eq(data),
            isNull()
        );

    assertThrows(
        IllegalStateException.class,
        () -> service.executar(data)
    );

    verify(processarContaService)
        .processar(eq(1L), eq(data), isNull());

    verify(processarContaService)
        .processar(eq(2L), eq(data), isNull());

    verify(processarContaService, never())
        .processar(eq(3L), eq(data), isNull());

    verify(fechamentoEstadoService)
        .atualizarProgresso(data, 1);

    verify(fechamentoEstadoService)
        .registrarErro(data);

    verify(fechamentoEstadoService, never())
        .concluir(data);
  }
}
