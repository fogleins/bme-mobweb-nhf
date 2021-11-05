package hu.bme.aut.android.ktodo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import hu.bme.aut.android.ktodo.adapter.TodoAdapter
import hu.bme.aut.android.ktodo.data.todo.TodoDatabase
import hu.bme.aut.android.ktodo.data.todo.TodoItem
import hu.bme.aut.android.ktodo.databinding.ActivityMainBinding
import hu.bme.aut.android.ktodo.fragment.TodoPropertiesDialogFragment
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity(), TodoAdapter.TodoItemClickListener, TodoPropertiesDialogFragment.TodoPropertiesDialogListener {
    private lateinit var binding: ActivityMainBinding

    private lateinit var database: TodoDatabase
    private lateinit var adapter: TodoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = TodoDatabase.getDatabase(applicationContext)

        binding.addTask.setOnClickListener {
            TodoPropertiesDialogFragment().show(
                supportFragmentManager,
                TodoPropertiesDialogFragment.TAG
            )
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
}