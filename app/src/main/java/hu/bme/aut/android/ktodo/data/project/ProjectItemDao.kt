package hu.bme.aut.android.ktodo.data.project

import androidx.room.*

@Dao
interface ProjectItemDao {
    @Query("SELECT * FROM projects")
    fun getProjects(): List<ProjectItem>

    @Query("SELECT name FROM projects WHERE id = :projectId")
    fun getProjectName(projectId: Long): String

    @Insert
    fun add(projectItem: ProjectItem): Long

    @Update
    fun update(projectItem: ProjectItem)

    @Delete
    fun delete(projectItem: ProjectItem)
}