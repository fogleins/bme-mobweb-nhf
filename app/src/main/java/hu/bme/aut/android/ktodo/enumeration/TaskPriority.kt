package hu.bme.aut.android.ktodo.enumeration

import androidx.room.TypeConverter

enum class TaskPriority {
    NONE, LOW, MEDIUM, HIGH;

    companion object {
        @JvmStatic
        @TypeConverter
        fun getByOrdinal(ordinal: Int): TaskPriority? {
            var ret: TaskPriority? = null
            for (cat in values()) {
                if (cat.ordinal == ordinal) {
                    ret = cat
                    break
                }
            }
            return ret
        }

        @JvmStatic
        @TypeConverter
        fun toInt(priority: TaskPriority): Int {
            return priority.ordinal
        }
    }
}