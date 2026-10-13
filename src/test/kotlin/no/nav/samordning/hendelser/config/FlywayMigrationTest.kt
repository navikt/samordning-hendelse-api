package no.nav.samordning.hendelser.config

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.jdbc.core.JdbcTemplate

class FlywayMigrationTest {

    @ParameterizedTest
    @CsvSource("8, 4", "10, 2")
    fun sequence_indexes_are_created_when_upgrading(version: String, expectedMigrations: Int) {
        EmbeddedPostgres.builder().start().use { postgres ->
            val dataSource = postgres.postgresDatabase
            Flyway.configure()
                .dataSource(dataSource)
                .target(version)
                .load()
                .migrate()

            val flyway = Flyway.configure()
                .dataSource(dataSource)
                .load()
            assertEquals(expectedMigrations, flyway.migrate().migrationsExecuted)
            flyway.validate()

            val indexes = JdbcTemplate(dataSource).queryForList(
                """
                SELECT c.relname AS index_name, i.indisvalid AS valid,
                       pg_get_indexdef(i.indexrelid) AS definition
                FROM pg_index i
                JOIN pg_class c ON c.oid = i.indexrelid
                WHERE c.relname IN (
                    'vedtak_hendelse_tpnr_sekvensnummer_idx',
                    'ytelse_hendelser_mottaker_sekvensnummer_idx',
                    'person_endring_tpnr_sekvensnummer_idx',
                    'manglende_refusjonskrav_tpnr_sekvensnummer_idx'
                )
                """.trimIndent()
            ).associateBy { it["index_name"] }

            assertEquals(4, indexes.size)
            assertEquals(true, indexes.getValue("vedtak_hendelse_tpnr_sekvensnummer_idx")["valid"])
            assertEquals(true, indexes.getValue("ytelse_hendelser_mottaker_sekvensnummer_idx")["valid"])
            assertEquals(true, indexes.getValue("person_endring_tpnr_sekvensnummer_idx")["valid"])
            assertEquals(true, indexes.getValue("manglende_refusjonskrav_tpnr_sekvensnummer_idx")["valid"])
            assertEquals(
                "CREATE INDEX vedtak_hendelse_tpnr_sekvensnummer_idx ON public.vedtak_hendelse USING btree (tpnr, sekvensnummer)",
                indexes.getValue("vedtak_hendelse_tpnr_sekvensnummer_idx")["definition"]
            )
            assertEquals(
                "CREATE INDEX ytelse_hendelser_mottaker_sekvensnummer_idx ON public.ytelse_hendelser USING btree (mottaker, sekvensnummer)",
                indexes.getValue("ytelse_hendelser_mottaker_sekvensnummer_idx")["definition"]
            )
            assertEquals(
                "CREATE INDEX person_endring_tpnr_sekvensnummer_idx ON public.person_endring USING btree (tpnr, sekvensnummer)",
                indexes.getValue("person_endring_tpnr_sekvensnummer_idx")["definition"]
            )
            assertEquals(
                "CREATE INDEX manglende_refusjonskrav_tpnr_sekvensnummer_idx ON public.manglende_refusjonskrav USING btree (tpnr, sekvensnummer)",
                indexes.getValue("manglende_refusjonskrav_tpnr_sekvensnummer_idx")["definition"]
            )
            assertEquals(0, flyway.migrate().migrationsExecuted)
        }
    }
}
