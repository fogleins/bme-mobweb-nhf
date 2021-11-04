package hu.bme.aut.android.ktodo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import hu.bme.aut.android.ktodo.adapter.TodoAdapter
import hu.bme.aut.android.ktodo.data.todo.TodoDatabase
import hu.bme.aut.android.ktodo.data.todo.TodoItem
import hu.bme.aut.android.ktodo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), TodoAdapter.TodoItemClickListener {
    private lateinit var binding: ActivityMainBinding

    private lateinit var database: TodoDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = TodoDatabase.getDatabase(applicationContext)

        binding.addTask.setOnClickListener {
            // TODO: show dialog
        }
    }

    override fun onItemChanged(item: TodoItem) {
        TODO("Not yet implemented")
    }

    override fun onItemRemoved(item: TodoItem) {
        TODO("Not yet implemented")
    }
}