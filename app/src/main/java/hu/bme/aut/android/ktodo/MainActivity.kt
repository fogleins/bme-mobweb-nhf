package hu.bme.aut.android.ktodo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import hu.bme.aut.android.ktodo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}