package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.google.android.material.textfield.TextInputEditText

class AddContactFragment : Fragment() {

    private val viewModel: ContactsViewModel by activityViewModels {
        ContactsViewModelFactory((activity as MainActivity).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_contact, container, false)

        val etName = view.findViewById<TextInputEditText>(R.id.etName)
        val etPhone = view.findViewById<TextInputEditText>(R.id.etPhone)
        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmail)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        btnSave.setOnClickListener {
            val name = etName.text?.toString()?.trim() ?: ""
            val phone = etPhone.text?.toString()?.trim() ?: ""
            val email = etEmail.text?.toString()?.trim() ?: ""

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(context, "Name and Phone are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.addContact(name, phone, email)
        }

        viewModel.operationStatus.observe(viewLifecycleOwner) { status ->
            if (status?.first == "Add Contact") {
                if (status.second) {
                    Toast.makeText(context, "Contact Added Successfully", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(context, "Failed to Add Contact", Toast.LENGTH_SHORT).show()
                }
                viewModel.clearStatus()
            }
        }

        return view
    }
}
