package com.arsiwala.shamoil.astrosattendance

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.createDataStore
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var navController : NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var auth: FirebaseAuth
    private lateinit var useremail: String
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var db: FirebaseFirestore
    private val seniors = Firebase.firestore.collection("admin").document("seniors")
    private var dataStore : DataStore<Preferences> = createDataStore(name = "last")
    private val AUTH_CODE = 121
    private val CAMERA_REQUEST_CODE = 101
    var loggedInState : Boolean = false
    val bt = Bluetooth()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        auth = FirebaseAuth.getInstance()
        loggedInState = auth.currentUser != null

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.nav_home, R.id.nav_scanqr, R.id.nav_makeqr, R.id.nav_lights,R.id.nav_logout), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        if (!loggedInState) login()

        navView.setNavigationItemSelectedListener {
            onSelect(it.itemId)
            false
        }
    }

    fun onSelect(id: Int) {
//        drawerLayout.closeDrawer((GravityCompat.START))
//        Log.d("TEST",navController.previousBackStackEntry?.destination?.displayName.toString())
//        val fr = navController.currentBackStackEntry?.destination?.displayName
//        Log.d("TEST", fr.toString())
//        val d =supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.childFragmentManager?.fragments?.size
        when(id) {
            R.id.nav_home -> {
                navController.navigate(R.id.nav_home)
            }
            R.id.nav_scanqr -> {
                val permission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                if(permission != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
                }
                else {
                    navController.navigate(R.id.nav_scanqr)
                }
            }
            R.id.nav_makeqr -> {
                navController.navigate(R.id.nav_makeqr)
            }
            R.id.nav_lights -> {
                if(bluetooth())
                    navController.navigate(R.id.nav_lights)
            }
            R.id.nav_logout -> {
                login()
            }
        }
    }

    suspend fun save(key : String, value : String) {
        val dataStoreKey = preferencesKey<String>(key)
        dataStore.edit {
            it[dataStoreKey] = value
        }
    }

    suspend fun read(key : String): String? {
        val dataStoreKey = preferencesKey<String>(key)
        val preferences = dataStore.data.first()
        return preferences[dataStoreKey]
    }

    fun checkadmin() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val querySnapshot = seniors.get().await()
            val admins = querySnapshot.get("emails") as ArrayList<String>
            Log.d("QR", useremail)
            Log.d("QR", admins.toString())
            navView.menu.findItem(R.id.nav_makeqr).isEnabled = admins.contains(useremail)
        } catch (e:Exception) {
            Log.d("checkadmin", e.message.toString())
        }
    }

    fun login() {
        if(loggedInState) {
            try {
                mGoogleSignInClient.signOut()
                auth.signOut()
                navView.menu.findItem(R.id.nav_logout).title = "Log In"
                loggedInState = false
                Toast.makeText(this, "Signed Out", Toast.LENGTH_SHORT).show()
            } catch (e:Exception) {
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
        }
        else {
            mGoogleSignInClient.signInIntent.also {
                startActivityForResult(it, AUTH_CODE)
            }
            navView.menu.findItem(R.id.nav_logout).title = "Log Out"
        }
    }

    private fun googleAuthForFirebase(account: GoogleSignInAccount) {
        val credentials = GoogleAuthProvider.getCredential(account.idToken, null)
        try {
            auth.signInWithCredential(credentials)
            loggedInState = true
        }
        catch (e:Exception){
            Log.d("QR", e.message.toString())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == AUTH_CODE) {
            val account = GoogleSignIn.getSignedInAccountFromIntent(data).result
            account?.let {
                useremail = account.email.toString()
                googleAuthForFirebase(it)
                checkadmin()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.d("QR", "Permission has been denied by user")
                } else {
                    onSelect(R.id.nav_scanqr)
                    Log.d("QR", "Permission has been granted by user")
                }
            }
        }
    }

    fun bluetooth(): Boolean {
        if(bt.check_paired()){
            if(bt.connect()){
                return true
            }else
                Toast.makeText(this,"Couldn't connect to AstrosLights", Toast.LENGTH_SHORT).show()
        }
        else
            Toast.makeText(this, "Device Not Paired", Toast.LENGTH_SHORT).show()
        return false
    }


    fun signedIn():String?{
        if(useremail != null)
            return useremail
        else
            return null
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}