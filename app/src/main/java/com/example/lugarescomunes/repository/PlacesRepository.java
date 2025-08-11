package com.example.lugarescomunes.repository;

import android.location.Location;
import android.util.Log;

import com.example.lugarescomunes.Place;
import com.example.lugarescomunes.PlaceType;
import com.example.lugarescomunes.config.SupabaseApiService;
import com.example.lugarescomunes.config.SupabaseConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlacesRepository {

    private static final String TAG = "PlacesRepository";
    private static PlacesRepository instance;
    private SupabaseApiService apiService;

    private PlacesRepository() {
        // Inicializar servicio API de Supabase
        try {
            apiService = SupabaseConfig.getApiService();
            Log.i(TAG, "Supabase API Service inicializado correctamente");
        } catch (Exception e) {
            Log.e(TAG, "Error inicializando Supabase API Service", e);
            apiService = null;
        }
    }

    public static synchronized PlacesRepository getInstance() {
        if (instance == null) {
            instance = new PlacesRepository();
        }
        return instance;
    }

    // Obtener todos los lugares desde Supabase
    public CompletableFuture<List<Place>> getAllPlaces() {
        CompletableFuture<List<Place>> future = new CompletableFuture<>();

        if (apiService == null) {
            Log.w(TAG, "API Service no disponible, usando datos locales");
            future.complete(getSamplePlaces());
            return future;
        }

        Log.i(TAG, "Cargando lugares desde Supabase...");

        Call<List<Place>> call = apiService.getAllPlaces();
        call.enqueue(new Callback<List<Place>>() {
            @Override
            public void onResponse(Call<List<Place>> call, Response<List<Place>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Place> places = response.body();
                    Log.i(TAG, "Lugares cargados desde Supabase: " + places.size());

                    // Calcular distancias relativas para los datos reales
                    calculateSampleDistances(places);

                    future.complete(places);
                } else {
                    Log.w(TAG, "Respuesta no exitosa de Supabase: " + response.code() + " - " + response.message());
                    // Fallback a datos locales
                    future.complete(getSamplePlaces());
                }
            }

            @Override
            public void onFailure(Call<List<Place>> call, Throwable t) {
                Log.e(TAG, "Error conectando con Supabase", t);
                // Fallback a datos locales
                future.complete(getSamplePlaces());
            }
        });

        return future;
    }

    // Obtener lugares por tipo desde Supabase
    public CompletableFuture<List<Place>> getPlacesByType(PlaceType placeType) {
        CompletableFuture<List<Place>> future = new CompletableFuture<>();

        if (apiService == null) {
            Log.w(TAG, "API Service no disponible, filtrando datos locales");
            List<Place> filteredPlaces = new ArrayList<>();
            for (Place place : getSamplePlaces()) {
                if (place.getType() == placeType) {
                    filteredPlaces.add(place);
                }
            }
            future.complete(filteredPlaces);
            return future;
        }

        Log.i(TAG, "Buscando lugares por tipo en Supabase: " + placeType.name());

        Call<List<Place>> call = apiService.getPlacesByType(placeType.name());
        call.enqueue(new Callback<List<Place>>() {
            @Override
            public void onResponse(Call<List<Place>> call, Response<List<Place>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Place> places = response.body();
                    Log.i(TAG, "Lugares encontrados por tipo: " + places.size());
                    calculateSampleDistances(places);
                    future.complete(places);
                } else {
                    Log.w(TAG, "Error buscando por tipo: " + response.code());
                    // Fallback a filtrado local
                    List<Place> filteredPlaces = new ArrayList<>();
                    for (Place place : getSamplePlaces()) {
                        if (place.getType() == placeType) {
                            filteredPlaces.add(place);
                        }
                    }
                    future.complete(filteredPlaces);
                }
            }

            @Override
            public void onFailure(Call<List<Place>> call, Throwable t) {
                Log.e(TAG, "Error en búsqueda por tipo", t);
                // Fallback a filtrado local
                List<Place> filteredPlaces = new ArrayList<>();
                for (Place place : getSamplePlaces()) {
                    if (place.getType() == placeType) {
                        filteredPlaces.add(place);
                    }
                }
                future.complete(filteredPlaces);
            }
        });

        return future;
    }

    // Buscar lugares por texto en Supabase
    public CompletableFuture<List<Place>> searchPlaces(String query) {
        CompletableFuture<List<Place>> future = new CompletableFuture<>();

        if (apiService == null || query.trim().isEmpty()) {
            // Búsqueda local como fallback
            future.complete(searchPlacesLocally(query));
            return future;
        }

        Log.i(TAG, "Buscando en Supabase: " + query);

        // Construir query para búsqueda en múltiples campos de Supabase
        String searchQuery = String.format("(name.ilike.%%%s%%,category.ilike.%%%s%%,description.ilike.%%%s%%,what3words.ilike.%%%s%%)",
                query, query, query, query);

        Call<List<Place>> call = apiService.searchPlaces(searchQuery);
        call.enqueue(new Callback<List<Place>>() {
            @Override
            public void onResponse(Call<List<Place>> call, Response<List<Place>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Place> places = response.body();
                    Log.i(TAG, "Resultados de búsqueda: " + places.size());
                    calculateSampleDistances(places);
                    future.complete(places);
                } else {
                    Log.w(TAG, "Error en búsqueda: " + response.code());
                    future.complete(searchPlacesLocally(query));
                }
            }

            @Override
            public void onFailure(Call<List<Place>> call, Throwable t) {
                Log.e(TAG, "Error en búsqueda", t);
                future.complete(searchPlacesLocally(query));
            }
        });

        return future;
    }

    // Obtener lugares cercanos
    public CompletableFuture<List<Place>> getNearbyPlaces(double latitude, double longitude, double radiusKm) {
        CompletableFuture<List<Place>> future = new CompletableFuture<>();

        if (apiService == null) {
            future.complete(getNearbyPlacesLocally(latitude, longitude, radiusKm));
            return future;
        }

        Log.i(TAG, "Buscando lugares cercanos en Supabase");

        Call<List<Place>> call = apiService.getNearbyPlaces(latitude, longitude, radiusKm);
        call.enqueue(new Callback<List<Place>>() {
            @Override
            public void onResponse(Call<List<Place>> call, Response<List<Place>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Place> places = response.body();
                    Log.i(TAG, "Lugares cercanos encontrados: " + places.size());

                    // Calcular distancias reales
                    Location userLocation = new Location("user");
                    userLocation.setLatitude(latitude);
                    userLocation.setLongitude(longitude);

                    for (Place place : places) {
                        Location placeLocation = new Location("place");
                        placeLocation.setLatitude(place.getLatitude());
                        placeLocation.setLongitude(place.getLongitude());
                        float distance = userLocation.distanceTo(placeLocation);
                        place.setDistanceInMeters((int) distance);
                    }

                    // Ordenar por distancia
                    places.sort((a, b) -> Integer.compare(a.getDistanceInMeters(), b.getDistanceInMeters()));

                    future.complete(places);
                } else {
                    Log.w(TAG, "Error buscando cercanos: " + response.code());
                    future.complete(getNearbyPlacesLocally(latitude, longitude, radiusKm));
                }
            }

            @Override
            public void onFailure(Call<List<Place>> call, Throwable t) {
                Log.e(TAG, "Error buscando lugares cercanos", t);
                future.complete(getNearbyPlacesLocally(latitude, longitude, radiusKm));
            }
        });

        return future;
    }

    // Obtener lugar por ID desde Supabase
    public CompletableFuture<Place> getPlaceById(String placeId) {
        CompletableFuture<Place> future = new CompletableFuture<>();

        if (apiService == null) {
            for (Place place : getSamplePlaces()) {
                if (place.getId().equals(placeId)) {
                    future.complete(place);
                    return future;
                }
            }
            future.complete(null);
            return future;
        }

        Log.i(TAG, "Buscando lugar por ID en Supabase: " + placeId);

        Call<List<Place>> call = apiService.getPlaceById(placeId);
        call.enqueue(new Callback<List<Place>>() {
            @Override
            public void onResponse(Call<List<Place>> call, Response<List<Place>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Place place = response.body().get(0);
                    Log.i(TAG, "Lugar encontrado: " + place.getName());
                    future.complete(place);
                } else {
                    Log.w(TAG, "Lugar no encontrado: " + placeId);
                    future.complete(null);
                }
            }

            @Override
            public void onFailure(Call<List<Place>> call, Throwable t) {
                Log.e(TAG, "Error buscando lugar por ID", t);
                future.complete(null);
            }
        });

        return future;
    }

    // Métodos auxiliares para fallback local
    private List<Place> searchPlacesLocally(String query) {
        List<Place> filteredPlaces = new ArrayList<>();
        String lowercaseQuery = query.toLowerCase();

        for (Place place : getSamplePlaces()) {
            if (place.getName().toLowerCase().contains(lowercaseQuery) ||
                    place.getCategory().toLowerCase().contains(lowercaseQuery) ||
                    place.getDescription().toLowerCase().contains(lowercaseQuery) ||
                    place.getWhat3words().toLowerCase().contains(lowercaseQuery)) {
                filteredPlaces.add(place);
            }
        }
        return filteredPlaces;
    }

    private List<Place> getNearbyPlacesLocally(double latitude, double longitude, double radiusKm) {
        List<Place> nearbyPlaces = new ArrayList<>();
        Location userLocation = new Location("user");
        userLocation.setLatitude(latitude);
        userLocation.setLongitude(longitude);

        for (Place place : getSamplePlaces()) {
            Location placeLocation = new Location("place");
            placeLocation.setLatitude(place.getLatitude());
            placeLocation.setLongitude(place.getLongitude());

            float distance = userLocation.distanceTo(placeLocation);
            if (distance <= radiusKm * 1000) {
                place.setDistanceInMeters((int) distance);
                nearbyPlaces.add(place);
            }
        }

        nearbyPlaces.sort((a, b) -> Integer.compare(a.getDistanceInMeters(), b.getDistanceInMeters()));
        return nearbyPlaces;
    }

    private void calculateSampleDistances(List<Place> places) {
        // Asignar distancias de ejemplo para datos reales
        for (int i = 0; i < places.size(); i++) {
            Place place = places.get(i);
            place.setDistanceInMeters(50 + (i * 30)); // 50m, 80m, 110m, etc.
        }
    }

    // Datos de ejemplo (solo como fallback)
    private List<Place> getSamplePlaces() {
        List<Place> places = new ArrayList<>();

        // Aula A-101
        Place aula = new Place(
                "1",
                "Aula A-101",
                "Aula",
                "Aula de clases magistrales con capacidad para 40 estudiantes. Equipada con proyector y sistema de audio.",
                "música.ejemplo.libertad",
                true,
                120,
                PlaceType.CLASSROOM
        );
        aula.setLatitude(-0.210959);
        aula.setLongitude(-78.487259);
        aula.setCapacity(40);
        aula.setSchedule("Lunes a Viernes 7:00 - 22:00");
        aula.setBuildingName("Edificio Principal");
        aula.setFloorNumber(1);
        aula.setRoomCode("A-101");
        places.add(aula);

        // Laboratorio de Informática
        Place laboratorio = new Place(
                "2",
                "Laboratorio de Informática 1",
                "Laboratorio",
                "Laboratorio con 30 computadoras actualizadas, ideal para clases de programación y diseño.",
                "código.digital.futuro",
                false,
                85,
                PlaceType.LABORATORY
        );
        laboratorio.setLatitude(-0.211100);
        laboratorio.setLongitude(-78.487000);
        laboratorio.setCapacity(30);
        laboratorio.setSchedule("Lunes a Viernes 8:00 - 20:00");
        laboratorio.setBuildingName("Edificio de Tecnología");
        laboratorio.setFloorNumber(1);
        laboratorio.setRoomCode("LAB-101");
        places.add(laboratorio);

        // Biblioteca Central
        Place biblioteca = new Place(
                "3",
                "Biblioteca Central",
                "Biblioteca",
                "Biblioteca principal del campus con más de 50,000 libros y salas de estudio.",
                "silencio.libros.conocimiento",
                true,
                200,
                PlaceType.LIBRARY
        );
        biblioteca.setLatitude(-0.210800);
        biblioteca.setLongitude(-78.487500);
        biblioteca.setCapacity(150);
        biblioteca.setSchedule("Lunes a Viernes 6:00 - 23:00, Sábados 8:00 - 18:00");
        biblioteca.setBuildingName("Biblioteca Central");
        biblioteca.setFloorNumber(1);
        biblioteca.setRoomCode("BIB-001");
        places.add(biblioteca);

        return places;
    }
}