# Risiko- og sårbarhetsanalyse (ROS)
## PersonFeedController — `/hendelser/personer`

**System:** samordning-hendelse-api  
**Team:** pensjonsamhandling  
**Dato:** 2026-05-27  
**Versjon:** 1.0  

---

## 1. Systembeskrivelse

### Formål
PersonFeedController eksponerer en paginert feed med personendringshendelser (sivilstandsendring, adresseendring, dødsfallsmelding, fødselsnummerendring) til eksterne tjenestepensjons­leverandører (TP-leverandører).

### Dataflyt
```
Kafka (person-endring) → PersonEndringListener → PostgreSQL (PERSON_ENDRING)
                                                        ↓
Eksterne TP-leverandører → KrakenD Gateway → PersonFeedController → PersonService → DB
```

### Dataklassifisering: HØYT
Endepunktet eksponerer følgende personopplysninger:

| Felt | Kategori | Hjemmel |
|------|----------|---------|
| `fnr` | Fødselsnummer | Samordningsloven |
| `fnrGammelt` | Tidligere fødselsnummer | Samordningsloven |
| `sivilstand` | Sivilstatus | Samordningsloven |
| `sivilstandDato` | Dato for sivilstandsendring | Samordningsloven |
| `doedsdato` | Dødsdato | Samordningsloven |
| `adresse` | Fullstendig adresse (linje1-3, postnr, poststed, land) | Samordningsloven |
| `meldingskode` | Type hendelse (SIVILSTAND, FODSELSNUMMER, ADRESSE, DOEDSFALL) | Samordningsloven |

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

### Spesifikke risikoer for PersonFeedController

| ID | Uønsket hendelse | S | K | R | Eksisterende tiltak | Foreslåtte tiltak | Restrisiko |
|----|-------------------|---|---|---|---------------------|-------------------|------------|
| P-01 | **Uautorisert tilgang til personsensitive data** — TP-leverandør henter persondata for tpnr de ikke eier | 1 | 5 | 🟢 5 | TpConfigOrgNoValidator sjekker at orgno matcher tpnr. Maskinporten sikrer identitet | Ingen — tilstrekkelig sikret | 🟢 Lav |
| P-02 | **Overeksponering av data** — TP-leverandør mottar mer persondata enn nødvendig for samordning | 3 | 4 | 🟡 12 | Alle felter returneres alltid (fnr, adresse, sivilstand, dødsdato). Responsen er et fast skjema — ingen feltfiltrering | Vurdere om alle felter alltid er nødvendige. Meldingskode indikerer type endring — vurder å kun inkludere relevante felter per meldingskode. Dokumentere dataminimering i API-kontrakt | 🟡 Moderat |
| P-03 | **Persondata lekker i logger** — fnr eller andre PII skrives til logg | 2 | 5 | 🟡 10 | Controller logger kun `tpnr` og `hendelser.size` på DEBUG-nivå. Ingen fnr i logger. Kafka-listener logger `hendelseId` og `meldingskode` | Gjennomgå at logback-config maskerer evt. PII i stack traces. Legge til PII-scanning i CI/CD | 🟡 Moderat |
| P-04 | **Manglende forretningsmetrikker** — kan ikke oppdage unormal bruk eller misbruk | 3 | 3 | 🟡 9 | Micrometer `@Timed` gir generelle HTTP-metrikker. Prometheus-endepunkt er aktivert. Ingen domenespesifikke metrikker (i motsetning til vedtak/ytelse som har `AppMetrics`) | Legge til `AppMetrics.incPersonHendelserLest(tpnr, count)` tilsvarende vedtak-feedens mønster. Sette opp Grafana-alert ved unormal bruksmønster | 🟢 Lav |
| P-05 | **Adressedata eksponeres unødvendig** — full adresse (linje1-3, postnr, poststed, land) sendes ved alle meldingskoder, ikke bare ADRESSE | 3 | 3 | 🟡 9 | Adresse-feltet er nullable — settes kun ved ADRESSE-meldingskode fra Kafka-listener | Verifisere at adresse kun populeres for ADRESSE-meldingskode i alle tilfeller. Dokumentere dette i API-spesifikasjon | 🟢 Lav |
| P-06 | **Database-spørring returnerer data for feil TP-leverandør** — feil i sekvensnummer-logikk gir tilgang til andres data | 1 | 5 | 🟢 5 | `findByTpnrAndSekvensnummerBetween` filtrerer alltid på tpnr. JPA parameteriserte spørringer hindrer SQL-injection | Ingen — tilstrekkelig sikret | 🟢 Lav |

---

## 4. Risikomatrise — PersonFeedController

```
K o n s e k v e n s →
        1       2       3       4       5
  5 ┌───────┬───────┬───────┬───────┬───────┐
S   │       │       │       │       │       │
a 4 ├───────┼───────┼───────┼───────┼───────┤
n   │       │       │       │       │       │
n 3 ├───────┼───────┼───────┼───────┼───────┤
s   │       │       │ P-04  │ P-02  │       │
y   │       │       │ P-05  │       │       │
n 2 ├───────┼───────┼───────┼───────┼───────┤
l   │       │ F-03  │ F-04  │ F-05  │ P-03  │
i   │       │       │       │       │       │
g 1 ├───────┼───────┼───────┼───────┼───────┤
h   │       │       │       │       │F-01   │
e   │       │       │       │       │P-01   │
t   │       │       │       │       │P-06   │
  ↓ └───────┴───────┴───────┴───────┴───────┘
```

---

## 5. Oppsummering og anbefalinger

### Aksepterte risikoer (🟢)
- F-01, F-03, F-04, P-01, P-06 — tilstrekkelig sikret med eksisterende tiltak

### Risikoer som bør reduseres (🟡)
| ID | Tiltak | Prioritet |
|----|--------|-----------|
| P-02 | Vurdere dataminimering per meldingskode | Høy |
| P-03 | PII-scanning i CI/CD, logback-gjennomgang | Middels |
| F-05 | Audit-logging på INFO-nivå med orgno+tpnr | Middels |
| P-04 | Legge til AppMetrics for person-feed | Lav |
| F-02 | Vurdere caching av tp-config-validering | Lav |
| P-05 | Verifisere adresse-populering, dokumentere | Lav |

### Konklusjon
Tjenesten har en **akseptabel risikoprofil** med grundig autentisering og autorisasjon. Hovedbekymringen er **dataminimering** (P-02) — person-feeden eksponerer potensielt mer data enn nødvendig per hendelsestype. Dette bør vurderes som en forbedring.
