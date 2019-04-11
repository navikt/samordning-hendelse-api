CREATE TABLE IF NOT EXISTS HENDELSER (
    ID SERIAL PRIMARY KEY,
    HENDELSE_DATA JSONB
);

INSERT INTO HENDELSER(HENDELSE_DATA) VALUES
('{"identifikator": "00000000000", "ytelsesType": "AAP", "vedtakId": "0", "fom": "2020-01-01"}'),
('{"identifikator": "11111111111", "ytelsesType": "ET", "vedtakId": "1", "fom": "2021-01-01"}'),
('{"identifikator": "22222222222", "ytelsesType": "GP", "vedtakId": "2", "fom": "2022-01-01"}'),
('{"identifikator": "33333333333", "ytelsesType": "IP", "vedtakId": "3", "fom": "2023-01-01"}'),
('{"identifikator": "44444444444", "ytelsesType": "PT", "vedtakId": "4", "fom": "2024-01-01"}'),
('{"identifikator": "55555555555", "ytelsesType": "SP", "vedtakId": "5", "fom": "2025-01-01", "tom": "2030-01-01"}'),
('{"identifikator": "66666666666", "ytelsesType": "ST", "vedtakId": "6", "fom": "2026-01-01", "tom": "2031-01-01"}'),
('{"identifikator": "77777777777", "ytelsesType": "TFB", "vedtakId": "7", "fom": "2027-01-01", "tom": "2032-01-01"}'),
('{"identifikator": "88888888888", "ytelsesType": "TP", "vedtakId": "8", "fom": "2028-01-01", "tom": "2033-01-01"}'),
('{"identifikator": "99999999999", "ytelsesType": "TSB", "vedtakId": "9", "fom": "2029-01-01", "tom": "2034-01-01"}');
