CREATE TABLE T_SAMORDNINGSPLIKTIG_VEDTAK(
    DATA JSONB
);

INSERT INTO T_SAMORDNINGSPLIKTIG_VEDTAK VALUES('{"identifikator": "12345678901", "ytelsesType": "AAP", "vedtakId": "ABC123", "fom": "2020-01-01"}');
INSERT INTO T_SAMORDNINGSPLIKTIG_VEDTAK VALUES('{"identifikator": "23456789012", "ytelsesType": "AAP", "vedtakId": "123ABC", "fom": "2030-01-01", "tom": "2031-02-02"}');
INSERT INTO T_SAMORDNINGSPLIKTIG_VEDTAK VALUES('{"identifikator": "10000000001", "ytelsesType": "AAP", "vedtakId": "A1B2C3", "fom": "2040-01-01", "tom": "2041-02-02"}');
