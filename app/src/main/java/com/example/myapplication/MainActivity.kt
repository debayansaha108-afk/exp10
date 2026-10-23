package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.myapplication.data.ContactsRepository
import com.example.myapplication.ui.HomeFragment

class MainActivity : AppCompatActivity() {

    lateinit var repository: ContactsRepository
        private set

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val readGranted = permissions[Manifest.permission.READ_CONTACTS] ?: false
            val writeGranted = permissions[Manifest.permission.WRITE_CONTACTS] ?: false
            
            if (readGranted && writeGranted) {
                navigateTo(HomeFragment())
            } else {
                Toast.makeText(this, "Permissions required to manage contacts", Toast.LENGTH_LONG).show()
            }
        }

    private val pickContactLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data?.data
            data?.let {
                // Here we get temporary access to the picked contact URI (Android 17+ contact picker behavior when using correct intent)
                Toast.makeText(this, "Contact Picked: $it", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        repository = ContactsRepository(this)

        if (savedInstanceState == null) {
            checkPermissionsAndStart()
        }
    }

    private fun checkPermissionsAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            navigateTo(HomeFragment())
        } else {
            requestPermissionLauncher.launch(
                arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)
            )
        }
    }

    fun navigateTo(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun launchContactPicker() {
        // Launch standard contact picker
        val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        pickContactLauncher.launch(pickContactIntent)
    }
}
