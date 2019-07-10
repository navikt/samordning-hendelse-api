CREATE TABLE IF NOT EXISTS HENDELSER (
    ID SERIAL PRIMARY KEY,
    HENDELSE_DATA JSONB
);

INSERT INTO HENDELSER(HENDELSE_DATA) VALUES
('{"tpnr": "1000", "identifikator": "01016600000", "ytelsesType": "AAP", "vedtakId": "1", "fom": "2020-01-01", "tom": null}'),
('{"tpnr": "2000", "identifikator": "01016700000", "ytelsesType": "ET", "vedtakId": "2", "fom": "2021-01-01", "tom": "2030-01-01"}'),
('{"tpnr": "3000", "identifikator": "01016700000", "ytelsesType": "ET", "vedtakId": "2", "fom": "2021-01-01", "tom": "2030-01-01"}'),
('{"tpnr": "4000", "identifikator": "01016800000", "ytelsesType": "GP", "vedtakId": "3", "fom": "2022-01-01", "tom": null}'),
('{"tpnr": "4000", "identifikator": "01016900000", "ytelsesType": "IP", "vedtakId": "4", "fom": "2023-01-01", "tom": "2033-01-01"}'),
('{"tpnr": "4000", "identifikator": "01017000000", "ytelsesType": "PT", "vedtakId": "5", "fom": "2024-01-01", "tom": "2034-01-01"}');