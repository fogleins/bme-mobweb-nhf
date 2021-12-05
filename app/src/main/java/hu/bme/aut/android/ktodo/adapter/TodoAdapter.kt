package hu.bme.aut.android.ktodo.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.CompoundButtonCompat
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.android.ktodo.MainActivity
import hu.bme.aut.android.ktodo.R
import hu.bme.aut.android.ktodo.data.todo.TodoItem
import hu.bme.aut.android.ktodo.databinding.ItemTodoListBinding
import hu.bme.aut.android.ktodo.enumeration.TaskPriority
import hu.bme.aut.android.ktodo.fragment.MainListViewFragment
import java.time.LocalDate
import kotlin.concurrent.thread

class TodoAdapter(private val listener: TodoItemClickListener) :
    RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    val items = mutableListOf<TodoItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = TodoViewHolder(
        ItemTodoListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val todo = items[position]
        holder.binding.taskTitle.text = todo.title
        holder.binding.taskDescription.text = todo.description
        holder.binding.taskDueDate.text = if (todo.dueDate.toString() != "null") todo.dueDate.toString() else "-"
        val activity = (listener as? MainListViewFragment ?: throw RuntimeException("activity is null")).activity
        val context = listener.context
        // separate thread is needed for getting the project's name
        thread {
            val projectName = if (todo.project != null) todo.project.let {
                MainActivity.database.projectItemDao().getProjectName(it!!)
            } else "Inbox"
            activity!!.runOnUiThread {
                holder.binding.taskProject.text = projectName
            }
        }
        // change calendar icon to red if the task is overdue
        if (todo.dueDate != null && todo.dueDate!!.toEpochDay() < LocalDate.now().toEpochDay()) {
            holder.binding.calendarIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    context!!,
                    R.drawable.ic_baseline_calendar_overdue_36
                )
            )
        } else {
            holder.binding.calendarIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    context!!,
                    R.drawable.ic_baseline_calendar_36
                )
            )
        }

        holder.binding.isCompleted.isChecked = todo.completed
        val color = when(todo.priority) {
            TaskPriority.HIGH -> ColorStateList.valueOf(Color.parseColor("#EE543A"))
            TaskPriority.MEDIUM -> ColorStateList.valueOf(Color.parseColor("#FCB941"))
            TaskPriority.LOW -> ColorStateList.valueOf(Color.parseColor("#2C82C9"))
            TaskPriority.NONE -> ColorStateList.valueOf(Color.GRAY)
        }
        CompoundButtonCompat.setButtonTintList(holder.binding.isCompleted, color)
        holder.binding.isCompleted.setOnCheckedChangeListener { buttonView, isChecked ->
            holder.binding.isCompleted.isChecked = todo.completed
            listener.onTodoCompleted(todo)
        }

        holder.bindListItemClickListener(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    interface TodoItemClickListener {
        fun onItemEdit(item: TodoItem)
        fun onTodoCompleted(item: TodoItem)
        fun onItemRemoved(item: TodoItem)
    }

    inner class TodoViewHolder(val binding: ItemTodoListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindListItemClickListener(item: TodoItem) {
            binding.root.setOnClickListener {
                listener.onItemEdit(item)
            }
        }
    }

    fun addTodo(todo: TodoItem) {
        items.add(todo)
        notifyItemInserted(items.size - 1)
    }

    fun removeTodo(todo: TodoItem) {
        val index = items.indexOf(todo)
        items.remove(todo)
        notifyItemRemoved(index)
    }

    fun updateTodo(todo: TodoItem) {
        notifyItemChanged(items.indexOf(todo))
    }

    fun update(todoItems: List<TodoItem>) {
        items.clear()
        items.addAll(todoItems)
        notifyDataSetChanged()
    }
}