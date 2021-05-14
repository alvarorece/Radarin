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
    lateinit var user: User

    val isBanned = MutableLiveData(false)

    enum class FriendsStatus { LOADING, ERROR, DONE }

    private val _status = MutableLiveData<FriendsStatus>()
    val status: LiveData<FriendsStatus> = _status

    private val _users = MutableLiveData<List<Friend>>()
    val users: LiveData<List<Friend>> = _users

    init {
        loadData()
    }

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
                _users.value = loadFriends()
                updateDistances(location)
                _status.value = FriendsStatus.DONE
            } catch (e: Exception) {
                _status.value = FriendsStatus.ERROR
                _users.value = listOf()
            }
        }
    }

    fun updateDistances(location: Location) {
        _users.value?.forEach { fr ->
            fr.distance = fr.location?.let { getDistance(location, it).toInt() }
        }
    }

    private suspend fun loadFriends() = coroutineScope {
        val webId = user.webId
        val friendsTask = async { RDFStore.getFriends(webId) }
        val friends = friendsTask.await()
        val locationResponses = (async {
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

    fun getDistance(userLocation: Location, coords: Coords): Float {
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
}