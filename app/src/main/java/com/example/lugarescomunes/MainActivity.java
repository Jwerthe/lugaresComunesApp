package com.example.lugarescomunes;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lugarescomunes.repository.PlacesRepository;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Views del Splash Screen
    private LinearLayout splashContainer;
    private LinearLayout mainContentContainer;

    // Views del Header
    private ImageView searchIconImageView;
    private ImageView profileIconImageView;
    private ImageView mapIconImageView;
    private LinearLayout searchBarContainer;
    private EditText searchEditText;
    private ImageView clearSearchImageView;

    // Views del contenido principal
    private RecyclerView placesRecyclerView;
    private LinearLayout emptyStateContainer;
    private ProgressBar loadingProgressBar;

    // Adapter y datos
    private PlacesAdapter placesAdapter;
    private List<Place> placesList;
    private List<Place> filteredPlacesList;
    private PlacesRepository placesRepository;

    // Constantes
    private static final int SPLASH_DURATION = 2500; // 2.5 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();

        // CRÍTICO: Inicializar repositorio ANTES de setupRecyclerView
        placesRepository = PlacesRepository.getInstance();

        // Verificar que se inicializó correctamente
        if (placesRepository == null) {
            Toast.makeText(this, "Error: No se pudo inicializar el repositorio", Toast.LENGTH_LONG).show();
            return;
        }

        setupRecyclerView();     // Ahora ya puede usar placesRepository
        setupClickListeners();
        setupSearchFunctionality();

        // Mostrar splash screen y luego la pantalla principal
        showSplashScreen();
    }
    private void initializeViews() {
        // Splash screen views
        splashContainer = findViewById(R.id.splashContainer);
        mainContentContainer = findViewById(R.id.mainContentContainer);

        // Header views
        searchIconImageView = findViewById(R.id.searchIconImageView);
        profileIconImageView = findViewById(R.id.profileIconImageView);
        mapIconImageView = findViewById(R.id.mapIconImageView);
        searchBarContainer = findViewById(R.id.searchBarContainer);
        searchEditText = findViewById(R.id.searchEditText);
        clearSearchImageView = findViewById(R.id.clearSearchImageView);

        // Content views
        placesRecyclerView = findViewById(R.id.placesRecyclerView);
        emptyStateContainer = findViewById(R.id.emptyStateContainer);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
    }

    private void setupRecyclerView() {
        // Inicializar listas
        placesList = new ArrayList<>();
        filteredPlacesList = new ArrayList<>();

        // Configurar RecyclerView
        placesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        placesAdapter = new PlacesAdapter(filteredPlacesList, this);
        placesRecyclerView.setAdapter(placesAdapter);

        // Configurar listener para clicks en las cards
        placesAdapter.setOnPlaceClickListener(new PlacesAdapter.OnPlaceClickListener() {
            @Override
            public void onPlaceClick(Place place) {
                // Navegar a detalles del lugar
                Intent intent = PlaceDetailActivity.createIntent(MainActivity.this, place);
                startActivity(intent);
            }

            @Override
            public void onFavoriteClick(Place place) {
                // Manejar click en favorito
                String message = place.isFavorite() ?
                        "Agregado a favoritos" : "Removido de favoritos";
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNavigateClick(Place place) {
                // Navegar directamente al lugar
                Intent intent = PlaceDetailActivity.createIntent(MainActivity.this, place);
                startActivity(intent);
            }
        });

        // Cargar datos de ejemplo
        loadSampleData();
    }

    private void setupClickListeners() {
        // Click en icono de búsqueda
        searchIconImageView.setOnClickListener(v -> toggleSearchBar());

        mapIconImageView.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(intent);
        });

        // Click en icono de perfil
        profileIconImageView.setOnClickListener(v -> {
            // TODO: Implementar navegación a perfil
            // Por ahora solo mostramos un toast
            android.widget.Toast.makeText(this, "Perfil de usuario", android.widget.Toast.LENGTH_SHORT).show();
        });

        // Click en limpiar búsqueda
        clearSearchImageView.setOnClickListener(v -> {
            searchEditText.setText("");
            clearSearchImageView.setVisibility(View.GONE);
        });
    }

    private void setupSearchFunctionality() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No necesitamos implementar esto
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Mostrar/ocultar botón de limpiar
                clearSearchImageView.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);

                // Filtrar lugares
                filterPlaces(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No necesitamos implementar esto
            }
        });

        // Acción de búsqueda al presionar enter
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                // Ocultar teclado
                android.view.inputmethod.InputMethodManager imm =
                        (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
                return true;
            }
            return false;
        });
    }

    private void showSplashScreen() {
        splashContainer.setVisibility(View.VISIBLE);
        mainContentContainer.setVisibility(View.GONE);

        // Después del tiempo definido, mostrar contenido principal
        new Handler(Looper.getMainLooper()).postDelayed(this::showMainContent, SPLASH_DURATION);
    }

    private void showMainContent() {
        splashContainer.setVisibility(View.GONE);
        mainContentContainer.setVisibility(View.VISIBLE);

        // Cargar datos (simulamos carga)
        loadPlaces();
    }

    private void toggleSearchBar() {
        if (searchBarContainer.getVisibility() == View.GONE) {
            searchBarContainer.setVisibility(View.VISIBLE);
            searchEditText.requestFocus();

            // Mostrar teclado
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.showSoftInput(searchEditText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
        } else {
            searchBarContainer.setVisibility(View.GONE);
            searchEditText.setText("");

            // Ocultar teclado
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
        }
    }

    private void loadPlaces() {
        showLoading(true);

        // Simular carga de datos desde API
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            showLoading(false);
            updatePlacesList();
        }, 1500);
    }

    private void loadSampleData() {
        // Cargar datos desde el repositorio
        placesRepository.getAllPlaces()
                .thenAccept(places -> {
                    runOnUiThread(() -> {
                        placesList.clear();
                        placesList.addAll(places);
                        filteredPlacesList.clear();
                        filteredPlacesList.addAll(places);
                        placesAdapter.notifyDataSetChanged();
                    });
                })
                .exceptionally(throwable -> {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Error cargando lugares: " + throwable.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
                    return null;
                });
    }

    private void filterPlaces(String query) {
        if (query.isEmpty()) {
            filteredPlacesList.clear();
            filteredPlacesList.addAll(placesList);
            updatePlacesList();
        } else {
            // Usar repositorio para búsqueda
            placesRepository.searchPlaces(query)
                    .thenAccept(places -> {
                        runOnUiThread(() -> {
                            filteredPlacesList.clear();
                            filteredPlacesList.addAll(places);
                            updatePlacesList();
                        });
                    })
                    .exceptionally(throwable -> {
                        runOnUiThread(() -> {
                            // En caso de error, filtrar localmente
                            filteredPlacesList.clear();
                            String lowercaseQuery = query.toLowerCase();
                            for (Place place : placesList) {
                                if (place.getName().toLowerCase().contains(lowercaseQuery) ||
                                        place.getCategory().toLowerCase().contains(lowercaseQuery) ||
                                        place.getDescription().toLowerCase().contains(lowercaseQuery) ||
                                        place.getWhat3words().toLowerCase().contains(lowercaseQuery)) {
                                    filteredPlacesList.add(place);
                                }
                            }
                            updatePlacesList();
                        });
                        return null;
                    });
        }
    }

    private void updatePlacesList() {
        placesAdapter.notifyDataSetChanged();

        if (filteredPlacesList.isEmpty()) {
            emptyStateContainer.setVisibility(View.VISIBLE);
            placesRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateContainer.setVisibility(View.GONE);
            placesRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean show) {
        loadingProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        placesRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        emptyStateContainer.setVisibility(View.GONE);
    }
}