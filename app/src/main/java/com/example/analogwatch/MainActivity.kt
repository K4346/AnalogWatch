package com.example.analogwatch

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.analogwatch.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        initButton()

    }

    private fun initButton() {
        binding.buttonToCustomScreen.setOnClickListener {
            val intent = Intent(this, CustomWatchActivity::class.java)
            startActivity(intent)
        }
    }
}