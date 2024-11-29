package com.example.campusthrifts.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusthrifts.R
import com.example.campusthrifts.adapters.FavoriteItemsAdapter
import com.example.campusthrifts.models.Item
import com.google.firebase.database.*
import kotlinx.coroutines.launch

class FavoriteFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var favoriteItemsAdapter: FavoriteItemsAdapter
    private val favoriteItemsList = mutableListOf<Item>()
    private lateinit var databaseReference: DatabaseReference
    private var itemsListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.favorite_items_list)
        recyclerView.layoutManager = LinearLayoutManager(context)

        favoriteItemsAdapter = FavoriteItemsAdapter(favoriteItemsList)
        recyclerView.adapter = favoriteItemsAdapter

        loadPopularItems()
    }

    private fun loadPopularItems() {
        // Reference to the "popular_items" node in the database
        databaseReference = FirebaseDatabase.getInstance().getReference("popular_items")

        itemsListener = databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                favoriteItemsList.clear()
                if (snapshot.exists()) {
                    for (itemSnapshot in snapshot.children) {
                        val itemId = itemSnapshot.child("id").value.toString()
                        // Fetch each item detail using the itemId from the "items" node
                        fetchItemDetails(itemId)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (isAdded) {
                    Toast.makeText(context, "Failed to load popular items", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun fetchItemDetails(itemId: String) {
        val itemRef = FirebaseDatabase.getInstance().getReference("items").child(itemId)
        itemRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(itemDetailSnapshot: DataSnapshot) {
                val item = itemDetailSnapshot.getValue(Item::class.java)
                item?.let {
                    favoriteItemsList.add(it)
                    // Notify the adapter that an item was added
                    favoriteItemsAdapter.notifyItemInserted(favoriteItemsList.size - 1)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (isAdded) {
                    Toast.makeText(context, "Failed to load item details", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove Firebase listener to avoid memory leaks and null reference exceptions
        itemsListener?.let {
            databaseReference.removeEventListener(it)
        }
    }
}
