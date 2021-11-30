package hu.bme.aut.android.ktodo

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
import androidx.recyclerview.widget.LinearLayoutManager
import hu.bme.aut.android.ktodo.adapter.TodoAdapter
import hu.bme.aut.android.ktodo.data.KTodoDatabase
import hu.bme.aut.android.ktodo.data.project.ProjectItem
import hu.bme.aut.android.ktodo.data.todo.TodoItem
import hu.bme.aut.android.ktodo.databinding.ActivityMainBinding
import hu.bme.aut.android.ktodo.fragment.TodoPropertiesDialogFragment
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
            if (selectedMenuItem.title == "Add project") {
                Log.d("addProjectClickListener", "add project click listener works")
                showNewProjectDialog()
                selectedMenuItem.isChecked = false
                previouslySelected.isChecked = true
            } else if (selectedMenuItem.title == "Stats") {
                Log.d("statsClickListener", "works")
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            return@setNavigationItemSelectedListener true
        }

        initRecyclerView()
    }

    override fun onItemChanged(item: TodoItem) {
        TODO("Not yet implemented")
    }

    override fun onItemRemoved(item: TodoItem) {
        TODO("Not yet implemented")
    }

    private fun initRecyclerView() {
        adapter = TodoAdapter(this)
        binding.rvTodo.layoutManager = LinearLayoutManager(this)
        binding.rvTodo.adapter = adapter
        loadItemsInBackground()
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
            // todo: do not show tasks where due date = null in the upcoming view
            loadItemsInBackground()
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
            .setTitle("Enter project name")
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