package com.example.campusthrifts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusthrifts.databinding.FragmentCartBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CartFragment : Fragment() {

    private lateinit var cartAdapter: CartAdapter
    private lateinit var cartItems: MutableList<CartItem>
    private lateinit var binding: FragmentCartBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCartBinding.inflate(inflater, container, false)

        // Assuming userId is retrieved from FirebaseAuth
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return binding.root

        // Initialize cart items list
        cartItems = mutableListOf()

        // Setup adapter
        cartAdapter = CartAdapter(cartItems) { cartItem ->
            // Handle item removal
            removeProductFromCart(userId, cartItem.productId!!)
            cartItems.remove(cartItem)
            cartAdapter.notifyDataSetChanged()
            updateTotalPrice() // Update total after removing item
        }

        // Setup RecyclerView
        binding.cartRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.cartRecyclerView.adapter = cartAdapter

        // Load cart items
        getCartItems(userId) { items ->
            cartItems.clear()
            cartItems.addAll(items)
            cartAdapter.notifyDataSetChanged()
            updateTotalPrice() // Update total after loading items
        }

        // Clear cart button
        binding.clearCartButton.setOnClickListener {
            clearCart(userId)
            cartItems.clear()
            cartAdapter.notifyDataSetChanged()
            updateTotalPrice() // Update total after clearing cart
        }

        return binding.root
    }

    // Get cart items for the user from Firebase
    fun getCartItems(userId: String, callback: (List<CartItem>) -> Unit) {
        val cartRef = FirebaseDatabase.getInstance().getReference("cartItems").child(userId)

        cartRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cartItems = mutableListOf<CartItem>()
                for (productSnapshot in snapshot.children) {
                    val cartItem = productSnapshot.getValue(CartItem::class.java)
                    cartItem?.let {
                        cartItems.add(it)
                    }
                }
                callback(cartItems)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Cart", "Failed to load cart items", error.toException())
            }
        })
    }

    // Remove a product from the cart
    fun removeProductFromCart(userId: String, productId: String) {
        val cartRef = FirebaseDatabase.getInstance().getReference("cartItems").child(userId).child(productId)

        cartRef.removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d("Cart", "Product removed from cart!")
            } else {
                Log.e("Cart", "Failed to remove product from cart", it.exception)
            }
        }
    }

    // Clear all items from the cart
    fun clearCart(userId: String) {
        val cartRef = FirebaseDatabase.getInstance().getReference("cartItems").child(userId)

        cartRef.removeValue().addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d("Cart", "Cart cleared!")
            } else {
                Log.e("Cart", "Failed to clear cart", it.exception)
            }
        }
    }

    // Update the total price based on the cart items
    private fun updateTotalPrice() {
        // Calculate the total price (price * quantity for each item)
        val totalPrice = cartItems.sumByDouble { (it.price?.times(it.quantity) ?: 0.0) }

        // Update the total label text
        binding.cartTotalLabel.text = "Total: $${"%.2f".format(totalPrice)}"
    }
}
