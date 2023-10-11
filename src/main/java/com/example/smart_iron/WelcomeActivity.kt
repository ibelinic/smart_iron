package com.example.smart_iron

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import kotlin.system.exitProcess

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val powerButton = findViewById<ImageView>(R.id.powerButton)
        powerButton.setOnClickListener {
            finishAffinity()
        }

        // Inicijalizirajte TextView i postavite klik slušatelja
        val startIroningTextView = findViewById<TextView>(R.id.startIroningTextView)

        // Startiranje pulsirajuće animacije iz XML-a
        startIroningTextView.startAnimation(
            AnimationUtils.loadAnimation(this, R.anim.pulse_animation)
        )

        // Postavljanje klika na TextView
        startIroningTextView.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Metoda koja se poziva pritiskom na TextView
    fun startMainActivity(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}