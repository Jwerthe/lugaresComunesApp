package com.example.lugarescomunes;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.button.MaterialButton;

public class PlaceDetailActivity extends AppCompatActivity {

    // Constantes
    public static final String EXTRA_PLACE_ID = "place_id";
    public static final String EXTRA_PLACE_NAME = "place_name";
    public static final String EXTRA_PLACE_CATEGORY = "place_category";
    public static final String EXTRA_PLACE_DESCRIPTION = "place_description";
    public static final String EXTRA_PLACE_WHAT3WORDS = "place_what3words";
    public static final String EXTRA_PLACE_IS_AVAILABLE = "place_is_available";
    public static final String EXTRA_PLACE_DISTANCE = "place_distance";
    public static final String EXTRA_PLACE_TYPE = "place_type";
    public static final String EXTRA_PLACE_IS_FAVORITE = "place_is_favorite";
    public static final String EXTRA_PLACE_CAPACITY = "place_capacity";
    public static final String EXTRA_PLACE_SCHEDULE = "place_schedule";

    // Views
    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;
    private ImageView placeImageView;
    private ImageView placeTypeIcon;
    private TextView placeCategoryTextView;
    private TextView placeNameTextView;
    private ImageView favoriteButton;
    private View availabilityIndicator;
    private TextView availabilityTextView;
    private TextView distanceTextView;
    private TextView placeDescriptionTextView;
    private TextView what3wordsTextView;
    private TextView capacityTextView;
    private TextView scheduleTextView;
    private ImageView copyWhat3wordsButton;
    private MaterialButton navigateButton;
    private MaterialButton shareButton;
    private MaterialButton reportButton;

    // Datos del lugar
    private Place place;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);

        initializeViews();
        setupToolbar();
        loadPlaceData();
        setupClickListeners();
    }

    private void initializeViews() {
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        toolbar = findViewById(R.id.toolbar);
        placeImageView = findViewById(R.id.placeImageView);
        placeTypeIcon = findViewById(R.id.placeTypeIcon);
        placeCategoryTextView = findViewById(R.id.placeCategoryTextView);
        placeNameTextView = findViewById(R.id.placeNameTextView);
        favoriteButton = findViewById(R.id.favoriteButton);
        availabilityIndicator = findViewById(R.id.availabilityIndicator);
        availabilityTextView = findViewById(R.id.availabilityTextView);
        distanceTextView = findViewById(R.id.distanceTextView);
        placeDescriptionTextView = findViewById(R.id.placeDescriptionTextView);
        what3wordsTextView = findViewById(R.id.what3wordsTextView);
        capacityTextView = findViewById(R.id.capacityTextView);
        scheduleTextView = findViewById(R.id.scheduleTextView);
        copyWhat3wordsButton = findViewById(R.id.copyWhat3wordsButton);
        navigateButton = findViewById(R.id.navigateButton);
        shareButton = findViewById(R.id.shareButton);
        reportButton = findViewById(R.id.reportButton);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void loadPlaceData() {
        // Obtener datos del intent
        Intent intent = getIntent();

        // Crear objeto Place con los datos recibidos
        place = new Place();
        place.setId(intent.getStringExtra(EXTRA_PLACE_ID));
        place.setName(intent.getStringExtra(EXTRA_PLACE_NAME));
        place.setCategory(intent.getStringExtra(EXTRA_PLACE_CATEGORY));
        place.setDescription(intent.getStringExtra(EXTRA_PLACE_DESCRIPTION));
        place.setWhat3words(intent.getStringExtra(EXTRA_PLACE_WHAT3WORDS));
        place.setAvailable(intent.getBooleanExtra(EXTRA_PLACE_IS_AVAILABLE, true));
        place.setDistanceInMeters(intent.getIntExtra(EXTRA_PLACE_DISTANCE, 0));
        place.setFavorite(intent.getBooleanExtra(EXTRA_PLACE_IS_FAVORITE, false));
        place.setCapacity(intent.getIntExtra(EXTRA_PLACE_CAPACITY, 0));
        place.setSchedule(intent.getStringExtra(EXTRA_PLACE_SCHEDULE));

        // Obtener tipo de lugar
        String typeString = intent.getStringExtra(EXTRA_PLACE_TYPE);
        if (typeString != null) {
            place.setType(PlaceType.valueOf(typeString));
        } else {
            place.setType(PlaceType.CLASSROOM);
        }

        // Mostrar datos en la UI
        displayPlaceData();
    }

    private void displayPlaceData() {
        // Título en la toolbar
        collapsingToolbar.setTitle(place.getName());

        // Información básica
        placeNameTextView.setText(place.getName());
        placeCategoryTextView.setText(place.getCategory());
        placeDescriptionTextView.setText(place.getDescription());
        what3wordsTextView.setText(place.getWhat3words());
        distanceTextView.setText(place.getFormattedDistance());

        // Capacidad y horario
        if (place.getCapacity() > 0) {
            capacityTextView.setText(place.getCapacity() + " personas");
        } else {
            capacityTextView.setText("No especificada");
        }

        if (place.getSchedule() != null && !place.getSchedule().isEmpty()) {
            scheduleTextView.setText(place.getSchedule());
        } else {
            scheduleTextView.setText("Lunes a Viernes 7:00 - 22:00");
        }

        // Icono del tipo de lugar
        placeTypeIcon.setImageResource(getIconForPlaceType(place.getType()));
        placeTypeIcon.setBackgroundResource(getBackgroundColorForPlaceType(place.getType()));

        // Background de la categoría
        placeCategoryTextView.setBackgroundResource(getCategoryBackgroundForPlaceType(place.getType()));
        placeCategoryTextView.setTextColor(getResources().getColor(getCategoryTextColorForPlaceType(place.getType())));

        // Estado de disponibilidad
        availabilityTextView.setText(place.getAvailabilityText());
        availabilityIndicator.setBackgroundResource(
                place.isAvailable() ? R.drawable.circle_background_green : R.drawable.circle_background_red
        );
        availabilityTextView.setTextColor(getResources().getColor(
                place.isAvailable() ? android.R.color.holo_green_light : android.R.color.holo_red_light
        ));

        // Estado de favorito
        updateFavoriteButton();

        // TODO: Cargar imagen real cuando tengamos la URL
        // Por ahora usamos un placeholder
        // Glide.with(this).load(place.getImageUrl()).into(placeImageView);
    }

    private void setupClickListeners() {
        // Botón de favoritos
        favoriteButton.setOnClickListener(v -> toggleFavorite());

        // Copiar código what3words
        copyWhat3wordsButton.setOnClickListener(v -> copyWhat3wordsToClipboard());

        // Botón de navegación
        navigateButton.setOnClickListener(v -> navigateToPlace());

        // Botón de compartir
        shareButton.setOnClickListener(v -> sharePlace());

        // Botón de reportar
        reportButton.setOnClickListener(v -> reportPlace());

        // Click en código what3words para copiar
        what3wordsTextView.setOnClickListener(v -> copyWhat3wordsToClipboard());
    }

    private void toggleFavorite() {
        place.setFavorite(!place.isFavorite());
        updateFavoriteButton();

        String message = place.isFavorite() ?
                "Agregado a favoritos" : "Removido de favoritos";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        // TODO: Actualizar en la base de datos cuando esté implementada
    }

    private void updateFavoriteButton() {
        int iconRes = place.isFavorite() ?
                R.drawable.ic_favorite : R.drawable.ic_favorite_border;
        favoriteButton.setImageResource(iconRes);
    }

    private void copyWhat3wordsToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("what3words", place.getWhat3words());
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, "Código copiado: " + place.getWhat3words(), Toast.LENGTH_SHORT).show();
    }

    private void navigateToPlace() {
        // TODO: Implementar navegación real con mapas
        Toast.makeText(this, "Abriendo navegación a " + place.getName(), Toast.LENGTH_SHORT).show();

        // Por ahora simulamos la funcionalidad
        // En el futuro aquí abriremos la pantalla de mapas o una app externa
    }

    private void sharePlace() {
        String shareText = String.format(
                "📍 %s\n" +
                        "📂 %s\n" +
                        "📝 %s\n" +
                        "🎯 %s\n\n" +
                        "Compartido desde Lugares Comunes - PUCE",
                place.getName(),
                place.getCategory(),
                place.getDescription(),
                place.getWhat3words()
        );

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Lugar: " + place.getName());

        startActivity(Intent.createChooser(shareIntent, "Compartir lugar"));
    }

    private void reportPlace() {
        // TODO: Implementar sistema de reportes
        Toast.makeText(this, "Función de reportes próximamente", Toast.LENGTH_SHORT).show();
    }

    // Métodos auxiliares para iconos y colores
    private int getIconForPlaceType(PlaceType type) {
        switch (type) {
            case CLASSROOM: return R.drawable.ic_classroom;
            case LABORATORY: return R.drawable.ic_laboratory;
            case LIBRARY: return R.drawable.ic_library;
            case CAFETERIA: return R.drawable.ic_cafeteria;
            case OFFICE: return R.drawable.ic_office;
            case AUDITORIUM: return R.drawable.ic_auditorium;
            case SERVICE: return R.drawable.ic_service;
            default: return R.drawable.ic_classroom;
        }
    }

    private int getBackgroundColorForPlaceType(PlaceType type) {
        switch (type) {
            case CLASSROOM: return R.drawable.circle_background_blue;
            case LABORATORY: return R.drawable.circle_background_orange;
            case LIBRARY: return R.drawable.circle_background_purple;
            case CAFETERIA: return R.drawable.circle_background_green;
            case OFFICE: return R.drawable.circle_background_red;
            case AUDITORIUM: return R.drawable.circle_background_gray;
            case SERVICE: return R.drawable.circle_background_blue;
            default: return R.drawable.circle_background_blue;
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

    private int getCategoryTextColorForPlaceType(PlaceType type) {
        switch (type) {
            case CLASSROOM: return android.R.color.holo_blue_dark;
            case LABORATORY: return android.R.color.holo_orange_dark;
            case LIBRARY: return android.R.color.holo_purple;
            case CAFETERIA: return android.R.color.holo_green_dark;
            case OFFICE: return android.R.color.holo_red_dark;
            case AUDITORIUM: return android.R.color.darker_gray;
            case SERVICE: return android.R.color.holo_blue_dark;
            default: return android.R.color.holo_green_dark;
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

    // Método estático para crear el intent con todos los datos
    public static Intent createIntent(Context context, Place place) {
        Intent intent = new Intent(context, PlaceDetailActivity.class);
        intent.putExtra(EXTRA_PLACE_ID, place.getId());
        intent.putExtra(EXTRA_PLACE_NAME, place.getName());
        intent.putExtra(EXTRA_PLACE_CATEGORY, place.getCategory());
        intent.putExtra(EXTRA_PLACE_DESCRIPTION, place.getDescription());
        intent.putExtra(EXTRA_PLACE_WHAT3WORDS, place.getWhat3words());
        intent.putExtra(EXTRA_PLACE_IS_AVAILABLE, place.isAvailable());
        intent.putExtra(EXTRA_PLACE_DISTANCE, place.getDistanceInMeters());
        intent.putExtra(EXTRA_PLACE_TYPE, place.getType().name());
        intent.putExtra(EXTRA_PLACE_IS_FAVORITE, place.isFavorite());
        intent.putExtra(EXTRA_PLACE_CAPACITY, place.getCapacity());
        intent.putExtra(EXTRA_PLACE_SCHEDULE, place.getSchedule());
        return intent;
    }
}