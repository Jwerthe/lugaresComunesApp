package com.example.lugarescomunes.config;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class SupabaseConfig {

    // Tus credenciales reales de Supabase
    private static final String SUPABASE_URL = "https://rjiczgicurzsckituhik.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJqaWN6Z2ljdXJ6c2NraXR1aGlrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTA4Mzk4NzksImV4cCI6MjA2NjQxNTg3OX0.RF2Vmna6wch7F8lmrOLgRLC2EN0VYkWqfQl_gaGjVLM";

    // URL base para la API REST de Supabase
    private static final String BASE_URL = SUPABASE_URL + "/rest/v1/";

    private static Retrofit retrofitInstance;
    private static SupabaseApiService apiService;

    // Crear cliente HTTP con headers de autenticación
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
                                .header("apikey", SUPABASE_ANON_KEY)
                                .header("Authorization", "Bearer " + SUPABASE_ANON_KEY)
                                .header("Content-Type", "application/json")
                                .header("Prefer", "return=representation");

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
    public static synchronized SupabaseApiService getApiService() {
        if (apiService == null) {
            apiService = getRetrofitInstance().create(SupabaseApiService.class);
        }
        return apiService;
    }

    // Getters para las configuraciones
    public static String getSupabaseUrl() {
        return SUPABASE_URL;
    }

    public static String getSupabaseAnonKey() {
        return SUPABASE_ANON_KEY;
    }

    // Verificar si está configurado correctamente
    public static boolean isConfigured() {
        return SUPABASE_URL != null && !SUPABASE_URL.isEmpty() &&
                SUPABASE_ANON_KEY != null && !SUPABASE_ANON_KEY.isEmpty();
    }

    // Método para logging
    public static void logStatus() {
        if (isConfigured()) {
            android.util.Log.i("SupabaseConfig", "Supabase configurado correctamente: " + SUPABASE_URL);
        } else {
            android.util.Log.w("SupabaseConfig", "Supabase no configurado correctamente");
        }
    }
}