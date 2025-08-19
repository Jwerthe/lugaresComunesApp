package com.example.lugarescomunes.api;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ApiConfig {

    private static final String BASE_URL = "http://20.57.32.45:8080/api/";

    private static Retrofit retrofitInstance;
    private static LugaresApiService apiService;
    private static String authToken = null;

    // Crear cliente HTTP con interceptor para JWT
    private static OkHttpClient createHttpClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();
                        Request.Builder builder = original.newBuilder()
                                .header("Content-Type", "application/json");

                        // Agregar token JWT si está disponible
                        if (authToken != null && !authToken.isEmpty()) {
                            builder.header("Authorization", "Bearer " + authToken);
                        }

                        Request request = builder.build();
                        return chain.proceed(request);
                    }
                })
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    // Obtener instancia de Retrofit
    public static synchronized Retrofit getRetrofitInstance() {
        if (retrofitInstance == null) {
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
        }
        return apiService;
    }

    // Establecer token de autenticación
    public static void setAuthToken(String token) {
        authToken = token;
        // Recrear instancias para usar el nuevo token
        retrofitInstance = null;
        apiService = null;
    }

    // Limpiar token (logout)
    public static void clearAuthToken() {
        authToken = null;
        retrofitInstance = null;
        apiService = null;
    }

    // Verificar si hay token
    public static boolean hasAuthToken() {
        return authToken != null && !authToken.isEmpty();
    }

    // Getters
    public static String getBaseUrl() {
        return BASE_URL;
    }

    public static String getAuthToken() {
        return authToken;
    }
}