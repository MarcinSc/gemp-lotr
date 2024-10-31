package org.ccgemp.db

import org.sql2o.converters.Converter
import org.sql2o.converters.ConverterException
import java.sql.Date
import java.time.LocalDate
import java.time.ZoneOffset

class LocalDateConverter : Converter<LocalDate?> {
    @Throws(ConverterException::class)
    override fun convert(value: Any): LocalDate? =
        if (value is Date) {
            value.toLocalDate()
        } else {
            null
        }

    override fun toDatabaseParam(value: LocalDate?): Date? =
        if (value != null) {
            Date(value.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())
        } else {
            null
        }
}
