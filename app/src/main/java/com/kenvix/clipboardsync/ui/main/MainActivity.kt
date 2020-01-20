package com.kenvix.clipboardsync.ui.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.kenvix.clipboardsync.R
import com.kenvix.android.exception.EnvironmentNotSatisfiedException
import com.kenvix.clipboardsync.feature.bluetooth.BluetoothUtils
import com.kenvix.android.ui.base.BaseActivity
import com.kenvix.utils.android.annotation.ViewAutoLoad

class MainActivity : BaseActivity() {
    @ViewAutoLoad lateinit var mainFragmentContainer: FrameLayout
    @ViewAutoLoad lateinit var mainToolbar: Toolbar
    @ViewAutoLoad lateinit var mainNavView: NavigationView
    @ViewAutoLoad lateinit var mainDrawerLayout: DrawerLayout

    private lateinit var settingFragment: SettingFragment

    override fun onInitialize(savedInstanceState: Bundle?) {
        //Action bar init
        setSupportActionBar(mainToolbar)
        val toggle = ActionBarDrawerToggle(
            this, mainDrawerLayout, mainToolbar, R.string.action_open, R.string.action_close
        )
        mainNavView.setNavigationItemSelectedListener(::onNavigationItemSelected)

        //Toggle init
        mainDrawerLayout.addDrawerListener(toggle)
        toggle.syncState()


        try {
            settingFragment = SettingFragment(this)

            BluetoothUtils.tryEnableBluetoothDevice()
            setForegroundFragment(baseContainer, settingFragment)
        } catch (e: EnvironmentNotSatisfiedException) {
            ui.showAlertDialog(getString(R.string.no_bluetooth_device)) {
                finish()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)

        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    private fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        super.onOptionsItemSelected(item)

        try {
            when (id) {
                R.id.nav_server_list -> {
                    //setForegroundFragment(R.id.main_fragment_container, serversFragment)
                    title = getString(R.string.title_main)
                }

                R.id.nav_quick_commands -> {
                    //setForegroundFragment(R.id.main_fragment_container, quickCommandsFragment)
                    title = getString(R.string.title_quick_command)
                }

                R.id.nav_settings -> {
                    setForegroundFragment(baseContainer, settingFragment)
                    title = getString(R.string.action_settings)
                }
            }
        } catch (ex: Exception) {
            ui.showExceptionSnackbarPrompt(ex)
        }

        mainDrawerLayout.closeDrawers()
        return true
    }

    override fun getBaseLayout(): Int = R.layout.activity_main
    override fun getBaseContainer(): Int = R.id.main_fragment_container
}
