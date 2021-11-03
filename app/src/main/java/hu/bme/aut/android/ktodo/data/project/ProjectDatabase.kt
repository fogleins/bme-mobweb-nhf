package hu.bme.aut.android.ktodo.data.project

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ProjectItem::class], version = 1)
abstract class ProjectDatabase : RoomDatabase() {
    abstract fun projectItemDao(): ProjectItemDao

    companion object {
        fun getDatabasae(applicationContext: Context): ProjectDatabase {
            return Room.databaseBuilder(
                applicationContext, ProjectDatabase::class.java, "ktodo"
            ).build()
        }
    }
}