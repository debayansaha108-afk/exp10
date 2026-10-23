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

class ManageContactFragment : Fragment() {

    private val viewModel: ContactsViewModel by activityViewModels {
        ContactsViewModelFactory((activity as MainActivity).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_manage_contact, container, false)

        val etSearchId = view.findViewById<TextInputEditText>(R.id.etSearchId)
        val etUpdateName = view.findViewById<TextInputEditText>(R.id.etUpdateName)
        val etUpdatePhone = view.findViewById<TextInputEditText>(R.id.etUpdatePhone)
        val btnUpdate = view.findViewById<Button>(R.id.btnUpdate)
        val btnDelete = view.findViewById<Button>(R.id.btnDelete)

        btnUpdate.setOnClickListener {
            val idStr = etSearchId.text?.toString()?.trim() ?: ""
            val name = etUpdateName.text?.toString()?.trim() ?: ""
            val phone = etUpdatePhone.text?.toString()?.trim() ?: ""

            if (idStr.isEmpty() || name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(context, "All fields are required to update", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val contactId = idStr.toLongOrNull()
            if (contactId != null) {
                viewModel.updateContact(contactId, name, phone)
            } else {
                Toast.makeText(context, "Invalid ID format", Toast.LENGTH_SHORT).show()
            }
        }

        btnDelete.setOnClickListener {
            val idStr = etSearchId.text?.toString()?.trim() ?: ""

            if (idStr.isEmpty()) {
                Toast.makeText(context, "ID is required to delete", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val contactId = idStr.toLongOrNull()
            if (contactId != null) {
                viewModel.deleteContact(contactId)
            } else {
                Toast.makeText(context, "Invalid ID format", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.operationStatus.observe(viewLifecycleOwner) { status ->
            if (status != null && (status.first == "Update Contact" || status.first == "Delete Contact")) {
                if (status.second) {
                    Toast.makeText(context, "${status.first} Successful", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(context, "${status.first} Failed (Check ID)", Toast.LENGTH_SHORT).show()
                }
                viewModel.clearStatus()
            }
        }

        return view
    }
}
