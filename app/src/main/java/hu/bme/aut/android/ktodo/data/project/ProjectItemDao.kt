package hu.bme.aut.android.ktodo.data.project

import androidx.room.*

@Dao
interface ProjectItemDao {
    @Query("SELECT * FROM projects WHERE deleted = 0")
    fun getProjects()

    @Insert
    fun add(projectItem: ProjectItem): Long

    @Update
    fun update(projectItem: ProjectItem)

    @Delete
    fun delete(projectItem: ProjectItem)
}