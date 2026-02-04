package no.nav.samordning.hendelser.vedtak.hendelse

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.postgresql.util.PGobject
import tools.jackson.core.JacksonException
import tools.jackson.databind.ObjectMapper
import java.io.IOException
import java.sql.SQLException


@Converter
class JpaConverterJson(
    private val objectMapper: ObjectMapper
) : AttributeConverter<Hendelse, PGobject> {
    override fun convertToDatabaseColumn(meta: Hendelse): PGobject {
        return try {
            val pGobject = PGobject()
            pGobject.type = "jsonb"
            pGobject.value = objectMapper.writeValueAsString(meta)
            pGobject
        } catch (e: JacksonException) {
            throw RuntimeException(e)
        } catch (e: SQLException) {
            println(e.message)
            throw RuntimeException(e)
        }
    }

    override fun convertToEntityAttribute(dbData: PGobject): Hendelse {
        return try {
            objectMapper.readValue(dbData.value, Hendelse::class.java)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}
