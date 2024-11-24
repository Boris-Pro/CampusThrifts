package com.example.campusthrifts

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity

class CheckoutActivity : AppCompatActivity() {

    private lateinit var totalPriceTextView: TextView
    private lateinit var proceedButton: Button
    private lateinit var backButton: Button
    private lateinit var paymentMethodsGroup: RadioGroup
    private lateinit var radioCash: RadioButton
    private lateinit var radioCard: RadioButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)


        totalPriceTextView = findViewById(R.id.total_price_text)
        proceedButton = findViewById(R.id.btn_proceed)
        backButton = findViewById(R.id.btn_back)
        paymentMethodsGroup = findViewById(R.id.payment_methods_group)
        radioCash = findViewById(R.id.radio_cash)
        radioCard = findViewById(R.id.radio_card)


        val totalPrice = intent.getDoubleExtra("TOTAL_PRICE", 0.0)
        totalPriceTextView.text = "Total: $$totalPrice"


        proceedButton.setOnClickListener {
            val selectedPaymentMethod = when (paymentMethodsGroup.checkedRadioButtonId) {
                R.id.radio_cash -> "Cash"
                R.id.radio_card -> "Card"
                else -> "None"
            }

            Toast.makeText(this, "Proceeding with $selectedPaymentMethod payment", Toast.LENGTH_SHORT).show()


            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            finish()
        }

        backButton.setOnClickListener {

            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}