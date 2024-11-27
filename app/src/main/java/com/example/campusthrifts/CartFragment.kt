package com.example.campusthrifts

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.database.*

class CartFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private lateinit var checkoutButton: Button
    private lateinit var database: DatabaseReference
    private val cartItems = mutableListOf<CartItem>()
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = arguments?.getString("USER_ID")
        if (userId != null) {
            database = FirebaseDatabase.getInstance().reference
                .child("cart").child(userId!!)
        } else {
            Toast.makeText(requireContext(), "User ID not provided", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_cart, container, false)

        recyclerView = rootView.findViewById(R.id.recycler_view)
        checkoutButton = rootView.findViewById(R.id.checkout_button)

        return rootView
    }
    private fun updateCartTotal() {
        val totalPrice = cartItems.sumOf { it.getTotalPrice() }
        val totalTextView: TextView = view?.findViewById(R.id.heading_total_price) ?: return
        totalTextView.text = "Total: $$totalPrice"
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        cartAdapter = CartAdapter(
            cartItems,
            onQuantityChanged = { updatedCartItem ->
                updateCartInFirebase(updatedCartItem)
                updateCartTotal()
            },
            onItemRemoved = { removedCartItem ->
                removeCartItemFromFirebase(removedCartItem)
                updateCartTotal()
            }
        )
        recyclerView.adapter = cartAdapter

        fetchCartItems()
        updateCartTotal()

        checkoutButton.setOnClickListener {
            val totalPrice = cartItems.sumOf { it.getTotalPrice() }
            val intent = Intent(requireContext(), CheckoutActivity::class.java)
            intent.putExtra("TOTAL_PRICE", totalPrice)
            startActivity(intent)
        }
    }

    private fun fetchCartItems() {
        if (userId == null) {
            Toast.makeText(requireContext(), "User Id not available", Toast.LENGTH_SHORT).show()
            return
        }

        // Fetch cart items from Firebase for the current user
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cartItems.clear()  // Clear the list before adding new data

                for (cartItemSnapshot in snapshot.children) {
                    val cartItem = cartItemSnapshot.getValue(CartItem::class.java)
                    if (cartItem != null) {
                        cartItems.add(cartItem)
                    }
                }

                cartAdapter.notifyDataSetChanged()
                updateCartTotal()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load cart items: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateCartInFirebase(updatedCartItem: CartItem) {
        val cartItemId = updatedCartItem.id

        database.child(cartItemId).setValue(updatedCartItem)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Cart updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to update cart", Toast.LENGTH_SHORT).show()
                }
            }

    }

    private fun removeCartItemFromFirebase(cartItem: CartItem) {

        val cartItemId = cartItem.id
        //Toast.makeText(requireContext(), "JZG Proceed to $cartItemId  payment", Toast.LENGTH_SHORT).show()
        database.child(cartItemId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Item removed from cart", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to remove item", Toast.LENGTH_SHORT).show()
                }
            }

    }
}