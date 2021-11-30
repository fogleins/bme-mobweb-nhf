package hu.bme.aut.android.ktodo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.android.ktodo.data.project.ProjectItem
import hu.bme.aut.android.ktodo.databinding.ItemProjectListBinding

class ProjectAdapter(private val listener: ProjectItemClickListener) :
    RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder>() {

    private val projects = mutableListOf<ProjectItem>()

    interface ProjectItemClickListener {
        fun onProjectItemRemoved(item: ProjectItem)
    }

    inner class ProjectViewHolder(val binding: ItemProjectListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        iewType: Int
    ) = ProjectViewHolder (
        ItemProjectListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val projectItem = projects[position]

        holder.binding.projectName.text = projectItem.name

        holder.binding.btnRemoveProject.setOnClickListener {
            listener.onProjectItemRemoved(projectItem)
        }
    }

    override fun getItemCount(): Int = projects.size

    fun update(projectItems: List<ProjectItem>) {
        projects.clear()
        projects.addAll(projectItems)
        notifyDataSetChanged()
    }

    fun removeItem(item: ProjectItem) {
        val index = projects.indexOf(item)
        projects.remove(item)
        notifyItemRemoved(index)
    }
}