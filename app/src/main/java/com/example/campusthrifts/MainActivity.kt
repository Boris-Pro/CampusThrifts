package com.example.campusthrifts

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
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
import com.example.campusthrifts.fragments.AddItemFragment
import com.example.campusthrifts.fragments.FavoriteFragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import android.widget.ImageView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var databaseReference: DatabaseReference

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            makeText(this, "Notification Permission Granted", LENGTH_SHORT).show()
            Toast.makeText(this, "Notification Permission Granted", Toast.LENGTH_SHORT).show()
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
        Log.d("MainActivity", "onCreate called")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar
        setSupportActionBar(binding.toolbar)
        Log.d("MainActivity", "Toolbar set")

        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.open_nav,
            R.string.close_nav
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        Log.d("MainActivity", "Drawer toggle synced")

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
        Log.d("MainActivity", "Navigation item selected listener set")

        // Load Default Fragment on Launch (e.g., HomeFragment)
        if (savedInstanceState == null) {
            Log.d("MainActivity", "Loading HomeFragment as default")
            navigateToFragment(HomeFragment(), "Home")
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

        // Bottom Navigation View handling
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.background = null
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val selectedFragment: Fragment? = when (item.itemId) {
                R.id.bottom_home -> {
                    Log.d("MainActivity", "Home button clicked")
                    HomeFragment()
                }
                R.id.bottom_add_item -> {
                    Log.d("MainActivity", "Add Item button clicked")
                    AddItemFragment()
                }
                R.id.bottom_favorite_item -> {
                    Log.d("MainActivity", "Favorites button clicked")
                    FavoriteFragment()
                }
                R.id.bottom_chat -> {
                    Log.d("MainActivity", "Chat button clicked")
                    startActivity(Intent(this, ChatActivity::class.java))
                    openFragment(UserListFragment())
                    return@setOnItemSelectedListener true
                }
                else -> null
            }
            if (selectedFragment != null) {
                val title = when (item.itemId) {
                    R.id.bottom_home -> "Home"
                    R.id.bottom_add_item -> "Add Item"
                    R.id.bottom_favorite_item -> "Favorites"
                    else -> "CampusThrifts"
                }
                navigateToFragment(selectedFragment, title)
            }
            true
        }

        // Request notification permission for Android API >= 33
        askNotificationPermission()
        retrieveFCMToken()

        // Load profile image for toolbar profile icon
        loadUserProfileImage()
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
    private fun navigateToFragment(fragment: Fragment, title: String) {
        Log.d("MainActivity", "Navigating to Fragment: $title")
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
        updateToolbarTitle(title)
    }

    private fun updateToolbarTitle(title: String) {
        Log.d("MainActivity", "Updating toolbar title to: $title")
        supportActionBar?.title = title
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.black))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)

        // Load user profile image into profile menu item
        val profileMenuItem = menu?.findItem(R.id.action_profile)
        val profileImageView = profileMenuItem?.actionView as? ImageView

        if (profileImageView != null) {
            // Load the profile image from Firebase
            Log.d("MainActivity", "Loading user profile image for toolbar")
            loadUserProfileImage(profileImageView)
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
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_cart -> {
                Log.d("MainActivity", "Cart button clicked")
                navigateToFragment(CartFragment(), "Cart")
                true
            }
            R.id.action_profile -> {
                Log.d("MainActivity", "Profile button clicked")
                navigateToFragment(ProfileFragment(), "Profile")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("MainActivity", "Requesting notification permission")
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                Log.d("MainActivity", "Notification permission already granted")
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
            Log.d("MainActivity", "FCM Token: $token")
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_logout -> {
                Log.d("MainActivity", "Logout button clicked")
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                return true
            }
            val msg = getString(R.string.msg_token_fmt, token)
            Log.d("MainActivity", msg)
            sendRegistrationTokenToServer(token)
        }
        binding.drawerLayout.closeDrawer(binding.navigationDrawer)
        return true
    }

    private fun sendRegistrationTokenToServer(token: String?) {
        // TODO: Implement the logic to send the token to your app server.
        Log.d("MainActivity", "sendRegistrationTokenToServer($token)")
    private fun loadUserProfileImage(profileImageView: ImageView? = null) {
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.uid)

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profilePictureUrl = snapshot.child("profilePictureUrl").getValue(String::class.java)
                if (profilePictureUrl.isNullOrEmpty()) {
                    Log.d("MainActivity", "No profile picture found, using default image")
                    profileImageView?.setImageResource(R.drawable.ic_person)
                } else {
                    Log.d("MainActivity", "Profile picture found, loading into ImageView")
                    profileImageView?.let {
                        Picasso.get().load(profilePictureUrl).into(it)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("MainActivity", "Failed to load profile picture", error.toException())
            }
        })
    }

    // ======= Added Code End =======

}
