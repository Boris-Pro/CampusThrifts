package com.example.campusthrifts

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.campusthrifts.databinding.ActivityMainBinding
import com.example.campusthrifts.fragments.*
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.google.firebase.database.FirebaseDatabase


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var databaseReference: DatabaseReference
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        Toast.makeText(
            this,
            if (isGranted) "Notification Permission Granted" else "Notification Permission Denied",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (BuildConfig.DEBUG) {
            FirebaseDatabase.getInstance().useEmulator("10.0.2.2", 9000)
            FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)
            FirebaseStorage.getInstance().useEmulator("10.0.2.2", 9199)
        }

        // Setup Toolbar and Navigation Drawer
        setSupportActionBar(binding.toolbar)
        val toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.open_nav, R.string.close_nav)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navigationDrawer.setNavigationItemSelectedListener(this)

        setupNavigationHeader()
        setupBottomNavigation()

        // Load the default fragment
        if (savedInstanceState == null) {
            navigateToFragment(HomeFragment(), "Home")
        }

        askNotificationPermission()
        retrieveFCMToken()
    }

    private fun setupNavigationHeader() {
        val headerView = binding.navigationDrawer.getHeaderView(0)
        val userNameTextView = headerView.findViewById<TextView>(R.id.userUserNameTextView)
        val userEmailTextView = headerView.findViewById<TextView>(R.id.userEmailTextView)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseDatabase.getInstance().getReference("users").child(userId)
                .get().addOnSuccessListener { snapshot ->
                    val user = snapshot.getValue(User::class.java)
                    userNameTextView.text = user?.username ?: "Username"
                    userEmailTextView.text = user?.email ?: "user@example.com"
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.background = null
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_home -> {
                    navigateToFragment(HomeFragment(), "Home")
                }
                R.id.bottom_add_item -> {
                    startActivity(Intent(this, AddProductActivity::class.java))  // Updated to navigate to AddProductActivity
                }
                R.id.bottom_favorite_item -> {
                    navigateToFragment(FavoriteFragment(), "Favorites")
                }
                R.id.bottom_chat -> {
                    navigateToFragment(ChatFragment(), "Chat")  // Updated to navigate to ChatFragment
                }
            }
            true
        }
    }

    private fun navigateToFragment(fragment: Fragment, title: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
        updateToolbarTitle(title)
    }

    private fun updateToolbarTitle(title: String) {
        supportActionBar?.title = title
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.black))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        val profileMenuItem = menu?.findItem(R.id.action_profile)
        val profileImageView = profileMenuItem?.actionView as? ImageView
        profileImageView?.let {
            loadUserProfileImage(it)
        }
        return true
    }

    private fun loadUserProfileImage(profileImageView: ImageView) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseDatabase.getInstance().getReference("users").child(userId).child("profileImageUrl")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val imageUrl = snapshot.getValue(String::class.java)
                        if (!imageUrl.isNullOrEmpty()) {
                            Picasso.get().load(imageUrl).into(profileImageView)
                        } else {
                            profileImageView.setImageResource(R.drawable.ic_person) // Default image
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.w("MainActivity", "Failed to read profile image URL.", error.toException())
                    }
                })
        }
    }

    fun toggleFavorite(itemId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val databaseRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("favorites")

        databaseRef.child(itemId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // Remove the item from favorites
                databaseRef.child(itemId).removeValue().addOnSuccessListener {
                    Toast.makeText(this, "Item removed from favorites", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to remove favorite", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Add the item to favorites
                databaseRef.child(itemId).setValue(true).addOnSuccessListener {
                    Toast.makeText(this, "Item added to favorites", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to add favorite", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_cart -> {
                navigateToFragment(CartFragment(), "Cart")
                true
            }
            R.id.action_profile -> {
                navigateToFragment(ProfileFragment(), "Profile")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> navigateToFragment(HomeFragment(), "Home")
            R.id.nav_profile -> navigateToFragment(ProfileFragment(), "Profile")
            R.id.nav_chat -> navigateToFragment(UserListFragment(), "Chat")
            R.id.nav_dashboard -> navigateToFragment(DashboardFragment(), "Dashboard")
            R.id.nav_settings -> navigateToFragment(SettingsFragment(), "Settings")
            R.id.nav_about -> navigateToFragment(AboutFragment(), "About")
            R.id.nav_logout -> {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                Log.d("MainActivity", "Notification permission already granted")
            }
        }
    }

    private fun retrieveFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("MainActivity", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("MainActivity", "FCM Token: $token")
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    // Optional method to send the FCM token to the server
    private fun sendRegistrationTokenToServer(token: String?) {
        // TODO: Implement logic to send token to your server
        Log.d("MainActivity", "Sending registration token to server: $token")
    }
}
