package com.example.lugarescomunes;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.lugarescomunes.repository.PlacesRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.widget.Toolbar;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    // Coordenadas del campus PUCE (centro aproximado)
    private static final LatLng PUCE_CAMPUS = new LatLng(-0.210759, -78.487359);

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private PlacesRepository placesRepository;
    private Map<Marker, Place> markerPlaceMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Inicializar servicios
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        placesRepository = PlacesRepository.getInstance();
        markerPlaceMap = new HashMap<>();

        // Inicializar mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();  // Cierra esta actividad y vuelve atrás
        return true;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        Log.d(TAG, "Mapa listo, configurando...");

        // Configurar mapa
        setupMap();

        // Verificar permisos y obtener ubicación
        checkLocationPermission();

        // Cargar lugares
        loadPlacesOnMap();
    }

    private void setupMap() {
        // Configurar tipo de mapa
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        // Mover cámara al campus PUCE
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(PUCE_CAMPUS, 17f));

        // Configurar controles
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Listener para clicks en marcadores
        mMap.setOnMarkerClickListener(marker -> {
            Place place = markerPlaceMap.get(marker);
            if (place != null) {
                showPlaceInfo(place);
            }
            return false;
        });

        // Listener para clicks en ventana de información
        mMap.setOnInfoWindowClickListener(marker -> {
            Place place = markerPlaceMap.get(marker);
            if (place != null) {
                // Abrir detalles del lugar
                startActivity(PlaceDetailActivity.createIntent(this, place));
            }
        });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);

            // Obtener ubicación actual
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                Log.d(TAG, "Ubicación actual: " + currentLocation);

                                // Si está dentro del campus, centrar en ubicación actual
                                if (isLocationInCampus(currentLocation)) {
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18f));
                                }
                            }
                        }
                    });
        }
    }

    private boolean isLocationInCampus(LatLng location) {
        // Verificar si la ubicación está aproximadamente dentro del campus PUCE
        double campusLat = PUCE_CAMPUS.latitude;
        double campusLng = PUCE_CAMPUS.longitude;
        double radius = 0.005; // Aproximadamente 500m

        return Math.abs(location.latitude - campusLat) < radius &&
                Math.abs(location.longitude - campusLng) < radius;
    }

    private void loadPlacesOnMap() {
        Log.d(TAG, "Cargando lugares en el mapa...");

        placesRepository.getAllPlaces()
                .thenAccept(places -> {
                    runOnUiThread(() -> {
                        Log.d(TAG, "Agregando " + places.size() + " lugares al mapa");
                        addPlacesToMap(places);
                    });
                })
                .exceptionally(throwable -> {
                    runOnUiThread(() -> {
                        Log.e(TAG, "Error cargando lugares", throwable);
                        Toast.makeText(this, "Error cargando lugares: " + throwable.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
                    return null;
                });
    }

    private void addPlacesToMap(List<Place> places) {
        mMap.clear();
        markerPlaceMap.clear();

        for (Place place : places) {
            if (place.getLatitude() != 0 && place.getLongitude() != 0) {
                LatLng position = new LatLng(place.getLatitude(), place.getLongitude());

                MarkerOptions markerOptions = new MarkerOptions()
                        .position(position)
                        .title(place.getName())
                        .snippet(place.getCategory() + " • " + place.getWhat3words())
                        .icon(BitmapDescriptorFactory.defaultMarker(getMarkerColor(place.getType())));

                Marker marker = mMap.addMarker(markerOptions);
                if (marker != null) {
                    markerPlaceMap.put(marker, place);
                }
            }
        }

        Log.d(TAG, "Marcadores agregados: " + markerPlaceMap.size());
    }

    private float getMarkerColor(PlaceType placeType) {
        switch (placeType) {
            case CLASSROOM:
                return BitmapDescriptorFactory.HUE_BLUE;
            case LABORATORY:
                return BitmapDescriptorFactory.HUE_ORANGE;
            case LIBRARY:
                return BitmapDescriptorFactory.HUE_VIOLET;
            case CAFETERIA:
                return BitmapDescriptorFactory.HUE_GREEN;
            case OFFICE:
                return BitmapDescriptorFactory.HUE_RED;
            case AUDITORIUM:
                return BitmapDescriptorFactory.HUE_YELLOW;
            case SERVICE:
                return BitmapDescriptorFactory.HUE_CYAN;
            default:
                return BitmapDescriptorFactory.HUE_RED;
        }
    }

    private void showPlaceInfo(Place place) {
        String info = String.format("%s\n%s\nDisponible: %s\n📍 %s",
                place.getName(),
                place.getDescription(),
                place.isAvailable() ? "Sí" : "No",
                place.getWhat3words());

        Toast.makeText(this, info, Toast.LENGTH_LONG).show();
    }

    // Método público para navegar a un lugar específico
    public void navigateToPlace(Place place) {
        if (mMap != null && place.getLatitude() != 0 && place.getLongitude() != 0) {
            LatLng position = new LatLng(place.getLatitude(), place.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 19f));

            // Encontrar y mostrar el marcador
            for (Map.Entry<Marker, Place> entry : markerPlaceMap.entrySet()) {
                if (entry.getValue().getId().equals(place.getId())) {
                    entry.getKey().showInfoWindow();
                    break;
                }
            }
        }
    }
}