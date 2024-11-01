package com.example.campusthrifts

import Product
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView



class SearchFragment : Fragment() {

    private lateinit var listView: ListView
    private lateinit var productAdapter: ProductAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        // Sample data
        val products = listOf(
            Product("Fridge", 10000.00, R.drawable.fridge_image),
            Product("Jacket", 3500.00, R.drawable.jacket_image),
            Product("Book", 2000.00, R.drawable.book_image),
            Product("Pot", 3000.00, R.drawable.pot_image),
            Product("Iphone", 60000.00, R.drawable.iphone_image),
            Product("Blouse", 1000.00, R.drawable.blouse_image),
            Product("Pants", 2500.00, R.drawable.pants_image),

        )

        // Initialize ListView and Adapter
        productAdapter = ProductAdapter( parentFragmentManager,products)


        view.findViewById<ListView>(R.id.ListItems).adapter = productAdapter

        return view
    }


}