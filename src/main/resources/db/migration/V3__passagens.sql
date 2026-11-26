CREATE TABLE passagem (
    id                UUID                     PRIMARY KEY,
    viagem_id         UUID                     NOT NULL,
    parada_origem_id  UUID                     NOT NULL,
    parada_destino_id UUID                     NOT NULL,
    passageiro_nome   VARCHAR(120)             NOT NULL,
    canal             VARCHAR(10)              NOT NULL,
    local_venda       VARCHAR(100),
    preco_centavos    BIGINT                   NOT NULL,
    status            VARCHAR(15)              NOT NULL,
    reservada_ate     TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_passagem_canal CHECK (canal IN ('INTERNET', 'GUICHE')),
    CONSTRAINT ck_passagem_status CHECK (status IN ('RESERVADA', 'CONFIRMADA')),
    CONSTRAINT ck_passagem_preco CHECK (preco_centavos >= 0),
    CONSTRAINT ck_passagem_trecho CHECK (parada_origem_id <> parada_destino_id),
    CONSTRAINT ck_passagem_local_venda CHECK (
        (canal = 'INTERNET' AND local_venda IS NULL)
        OR (canal = 'GUICHE' AND local_venda IS NOT NULL)
    )
);

CREATE INDEX ix_passagem_viagem ON passagem (viagem_id);

CREATE TABLE pagamento (
    id             UUID                     PRIMARY KEY,
    passagem_id    UUID                     NOT NULL,
    forma          VARCHAR(10)              NOT NULL,
    valor_centavos BIGINT                   NOT NULL,
    status         VARCHAR(10)              NOT NULL,
    registrado_em  TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_pagamento_passagem FOREIGN KEY (passagem_id) REFERENCES passagem (id),
    CONSTRAINT ck_pagamento_forma CHECK (forma IN ('CARTAO', 'DINHEIRO')),
    CONSTRAINT ck_pagamento_status CHECK (status IN ('APROVADO', 'RECUSADO')),
    CONSTRAINT ck_pagamento_valor CHECK (valor_centavos >= 0)
);

CREATE INDEX ix_pagamento_passagem ON pagamento (passagem_id);
