package hu.bme.aut.android.ktodo.data.todo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import hu.bme.aut.android.ktodo.enumeration.TaskPriority
import java.time.LocalDateTime
import java.util.*

@Entity(tableName = "tasks")
data class TodoItem(
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) var id: Long? = null,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "description") var description: String,
    @ColumnInfo(name = "priority") var priority: TaskPriority,
    @ColumnInfo(name = "project_id") var project: Int,
    @ColumnInfo(name = "due_date") var dueDate: Date,
    @ColumnInfo(name = "completed") var completed: Boolean,
    @ColumnInfo(name = "created") var created: LocalDateTime = LocalDateTime.now(),
    @ColumnInfo(name = "modified") var modified: LocalDateTime = LocalDateTime.now()
) {
}