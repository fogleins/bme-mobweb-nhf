package hu.bme.aut.android.ktodo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.android.ktodo.data.KTodoDatabase
import hu.bme.aut.android.ktodo.data.todo.TodoItem
import hu.bme.aut.android.ktodo.databinding.ItemTodoListBinding
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
        holder.binding.taskDueDate.text = todo.dueDate.toString()
        val activity = listener as AppCompatActivity
        val context = activity.applicationContext
        // separate thread is needed for getting the project's name
        thread {
            val projectName = if (todo.project != null) todo.project.let {
                // TODO: menjen át a main activitybe?
                KTodoDatabase.getDatabase(context).projectItemDao().getProjectName(it!!)
            } else "Inbox"
            activity.runOnUiThread {
                holder.binding.taskProject.text = projectName
            }
        }

        holder.binding.isCompleted.setOnCheckedChangeListener { buttonView, isChecked ->
            todo.completed = isChecked
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