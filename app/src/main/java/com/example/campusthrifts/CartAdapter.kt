package com.example.campusthrifts

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.campusthrifts.databinding.FragmentCartItemBinding

class CartAdapter(
    private val cartItems: List<CartItem>,
    private val onDeleteClick: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = FragmentCartItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = cartItems[position]
        holder.bind(cartItem)
    }

    override fun getItemCount(): Int = cartItems.size

    inner class CartViewHolder(private val binding: FragmentCartItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cartItem: CartItem) {
            Glide.with(itemView.context).load(cartItem.imageUrl).into(binding.cartItemImage)
            binding.cartItemName.text = cartItem.name
            Log.d("CartAdapter", "Binding item name: ${binding.cartItemName.text}")
            binding.cartItemPrice.text = "$${cartItem.price}"
            Log.d("CartAdapter", "Binding item price: ${binding.cartItemPrice.text}")
            binding.cartItemQuantity.text = "Quantity: ${cartItem.quantity}"
            Log.d("CartAdapter", "Binding item quantity: ${binding.cartItemQuantity.text}")

            // Set delete button click listener
            binding.cartDeleteButton.setOnClickListener {
                onDeleteClick(cartItem)
            }
        }
    }
}
