package com.example.contactsapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.contactsapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Creates the NavHostFragment dynamically or finds the existing one
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.searchAndResultsFragmentContainer) as? NavHostFragment
            ?: NavHostFragment.create(R.navigation.nav_graph)

        // Uses the extension function to set up the NavHostFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.searchAndResultsFragmentContainer, navHostFragment)
            .setPrimaryNavigationFragment(navHostFragment)
            .commitNow()

        // Initialize the NavController
        navController = navHostFragment.navController
    }
}
