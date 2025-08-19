package com.example.lugarescomunes.api;

import android.util.Log;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ApiConfig {

    private static final String TAG = "ApiConfig";
    private static final String BASE_URL = "http://20.57.32.45:8080/api/";

    private static Retrofit retrofitInstance;
    private static LugaresApiService apiService;
    private static String authToken = null;

    // Crear cliente HTTP con interceptor para JWT y headers correctos
    private static OkHttpClient createHttpClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> {
            Log.d(TAG, "HTTP: " + message);
        });
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();
                        Request.Builder builder = original.newBuilder()
                                .header("Content-Type", "application/json")
                                .header("Accept", "application/json")
                                .header("User-Agent", "LugaresComunes-Android/1.0");

                        // Agregar token JWT si está disponible
                        if (authToken != null && !authToken.isEmpty()) {
                            builder.header("Authorization", "Bearer " + authToken);
                            Log.d(TAG, "Adding JWT token to request");
                        }

                        Request request = builder.build();

                        Log.d(TAG, "Request URL: " + request.url());
                        Log.d(TAG, "Request Method: " + request.method());

                        Response response = chain.proceed(request);

                        Log.d(TAG, "Response Code: " + response.code());
                        Log.d(TAG, "Response Message: " + response.message());

                        return response;
                    }
                })
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    // Obtener instancia de Retrofit
    public static synchronized Retrofit getRetrofitInstance() {
        if (retrofitInstance == null) {
            Log.i(TAG, "Creando nueva instancia de Retrofit con URL: " + BASE_URL);
            retrofitInstance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(createHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitInstance;
    }

    // Obtener servicio API
    public static synchronized LugaresApiService getApiService() {
        if (apiService == null) {
            apiService = getRetrofitInstance().create(LugaresApiService.class);
            Log.i(TAG, "Servicio API creado");
        }
        return apiService;
    }

    // Establecer token de autenticación
    public static void setAuthToken(String token) {
        authToken = token;
        Log.i(TAG, "Token de autenticación establecido: " + (token != null ? "***" + token.substring(Math.max(0, token.length() - 10)) : "null"));

        // Recrear instancias para usar el nuevo token
        retrofitInstance = null;
        apiService = null;
    }

    // Limpiar token (logout)
    public static void clearAuthToken() {
        Log.i(TAG, "Token de autenticación limpiado");
        authToken = null;
        retrofitInstance = null;
        apiService = null;
    }

    // Verificar si hay token
    public static boolean hasAuthToken() {
        boolean hasToken = authToken != null && !authToken.isEmpty();
        Log.d(TAG, "Has auth token: " + hasToken);
        return hasToken;
    }

    // Getters
    public static String getBaseUrl() {
        return BASE_URL;
    }

    public static String getAuthToken() {
        return authToken;
    }

    // Método para verificar conectividad
    public static void logConnectionInfo() {
        Log.i(TAG, "=== API Configuration ===");
        Log.i(TAG, "Base URL: " + BASE_URL);
        Log.i(TAG, "Has Auth Token: " + hasAuthToken());
        Log.i(TAG, "Retrofit Instance: " + (retrofitInstance != null ? "Created" : "Null"));
        Log.i(TAG, "API Service: " + (apiService != null ? "Created" : "Null"));
        Log.i(TAG, "========================");
    }
}