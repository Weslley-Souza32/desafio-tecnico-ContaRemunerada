package br.com.contaremunerada.fechamento.repository;

import br.com.contaremunerada.fechamento.domain.Fechamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface FechamentoRepository extends JpaRepository<Fechamento, LocalDate> {

  @Modifying
  @Query(value = """
        INSERT INTO fechamentos (
            data_referencia,
            status,
            contas_processadas,
            iniciado_em,
            finalizado_em
        )
        VALUES (
            :dataReferencia,
            'EM_ANDAMENTO',
            0,
            CURRENT_TIMESTAMP,
            NULL
        )
        ON CONFLICT (data_referencia)
        DO UPDATE SET
            status = 'EM_ANDAMENTO',
            contas_processadas = 0,
            iniciado_em = CURRENT_TIMESTAMP,
            finalizado_em = NULL
        WHERE fechamentos.status <> 'EM_ANDAMENTO'
        """, nativeQuery = true)
  int tentarIniciar(@Param("dataReferencia") LocalDate dataReferencia);
}
