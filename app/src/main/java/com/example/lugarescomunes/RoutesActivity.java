package com.example.lugarescomunes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lugarescomunes.repository.RoutesRepository;
import com.example.lugarescomunes.repository.PlacesRepository;
import com.example.lugarescomunes.models.api.RouteResponse;
import com.example.lugarescomunes.Place;

import java.util.ArrayList;
import java.util.List;

public class RoutesActivity extends AppCompatActivity {

    private static final String TAG = "RoutesActivity";

    // Constantes para Intent extras
    public static final String EXTRA_DESTINATION_ID = "destination_id";
    public static final String EXTRA_DESTINATION_NAME = "destination_name";

    // Views del header con detalles del lugar
    private Toolbar toolbar;
    private ImageView placeImageView;
    private ImageView placeTypeIcon;
    private TextView placeNameTextView;
    private TextView placeCategoryTextView;
    private TextView placeDescriptionTextView;
    private TextView placeWhat3wordsTextView;
    private TextView placeScheduleTextView;
    private TextView placeCapacityTextView;
    private View placeAvailabilityIndicator;
    private TextView placeAvailabilityTextView;

    // Views de rutas
    private TextView routesSectionTitleTextView;
    private RecyclerView routesRecyclerView;
    private ProgressBar loadingProgressBar;
    private TextView emptyStateTextView;

    // Adapter y datos
    private RoutesAdapter routesAdapter;
    private List<RouteResponse> routesList;

    // Repositorios
    private RoutesRepository routesRepository;
    private PlacesRepository placesRepository;

    // Datos del destino
    private String destinationId;
    private String destinationName;
    private Place destinationPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);

        Log.d(TAG, "=== ROUTES ACTIVITY INICIADA ===");

        // Obtener datos del intent
        getIntentData();

        // Inicializar repositorios
        routesRepository = RoutesRepository.getInstance();
        placesRepository = PlacesRepository.getInstance();

        // Inicializar views
        initializeViews();
        setupToolbar();
        setupRecyclerView();

        // Cargar datos del lugar y sus rutas
        loadPlaceDetailsAndRoutes();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        destinationId = intent.getStringExtra(EXTRA_DESTINATION_ID);
        destinationName = intent.getStringExtra(EXTRA_DESTINATION_NAME);

        Log.d(TAG, "Destino recibido - ID: " + destinationId + ", Nombre: " + destinationName);

        if (destinationId == null || destinationId.trim().isEmpty()) {
            Log.e(TAG, "ID de destino no válido");
            Toast.makeText(this, "Error: Destino no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (destinationName == null || destinationName.trim().isEmpty()) {
            destinationName = "Destino";
        }
    }

    private void initializeViews() {
        // Toolbar
        toolbar = findViewById(R.id.toolbar);

        // Views del lugar
        placeImageView = findViewById(R.id.placeImageView);
        placeTypeIcon = findViewById(R.id.placeTypeIcon);
        placeNameTextView = findViewById(R.id.placeNameTextView);
        placeCategoryTextView = findViewById(R.id.placeCategoryTextView);
        placeDescriptionTextView = findViewById(R.id.placeDescriptionTextView);
        placeWhat3wordsTextView = findViewById(R.id.placeWhat3wordsTextView);
        placeScheduleTextView = findViewById(R.id.placeScheduleTextView);
        placeCapacityTextView = findViewById(R.id.placeCapacityTextView);
        placeAvailabilityIndicator = findViewById(R.id.placeAvailabilityIndicator);
        placeAvailabilityTextView = findViewById(R.id.placeAvailabilityTextView);

        // Views de rutas
        routesSectionTitleTextView = findViewById(R.id.routesSectionTitleTextView);
        routesRecyclerView = findViewById(R.id.routesRecyclerView);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        emptyStateTextView = findViewById(R.id.emptyStateTextView);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Rutas disponibles");
        }
    }

    private void setupRecyclerView() {
        routesList = new ArrayList<>();
        routesAdapter = new RoutesAdapter(routesList, new RoutesAdapter.OnRouteClickListener() {
            @Override
            public void onRouteClick(RouteResponse route) {
                handleRouteClick(route);
            }

            @Override
            public void onNavigateClick(RouteResponse route) {
                handleNavigateClick(route);
            }
        });

        routesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        routesRecyclerView.setAdapter(routesAdapter);
    }

    // ✅ NUEVA FUNCIÓN: Cargar detalles del lugar y sus rutas
    private void loadPlaceDetailsAndRoutes() {
        showLoading(true);

        Log.d(TAG, "Cargando detalles del lugar: " + destinationId);

        // Primero cargar detalles del lugar
        placesRepository.getPlaceById(destinationId)
                .thenAccept(place -> {
                    runOnUiThread(() -> {
                        destinationPlace = place;
                        displayPlaceDetails(place);

                        // Luego cargar las rutas
                        loadRoutesToDestination();
                    });
                })
                .exceptionally(throwable -> {
                    runOnUiThread(() -> {
                        Log.w(TAG, "No se pudieron cargar detalles del lugar: " + throwable.getMessage());

                        // Crear un lugar básico con la información que tenemos
                        destinationPlace = new Place();
                        destinationPlace.setId(destinationId);
                        destinationPlace.setName(destinationName);
                        destinationPlace.setDescription("Información detallada no disponible");

                        displayPlaceDetails(destinationPlace);

                        // Continuar cargando rutas
                        loadRoutesToDestination();
                    });
                    return null;
                });
    }

    // ✅ NUEVA FUNCIÓN: Mostrar detalles del lugar
    private void displayPlaceDetails(Place place) {
        if (place == null) return;

        Log.d(TAG, "Mostrando detalles del lugar: " + place.getName());

        // Nombre del lugar
        placeNameTextView.setText(place.getName());

        // Categoría
        placeCategoryTextView.setText(place.getCategory());

        // Descripción
        if (place.getDescription() != null && !place.getDescription().trim().isEmpty()) {
            placeDescriptionTextView.setText(place.getDescription());
            placeDescriptionTextView.setVisibility(View.VISIBLE);
        } else {
            placeDescriptionTextView.setVisibility(View.GONE);
        }

        // What3words
        if (place.getWhat3words() != null && !place.getWhat3words().trim().isEmpty()) {
            placeWhat3wordsTextView.setText("📍 " + place.getWhat3words());
            placeWhat3wordsTextView.setVisibility(View.VISIBLE);
        } else {
            placeWhat3wordsTextView.setVisibility(View.GONE);
        }

        // Horario
        if (place.getSchedule() != null && !place.getSchedule().trim().isEmpty()) {
            placeScheduleTextView.setText("🕒 " + place.getSchedule());
            placeScheduleTextView.setVisibility(View.VISIBLE);
        } else {
            placeScheduleTextView.setVisibility(View.GONE);
        }

        // Capacidad
        if (place.getCapacity() > 0) {
            placeCapacityTextView.setText("👥 Capacidad: " + place.getCapacity() + " personas");
            placeCapacityTextView.setVisibility(View.VISIBLE);
        } else {
            placeCapacityTextView.setVisibility(View.GONE);
        }

        // Disponibilidad
        placeAvailabilityTextView.setText(place.isAvailable() ? "Disponible" : "Ocupado");
        placeAvailabilityIndicator.setBackgroundResource(
                place.isAvailable() ? R.drawable.circle_background_green : R.drawable.circle_background_red
        );
        placeAvailabilityTextView.setTextColor(getResources().getColor(
                place.isAvailable() ? android.R.color.holo_green_dark : android.R.color.holo_red_dark
        ));

        // Icono del tipo de lugar
        if (place.getType() != null) {
            placeTypeIcon.setImageResource(getIconForPlaceType(place.getType()));
            placeCategoryTextView.setBackgroundResource(getCategoryBackgroundForPlaceType(place.getType()));
        }

        // TODO: Cargar imagen real cuando esté disponible
        // placeImageView.setImageResource(R.drawable.placeholder_place_image);
    }

    private void loadRoutesToDestination() {
        Log.d(TAG, "Cargando rutas para destino: " + destinationId);

        routesRepository.getRoutesToDestination(destinationId)
                .thenAccept(routes -> {
                    runOnUiThread(() -> {
                        showLoading(false);

                        Log.d(TAG, "Rutas recibidas: " + (routes != null ? routes.size() : 0));

                        if (routes != null && !routes.isEmpty()) {
                            routesList.clear();
                            routesList.addAll(routes);
                            routesAdapter.notifyDataSetChanged();
                            showRoutesContent();

                            // Actualizar título de la sección
                            routesSectionTitleTextView.setText("🗺️ " + routes.size() + " rutas disponibles");

                            Log.i(TAG, "Rutas cargadas exitosamente: " + routes.size());
                            Toast.makeText(this, routes.size() + " rutas encontradas", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.w(TAG, "No se encontraron rutas para el destino");
                            showEmptyState();
                        }
                    });
                })
                .exceptionally(throwable -> {
                    runOnUiThread(() -> {
                        showLoading(false);
                        Log.e(TAG, "Error cargando rutas", throwable);

                        String errorMessage = "Error cargando rutas";
                        if (throwable.getMessage() != null) {
                            errorMessage += ": " + throwable.getMessage();
                        }

                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                        showEmptyState();
                    });
                    return null;
                });
    }

    private void handleRouteClick(RouteResponse route) {
        Log.d(TAG, "Click en ruta: " + route.getName());

        // Mostrar detalles de la ruta
        String routeInfo = String.format("📍 %s\n\n📏 %s\n⏱️ %s\n⚡ %s",
                route.getName(),
                route.getFormattedDistance() != null ? route.getFormattedDistance() : "Distancia no disponible",
                route.getFormattedTime() != null ? route.getFormattedTime() : "Tiempo no disponible",
                route.getDifficultyText() != null ? route.getDifficultyText() : "Dificultad no disponible");

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Detalles de la ruta")
                .setMessage(routeInfo)
                .setPositiveButton("Usar esta ruta", (dialog, which) -> handleNavigateClick(route))
                .setNeutralButton("Cerrar", null)
                .show();
    }

    private void handleNavigateClick(RouteResponse route) {
        Log.d(TAG, "Iniciar navegación con ruta: " + route.getName());

        String message = String.format("🧭 Iniciando navegación hacia %s\nUsando: %s",
                destinationName, route.getName());

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        // TODO: Implementar navegación real
        // Intent navigationIntent = new Intent(this, NavigationActivity.class);
        // navigationIntent.putExtra("route_id", route.getId());
        // navigationIntent.putExtra("destination_place", destinationPlace);
        // startActivity(navigationIntent);
    }

    private void showLoading(boolean show) {
        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (routesRecyclerView != null) {
            routesRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
        if (emptyStateTextView != null) {
            emptyStateTextView.setVisibility(View.GONE);
        }
    }

    private void showRoutesContent() {
        if (routesRecyclerView != null) {
            routesRecyclerView.setVisibility(View.VISIBLE);
        }
        if (emptyStateTextView != null) {
            emptyStateTextView.setVisibility(View.GONE);
        }
        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(View.GONE);
        }
    }

    private void showEmptyState() {
        if (emptyStateTextView != null) {
            emptyStateTextView.setVisibility(View.VISIBLE);
            emptyStateTextView.setText("No hay rutas disponibles hacia " + destinationName);
        }
        if (routesRecyclerView != null) {
            routesRecyclerView.setVisibility(View.GONE);
        }
        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(View.GONE);
        }
    }

    // Métodos de utilidad para iconos y colores
    private int getIconForPlaceType(PlaceType type) {
        switch (type) {
            case CLASSROOM: return R.drawable.ic_classroom;
            case LABORATORY: return R.drawable.ic_laboratory;
            case LIBRARY: return R.drawable.ic_library;
            case CAFETERIA: return R.drawable.ic_cafeteria;
            case OFFICE: return R.drawable.ic_office;
            case AUDITORIUM: return R.drawable.ic_auditorium;
            case SERVICE: return R.drawable.ic_service;
//            case PARKING: return R.drawable.ic_parking;
//            case RECREATION: return R.drawable.ic_recreation;
//            case ENTRANCE: return R.drawable.ic_entrance;
            default: return R.drawable.ic_classroom;
        }
    }

    private int getCategoryBackgroundForPlaceType(PlaceType type) {
        switch (type) {
            case CLASSROOM: return R.drawable.category_tag_background_blue;
            case LABORATORY: return R.drawable.category_tag_background_orange;
            case LIBRARY: return R.drawable.category_tag_background_purple;
            case CAFETERIA: return R.drawable.category_tag_background_green;
            case OFFICE: return R.drawable.category_tag_background_red;
            case AUDITORIUM: return R.drawable.category_tag_background_gray;
            case SERVICE: return R.drawable.category_tag_background_blue;
            default: return R.drawable.category_tag_background;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "RoutesActivity resumed");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "RoutesActivity paused");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "RoutesActivity destroyed");
    }
}