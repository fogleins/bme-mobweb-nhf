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

class TodoPropertiesDialogFragment(
    private val listener: TodoPropertiesDialogListener,
    private val item: TodoItem? = null
) : DialogFragment() {
    interface TodoPropertiesDialogListener {
        fun onTodoCreated(newItem: TodoItem)
        fun onTodoEdited(editedItem: TodoItem)
    }

    private lateinit var binding: DialogAddTodoBinding

    private lateinit var projects: List<ProjectItem>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        refreshProjects()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAddTodoBinding.inflate(LayoutInflater.from(context))
        val dateFormat = "yyyy-MM-dd"
        val simpleDateFormat = SimpleDateFormat(dateFormat, Locale.forLanguageTag("HU"))
        val calendar: Calendar = Calendar.getInstance()
        val datepicker = DatePickerDialog(requireContext())
        binding.etDueDate.setOnClickListener {
            datepicker.setOnDateSetListener { view, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                binding.etDueDate.setText(simpleDateFormat.format(calendar.time))
            }
            datepicker.show()
        }
        binding.spProject.adapter = ArrayAdapter(
            requireContext(),
            R.layout.support_simple_spinner_dropdown_item,
            projects
        )

        // if editing an existing item
        if (item != null) {
            binding.etTitle.setText(item.title)
            binding.etDescription.setText(item.description)
            var itemProject: ProjectItem? = null
            for (project in projects) {
                if (project.id == item.project) {
                    itemProject = project
                }
            }
            if (itemProject != null)
                binding.spProject.setSelection(projects.indexOf(itemProject))
            else binding.spProject.setSelection(0)
            binding.sbPriority.progress = item.priority.ordinal
            if (item.dueDate != null) {
                calendar.set(
                    item.dueDate!!.year,
                    item.dueDate!!.monthValue - 1,
                    item.dueDate!!.dayOfMonth
                )
                binding.etDueDate.setText(simpleDateFormat.format(calendar.time))
            }

        }

        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.task_properties)
            .setView(binding.root)
            .setPositiveButton(R.string.button_ok) { _, _ ->
                if (item == null) {
                    if (inputIsValid()) {
                        listener.onTodoCreated(getTodoObject())
                    }
                } else {
                    if (inputIsValid()) {
                        // update the task's attributes
                        item.title = binding.etTitle.text.toString()
                        item.description = binding.etDescription.text.toString()
                        item.project = (binding.spProject.selectedItem as ProjectItem).id
                        item.priority = TaskPriority.getByOrdinal(binding.sbPriority.progress)
                            ?: TaskPriority.NONE
                        item.dueDate =
                            if (binding.etDueDate.text.toString().isNotBlank()) LocalDate.parse(
                                binding.etDueDate.text
                            ) else null
                        listener.onTodoEdited(item)
                    }
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
        description = if (binding.etDescription.text.toString()
                .isBlank()
        ) null else binding.etDescription.text.toString(),
        project = (binding.spProject.selectedItem as ProjectItem).id,
        priority = TaskPriority.getByOrdinal(binding.sbPriority.progress) ?: TaskPriority.NONE,
        completed = false,
        dueDate = if (binding.etDueDate.text.toString().isNotBlank()
        ) LocalDate.parse(binding.etDueDate.text) else null
    )

    fun refreshProjects() {
        thread {
            projects = KTodoDatabase.getDatabase(requireContext()).projectItemDao().getProjects()
            // dummy for default value in the spinner
            (projects as MutableList).add(0, ProjectItem(name = "Inbox"))
        }
    }
}