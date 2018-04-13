package com.taskail.placesapp.main

import android.location.Location
import android.os.Bundle
import android.util.Log
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.Place
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.taskail.googleplacessearchdialog.SimplePlacesSearchDialog
import com.taskail.googleplacessearchdialog.SimplePlacesSearchDialogBuilder
import com.taskail.placesapp.R
import com.taskail.placesapp.data.PlacesRepository
import com.taskail.placesapp.getRepository
import com.taskail.placesapp.location.LocationServiceActivity
import com.taskail.placesapp.ui.TabsPagerAdapter
import com.taskail.placesapp.ui.animation.DismissibleAnimation
import com.taskail.placesapp.ui.animation.fabToFragmentReveal
import com.taskail.placesapp.util.favoritesString
import com.taskail.placesapp.util.nearbyString
import com.taskail.placesapp.util.isLollipopOrLater
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_viewpage_list.*

/**
 * the main entry point and the presenter for the application.
 */

class MainActivity : LocationServiceActivity(), MainContract.Presenter {

    private val TAG = javaClass.simpleName

    private lateinit var pagerAdapter: TabsPagerAdapter
    private var mapView: MainContract.MapView? = null
    private var currentLocation: Location? = null

    private lateinit var repository: PlacesRepository
    private lateinit var disposable: CompositeDisposable
    private lateinit var nearbyView: NearbyFragment

    private var locationReceived = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        disposable = CompositeDisposable()
        repository = getRepository(disposable)

        pagerAdapter = TabsPagerAdapter(supportFragmentManager)
        setupViewPager()

        fab.setOnClickListener {
            openMapsViewFragment()
        }
    }

    override fun fetchNearbyResults() {
        Log.d(TAG, "request results")
        if (locationReceived) {
            repository.getNearbyPlaces("establishment",
                    "${currentLocation?.latitude},${currentLocation?.longitude}",
                    1000,
                    {
                        Log.d(TAG, it.status)
                        if (it.status == "OK") {
                            nearbyView.displayResults(it.results)
                        }
                    })
        } else {
            Log.d(TAG, "no location")
        }
    }

    override fun lastKnowLocation(location: Location) {
        Log.d(TAG, "location received")
        currentLocation = location
        locationReceived = true

        if (nearbyView.resultHasBeenLoaded()) {

        }
    }

    override fun requestLocation(zoomToLocation: (LatLng) -> Unit) {
        getAccurateLocation {
            currentLocation = it
            val myLatLng = LatLng(it.latitude, it.longitude)
            zoomToLocation.invoke(myLatLng)
        }
    }

    /**
     *
     */
    override fun isLocationGranted(): Boolean {
        return permissionGranted
    }

    /**
     * Load the two main fragments into the viewpager
     */
    private fun setupViewPager() {

        pagerAdapter.addFragment(NearbyFragment().apply {
            Log.d(TAG, "adding fragment to view pager")
            nearbyView = this
            presenter = this@MainActivity
        }, nearbyString())
        pagerAdapter.addFragment(FavoritesFragment(), favoritesString())

        viewPager.adapter = pagerAdapter
        tabLayout.setupWithViewPager(viewPager)
    }

    /**
     * Fab button opens up the mapView.
     * Mapview will have a circular animation for
     * devices running Android 5.0 and above.
     */
    private fun openMapsViewFragment() {
        val mapFragment = (if (isLollipopOrLater())
            MapViewFragment
                    .newAnimatedInstance(
                            fabToFragmentReveal(fab, container)
                    )
        else
            MapViewFragment.newInstance()
                )
                .also {
                    mapView = it
                }.apply {
                    presenter = this@MainActivity
                }

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, mapFragment)
                .commit()
    }

    override fun closeMapView() {

        if (isLollipopOrLater()) {

            (mapView as DismissibleAnimation)
                    .dismiss {
                        removeMap()
                    }
        } else {
            removeMap()
        }

    }

    private fun removeMap() {
        supportFragmentManager
                .beginTransaction()
                .remove(mapView as MapViewFragment)
                .commit()
    }

    override fun handleSearchFabClick(latLngBounds: LatLngBounds) {
        SimplePlacesSearchDialogBuilder(this)
                .setResultsFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT)
                .setLatLngBounds(latLngBounds)
                .setLocationListener(object : SimplePlacesSearchDialog.PlaceSelectedCallback {
                    override fun onPlaceSelected(place: Place) {

                        val marker = MarkerOptions().position(place.latLng).title(place.name.toString())
                        mapView?.addMarker(marker)
                        mapView?.zoomToLocation(place.latLng)
                    }
                })
                .build()
                .show()
    }

    override fun onBackPressed() {
        if (mapView != null && mapView!!.isOpened) {
            closeMapView()
            return
        }
        super.onBackPressed()
    }

    override fun onStop() {
        super.onStop()
        disposable.clear()
    }
}
