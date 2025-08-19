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

import com.example.lugarescomunes.repository.NewPlacesRepository;
import com.example.lugarescomunes.repository.AuthRepository;
import com.example.lugarescomunes.models.api.UserResponse;

import java.util.ArrayList;
import java.util.List;

public class UpdatedMainActivity extends AppCompatActivity {

    private static final String TAG = "UpdatedMainActivity";
    private static final int SPLASH_DURATION = 2000; // 2 segundos

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
    private NewPlacesRepository placesRepository;
    private AuthRepository authRepository;

    // Estado del usuario
    private boolean isLoggedIn = false;
    private UserResponse currentUser = null;

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

        // Inicializar repositorios
        placesRepository = NewPlacesRepository.getInstance();
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
        if (isLoggedIn) {
            currentUser = authRepository.getCurrentUserSync();

            // Opcional: Actualizar datos del usuario desde el servidor
            authRepository.getCurrentUser()
                    .thenAccept(user -> {
                        if (user != null) {
                            currentUser = user;
                            runOnUiThread(this::updateUserInterface);
                        }
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

        // Crear TextView para saludo de usuario (agregar al layout si no existe)
        userWelcomeTextView = findViewById(R.id.userWelcomeTextView);
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
                Intent intent = PlaceDetailActivity.createIntent(UpdatedMainActivity.this, place);
                startActivity(intent);
            }

            @Override
            public void onFavoriteClick(Place place) {
                handleFavoriteClick(place);
            }

            @Override
            public void onNavigateClick(Place place) {
                // Navegar directamente al lugar
                Intent intent = PlaceDetailActivity.createIntent(UpdatedMainActivity.this, place);
                startActivity(intent);
            }
        });
    }

    private void setupClickListeners() {
        // Click en icono de búsqueda
        searchIconImageView.setOnClickListener(v -> toggleSearchBar());

        // Click en icono de mapa
        mapIconImageView.setOnClickListener(v -> {
            Intent intent = new Intent(UpdatedMainActivity.this, MapsActivity.class);
            startActivity(intent);
        });

        // Click en icono de perfil
        profileIconImageView.setOnClickListener(v -> handleProfileClick());

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

                // Filtrar lugares en tiempo real
                filterPlaces(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No necesitamos implementar esto
            }
        });
    }

    private void showSplashScreen() {
        splashContainer.setVisibility(View.VISIBLE);
        mainContentContainer.setVisibility(View.GONE);

        // Después del splash, decidir qué mostrar
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Si no está logueado y es la primera vez, mostrar AuthActivity
            if (!isLoggedIn && isFirstTime()) {
                showAuthActivity();
            } else {
                // Continuar con la app normal
                showMainContent();
            }
        }, SPLASH_DURATION);
    }

    private boolean isFirstTime() {
        // Puedes implementar lógica para detectar si es la primera vez
        // Por ahora, siempre mostrar AuthActivity si no está logueado
        return true;
    }

    private void showAuthActivity() {
        Intent intent = new Intent(this, AuthActivity.class);
        startActivity(intent);
        finish();
    }

    private void showMainContent() {
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
        }
    }

    private void loadPlacesData() {
        showLoading(true);

        // Verificar conectividad primero
        placesRepository.checkHealth()
                .thenAccept(isHealthy -> {
                    if (isHealthy) {
                        // Cargar datos del backend
                        loadPlacesFromBackend();
                    } else {
                        runOnUiThread(() -> {
                            showLoading(false);
                            Toast.makeText(this, "No se puede conectar al servidor. Verifique su conexión.", Toast.LENGTH_LONG).show();
                            showEmptyState();
                        });
                    }
                })
                .exceptionally(throwable -> {
                    runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(this, "Error verificando conexión", Toast.LENGTH_SHORT).show();
                        showEmptyState();
                    });
                    return null;
                });
    }

    private void loadPlacesFromBackend() {
        placesRepository.getAllPlaces()
                .thenAccept(places -> {
                    runOnUiThread(() -> {
                        showLoading(false);

                        if (places != null && !places.isEmpty()) {
                            placesList.clear();
                            placesList.addAll(places);

                            filteredPlacesList.clear();
                            filteredPlacesList.addAll(places);

                            placesAdapter.notifyDataSetChanged();
                            showContentWithData();

                            Toast.makeText(this, "Lugares cargados: " + places.size(), Toast.LENGTH_SHORT).show();
                        } else {
                            showEmptyState();
                        }
                    });
                })
                .exceptionally(throwable -> {
                    runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(this, "Error cargando lugares", Toast.LENGTH_SHORT).show();
                        showEmptyState();
                    });
                    return null;
                });
    }

    private void filterPlaces(String query) {
        if (query.trim().isEmpty()) {
            // Mostrar todos los lugares
            filteredPlacesList.clear();
            filteredPlacesList.addAll(placesList);
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

            // Opcional: También buscar en el servidor
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
        placesRepository.searchPlaces(query)
                .thenAccept(searchResults -> {
                    runOnUiThread(() -> {
                        if (searchResults != null && !searchResults.isEmpty()) {
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
                        }
                    });
                })
                .exceptionally(throwable -> {
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
    }

    private void toggleSearchBar() {
        if (searchBarContainer.getVisibility() == View.VISIBLE) {
            // Ocultar barra de búsqueda
            searchBarContainer.setVisibility(View.GONE);
            searchEditText.setText("");
            searchEditText.clearFocus();
        } else {
            // Mostrar barra de búsqueda
            searchBarContainer.setVisibility(View.VISIBLE);
            searchEditText.requestFocus();

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
    }

    private void showContentWithData() {
        loadingProgressBar.setVisibility(View.GONE);
        emptyStateContainer.setVisibility(View.GONE);
        placesRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showEmptyState() {
        loadingProgressBar.setVisibility(View.GONE);
        placesRecyclerView.setVisibility(View.GONE);
        emptyStateContainer.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Verificar si el estado de autenticación cambió
        checkAuthenticationStatus();
    }
}