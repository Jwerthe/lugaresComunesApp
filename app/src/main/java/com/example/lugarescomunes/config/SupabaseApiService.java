package com.example.lugarescomunes.config;

import com.example.lugarescomunes.Place;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.DELETE;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SupabaseApiService {

    // Obtener todos los lugares
    @GET("places")
    Call<List<Place>> getAllPlaces();

    // Obtener lugares por tipo
    @GET("places")
    Call<List<Place>> getPlacesByType(@Query("place_type") String placeType);

    // Buscar lugares por texto (búsqueda en múltiples campos)
    @GET("places")
    Call<List<Place>> searchPlaces(
            @Query("or") String searchQuery
    );

    // Obtener lugar por ID
    @GET("places")
    Call<List<Place>> getPlaceById(@Query("id") String id);

    // Obtener lugares cercanos (usando función de PostgreSQL)
    @GET("rpc/get_nearby_places")
    Call<List<Place>> getNearbyPlaces(
            @Query("lat") double latitude,
            @Query("lng") double longitude,
            @Query("radius_km") double radiusKm
    );

    // Obtener lugares por edificio
    @GET("places")
    Call<List<Place>> getPlacesByBuilding(@Query("building_name") String buildingName);

    // Obtener lugares disponibles
    @GET("places")
    Call<List<Place>> getAvailablePlaces(@Query("is_available") boolean isAvailable);

    // CRUD operations (para futuro uso administrativo)

    // Crear lugar
    @POST("places")
    Call<Place> createPlace(@Body Place place);

    // Actualizar lugar
    @PUT("places")
    Call<Place> updatePlace(@Query("id") String id, @Body Place place);

    // Eliminar lugar
    @DELETE("places")
    Call<Void> deletePlace(@Query("id") String id);

    // === ENDPOINTS PARA FAVORITOS ===

    // Obtener favoritos de usuario
    @GET("user_favorites")
    Call<List<UserFavorite>> getUserFavorites(@Query("user_id") String userId);

    // Agregar favorito
    @POST("user_favorites")
    Call<UserFavorite> addFavorite(@Body UserFavorite favorite);

    // Remover favorito
    @DELETE("user_favorites")
    Call<Void> removeFavorite(@Query("user_id") String userId, @Query("place_id") String placeId);

    // === ENDPOINTS PARA REPORTES ===

    // Crear reporte
    @POST("place_reports")
    Call<PlaceReport> createReport(@Body PlaceReport report);

    // Obtener reportes de un lugar
    @GET("place_reports")
    Call<List<PlaceReport>> getPlaceReports(@Query("place_id") String placeId);

    // === CLASES INTERNAS PARA RESPUESTAS ===

    class UserFavorite {
        public String id;
        public String user_id;
        public String place_id;
        public String created_at;

        public UserFavorite() {}

        public UserFavorite(String userId, String placeId) {
            this.user_id = userId;
            this.place_id = placeId;
        }
    }

    class PlaceReport {
        public String id;
        public String place_id;
        public String user_id;
        public String report_type; // 'incorrect_info', 'maintenance_needed', 'unavailable', 'accessibility_issue', 'other'
        public String description;
        public String status; // 'pending', 'in_progress', 'resolved', 'dismissed'
        public String created_at;
        public String resolved_at;

        public PlaceReport() {}

        public PlaceReport(String placeId, String userId, String reportType, String description) {
            this.place_id = placeId;
            this.user_id = userId;
            this.report_type = reportType;
            this.description = description;
            this.status = "pending";
        }
    }
}