package com.example.lugarescomunes.repository;

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

public class NewPlacesRepository {

    private static final String TAG = "NewPlacesRepository";
    private static NewPlacesRepository instance;
    private LugaresApiService apiService;

    private NewPlacesRepository() {
        apiService = ApiConfig.getApiService();
        Log.i(TAG, "NewPlacesRepository inicializado con backend: " + ApiConfig.getBaseUrl());
    }

    public static synchronized NewPlacesRepository getInstance() {
        if (instance == null) {
            instance = new NewPlacesRepository();
        }
        return instance;
    }

    // Obtener todos los lugares
    public CompletableFuture<List<Place>> getAllPlaces() {
        CompletableFuture<List<Place>> future = new CompletableFuture<>();

        Log.i(TAG, "Obteniendo todos los lugares desde backend...");

        Call<ApiResponse<List<PlaceResponse>>> call = apiService.getAllPlaces();
        call.enqueue(new Callback<ApiResponse<List<PlaceResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<PlaceResponse>>> call, Response<ApiResponse<List<PlaceResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<PlaceResponse> placeResponses = response.body().getData();
                    List<Place> places = convertPlaceResponsesToPlaces(placeResponses);
                    Log.i(TAG, "Lugares obtenidos exitosamente: " + places.size());
                    future.complete(places);
                } else {
                    Log.w(TAG, "Error en respuesta del servidor: " + response.code());
                    future.complete(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<PlaceResponse>>> call, Throwable t) {
                Log.e(TAG, "Error conectando con backend", t);
                future.complete(new ArrayList<>());
            }
        });

        return future;
    }

    // Buscar lugares por texto
    public CompletableFuture<List<Place>> searchPlaces(String query) {
        CompletableFuture<List<Place>> future = new CompletableFuture<>();

        if (query == null || query.trim().isEmpty()) {
            future.complete(new ArrayList<>());
            return future;
        }

        Log.i(TAG, "Buscando lugares con query: " + query);

        Call<ApiResponse<List<PlaceResponse>>> call = apiService.searchPlaces(query.trim());
        call.enqueue(new Callback<ApiResponse<List<PlaceResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<PlaceResponse>>> call, Response<ApiResponse<List<PlaceResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<PlaceResponse> placeResponses = response.body().getData();
                    List<Place> places = convertPlaceResponsesToPlaces(placeResponses);
                    Log.i(TAG, "Búsqueda exitosa, lugares encontrados: " + places.size());
                    future.complete(places);
                } else {
                    Log.w(TAG, "Error en búsqueda: " + response.code());
                    future.complete(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<PlaceResponse>>> call, Throwable t) {
                Log.e(TAG, "Error en búsqueda", t);
                future.complete(new ArrayList<>());
            }
        });

        return future;
    }

    // Obtener lugares por tipo
    public CompletableFuture<List<Place>> getPlacesByType(PlaceType placeType) {
        CompletableFuture<List<Place>> future = new CompletableFuture<>();

        Log.i(TAG, "Obteniendo lugares por tipo: " + placeType.name());

        Call<ApiResponse<List<PlaceResponse>>> call = apiService.getPlacesByType(placeType.name());
        call.enqueue(new Callback<ApiResponse<List<PlaceResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<PlaceResponse>>> call, Response<ApiResponse<List<PlaceResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<PlaceResponse> placeResponses = response.body().getData();
                    List<Place> places = convertPlaceResponsesToPlaces(placeResponses);
                    Log.i(TAG, "Lugares por tipo obtenidos: " + places.size());
                    future.complete(places);
                } else {
                    Log.w(TAG, "Error obteniendo lugares por tipo: " + response.code());
                    future.complete(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<PlaceResponse>>> call, Throwable t) {
                Log.e(TAG, "Error obteniendo lugares por tipo", t);
                future.complete(new ArrayList<>());
            }
        });

        return future;
    }

    // Obtener lugares disponibles
    public CompletableFuture<List<Place>> getAvailablePlaces() {
        CompletableFuture<List<Place>> future = new CompletableFuture<>();

        Log.i(TAG, "Obteniendo lugares disponibles...");

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
                    future.complete(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<PlaceResponse>>> call, Throwable t) {
                Log.e(TAG, "Error obteniendo lugares disponibles", t);
                future.complete(new ArrayList<>());
            }
        });

        return future;
    }

    // Obtener lugares cercanos
    public CompletableFuture<List<Place>> getNearbyPlaces(double latitude, double longitude, double radiusKm) {
        CompletableFuture<List<Place>> future = new CompletableFuture<>();

        Log.i(TAG, "Obteniendo lugares cercanos...");

        Call<ApiResponse<List<PlaceResponse>>> call = apiService.getNearbyPlaces(latitude, longitude, radiusKm);
        call.enqueue(new Callback<ApiResponse<List<PlaceResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<PlaceResponse>>> call, Response<ApiResponse<List<PlaceResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<PlaceResponse> placeResponses = response.body().getData();
                    List<Place> places = convertPlaceResponsesToPlaces(placeResponses);
                    Log.i(TAG, "Lugares cercanos obtenidos: " + places.size());
                    future.complete(places);
                } else {
                    Log.w(TAG, "Error obteniendo lugares cercanos: " + response.code());
                    future.complete(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<PlaceResponse>>> call, Throwable t) {
                Log.e(TAG, "Error obteniendo lugares cercanos", t);
                future.complete(new ArrayList<>());
            }
        });

        return future;
    }

    // Obtener lugar por ID
    public CompletableFuture<Place> getPlaceById(String placeId) {
        CompletableFuture<Place> future = new CompletableFuture<>();

        Log.i(TAG, "Obteniendo lugar por ID: " + placeId);

        Call<ApiResponse<PlaceResponse>> call = apiService.getPlaceById(placeId);
        call.enqueue(new Callback<ApiResponse<PlaceResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<PlaceResponse>> call, Response<ApiResponse<PlaceResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    PlaceResponse placeResponse = response.body().getData();
                    Place place = convertPlaceResponseToPlace(placeResponse);
                    Log.i(TAG, "Lugar obtenido por ID: " + place.getName());
                    future.complete(place);
                } else {
                    Log.w(TAG, "Error obteniendo lugar por ID: " + response.code());
                    future.complete(null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PlaceResponse>> call, Throwable t) {
                Log.e(TAG, "Error obteniendo lugar por ID", t);
                future.complete(null);
            }
        });

        return future;
    }

    // Convertir PlaceResponse a Place
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

        // Para distancia, usaremos un valor por defecto o calculado
        place.setDistanceInMeters(0);

        // Por defecto no es favorito hasta que implementemos favoritos
        place.setFavorite(false);

        return place;
    }

    // Método para verificar conectividad con el backend
    public CompletableFuture<Boolean> checkHealth() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

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
}