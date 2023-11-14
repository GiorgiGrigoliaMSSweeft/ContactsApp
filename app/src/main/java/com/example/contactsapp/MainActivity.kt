package com.example.contactsapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.contactsapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val list = mutableListOf(
            Item(R.drawable.baseline_person_24, "Scottie Pippen", "577112233"),
            Item(R.drawable.baseline_person_24, "LeBron James", "599334455"),
            Item(R.drawable.baseline_person_24, "Chris Andersen", "500110011"),
            Item(R.drawable.baseline_person_24, "Shaquille O'Neal", "567891011"),
            Item(R.drawable.baseline_person_24, "Carmelo Anthony", "501101010")
        )

        val adapter = Adapter(list)
        binding.rvItems.adapter = adapter
        binding.rvItems.layoutManager = LinearLayoutManager(this)
    }
}