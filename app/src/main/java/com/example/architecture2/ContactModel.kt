package com.example.architecture2

data class ContactModel(
    val id: Long = 0L,
    var firstName: String,
    var lastName: String,
    var phone: String,
    var email: String,
    var company: String,
    var jobTitle: String,
    var address: String,
    var website: String,
    var notes: String
) {
    val fullName: String
        get() = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")
}

