package es.uniovi.asw.radarinen3b

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import es.uniovi.asw.radarinen3b.databinding.ActivityMainBinding
import es.uniovi.asw.radarinen3b.dialogs.SavedLocationDialogFragment


class MainActivity : AppCompatActivity(), SavedLocationDialogFragment.SavedLocationDialogListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toHide01: MenuItem
    private lateinit var toHide02: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbar)
        val pref = getPreferences(Context.MODE_PRIVATE)
        val firstStart = pref.getBoolean(getString(R.string.first_start_preference), true)
        if (firstStart) {
            val intent = Intent(this, MainIntroActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_INTRO)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        toHide01 = menu.findItem(R.id.action_settings)
        toHide02 = menu.findItem(R.id.action_logout)
        if (supportFragmentManager.primaryNavigationFragment != null && (supportFragmentManager.primaryNavigationFragment?.findNavController()?.currentDestination as FragmentNavigator.Destination).className == QrLoginFragment::class.qualifiedName)
            hideSettings(true)
        else {
            hideSettings(false)
        }
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
            R.id.action_logout -> {
                val sharedPreferences =
                    getPreferences(Context.MODE_PRIVATE) ?: return true
                with(sharedPreferences.edit()) {
                    putString(getString(R.string.webId_preference), "")
                    putString(getString(R.string.privateKey_preference), "")
                    commit()
                }
                (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment)
                    .findNavController().navigate(R.id.qrLoginFragment)
                hideSettings(true)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_INTRO) {
            if (resultCode == RESULT_OK) {
                val pref = getPreferences(Context.MODE_PRIVATE)
                with(pref.edit()) {
                    putBoolean(getString(R.string.first_start_preference), false)
                    apply()
                }
            } else {
                val pref = getPreferences(Context.MODE_PRIVATE)
                with(pref.edit()) {
                    putBoolean(getString(R.string.first_start_preference), true)
                    apply()
                }
                finish()
            }
        }
    }

    fun hideSettings(shouldHide: Boolean) {
        if (this::toHide01.isInitialized)
            toHide01.isVisible = !shouldHide
        if (this::toHide02.isInitialized)
            toHide02.isVisible = !shouldHide
    }

    fun showSettings() {
        toHide01.isVisible = true
        toHide02.isVisible = true
    }

    companion object {
        internal val REQUEST_CODE_INTRO = 24;
    }
}