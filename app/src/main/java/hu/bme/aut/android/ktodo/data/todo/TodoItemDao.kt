package hu.bme.aut.android.ktodo.data.todo

import androidx.room.*

@Dao
interface TodoItemDao {
    /**
     * Queries the upcoming tasks.
     */
    @Query("SELECT * FROM tasks WHERE due_date IS NOT NULL AND due_date < date('now', '+30 days') AND completed = 0 ORDER BY due_date, title")
    fun getUpcoming(): List<TodoItem>

    /**
     * Gets all tasks from the given project.
     */
    @Query("SELECT * FROM tasks WHERE project_id = :projectId")
    fun getTasksInProject(projectId: Long): List<TodoItem>

    @Insert
    fun add(todoItem: TodoItem): Long

    @Update
    fun update(todoItem: TodoItem)

    @Delete
    fun delete(todoItem: TodoItem)
}