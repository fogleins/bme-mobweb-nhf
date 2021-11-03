package hu.bme.aut.android.ktodo.data.typeconverter

import androidx.room.TypeConverter
import java.time.*

class DateTimeConverter {
    @TypeConverter
    fun toDate(dateLong: Long): LocalDateTime {
        return LocalDateTime.ofEpochSecond(dateLong, 0, OffsetDateTime.now(ZoneId.systemDefault()).offset)
    }

    @TypeConverter
    fun fromDate(date: LocalDateTime): Long {
        return date.atZone(ZoneId.systemDefault()).toEpochSecond()
    }
}