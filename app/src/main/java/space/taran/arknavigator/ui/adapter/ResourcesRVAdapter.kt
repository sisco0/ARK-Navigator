package space.taran.arknavigator.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import space.taran.arknavigator.R
import space.taran.arknavigator.databinding.ItemFileGridBinding
import space.taran.arknavigator.mvp.presenter.adapter.ResourcesGridPresenter
import space.taran.arknavigator.mvp.view.item.FileItemViewHolder
import space.taran.arknavigator.ui.App

class ResourcesRVAdapter(
    private val presenter: ResourcesGridPresenter
): RecyclerView.Adapter<FileItemViewHolder>() {
    override fun getItemCount() = presenter.getCount()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = FileItemViewHolder(
        ItemFileGridBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        .also {
            App.instance.appComponent.inject(it)
        }

    override fun onBindViewHolder(holder: FileItemViewHolder, position: Int) {
        presenter.bindView(holder)

        holder.itemView.setOnClickListener {
            presenter.onItemClick(position)
        }
    }
}