INSERT INTO HENDELSER(TPNR, HENDELSE_DATA) VALUES
   ('1000', '{"identifikator": "01016600000", "ytelsesType": "AAP", "vedtakId": "1", "fom": "2020-01-01", "tom": null}'),
   ('2000', '{"identifikator": "01016700000", "ytelsesType": "ET", "vedtakId": "2", "fom": "2021-01-01", "tom": "2030-01-01"}'),
   ('3000', '{"identifikator": "01016700000", "ytelsesType": "ET", "vedtakId": "2", "fom": "2021-01-01", "tom": "2030-01-01"}'),
   ('4000', '{"identifikator": "01016800000", "ytelsesType": "GP", "vedtakId": "3", "fom": "2022-01-01", "tom": null}'),
   ('4000', '{"identifikator": "01016900000", "ytelsesType": "IP", "vedtakId": "4", "fom": "2023-01-01", "tom": "2033-01-01"}'),
   ('4000', '{"identifikator": "01017000000", "ytelsesType": "PT", "vedtakId": "5", "fom": "2024-01-01", "tom": "2034-01-01"}'),
   ('5000', '{"identifikator": "01018000000", "ytelsesType": "NOT_IN_FILTER", "vedtakId": "6", "fom": "2024-01-01", "tom": "2034-01-01"}'),
   ('6000', '{"identifikator": "01019000000", "ytelsesType": "OMS", "vedtakId": "7", "fom": "2024-01-01", "tom": null}');

INSERT INTO YTELSE_HENDELSER(TPNR, MOTTAKER, SEKVENSNUMMER, IDENTIFIKATOR, HENDELSE_TYPE, YTELSE_TYPE, DATO_BRUK_FOM, DATO_BRUK_TOM) VALUES
    ('3010', '3200', '1', '01016600000', 'OPPRETT', 'LIVSVARIG_AFP', '2020-01-01 12:12:12', '2022-01-01 13:13:13'),
    ('3200', '3010', '1', '14087459887', 'OPPRETT', 'UFORE', '2024-01-01 12:12:12', null),
    ('3200', '3010', '2', '14087459999', 'OPPRETT', 'ALDER', '2024-01-01 12:12:12', null)

