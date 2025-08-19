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

import com.example.lugarescomunes.repository.PlacesRepository;
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
    private PlacesRepository placesRepository;
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
        placesRepository = PlacesRepository.getInstance();
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

        // ✅ NUEVA IMPLEMENTACIÓN: Configurar listener para navegar a RoutesActivity
        placesAdapter.setOnPlaceClickListener(new PlacesAdapter.OnPlaceClickListener() {
            @Override
            public void onPlaceClick(Place place) {
                Log.d(TAG, "Click en lugar: " + place.getName() + " (ID: " + place.getId() + ")");

                // 🎯 NAVEGAR A ROUTES ACTIVITY EN LUGAR DE PLACE DETAIL
                Intent intent = new Intent(UpdatedMainActivity.this, RoutesActivity.class);
                intent.putExtra(RoutesActivity.EXTRA_DESTINATION_ID, place.getId());
                intent.putExtra(RoutesActivity.EXTRA_DESTINATION_NAME, place.getName());
                startActivity(intent);
            }

            @Override
            public void onFavoriteClick(Place place) {
                handleFavoriteClick(place);
            }

            @Override
            public void onNavigateClick(Place place) {
                Log.d(TAG, "Navegación directa a lugar: " + place.getName());

                // También navegar a rutas para navegación directa
                Intent intent = new Intent(UpdatedMainActivity.this, RoutesActivity.class);
                intent.putExtra(RoutesActivity.EXTRA_DESTINATION_ID, place.getId());
                intent.putExtra(RoutesActivity.EXTRA_DESTINATION_NAME, place.getName());
                startActivity(intent);
            }
        });

        Log.d(TAG, "RecyclerView configurado correctamente");
    }

    private void handleFavoriteClick(Place place) {
        place.setFavorite(!place.isFavorite());

        String message = place.isFavorite() ?
                "📍 " + place.getName() + " agregado a favoritos" :
                "💔 " + place.getName() + " removido de favoritos";

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        // Actualizar adapter para reflejar el cambio
        placesAdapter.notifyDataSetChanged();

        // TODO: Aquí se podría guardar en el servidor si el usuario está logueado
        if (isLoggedIn) {
            Log.d(TAG, "TODO: Guardar favorito en servidor para usuario: " + currentUser.getEmail());
        }
    }

    private void setupClickListeners() {
        // Click en icono de búsqueda
        searchIconImageView.setOnClickListener(v -> toggleSearchBar());

        // Click en icono de mapa
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
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                filterPlaces(query);
            }
        });

        Log.d(TAG, "Funcionalidad de búsqueda configurada");
    }

    private void showSplashScreen() {
        // Mostrar splash
        splashContainer.setVisibility(View.VISIBLE);
        mainContentContainer.setVisibility(View.GONE);

        // Después del tiempo del splash, mostrar contenido principal
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            splashContainer.setVisibility(View.GONE);
            mainContentContainer.setVisibility(View.VISIBLE);

            // Cargar datos después de mostrar el contenido
            loadPlacesWithHealthCheck();
        }, SPLASH_DURATION);

        Log.d(TAG, "Splash screen mostrado por " + SPLASH_DURATION + "ms");
    }

    private void updateUserInterface() {
        if (userWelcomeTextView != null) {
            if (currentUser != null) {
                String welcomeText = "¡Hola, " + currentUser.getFullName() + "! 👋";
                userWelcomeTextView.setText(welcomeText);
                userWelcomeTextView.setVisibility(View.VISIBLE);
            } else if (isLoggedIn) {
                // Usuario logueado pero sin datos detallados
                userWelcomeTextView.setText("¡Bienvenido! 👋");
                userWelcomeTextView.setVisibility(View.VISIBLE);
            } else {
                // Usuario visitante
                userWelcomeTextView.setText("👋 Modo visitante - Explora lugares y rutas");
                userWelcomeTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void handleProfileClick() {
        if (isLoggedIn) {
            Log.d(TAG, "Usuario logueado, mostrar opciones de perfil");

            // Crear diálogo con opciones
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("Perfil de " + currentUser.getFullName())
                    .setMessage("¿Qué deseas hacer?")
                    .setPositiveButton("Ver Perfil", (dialog, which) -> {
                        Toast.makeText(this, "Perfil de " + currentUser.getFullName(), Toast.LENGTH_SHORT).show();
                        // TODO: Navegar a ProfileActivity cuando esté implementada
                    })
                    .setNegativeButton("Cerrar Sesión", (dialog, which) -> {
                        handleLogout();
                    })
                    .setNeutralButton("Cancelar", null)
                    .show();
        } else {
            Log.d(TAG, "Usuario no logueado, mostrar login");
            Intent intent = new Intent(this, AuthActivity.class);
            startActivity(intent);
        }
    }

    private void handleLogout() {
        Log.d(TAG, "Cerrando sesión del usuario");

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro que deseas cerrar sesión?")
                .setPositiveButton("Sí, cerrar sesión", (dialog, which) -> {
                    // Limpiar datos de autenticación
                    authRepository.logout();

                    // Actualizar estado local
                    isLoggedIn = false;
                    currentUser = null;

                    // Actualizar UI
                    updateUserInterface();

                    // Mostrar mensaje de confirmación
                    Toast.makeText(this, "👋 Sesión cerrada exitosamente", Toast.LENGTH_SHORT).show();

                    Log.i(TAG, "Sesión cerrada exitosamente");
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void toggleSearchBar() {
        boolean isVisible = searchBarContainer.getVisibility() == View.VISIBLE;

        if (!isVisible) {
            searchBarContainer.setVisibility(View.VISIBLE);
            searchEditText.requestFocus();

            // Mostrar teclado
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.showSoftInput(searchEditText,
                    android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
        } else {
            searchBarContainer.setVisibility(View.GONE);
            searchEditText.setText("");

            // Ocultar teclado
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
        }
    }

    private void loadPlacesWithHealthCheck() {
        showLoading(true);

        Log.d(TAG, "Iniciando health check de API");

        // Verificar salud de la API primero
        placesRepository.checkApiHealth()
                .thenAccept(healthy -> {
                    if (healthy) {
                        Log.i(TAG, "API disponible, cargando destinos");
                        runOnUiThread(() -> {
                            loadPlacesFromBackend();
                        });
                    } else {
                        runOnUiThread(() -> {
                            Log.w(TAG, "API no disponible");
                            showLoading(false);
                            Toast.makeText(this, "⚠️ Servicio temporalmente no disponible. " +
                                    "Verifica tu conexión a internet.", Toast.LENGTH_LONG).show();
                            showEmptyState();
                        });
                    }
                })
                .exceptionally(throwable -> {
                    Log.e(TAG, "Error en health check", throwable);
                    runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(this, "❌ Error de conexión. Verifica tu internet.", Toast.LENGTH_LONG).show();
                        showEmptyState();
                    });
                    return null;
                });
    }

    private void loadPlacesFromBackend() {
        Log.d(TAG, "Cargando destinos desde /routes/destinations");

        placesRepository.getAllPlaces()
                .thenAccept(places -> {
                    runOnUiThread(() -> {
                        showLoading(false);

                        Log.d(TAG, "Destinos recibidos: " + (places != null ? places.size() : 0));

                        if (places != null && !places.isEmpty()) {
                            placesList.clear();
                            placesList.addAll(places);

                            filteredPlacesList.clear();
                            filteredPlacesList.addAll(places);

                            placesAdapter.notifyDataSetChanged();
                            showContentWithData();

                            Log.i(TAG, "Destinos cargados exitosamente: " + places.size());
                            Toast.makeText(this, "🎯 " + places.size() + " destinos disponibles", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.w(TAG, "No se encontraron destinos");
                            showEmptyState();
                        }
                    });
                })
                .exceptionally(throwable -> {
                    runOnUiThread(() -> {
                        showLoading(false);
                        Log.e(TAG, "Error cargando destinos", throwable);

                        String errorMessage = "Error cargando destinos";
                        if (throwable.getMessage() != null) {
                            errorMessage += ": " + throwable.getMessage();
                        }

                        Toast.makeText(this, "❌ " + errorMessage, Toast.LENGTH_LONG).show();
                        showEmptyState();
                    });
                    return null;
                });
    }

    private void filterPlaces(String query) {
        Log.d(TAG, "Filtrando destinos con query: '" + query + "'");

        if (query.trim().isEmpty()) {
            // Mostrar todos los lugares
            filteredPlacesList.clear();
            filteredPlacesList.addAll(placesList);
            Log.d(TAG, "Mostrando todos los destinos: " + filteredPlacesList.size());
        } else {
            // Usar repositorio para búsqueda
            placesRepository.searchPlaces(query)
                    .thenAccept(searchResults -> {
                        runOnUiThread(() -> {
                            filteredPlacesList.clear();
                            filteredPlacesList.addAll(searchResults);
                            placesAdapter.notifyDataSetChanged();

                            Log.d(TAG, "Resultados de búsqueda: " + searchResults.size());

                            if (searchResults.isEmpty()) {
                                Toast.makeText(this, "No se encontraron destinos para '" + query + "'", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .exceptionally(throwable -> {
                        runOnUiThread(() -> {
                            Log.e(TAG, "Error en búsqueda", throwable);
                            Toast.makeText(this, "Error en búsqueda", Toast.LENGTH_SHORT).show();
                        });
                        return null;
                    });
            return;
        }

        placesAdapter.notifyDataSetChanged();
        updateContentVisibility();
    }

    private void showLoading(boolean show) {
        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (placesRecyclerView != null) {
            placesRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
        if (emptyStateContainer != null) {
            emptyStateContainer.setVisibility(View.GONE);
        }
    }

    private void showContentWithData() {
        if (placesRecyclerView != null) {
            placesRecyclerView.setVisibility(View.VISIBLE);
        }
        if (emptyStateContainer != null) {
            emptyStateContainer.setVisibility(View.GONE);
        }
        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(View.GONE);
        }
    }

    private void showEmptyState() {
        if (emptyStateContainer != null) {
            emptyStateContainer.setVisibility(View.VISIBLE);
        }
        if (placesRecyclerView != null) {
            placesRecyclerView.setVisibility(View.GONE);
        }
        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(View.GONE);
        }
    }

    private void updateContentVisibility() {
        if (filteredPlacesList.isEmpty()) {
            showEmptyState();
        } else {
            showContentWithData();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "UpdatedMainActivity resumed");

        // Verificar si el estado de autenticación cambió
        boolean currentLoginState = authRepository.isLoggedIn();
        if (currentLoginState != isLoggedIn) {
            isLoggedIn = currentLoginState;
            checkAuthenticationStatus();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "UpdatedMainActivity paused");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "UpdatedMainActivity destroyed");
    }
}