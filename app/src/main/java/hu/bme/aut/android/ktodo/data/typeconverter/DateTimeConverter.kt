package hu.bme.aut.android.ktodo.data.typeconverter

import androidx.room.TypeConverter
import java.time.*

class DateTimeConverter {
    @TypeConverter
    fun toDate(dateLong: Long?): LocalDateTime? {
        return dateLong?.let { LocalDateTime.ofEpochSecond(it, 0, OffsetDateTime.now(ZoneId.systemDefault()).offset) }
    }

    @TypeConverter
    fun fromDate(date: LocalDateTime?): Long? {
        if (date != null) {
            return date.atZone(ZoneId.systemDefault()).toEpochSecond()
        }
        return null
    }
}