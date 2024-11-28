package com.example.campusthrifts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.SwitchCompat

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Get the Switch view from the layout
        val darkModeSwitch: SwitchCompat = view.findViewById(R.id.darkModeSwitch)

        // Check the current theme and set the Switch state accordingly
        val isDarkMode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        darkModeSwitch.isChecked = isDarkMode

        // Listen for the Switch toggle to enable or disable dark mode
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Enable dark mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                // Disable dark mode (light mode)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        return view
    }
}
