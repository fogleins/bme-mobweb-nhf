package hu.bme.aut.android.ktodo.data.todo

import androidx.room.ColumnInfo

data class TodoProductivityTuple(
    @ColumnInfo(name = "comp") val date: String?,
    @ColumnInfo(name = "count") val count: String?
)
