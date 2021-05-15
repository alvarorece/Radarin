package es.uniovi.asw.radarinen3b

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.LocationServices
import es.uniovi.asw.radarinen3b.databinding.FragmentOfflineFriendsBinding
import es.uniovi.asw.radarinen3b.databinding.FragmentOnlineFriendsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class OfflineFriendsFragment : Fragment() {
    private val model: FriendsViewModel by activityViewModels()
    private lateinit var binding: FragmentOfflineFriendsBinding

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOfflineFriendsBinding.inflate(inflater, container, false)
        val view = binding.root
        val divider =
            DividerItemDecoration(binding.recyclerVOnline.context, DividerItemDecoration.VERTICAL)
        binding.lifecycleOwner = this
        binding.viewModel = model
        binding.recyclerVOnline.addItemDecoration(divider)
        binding.recyclerVOnline.adapter = CustomAdapter()
        binding.recyclerVOnline.layoutManager = LinearLayoutManager(requireContext())
        val lClient = LocationServices.getFusedLocationProviderClient(requireContext())
        binding.swipe.setOnRefreshListener {
            lClient.lastLocation.addOnSuccessListener { l ->
                CoroutineScope(Dispatchers.IO).launch {
                    model.updateFriends(l)
                }.invokeOnCompletion {
                    binding.swipe.isRefreshing = false
                }
            }
        }
        return view
    }

}