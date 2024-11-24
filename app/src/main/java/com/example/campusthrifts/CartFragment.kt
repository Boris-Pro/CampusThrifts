package com.example.campusthrifts

import android.content.Intent

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class CartFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private lateinit var checkoutButton: Button
    private val cartItems = mutableListOf(
        CartItem("Item 1", 10.0, 1),
        CartItem("Item 2", 15.0, 2),
        CartItem("Item 3", 7.5, 3)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_cart, container, false)

        recyclerView = rootView.findViewById(R.id.recycler_view)
        checkoutButton = rootView.findViewById(R.id.checkout_button)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        cartAdapter = CartAdapter(
            cartItems,
            onQuantityChanged = { updateCartTotal() },
            onItemRemoved = { updateCartTotal() }
        )
        recyclerView.adapter = cartAdapter

        updateCartTotal()


        checkoutButton.setOnClickListener {

            val totalPrice = cartItems.sumOf { it.getTotalPrice() }


            val intent = Intent(requireContext(), CheckoutActivity::class.java)
            intent.putExtra("TOTAL_PRICE", totalPrice)
            startActivity(intent)
        }
    }

    private fun updateCartTotal() {
        val totalPrice = cartItems.sumOf { it.getTotalPrice() }
        val totalTextView: TextView = view?.findViewById(R.id.heading_total_price) ?: return
        totalTextView.text = "Total: $$totalPrice"
    }
}
