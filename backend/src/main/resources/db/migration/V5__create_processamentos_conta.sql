CREATE TABLE processamentos_conta
(
    conta_id BIGINT NOT NULL,
    data_referencia DATE NOT NULL,
    processado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_processamentos_conta
        PRIMARY KEY (conta_id, data_referencia),

    CONSTRAINT fk_processamentos_conta_conta
        FOREIGN KEY (conta_id)
        REFERENCES contas(id),

    CONSTRAINT fk_processamentos_conta_fechamento
        FOREIGN KEY (data_referencia)
        REFERENCES fechamentos(data_referencia)
);

CREATE INDEX idx_processamentos_conta_data_referencia
        ON processamentos_conta(data_referencia);