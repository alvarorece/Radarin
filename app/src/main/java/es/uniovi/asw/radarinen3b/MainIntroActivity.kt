package es.uniovi.asw.radarinen3b

import android.os.Bundle
import com.heinrichreimersoftware.materialintro.app.IntroActivity
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide


class MainIntroActivity : IntroActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addSlide(
            SimpleSlide.Builder()
                .title("Welcome to Radarin")
                .description("Please, scan your webapp QRCode")
                .image(R.drawable.qr)
                .background(R.color.purple_200)
                .backgroundDark(R.color.purple_500)
                .scrollable(false)
                .build()
        )
        addSlide(
            SimpleSlide.Builder()
                .title("We need your permission")
                .description("For the app to work you need to allow us to track your location")
                .image(R.drawable.editpermission)
                .background(R.color.purple_200)
                .backgroundDark(R.color.purple_500)
                .scrollable(false)
                .build()
        )
        addSlide(
            SimpleSlide.Builder()
                .title("We need your permission")
                .description("Please, turn on the switch and accept Android's permission")
                .image(R.drawable.givenpermission)
                .background(R.color.purple_200)
                .backgroundDark(R.color.purple_500)
                .scrollable(false)
                .build()
        )
        addSlide(
            SimpleSlide.Builder()
                .title("Friendship!")
                .description("You can take a look at your friends and where they are")
                .image(R.drawable.friendsview)
                .background(R.color.purple_200)
                .backgroundDark(R.color.purple_500)
                .scrollable(false)
                .build()
        )
        addSlide(
            SimpleSlide.Builder()
                .title("Save in your POD!")
                .description("Share locations with your friends so they can see cool places")
                .image(R.drawable.addlocation)
                .background(R.color.purple_200)
                .backgroundDark(R.color.purple_500)
                .scrollable(false)
                .build()
        )
        addSlide(
            SimpleSlide.Builder()
                .title("Save in your POD!")
                .description("Share locations with your friends so they can see cool places")
                .image(R.drawable.createlocation)
                .background(R.color.purple_200)
                .backgroundDark(R.color.purple_500)
                .scrollable(false)
                .build()
        )
    }
}