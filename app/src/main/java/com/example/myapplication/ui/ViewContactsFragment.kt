package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.MainActivity
import com.example.myapplication.R

class ViewContactsFragment : Fragment() {

    private val viewModel: ContactsViewModel by activityViewModels {
        ContactsViewModelFactory((activity as MainActivity).repository)
    }

    private lateinit var adapter: ContactsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_contacts, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = ContactsAdapter(emptyList())
        recyclerView.adapter = adapter

        viewModel.contacts.observe(viewLifecycleOwner) { contacts ->
            adapter.updateContacts(contacts)
        }

        viewModel.loadContacts()

        return view
    }
}
