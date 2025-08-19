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
            List<Place> samplePlaces = getSamplePlaces();
            updateCache(samplePlaces);
            future.complete(samplePlaces);
            return future;
        }

        Log.i(TAG, "Cargando lugares desde backend...");

        Call<ApiResponse<List<PlaceResponse>>> call = apiService.getAllPlaces();
        call.enqueue(new Callback<ApiResponse<List<PlaceResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<PlaceResponse>>> call, Response<ApiResponse<List<PlaceResponse>>> response) {
                try {
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
                } catch (Exception e) {
                    Log.e(TAG, "Error procesando respuesta de getAllPlaces", e);
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
                try {
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
                } catch (Exception e) {
                    Log.e(TAG, "Error procesando búsqueda", e);
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
                try {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        List<PlaceResponse> placeResponses = response.body().getData();
                        List<Place> places = convertPlaceResponsesToPlaces(placeResponses);
                        Log.i(TAG, "Lugares por tipo obtenidos: " + places.size());
                        future.complete(places);
                    } else {
                        Log.w(TAG, "Error obteniendo lugares por tipo: " + response.code());
                        List<Place> filteredPlaces = filterPlacesByType(getSamplePlaces(), placeType);
                        future.complete(filteredPlaces);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error procesando lugares por tipo", e);
                    List<Place> filteredPlaces = filterPlacesByType(getSamplePlaces(), placeType);
                    future.complete(filteredPlaces);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<PlaceResponse>>> call, Throwable t) {
                Log.e(TAG, "Error obteniendo lugares por tipo", t);
                List<Place> filteredPlaces = filterPlacesByType(getSamplePlaces(), placeType);
                future.complete(filteredPlaces);
            }
        });

        return future;
    }

    // Verificar conectividad con el backend - USANDO ENDPOINT QUE SÍ EXISTE
    public CompletableFuture<Boolean> checkHealth() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        if (apiService == null) {
            future.complete(false);
            return future;
        }

        // ✅ CORRECCIÓN: Usar endpoint de auth que SÍ existe
        Call<ApiResponse<Object>> call = apiService.authHealth();
        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                boolean healthy = response.isSuccessful();
                Log.i(TAG, "Health check (auth): " + (healthy ? "OK" : "FAILED") + " - Código: " + response.code());
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

    // Convertir lista de PlaceResponse a Place - CON MANEJO SEGURO DE NULLS
    private List<Place> convertPlaceResponsesToPlaces(List<PlaceResponse> placeResponses) {
        List<Place> places = new ArrayList<>();
        if (placeResponses != null) {
            for (PlaceResponse placeResponse : placeResponses) {
                try {
                    Place place = convertPlaceResponseToPlace(placeResponse);
                    if (place != null) {
                        places.add(place);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error convirtiendo lugar: " + (placeResponse != null ? placeResponse.getName() : "null"), e);
                    // Continúar con el siguiente lugar en lugar de crashear
                }
            }
        }
        return places;
    }

    // Convertir PlaceResponse individual a Place - CON MANEJO SEGURO DE NULLS
    private Place convertPlaceResponseToPlace(PlaceResponse placeResponse) {
        if (placeResponse == null) {
            Log.w(TAG, "PlaceResponse es null, saltando conversión");
            return null;
        }

        try {
            Place place = new Place();

            // Campos básicos con valores seguros
            place.setId(placeResponse.getId() != null ? placeResponse.getId() : "unknown_" + System.currentTimeMillis());
            place.setName(placeResponse.getName() != null ? placeResponse.getName() : "Lugar sin nombre");
            place.setCategory(placeResponse.getCategory() != null ? placeResponse.getCategory() : "Sin categoría");
            place.setDescription(placeResponse.getDescription() != null ? placeResponse.getDescription() : "Sin descripción");
            place.setWhat3words(placeResponse.getWhat3words() != null ? placeResponse.getWhat3words() : "");

            // Coordenadas con manejo seguro
            if (placeResponse.getLatitude() != null) {
                place.setLatitude(placeResponse.getLatitude().doubleValue());
            } else {
                place.setLatitude(-0.210759); // Default PUCE
            }

            if (placeResponse.getLongitude() != null) {
                place.setLongitude(placeResponse.getLongitude().doubleValue());
            } else {
                place.setLongitude(-78.487359); // Default PUCE
            }

            // Disponibilidad con valor por defecto
            place.setAvailable(placeResponse.getIsAvailable() != null ? placeResponse.getIsAvailable() : true);

            // Tipo de lugar con manejo seguro
            if (placeResponse.getPlaceType() != null) {
                try {
                    place.setType(PlaceType.valueOf(placeResponse.getPlaceType()));
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Tipo de lugar desconocido: " + placeResponse.getPlaceType() + ", usando SERVICE como default");
                    place.setType(PlaceType.SERVICE); // Valor por defecto seguro
                }
            } else {
                place.setType(PlaceType.SERVICE);
            }

            // ✅ CORRECCIÓN: Manejo seguro de Integer capacity (LÍNEA QUE CAUSABA CRASH)
            if (placeResponse.getCapacity() != null) {
                place.setCapacity(placeResponse.getCapacity().intValue());
            } else {
                place.setCapacity(0); // Valor por defecto seguro
            }

            // Otros campos opcionales
            place.setSchedule(placeResponse.getSchedule() != null ? placeResponse.getSchedule() : "Horario no disponible");

            // Campos por defecto
            place.setDistanceInMeters(0);
            place.setFavorite(false);

            Log.d(TAG, "Lugar convertido exitosamente: " + place.getName() + " (" + place.getType() + ")");
            return place;

        } catch (Exception e) {
            Log.e(TAG, "Error fatal convirtiendo lugar: " + placeResponse.getName(), e);
            return null; // Retornar null en lugar de crashear
        }
    }

    // MÉTODOS DE CACHE

    private boolean isCacheValid() {
        return !cachedPlaces.isEmpty() &&
                (System.currentTimeMillis() - lastCacheTime) < CACHE_DURATION;
    }

    private void updateCache(List<Place> places) {
        cachedPlaces.clear();
        if (places != null) {
            cachedPlaces.addAll(places);
        }
        lastCacheTime = System.currentTimeMillis();
        Log.d(TAG, "Cache actualizado con " + cachedPlaces.size() + " lugares");
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

        Log.d(TAG, "Búsqueda local para '" + query + "' encontró " + filtered.size() + " lugares");
        return filtered;
    }

    // DATOS DE MUESTRA PARA FALLBACK
    private List<Place> getSamplePlaces() {
        List<Place> samplePlaces = new ArrayList<>();

        // Biblioteca Central
        Place biblioteca = new Place();
        biblioteca.setId("sample_1");
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
        cafeteria.setId("sample_2");
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

        // Entrada Principal
        Place entrada = new Place();
        entrada.setId("sample_3");
        entrada.setName("Entrada Principal");
        entrada.setCategory("Acceso");
        entrada.setDescription("Entrada principal del campus universitario");
        entrada.setLatitude(-0.211059);
        entrada.setLongitude(-78.487259);
        entrada.setAvailable(true);
        entrada.setType(PlaceType.ENTRANCE);
        entrada.setCapacity(0);
        entrada.setSchedule("24 horas");
        entrada.setWhat3words("///ejemplo.entrada.puce");
        entrada.setDistanceInMeters(0);
        entrada.setFavorite(false);
        samplePlaces.add(entrada);

        Log.d(TAG, "Datos de muestra creados: " + samplePlaces.size() + " lugares");
        return samplePlaces;
    }

    // Limpiar cache (útil para refresh manual)
    public void clearCache() {
        cachedPlaces.clear();
        lastCacheTime = 0;
        Log.i(TAG, "Cache limpiado");
    }
}