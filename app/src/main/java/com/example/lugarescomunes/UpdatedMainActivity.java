package com.example.lugarescomunes;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import com.example.lugarescomunes.repository.PlacesRepository; // ✅ USAR EL REPOSITORIO CORRECTO
import com.example.lugarescomunes.repository.AuthRepository;
import com.example.lugarescomunes.models.api.UserResponse;

import java.util.ArrayList;
import java.util.List;

public class UpdatedMainActivity extends AppCompatActivity {

    private static final String TAG = "UpdatedMainActivity";
    private static final int SPLASH_DURATION = 1500; // 1.5 segundos

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
    private TextView userWelcomeTextView;

    // Adapter y datos
    private PlacesAdapter placesAdapter;
    private List<Place> placesList;
    private List<Place> filteredPlacesList;

    // Repositorios
    private PlacesRepository placesRepository; // ✅ USAR EL REPOSITORIO CORRECTO
    private AuthRepository authRepository;

    // Estado del usuario
    private boolean isLoggedIn = false;
    private UserResponse currentUser = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "=== UPDATED MAIN ACTIVITY INICIADA ===");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar repositorios
        placesRepository = PlacesRepository.getInstance(); // ✅ USAR EL REPOSITORIO CORRECTO
        authRepository = AuthRepository.getInstance(this);

        // Verificar estado de autenticación
        checkAuthenticationStatus();

        initializeViews();
        setupRecyclerView();
        setupClickListeners();
        setupSearchFunctionality();

        // Mostrar splash screen primero
        showSplashScreen();
    }

    private void checkAuthenticationStatus() {
        isLoggedIn = authRepository.isLoggedIn();
        Log.d(TAG, "Usuario logueado: " + isLoggedIn);

        if (isLoggedIn) {
            currentUser = authRepository.getCurrentUserSync();
            Log.d(TAG, "Usuario actual: " + (currentUser != null ? currentUser.getFullName() : "null"));

            // Opcional: Actualizar datos del usuario desde el servidor
            authRepository.getCurrentUser()
                    .thenAccept(user -> {
                        if (user != null) {
                            currentUser = user;
                            runOnUiThread(this::updateUserInterface);
                        }
                    })
                    .exceptionally(throwable -> {
                        Log.w(TAG, "Error actualizando datos de usuario", throwable);
                        return null;
                    });
        }
    }

    private void initializeViews() {
        // Splash views
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

        // User welcome text (agregar al layout si no existe)
        userWelcomeTextView = findViewById(R.id.userWelcomeTextView);

        Log.d(TAG, "Views inicializadas correctamente");
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
                Log.d(TAG, "Click en lugar: " + place.getName());
                // Navegar a detalles del lugar
                Intent intent = PlaceDetailActivity.createIntent(UpdatedMainActivity.this, place);
                startActivity(intent);
            }

            @Override
            public void onFavoriteClick(Place place) {
                handleFavoriteClick(place);
            }

            @Override
            public void onNavigateClick(Place place) {
                Log.d(TAG, "Navegación a lugar: " + place.getName());
                // Navegar directamente al lugar
                Intent intent = PlaceDetailActivity.createIntent(UpdatedMainActivity.this, place);
                startActivity(intent);
            }
        });

        Log.d(TAG, "RecyclerView configurado correctamente");
    }

    private void setupClickListeners() {
        // Click en icono de búsqueda
        searchIconImageView.setOnClickListener(v -> toggleSearchBar());

        // Click en icono de mapa - ✅ CORRECCIÓN: Manejo seguro
        mapIconImageView.setOnClickListener(v -> {
            try {
                Log.d(TAG, "Abriendo MapsActivity");
                Intent intent = new Intent(UpdatedMainActivity.this, MapsActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error abriendo MapsActivity", e);
                Toast.makeText(this, "Error abriendo el mapa", Toast.LENGTH_SHORT).show();
            }
        });

        // Click en icono de perfil
        profileIconImageView.setOnClickListener(v -> handleProfileClick());

        // Click en limpiar búsqueda
        clearSearchImageView.setOnClickListener(v -> {
            searchEditText.setText("");
            clearSearchImageView.setVisibility(View.GONE);
        });

        Log.d(TAG, "Click listeners configurados");
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

                // Filtrar lugares en tiempo real
                filterPlaces(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No necesitamos implementar esto
            }
        });

        Log.d(TAG, "Funcionalidad de búsqueda configurada");
    }

    private void showSplashScreen() {
        splashContainer.setVisibility(View.VISIBLE);
        mainContentContainer.setVisibility(View.GONE);

        Log.d(TAG, "Mostrando splash screen");

        // Después del splash, mostrar contenido principal
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            showMainContent();
        }, SPLASH_DURATION);
    }

    private void showMainContent() {
        Log.d(TAG, "Mostrando contenido principal");

        splashContainer.setVisibility(View.GONE);
        mainContentContainer.setVisibility(View.VISIBLE);

        updateUserInterface();
        loadPlacesData();
    }

    private void updateUserInterface() {
        if (isLoggedIn && currentUser != null) {
            // Mostrar información del usuario
            if (userWelcomeTextView != null) {
                String welcome = "Hola, " + currentUser.getFullName();
                userWelcomeTextView.setText(welcome);
                userWelcomeTextView.setVisibility(View.VISIBLE);
                Log.d(TAG, "UI actualizada para usuario: " + currentUser.getFullName());
            }

            // Cambiar icono de perfil para mostrar que está logueado
            profileIconImageView.setImageResource(R.drawable.ic_account_circle_filled);
        } else {
            // Usuario visitante
            if (userWelcomeTextView != null) {
                userWelcomeTextView.setText("Modo Visitante");
                userWelcomeTextView.setVisibility(View.VISIBLE);
            }

            profileIconImageView.setImageResource(R.drawable.ic_account_circle);
            Log.d(TAG, "UI actualizada para modo visitante");
        }
    }

    private void loadPlacesData() {
        Log.d(TAG, "Iniciando carga de lugares");
        showLoading(true);

        // Verificar conectividad primero
        placesRepository.checkHealth()
                .thenAccept(isHealthy -> {
                    Log.d(TAG, "Health check resultado: " + isHealthy);
                    if (isHealthy) {
                        // Cargar datos del backend
                        loadPlacesFromBackend();
                    } else {
                        runOnUiThread(() -> {
                            showLoading(false);
                            Toast.makeText(this, "Conectividad limitada. Mostrando datos locales.", Toast.LENGTH_LONG).show();
                            // Cargar datos de muestra en lugar de mostrar error
                            loadPlacesFromBackend(); // El repository manejará el fallback automáticamente
                        });
                    }
                })
                .exceptionally(throwable -> {
                    Log.e(TAG, "Error en health check", throwable);
                    runOnUiThread(() -> {
                        showLoading(false);
                        loadPlacesFromBackend(); // Intentar cargar de todas formas
                    });
                    return null;
                });
    }

    private void loadPlacesFromBackend() {
        Log.d(TAG, "Cargando lugares desde backend/cache");

        placesRepository.getAllPlaces()
                .thenAccept(places -> {
                    runOnUiThread(() -> {
                        showLoading(false);

                        Log.d(TAG, "Lugares recibidos: " + (places != null ? places.size() : 0));

                        if (places != null && !places.isEmpty()) {
                            placesList.clear();
                            placesList.addAll(places);

                            filteredPlacesList.clear();
                            filteredPlacesList.addAll(places);

                            placesAdapter.notifyDataSetChanged();
                            showContentWithData();

                            Log.i(TAG, "Lugares cargados exitosamente: " + places.size());
                            Toast.makeText(this, "Lugares cargados: " + places.size(), Toast.LENGTH_SHORT).show();
                        } else {
                            Log.w(TAG, "No se encontraron lugares");
                            showEmptyState();
                        }
                    });
                })
                .exceptionally(throwable -> {
                    runOnUiThread(() -> {
                        showLoading(false);
                        Log.e(TAG, "Error cargando lugares", throwable);
                        Toast.makeText(this, "Error cargando lugares", Toast.LENGTH_SHORT).show();
                        showEmptyState();
                    });
                    return null;
                });
    }

    private void filterPlaces(String query) {
        Log.d(TAG, "Filtrando lugares con query: '" + query + "'");

        if (query.trim().isEmpty()) {
            // Mostrar todos los lugares
            filteredPlacesList.clear();
            filteredPlacesList.addAll(placesList);
            Log.d(TAG, "Mostrando todos los lugares: " + filteredPlacesList.size());
        } else {
            // Filtrar localmente primero para respuesta inmediata
            filteredPlacesList.clear();
            String queryLower = query.toLowerCase();

            for (Place place : placesList) {
                if (place.getName().toLowerCase().contains(queryLower) ||
                        place.getCategory().toLowerCase().contains(queryLower) ||
                        (place.getDescription() != null && place.getDescription().toLowerCase().contains(queryLower))) {
                    filteredPlacesList.add(place);
                }
            }

            Log.d(TAG, "Filtro local encontró: " + filteredPlacesList.size() + " lugares");

            // También buscar en el servidor para resultados más completos
            searchInBackend(query);
        }

        placesAdapter.notifyDataSetChanged();

        // Mostrar/ocultar empty state
        if (filteredPlacesList.isEmpty()) {
            showEmptyState();
        } else {
            showContentWithData();
        }
    }

    private void searchInBackend(String query) {
        Log.d(TAG, "Buscando en backend: " + query);

        placesRepository.searchPlaces(query)
                .thenAccept(searchResults -> {
                    runOnUiThread(() -> {
                        if (searchResults != null && !searchResults.isEmpty()) {
                            Log.d(TAG, "Búsqueda en backend encontró: " + searchResults.size() + " lugares");

                            // Combinar resultados locales y del servidor (evitar duplicados)
                            for (Place place : searchResults) {
                                boolean exists = false;
                                for (Place existing : filteredPlacesList) {
                                    if (existing.getId().equals(place.getId())) {
                                        exists = true;
                                        break;
                                    }
                                }
                                if (!exists) {
                                    filteredPlacesList.add(place);
                                }
                            }
                            placesAdapter.notifyDataSetChanged();

                            if (!filteredPlacesList.isEmpty()) {
                                showContentWithData();
                            }
                        } else {
                            Log.d(TAG, "Búsqueda en backend no encontró resultados");
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Log.w(TAG, "Error en búsqueda del servidor", throwable);
                    // Error en búsqueda del servidor, pero no afecta la búsqueda local
                    return null;
                });
    }

    private void handleFavoriteClick(Place place) {
        if (!isLoggedIn) {
            Toast.makeText(this, "Inicia sesión para agregar favoritos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Toggle favorito
        place.setFavorite(!place.isFavorite());
        placesAdapter.notifyDataSetChanged();

        String message = place.isFavorite() ? "Agregado a favoritos" : "Removido de favoritos";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.d(TAG, message + ": " + place.getName());

        // TODO: Implementar llamada al backend para guardar/quitar favorito
        // favoritesRepository.toggleFavorite(place.getId());
    }

    private void handleProfileClick() {
        if (isLoggedIn) {
            // Mostrar opciones de usuario logueado
            showUserProfileOptions();
        } else {
            // Ir a AuthActivity
            Intent intent = new Intent(this, AuthActivity.class);
            startActivity(intent);
        }
    }

    private void showUserProfileOptions() {
        // Por ahora solo mostrar opción de logout
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Perfil de Usuario")
                .setMessage("Usuario: " + (currentUser != null ? currentUser.getFullName() : "Desconocido") +
                        "\nTipo: " + (currentUser != null ? currentUser.getUserType() : "Desconocido"))
                .setPositiveButton("Cerrar Sesión", (dialog, which) -> logout())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void logout() {
        authRepository.logout();
        isLoggedIn = false;
        currentUser = null;
        updateUserInterface();
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Usuario deslogueado");
    }

    private void toggleSearchBar() {
        if (searchBarContainer.getVisibility() == View.VISIBLE) {
            // Ocultar barra de búsqueda
            searchBarContainer.setVisibility(View.GONE);
            searchEditText.setText("");
            searchEditText.clearFocus();
            Log.d(TAG, "Barra de búsqueda ocultada");
        } else {
            // Mostrar barra de búsqueda
            searchBarContainer.setVisibility(View.VISIBLE);
            searchEditText.requestFocus();
            Log.d(TAG, "Barra de búsqueda mostrada");

            // Mostrar teclado
            searchEditText.postDelayed(() -> {
                android.view.inputmethod.InputMethodManager imm =
                        (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(searchEditText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                }
            }, 100);
        }
    }

    private void showLoading(boolean show) {
        loadingProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        placesRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        emptyStateContainer.setVisibility(View.GONE);
        Log.d(TAG, "Loading state: " + show);
    }

    private void showContentWithData() {
        loadingProgressBar.setVisibility(View.GONE);
        emptyStateContainer.setVisibility(View.GONE);
        placesRecyclerView.setVisibility(View.VISIBLE);
        Log.d(TAG, "Mostrando contenido con datos");
    }

    private void showEmptyState() {
        loadingProgressBar.setVisibility(View.GONE);
        placesRecyclerView.setVisibility(View.GONE);
        emptyStateContainer.setVisibility(View.VISIBLE);
        Log.d(TAG, "Mostrando estado vacío");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume - Verificando estado de autenticación");
        // Verificar si el estado de autenticación cambió
        checkAuthenticationStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "UpdatedMainActivity destruida");
    }
}