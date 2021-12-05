package hu.bme.aut.android.ktodo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import hu.bme.aut.android.ktodo.data.KTodoDatabase
import hu.bme.aut.android.ktodo.data.project.ProjectItem
import hu.bme.aut.android.ktodo.databinding.ActivityMainBinding
import hu.bme.aut.android.ktodo.enumeration.ListViewType
import hu.bme.aut.android.ktodo.fragment.MainListViewFragment
import java.util.*
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var fragment: MainListViewFragment

    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout

    private val navDrawerUpcomingIndex = 0
    private val navDrawerInboxIndex = 1
    private val navDrawerStatsIndex = 2
    private val navDrawerAddProjectIndex = 3
    private val navDrawerManageProjectsIndex = 4
    private val navDrawerSubmenuIndex = 5

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

        initDrawerItemClickListeners()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        // show the upcoming view by default
        fragment = MainListViewFragment()
        fragmentTransaction.add(binding.mainContent.id, fragment)
        fragmentTransaction.commit()

        populateNavDrawerWithProjectNames()
    }

    override fun onResume() {
        super.onResume()
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

    companion object {
        // use the same database object throughout the app
        lateinit var database: KTodoDatabase
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
                    runOnUiThread {
                        binding.navList.menu.getItem(navDrawerSubmenuIndex).subMenu.clear()
                    }
                    populateNavDrawerWithProjectNames()
                }
            }
            .setNegativeButton(R.string.button_cancel, null)
            .create().show()
    }

    /**
     * Adds all projects to the navigation drawer
     */
    private fun populateNavDrawerWithProjectNames() {
        // update list of projects in the nav drawer
        thread {
            val projects = database.projectItemDao().getProjects()
            val projectsMenu = binding.navList.menu.getItem(navDrawerSubmenuIndex).subMenu
            runOnUiThread {
                for (project in projects) {
                    val menuItem = projectsMenu.add(project.name)
                    menuItem.setOnMenuItemClickListener {
                        val fragmentTransaction = supportFragmentManager.beginTransaction()
                        fragmentTransaction.remove(fragment)
                        fragment = MainListViewFragment(ListViewType.PROJECT, project)
                        fragmentTransaction.add(binding.mainContent.id, fragment)
                        fragmentTransaction.commit()
                        closeDrawer()
                        title = menuItem.title
                        project.name = title.toString()
                        return@setOnMenuItemClickListener true
                    }
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
            val projectsMenu = binding.navList.menu.getItem(navDrawerSubmenuIndex).subMenu
            if (projects.isEmpty() || projectsMenu.size() == 0) return@thread
            projects.forEachIndexed { index, projectItem ->
                val menuItem = projectsMenu.getItem(index)
                if (menuItem.title != projectItem.name) {
                    runOnUiThread {
                        menuItem.title = projectItem.name
                    }
                }
            }
            if (projectsMenu.size() > projects.size) {
                for (i in projects.size until projectsMenu.size()) {
                    runOnUiThread {
                        projectsMenu.getItem(i).isVisible = false
                    }
                }
            }
        }
    }

    private fun closeDrawer() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    /**
     * Adds click event handlers for the items on the nav drawer.
     */
    private fun initDrawerItemClickListeners() {
        binding.navList.menu.getItem(navDrawerUpcomingIndex).setOnMenuItemClickListener {
            if (fragment.listViewType == ListViewType.UPCOMING) {
                closeDrawer()
                return@setOnMenuItemClickListener false
            }
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.remove(fragment)
            fragment = MainListViewFragment(ListViewType.UPCOMING)
            fragmentTransaction.add(binding.mainContent.id, fragment)
            fragmentTransaction.commit()
            closeDrawer()
            return@setOnMenuItemClickListener true
        }
        binding.navList.menu.getItem(navDrawerInboxIndex).setOnMenuItemClickListener {
            if (fragment.listViewType == ListViewType.INBOX) {
                closeDrawer()
                return@setOnMenuItemClickListener false
            }
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.remove(fragment)
            fragment = MainListViewFragment(ListViewType.INBOX)
            fragmentTransaction.add(binding.mainContent.id, fragment)
            fragmentTransaction.commit()
            closeDrawer()
            return@setOnMenuItemClickListener true
        }
        binding.navList.menu.getItem(navDrawerStatsIndex).setOnMenuItemClickListener {
            val intent = Intent(this, StatsActivity::class.java)
            closeDrawer()
            startActivity(intent)
            return@setOnMenuItemClickListener true
        }
        binding.navList.menu.getItem(navDrawerAddProjectIndex).setOnMenuItemClickListener {
            closeDrawer()
            showNewProjectDialog()
            return@setOnMenuItemClickListener true
        }
        binding.navList.menu.getItem(navDrawerManageProjectsIndex).setOnMenuItemClickListener {
            val intent = Intent(this, ProjectManager::class.java)
            closeDrawer()
            startActivity(intent)
            return@setOnMenuItemClickListener true
        }
    }
}