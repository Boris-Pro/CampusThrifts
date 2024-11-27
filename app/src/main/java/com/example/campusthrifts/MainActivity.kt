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
import android.widget.Toast.*
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var fragmentManager: FragmentManager
    private lateinit var binding: ActivityMainBinding

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            makeText(this, "Notification Permission Granted", LENGTH_SHORT).show()
        } else {
            makeText(this, "Notification Permission Denied", LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (BuildConfig.DEBUG) {
            val database = FirebaseDatabase.getInstance()
            database.useEmulator("10.0.2.2", 9000)

            val auth = FirebaseAuth.getInstance()
            auth.useEmulator("10.0.2.2", 9099)

            val storage = FirebaseStorage.getInstance()
            storage.useEmulator("10.0.2.2", 9199)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.open_nav,
            R.string.close_nav
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navigationDrawer.setNavigationItemSelectedListener(this)

        setupNavigationHeader()
        setupBottomNavigation()

        fragmentManager = supportFragmentManager
        if (savedInstanceState == null) {
            openFragment(HomeFragment())
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
                .get()
                .addOnSuccessListener { snapshot ->
                    val user = snapshot.getValue(User::class.java)
                    userNameTextView.text = user?.username ?: "Username"
                    userEmailTextView.text = user?.email ?: "user@example.com"
                }
                .addOnFailureListener {
                    makeText(this, "Failed to load user data", LENGTH_SHORT).show()
                }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.background = null
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            val selectedFragment = when (item.itemId) {
                R.id.bottom_home -> HomeFragment()
                R.id.bottom_search -> SearchFragment()
                R.id.bottom_cart -> CartFragment()
                R.id.bottom_profile -> ProfileFragment()
                R.id.bottom_chat -> {
                    openFragment(UserListFragment())
                    return@setOnItemSelectedListener true
                }
                else -> null
            }
            if (selectedFragment != null && currentFragment?.javaClass != selectedFragment.javaClass) {
                openFragment(selectedFragment)
            }
            true
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> openFragment(HomeFragment())
            R.id.nav_profile -> openFragment(ProfileFragment())
            R.id.nav_chat -> openFragment(UserListFragment())
            R.id.nav_dashboard -> openFragment(DashboardFragment())
            R.id.nav_settings -> openFragment(SettingsFragment())
            R.id.nav_about -> openFragment(AboutFragment())
            R.id.nav_logout -> {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        when {
            binding.drawerLayout.isDrawerOpen(GravityCompat.START) -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
            supportFragmentManager.backStackEntryCount > 1 -> {
                supportFragmentManager.popBackStack()
                resetToolbarIfNecessary()
            }
            else -> {
                super.onBackPressed()
            }
        }
    }

    private fun resetToolbarIfNecessary() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment !is ChatFragment) {
            (this as? AppCompatActivity)?.supportActionBar?.apply {
                title = "CampusThrifts" // Default title or any other default title you prefer
                setDisplayHomeAsUpEnabled(false)
            }
        }
    }

    fun openFragment(fragment: Fragment) {
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        fragmentTransaction.commit()
    }

    fun openChatWithUser(selectedUser: User) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            val chatRoomId = getChatRoomId(currentUserId, selectedUser.uid)
            val chatFragment = ChatFragment.newInstance(chatRoomId, selectedUser.username)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, chatFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun getChatRoomId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d("MainActivity", "Notification permission already granted.")
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    showPermissionRationaleDialog()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Notification Permission Needed")
            .setMessage("CampusThrifts uses notifications to inform you about new messages and updates.")
            .setPositiveButton("Allow") { dialog, _ ->
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                dialog.dismiss()
            }
            .setNegativeButton("Deny") { dialog, _ ->
                makeText(this, "Notifications will be disabled.", LENGTH_SHORT).show()
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
            val token = task.result
            val msg = getString(R.string.msg_token_fmt, token)
            Log.d("MainActivity", msg)
            sendRegistrationTokenToServer(token)
        }
    }

    private fun sendRegistrationTokenToServer(token: String?) {
        // TODO: Implement the logic to send the token to your app server.
        Log.d("MainActivity", "sendRegistrationTokenToServer($token)")
    }
}