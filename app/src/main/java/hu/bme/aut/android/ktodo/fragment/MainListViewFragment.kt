package hu.bme.aut.android.ktodo.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import hu.bme.aut.android.ktodo.MainActivity
import hu.bme.aut.android.ktodo.R
import hu.bme.aut.android.ktodo.adapter.TodoAdapter
import hu.bme.aut.android.ktodo.data.KTodoDatabase
import hu.bme.aut.android.ktodo.data.project.ProjectItem
import hu.bme.aut.android.ktodo.data.todo.TodoItem
import hu.bme.aut.android.ktodo.databinding.FragmentMainListViewBinding
import hu.bme.aut.android.ktodo.enumeration.ListViewType
import java.time.LocalDateTime
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.thread

class MainListViewFragment(
    listViewType: ListViewType = ListViewType.UPCOMING,
    private val project: ProjectItem? = null
) : Fragment(), TodoAdapter.TodoItemClickListener,
    TodoPropertiesDialogFragment.TodoPropertiesDialogListener {

    var listViewType = listViewType
        private set

    private lateinit var binding: FragmentMainListViewBinding

    private lateinit var database: KTodoDatabase
    private lateinit var adapter: TodoAdapter
    private lateinit var activity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity = getActivity() as MainActivity
        binding = FragmentMainListViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // set activity title
        when (listViewType) {
            ListViewType.UPCOMING -> activity.title = "Upcoming"
            ListViewType.INBOX -> activity.title = "Inbox"
            ListViewType.PROJECT -> activity.title = project?.name ?: "Project"
        }

        binding.addTask.setOnClickListener {
            TodoPropertiesDialogFragment(this).show(
                activity.supportFragmentManager,
                TodoPropertiesDialogFragment.TAG
            )
        }
        database = KTodoDatabase.getDatabase(context!!)
        initRecyclerView()
    }

    companion object {
        const val TAG = "MainListViewFragment"
    }

    /**
     * Event handler for when the user clicks a task to edit its properties.
     */
    override fun onItemEdit(item: TodoItem) {
        TodoPropertiesDialogFragment(this, item).show(
            activity.supportFragmentManager,
            TodoPropertiesDialogFragment.TAG
        )
    }

    override fun onTodoCompleted(item: TodoItem) {
        // todo: snackbar undo?
        thread {
            val now = LocalDateTime.now()
            item.completedAt = now
            item.modified = now
            database.todoItemDao().update(item)
            activity.runOnUiThread {
                adapter.updateTodo(item)
                // if a task is marked as complete, we remove it from the list of tasks
                adapter.removeTodo(item)
            }
        }
    }

    override fun onItemRemoved(item: TodoItem) {
        var threadDeleteEnabled = true
        val index = adapter.items.indexOf(item)
        adapter.removeTodo(item)

        val snackbar = Snackbar.make(
            binding.rvTodo,
            activity.applicationContext.getString(
                R.string.snackbar_todo_deleted_message,
                item.title
            ),
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
        binding.rvTodo.layoutManager = LinearLayoutManager(activity)
        binding.rvTodo.adapter = adapter
        // add a divider in between list items
        binding.rvTodo.addItemDecoration(
            DividerItemDecoration(
                activity.applicationContext,
                DividerItemDecoration.VERTICAL
            )
        )
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

    fun loadItemsInBackground() {
        thread {
            val items = when (listViewType) {
                ListViewType.UPCOMING -> database.todoItemDao().getUpcoming()
                ListViewType.INBOX -> database.todoItemDao().getInbox()
                ListViewType.PROJECT -> database.todoItemDao().getTasksInProject(project?.id!!)
            }
            activity.runOnUiThread {
                adapter.update(items)
            }
        }
    }

    override fun onTodoCreated(newItem: TodoItem) {
        thread {
            newItem.id = database.todoItemDao().add(newItem)
            activity.runOnUiThread {
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
        thread {
            editedItem.modified = LocalDateTime.now()
            database.todoItemDao().update(editedItem)
            var items: List<TodoItem>? = null
            var update = false
            if (listViewType == ListViewType.INBOX) {
                items = database.todoItemDao().getInbox()
                update = true
            } else if (listViewType == ListViewType.PROJECT) {
                items = database.todoItemDao().getTasksInProject(project?.id!!)
                update = true
            }
            activity.runOnUiThread {
                if (update) {
                    adapter.update(items!!)
                }
                adapter.updateTodo(editedItem)
            }
        }
    }
}