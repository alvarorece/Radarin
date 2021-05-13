package es.uniovi.asw.radarinen3b

import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import es.uniovi.asw.radarinen3b.databinding.ActivityMainBinding
import es.uniovi.asw.radarinen3b.dialogs.SavedLocationDialogFragment


class MainActivity : AppCompatActivity(), SavedLocationDialogFragment.SavedLocationDialogListener {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment)
                    .findNavController().navigate(R.id.action_global_settingsFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDialogPositiveClick(dialog: DialogFragment, uri: Uri) {
        val b = CustomTabsIntent.Builder()
        val customTabsIntent = b.build()
        customTabsIntent.launchUrl(this, Uri.parse(uri.toString()))
    }
}