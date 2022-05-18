package fi.tuni.tamk.weatherapp

import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.TextView


class ForecastActivity : AppCompatActivity() {

    lateinit var city: String

    lateinit var testView: TextView

    lateinit var location: Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forecast)
        testView = findViewById(R.id.testView)
        val intent = intent
        if(intent.extras?.get("city") != null) {
            city = intent.extras?.get("city") as String
            testView.text = city
        } else {
            location = intent.extras?.get("coords") as Location
            testView.text = location.toString()
        }
    }

    /**
     * Called when the activity has detected the user's press of the back
     * key. The [OnBackPressedDispatcher][.getOnBackPressedDispatcher] will be given a
     * chance to handle the back button before the default behavior of
     * [android.app.Activity.onBackPressed] is invoked.
     *
     * @see .getOnBackPressedDispatcher
     */
    override fun onBackPressed() {
        super.onBackPressed()
    }
}