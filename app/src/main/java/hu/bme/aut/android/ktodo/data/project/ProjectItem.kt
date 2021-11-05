package hu.bme.aut.android.ktodo.data.project

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "projects")
data class ProjectItem(
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) var id: Long? = null,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "created") var created: LocalDateTime = LocalDateTime.now(),
    @ColumnInfo(name = "modified") var modified: LocalDateTime = LocalDateTime.now()
) {
}