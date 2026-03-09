package com.example.architecture2

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.architecture2.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), ContactAdapter.ContactClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHelper: DBHelper
    private lateinit var adapter: ContactAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        dbHelper = DBHelper(this)

        adapter = ContactAdapter(emptyList(), this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, AddContactActivity::class.java)
            startActivity(intent)
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                loadContacts(s?.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_import_vcard -> {
                openVCardPicker()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        loadContacts()
    }

    private fun loadContacts(query: String? = null) {
        val contacts = dbHelper.getContacts(query)
        adapter.updateData(contacts)
    }

    private fun openVCardPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/x-vcard"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/x-vcard", "text/vcard", "application/vcard"))
        }
        startActivityForResult(intent, REQUEST_IMPORT_VCARD)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMPORT_VCARD && resultCode == Activity.RESULT_OK) {
            val uri: Uri? = data?.data
            if (uri != null) {
                importFromVCard(uri)
            }
        }
    }

    private fun importFromVCard(uri: Uri) {
        val contacts = VCardUtils.parseVCard(this, uri)
        if (contacts.isEmpty()) {
            Toast.makeText(this, R.string.import_error, Toast.LENGTH_SHORT).show()
            return
        }

        var importedCount = 0
        contacts.forEach { contact ->
            val id = dbHelper.insertContact(contact)
            if (id != -1L) {
                importedCount++
            }
        }

        if (importedCount > 0) {
            Toast.makeText(this, getString(R.string.import_success, importedCount), Toast.LENGTH_SHORT).show()
            loadContacts(binding.etSearch.text?.toString())
        } else {
            Toast.makeText(this, R.string.import_error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCall(contact: ContactModel) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${contact.phone}")
        }
        startActivity(intent)
    }

    override fun onEdit(contact: ContactModel) {
        val intent = Intent(this, AddContactActivity::class.java).apply {
            putExtra(AddContactActivity.EXTRA_CONTACT_ID, contact.id)
        }
        startActivity(intent)
    }

    override fun onDelete(contact: ContactModel) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_contact))
            .setMessage(getString(R.string.delete_confirmation, contact.fullName))
            .setPositiveButton(android.R.string.ok) { _: DialogInterface, _: Int ->
                dbHelper.deleteContact(contact.id)
                loadContacts(binding.etSearch.text?.toString())
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    companion object {
        private const val REQUEST_IMPORT_VCARD = 1001
    }
}