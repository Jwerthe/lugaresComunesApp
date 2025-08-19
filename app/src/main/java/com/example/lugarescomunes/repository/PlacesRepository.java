package com.example.lugarescomunes.repository;

import android.location.Location;
import android.util.Log;

import com.example.lugarescomunes.Place;
import com.example.lugarescomunes.PlaceType;
import com.example.lugarescomunes.api.ApiConfig;
import com.example.lugarescomunes.api.LugaresApiService;
import com.example.lugarescomunes.models.api.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlacesRepository {

    private static final String TAG = "PlacesRepository";
    private static PlacesRepository instance;
    private LugaresApiService apiService;

    // Cache para mejor rendimiento
    private List<Place> cachedPlaces = new ArrayList<>();
    private long lastCacheTime = 0;
    private static final long CACHE_DURATION = 5 * 60 * 1000; // 5 minutos

    private PlacesRepository() {
        // Inicializar servicio API del nuevo backend
        try {
            apiService = ApiConfig.getApiService();
            Log.i(TAG, "PlacesRepository inicializado con backend: " + ApiConfig.getBaseUrl());
        } catch (Exception e) {
            Log.e(TAG, "Error inicializando API Service", e);
            apiService = null;
        }
    }

    public static synchronized PlacesRepository getInstance() {
        if (instance == null) {
            instance = new PlacesRepository();
        }
        return instance;
    }

    // Obtener todos los lugares desde el backend
    public CompletableFuture<List<Place>> getAllPlaces() {
        CompletableFuture<List<Place>> future = new CompletableFuture<>();

        // Verificar cache primero
        if (isCacheValid()) {
            Log.i(TAG, "Retornando lugares desde cache: " + cachedPlaces.size());
            future.complete(new ArrayList<>(cachedPlaces));
            return future;
        }

        if (apiService == null) {
            Log.w(TAG, "API Service no disponible, usando datos de muestra");
            future.complete(getSamplePlaces());
            return future;
        }

        Log.i(TAG, "Cargando lugares desde backend...");

        Call<ApiResponse<List<PlaceResponse>>> call = apiService.getAllPlaces();
        call.enqueue(new Callback<ApiResponse<List<PlaceResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<PlaceResponse>>> call, Response<ApiResponse<List<PlaceResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<PlaceResponse> placeResponses = response.body().getData();
                    List<Place> places = convertPlaceResponsesToPlaces(placeResponses);

                    // Actualizar cache
                    updateCache(places);

                    Log.i(TAG, "Lugares cargados desde backend: " + places.size());
                    future.complete(places);
                } else {
                    Log.w(TAG, "Respuesta no exitosa del backend: " + response.code() + " - " + response.message());
                    // Fallback a datos de muestra
                    List<Place> samplePlaces = getSamplePlaces();
                    updateCache(samplePlaces);
                    future.complete(samplePlaces);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<PlaceResponse>>> call, Throwable t) {
                Log.e(TAG, "Error conectando con backend", t);
                // Fallback a datos de muestra o cache
                if (isCacheValid()) {
                    future.complete(new ArrayList<>(cachedPlaces));
                } else {
                    List<Place> samplePlaces = getSamplePlaces();
                    updateCache(samplePlaces);
                    future.complete(samplePlaces);
                }
            }
        });

        return future;
    }

    // Obtener lugares por tipo desde el backend
    public CompletableFuture<List<Place>> getPlacesByType(PlaceType placeType) {
        CompletableFuture<List<Place>> future = new CompletableFuture<>();

        if (apiService == null) {
            Log.w(TAG, "API Service no disponible, filtrando datos locales");
            List<Place> filteredPlaces = filterPlacesByType(getSamplePlaces(), placeType);
            future.complete(filteredPlaces);
            return future;
        }

        Log.i(TAG, "Buscando lugares por tipo en backend: " + placeType.name());

        Call<ApiResponse<List<PlaceResponse>>> call = apiService.getPlacesByType(placeType.name());
        call.enqueue(new Callback<ApiResponse<List<PlaceResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<PlaceResponse>>> call, Response<ApiResponse<List<PlaceResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<PlaceResponse> placeResponses = response.body().getData();
                    List<Place> places = convertPlaceResponsesToPlaces(placeResponses);
                    Log.i(TAG, "Lugares encontrados por tipo: " + places.size());
                    future.complete(places);
                } else {
                    Log.w(TAG, "Error buscando por tipo: " + response.code());
                    // Fallback a filtrado local
                    List<Place> filteredPlaces = filterPlacesByType(getSamplePlaces(), placeType);
                    future.complete(filteredPlaces);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<PlaceResponse>>> call, Throwable t) {
                Log.e(TAG, "Error en búsqueda por tipo", t);
                // Fallback a filtrado local
                List<Place> filteredPlaces = filterPlacesByType(getSamplePlaces(), placeType);
                future.complete(filteredPlaces);
            }
        });

        return future;
    }

    // Buscar lugares por texto en el backend
    public CompletableFuture<List<Place>> searchPlaces(String query) {
        CompletableFuture<List<Place>> future = new CompletableFuture<>();

        if (apiService == null || query == null || query.trim().isEmpty()) {
            // Búsqueda local como fallback
            future.complete(searchPlacesLocally(query));
            return future;
        }

        Log.i(TAG, "Buscando en backend: " + query);

        Call<ApiResponse<List<PlaceResponse>>> call = apiService.searchPlaces(query.trim());
        call.enqueue(new Callback<ApiResponse<List<PlaceResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<PlaceResponse>>> call, Response<ApiResponse<List<PlaceResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<PlaceResponse> placeResponses = response.body().getData();
                    List<Place> places = convertPlaceResponsesToPlaces(placeResponses);
                    Log.i(TAG, "Búsqueda exitosa: " + places.size() + " lugares encontrados");
                    future.complete(places);
                } else {
                    Log.w(TAG, "Error en búsqueda: " + response.code());
                    // Fallback a búsqueda local
                    future.complete(searchPlacesLocally(query));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<PlaceResponse>>> call, Throwable t) {
                Log.e(TAG, "Error en búsqueda", t);
                // Fallback a búsqueda local
                future.complete(searchPlacesLocally(query));
            }
        });

        return future;
    }

    // Obtener lugares cercanos
    public CompletableFuture<List<Place>> getNearbyPlaces(double latitude, double longitude, double radiusKm) {
        CompletableFuture<List<Place>> future = new CompletableFuture<>();

        if (apiService == null) {
            Log.w(TAG, "API Service no disponible, calculando localmente");
            future.complete(getNearbyPlacesLocally(latitude, longitude, radiusKm));
            return future;
        }

        Log.i(TAG, "Obteniendo lugares cercanos desde backend...");

        Call<ApiResponse<List<PlaceResponse>>> call = apiService.getNearbyPlaces(latitude, longitude, radiusKm);
        call.enqueue(new Callback<ApiResponse<List<PlaceResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<PlaceResponse>>> call, Response<ApiResponse<List<PlaceResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<PlaceResponse> placeResponses = response.body().getData();
                    List<Place> places = convertPlaceResponsesToPlaces(placeResponses);

                    // Calcular distancias y ordenar
                    calculateAndSortByDistance(places, latitude, longitude);

                    Log.i(TAG, "Lugares cercanos obtenidos: " + places.size());
                    future.complete(places);
                } else {
                    Log.w(TAG, "Error obteniendo lugares cercanos: " + response.code());
                    future.complete(getNearbyPlacesLocally(latitude, longitude, radiusKm));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<PlaceResponse>>> call, Throwable t) {
                Log.e(TAG, "Error obteniendo lugares cercanos", t);
                future.complete(getNearbyPlacesLocally(latitude, longitude, radiusKm));
            }
        });

        return future;
    }

    // Obtener lugar por ID desde el backend
    public CompletableFuture<Place> getPlaceById(String placeId) {
        CompletableFuture<Place> future = new CompletableFuture<>();

        if (apiService == null) {
            // Buscar en cache o datos de muestra
            Place place = findPlaceInCache(placeId);
            future.complete(place);
            return future;
        }

        Log.i(TAG, "Buscando lugar por ID en backend: " + placeId);

        Call<ApiResponse<PlaceResponse>> call = apiService.getPlaceById(placeId);
        call.enqueue(new Callback<ApiResponse<PlaceResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<PlaceResponse>> call, Response<ApiResponse<PlaceResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    PlaceResponse placeResponse = response.body().getData();
                    Place place = convertPlaceResponseToPlace(placeResponse);
                    Log.i(TAG, "Lugar encontrado: " + (place != null ? place.getName() : "null"));
                    future.complete(place);
                } else {
                    Log.w(TAG, "Lugar no encontrado: " + placeId);
                    future.complete(null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PlaceResponse>> call, Throwable t) {
                Log.e(TAG, "Error obteniendo lugar por ID", t);
                // Fallback a búsqueda en cache
                Place place = findPlaceInCache(placeId);
                future.complete(place);
            }
        });

        return future;
    }

    // Obtener lugares disponibles
    public CompletableFuture<List<Place>> getAvailablePlaces() {
        CompletableFuture<List<Place>> future = new CompletableFuture<>();

        if (apiService == null) {
            Log.w(TAG, "API Service no disponible, filtrando localmente");
            List<Place> availablePlaces = filterAvailablePlaces(getSamplePlaces());
            future.complete(availablePlaces);
            return future;
        }

        Log.i(TAG, "Obteniendo lugares disponibles desde backend...");

        Call<ApiResponse<List<PlaceResponse>>> call = apiService.getAvailablePlaces();
        call.enqueue(new Callback<ApiResponse<List<PlaceResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<PlaceResponse>>> call, Response<ApiResponse<List<PlaceResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<PlaceResponse> placeResponses = response.body().getData();
                    List<Place> places = convertPlaceResponsesToPlaces(placeResponses);
                    Log.i(TAG, "Lugares disponibles obtenidos: " + places.size());
                    future.complete(places);
                } else {
                    Log.w(TAG, "Error obteniendo lugares disponibles: " + response.code());
                    List<Place> availablePlaces = filterAvailablePlaces(getSamplePlaces());
                    future.complete(availablePlaces);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<PlaceResponse>>> call, Throwable t) {
                Log.e(TAG, "Error obteniendo lugares disponibles", t);
                List<Place> availablePlaces = filterAvailablePlaces(getSamplePlaces());
                future.complete(availablePlaces);
            }
        });

        return future;
    }

    // Verificar conectividad con el backend
    public CompletableFuture<Boolean> checkHealth() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        if (apiService == null) {
            future.complete(false);
            return future;
        }

        Call<ApiResponse<Object>> call = apiService.generalHealth();
        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                boolean healthy = response.isSuccessful() && response.body() != null && response.body().isSuccess();
                Log.i(TAG, "Health check: " + (healthy ? "OK" : "FAILED"));
                future.complete(healthy);
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Log.e(TAG, "Health check failed", t);
                future.complete(false);
            }
        });

        return future;
    }

    // MÉTODOS PRIVADOS DE CONVERSIÓN Y UTILIDADES

    // Convertir lista de PlaceResponse a Place
    private List<Place> convertPlaceResponsesToPlaces(List<PlaceResponse> placeResponses) {
        List<Place> places = new ArrayList<>();
        if (placeResponses != null) {
            for (PlaceResponse placeResponse : placeResponses) {
                Place place = convertPlaceResponseToPlace(placeResponse);
                if (place != null) {
                    places.add(place);
                }
            }
        }
        return places;
    }

    // Convertir PlaceResponse individual a Place
    private Place convertPlaceResponseToPlace(PlaceResponse placeResponse) {
        if (placeResponse == null) return null;

        Place place = new Place();
        place.setId(placeResponse.getId());
        place.setName(placeResponse.getName());
        place.setCategory(placeResponse.getCategory());
        place.setDescription(placeResponse.getDescription());
        place.setWhat3words(placeResponse.getWhat3words());

        // Convertir BigDecimal a double para coordenadas
        if (placeResponse.getLatitude() != null) {
            place.setLatitude(placeResponse.getLatitude().doubleValue());
        }
        if (placeResponse.getLongitude() != null) {
            place.setLongitude(placeResponse.getLongitude().doubleValue());
        }

        place.setAvailable(placeResponse.getIsAvailable() != null ? placeResponse.getIsAvailable() : true);

        // Convertir string de tipo a enum
        if (placeResponse.getPlaceType() != null) {
            try {
                place.setType(PlaceType.valueOf(placeResponse.getPlaceType()));
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "Tipo de lugar desconocido: " + placeResponse.getPlaceType());
                place.setType(PlaceType.SERVICE); // Valor por defecto
            }
        } else {
            place.setType(PlaceType.SERVICE);
        }

        place.setCapacity(placeResponse.getCapacity());
        place.setSchedule(placeResponse.getSchedule());

        // Para distancia, usaremos un valor por defecto
        place.setDistanceInMeters(0);

        // Por defecto no es favorito hasta que implementemos favoritos
        place.setFavorite(false);

        return place;
    }

    // MÉTODOS DE CACHE

    private boolean isCacheValid() {
        return !cachedPlaces.isEmpty() &&
                (System.currentTimeMillis() - lastCacheTime) < CACHE_DURATION;
    }

    private void updateCache(List<Place> places) {
        cachedPlaces.clear();
        cachedPlaces.addAll(places);
        lastCacheTime = System.currentTimeMillis();
    }

    private Place findPlaceInCache(String placeId) {
        for (Place place : cachedPlaces) {
            if (place.getId().equals(placeId)) {
                return place;
            }
        }
        // Si no está en cache, buscar en datos de muestra
        for (Place place : getSamplePlaces()) {
            if (place.getId().equals(placeId)) {
                return place;
            }
        }
        return null;
    }

    // MÉTODOS DE FILTRADO LOCAL

    private List<Place> filterPlacesByType(List<Place> places, PlaceType placeType) {
        List<Place> filtered = new ArrayList<>();
        for (Place place : places) {
            if (place.getType() == placeType) {
                filtered.add(place);
            }
        }
        return filtered;
    }

    private List<Place> filterAvailablePlaces(List<Place> places) {
        List<Place> filtered = new ArrayList<>();
        for (Place place : places) {
            if (place.isAvailable()) {
                filtered.add(place);
            }
        }
        return filtered;
    }

    private List<Place> searchPlacesLocally(String query) {
        List<Place> allPlaces = cachedPlaces.isEmpty() ? getSamplePlaces() : cachedPlaces;

        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(allPlaces);
        }

        List<Place> filtered = new ArrayList<>();
        String queryLower = query.toLowerCase();

        for (Place place : allPlaces) {
            if (place.getName().toLowerCase().contains(queryLower) ||
                    place.getCategory().toLowerCase().contains(queryLower) ||
                    (place.getDescription() != null && place.getDescription().toLowerCase().contains(queryLower)) ||
                    (place.getWhat3words() != null && place.getWhat3words().toLowerCase().contains(queryLower))) {
                filtered.add(place);
            }
        }

        return filtered;
    }

    private List<Place> getNearbyPlacesLocally(double latitude, double longitude, double radiusKm) {
        List<Place> allPlaces = cachedPlaces.isEmpty() ? getSamplePlaces() : cachedPlaces;
        List<Place> nearbyPlaces = new ArrayList<>();

        for (Place place : allPlaces) {
            float[] results = new float[1];
            Location.distanceBetween(latitude, longitude, place.getLatitude(), place.getLongitude(), results);
            float distanceInKm = results[0] / 1000.0f;

            if (distanceInKm <= radiusKm) {
                place.setDistanceInMeters((int) results[0]);
                nearbyPlaces.add(place);
            }
        }

        // Ordenar por distancia
        nearbyPlaces.sort((a, b) -> Integer.compare(a.getDistanceInMeters(), b.getDistanceInMeters()));

        return nearbyPlaces;
    }

    private void calculateAndSortByDistance(List<Place> places, double latitude, double longitude) {
        for (Place place : places) {
            float[] results = new float[1];
            Location.distanceBetween(latitude, longitude, place.getLatitude(), place.getLongitude(), results);
            place.setDistanceInMeters((int) results[0]);
        }

        // Ordenar por distancia
        places.sort((a, b) -> Integer.compare(a.getDistanceInMeters(), b.getDistanceInMeters()));
    }

    // DATOS DE MUESTRA PARA FALLBACK
    private List<Place> getSamplePlaces() {
        List<Place> samplePlaces = new ArrayList<>();

        // Biblioteca Central
        Place biblioteca = new Place();
        biblioteca.setId("1");
        biblioteca.setName("Biblioteca Central PUCE");
        biblioteca.setCategory("Biblioteca");
        biblioteca.setDescription("Biblioteca principal con acceso a recursos académicos y espacios de estudio");
        biblioteca.setLatitude(-0.210759);
        biblioteca.setLongitude(-78.487359);
        biblioteca.setAvailable(true);
        biblioteca.setType(PlaceType.LIBRARY);
        biblioteca.setCapacity(200);
        biblioteca.setSchedule("Lunes a Viernes: 7:00 - 21:00");
        biblioteca.setWhat3words("///ejemplo.biblioteca.puce");
        biblioteca.setDistanceInMeters(50);
        biblioteca.setFavorite(false);
        samplePlaces.add(biblioteca);

        // Cafetería Principal
        Place cafeteria = new Place();
        cafeteria.setId("2");
        cafeteria.setName("Cafetería Principal");
        cafeteria.setCategory("Alimentación");
        cafeteria.setDescription("Cafetería con variedad de alimentos y bebidas para la comunidad universitaria");
        cafeteria.setLatitude(-0.210959);
        cafeteria.setLongitude(-78.487159);
        cafeteria.setAvailable(true);
        cafeteria.setType(PlaceType.CAFETERIA);
        cafeteria.setCapacity(150);
        cafeteria.setSchedule("Lunes a Viernes: 6:30 - 18:00");
        cafeteria.setWhat3words("///ejemplo.cafeteria.puce");
        cafeteria.setDistanceInMeters(120);
        cafeteria.setFavorite(false);
        samplePlaces.add(cafeteria);

        // Laboratorio de Computación
        Place laboratorio = new Place();
        laboratorio.setId("3");
        laboratorio.setName("Laboratorio de Computación A");
        laboratorio.setCategory("Laboratorio");
        laboratorio.setDescription("Laboratorio equipado con computadoras para prácticas de programación");
        laboratorio.setLatitude(-0.211059);
        laboratorio.setLongitude(-78.487259);
        laboratorio.setAvailable(false);
        laboratorio.setType(PlaceType.LABORATORY);
        laboratorio.setCapacity(30);
        laboratorio.setSchedule("Lunes a Viernes: 8:00 - 17:00");
        laboratorio.setWhat3words("///ejemplo.laboratorio.puce");
        laboratorio.setDistanceInMeters(200);
        laboratorio.setFavorite(false);
        samplePlaces.add(laboratorio);

        // Aula Magna
        Place aula = new Place();
        aula.setId("4");
        aula.setName("Aula Magna");
        aula.setCategory("Auditorio");
        aula.setDescription("Auditorio principal para eventos, conferencias y graduaciones");
        aula.setLatitude(-0.210559);
        aula.setLongitude(-78.487459);
        aula.setAvailable(true);
        aula.setType(PlaceType.AUDITORIUM);
        aula.setCapacity(500);
        aula.setSchedule("Eventos programados");
        aula.setWhat3words("///ejemplo.aulamagna.puce");
        aula.setDistanceInMeters(300);
        aula.setFavorite(false);
        samplePlaces.add(aula);

        return samplePlaces;
    }

    // Limpiar cache (útil para refresh manual)
    public void clearCache() {
        cachedPlaces.clear();
        lastCacheTime = 0;
        Log.i(TAG, "Cache limpiado");
    }
}