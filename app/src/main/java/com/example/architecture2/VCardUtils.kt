package com.example.architecture2

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader

object VCardUtils {

    fun createVCardFile(context: Context, contact: ContactModel): Uri? {
        val vcard = buildString {
            appendLine("BEGIN:VCARD")
            appendLine("VERSION:3.0")
            appendLine("N:${contact.lastName};${contact.firstName};;;")
            appendLine("FN:${contact.fullName}")
            if (contact.phone.isNotBlank()) {
                appendLine("TEL;TYPE=CELL:${contact.phone}")
            }
            if (contact.email.isNotBlank()) {
                appendLine("EMAIL;TYPE=INTERNET:${contact.email}")
            }
            if (contact.company.isNotBlank()) {
                appendLine("ORG:${contact.company}")
            }
            if (contact.jobTitle.isNotBlank()) {
                appendLine("TITLE:${contact.jobTitle}")
            }
            if (contact.address.isNotBlank()) {
                appendLine("ADR;TYPE=HOME:;;${contact.address};;;;")
            }
            if (contact.website.isNotBlank()) {
                appendLine("URL:${contact.website}")
            }
            if (contact.notes.isNotBlank()) {
                appendLine("NOTE:${contact.notes}")
            }
            appendLine("END:VCARD")
        }

        val dir = context.getExternalFilesDir(null) ?: context.filesDir
        val file = File(dir, "contact_${contact.id}.vcf")

        return try {
            FileOutputStream(file).use { fos ->
                fos.write(vcard.toByteArray(Charsets.UTF_8))
            }
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun parseVCard(context: Context, uri: Uri): List<ContactModel> {
        val contacts = mutableListOf<ContactModel>()

        val inputStream = try {
            context.contentResolver.openInputStream(uri)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        if (inputStream == null) {
            return contacts
        }

        BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
            var line: String?

            var firstName = ""
            var lastName = ""
            var phone = ""
            var email = ""
            var company = ""
            var jobTitle = ""
            var address = ""
            var website = ""
            var notes = ""

            fun addContactIfValid() {
                if (firstName.isBlank() && lastName.isBlank() && phone.isBlank()) {
                    return
                }
                contacts.add(
                    ContactModel(
                        id = 0L,
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
                )
            }

            while (true) {
                line = reader.readLine() ?: break
                val trimmed = line?.trim() ?: continue

                if (trimmed.equals("BEGIN:VCARD", ignoreCase = true)) {
                    firstName = ""
                    lastName = ""
                    phone = ""
                    email = ""
                    company = ""
                    jobTitle = ""
                    address = ""
                    website = ""
                    notes = ""
                    continue
                }

                if (trimmed.equals("END:VCARD", ignoreCase = true)) {
                    addContactIfValid()
                    continue
                }

                val separatorIndex = trimmed.indexOf(':')
                if (separatorIndex == -1) continue

                val keyPart = trimmed.substring(0, separatorIndex)
                val valuePart = trimmed.substring(separatorIndex + 1)

                when {
                    keyPart.startsWith("N", ignoreCase = true) -> {
                        val parts = valuePart.split(';')
                        lastName = parts.getOrNull(0) ?: ""
                        firstName = parts.getOrNull(1) ?: ""
                    }
                    keyPart.startsWith("FN", ignoreCase = true) -> {
                        if (firstName.isBlank() && lastName.isBlank()) {
                            val parts = valuePart.split(' ')
                            firstName = parts.firstOrNull() ?: ""
                            lastName = if (parts.size > 1) {
                                parts.subList(1, parts.size).joinToString(" ")
                            } else {
                                ""
                            }
                        }
                    }
                    keyPart.startsWith("TEL", ignoreCase = true) -> {
                        if (phone.isBlank()) {
                            phone = valuePart
                        }
                    }
                    keyPart.startsWith("EMAIL", ignoreCase = true) -> {
                        if (email.isBlank()) {
                            email = valuePart
                        }
                    }
                    keyPart.startsWith("ORG", ignoreCase = true) -> {
                        if (company.isBlank()) {
                            company = valuePart
                        }
                    }
                    keyPart.startsWith("TITLE", ignoreCase = true) -> {
                        if (jobTitle.isBlank()) {
                            jobTitle = valuePart
                        }
                    }
                    keyPart.startsWith("ADR", ignoreCase = true) -> {
                        if (address.isBlank()) {
                            val parts = valuePart.split(';')
                            address = parts.getOrNull(2) ?: valuePart
                        }
                    }
                    keyPart.startsWith("URL", ignoreCase = true) -> {
                        if (website.isBlank()) {
                            website = valuePart
                        }
                    }
                    keyPart.startsWith("NOTE", ignoreCase = true) -> {
                        if (notes.isBlank()) {
                            notes = valuePart
                        }
                    }
                }
            }
        }

        return contacts
    }
}

