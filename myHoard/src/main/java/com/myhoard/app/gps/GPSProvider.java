package com.myhoard.app.gps;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * Klasa pobierająca na bierzące dane GPS i przekazująca do aktywności przy
 * pomocy przywiązanej usługi
 * <p/>
 * Created by Sebastian Peryt on 07.03.14.
 */
public class GPSProvider extends Service implements LocationListener {

	public static final String BROADCAST_ACTION = "com.myhoard.app.displayevent";
	private static final String TAG = GPSProvider.class
			.getSimpleName();
	private static final boolean D = true;// debug

    public static final String POS_LON = "lon";
    public static final String POS_LAT = "lat";

	// Binder dla aktywnosci przywiazanych
	private final IBinder mBinder = new LocalGPSBinder();
	// Zmienne location
	private LocationManager locationManager = null;
	private Location lastLocation = null;
	private String locationProvider;
	// KONIEC - Zmienne location
	private double lon;
	private double lat;
	private boolean enabledGPS = false; // dostępny jest provider GPS

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

        Bundle b = new Bundle();
        // Przygotowanie broadcasta
        Intent out = new Intent(BROADCAST_ACTION);
        b.putBoolean("gps",enabledGPS);
        out.putExtras(b);
        // Wyslanie broadcasta
        sendBroadcast(out);

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

	@Override
	public void onDestroy() {
		if (D)
			Log.d(TAG, "KILL");
		locationManager.removeUpdates(this);
	}

	/**
	 * Funkcja zwraca IBinder, który ma info nt. pozycji i statusu GPS
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		if (D)
			Log.d(TAG, "bind");
/* AWA:FIXME: Magic numbers
*/
		locationManager
				.requestLocationUpdates(locationProvider, 10000, 5, this);

		return mBinder;
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
        b.putDouble(GPSProvider.POS_LON, lon);
        b.putDouble(GPSProvider.POS_LAT, lat);
        b.putBoolean("gps",enabledGPS);
        out.putExtras(b);
        // Wyslanie broadcasta
        sendBroadcast(out);

        lastLocation = location;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
        Log.e("TAG","enabled: " + s);
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.e("TAG","disabled: " + s);
    }
}
