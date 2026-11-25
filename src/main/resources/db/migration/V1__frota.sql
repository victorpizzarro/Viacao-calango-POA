CREATE TABLE onibus (
    id                           UUID        PRIMARY KEY,
    tipo                         VARCHAR(20) NOT NULL,
    capacidade                   INTEGER     NOT NULL,
    quilometragem_total          INTEGER     NOT NULL,
    quilometragem_ultima_revisao INTEGER     NOT NULL,
    CONSTRAINT ck_onibus_tipo CHECK (tipo IN ('LEITO', 'SEMI_LEITO', 'EXECUTIVO')),
    CONSTRAINT ck_onibus_capacidade CHECK (capacidade IN (23, 28, 32)),
    CONSTRAINT ck_onibus_quilometragem CHECK (
        quilometragem_ultima_revisao >= 0
        AND quilometragem_ultima_revisao <= quilometragem_total
    )
);
