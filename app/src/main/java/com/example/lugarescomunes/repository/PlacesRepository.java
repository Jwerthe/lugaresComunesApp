package com.example.lugarescomunes.repository;

import android.util.Log;
import com.example.lugarescomunes.api.ApiConfig;
import com.example.lugarescomunes.api.LugaresApiService;
import com.example.lugarescomunes.Place;
import com.example.lugarescomunes.PlaceType;
import com.example.lugarescomunes.models.api.ApiResponse;
import com.example.lugarescomunes.models.api.PlaceResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PlacesRepository {

    private static final String TAG = "PlacesRepository";
    private static PlacesRepository instance;
    private LugaresApiService apiService;

    // Cache
    private List<Place> cachedPlaces = new ArrayList<>();
    private long lastCacheUpdate = 0;
    private static final long CACHE_DURATION = 5 * 60 * 1000; // 5 minutos

    private PlacesRepository() {
        this.apiService = ApiConfig.getApiService();
        Log.i(TAG, "PlacesRepository inicializado");
    }

    public static synchronized PlacesRepository getInstance() {
        if (instance == null) {
            instance = new PlacesRepository();
        }
        return instance;
    }

    // ✅ NUEVO: Obtener destinos disponibles desde /routes/destinations
    public CompletableFuture<List<Place>> getAllPlaces() {
        CompletableFuture<List<Place>> future = new CompletableFuture<>();

        if (apiService == null) {
            Log.e(TAG, "API Service no disponible");
            future.completeExceptionally(new RuntimeException("API Service no disponible"));
            return future;
        }

        // Verificar cache válido
        if (isCacheValid()) {
            Log.d(TAG, "Retornando datos desde cache: " + cachedPlaces.size() + " lugares");
            future.complete(new ArrayList<>(cachedPlaces));
            return future;
        }

        Log.i(TAG, "Cargando destinos desde /routes/destinations");

        Call<ApiResponse<List<PlaceResponse>>> call = apiService.getRouteDestinations();
        call.enqueue(new Callback<ApiResponse<List<PlaceResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<PlaceResponse>>> call, Response<ApiResponse<List<PlaceResponse>>> response) {
                try {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        List<PlaceResponse> placeResponses = response.body().getData();
                        List<Place> places = convertPlaceResponsesToPlaces(placeResponses);

                        Log.i(TAG, "Destinos cargados exitosamente: " + places.size());
                        updateCache(places);
                        future.complete(places);
                    } else {
                        String errorMsg = "Error en respuesta: " + response.code();
                        if (response.body() != null && response.body().getMessage() != null) {
                            errorMsg += " - " + response.body().getMessage();
                        }
                        Log.w(TAG, errorMsg);
                        future.completeExceptionally(new RuntimeException(errorMsg));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error procesando respuesta de destinos", e);
                    future.completeExceptionally(e);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<PlaceResponse>>> call, Throwable t) {
                Log.e(TAG, "Error en llamada a /routes/destinations", t);
                future.completeExceptionally(new RuntimeException("Error de conexión", t));
            }
        });

        return future;
    }

    // Buscar lugares por texto
    public CompletableFuture<List<Place>> searchPlaces(String query) {
        CompletableFuture<List<Place>> future = new CompletableFuture<>();

        if (query == null || query.trim().isEmpty()) {
            return getAllPlaces();
        }

        // Búsqueda local primero si tenemos cache
        if (isCacheValid()) {
            List<Place> filtered = searchPlacesLocally(query);
            future.complete(filtered);
            return future;
        }

        // Si no hay cache, obtener todos y luego filtrar
        getAllPlaces()
                .thenAccept(places -> {
                    List<Place> filtered = searchPlacesLocally(query, places);
                    future.complete(filtered);
                })
                .exceptionally(throwable -> {
                    future.completeExceptionally(throwable);
                    return null;
                });

        return future;
    }

    // Obtener lugar por ID
    public CompletableFuture<Place> getPlaceById(String placeId) {
        CompletableFuture<Place> future = new CompletableFuture<>();

        if (apiService == null || placeId == null || placeId.trim().isEmpty()) {
            future.completeExceptionally(new RuntimeException("Parámetros inválidos"));
            return future;
        }

        Log.i(TAG, "Obteniendo lugar por ID: " + placeId);

        Call<ApiResponse<PlaceResponse>> call = apiService.getPlaceById(placeId);
        call.enqueue(new Callback<ApiResponse<PlaceResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<PlaceResponse>> call, Response<ApiResponse<PlaceResponse>> response) {
                try {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        PlaceResponse placeResponse = response.body().getData();
                        Place place = convertPlaceResponseToPlace(placeResponse);
                        Log.i(TAG, "Lugar obtenido exitosamente: " + place.getName());
                        future.complete(place);
                    } else {
                        String errorMsg = "Lugar no encontrado: " + response.code();
                        Log.w(TAG, errorMsg);
                        future.completeExceptionally(new RuntimeException(errorMsg));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error procesando lugar", e);
                    future.completeExceptionally(e);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PlaceResponse>> call, Throwable t) {
                Log.e(TAG, "Error obteniendo lugar", t);
                future.completeExceptionally(new RuntimeException("Error de conexión", t));
            }
        });

        return future;
    }

    // Cache management
    private boolean isCacheValid() {
        boolean valid = !cachedPlaces.isEmpty() &&
                (System.currentTimeMillis() - lastCacheUpdate) < CACHE_DURATION;
        Log.d(TAG, "Cache válido: " + valid + " (tamaño: " + cachedPlaces.size() + ")");
        return valid;
    }

    private void updateCache(List<Place> places) {
        cachedPlaces.clear();
        cachedPlaces.addAll(places);
        lastCacheUpdate = System.currentTimeMillis();
        Log.d(TAG, "Cache actualizado con " + places.size() + " lugares");
    }

    // Búsqueda local
    private List<Place> searchPlacesLocally(String query) {
        return searchPlacesLocally(query, cachedPlaces);
    }

    private List<Place> searchPlacesLocally(String query, List<Place> places) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(places);
        }

        List<Place> filtered = new ArrayList<>();
        String queryLower = query.toLowerCase();

        for (Place place : places) {
            if (place.getName().toLowerCase().contains(queryLower) ||
                    place.getCategory().toLowerCase().contains(queryLower) ||
                    (place.getDescription() != null && place.getDescription().toLowerCase().contains(queryLower)) ||
                    (place.getWhat3words() != null && place.getWhat3words().toLowerCase().contains(queryLower))) {
                filtered.add(place);
            }
        }

        Log.d(TAG, "Búsqueda local para '" + query + "' encontró " + filtered.size() + " lugares");
        return filtered;
    }

    // Conversión de PlaceResponse a Place
    private List<Place> convertPlaceResponsesToPlaces(List<PlaceResponse> placeResponses) {
        List<Place> places = new ArrayList<>();

        if (placeResponses == null) {
            return places;
        }

        for (PlaceResponse response : placeResponses) {
            Place place = convertPlaceResponseToPlace(response);
            if (place != null) {
                places.add(place);
            }
        }

        return places;
    }

    private Place convertPlaceResponseToPlace(PlaceResponse response) {
        if (response == null) {
            return null;
        }

        Place place = new Place();
        place.setId(response.getId());
        place.setName(response.getName());
        place.setCategory(response.getCategory());
        place.setDescription(response.getDescription());

        // ✅ CONVERSIÓN BigDecimal a double
        place.setLatitude(response.getLatitude() != null ? response.getLatitude().doubleValue() : 0.0);
        place.setLongitude(response.getLongitude() != null ? response.getLongitude().doubleValue() : 0.0);

        place.setAvailable(response.getIsAvailable() != null ? response.getIsAvailable() : true);
        place.setWhat3words(response.getWhat3words());
        place.setCapacity(response.getCapacity() != null ? response.getCapacity() : 0);
        place.setSchedule(response.getSchedule());

        // Convertir tipo de lugar
        if (response.getPlaceType() != null) {
            try {
                place.setType(PlaceType.valueOf(response.getPlaceType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                place.setType(PlaceType.SERVICE);
                Log.w(TAG, "Tipo de lugar desconocido: " + response.getPlaceType());
            }
        } else {
            place.setType(PlaceType.SERVICE);
        }

        // Configurar distancia por defecto (se actualizará con GPS)
        place.setDistanceInMeters(0);
        place.setFavorite(false);

        return place;
    }

    // Limpiar cache (útil para refrescar datos)
    public void clearCache() {
        cachedPlaces.clear();
        lastCacheUpdate = 0;
        Log.i(TAG, "Cache limpiado");
    }

    // Health check
    public CompletableFuture<Boolean> checkApiHealth() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        if (apiService == null) {
            future.complete(false);
            return future;
        }

        Call<ApiResponse<Object>> call = apiService.generalHealth();
        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                boolean healthy = response.isSuccessful();
                Log.i(TAG, "API Health check: " + (healthy ? "OK" : "ERROR"));
                future.complete(healthy);
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Log.w(TAG, "API Health check failed", t);
                future.complete(false);
            }
        });

        return future;
    }
}