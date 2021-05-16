package es.uniovi.asw.radarinen3b

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import es.uniovi.asw.radarinen3b.databinding.RecyclerViewItemBinding
import es.uniovi.asw.radarinen3b.models.Friend

class CustomAdapter(private val onClick: (Friend) -> Unit) :
    ListAdapter<Friend, CustomAdapter.ViewHolder>(DiffCallback) {
    private lateinit var binding: RecyclerViewItemBinding

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(private var binding: RecyclerViewItemBinding, val onClick: (Friend) -> Unit) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(friend: Friend) {
            binding.friend = friend
            itemView.setOnClickListener {
                onClick(friend)
            }
            // This is important, because it forces the data binding to execute immediately,
            // which allows the RecyclerView to make the correct view size measurements
            binding.executePendingBindings()
        }
    }


    companion object DiffCallback : DiffUtil.ItemCallback<Friend>() {
        override fun areItemsTheSame(oldItem: Friend, newItem: Friend): Boolean {
            return oldItem.webId == newItem.webId
        }

        override fun areContentsTheSame(oldItem: Friend, newItem: Friend): Boolean {
            return (oldItem.distance == null && newItem.distance == null || newItem.distance != null && oldItem.distance != null || oldItem.distance == newItem.distance)
                    && (oldItem.location == null && newItem.location == null || newItem.location != null && oldItem.location != null || oldItem.location == newItem.location)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        binding = RecyclerViewItemBinding.inflate(LayoutInflater.from(viewGroup.context))
        val view = binding.root
        return ViewHolder(binding, onClick)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val friend = getItem(position)
        viewHolder.bind(friend);
    }
    }
