package no.nav.samordning.hendelser.config

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.JdbcTemplate

class FlywayMigrationTest {

    @Test
    fun sequence_indexes_are_created_when_upgrading_from_v8() {
        EmbeddedPostgres.builder().start().use { postgres ->
            val dataSource = postgres.postgresDatabase
            Flyway.configure()
                .dataSource(dataSource)
                .target("8")
                .load()
                .migrate()

            val flyway = Flyway.configure()
                .dataSource(dataSource)
                .load()
            assertEquals(2, flyway.migrate().migrationsExecuted)
            flyway.validate()

            val indexes = JdbcTemplate(dataSource).queryForList(
                """
                SELECT c.relname AS index_name, i.indisvalid AS valid,
                       pg_get_indexdef(i.indexrelid) AS definition
                FROM pg_index i
                JOIN pg_class c ON c.oid = i.indexrelid
                WHERE c.relname IN (
                    'vedtak_hendelse_tpnr_sekvensnummer_idx',
                    'ytelse_hendelser_mottaker_sekvensnummer_idx'
                )
                """.trimIndent()
            ).associateBy { it["index_name"] }

            assertEquals(2, indexes.size)
            assertEquals(true, indexes.getValue("vedtak_hendelse_tpnr_sekvensnummer_idx")["valid"])
            assertEquals(true, indexes.getValue("ytelse_hendelser_mottaker_sekvensnummer_idx")["valid"])
            assertEquals(
                "CREATE INDEX vedtak_hendelse_tpnr_sekvensnummer_idx ON public.vedtak_hendelse USING btree (tpnr, sekvensnummer)",
                indexes.getValue("vedtak_hendelse_tpnr_sekvensnummer_idx")["definition"]
            )
            assertEquals(
                "CREATE INDEX ytelse_hendelser_mottaker_sekvensnummer_idx ON public.ytelse_hendelser USING btree (mottaker, sekvensnummer)",
                indexes.getValue("ytelse_hendelser_mottaker_sekvensnummer_idx")["definition"]
            )
            assertEquals(0, flyway.migrate().migrationsExecuted)
        }
    }
}
