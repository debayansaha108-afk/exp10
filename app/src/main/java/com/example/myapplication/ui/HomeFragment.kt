package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.myapplication.MainActivity
import com.example.myapplication.R

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        view.findViewById<Button>(R.id.btnAddContact).setOnClickListener {
            (activity as? MainActivity)?.navigateTo(AddContactFragment())
        }
        
        view.findViewById<Button>(R.id.btnViewContacts).setOnClickListener {
            (activity as? MainActivity)?.navigateTo(ViewContactsFragment())
        }

        view.findViewById<Button>(R.id.btnManageContact).setOnClickListener {
            (activity as? MainActivity)?.navigateTo(ManageContactFragment())
        }

        view.findViewById<Button>(R.id.btnPickContact).setOnClickListener {
            (activity as? MainActivity)?.launchContactPicker()
        }

        return view
    }
}
