package com.example.myapplication.data

data class Contact(
    val id: Long,
    val name: String,
    val phone: String,
    val email: String = ""
)
