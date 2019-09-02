# Samordning-Hendelse-API

Samordning-hendelse-api gir en liste med samordningspliktige hendelser for et gitt tpnummer på json format. 
Denne listen hentes via /hendelser endepunktet. Se konkrete eksempler under.

#### API Portal

API'et er eksponert via NAVs API Portal:

`preprod`:

https://api-portal-preprod.nav.no/docs/services/pensjon-samordning/operations/hendelserUsingGET

`prod`:

TBA


#### Eksempel:
```bash
curl -k -X GET https://samordning-hendelse-api.nais.preprod.local/hendelser?tpnr=<tpnr> \
    -H 'Accept: application/json' \
    -H "Authorization: Bearer ${TOKEN}"
# Output:
# {
#   "hendelser":[
#       {
#       "vedtakId": "...",
#       "ytelsesType": "...",
#       "identifikator": "...",
#       "fom": "...",
#       "tom": "..."
#       },
#       {
#       "vedtakId": "...",
#       "ytelsesType": "...",
#       "identifikator": "...",
#       "fom": "...",
#       "tom": "..."
#       }
#   ],
#   "nextUrl": ...
# }
```

Parametere:

| Parameter                   | Beskrivelse                       |
|:----------------------------|:----------------------------------|
| `tpnr`                      | Tjenestepensjonsnummer som vedtaket tilhører. |              
| `side`                      | Sidenummer. |              
| `antall`                    | Antall hendelser per side.        |
| `sekvensnummer`               | Start innlesningen fra dette sekvensnummeret. |

#### Metrikker
Grafana dashboards brukes for å f.eks. monitorere minne, cpu-bruk og andre metrikker.
Se [samordning-hendelse-api grafana dasboard](https://grafana.adeo.no/d/ZxmTPP-mk/samordning-hendelse-api?orgId=1)

#### Logging
[Kibana](https://logs.adeo.no/app/kibana) benyttes til logging. Søk på f.eks. ```application:samordning-hendelse-api AND environment:q``` for logginnslag fra preprod.

#### Bygging
Jenkins benyttes til bygging. Status på bygg finner du her: [samordning-hendelse-api jenkins](https://jenkins-peon.adeo.no/job/samordning-hendelse-api/)

Kontakt Team Peon dersom du har noen spørsmål. Vi finnes blant annet på Slack, i kanalen [#peon](https://nav-it.slack.com/messages/C6M80587R/)