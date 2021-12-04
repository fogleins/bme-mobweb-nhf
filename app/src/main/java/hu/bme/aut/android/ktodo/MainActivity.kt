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
import hu.bme.aut.android.ktodo.data.KTodoDatabase
import hu.bme.aut.android.ktodo.data.project.ProjectItem
import hu.bme.aut.android.ktodo.databinding.ActivityMainBinding
import hu.bme.aut.android.ktodo.fragment.MainListViewFragment
import java.util.*
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var database: KTodoDatabase
    private lateinit var fragment: MainListViewFragment

    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var selectedMenuItem: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = getString(R.string.upcoming) // shows the upcoming view by default

        database = KTodoDatabase.getDatabase(applicationContext)

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
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        // show the upcoming view by default
        fragment = MainListViewFragment()
        fragmentTransaction.add(binding.mainContent.id, fragment)
        fragmentTransaction.commit()

        populateNavDrawerWithProjectNames()
    }

    override fun onRestart() {
        super.onRestart()
        // refresh the list of tasks when returning to the activity
        fragment.loadItemsInBackground()
        updateNavDrawerProjects()
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

    /**
     * Adds all projects
     */
    private fun populateNavDrawerWithProjectNames() {
        // update list of projects in the nav drawer
        thread {
            val projects = database.projectItemDao().getProjects()
            val projectsMenu = binding.navList.menu.getItem(3).subMenu
            runOnUiThread {
                projectsMenu.add(getString(R.string.project_default_inbox))
                for (project in projects) {
                    projectsMenu.add(project.name)
                    // todo: add onClickListener for the menu item
                }
            }
        }
    }

    /**
     * Checks whether a project's name has been modified, and updates the list of projects in the
     * nav drawer if needed.
     */
    private fun updateNavDrawerProjects() {
        thread {
            val projects = database.projectItemDao().getProjects()
            val projectsMenu = binding.navList.menu.getItem(3).subMenu
            projects.forEachIndexed { index, projectItem ->
                val menuItem = projectsMenu.getItem(index)
                if (menuItem.title != projectItem.name) {
                    runOnUiThread {
                        menuItem.title = projectItem.name
                    }
                }
            }
        }
    }
}