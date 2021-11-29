package hu.bme.aut.android.ktodo.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import hu.bme.aut.android.ktodo.data.project.ProjectItem
import hu.bme.aut.android.ktodo.data.project.ProjectItemDao
import hu.bme.aut.android.ktodo.data.todo.TodoItem
import hu.bme.aut.android.ktodo.data.todo.TodoItemDao
import hu.bme.aut.android.ktodo.data.typeconverter.DateConverter
import hu.bme.aut.android.ktodo.data.typeconverter.DateTimeConverter
import hu.bme.aut.android.ktodo.enumeration.TaskPriority

@Database(entities = [TodoItem::class, ProjectItem::class], version = 1)
@TypeConverters(value = [TaskPriority::class, DateConverter::class, DateTimeConverter::class])
abstract class KTodoDatabase : RoomDatabase() {
    abstract fun todoItemDao(): TodoItemDao
    abstract fun projectItemDao(): ProjectItemDao

    companion object {
        fun getDatabase(applicationContext: Context): KTodoDatabase {
            return Room.databaseBuilder(
                applicationContext, KTodoDatabase::class.java, "ktodo"
            ).build()
        }
    }
}