package hu.bme.aut.android.ktodo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import hu.bme.aut.android.ktodo.adapter.TodoAdapter
import hu.bme.aut.android.ktodo.data.KTodoDatabase
import hu.bme.aut.android.ktodo.data.project.ProjectItem
import hu.bme.aut.android.ktodo.data.todo.TodoItem
import hu.bme.aut.android.ktodo.databinding.ActivityMainBinding
import hu.bme.aut.android.ktodo.fragment.TodoPropertiesDialogFragment
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity(), TodoAdapter.TodoItemClickListener,
    TodoPropertiesDialogFragment.TodoPropertiesDialogListener {
    private lateinit var binding: ActivityMainBinding

    private lateinit var database: KTodoDatabase
    private lateinit var adapter: TodoAdapter

    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var selectedMenuItem: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "Upcoming" // shows the upcoming view by default

        database = KTodoDatabase.getDatabase(applicationContext)

        binding.addTask.setOnClickListener {
            TodoPropertiesDialogFragment().show(
                supportFragmentManager,
                TodoPropertiesDialogFragment.TAG
            )
        }

        // drawer layout init
        drawerLayout = binding.drawerLayout
        drawerToggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.nav_drawer_open,
            R.string.nav_drawer_close
        )
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        selectedMenuItem = binding.navList.menu.getItem(0)
        binding.navList.setNavigationItemSelectedListener {
            selectedMenuItem.isChecked = false
            val previouslySelected = selectedMenuItem /* Needed when adding a new project */
            selectedMenuItem = it
            selectedMenuItem.isChecked = true
            Log.d("item", selectedMenuItem.title.toString())
            when (selectedMenuItem.title) {
                "Add project" -> {
                    Log.d("addProjectClickListener", "add project click listener works")
                    showNewProjectDialog()
                    selectedMenuItem.isChecked = false
                    previouslySelected.isChecked = true
                }
                "Manage projects" -> {
                    val intent = Intent(this, ProjectManager::class.java)
                    startActivity(intent)
                }
                "Stats" -> {
                    Log.d("statsClickListener", "works")
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            return@setNavigationItemSelectedListener true
        }

        initRecyclerView()
    }

    override fun onRestart() {
        super.onRestart()
        // refresh the list of tasks when returning to the activity
        loadItemsInBackground()
    }

    /**
     * Event handler for when the user clicks a task to edit its properties.
     */
    override fun onItemEdit(item: TodoItem) {
        TodoPropertiesDialogFragment(item).show(
            supportFragmentManager,
            TodoPropertiesDialogFragment.TAG
        )
    }

    override fun onTodoCompleted(item: TodoItem) {
        thread {
            database.todoItemDao().update(item)
            val index = adapter.items[adapter.items.indexOf(item)]
            runOnUiThread {
                // if a task is marked as complete, we remove it from the upcoming list
                adapter.removeTodo(index)
            }
        }
    }

    override fun onItemRemoved(item: TodoItem) {
        var threadDeleteEnabled = true
        val index = adapter.items.indexOf(item)
        adapter.removeTodo(item)

        val snackbar = Snackbar.make(
            binding.rvTodo,
            applicationContext.getString(R.string.snackbar_todo_deleted_message, item.title),
            Snackbar.LENGTH_LONG
        ).setAction(R.string.button_undo) {
            adapter.items.add(index, item)
            adapter.notifyItemInserted(index)
            threadDeleteEnabled = false
        }
        val snackbarDuration = snackbar.duration
        snackbar.show()

        Timer().schedule(if (snackbarDuration == Snackbar.LENGTH_LONG) 2750 else 1500) {
            thread {
                if (threadDeleteEnabled)
                    database.todoItemDao().delete(item)
            }
        }

    }

    private fun initRecyclerView() {
        adapter = TodoAdapter(this)
        binding.rvTodo.layoutManager = LinearLayoutManager(this)
        binding.rvTodo.adapter = adapter
        loadItemsInBackground()

        // add swipe actions + a snackbar to undo the deletion
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val index = viewHolder.adapterPosition
                val toDelete = adapter.items[index]
                onItemRemoved(toDelete)
            }
        }).attachToRecyclerView(binding.rvTodo)
    }

    private fun loadItemsInBackground() {
        thread {
            val items = database.todoItemDao().getUpcoming()
            runOnUiThread {
                adapter.update(items)
            }
        }
    }

    override fun onTodoCreated(newItem: TodoItem) {
        thread {
            newItem.id = database.todoItemDao().add(newItem)
            runOnUiThread {
                adapter.addTodo(newItem)
            }
            loadItemsInBackground()
        }
    }

    /**
     * Called from the TodoPropertiesDialogFragment class after a task has been updated; applies the
     * changes on the database.
     */
    override fun onTodoEdited(editedItem: TodoItem) {
        // TODO: snack bar undo?
        thread {
            database.todoItemDao().update(editedItem)
            runOnUiThread {
                adapter.updateTodo(editedItem)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (drawerToggle.onOptionsItemSelected(item)) true else super.onOptionsItemSelected(item)

    // do not exit the app/change activity when the drawer is open and the user presses the back button
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else super.onBackPressed()
    }

    /**
     * Displays a text input dialog and adds a new project.
     */
    private fun showNewProjectDialog() {
        val et = EditText(this)
        et.inputType = InputType.TYPE_CLASS_TEXT
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_title_add_project))
            .setView(et)
            .setPositiveButton(R.string.button_ok) { _, _ ->
                thread {
                    database.projectItemDao().add(
                        ProjectItem(
                            name = et.text.toString()
                        )
                    )
                }
            }
            .setNegativeButton(R.string.button_cancel, null)
            .create().show()
    }
}