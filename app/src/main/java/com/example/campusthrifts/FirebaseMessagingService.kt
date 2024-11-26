package com.example.campusthrifts

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val tag = "MyFirebaseMsgService"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(tag, "Refreshed token: $token")

        // Send the token to your app server
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        // Implement this method to send the token to your app server.
        // You can use Retrofit, Volley, or any other networking library.
        Log.d(tag, "Sending token to server: $token")
        // Example: Use Retrofit to send the token
    }

    // Optionally handle incoming messages
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(tag, "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        remoteMessage.data.isNotEmpty().let {
            Log.d(tag, "Message data payload: ${remoteMessage.data}")

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                scheduleJob()
            } else {
                // Handle message within 10 seconds
                handleNow()
            }
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(tag, "Message Notification Body: ${it.body}")
            // Optionally, generate your own notifications here
            // sendNotification(it.body)
        }
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private fun scheduleJob() {
        Log.d(tag, "Scheduling a job.")
        // Implement your long-running job here, for example using WorkManager
    }

    /**
     * Handle a short-lived task within 10 seconds.
     */
    private fun handleNow() {
        Log.d(tag, "Short-lived task is done.")
        // Implement your short task here
    }

    // Optional: Implement your own notification handling
    /*
    private fun sendNotification(messageBody: String?) {
        // Create and show a notification containing the received FCM message.
    }
    */
}
