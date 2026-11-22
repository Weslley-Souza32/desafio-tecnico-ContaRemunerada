CREATE TABLE taxa_diaria
(
    id SMALLINT PRIMARY KEY ,
    taxa_diaria NUMERIC(18, 10) NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_taxa_diaria_id_unico
        CHECK ( id = 1 ),

    CONSTRAINT ck_taxa_diaria_nao_negativa
        CHECK ( taxa_diaria >= 0 )
);