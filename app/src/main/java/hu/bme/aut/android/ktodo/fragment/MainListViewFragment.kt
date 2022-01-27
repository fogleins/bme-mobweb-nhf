package hu.bme.aut.android.ktodo.fragment

import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
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
            ListViewType.UPCOMING -> activity.title = getString(R.string.upcoming)
            ListViewType.INBOX -> activity.title = getString(R.string.inbox)
            ListViewType.PROJECT -> activity.title = project?.name ?: getString(R.string.project)
        }

        binding.addTask.setOnClickListener {
            TodoPropertiesDialogFragment(this).show(
                activity.supportFragmentManager,
                TodoPropertiesDialogFragment.TAG
            )
        }
        database = MainActivity.database
        initRecyclerView()
    }

    companion object {
        const val TAG = "MainListViewFragment"
        var shouldUpdateEntireList = false
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
        var threadCompleteEnabled = true
        val index = adapter.items.indexOf(item)
        adapter.updateTodo(item)
        adapter.removeTodo(item)
        val now = LocalDateTime.now()
        // make sure the task is marked as complete even if the activity is destroyed before the
        // timer would run
        MainActivity.pendingCompletions.add(item)

        val snackbar = Snackbar.make(
            binding.rvTodo,
            activity.applicationContext.getString(
                R.string.snackbar_todo_completed_message,
                item.title
            ),
            Snackbar.LENGTH_LONG
        ).setAction(R.string.button_undo) {
            adapter.items.add(index, item)
            adapter.notifyItemInserted(index)
            threadCompleteEnabled = false
        }
        val snackbarDuration = snackbar.duration
        snackbar.show()

        Timer().schedule(if (snackbarDuration == Snackbar.LENGTH_LONG) 2750 else 1500) {
            if (threadCompleteEnabled) {
                item.completedAt = now
                item.completed = true
                item.modified = now
                database.todoItemDao().update(item)
                MainActivity.pendingCompletions.remove(item)
            }
        }
    }

    override fun onItemRemoved(item: TodoItem) {
        var threadDeleteEnabled = true
        val index = adapter.items.indexOf(item)
        adapter.removeTodo(item)
        // make sure the task is deleted even if the activity is destroyed before the
        // timer would run
        MainActivity.pendingDeletions.add(item)

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
            if (threadDeleteEnabled) {
                database.todoItemDao().delete(item)
                MainActivity.pendingDeletions.remove(item)
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
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            val paint = Paint()
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val index = viewHolder.adapterPosition
                val toDelete = adapter.items[index]
                onItemRemoved(toDelete)
                paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )

                val deleteDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_swipe_to_delete_24, null)
                val mBackground = ColorDrawable()
                val backgroundColor = Color.parseColor("#b80f0a")
                val intrinsicWidth = deleteDrawable!!.intrinsicWidth;
                val intrinsicHeight = deleteDrawable.intrinsicHeight

                val itemView = viewHolder.itemView
                val itemHeight = itemView.height

                val isCancelled = dX == 0f && !isCurrentlyActive

                if (isCancelled) {
                    clearCanvas(
                        c, itemView.right + dX,
                        itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat()
                    )
                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                    return
                }

                mBackground.color = backgroundColor
                mBackground.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
                mBackground.draw(c)

                val deleteIconTop: Int = itemView.top + (itemHeight - intrinsicHeight) / 2
                val deleteIconMargin: Int = (itemHeight - intrinsicHeight) / 2
                val deleteIconLeft: Int = itemView.right - deleteIconMargin - intrinsicWidth
                val deleteIconRight = itemView.right - deleteIconMargin
                val deleteIconBottom: Int = deleteIconTop + intrinsicHeight


                deleteDrawable.setBounds(
                    deleteIconLeft,
                    deleteIconTop,
                    deleteIconRight,
                    deleteIconBottom
                )
                deleteDrawable.draw(c)

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )


            }

            private fun clearCanvas(
                c: Canvas,
                left: Float,
                top: Float,
                right: Float,
                bottom: Float
            ) {
                c.drawRect(left, top, right, bottom, paint)
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
            if (shouldUpdateEntireList) {
                when (listViewType) {
                    ListViewType.INBOX -> {
                        items = database.todoItemDao().getInbox()
                        update = true
                    }
                    ListViewType.PROJECT -> {
                        items = database.todoItemDao().getTasksInProject(project?.id!!)
                        update = true
                    }
                    else -> {
                        items = database.todoItemDao().getUpcoming()
                        update = true
                    }
                }
            }
            activity.runOnUiThread {
                if ((update || shouldUpdateEntireList) && items != null) {
                    adapter.update(items)
                }
                adapter.updateTodo(editedItem)
            }
        }
    }
}