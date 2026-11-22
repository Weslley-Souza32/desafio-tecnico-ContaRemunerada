CREATE TABLE fechamentos
(
    data_referencia DATE PRIMARY KEY,
    status VARCHAR(20) NOT NULL,
    contas_processadas INTEGER NOT NULL DEFAULT 0,
    iniciado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finalizado_em TIMESTAMPTZ,

    CONSTRAINT ck_fechamentos_status
        CHECK ( status IN ('EM_ANDAMENTO', 'CONCLUIDO', 'ERRO') ),

    CONSTRAINT ck_fechamentos_contas_processadas
        CHECK ( contas_processadas >= 0 )
);