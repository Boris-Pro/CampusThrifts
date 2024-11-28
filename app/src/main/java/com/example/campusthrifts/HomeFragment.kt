package com.example.campusthrifts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusthrifts.models.Category
import com.example.campusthrifts.models.Item
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private lateinit var database: FirebaseDatabase

    private lateinit var popularItemsAdapter: ItemAdapter
    private lateinit var categoriesAdapter: CategoryAdapter
    private lateinit var itemsAdapter: ItemAdapter

    private lateinit var buyNowButton: Button
    private lateinit var allButton: Button

    private lateinit var cartIcon: ImageView
    private lateinit var profileIcon: ImageView
    private lateinit var popularItemsArrow: ImageView
    private lateinit var categoriesArrow: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize Firebase Realtime Database
        database = FirebaseDatabase.getInstance()

        // Initialize views
        cartIcon = view.findViewById(R.id.home_cart_icon)
        profileIcon = view.findViewById(R.id.home_profile_icon)
        buyNowButton = view.findViewById(R.id.buy_now_button)
        allButton = view.findViewById(R.id.all_button)
        popularItemsArrow = view.findViewById(R.id.popular_items_arrow)
        categoriesArrow = view.findViewById(R.id.categories_arrow)

        // Set up click listeners
        cartIcon.setOnClickListener {
            navigateToFragment(CartFragment())
        }

        profileIcon.setOnClickListener {
            navigateToFragment(ProfileFragment())
        }

        buyNowButton.setOnClickListener {
            navigateToFragment(SearchFragment())
        }

        allButton.setOnClickListener {
            navigateToFragment(SearchFragment())
        }

        popularItemsArrow.setOnClickListener {
            // Placeholder action
            Toast.makeText(context, "Popular items clicked", Toast.LENGTH_SHORT).show()
        }

        categoriesArrow.setOnClickListener {
            // Placeholder action
            Toast.makeText(context, "Categories clicked", Toast.LENGTH_SHORT).show()
        }

        // Set up RecyclerViews
        val recyclerViewPopularItems = view.findViewById<RecyclerView>(R.id.popular_items_recyclerview)
        popularItemsAdapter = ItemAdapter(emptyList())
        recyclerViewPopularItems.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerViewPopularItems.adapter = popularItemsAdapter

        val recyclerViewCategories = view.findViewById<RecyclerView>(R.id.categories_recyclerview)
        categoriesAdapter = CategoryAdapter(emptyList())
        recyclerViewCategories.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerViewCategories.adapter = categoriesAdapter

        val recyclerViewItems = view.findViewById<RecyclerView>(R.id.items_recyclerview)
        itemsAdapter = ItemAdapter(emptyList())
        recyclerViewItems.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewItems.adapter = itemsAdapter

        // Fetch data from Firebase
        fetchPopularItems()
        fetchCategories()
        fetchItems()

        return view
    }

    private fun fetchPopularItems() {
        val popularItemsRef = database.getReference("popular_items")
        popularItemsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val itemIds = snapshot.children.mapNotNull { it.child("id").getValue(String::class.java) }
                val itemsRef = database.getReference("items")
                itemsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(itemsSnapshot: DataSnapshot) {
                        val items = mutableListOf<Item>()
                        itemIds.forEach { itemId ->
                            val itemSnapshot = itemsSnapshot.child(itemId)
                            val item = itemSnapshot.getValue(Item::class.java)
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

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load popular items", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchCategories() {
        val categoriesRef = database.getReference("categories")
        categoriesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categories = mutableListOf<Category>()
                snapshot.children.forEach {
                    val category = it.getValue(Category::class.java)
                    if (category != null) {
                        categories.add(category)
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load categories", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchItems() {
        val itemsRef = database.getReference("items")
        itemsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<Item>()
                snapshot.children.forEach {
                    val item = it.getValue(Item::class.java)
                    if (item != null) {
                        items.add(item)
                    }
                }
                itemsAdapter.updateItems(items)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load items", Toast.LENGTH_SHORT).show()
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
