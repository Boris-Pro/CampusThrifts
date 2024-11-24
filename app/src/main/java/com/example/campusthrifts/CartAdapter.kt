package com.example.campusthrifts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager


class CartAdapter(
    private val cartItems: MutableList<CartItem>,
    private val onQuantityChanged: (CartItem) -> Unit,
    private val onItemRemoved: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {


    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.item_name)
        val itemPrice: TextView = itemView.findViewById(R.id.item_price)
        val itemQuantity: EditText = itemView.findViewById(R.id.item_quantity)
        val itemTotalPrice: TextView = itemView.findViewById(R.id.item_total_price)
        val btnIncrease: Button = itemView.findViewById(R.id.btn_increase)
        val btnDecrease: Button = itemView.findViewById(R.id.btn_decrease)
        val btnRemove: Button = itemView.findViewById(R.id.btn_remove)

        init {
            // Increase quantity
            btnIncrease.setOnClickListener {
                val cartItem = cartItems[adapterPosition]
                cartItem.quantity++
                itemQuantity.setText(cartItem.quantity.toString())
                onQuantityChanged(cartItem) // Notify the activity to update Firebase
            }

            // Decrease quantity
            btnDecrease.setOnClickListener {
                val cartItem = cartItems[adapterPosition]
                if (cartItem.quantity > 1) {
                    cartItem.quantity--
                    itemQuantity.setText(cartItem.quantity.toString())
                    onQuantityChanged(cartItem) // Notify the activity to update Firebase
                }
            }

            // Remove item from cart
            btnRemove.setOnClickListener {
                val cartItem = cartItems[adapterPosition]
                onItemRemoved(cartItem) // Notify the activity to remove the item from Firebase
            }

            // Update quantity in real-time when manually changed in EditText
            itemQuantity.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val newQuantity = s.toString().toIntOrNull() ?: 1
                    val cartItem = cartItems[adapterPosition]
                    if (cartItem.quantity != newQuantity) {
                        cartItem.quantity = newQuantity
                        onQuantityChanged(cartItem) // Notify the activity to update Firebase
                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.cart_item, parent, false)
        return CartViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = cartItems[position]

        holder.itemName.text = cartItem.name
        holder.itemPrice.text = "$${cartItem.price}"
        holder.itemQuantity.setText(cartItem.quantity.toString())
        holder.itemTotalPrice.text = "$${cartItem.getTotalPrice()}"


        holder.itemQuantity.setSelection(holder.itemQuantity.text.length)
    }

    override fun getItemCount(): Int = cartItems.size
}