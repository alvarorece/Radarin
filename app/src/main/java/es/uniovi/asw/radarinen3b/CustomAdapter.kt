package es.uniovi.asw.radarinen3b

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import es.uniovi.asw.radarinen3b.databinding.RecyclerViewItemBinding
import es.uniovi.asw.radarinen3b.models.Friend

class CustomAdapter(private val dataSet: List<Friend>) :
    RecyclerView.Adapter<CustomAdapter.ViewHolder>() {
    private lateinit var binding: RecyclerViewItemBinding

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(binding: RecyclerViewItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {

        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        binding = RecyclerViewItemBinding.inflate(LayoutInflater.from(viewGroup.context))
        val view = binding.root
        return ViewHolder(binding)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
         val friend = dataSet[position]
        binding.NameTxt.text = friend.fn
        binding.webIdTxt.text = friend.webId
        if (friend.distance == null)
            binding.distanceTxt.text = "Disconnected"
        else
            binding.distanceTxt.text = "${friend.distance}m"
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}
