package no.nav.samordning.hendelser.hendelse

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.postgresql.util.PGobject
import java.io.IOException
import java.sql.SQLException


@Converter
class JpaConverterJson : AttributeConverter<Hendelse, PGobject> {
    override fun convertToDatabaseColumn(meta: Hendelse): PGobject {
        return try {
            val pGobject = PGobject()
            pGobject.type = "jsonb"
            pGobject.value = objectMapper.writeValueAsString(meta)
            pGobject
        } catch (e: JsonProcessingException) {
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

    companion object {
        private val objectMapper: ObjectMapper = ObjectMapper()
            .registerModule(KotlinModule.Builder().build())
            .registerModule(JavaTimeModule())
    }
}
