package com.example.lugarescomunes.repository;

import android.util.Log;
import com.example.lugarescomunes.api.ApiConfig;
import com.example.lugarescomunes.api.LugaresApiService;
import com.example.lugarescomunes.models.api.ApiResponse;
import com.example.lugarescomunes.models.api.RouteResponse;
import com.example.lugarescomunes.models.api.RouteDetailsResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RoutesRepository {

    private static final String TAG = "RoutesRepository";
    private static RoutesRepository instance;
    private LugaresApiService apiService;

    // Cache para rutas por destino
    private static final long CACHE_DURATION = 2 * 60 * 1000; // 2 minutos
    private final java.util.Map<String, List<RouteResponse>> routesCache = new java.util.HashMap<>();
    private final java.util.Map<String, Long> cacheTimestamps = new java.util.HashMap<>();

    private RoutesRepository() {
        this.apiService = ApiConfig.getApiService();
        Log.i(TAG, "RoutesRepository inicializado");
    }

    public static synchronized RoutesRepository getInstance() {
        if (instance == null) {
            instance = new RoutesRepository();
        }
        return instance;
    }

    // Obtener rutas hacia un destino específico
    public CompletableFuture<List<RouteResponse>> getRoutesToDestination(String destinationId) {
        CompletableFuture<List<RouteResponse>> future = new CompletableFuture<>();

        if (apiService == null || destinationId == null || destinationId.trim().isEmpty()) {
            Log.e(TAG, "Parámetros inválidos para obtener rutas");
            future.completeExceptionally(new RuntimeException("Parámetros inválidos"));
            return future;
        }

        // Verificar cache
        if (isRouteCacheValid(destinationId)) {
            List<RouteResponse> cachedRoutes = routesCache.get(destinationId);
            Log.d(TAG, "Retornando rutas desde cache para destino: " + destinationId + " (cantidad: " + cachedRoutes.size() + ")");
            future.complete(new ArrayList<>(cachedRoutes));
            return future;
        }

        Log.i(TAG, "Cargando rutas desde API para destino: " + destinationId);

        Call<ApiResponse<List<RouteResponse>>> call = apiService.getRoutesToPlace(destinationId);
        call.enqueue(new Callback<ApiResponse<List<RouteResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<RouteResponse>>> call, Response<ApiResponse<List<RouteResponse>>> response) {
                try {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        List<RouteResponse> routes = response.body().getData();

                        if (routes == null) {
                            routes = new ArrayList<>();
                        }

                        Log.i(TAG, "Rutas cargadas exitosamente para destino " + destinationId + ": " + routes.size() + " rutas");

                        // Actualizar cache
                        updateRouteCache(destinationId, routes);

                        future.complete(routes);
                    } else {
                        String errorMsg = "Error obteniendo rutas: " + response.code();
                        if (response.body() != null && response.body().getMessage() != null) {
                            errorMsg += " - " + response.body().getMessage();
                        }
                        Log.w(TAG, errorMsg);
                        future.completeExceptionally(new RuntimeException(errorMsg));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error procesando respuesta de rutas", e);
                    future.completeExceptionally(e);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<RouteResponse>>> call, Throwable t) {
                Log.e(TAG, "Error en llamada para obtener rutas a destino: " + destinationId, t);
                future.completeExceptionally(new RuntimeException("Error de conexión obteniendo rutas", t));
            }
        });

        return future;
    }

    // Obtener detalles de una ruta específica
    public CompletableFuture<RouteResponse> getRouteDetails(String routeId) {
        CompletableFuture<RouteResponse> future = new CompletableFuture<>();

        if (apiService == null || routeId == null || routeId.trim().isEmpty()) {
            future.completeExceptionally(new RuntimeException("Parámetros inválidos"));
            return future;
        }

        Log.i(TAG, "Obteniendo detalles de ruta: " + routeId);

        // ✅ CORRECCIÓN: Ahora con el import correcto
        Call<ApiResponse<RouteDetailsResponse>> call = apiService.getRouteDetails(routeId);
        call.enqueue(new Callback<ApiResponse<RouteDetailsResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<RouteDetailsResponse>> call, Response<ApiResponse<RouteDetailsResponse>> response) {
                try {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        RouteDetailsResponse routeDetails = response.body().getData();
                        Log.i(TAG, "Detalles de ruta obtenidos exitosamente: " + routeDetails.getName());
                        // RouteDetailsResponse extiende RouteResponse, así que podemos retornar directamente
                        future.complete(routeDetails);
                    } else {
                        String errorMsg = "Ruta no encontrada: " + response.code();
                        Log.w(TAG, errorMsg);
                        future.completeExceptionally(new RuntimeException(errorMsg));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error procesando detalles de ruta", e);
                    future.completeExceptionally(e);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<RouteDetailsResponse>> call, Throwable t) {
                Log.e(TAG, "Error obteniendo detalles de ruta", t);
                future.completeExceptionally(new RuntimeException("Error de conexión", t));
            }
        });

        return future;
    }

    // Buscar ruta más cercana al usuario
    public CompletableFuture<RouteResponse> getNearestRoute(double latitude, double longitude, String destinationId) {
        CompletableFuture<RouteResponse> future = new CompletableFuture<>();

        if (apiService == null || destinationId == null || destinationId.trim().isEmpty()) {
            future.completeExceptionally(new RuntimeException("Parámetros inválidos"));
            return future;
        }

        Log.i(TAG, "Buscando ruta más cercana a destino: " + destinationId + " desde posición: " + latitude + ", " + longitude);

        Call<ApiResponse<RouteResponse>> call = apiService.getNearestRoute(latitude, longitude, destinationId);
        call.enqueue(new Callback<ApiResponse<RouteResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<RouteResponse>> call, Response<ApiResponse<RouteResponse>> response) {
                try {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        RouteResponse route = response.body().getData();
                        Log.i(TAG, "Ruta más cercana encontrada: " + route.getName());
                        future.complete(route);
                    } else {
                        String errorMsg = "No se encontró ruta cercana: " + response.code();
                        Log.w(TAG, errorMsg);
                        future.completeExceptionally(new RuntimeException(errorMsg));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error procesando ruta más cercana", e);
                    future.completeExceptionally(e);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<RouteResponse>> call, Throwable t) {
                Log.e(TAG, "Error buscando ruta más cercana", t);
                future.completeExceptionally(new RuntimeException("Error de conexión", t));
            }
        });

        return future;
    }

    // Cache management
    private boolean isRouteCacheValid(String destinationId) {
        if (!routesCache.containsKey(destinationId) || !cacheTimestamps.containsKey(destinationId)) {
            return false;
        }

        long cacheAge = System.currentTimeMillis() - cacheTimestamps.get(destinationId);
        boolean valid = cacheAge < CACHE_DURATION;

        Log.d(TAG, "Cache de rutas para " + destinationId + " es válido: " + valid + " (edad: " + cacheAge + "ms)");
        return valid;
    }

    private void updateRouteCache(String destinationId, List<RouteResponse> routes) {
        routesCache.put(destinationId, new ArrayList<>(routes));
        cacheTimestamps.put(destinationId, System.currentTimeMillis());
        Log.d(TAG, "Cache de rutas actualizado para destino " + destinationId + " con " + routes.size() + " rutas");
    }

    // Limpiar cache específico
    public void clearRouteCache(String destinationId) {
        routesCache.remove(destinationId);
        cacheTimestamps.remove(destinationId);
        Log.i(TAG, "Cache de rutas limpiado para destino: " + destinationId);
    }

    // Limpiar todo el cache
    public void clearAllCache() {
        routesCache.clear();
        cacheTimestamps.clear();
        Log.i(TAG, "Todo el cache de rutas limpiado");
    }

    // Health check
    public CompletableFuture<Boolean> checkRoutesApiHealth() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        if (apiService == null) {
            future.complete(false);
            return future;
        }

        Call<ApiResponse<Object>> call = apiService.routesHealth();
        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                boolean healthy = response.isSuccessful();
                Log.i(TAG, "Routes API Health check: " + (healthy ? "OK" : "ERROR"));
                future.complete(healthy);
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                Log.w(TAG, "Routes API Health check failed", t);
                future.complete(false);
            }
        });

        return future;
    }
}
