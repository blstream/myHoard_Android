package com.myhoard.app.gps;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

/**
 * Klasa pobierająca na bierzące dane GPS i przekazująca do aktywności przy
 * pomocy przywiązanej usługi
 *
 * Created by Sebastian Peryt on 07.03.14.
 */
//TODO in production
public class GPSProvider extends Service implements LocationListener {

	private static final String TAG = GPSProvider.class
			.getSimpleName();
	private static final boolean D = true;// debug

	// Zmienne location
	private LocationManager locationManager = null;
	private Location lastLocation = null;
	private String locationProvider;
	private double lon;
	private double lat;
	// KONIEC - Zmienne location

	private boolean enabledGPS = false; // dostępny jest provider GPS

	// Binder dla aktywnosci przywiazanych
	private final IBinder mBinder = new LocalGPSBinder();
	public static final String BROADCAST_ACTION = "com.myhoard.app.displayevent";

	// KONIEC - Zmienne sieci

	public GPSProvider() {
		if (D)
			Log.d(TAG, "Konstruktor");
	}

	@Override
	public void onCreate() {
		super.onCreate();

		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		// Pobranie danych nt. tego, czy dostepne są wybrane LocationManagery
		enabledGPS = locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);

		// Uruchamianie providera
		locationProvider = LocationManager.GPS_PROVIDER;

		// Pobranie i ew. ustawienie ostatniej wartości
		lastLocation = locationManager.getLastKnownLocation(locationProvider);
		if (lastLocation != null) {
			onLocationChanged(lastLocation);
			if (D)
				Log.d(TAG, "Jest lastLocation");
			if (D)
				Log.d(TAG, "lastLocation: " + lastLocation.getLatitude()
						+ " / " + lastLocation.getLongitude());
		} else {
			if (D)
				Log.d(TAG, "Brak lastLocation");
		}
	}

	/**
	 * Funkcja zwraca IBinder, który ma info nt. pozycji i statusu GPS
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		if (D)
			Log.d(TAG, "bind");

		locationManager
				.requestLocationUpdates(locationProvider, 10000, 2, this);

		return mBinder;
	}

	@Override
	public void onDestroy() {
		if (D)
			Log.d(TAG, "KILL");
		locationManager.removeUpdates(this);
	}

	/**
	 * Klasa bindera klineta
	 */
	public class LocalGPSBinder extends Binder {
		public GPSProvider getService() {
			// Zwrocenie instancje GPSProvide aby klienci mogli
			// wywolywac metody publiczne
			return GPSProvider.this;
		}
	}

	/**
	 * Metoda uruchamiana przy zmianie pozycji, lub po ustalonym czasie
	 *
	 */
	@Override
	public void onLocationChanged(Location location) {
		if (D)
			Log.d(TAG, "Uruchomiono onLocationChaged");
		if (location == null)
			return;

		if (lastLocation != null && lastLocation.equals(location))
			return;

		lon = location.getLongitude();
		lat = location.getLatitude();

		Bundle b = new Bundle();
		// Przygotowanie broadcasta
		Intent out = new Intent(BROADCAST_ACTION);
		//b.putDouble(GConstants.POI_LON, lon);
		//b.putDouble(GConstants.POI_LAT, lat);
		out.putExtras(b);
		// Wyslanie broadcasta
		sendBroadcast(out);

		lastLocation = location;
	}

    @Override
    public void onStatusChanged(String provider, int status, Bundle bundle) {
        switch (status) {
            case LocationProvider.AVAILABLE:
                if (D)
                    Log.d(TAG, "GPS available again");
                enabledGPS = true;
                break;
            case LocationProvider.OUT_OF_SERVICE:
                if (D)
                    Log.d(TAG, "GPS out of service");
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                if (D)
                    Log.d(TAG, "GPS temporarily unavailable");
                enabledGPS = false;
                break;
        }
        Bundle b = new Bundle();
        // Przygotowanie broadcasta
        Intent out = new Intent(BROADCAST_ACTION);
        b.putBoolean("GPS", enabledGPS);
        out.putExtras(b);
        // Wyslanie broadcasta
        sendBroadcast(out);
    }

    /**
	 * Metoda od LocationListener
	 */
	@Override
	public void onProviderDisabled(String arg0) {
		if (D)
			Log.d(TAG, "onProviderDisabled");
        Bundle b = new Bundle();
        // Przygotowanie broadcasta
        Intent out = new Intent(BROADCAST_ACTION);
        b.putBoolean("GPS", false);
        out.putExtras(b);
        // Wyslanie broadcasta
        sendBroadcast(out);
	}

	/**
	 * Metoda od LocationListener
     * ?? Never called ??
	 */
	@Override
	public void onProviderEnabled(String provider) {
		if (D)
			Log.d(TAG, "onProviderEnabled");
	}
}
