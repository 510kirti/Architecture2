package com.example.architecture2

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.architecture2.databinding.ActivityAddContactBinding

class AddContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddContactBinding
    private lateinit var dbHelper: DBHelper

    private var contactId: Long = 0L
    private var existingContact: ContactModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DBHelper(this)

        contactId = intent.getLongExtra(EXTRA_CONTACT_ID, 0L)
        if (contactId != 0L) {
            title = getString(R.string.edit_contact)
            loadContact(contactId)
        } else {
            title = getString(R.string.add_contact)
        }

        binding.btnSave.setOnClickListener {
            saveContact()
        }
    }

    private fun loadContact(id: Long) {
        existingContact = dbHelper.getContactById(id)
        existingContact?.let { contact ->
            binding.etFirstName.setText(contact.firstName)
            binding.etLastName.setText(contact.lastName)
            binding.etPhone.setText(contact.phone)
            binding.etEmail.setText(contact.email)
            binding.etCompany.setText(contact.company)
            binding.etJobTitle.setText(contact.jobTitle)
            binding.etAddress.setText(contact.address)
            binding.etWebsite.setText(contact.website)
            binding.etNotes.setText(contact.notes)
        }
    }

    private fun saveContact() {
        val firstName = binding.etFirstName.text?.toString()?.trim().orEmpty()
        val lastName = binding.etLastName.text?.toString()?.trim().orEmpty()
        val phone = binding.etPhone.text?.toString()?.trim().orEmpty()
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val company = binding.etCompany.text?.toString()?.trim().orEmpty()
        val jobTitle = binding.etJobTitle.text?.toString()?.trim().orEmpty()
        val address = binding.etAddress.text?.toString()?.trim().orEmpty()
        val website = binding.etWebsite.text?.toString()?.trim().orEmpty()
        val notes = binding.etNotes.text?.toString()?.trim().orEmpty()

        if (TextUtils.isEmpty(firstName) && TextUtils.isEmpty(lastName)) {
            binding.tilFirstName.error = getString(R.string.name_required)
            return
        } else {
            binding.tilFirstName.error = null
        }

        if (phone.isBlank()) {
            binding.tilPhone.error = getString(R.string.phone_required)
            return
        } else {
            binding.tilPhone.error = null
        }

        val contact = ContactModel(
            id = existingContact?.id ?: 0L,
            firstName = firstName,
            lastName = lastName,
            phone = phone,
            email = email,
            company = company,
            jobTitle = jobTitle,
            address = address,
            website = website,
            notes = notes
        )

        if (existingContact == null) {
            val newId = dbHelper.insertContact(contact)
            if (newId == -1L) {
                Toast.makeText(this, R.string.export_error, Toast.LENGTH_SHORT).show()
            } else {
                val savedContact = contact.copy(id = newId)
                Toast.makeText(this, R.string.contact_added, Toast.LENGTH_SHORT).show()
                exportToSystemContacts(savedContact)
            }
        } else {
            dbHelper.updateContact(contact)
            Toast.makeText(this, R.string.contact_updated, Toast.LENGTH_SHORT).show()
        }

        finish()
    }

    private fun exportToSystemContacts(contact: ContactModel) {
        val uri = VCardUtils.createVCardFile(this, contact)
        if (uri == null) {
            Toast.makeText(this, R.string.export_error, Toast.LENGTH_SHORT).show()
            return
        }

        val contactsIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "text/x-vcard")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            `package` = "com.android.contacts"
        }

        val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "text/x-vcard")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        try {
            startActivity(contactsIntent)
        } catch (e: Exception) {
            try {
                startActivity(fallbackIntent)
            } catch (_: Exception) {
                Toast.makeText(this, R.string.export_error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val EXTRA_CONTACT_ID = "extra_contact_id"
    }
}

