package hu.bme.aut.android.ktodo.data.todo

import androidx.room.ColumnInfo
import java.time.LocalDateTime

data class TodoProductivityTuple(
    @ColumnInfo(name = "completed_at") val date: LocalDateTime?,
    @ColumnInfo(name = "comp") val isoDate: String?,
    @ColumnInfo(name = "count") val count: Int?
)
