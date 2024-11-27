package com.example.campusthrifts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusthrifts.models.Category
import com.example.campusthrifts.models.Item
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private lateinit var database: FirebaseDatabase
    private lateinit var newCollectionsAdapter: ItemAdapter
    private lateinit var popularItemsAdapter: ItemAdapter
    private lateinit var categoriesAdapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize Firebase Realtime Database
        database = FirebaseDatabase.getInstance()

        // Set up toolbar icons
        val cartIcon = view.findViewById<ImageView>(R.id.cart_icon)
        val userProfileIcon = view.findViewById<ImageView>(R.id.user_profile_icon)

        cartIcon.setOnClickListener {
            // Navigate to CartFragment
            navigateToFragment(CartFragment())
        }

        userProfileIcon.setOnClickListener {
            // Navigate to User Profile
            navigateToFragment(ProfileFragment())
        }

        // Set up New Collections RecyclerView
        val recyclerViewNewCollections = view.findViewById<RecyclerView>(R.id.recyclerViewNewCollections)
        newCollectionsAdapter = ItemAdapter(emptyList())
        recyclerViewNewCollections.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerViewNewCollections.adapter = newCollectionsAdapter

        // Set up Popular Items RecyclerView
        val recyclerViewPopularItems = view.findViewById<RecyclerView>(R.id.recyclerViewPopularItems)
        popularItemsAdapter = ItemAdapter(emptyList())
        recyclerViewPopularItems.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerViewPopularItems.adapter = popularItemsAdapter

        // Set up Categories RecyclerView
        val recyclerViewCategories = view.findViewById<RecyclerView>(R.id.recyclerViewCategories)
        categoriesAdapter = CategoryAdapter(emptyList())
        recyclerViewCategories.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewCategories.adapter = categoriesAdapter

        // Fetch data from Firebase
        fetchNewCollections()
        fetchPopularItems()
        fetchCategories()

        // Set up "Buy Now" button
        val buyNowButton = view.findViewById<Button>(R.id.buy_now_button)
        buyNowButton.setOnClickListener {
            // Navigate to SearchFragment to display items for sale
            navigateToFragment(SearchFragment())
        }

        return view
    }

    private fun fetchNewCollections() {
        // Fetch new collection items from Firebase and update RecyclerView
        val newCollectionsRef = database.getReference("new_collections")
        newCollectionsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<Item>()
                snapshot.children.forEach {
                    val item = it.getValue(Item::class.java)
                    if (item != null) {
                        items.add(item)
                    }
                }
                newCollectionsAdapter.updateItems(items)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load new collections", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchPopularItems() {
        // Fetch popular items from Firebase and update RecyclerView
        val popularItemsRef = database.getReference("popular_items")
        popularItemsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<Item>()
                snapshot.children.forEach {
                    val item = it.getValue(Item::class.java)
                    if (item != null) {
                        items.add(item)
                    }
                }
                popularItemsAdapter.updateItems(items)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load popular items", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchCategories() {
        // Fetch categories from Firebase and update RecyclerView
        val categoriesRef = database.getReference("categories")
        categoriesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categories = mutableListOf<Category>()
                snapshot.children.forEach {
                    val category = it.getValue(Category::class.java)
                    if (category != null) {
                        categories.add(category)
                    }
                }
                categoriesAdapter.updateCategories(categories)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load categories", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun navigateToFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
