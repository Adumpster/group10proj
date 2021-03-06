package com.example.jdeiv.picoftheday

import android.Manifest
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
//import android.widget.Toolbar
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.HashMap
//import com.soundcloud.android.crop.Crop



class MainActivity : AppCompatActivity(), com.google.android.gms.location.LocationListener {

    private var mViewpager: NonSwipeableViewPager? = null
    lateinit var toolbar: ActionBar
    private var REQUEST_LOCATION_CODE = 101
    private var mGoogleApiClient: GoogleApiClient? = null
    private lateinit var auth: FirebaseAuth
    var menuToolbar : Menu? = null

    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        //transaction.replace(R.id.fragmentContainerFrameLayout, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when(item.itemId) {
            R.id.navigation_camera -> {
                //toolbar.title = "Camera"
                //Some of these lines can be removed
                /*val intent = Intent(this, UploadActivity::class.java)
                startActivity(intent)*/
                /*val cameraFragment = CameraFragment.newInstance()
                openFragment(cameraFragment)*/
                mViewpager?.setCurrentItem(0)
                val checkmark = menuToolbar?.findItem(R.id.upload_check)
                //checkmark?.isVisible = true

                //PASS checkmark TO THE OTHER FRAGMENT SO THAT WE CAN PUT A LISTENER TO IT

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_home -> {
                //toolbar.title = "Home"
                /*val homeFragment = HomeFragment.newInstance()
                openFragment(homeFragment)*/

                mViewpager?.setCurrentItem(1)
                val checkmark = menuToolbar?.findItem(R.id.upload_check)
                checkmark?.isVisible = false
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_picOfTheDay -> {
                /*toolbar.title = "Pic of the Day"
                val picofthedayFragment = PicofthedayFragment.newInstance()
                openFragment(picofthedayFragment)*/
                mViewpager?.setCurrentItem(2)
                val checkmark = menuToolbar?.findItem(R.id.upload_check)
                checkmark?.isVisible = false
                return@OnNavigationItemSelectedListener true
            }
        }
        false

    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(my_toolbar)
        buildGoogleApiClient()

        createLocationFile()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Location Permission already granted
            checkLocationPermission()
        }

        if (!checkGPSEnabled()) {
        }else {
            Log.d("hejhej", "1")
            LocationTask(this, LocationServices.getFusedLocationProviderClient(this),this).execute()
        }

        /* Testing another fragment solution */
        val fragmentHashMap = HashMap<Int, Fragment>().apply {
            put(0, CameraFragment.newInstance())
            put(1, HomeFragment.newInstance())
            put(2, PicofthedayFragment.newInstance())
        }
        mViewpager = view_pager
        this.mViewpager?.setAdapter(ViewPagerAdapter(supportFragmentManager, fragmentHashMap))
        mViewpager?.setCurrentItem(1)
        mViewpager?.offscreenPageLimit = 2

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        /* Setting the first page to home page. This is needed for content to load without having to press a tab. */
        bottomNavigation.selectedItemId = R.id.navigation_home

        getSupportActionBar()?.setDisplayShowHomeEnabled(true)
        getSupportActionBar()?.setDisplayShowTitleEnabled(false)

    }

    private fun createLocationFile(){
        val fileName = "/location.txt"
        val file = File(this.dataDir.toString() + fileName)
    }

    override fun onLocationChanged(location: Location?) {
        //Boilerplate mumbojumbo
    }

    @Synchronized
    private fun buildGoogleApiClient() {

        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .build()
        mGoogleApiClient!!.connect()

    }

    private fun checkGPSEnabled(): Boolean {
        if (!isLocationEnabled())
            showAlert()
        return isLocationEnabled()
    }

    private fun showAlert() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Enable Location")
        dialog.setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " + "use this app")

        dialog.setPositiveButton("Location Settings") { paramDialogInterface, paramInt ->
                val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(myIntent)
            }

        dialog.setNegativeButton("Exit application") { paramDialogInterface, paramInt ->
            this.finishAffinity()
            }
        dialog.show()

    }

    private fun isLocationEnabled(): Boolean {
        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager!!.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)
    }

    private fun checkLocationPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder(this)
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs the Location permission, please accept to use location functionality")
                    .setPositiveButton("OK", { dialog, which ->
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_CODE)
                    })
                    .create()
                    .show()

            } else ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_LOCATION_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "permission granted", Toast.LENGTH_LONG).show()
                    }
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mGoogleApiClient?.connect()

        /* Check if user is logged in */
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            /* Send the user back to login screen. */
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } // Else, it's fine. User is logged in.
    }

    override fun onStop() {
        super.onStop()
        if (mGoogleApiClient!!.isConnected()) {
            mGoogleApiClient!!.disconnect()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflating the toolbar menu
        menuInflater.inflate(R.menu.top_menu_main, menu)
        val checkmark = menu?.findItem(R.id.upload_check)
        checkmark?.isVisible = false
        menuToolbar = menu
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.getItemId()

        if (id == R.id.settings_button){
            // Send user to settings page
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            finish()
            /* The following return should not run, but is necessary in order to compile */
            return false
        } else {
            return super.onOptionsItemSelected(item)
        }
    }

}