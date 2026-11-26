-- V2 pertence ao cadastro; V3 esta publicada em origin/feat/passagens.
-- Viagens ainda usa repositorios em memoria: viagem_id e referencia logica, sem tabela duplicada.
CREATE TABLE motorista_periodo_dirigido (
    id UUID PRIMARY KEY,
    motorista_id UUID NOT NULL REFERENCES motoristas(id),
    inicio TIMESTAMP WITH TIME ZONE NOT NULL,
    fim TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_periodo_dirigido_horarios CHECK (fim > inicio)
);
CREATE INDEX ix_periodo_dirigido_motorista ON motorista_periodo_dirigido(motorista_id);

CREATE TABLE motorista_escala (
    viagem_id UUID PRIMARY KEY,
    onibus_id UUID NOT NULL REFERENCES onibus(id)
);

CREATE TABLE motorista_escala_trecho (
    viagem_id UUID NOT NULL REFERENCES motorista_escala(viagem_id),
    ordem INTEGER NOT NULL,
    origem TEXT NOT NULL,
    destino TEXT NOT NULL,
    partida TIMESTAMP WITH TIME ZONE NOT NULL,
    chegada TIMESTAMP WITH TIME ZONE NOT NULL,
    distancia_km INTEGER NOT NULL,
    motorista_id UUID NOT NULL REFERENCES motoristas(id),
    PRIMARY KEY (viagem_id, ordem),
    CONSTRAINT ck_escala_trecho_ordem CHECK (ordem > 0),
    CONSTRAINT ck_escala_trecho_horarios CHECK (chegada > partida),
    CONSTRAINT ck_escala_trecho_distancia CHECK (distancia_km > 0)
);
CREATE INDEX ix_escala_trecho_motorista ON motorista_escala_trecho(motorista_id, partida, chegada);
