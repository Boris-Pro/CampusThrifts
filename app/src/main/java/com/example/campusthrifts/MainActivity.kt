package com.example.campusthrifts

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.campusthrifts.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
// import com.google.firebase.auth
import com.google.firebase.database.FirebaseDatabase
// import com.google.firebase.database
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage

// import com.google.firebase.storage

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var fragmentManager: FragmentManager
    private lateinit var binding: ActivityMainBinding

    // ======= Added Code Start =======

    // Declare the permission launcher at the top of the MainActivity class
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted. FCM can post notifications.
            Toast.makeText(this, "Notification Permission Granted", Toast.LENGTH_SHORT).show()
        } else {
            // Permission denied. Inform the user that notifications won't be shown.
            Toast.makeText(this, "Notification Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // When running in debug mode, connect to the Firebase Emulator Suite.
        // "10.0.2.2" is a special IP address which allows the Android Emulator
        // to connect to "localhost" on the host computer. The port values (9xxx)
        // must match the values defined in the firebase.json file.
        if (BuildConfig.DEBUG) {
            // Use the Firebase Realtime Database emulator
            val database = FirebaseDatabase.getInstance()
            database.useEmulator("10.0.2.2", 9000)

            // Use the Firebase Authentication emulator
            val auth = FirebaseAuth.getInstance()
            auth.useEmulator("10.0.2.2", 9099)

            // Use the Firebase Storage emulator
            val storage = FirebaseStorage.getInstance()
            storage.useEmulator("10.0.2.2", 9199)
        }

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

        // ======= Added Code Start =======

        // Request notification permission
        askNotificationPermission()

        // Retrieve FCM token
        retrieveFCMToken()
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

    // ======= Added Code Start =======

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted. FCM can post notifications.
                    Log.d("MainActivity", "Notification permission already granted.")
                }

                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Show an educational UI explaining why the permission is needed
                    showPermissionRationaleDialog()
                }

                else -> {
                    // Directly ask for the permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Notification Permission Needed")
            .setMessage("CampusThrifts uses notifications to inform you about new messages and updates. Please allow notifications to stay informed.")
            .setPositiveButton("Allow") { dialog, _ ->
                // Request the permission after the user acknowledges the rationale
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                dialog.dismiss()
            }
            .setNegativeButton("Deny") { dialog, _ ->
                // User chose not to grant permission. Handle accordingly.
                Toast.makeText(this, "Notifications will be disabled.", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun retrieveFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("MainActivity", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and send the token to your server
            val msg = getString(R.string.msg_token_fmt, token)
            Log.d("MainActivity", msg)

            // Implement this method to send token to your app server
            sendRegistrationTokenToServer(token)
        }
    }

    private fun sendRegistrationTokenToServer(token: String?) {
        // TODO: Implement the logic to send the token to your app server.
        // This could involve making a network request using Retrofit, Volley, etc.
        // Example using Retrofit (assuming you have an ApiService defined):
        /*
        if (token != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitInstance.api.sendTokenToServer(TokenRequest(token))
                    if (response.isSuccessful) {
                        Log.d("MainActivity", "Token sent to server successfully.")
                    } else {
                        Log.e("MainActivity", "Failed to send token to server.")
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error sending token to server: ${e.message}")
                }
            }
        }
        */
        Log.d("MainActivity", "sendRegistrationTokenToServer($token)")
    }

    // ======= Added Code End =======

}
