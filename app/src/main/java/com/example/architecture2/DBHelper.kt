package com.example.architecture2

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_CONTACTS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_FIRST_NAME TEXT NOT NULL,
                $COL_LAST_NAME TEXT,
                $COL_PHONE TEXT NOT NULL,
                $COL_EMAIL TEXT,
                $COL_COMPANY TEXT,
                $COL_JOB_TITLE TEXT,
                $COL_ADDRESS TEXT,
                $COL_WEBSITE TEXT,
                $COL_NOTES TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CONTACTS")
        onCreate(db)
    }

    fun insertContact(contact: ContactModel): Long {
        val db = writableDatabase
        val values = toContentValues(contact)
        return db.insert(TABLE_CONTACTS, null, values)
    }

    fun updateContact(contact: ContactModel): Int {
        val db = writableDatabase
        val values = toContentValues(contact)
        return db.update(
            TABLE_CONTACTS,
            values,
            "$COL_ID = ?",
            arrayOf(contact.id.toString())
        )
    }

    fun deleteContact(id: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_CONTACTS, "$COL_ID = ?", arrayOf(id.toString()))
    }

    fun getContactById(id: Long): ContactModel? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_CONTACTS,
            null,
            "$COL_ID = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        cursor.use {
            if (it.moveToFirst()) {
                return fromCursor(it)
            }
        }
        return null
    }

    fun getContacts(query: String? = null): List<ContactModel> {
        val db = readableDatabase
        val contacts = mutableListOf<ContactModel>()

        val selection: String?
        val selectionArgs: Array<String>?

        if (query.isNullOrBlank()) {
            selection = null
            selectionArgs = null
        } else {
            selection = "($COL_FIRST_NAME LIKE ? OR $COL_LAST_NAME LIKE ?)"
            val pattern = "%${query.trim()}%"
            selectionArgs = arrayOf(pattern, pattern)
        }

        val cursor = db.query(
            TABLE_CONTACTS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            "$COL_FIRST_NAME COLLATE NOCASE ASC, $COL_LAST_NAME COLLATE NOCASE ASC"
        )

        cursor.use {
            if (it.moveToFirst()) {
                do {
                    contacts.add(fromCursor(it))
                } while (it.moveToNext())
            }
        }
        return contacts
    }

    private fun toContentValues(contact: ContactModel): ContentValues {
        return ContentValues().apply {
            put(COL_FIRST_NAME, contact.firstName)
            put(COL_LAST_NAME, contact.lastName)
            put(COL_PHONE, contact.phone)
            put(COL_EMAIL, contact.email)
            put(COL_COMPANY, contact.company)
            put(COL_JOB_TITLE, contact.jobTitle)
            put(COL_ADDRESS, contact.address)
            put(COL_WEBSITE, contact.website)
            put(COL_NOTES, contact.notes)
        }
    }

    private fun fromCursor(cursor: Cursor): ContactModel {
        return ContactModel(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
            firstName = cursor.getString(cursor.getColumnIndexOrThrow(COL_FIRST_NAME)),
            lastName = cursor.getString(cursor.getColumnIndexOrThrow(COL_LAST_NAME)),
            phone = cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE)),
            email = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL)),
            company = cursor.getString(cursor.getColumnIndexOrThrow(COL_COMPANY)),
            jobTitle = cursor.getString(cursor.getColumnIndexOrThrow(COL_JOB_TITLE)),
            address = cursor.getString(cursor.getColumnIndexOrThrow(COL_ADDRESS)),
            website = cursor.getString(cursor.getColumnIndexOrThrow(COL_WEBSITE)),
            notes = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTES))
        )
    }

    companion object {
        private const val DATABASE_NAME = "contacts.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_CONTACTS = "contacts"
        const val COL_ID = "_id"
        const val COL_FIRST_NAME = "first_name"
        const val COL_LAST_NAME = "last_name"
        const val COL_PHONE = "phone"
        const val COL_EMAIL = "email"
        const val COL_COMPANY = "company"
        const val COL_JOB_TITLE = "job_title"
        const val COL_ADDRESS = "address"
        const val COL_WEBSITE = "website"
        const val COL_NOTES = "notes"
    }
}

