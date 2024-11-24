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

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productName: TextView = view.findViewById(R.id.item_name)
        val productPrice: TextView = view.findViewById(R.id.item_price)
        val quantity: EditText = view.findViewById(R.id.item_quantity)
        val totalPrice: TextView = view.findViewById(R.id.item_total_price)
        val btnIncrease: Button = view.findViewById(R.id.btn_increase)
        val btnDecrease: Button = view.findViewById(R.id.btn_decrease)
        val btnRemove: Button = view.findViewById(R.id.btn_remove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cart_item, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = cartItems[position]

        holder.productName.text = cartItem.name
        holder.productPrice.text = "$${cartItem.price}"
        holder.quantity.setText(cartItem.quantity.toString())
        holder.totalPrice.text = "$${cartItem.getTotalPrice()}"

        // Increase Quantity
        holder.btnIncrease.setOnClickListener {
            cartItem.quantity++
            holder.quantity.setText(cartItem.quantity.toString())
            holder.totalPrice.text = "$${cartItem.getTotalPrice()}"
            onQuantityChanged(cartItem)
        }

        // Decrease Quantity
        holder.btnDecrease.setOnClickListener {
            if (cartItem.quantity > 1) {
                cartItem.quantity--
                holder.quantity.setText(cartItem.quantity.toString())
                holder.totalPrice.text = "$${cartItem.getTotalPrice()}"
                onQuantityChanged(cartItem)
            }
        }

        // Remove Item
        holder.btnRemove.setOnClickListener {
            cartItems.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, cartItems.size)
            onItemRemoved(cartItem)
        }
    }

    override fun getItemCount(): Int = cartItems.size
}