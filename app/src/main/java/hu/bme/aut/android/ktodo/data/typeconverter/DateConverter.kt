package hu.bme.aut.android.ktodo.data.typeconverter

import androidx.room.TypeConverter
import java.time.LocalDate

class DateConverter {

    @TypeConverter
    fun toDate(dateLong: Long?): LocalDate? = dateLong?.let { LocalDate.ofEpochDay(it) }

    @TypeConverter
    fun fromDate(date: LocalDate?): Long? = date?.toEpochDay()
}