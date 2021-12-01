package hu.bme.aut.android.ktodo.fragment

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import hu.bme.aut.android.ktodo.R
import hu.bme.aut.android.ktodo.data.KTodoDatabase
import hu.bme.aut.android.ktodo.data.project.ProjectItem
import hu.bme.aut.android.ktodo.data.todo.TodoItem
import hu.bme.aut.android.ktodo.databinding.DialogAddTodoBinding
import hu.bme.aut.android.ktodo.enumeration.TaskPriority
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import kotlin.concurrent.thread

class TodoPropertiesDialogFragment : DialogFragment() {
    interface TodoPropertiesDialogListener {
        fun onTodoCreated(newItem: TodoItem)
    }

    private lateinit var listener: TodoPropertiesDialogListener

    private lateinit var binding: DialogAddTodoBinding

    private lateinit var projects: List<ProjectItem>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? TodoPropertiesDialogListener
            ?: throw RuntimeException("Activity must implement the TodoPropertiesDialogListener interface!")
        refreshProjects()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAddTodoBinding.inflate(LayoutInflater.from(context))
        binding.etDueDate.setOnClickListener {
            val calendar: Calendar = Calendar.getInstance()
            val datepicker = DatePickerDialog(requireContext())
            datepicker.setOnDateSetListener { view, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val dateFormat = "yyyy-MM-dd"
                val simpleDateFormat = SimpleDateFormat(dateFormat, Locale.forLanguageTag("HU"))
                binding.etDueDate.setText(simpleDateFormat.format(calendar.time))
            }
            datepicker.show()
        }
        binding.spProject.adapter = ArrayAdapter(
            requireContext(),
            R.layout.support_simple_spinner_dropdown_item,
            projects
        )

        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.task_properties)
            .setView(binding.root)
            .setPositiveButton(R.string.button_ok) { dialogInterface, i ->
                if (inputIsValid()) {
                    listener.onTodoCreated(getTodoObject())
                }
            }
            .setNegativeButton(R.string.button_cancel, null)
            .create()
    }

    companion object {
        const val TAG = "TaskPropertiesDialogFragment"
    }

    private fun inputIsValid() = binding.etTitle.text.isNotBlank()

    private fun getTodoObject() = TodoItem(
        title = binding.etTitle.text.toString(),
        description = if (binding.etDescription.text.toString().isBlank()) null else binding.etDescription.text.toString(),
        project = (binding.spProject.selectedItem as ProjectItem).id,
        priority = TaskPriority.getByOrdinal(binding.sbPriority.progress) ?: TaskPriority.NONE,
        completed = false,
        dueDate = if (binding.etDueDate.text.toString().isNotBlank()) LocalDate.parse(binding.etDueDate.text) else null
    )

    fun refreshProjects() {
        thread {
            projects = KTodoDatabase.getDatabase(requireContext()).projectItemDao().getProjects()
            // dummy for default value in the spinner
            (projects as MutableList).add(0, ProjectItem(name = "Inbox"))
        }
    }
}