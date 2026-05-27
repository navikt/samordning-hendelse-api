# Risiko- og sårbarhetsanalyse (ROS)
## ManglendeRefusjonFeedController — `/hendelser/manglendeRefusjonskrav`

**System:** samordning-hendelse-api  
**Team:** pensjonsamhandling  
**Dato:** 2026-05-27  
**Versjon:** 1.0  

---

## 1. Systembeskrivelse

### Formål
ManglendeRefusjonFeedController eksponerer en paginert feed med hendelser om manglende refusjonskrav til eksterne tjenestepensjons­leverandører (TP-leverandører). Feeden varsler TP-leverandører om at det finnes samordningsvedtak der refusjonskrav mangler, med en svarfrist.

### Dataflyt
```
Kafka (manglende-refusjonskrav) → ManglendeRefusjonskravListener → PostgreSQL (MANGLENDE_REFUSJONSKRAV)
                                                                          ↓
Eksterne TP-leverandører → KrakenD Gateway → ManglendeRefusjonFeedController → Service → DB
```

### Dataklassifisering: MODERAT
Endepunktet eksponerer følgende data:

| Felt | Kategori | Hjemmel |
|------|----------|---------|
| `fnr` | Fødselsnummer | Samordningsloven |
| `samId` | Samordnings-ID (referanse til vedtak) | Samordningsloven |
| `svarfrist` | Frist for innsending av refusjonskrav | Samordningsloven |
| `tpnr` | Tjenestepensjonsordningens nummer | Samordningsloven |

### Autentisering og autorisasjon
- **Autentisering:** Maskinporten med scope `nav:pensjon/v1/samordning`
- **Autorisasjon:** TpConfigOrgNoValidator — validerer at organisasjonsnummer i token matcher tpnr-parameteren via tp-config-tjenesten
- **API Gateway:** KrakenD med rate limiting (100 req/60s) — aktiv i q2, planlagt for prod

---

## 2. Skala-definisjoner

### Sannsynlighet (S)
| Verdi | Beskrivelse |
|-------|-------------|
| 1 | Svært lite sannsynlig — krever ekstraordinære omstendigheter |
| 2 | Lite sannsynlig — kan skje, men usannsynlig |
| 3 | Moderat — kan skje i løpet av systemets levetid |
| 4 | Sannsynlig — forventes å skje |
| 5 | Svært sannsynlig — forventes å skje ofte |

### Konsekvens (K)
| Verdi | Beskrivelse |
|-------|-------------|
| 1 | Ubetydelig — ingen merkbar effekt |
| 2 | Lav — mindre ulempe, raskt håndtert |
| 3 | Moderat — merkbar påvirkning, krever oppfølging |
| 4 | Alvorlig — betydelig skade for person/organisasjon |
| 5 | Svært alvorlig — alvorlig personvernbrudd, omdømmeskade |

### Risikoverdi (R = S × K)
| Nivå | Verdi | Aksept |
|------|-------|--------|
| 🟢 Grønn | 1–6 | Akseptabel risiko |
| 🟡 Gul | 7–12 | Bør reduseres, tiltak vurderes |
| 🔴 Rød | 13–25 | Uakseptabel, tiltak påkrevd |

---

## 3. Risikovurdering

### Felles risikoer (delt med alle feed-kontrollere)

| ID | Uønsket hendelse | S | K | R | Eksisterende tiltak | Foreslåtte tiltak | Restrisiko |
|----|-------------------|---|---|---|---------------------|-------------------|------------|
| F-01 | **Ugyldig Maskinporten-token aksepteres** — angriper forfalker token og får tilgang til feed | 1 | 5 | 🟢 5 | Maskinporten-validering med JWK-verifisering, scope-sjekk (`nav:pensjon/v1/samordning`) | Ingen — tilstrekkelig sikret | 🟢 Lav |
| F-02 | **TpConfig er nede** — autorisasjonssjekk feiler, forespørsler avvises | 3 | 3 | 🟡 9 | TpConfigConsumer har connect-timeout (3s) og read-timeout (5s). Feil returnerer HTTP 500 | Vurdere caching av tp-config-svar for kort tid (f.eks. 5 min) for å redusere avhengighet | 🟢 Lav |
| F-03 | **Input-manipulasjon** — angriper sender ugyldige parametere for å trigge feil eller uventede spørringer | 2 | 2 | 🟢 4 | Jakarta Bean Validation: `@Digits(4,0)` for tpnr, `@Min`/`@Max` for paginering, `@PositiveOrZero` for side. FeedExceptionHandler returnerer 400 | Ingen — tilstrekkelig sikret | 🟢 Lav |
| F-04 | **Manglende rate limiting på direkte ingress (prod)** — TP-leverandør overbelaster tjenesten via direkte URL | 2 | 3 | 🟢 6 | KrakenD har rate limiting (100/60s) i q2. I prod brukes foreløpig direkte ingress uten rate limiting. Maskinporten begrenser tilgang til autoriserte aktører | Legge til KrakenD-routing i prod etter ekstern testing. Vurdere Spring-basert rate limiting som fallback | 🟢 Lav |
| F-05 | **Manglende audit-logging** — kan ikke spore hvem som hentet hvilke data | 2 | 4 | 🟡 8 | Maskinporten-token inneholder orgno. TpConfigConsumer logger `validateOrganisation status [orgno, tpnr]`. Controller logger tpnr og antall hendelser på DEBUG-nivå | Logge orgno + tpnr + antall hendelser på INFO-nivå for alle forespørsler. Ikke logg fnr | 🟡 Moderat |

### Spesifikke risikoer for ManglendeRefusjonFeedController

| ID | Uønsket hendelse | S | K | R | Eksisterende tiltak | Foreslåtte tiltak | Restrisiko |
|----|-------------------|---|---|---|---------------------|-------------------|------------|
| R-01 | **Uautorisert tilgang til refusjonskrav-data** — TP-leverandør henter data for tpnr de ikke eier | 1 | 4 | 🟢 4 | TpConfigOrgNoValidator sjekker at orgno matcher tpnr. Maskinporten sikrer identitet | Ingen — tilstrekkelig sikret | 🟢 Lav |
| R-02 | **SamId-kobling avslører vedtaksinformasjon** — samId kan brukes til kryss-referering med vedtaks-feeden for å bygge utvidet profil | 2 | 3 | 🟢 6 | SamId er en intern referanse. TP-leverandøren har allerede tilgang til vedtaks-feeden med samme tpnr-filtrering. Ingen ekstra informasjon oppnås | Dokumentere at samId er ment for kryss-referanse — dette er tilsiktet bruk | 🟢 Lav |
| R-03 | **Manglende forretningsmetrikker** — kan ikke oppdage unormal bruk eller misbruk | 3 | 3 | 🟡 9 | Micrometer `@Timed` gir generelle HTTP-metrikker. Prometheus-endepunkt er aktivert. Ingen domenespesifikke metrikker | Legge til `AppMetrics.incManglendeRefusjonskravLest(tpnr, count)` tilsvarende vedtak-feedens mønster. Sette opp Grafana-alert ved unormal bruksmønster | 🟢 Lav |
| R-04 | **Utløpt svarfrist oppdages ikke** — TP-leverandør leser feeden for sent og mister fristen | 3 | 3 | 🟡 9 | Svarfrist er inkludert i responsen. TP-leverandøren er ansvarlig for å lese feeden regelmessig | Vurdere metrikk/alert for hendelser med svarfrist som nærmer seg. Dokumentere SLA for feed-polling i API-dokumentasjon | 🟡 Moderat |
| R-05 | **Duplikat-sjekk på samId hindrer reell re-sending** — dersom en Kafka-melding må sendes på nytt med oppdatert svarfrist, avvises den | 2 | 3 | 🟢 6 | `findBySamId` returnerer eksisterende og listener ACKer uten lagring. Hindrer duplikater | Vurdere om oppdatering av eksisterende rad (f.eks. ny svarfrist) er ønsket i stedet for avvisning. Dokumentere forventet oppførsel | 🟢 Lav |
| R-06 | **Database-spørring returnerer data for feil TP-leverandør** — feil i sekvensnummer-logikk gir tilgang til andres data | 1 | 4 | 🟢 4 | `findByTpnrAndSekvensnummerBetween` filtrerer alltid på tpnr. JPA parameteriserte spørringer hindrer SQL-injection | Ingen — tilstrekkelig sikret | 🟢 Lav |

---

## 4. Risikomatrise — ManglendeRefusjonFeedController

```
K o n s e k v e n s →
        1       2       3       4       5
  5 ┌───────┬───────┬───────┬───────┬───────┐
S   │       │       │       │       │       │
a 4 ├───────┼───────┼───────┼───────┼───────┤
n   │       │       │       │       │       │
n 3 ├───────┼───────┼───────┼───────┼───────┤
s   │       │       │ R-03  │       │       │
y   │       │       │ R-04  │       │       │
n 2 ├───────┼───────┼───────┼───────┼───────┤
l   │       │ F-03  │ R-02  │ F-05  │       │
i   │       │       │ R-05  │       │       │
g 1 ├───────┼───────┼───────┼───────┼───────┤
h   │       │       │       │ R-01  │ F-01  │
e   │       │       │       │ R-06  │       │
t   │       │       │       │       │       │
  ↓ └───────┴───────┴───────┴───────┴───────┘
```

---

## 5. Oppsummering og anbefalinger

### Aksepterte risikoer (🟢)
- F-01, F-03, F-04, R-01, R-02, R-05, R-06 — tilstrekkelig sikret med eksisterende tiltak

### Risikoer som bør reduseres (🟡)
| ID | Tiltak | Prioritet |
|----|--------|-----------|
| F-05 | Audit-logging på INFO-nivå med orgno+tpnr | Middels |
| R-04 | Dokumentere SLA for feed-polling, vurdere svarfrist-alert | Middels |
| F-02 | Vurdere caching av tp-config-validering | Lav |
| R-03 | Legge til AppMetrics for refusjonskrav-feed | Lav |

### Konklusjon
Tjenesten har en **akseptabel risikoprofil**. Dataklassifiseringen er lavere enn person-feeden (ingen adresse, sivilstand eller dødsdato). Hovedbekymringen er **svarfrist-håndtering** (R-04) — det bør dokumenteres tydelig for TP-leverandører hva som er forventet polling-frekvens for å sikre at svarfrister overholdes.
