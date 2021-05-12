package es.uniovi.asw.radarinen3b.models

import kotlinx.serialization.Serializable

@Serializable
data class User(val webId: String, val privateKey: String)
