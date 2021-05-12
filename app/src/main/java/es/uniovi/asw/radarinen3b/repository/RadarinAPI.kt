package es.uniovi.asw.radarinen3b.repository

import es.uniovi.asw.radarinen3b.models.LocationPost
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface RadarinAPI {
    @POST("locations")
    suspend fun saveLocation(@Body location: LocationPost): Response<LocationPost>

    @GET("locations")
    suspend fun getLocation(
        @Query("webId") webId: String,
        @Query("last") last: Boolean
    ): Response<LocationPost> //TODO: CHANGE TO LIST AND UNIFY API
}