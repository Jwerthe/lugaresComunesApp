package com.example.lugarescomunes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.PlaceViewHolder> {

    private List<Place> places;
    private Context context;
    private OnPlaceClickListener listener;

    // Interface para manejar clicks en los lugares
    public interface OnPlaceClickListener {
        void onPlaceClick(Place place);
        void onFavoriteClick(Place place);
        void onNavigateClick(Place place);
    }

    public PlacesAdapter(List<Place> places, Context context) {
        this.places = places;
        this.context = context;
    }

    public void setOnPlaceClickListener(OnPlaceClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_place_card, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Place place = places.get(position);
        holder.bind(place);
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    public class PlaceViewHolder extends RecyclerView.ViewHolder {

        private ImageView placeTypeIcon;
        private TextView placeNameTextView;
        private TextView placeCategoryTextView;
        private ImageView favoriteIcon;
        private TextView placeDescriptionTextView;
        private TextView what3wordsTextView;
        private LinearLayout availabilityContainer;
        private View availabilityIndicator;
        private TextView availabilityTextView;
        private LinearLayout distanceContainer;
        private TextView distanceTextView;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);

            // Inicializar views
            placeTypeIcon = itemView.findViewById(R.id.placeTypeIcon);
            placeNameTextView = itemView.findViewById(R.id.placeNameTextView);
            placeCategoryTextView = itemView.findViewById(R.id.placeCategoryTextView);
            favoriteIcon = itemView.findViewById(R.id.favoriteIcon);
            placeDescriptionTextView = itemView.findViewById(R.id.placeDescriptionTextView);
            what3wordsTextView = itemView.findViewById(R.id.what3wordsTextView);
            availabilityContainer = itemView.findViewById(R.id.availabilityContainer);
            availabilityIndicator = itemView.findViewById(R.id.availabilityIndicator);
            availabilityTextView = itemView.findViewById(R.id.availabilityTextView);
            distanceContainer = itemView.findViewById(R.id.distanceContainer);
            distanceTextView = itemView.findViewById(R.id.distanceTextView);
        }

        public void bind(Place place) {
            // Configurar información básica
            placeNameTextView.setText(place.getName());
            placeCategoryTextView.setText(place.getCategory());
            placeDescriptionTextView.setText(place.getDescription());
            what3wordsTextView.setText(place.getWhat3words());
            distanceTextView.setText(place.getFormattedDistance());

            // Configurar icono del tipo de lugar
            placeTypeIcon.setImageResource(getIconForPlaceType(place.getType()));

            // Configurar background del icono según el tipo
            int backgroundColor = getBackgroundColorForPlaceType(place.getType());
            placeTypeIcon.setBackgroundResource(backgroundColor);

            // Configurar disponibilidad
            availabilityTextView.setText(place.getAvailabilityText());
            int availabilityColor = place.isAvailable() ?
                    android.R.color.holo_green_light : android.R.color.holo_red_light;
            availabilityIndicator.setBackgroundResource(
                    place.isAvailable() ? R.drawable.circle_background_green : R.drawable.circle_background_red
            );
            availabilityTextView.setTextColor(context.getResources().getColor(availabilityColor));

            // Configurar icono de favorito
            int favoriteIconRes = place.isFavorite() ?
                    R.drawable.ic_favorite : R.drawable.ic_favorite_border;
            favoriteIcon.setImageResource(favoriteIconRes);

            // Configurar color de la categoría
            int categoryBackgroundColor = getCategoryBackgroundForPlaceType(place.getType());
            placeCategoryTextView.setBackgroundResource(categoryBackgroundColor);

            // Click listeners
            setupClickListeners(place);
        }

        private void setupClickListeners(Place place) {
            // Click en toda la card
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPlaceClick(place);
                } else {
                    // Comportamiento por defecto
                    Toast.makeText(context,
                            "Navegando a " + place.getName(),
                            Toast.LENGTH_SHORT).show();
                }
            });

            // Click en favorito
            favoriteIcon.setOnClickListener(v -> {
                place.setFavorite(!place.isFavorite());

                // Actualizar icono
                int favoriteIconRes = place.isFavorite() ?
                        R.drawable.ic_favorite : R.drawable.ic_favorite_border;
                favoriteIcon.setImageResource(favoriteIconRes);

                // Mostrar feedback
                String message = place.isFavorite() ?
                        "Agregado a favoritos" : "Removido de favoritos";
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

                if (listener != null) {
                    listener.onFavoriteClick(place);
                }
            });

            // Click en código what3words
            what3wordsTextView.setOnClickListener(v -> {
                // Copiar código al clipboard
                android.content.ClipboardManager clipboard =
                        (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText(
                        "what3words", place.getWhat3words());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(context,
                        "Código copiado: " + place.getWhat3words(),
                        Toast.LENGTH_SHORT).show();
            });

            // Long click para más opciones
            itemView.setOnLongClickListener(v -> {
                showPlaceOptions(place);
                return true;
            });
        }

        private void showPlaceOptions(Place place) {
            // TODO: Implementar menú de opciones contextual
            // Por ahora solo mostramos información adicional
            String info = String.format(
                    "Lugar: %s\nTipo: %s\nDistancia: %s\nDisponible: %s\nCódigo w3w: %s",
                    place.getName(),
                    place.getCategory(),
                    place.getFormattedDistance(),
                    place.getAvailabilityText(),
                    place.getWhat3words()
            );

            new android.app.AlertDialog.Builder(context)
                    .setTitle("Información del lugar")
                    .setMessage(info)
                    .setPositiveButton("Cerrar", null)
                    .setNeutralButton("Navegar", (dialog, which) -> {
                        if (listener != null) {
                            listener.onNavigateClick(place);
                        }
                    })
                    .show();
        }

        private int getIconForPlaceType(PlaceType type) {
            switch (type) {
                case CLASSROOM:
                    return R.drawable.ic_classroom;
                case LABORATORY:
                    return R.drawable.ic_laboratory;
                case LIBRARY:
                    return R.drawable.ic_library;
                case CAFETERIA:
                    return R.drawable.ic_cafeteria;
                case OFFICE:
                    return R.drawable.ic_office;
                case AUDITORIUM:
                    return R.drawable.ic_auditorium;
                case SERVICE:
                    return R.drawable.ic_service;
                default:
                    return R.drawable.ic_classroom;
            }
        }

        private int getBackgroundColorForPlaceType(PlaceType type) {
            switch (type) {
                case CLASSROOM:
                    return R.drawable.circle_background_blue;
                case LABORATORY:
                    return R.drawable.circle_background_orange;
                case LIBRARY:
                    return R.drawable.circle_background_purple;
                case CAFETERIA:
                    return R.drawable.circle_background_green;
                case OFFICE:
                    return R.drawable.circle_background_red;
                case AUDITORIUM:
                    return R.drawable.circle_background_gray;
                case SERVICE:
                    return R.drawable.circle_background_blue;
                default:
                    return R.drawable.circle_background_blue;
            }
        }

        private int getCategoryBackgroundForPlaceType(PlaceType type) {
            switch (type) {
                case CLASSROOM:
                    return R.drawable.category_tag_background_blue;
                case LABORATORY:
                    return R.drawable.category_tag_background_orange;
                case LIBRARY:
                    return R.drawable.category_tag_background_purple;
                case CAFETERIA:
                    return R.drawable.category_tag_background_green;
                case OFFICE:
                    return R.drawable.category_tag_background_red;
                case AUDITORIUM:
                    return R.drawable.category_tag_background_gray;
                case SERVICE:
                    return R.drawable.category_tag_background_blue;
                default:
                    return R.drawable.category_tag_background;
            }
        }
    }

    // Métodos para actualizar datos
    public void updatePlaces(List<Place> newPlaces) {
        this.places.clear();
        this.places.addAll(newPlaces);
        notifyDataSetChanged();
    }

    public void addPlace(Place place) {
        places.add(place);
        notifyItemInserted(places.size() - 1);
    }

    public void removePlace(int position) {
        if (position >= 0 && position < places.size()) {
            places.remove(position);
            notifyItemRemoved(position);
        }
    }

    public Place getPlaceAt(int position) {
        if (position >= 0 && position < places.size()) {
            return places.get(position);
        }
        return null;
    }
}