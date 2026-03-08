package com.example.architecture2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class ContactAdapter(
    private var items: List<ContactModel>,
    private val listener: ContactClickListener
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    interface ContactClickListener {
        fun onCall(contact: ContactModel)
        fun onEdit(contact: ContactModel)
        fun onDelete(contact: ContactModel)
        fun onExport(contact: ContactModel)
    }

    fun updateData(newItems: List<ContactModel>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatarText: TextView = itemView.findViewById(R.id.tvAvatar)
        private val nameText: TextView = itemView.findViewById(R.id.tvName)
        private val phoneText: TextView = itemView.findViewById(R.id.tvPhone)
        private val subtitleText: TextView = itemView.findViewById(R.id.tvSubtitle)
        private val btnCall: ImageButton = itemView.findViewById(R.id.btnCall)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        private val btnExportToPhone: MaterialButton = itemView.findViewById(R.id.btnExportToPhone)

        fun bind(contact: ContactModel) {
            nameText.text = contact.fullName.ifBlank { contact.phone }
            phoneText.text = contact.phone

            val initialsSource = when {
                contact.fullName.isNotBlank() -> contact.fullName
                contact.phone.isNotBlank() -> contact.phone
                else -> "#"
            }
            avatarText.text = initialsSource.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "#"

            val subtitle = when {
                contact.email.isNotBlank() -> contact.email
                contact.company.isNotBlank() && contact.jobTitle.isNotBlank() ->
                    "${contact.company} \u00b7 ${contact.jobTitle}"
                contact.company.isNotBlank() -> contact.company
                contact.jobTitle.isNotBlank() -> contact.jobTitle
                else -> ""
            }
            subtitleText.text = subtitle
            subtitleText.visibility = if (subtitle.isNotBlank()) View.VISIBLE else View.GONE

            btnCall.setOnClickListener { listener.onCall(contact) }
            btnEdit.setOnClickListener { listener.onEdit(contact) }
            btnDelete.setOnClickListener { listener.onDelete(contact) }
            btnExportToPhone.setOnClickListener { listener.onExport(contact) }

            itemView.setOnClickListener { listener.onCall(contact) }
        }
    }
}

