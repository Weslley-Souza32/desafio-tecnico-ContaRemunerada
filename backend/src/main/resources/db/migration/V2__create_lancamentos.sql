CREATE TABLE lancamentos
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    conta_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    valor NUMERIC(19, 2) NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_lancamentos_contas
        FOREIGN KEY (conta_id)
        REFERENCES contas (id),

    CONSTRAINT ck_lancamentos_tipo
        CHECK ( tipo IN ('DEPOSITO', 'SAQUE', 'JUROS') ),

    CONSTRAINT ck_lancamentos_valor
        CHECK (
            (tipo IN ('DEPOSITO', 'JUROS') AND valor > 0)
            OR
            (tipo = 'SAQUE' AND valor < 0)
        )
);

CREATE INDEX idx_lancamentos_conta_criado_em
    ON lancamentos(conta_id, criado_em DESC, id DESC);