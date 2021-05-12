package es.uniovi.asw.radarinen3b

import android.util.Base64
import es.uniovi.asw.radarinen3b.repository.RadarinAPI
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec

object LocationsService {
    private val BASE_URL = "https://radarinen3brestapi.herokuapp.com/api/"
    lateinit var prKey: String
    lateinit var webId: String
    val api: RadarinAPI by lazy {
        val keyContent = prKey.replace("\n","").replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "");
        val keyFact = KeyFactory.getInstance("RSA")
        val bytes = Base64.decode(keyContent, Base64.DEFAULT);
        val spec = PKCS8EncodedKeySpec(bytes)
        val pr = keyFact.generatePrivate(spec)
        val token = Jwts.builder().setSubject("test").claim("webid", webId).signWith(
            pr, SignatureAlgorithm.RS256
        ).compact()
        val client = OkHttpClient().newBuilder().addInterceptor { chain ->
            val og = chain.request()
            val reqBuilder = og.newBuilder().header("Authorization", "Bearer $token")
            val req = reqBuilder.build()
            chain.proceed(req)
        }.build()
        return@lazy Retrofit.Builder().baseUrl(BASE_URL).client(client)
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(RadarinAPI::class.java)
    }
}