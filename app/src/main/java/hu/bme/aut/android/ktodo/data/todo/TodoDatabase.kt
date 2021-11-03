package hu.bme.aut.android.ktodo.data.todo

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import hu.bme.aut.android.ktodo.data.typeconverter.DateConverter
import hu.bme.aut.android.ktodo.data.typeconverter.DateTimeConverter
import hu.bme.aut.android.ktodo.enumeration.TaskPriority

@Database(entities = [TodoItem::class], version = 1)
@TypeConverters(value = [TaskPriority::class, DateConverter::class, DateTimeConverter::class])
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoItemDao(): TodoItemDao

    companion object {
        fun getDatabase(applicationContext: Context): TodoDatabase {
            return Room.databaseBuilder(
                applicationContext, TodoDatabase::class.java, "ktodo"
            ).build()
        }
    }
}