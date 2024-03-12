package ru.studiq.test.imagegallery.model.UI.activityes

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import ru.studiq.test.imagegallery.model.App
import java.util.Locale

open class CustomActivity: AppCompatActivity() {
    private var intActivity: Activity? = null

    val activity: Activity?
        get() = intActivity

    var action: Long = -1L

    companion object {
        const val PARAM_ACTION = "PARAM_ACTION"
        val TAG = "CustomActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intActivity = this
        App.appContext = this
        action = intent.getLongExtra(PARAM_ACTION, -1L)
        setupActivity()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }
    @TargetApi(Build.VERSION_CODES.N)
    private fun updateResourcesLocale(context: Context, locale: Locale): Context? {
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }

    @Suppress("deprecation")
    private fun updateResourcesLocaleLegacy(context: Context, locale: Locale): Context? {
        val resources = context.resources
        val configuration = resources.configuration
        configuration.locale = locale
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return context
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
            // update overrideConfiguration with your locale
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                applicationContext.createConfigurationContext(overrideConfiguration!!)
            } else {
                val res = baseContext.resources
                val dm = res.displayMetrics
                res.updateConfiguration(overrideConfiguration, dm)
            }
        }
        super.applyOverrideConfiguration(overrideConfiguration)
    }
    open fun setupActivity() {

    }
    open fun onBackButtonClick() {

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackButtonClick()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }
}