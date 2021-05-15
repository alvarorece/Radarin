package es.uniovi.asw.radarinen3b

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.uniovi.asw.radarinen3b.models.Coords
import es.uniovi.asw.radarinen3b.models.Friend
import es.uniovi.asw.radarinen3b.models.User
import kotlinx.coroutines.*

class FriendsViewModel : ViewModel() {
    lateinit var user: MutableLiveData<User>

    val isBanned = MutableLiveData(false)

    enum class FriendsStatus { LOADING, ERROR, DONE }

    private val _status = MutableLiveData<FriendsStatus>()
    val status: LiveData<FriendsStatus> = _status

    private val _users = MutableLiveData<List<Friend>>()
    val users: LiveData<List<Friend>> = _users

    private val _offlineUsers = MutableLiveData<List<Friend>>()
    val offlineUsers: LiveData<List<Friend>> = _offlineUsers

    fun loadData() {
        viewModelScope.launch {
            _status.value = FriendsStatus.LOADING
            try {
                _users.value = loadFriends()
                _status.value = FriendsStatus.DONE
            } catch (e: Exception) {
                _status.value = FriendsStatus.ERROR
                _users.value = listOf()
            }
        }
    }

    fun updateFriends(location: Location) {
        viewModelScope.launch {
            _status.value = FriendsStatus.LOADING
            try {
                val aux = loadFriends()
                updateDistances(aux, location)
                _users.value = aux.filter { fr -> fr.isNear }
                _offlineUsers.value = aux.filter { fr -> !fr.isNear }
                _status.value = FriendsStatus.DONE
            } catch (e: Exception) {
                _status.value = FriendsStatus.ERROR
                _users.value = listOf()
            }
        }
    }

    fun updateDistances(friends: List<Friend>, location: Location) {
        friends.forEach { fr ->
            fr.distance = fr.location?.let { getDistance(location, it).toInt() }
        }
    }

    private suspend fun loadFriends() = coroutineScope {
        val webId = user.value?.webId ?: throw Exception("No id init")
        val friendsTask = async(Dispatchers.IO) { RDFStore.getFriends(webId) }
        val friends = friendsTask.await()
        val locationResponses = (async(Dispatchers.IO) {
            friends.map { fr -> LocationsService.api.getLocation(fr.webId, true) }
        }).await()
        val banned = locationResponses.all { lR -> lR.code() == 401 }
        if (banned) {
            isBanned.value = banned
            return@coroutineScope listOf<Friend>()
        }
        val locations = locationResponses.filter { r -> r.body() != null }.map { f -> f.body() }
        friends.forEach { friend ->
            friend.location = locations.find { location -> friend.webId == location?.webId }?.coords
        }
        return@coroutineScope friends
    }

    private fun getDistance(userLocation: Location, coords: Coords): Float {
        val result = FloatArray(3)
        Location.distanceBetween(
            userLocation.latitude,
            userLocation.longitude,
            coords.latitude,
            coords.longitude,
            result
        );
        return result[0]
    }

    companion object {
        internal const val NEAR_METERS = 2000
    }
}