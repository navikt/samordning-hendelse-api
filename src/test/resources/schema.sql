CREATE TABLE IF NOT EXISTS HENDELSER
(
    ID            SERIAL PRIMARY KEY,
    TPNR          VARCHAR(20),
    HENDELSE_DATA JSON
);

CREATE TABLE IF NOT EXISTS YTELSE_HENDELSER
(
    ID                  SERIAL PRIMARY KEY,
    SEKVENSNUMMER       BIGINT,
    TPNR                VARCHAR(20) not null,
    FNR                 VARCHAR(20) not null,
    HENDELSE_TYPE       VARCHAR(20) not null,
    YTELSE_TYPE         VARCHAR(20) not null,
    DATO_BRUK_FOM       TIMESTAMP   not null,
    DATO_BRUK_TOM       TIMESTAMP
);
