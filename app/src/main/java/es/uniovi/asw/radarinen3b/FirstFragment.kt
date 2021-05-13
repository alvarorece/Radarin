package es.uniovi.asw.radarinen3b

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import es.uniovi.asw.radarinen3b.databinding.FragmentFirstBinding
import es.uniovi.asw.radarinen3b.dialogs.SavedLocationDialogFragment
import es.uniovi.asw.radarinen3b.location.ForegroundOnlyLocationService
import es.uniovi.asw.radarinen3b.location.ForegroundOnlyLocationService.Companion.getDistance
import es.uniovi.asw.radarinen3b.location.SharedPreferenceUtil
import es.uniovi.asw.radarinen3b.models.Friend
import es.uniovi.asw.radarinen3b.models.User
import kotlinx.coroutines.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val TAG = "FirstFragment"

class FirstFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener{
    private lateinit var user: User
    private lateinit var binding: FragmentFirstBinding
    private lateinit var friends: MutableList<Friend>

    private var foregroundOnlyLocationServiceBound = false

    // Provides location updates for while-in-use feature.
    private var foregroundOnlyLocationService: ForegroundOnlyLocationService? = null

    // Listens for location broadcasts from ForegroundOnlyLocationService.
    private lateinit var foregroundOnlyBroadcastReceiver: ForegroundOnlyBroadcastReceiver

    private lateinit var sharedPreferences: SharedPreferences

    private val foregroundOnlyServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as ForegroundOnlyLocationService.LocalBinder
            foregroundOnlyLocationService = binder.service
            foregroundOnlyLocationServiceBound = true
            val enabled = sharedPreferences.getBoolean(
                SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false
            )
            if (!enabled) {
                foregroundOnlyLocationService?.unsubscribeToLocationUpdates()
            } else {
                // TODO: Step 1.0, Review Permissions: Checks and requests if needed.
                if (foregroundPermissionApproved()) {
                    foregroundOnlyLocationService?.subscribeToLocationUpdates()
                        ?: Log.d(TAG, "Service Not Bound")
                } else {
                    requestForegroundPermissions()
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            foregroundOnlyLocationService = null
            foregroundOnlyLocationServiceBound = false
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFirstBinding.inflate(layoutInflater, container, false)
        val view = binding.root
        val divider =
            DividerItemDecoration(binding.recyclerV.context, DividerItemDecoration.VERTICAL)
        binding.recyclerV.addItemDecoration(divider)
        friends = mutableListOf()
        val adapter = CustomAdapter(
            friends
        )
        binding.recyclerV.adapter = adapter
        binding.recyclerV.layoutManager = LinearLayoutManager(requireContext())

        return view
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val webIdPref = pref.getString(getString(R.string.webId_preference), "")
        val privatePref = pref.getString(getString(R.string.privateKey_preference), "")
        if (webIdPref == "" || privatePref == "") {
            val action = FirstFragmentDirections.actionFirstFragmentToQrLoginFragment()
            view.findNavController().navigate(action)
        } else {
            user = User(webIdPref!!, privatePref!!)
            foregroundOnlyBroadcastReceiver = ForegroundOnlyBroadcastReceiver()
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
            LocationsService.webId = user.webId
            LocationsService.prKey = user.privateKey
            val lClient = LocationServices.getFusedLocationProviderClient(requireContext())
            val location = lClient.lastLocation.addOnSuccessListener { l ->
                CoroutineScope(Dispatchers.IO).launch { updateView(l) }
            }
            binding.swipe.setOnRefreshListener {
                lClient.lastLocation.addOnSuccessListener { l ->
                    CoroutineScope(Dispatchers.IO).launch { updateView(l) }.invokeOnCompletion {
                        binding.swipe.isRefreshing = false
                    }
                }
            }
            binding.floatingActionButton.setOnClickListener {
                lClient.lastLocation.addOnSuccessListener {
                    SavedLocationDialogFragment(it.longitude.toInt(), it.latitude.toInt())
                        .show(parentFragmentManager, "Location")
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        val serviceIntent = Intent(requireActivity(), ForegroundOnlyLocationService::class.java)
        requireActivity().bindService(
            serviceIntent,
            foregroundOnlyServiceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            foregroundOnlyBroadcastReceiver,
            IntentFilter(
                ForegroundOnlyLocationService.ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST
            )
        )
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(
            foregroundOnlyBroadcastReceiver
        )
        super.onPause()
    }

    override fun onStop() {
        if (foregroundOnlyLocationServiceBound) {
            requireActivity().unbindService(foregroundOnlyServiceConnection)
            foregroundOnlyLocationServiceBound = false
        }
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)

        super.onStop()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        // Updates button states if new while in use location is added to SharedPreferences.
        if (key == SharedPreferenceUtil.KEY_FOREGROUND_ENABLED) {
        }
    }

    // TODO: Step 1.0, Review Permissions: Method checks if permissions approved.
    private fun foregroundPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    // TODO: Step 1.0, Review Permissions: Method requests permissions.
    private fun requestForegroundPermissions() {
        val provideRationale = foregroundPermissionApproved()

        // If the user denied a previous request, but didn't check "Don't ask again", provide
        // additional rationale.
        if (provideRationale) {
            Snackbar.make(
                requireView(),
                R.string.permission_rationale,
                Snackbar.LENGTH_LONG
            )
                .setAction(R.string.ok) {
                    // Request permission
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
                    )
                }
                .show()
        } else {
            Log.d(TAG, "Request foreground only permission")
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    // TODO: Step 1.0, Review Permissions: Handles permission result.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionResult")

        when (requestCode) {
            REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE -> when {
                grantResults.isEmpty() ->
                    // If user interaction was interrupted, the permission request
                    // is cancelled and you receive empty arrays.
                    Log.d(TAG, "User interaction was cancelled.")
                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
                    // Permission was granted.
                    foregroundOnlyLocationService?.subscribeToLocationUpdates()
                else -> {
                    // Permission denied.

                    Snackbar.make(
                        requireView(),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(R.string.settings) {
                            // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts(
                                "package",
                                BuildConfig.APPLICATION_ID,
                                null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        .show()
                }
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        requireContext().checkSelfPermission(
            it
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Receiver for location broadcasts from [ForegroundOnlyLocationService].
     */
    private inner class ForegroundOnlyBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val location = intent.getParcelableExtra<Location>(
                ForegroundOnlyLocationService.EXTRA_LOCATION
            )
            if (location != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    updateView(location)
                }
            }
        }
    }

    private fun logOut() {
        val pref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        with(pref.edit()) {
            putString(getString(R.string.webId_preference), "")
            putString(getString(R.string.privateKey_preference), "")
            commit()
        }
        val action = FirstFragmentDirections.actionFirstFragmentToQrLoginFragment()
        requireView().findNavController().navigate(action)
    }

    private suspend fun updateView(currentLocation: Location) =
        coroutineScope {
            val pref = requireActivity().getPreferences(Context.MODE_PRIVATE)
            val webId = pref.getString(getString(R.string.webId_preference), "")
            val friendsTask = async { RDFStore.getFriends(webId!!) }
            val fetchedFriends = friendsTask.await()
            val task = async {
                fetchedFriends.map { fr ->
                    LocationsService.api.getLocation(fr.webId, true)
                }
            }
            val locationResponses = task.await()

            // If everyone, else is just removed friend
            if (locationResponses.all { response -> response.code() == 401 }) {
                logOut()
            } else {
                val locations =
                    locationResponses.filter { r -> r.body() != null }.map { f -> f.body() }
                fetchedFriends.forEach { friend ->
                    val find = locations.find { location -> friend.webId == location?.webId }
                    friend.location = find?.coords
                    friend.distance =
                        friend.location?.let { getDistance(currentLocation, it).toInt() }
                }
                requireActivity().runOnUiThread {
                    friends.clear()
                    friends.addAll(fetchedFriends)
                    binding.recyclerV.adapter?.notifyDataSetChanged()
                }
            }
        }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 12
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }


}