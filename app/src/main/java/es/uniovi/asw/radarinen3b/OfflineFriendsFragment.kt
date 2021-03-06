package es.uniovi.asw.radarinen3b

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
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
import es.uniovi.asw.radarinen3b.models.Friend
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
            DividerItemDecoration(binding.recyclerVOffline.context, DividerItemDecoration.VERTICAL)
        binding.lifecycleOwner = this
        binding.viewModel = model
        binding.recyclerVOffline.addItemDecoration(divider)
        binding.recyclerVOffline.adapter = CustomAdapter(::friendOpenMaps)
        binding.recyclerVOffline.layoutManager = LinearLayoutManager(requireContext())
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

    private fun friendOpenMaps(friend: Friend) {
        if (friend.location != null) {
            val label = (friend.fn ?: friend.webId)
            val intentUri =
                Uri.parse("http://maps.google.com/maps?q=${friend.location!!.latitude},${friend.location!!.longitude} (${label})&iwloc=A&hl=es")
            val intent = Intent(Intent.ACTION_VIEW, intentUri)
            intent.setPackage("com.google.android.apps.maps")
            startActivity(intent)
        }
    }

}