package no.nav.samordning.hendelser.ytelse

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import java.sql.Timestamp
import java.time.LocalDateTime


@Converter(autoApply = true)
class LocalDateTimeAttributeConverter : AttributeConverter<LocalDateTime, Timestamp> {
    override fun convertToDatabaseColumn(locDateTime: LocalDateTime?): Timestamp? {
        return if (locDateTime == null) null else Timestamp.valueOf(locDateTime)
    }

    override fun convertToEntityAttribute(sqlTimestamp: Timestamp?): LocalDateTime? {
        return sqlTimestamp?.toLocalDateTime()
    }
}