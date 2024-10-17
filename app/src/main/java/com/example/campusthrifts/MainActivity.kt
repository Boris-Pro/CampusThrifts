package com.example.campusthrifts

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.campusthrifts.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Using ViewBinding to ensure views are bound correctly
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Wait for 2 seconds before transitioning to HomeActivity
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish() // Ensure the splash screen activity does not remain in the back stack
        }, 2000)
    }
}
