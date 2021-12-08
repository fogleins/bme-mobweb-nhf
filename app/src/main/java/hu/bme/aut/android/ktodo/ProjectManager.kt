package hu.bme.aut.android.ktodo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import hu.bme.aut.android.ktodo.adapter.ProjectAdapter
import hu.bme.aut.android.ktodo.data.KTodoDatabase
import hu.bme.aut.android.ktodo.data.project.ProjectItem
import hu.bme.aut.android.ktodo.databinding.ActivityProjectManagerBinding
import java.time.LocalDateTime
import kotlin.concurrent.thread

class ProjectManager : AppCompatActivity(), ProjectAdapter.ProjectItemClickListener {
    private lateinit var binding: ActivityProjectManagerBinding
    private lateinit var projectAdapter: ProjectAdapter
    private lateinit var database: KTodoDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = getString(R.string.project)
        database = MainActivity.database
        initRecyclerView()
    }

    private fun initRecyclerView() {
        projectAdapter = ProjectAdapter(this)
        binding.rvProjectManager.layoutManager = LinearLayoutManager(this)
        binding.rvProjectManager.adapter = projectAdapter
        thread {
            val items = database.projectItemDao().getProjects()
            runOnUiThread {
                projectAdapter.update(items)
            }
        }
    }

    override fun onProjectItemRemoved(item: ProjectItem) {
        AlertDialog.Builder(this).setTitle(
            applicationContext.getString(
                R.string.dialog_title_confirm_project_deletion,
                item.name
            )
        )
            .setMessage(R.string.dialog_message_confirm_project_deletion)
            .setPositiveButton(R.string.button_yes) { _, _ ->
                thread {
                    // first we remove all tasks in the given project
                    val itemsInProject = database.todoItemDao().getTasksInProject(item.id!!)
                    for (todoItem in itemsInProject) {
                        database.todoItemDao().delete(todoItem)
                    }
                    // and after that we remove the project itself
                    database.projectItemDao().delete(item)
                    runOnUiThread {
                        projectAdapter.removeItem(item)
                    }
                }
            }
            .setNegativeButton(R.string.button_no, null).create().show()
    }

    override fun onProjectItemEdit(item: ProjectItem) {
        val et = EditText(this)
        et.setText(item.name)
        et.inputType = InputType.TYPE_CLASS_TEXT
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_title_add_project))
            .setView(et)
            .setPositiveButton(R.string.button_ok) { _, _ ->
                if (et.text.toString().isNotBlank()) {
                    thread {
                        item.name = et.text.toString()
                        item.modified = LocalDateTime.now()
                        database.projectItemDao().update(item)
                        runOnUiThread {
                            projectAdapter.update(item)
                        }
                    }
                } else {
                    Snackbar.make(binding.root, getString(R.string.warning_project_name_empty), Snackbar.LENGTH_LONG).show()
                }
            }
            .setNegativeButton(R.string.button_cancel, null)
            .create().show()
    }
}