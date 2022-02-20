package com.martinlacorrona.ryve.mobile.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import com.martinlacorrona.ryve.mobile.R
import com.martinlacorrona.ryve.mobile.app.Properties
import com.martinlacorrona.ryve.mobile.rest.util.Resource
import com.martinlacorrona.ryve.mobile.viewmodel.MainViewModel
import com.martinlacorrona.ryve.mobile.viewmodel.PreferencesViewModel
import com.martinlacorrona.ryve.mobile.viewmodel.PrincipalViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class PrincipalActivity : BaseActivity() {

    private val vm: PrincipalViewModel by viewModels()

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var navigationView: NavigationView
    private lateinit var bottomNavigationView: BottomNavigationView
    lateinit var drawerLayout: DrawerLayout

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_principal)

        //Nav view bottom
        drawerLayout = findViewById(R.id.drawer_layout)
        bottomNavigationView = findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment)
        navigationView = findViewById(R.id.navigation_view)
        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.navigation_home,
            R.id.navigation_favourites,
            R.id.navigation_travel,
            R.id.navigation_alert,
            R.id.navigation_map))
        bottomNavigationView.setupWithNavController(navController)

        //Toolbar (top)
        setSupportActionBar(findViewById(R.id.toolbar))
        setTitle(R.string.menu_home)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)

        //Left options
        initLeftOptions()

        initObservers()
    }

    private fun initObservers() {
        vm.unregistertoken.observe(this) {
            when (it.status) {
                Resource.Status.LOADING -> {
                    //ALERTA DE CERRANDO SESION
                    setLoading(getString(R.string.logingout))
                }
                Resource.Status.SUCCESS -> {
                    firebaseAnalytics.logEvent("logout", Bundle())
                    closeLoading()
                    vm.deleteSharedPreferences()
                    loadWelcomeActivity()
                }
                Resource.Status.ERROR -> {
                    closeLoading()
                    //ALERTA DE ERROR INICIANDO SESION CONEXION
                    setAlertRetry(
                        AlertRetry(
                            getString(R.string.logout),
                            getString(R.string.error_logout)
                        )
                    )
                }
            }
        }
        vm.unviewedNotificationList.observe(this) {
            if (it.size > 0) {
                bottomNavigationView.getOrCreateBadge(R.id.navigation_alert).apply {
                    isVisible = true
                }
            } else {
                bottomNavigationView.removeBadge(R.id.navigation_alert)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            drawerLayout.openDrawer(Gravity.LEFT)
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    fun initLeftOptions() {
        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_left_edit_account -> {
                    loadEditAccountActivity()
                    true
                }
                R.id.navigation_left_preferences -> {
                    loadPreferencesActivity()
                    true
                }
                R.id.navigation_left_logout -> {
                    vm.unregisterToken()
                    true
                }

                else -> {
                    true
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun loadWelcomeActivity() {
        Intent(this, WelcomeActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(this)
        }
    }

    private fun loadPreferencesActivity() {
        Intent(this, PreferencesActivity::class.java).apply {
            putExtra(Properties.MODE_PREFERENCES_ACTIVITY, PreferencesViewModel.PreferencesMode.UPDATE)
            startActivity(this)
        }
    }

    private fun loadEditAccountActivity() {
        Intent(this, AccountActivity::class.java).apply {
            startActivity(this)
        }
    }
}