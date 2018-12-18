package com.nanotrick.nearrbyplaceskotlim

import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.nanotrick.nearrbyplaceskotlim.Common.Common.googleApiService
import com.nanotrick.nearrbyplaceskotlim.Model.MyPlaces
import com.nanotrick.nearrbyplaceskotlim.Remote.IGoogleAPIService
import kotlinx.android.synthetic.main.activity_maps.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private var latitude:Double=0.toDouble()
    private var  longtude:Double=0.toDouble()

    private lateinit var mLastLocation:Location
    private var mMarker:Marker?=null

    //Location
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var  locationRequest: LocationRequest
    lateinit var  locationCallback: LocationCallback
    companion object {
        private const val MY_PERMISSION_CODE: Int = 1000

    }
    lateinit var mServices:IGoogleAPIService
    internal lateinit var currentPlaces: MyPlaces


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //Init servies
        mServices = googleApiService

        // Request rintiome permossion
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
          if   (checkLocationPermission()){
        buildLocationRequest()
        buildLocationCallBack()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper())
    }
}
    else
    {
        buildLocationRequest()
        buildLocationCallBack()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper())
    }
        bootom_navigation_view.setOnNavigationItemReselectedListener{ item ->
            when(item.itemId)
            {
                R.id.action_hospital -> nearByPlace ("hospital")
                R.id.action_market -> nearByPlace ("market")
                R.id.action_resturaqnt -> nearByPlace ("restaurant")
                R.id.action_school -> nearByPlace ("school")
            }
            true
        }
}

    private fun nearByPlace(typePlace: String) {
        //Clear all marker on Map
        mMap.clear()
        //build url request base on location
        val url = getUrl(latitude,longtude,typePlace)

        mServices.getNearbyPlace(url)
            .enqueue(object : Callback<MyPlaces>{
                override fun onFailure(call: Call<MyPlaces>, t: Throwable) {
                 Toast.makeText(baseContext,""+t!!.message,Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<MyPlaces>, response: Response<MyPlaces>) {
                    currentPlaces = response!!.body()!!

                    if (response!!.isSuccessful)
                    {
                        for (i in 0 until  response!!.body()!!.results!!.size) {
                            val markerOptions = MarkerOptions()
                            val googlePlace = response.body()!!.results!![i]
                            val lat = googlePlace.geometry!!.location!!.lat
                            val lng = googlePlace.geometry!!.location!!.lng
                            val placeName = googlePlace.name
                            val latLng = LatLng(lat, lng)

                            markerOptions.position(latLng)
                            markerOptions.title(placeName)
                            if (typePlace == "hospital")
                                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_hospital))
                            else if (typePlace == "market")
                                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_market))
                            else if (typePlace == "school")
                                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_school))
                            else if (typePlace == "restaurant")
                                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_resturent))
                            else
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

                            markerOptions.snippet(i.toString()) // Assign index for market

                            //add marker to map


                            //move camera
                            mMap!!.addMarker(markerOptions)
                            mMap!!.moveCamera(CameraUpdateFactory.newLatLng(LatLng(lat, lng)))
                            mMap!!.animateCamera(CameraUpdateFactory.zoomTo(15f))
                        }


                    }
                }

            })





            }

    private fun getUrl(latitude: Double, longtude: Double, typePlace: String): String {


        val googlePlaceUrl = StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
        googlePlaceUrl.append("?location=$latitude,$longtude")
        googlePlaceUrl.append("&radius=500")
        googlePlaceUrl.append("&type=$typePlace")
        googlePlaceUrl.append("&key=AIzaSyBVQa5STmCt86ag-e4MFLf9rLqK5aWGBGM")

        Log.d("URL_DEBUG",googlePlaceUrl.toString())
        return  googlePlaceUrl.toString()


    }


    private fun buildLocationCallBack() {
        locationCallback = object  : LocationCallback() {

            override fun onLocationResult(p0: LocationResult?) {
               mLastLocation = p0!!.locations.get(p0.locations.size-1) // Get Last Location

                if (mMarker != null)
                    (
                            mMarker!!.remove()
                            )
                latitude = mLastLocation.latitude
                longtude = mLastLocation.longitude

                val latLng = LatLng(latitude,longtude)
                val  markerOptions = MarkerOptions()
                    .position(latLng)
                    .title("your position")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                mMarker = mMap.addMarker(markerOptions)


                //move Camera
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                mMap.animateCamera(CameraUpdateFactory.zoomTo(11f))
            }
        }}

    override fun onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.fastestInterval =5000
        locationRequest.smallestDisplacement= 10f
        locationRequest.fastestInterval = 3000
    }


    private fun checkLocationPermission() :Boolean{
        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.ACCESS_FINE_LOCATION))
                ActivityCompat.requestPermissions(this, arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ),MY_PERMISSION_CODE)
            else
                ActivityCompat.requestPermissions(this, arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ),MY_PERMISSION_CODE)
            return false
        }
        else
            return true


    }


    // override OnRequestpermission"Request
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode)
        {
            MY_PERMISSION_CODE->{
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                        if (checkLocationPermission()) {
                            mMap.isMyLocationEnabled = true
                        }
                }
                else
                {
                    Toast.makeText(this,"permission denied" ,Toast.LENGTH_SHORT).show()
                }
            }


        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //Init Google play services

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mMap.isMyLocationEnabled = true

            }

        } else
            mMap.isBuildingsEnabled = true

        //enaple zoom controol
        mMap.uiSettings.isZoomControlsEnabled=true
        mMap.uiSettings.isCompassEnabled=true
        mMap.uiSettings.isMyLocationButtonEnabled=true

    }
}
