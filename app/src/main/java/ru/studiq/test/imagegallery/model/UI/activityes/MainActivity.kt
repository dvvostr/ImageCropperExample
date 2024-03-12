package ru.studiq.test.imagegallery.model.UI.activityes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.studiq.test.imagegallery.R
import ru.studiq.test.imagegallery.model.UI.fragments.MainFragment

class MainActivity : AppCompatActivity() {
    private var fragment: MainFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setHomeButtonEnabled(false)

        supportActionBar?.title = "Crop image"
        supportActionBar?.subtitle = "Crop image description"

        fragment = MainFragment()
        supportFragmentManager.beginTransaction().replace(R.id.container_body, fragment!!).commit()
    }
}