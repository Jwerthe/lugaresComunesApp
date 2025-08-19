package com.example.lugarescomunes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lugarescomunes.models.api.RouteResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class RoutesAdapter extends RecyclerView.Adapter<RoutesAdapter.RouteViewHolder> {

    private List<RouteResponse> routes;
    private OnRouteClickListener listener;

    public interface OnRouteClickListener {
        void onRouteClick(RouteResponse route);
        void onNavigateClick(RouteResponse route);
    }

    public RoutesAdapter(List<RouteResponse> routes, OnRouteClickListener listener) {
        this.routes = routes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route, parent, false);
        return new RouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
        RouteResponse route = routes.get(position);
        holder.bind(route, listener);
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    static class RouteViewHolder extends RecyclerView.ViewHolder {

        // Views
        private MaterialCardView cardView;
        private ImageView routeIconImageView;
        private TextView routeNameTextView;
        private TextView routeDescriptionTextView;
        private TextView distanceTextView;
        private TextView timeTextView;
        private TextView difficultyTextView;
        private TextView ratingTextView;
        private ImageView ratingStarImageView;
        private MaterialButton navigateButton;

        public RouteViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.routeCardView);
            routeIconImageView = itemView.findViewById(R.id.routeIconImageView);
            routeNameTextView = itemView.findViewById(R.id.routeNameTextView);
            routeDescriptionTextView = itemView.findViewById(R.id.routeDescriptionTextView);
            distanceTextView = itemView.findViewById(R.id.distanceTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            difficultyTextView = itemView.findViewById(R.id.difficultyTextView);
            ratingTextView = itemView.findViewById(R.id.ratingTextView);
            ratingStarImageView = itemView.findViewById(R.id.ratingStarImageView);
            navigateButton = itemView.findViewById(R.id.navigateButton);
        }

        public void bind(RouteResponse route, OnRouteClickListener listener) {
            // Nombre de la ruta
            if (routeNameTextView != null) {
                routeNameTextView.setText(route.getName() != null ? route.getName() : "Ruta sin nombre");
            }

            // Descripción
            if (routeDescriptionTextView != null) {
                String description = route.getDescription();
                if (description != null && !description.trim().isEmpty()) {
                    routeDescriptionTextView.setText(description);
                    routeDescriptionTextView.setVisibility(View.VISIBLE);
                } else {
                    routeDescriptionTextView.setVisibility(View.GONE);
                }
            }

            // ✅ USAR CAMPOS CORRECTOS DEL JSON
            // Distancia - usar formattedDistance si está disponible, sino calcular
            if (distanceTextView != null) {
                String distanceText = route.getFormattedDistance();
                if (distanceText != null && !distanceText.trim().isEmpty()) {
                    distanceTextView.setText(distanceText);
                } else if (route.getTotalDistance() != null) {
                    distanceText = formatDistance(route.getTotalDistance());
                    distanceTextView.setText(distanceText);
                } else {
                    distanceTextView.setText("Distancia no disponible");
                }
            }

            // Tiempo estimado - usar formattedTime si está disponible, sino calcular
            if (timeTextView != null) {
                String timeText = route.getFormattedTime();
                if (timeText != null && !timeText.trim().isEmpty()) {
                    timeTextView.setText(timeText);
                } else if (route.getEstimatedTime() != null) {
                    timeText = formatTime(route.getEstimatedTime());
                    timeTextView.setText(timeText);
                } else {
                    timeTextView.setText("Tiempo no disponible");
                }
            }

            // Dificultad - usar difficultyText si está disponible, sino usar difficulty
            if (difficultyTextView != null) {
                String difficultyText = route.getDifficultyText();
                if (difficultyText != null && !difficultyText.trim().isEmpty()) {
                    difficultyTextView.setText(difficultyText);
                    setDifficultyColor(difficultyTextView, route.getDifficulty());
                } else {
                    String difficulty = route.getDifficulty();
                    if (difficulty != null) {
                        String formattedDifficulty = formatDifficulty(difficulty);
                        difficultyTextView.setText(formattedDifficulty);
                        setDifficultyColor(difficultyTextView, difficulty);
                    } else {
                        difficultyTextView.setText("Dificultad no disponible");
                        difficultyTextView.setTextColor(itemView.getContext().getColor(android.R.color.darker_gray));
                    }
                }
            }

            // Rating - usar ratingText si está disponible, sino calcular
            if (ratingTextView != null && ratingStarImageView != null) {
                String ratingText = route.getRatingText();
                if (ratingText != null && !ratingText.trim().isEmpty()) {
                    ratingTextView.setText(ratingText);
                    ratingTextView.setVisibility(View.VISIBLE);
                    ratingStarImageView.setVisibility(View.VISIBLE);
                } else if (route.hasRating()) {
                    String calculatedRating = String.format("%.1f (%d)",
                            route.getAverageRating(), route.getTotalRatings());
                    ratingTextView.setText(calculatedRating);
                    ratingTextView.setVisibility(View.VISIBLE);
                    ratingStarImageView.setVisibility(View.VISIBLE);
                } else {
                    ratingTextView.setVisibility(View.GONE);
                    ratingStarImageView.setVisibility(View.GONE);
                }
            }

            // Icono de ruta con indicadores especiales
            if (routeIconImageView != null) {
                setRouteIcon(routeIconImageView, route);
            }

            // Personalizar botón según características de la ruta
            if (navigateButton != null) {
                String buttonText = "🧭 Usar esta ruta";

                if (route.isPopularRoute()) {
                    buttonText = "🔥 Ruta popular";
                } else if (route.isWellRatedRoute()) {
                    buttonText = "⭐ Ruta recomendada";
                }

                navigateButton.setText(buttonText);
            }

            // Click listeners
            if (cardView != null && listener != null) {
                cardView.setOnClickListener(v -> listener.onRouteClick(route));
            }

            if (navigateButton != null && listener != null) {
                navigateButton.setOnClickListener(v -> listener.onNavigateClick(route));
            }
        }

        private String formatDistance(Integer distanceMeters) {
            if (distanceMeters < 1000) {
                return distanceMeters + " metros";
            } else {
                double distanceKm = distanceMeters / 1000.0;
                return String.format("%.1f km", distanceKm);
            }
        }

        private String formatTime(Integer timeMinutes) {
            if (timeMinutes < 60) {
                return timeMinutes + " minutos";
            } else {
                int hours = timeMinutes / 60;
                int remainingMinutes = timeMinutes % 60;
                if (remainingMinutes == 0) {
                    return hours + " hora" + (hours > 1 ? "s" : "");
                } else {
                    return hours + " h " + remainingMinutes + " min";
                }
            }
        }

        private String formatDifficulty(String difficulty) {
            if (difficulty == null) return "Sin especificar";

            switch (difficulty.toUpperCase()) {
                case "EASY":
                    return "Fácil";
                case "MEDIUM":
                    return "Medio";
                case "HARD":
                    return "Difícil";
                default:
                    return difficulty;
            }
        }

        private void setDifficultyColor(TextView textView, String difficulty) {
            if (difficulty == null) return;

            int colorResId;
            switch (difficulty.toUpperCase()) {
                case "EASY":
                    colorResId = android.R.color.holo_green_dark;
                    break;
                case "MEDIUM":
                    colorResId = android.R.color.holo_orange_dark;
                    break;
                case "HARD":
                    colorResId = android.R.color.holo_red_dark;
                    break;
                default:
                    colorResId = android.R.color.darker_gray;
                    break;
            }

            textView.setTextColor(itemView.getContext().getColor(colorResId));
        }

        private void setRouteIcon(ImageView imageView, RouteResponse route) {
            // Usar diferentes iconos según las características de la ruta
            int iconResId = android.R.drawable.ic_menu_directions; // fallback

            try {
                if (route.isPopularRoute()) {
                    iconResId = R.drawable.ic_route_popular;
                } else if (route.isWellRatedRoute()) {
                    iconResId = R.drawable.ic_route_recommended;
                } else if (route.getDifficulty() != null) {
                    switch (route.getDifficulty().toUpperCase()) {
                        case "EASY":
                            iconResId = R.drawable.ic_route_easy;
                            break;
                        case "MEDIUM":
                            iconResId = R.drawable.ic_route_medium;
                            break;
                        case "HARD":
                            iconResId = R.drawable.ic_route_hard;
                            break;
                        default:
                            iconResId = R.drawable.ic_route_default;
                            break;
                    }
                }

                imageView.setImageResource(iconResId);
            } catch (Exception e) {
                // Fallback si no existen los iconos específicos
                imageView.setImageResource(android.R.drawable.ic_menu_directions);
            }
        }
    }

    // Métodos para actualizar datos
    public void updateRoutes(List<RouteResponse> newRoutes) {
        this.routes.clear();
        this.routes.addAll(newRoutes);
        notifyDataSetChanged();
    }

    public void addRoute(RouteResponse route) {
        routes.add(route);
        notifyItemInserted(routes.size() - 1);
    }

    public void removeRoute(int position) {
        if (position >= 0 && position < routes.size()) {
            routes.remove(position);
            notifyItemRemoved(position);
        }
    }

    public RouteResponse getRouteAt(int position) {
        if (position >= 0 && position < routes.size()) {
            return routes.get(position);
        }
        return null;
    }
}