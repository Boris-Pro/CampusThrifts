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
import android.util.Log

class HomeFragment : Fragment() {

    private lateinit var database: FirebaseDatabase

    private lateinit var popularItemsAdapter: ItemAdapter
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var itemsAdapter: LandscapeItemAdapter

    private lateinit var buyNowButton: Button
    private lateinit var allButton: Button

    private lateinit var popularItemsArrow: ImageView
    private lateinit var categoriesArrow: ImageView
    private lateinit var homeImage: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("HomeFragment", "onCreateView called")
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize Firebase Realtime Database
        database = FirebaseDatabase.getInstance()

        buyNowButton = view.findViewById(R.id.buy_now_button)
        allButton = view.findViewById(R.id.all_button)
        popularItemsArrow = view.findViewById(R.id.popular_items_arrow)
        categoriesArrow = view.findViewById(R.id.categories_arrow)
        homeImage = view.findViewById(R.id.home_image)

        // Set an image (from drawable resources) for the homeImage ImageView
        homeImage.setImageResource(R.drawable.welcome_image) // Replace with your drawable image

        // Change button colors to black
        buyNowButton.setBackgroundColor(resources.getColor(android.R.color.black, null))
        allButton.setBackgroundColor(resources.getColor(android.R.color.black, null))

        buyNowButton.setOnClickListener {
            Log.d("HomeFragment", "Buy Now button clicked")
            navigateToFragment(SearchFragment())
        }

        allButton.setOnClickListener {
            Log.d("HomeFragment", "All button clicked")
            navigateToFragment(SearchFragment())
        }

        popularItemsArrow.setOnClickListener {
            Log.d("HomeFragment", "Popular items arrow clicked")
            Toast.makeText(context, "Popular items clicked", Toast.LENGTH_SHORT).show()
        }

        categoriesArrow.setOnClickListener {
            Log.d("HomeFragment", "Categories arrow clicked")
            Toast.makeText(context, "Categories clicked", Toast.LENGTH_SHORT).show()
        }

        // Set up RecyclerViews
        val recyclerViewPopularItems = view.findViewById<RecyclerView>(R.id.popular_items_recyclerview)
        popularItemsAdapter = ItemAdapter(emptyList(), this::onFavoriteClick)
        recyclerViewPopularItems.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerViewPopularItems.adapter = popularItemsAdapter

        val recyclerViewCategories = view.findViewById<RecyclerView>(R.id.categories_recyclerview)
        categoryAdapter = CategoryAdapter(emptyList())
        recyclerViewCategories.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerViewCategories.adapter = categoryAdapter

        val recyclerViewItems = view.findViewById<RecyclerView>(R.id.items_recyclerview)
        itemsAdapter = LandscapeItemAdapter(emptyList(), this::onProductClick)  // Adjusted to match expected constructor
        recyclerViewItems.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewItems.adapter = itemsAdapter

        // Fetch data from Firebase
        fetchPopularItems()
        fetchCategories()
        fetchItems()

        return view
    }

    private fun fetchPopularItems() {
        Log.d("HomeFragment", "fetchPopularItems called")
        val popularItemsRef = database.getReference("popular_items")
        popularItemsRef.orderByChild("popularity_score").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("HomeFragment", "Successfully fetched popular items snapshot")
                val items = mutableListOf<Item>()
                snapshot.children.reversed().forEach {
                    val itemId = it.child("id").getValue(String::class.java)
                    if (itemId != null) {
                        val productRef = database.getReference("products").child(itemId)
                        productRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(productSnapshot: DataSnapshot) {
                                val item = productSnapshot.getValue(Item::class.java)
                                if (item != null) {
                                    items.add(item)
                                    Log.d("HomeFragment", "Added item: ${item.name}")
                                    popularItemsAdapter.updateItems(items)
                                } else {
                                    Log.e("HomeFragment", "Item not found for ID: $itemId")
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("FirebaseError", "Failed to fetch product details: ${error.message}")
                            }
                        })
                    } else {
                        Log.e("HomeFragment", "Item ID is null in popular items")
                    }
                }

                if (items.isEmpty()) {
                    Toast.makeText(context, "No popular items available", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Failed to load popular items: ${error.message}")
                Toast.makeText(context, "Failed to load popular items", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchCategories() {
        Log.d("HomeFragment", "fetchCategories called")
        val categoriesRef = database.getReference("categories")
        categoriesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("HomeFragment", "Successfully fetched categories snapshot")
                val categories = mutableListOf<Category>()
                snapshot.children.forEach {
                    val category = it.getValue(Category::class.java)
                    if (category != null) {
                        categories.add(category)
                        Log.d("HomeFragment", "Added category: ${category.name}")
                    } else {
                        Log.e("HomeFragment", "Category is null in snapshot")
                    }
                }

                categoryAdapter.updateItems(categories)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Failed to load categories: ${error.message}")
                Toast.makeText(context, "Failed to load categories", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchItems() {
        Log.d("HomeFragment", "fetchItems called")
        val itemsRef = database.getReference("products")
        itemsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("HomeFragment", "Successfully fetched items snapshot")
                val items = mutableListOf<Item>()
                snapshot.children.forEach {
                    val item = it.getValue(Item::class.java)
                    if (item != null) {
                        items.add(item)
                        Log.d("HomeFragment", "Added item: ${item.name}")
                    } else {
                        Log.e("HomeFragment", "Item is null in snapshot")
                    }
                }
                itemsAdapter.updateItems(items)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Failed to load items: ${error.message}")
                Toast.makeText(context, "Failed to load items", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun onFavoriteClick(item: Item) {
        Log.d("HomeFragment", "onFavoriteClick called for item: ${item.name}")
        val popularItemsRef = database.getReference("popular_items/${item.id}")
        popularItemsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val popularityScore = snapshot.child("popularity_score").getValue(Int::class.java) ?: 0
                popularItemsRef.child("id").setValue(item.id)
                popularItemsRef.child("popularity_score").setValue(popularityScore + 1)
                    .addOnSuccessListener {
                        Log.d("HomeFragment", "Successfully updated favorite for item: ${item.name}")
                    }
                    .addOnFailureListener {
                        Log.e("FirebaseError", "Failed to update favorite: ${it.message}")
                        Toast.makeText(context, "Failed to update favorite", Toast.LENGTH_SHORT).show()
                    }
                Toast.makeText(context, "${item.name} added to favorites", Toast.LENGTH_SHORT).show()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Failed to update favorite: ${error.message}")
                Toast.makeText(context, "Failed to update favorite", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun onProductClick(item: Item) {
        // Navigate to ProductDetailFragment
        val fragment = ProductDetailFragment()
        val bundle = Bundle()
        bundle.putString("productId", item.id)  // Pass the product ID to the detail fragment
        fragment.arguments = bundle

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToFragment(fragment: Fragment) {
        Log.d("HomeFragment", "Navigating to fragment")
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
