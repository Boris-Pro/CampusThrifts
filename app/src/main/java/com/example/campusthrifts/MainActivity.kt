package com.example.campusthrifts

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.campusthrifts.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var fragmentManager: FragmentManager
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.open_nav, R.string.close_nav)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navigationDrawer.setNavigationItemSelectedListener(this)

        // Fetch user information to display in the navigation header
        val headerView = binding.navigationDrawer.getHeaderView(0)
        val userNameTextView = headerView.findViewById<TextView>(R.id.userUserNameTextView)
        val userEmailTextView = headerView.findViewById<TextView>(R.id.userEmailTextView)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val databaseRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
            databaseRef.get().addOnSuccessListener { snapshot ->
                Log.d("MainActivity", "Snapshot data: ${snapshot.value}")
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    userNameTextView.text = user.username
                    userEmailTextView.text = user.email
                } else {
                    userNameTextView.text = "Username"
                    userEmailTextView.text = "user@example.com"
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
        }


        binding.bottomNavigation.background = null
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            val selectedFragment = when (item.itemId) {
                R.id.bottom_home -> HomeFragment()
                R.id.bottom_search -> SearchFragment()
                R.id.bottom_cart -> CartFragment()
                R.id.bottom_profile -> ProfileFragment()
                R.id.bottom_chat -> ChatFragment()
                else -> null
            }
            if (selectedFragment != null && currentFragment?.javaClass != selectedFragment.javaClass) {
                openFragment(selectedFragment)
            }
            true
        }

        fragmentManager = supportFragmentManager
        openFragment(HomeFragment())
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> openFragment(HomeFragment())
            R.id.nav_profile -> openFragment(ProfileFragment())
            R.id.nav_dashboard -> openFragment(DashboardFragment())
            R.id.nav_settings -> openFragment(SettingsFragment())
            R.id.nav_about -> openFragment(AboutFragment())
            R.id.nav_logout -> {
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun openFragment(fragment: Fragment) {
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.addToBackStack(null) // Add fragment to back stack
        fragmentTransaction.commit()
    }
}
