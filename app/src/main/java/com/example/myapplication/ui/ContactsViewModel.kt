package com.example.myapplication.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Contact
import com.example.myapplication.data.ContactsRepository
import kotlinx.coroutines.launch

class ContactsViewModel(private val repository: ContactsRepository) : ViewModel() {

    private val _contacts = MutableLiveData<List<Contact>>()
    val contacts: LiveData<List<Contact>> = _contacts

    private val _operationStatus = MutableLiveData<Pair<String, Boolean>?>()
    val operationStatus: LiveData<Pair<String, Boolean>?> = _operationStatus

    fun loadContacts() {
        viewModelScope.launch {
            val list = repository.getContacts()
            _contacts.postValue(list)
        }
    }

    fun addContact(name: String, phone: String, email: String) {
        viewModelScope.launch {
            val success = repository.addContact(name, phone, email)
            _operationStatus.postValue(Pair("Add Contact", success))
            if (success) loadContacts()
        }
    }

    fun updateContact(contactId: Long, name: String, phone: String) {
        viewModelScope.launch {
            val success = repository.updateContact(contactId, name, phone)
            _operationStatus.postValue(Pair("Update Contact", success))
            if (success) loadContacts()
        }
    }

    fun deleteContact(contactId: Long) {
        viewModelScope.launch {
            val success = repository.deleteContact(contactId)
            _operationStatus.postValue(Pair("Delete Contact", success))
            if (success) loadContacts()
        }
    }

    fun clearStatus() {
        _operationStatus.value = null
    }
}
