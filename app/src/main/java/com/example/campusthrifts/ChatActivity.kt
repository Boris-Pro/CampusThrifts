// ChatActivity.kt
package com.example.campusthrifts

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class ChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ChatActivity", "onCreate started")
        setContentView(R.layout.activity_chat)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.chat_fragment_container, ChatFragment())
                .commit()
        }
    }
}