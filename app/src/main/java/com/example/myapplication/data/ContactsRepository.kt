package com.example.myapplication.data

import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactsRepository(private val context: Context) {
    private val contentResolver: ContentResolver = context.contentResolver

    suspend fun getContacts(): List<Contact> = withContext(Dispatchers.IO) {
        val contacts = mutableListOf<Contact>()
        
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        cursor?.use {
            val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val id = if (idIndex >= 0) it.getLong(idIndex) else 0L
                val name = if (nameIndex >= 0) it.getString(nameIndex) else ""
                val number = if (numberIndex >= 0) it.getString(numberIndex) else ""
                contacts.add(Contact(id, name ?: "", number ?: ""))
            }
        }
        
        // Group by ID to avoid duplicates from multiple phone numbers
        contacts.distinctBy { it.id }
    }

    suspend fun addContact(name: String, phone: String, email: String): Boolean = withContext(Dispatchers.IO) {
        val ops = ArrayList<ContentProviderOperation>()

        // 1. Insert RawContact
        val rawContactInsertIndex = ops.size
        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build()
        )

        // 2. Insert Name
        if (name.isNotEmpty()) {
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                    .build()
            )
        }

        // 3. Insert Phone
        if (phone.isNotEmpty()) {
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(CommonDataKinds.Phone.NUMBER, phone)
                    .withValue(CommonDataKinds.Phone.TYPE, CommonDataKinds.Phone.TYPE_MOBILE)
                    .build()
            )
        }

        // 4. Insert Email
        if (email.isNotEmpty()) {
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(CommonDataKinds.Email.DATA, email)
                    .withValue(CommonDataKinds.Email.TYPE, CommonDataKinds.Email.TYPE_WORK)
                    .build()
            )
        }

        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateContact(contactId: Long, newName: String, newPhone: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Get raw contact ID associated with this contact
            val rawContactId = getRawContactId(contactId) ?: return@withContext false

            val ops = ArrayList<ContentProviderOperation>()

            // Update Name
            val nameSelection = "${ContactsContract.Data.RAW_CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?"
            val nameArgs = arrayOf(rawContactId.toString(), CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
            ops.add(
                ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(nameSelection, nameArgs)
                    .withValue(CommonDataKinds.StructuredName.DISPLAY_NAME, newName)
                    .build()
            )

            // Update Phone
            val phoneSelection = "${ContactsContract.Data.RAW_CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?"
            val phoneArgs = arrayOf(rawContactId.toString(), CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
            ops.add(
                ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(phoneSelection, phoneArgs)
                    .withValue(CommonDataKinds.Phone.NUMBER, newPhone)
                    .build()
            )

            contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteContact(contactId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val rawContactId = getRawContactId(contactId) ?: return@withContext false
            val uri = ContactsContract.RawContacts.CONTENT_URI
            val selection = "${ContactsContract.RawContacts._ID} = ?"
            val selectionArgs = arrayOf(rawContactId.toString())
            
            val deletedRows = contentResolver.delete(uri, selection, selectionArgs)
            deletedRows > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun getRawContactId(contactId: Long): Long? {
        val uri = ContactsContract.RawContacts.CONTENT_URI
        val projection = arrayOf(ContactsContract.RawContacts._ID)
        val selection = "${ContactsContract.RawContacts.CONTACT_ID} = ?"
        val selectionArgs = arrayOf(contactId.toString())
        
        contentResolver.query(uri, projection, selection, selectionArgs, null)?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(ContactsContract.RawContacts._ID)
                if(index >= 0) {
                    return it.getLong(index)
                }
            }
        }
        return null
    }
}
