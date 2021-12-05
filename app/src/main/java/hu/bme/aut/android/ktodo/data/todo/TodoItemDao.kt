package hu.bme.aut.android.ktodo.data.todo

import androidx.room.*

@Dao
interface TodoItemDao {
    /**
     * Queries the upcoming tasks.
     */
    @Query("SELECT * FROM tasks WHERE due_date IS NOT NULL AND due_date < date('now', '+30 days') AND completed = 0 ORDER BY due_date, priority, title")
    fun getUpcoming(): List<TodoItem>

    @Query("SELECT * FROM tasks WHERE project_id IS NULL AND completed = 0 ORDER BY due_date, priority, title")
    fun getInbox(): List<TodoItem>

    /**
     * Gets all tasks from the given project.
     */
    @Query("SELECT * FROM tasks WHERE project_id = :projectId AND completed = 0 ORDER BY due_date, priority, title")
    fun getTasksInProject(projectId: Long): List<TodoItem>

    @Query("SELECT completed_at, DATETIME(completed_at, 'unixepoch') AS comp, COUNT(*) as count FROM tasks " +
            "WHERE completed = 1 AND DATETIME(completed_at, 'unixepoch') BETWEEN datetime('now', '-7 days') " +
            "AND datetime('now') GROUP BY strftime('%Y', comp), strftime('%m', comp), strftime('%d', comp) " +
            "ORDER BY comp ASC")
    fun getProductivity7days(): List<TodoProductivityTuple>

    @Query("SELECT completed_at, DATETIME(completed_at, 'unixepoch') AS comp, COUNT(*) as count FROM tasks " +
            "WHERE completed = 1 AND DATETIME(completed_at, 'unixepoch') BETWEEN datetime('now', '-30 days') " +
            "AND datetime('now') GROUP BY strftime('%Y', comp), strftime('%m', comp), strftime('%d', comp) " +
            "ORDER BY comp ASC")
    fun getProductivity30days(): List<TodoProductivityTuple>

    @Insert
    fun add(todoItem: TodoItem): Long

    @Update
    fun update(todoItem: TodoItem)

    @Delete
    fun delete(todoItem: TodoItem)
}