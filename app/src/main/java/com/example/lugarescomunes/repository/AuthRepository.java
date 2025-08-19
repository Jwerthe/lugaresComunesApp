package com.example.lugarescomunes.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.lugarescomunes.api.ApiConfig;
import com.example.lugarescomunes.api.LugaresApiService;
import com.example.lugarescomunes.models.api.*;

import java.util.concurrent.CompletableFuture;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private static final String TAG = "AuthRepository";
    private static final String PREFS_NAME = "LugaresComunes";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_TYPE = "user_type";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private static AuthRepository instance;
    private LugaresApiService apiService;
    private SharedPreferences sharedPreferences;
    private boolean isLoggedIn = false;
    private UserResponse currentUser = null;

    private AuthRepository(Context context) {
        apiService = ApiConfig.getApiService();
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadUserSession();
        Log.i(TAG, "AuthRepository inicializado");
    }

    public static synchronized AuthRepository getInstance(Context context) {
        if (instance == null) {
            instance = new AuthRepository(context.getApplicationContext());
        }
        return instance;
    }

    // Cargar sesión de usuario desde SharedPreferences
    private void loadUserSession() {
        isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
        if (isLoggedIn) {
            String token = sharedPreferences.getString(KEY_TOKEN, null);
            if (token != null) {
                ApiConfig.setAuthToken(token);

                // Recrear objeto de usuario
                currentUser = new UserResponse();
                currentUser.setId(sharedPreferences.getString(KEY_USER_ID, ""));
                currentUser.setEmail(sharedPreferences.getString(KEY_USER_EMAIL, ""));
                currentUser.setFullName(sharedPreferences.getString(KEY_USER_NAME, ""));
                currentUser.setUserType(sharedPreferences.getString(KEY_USER_TYPE, "VISITOR"));

                Log.i(TAG, "Sesión de usuario cargada: " + currentUser.getEmail());
            } else {
                // Si no hay token, limpiar estado
                clearUserSession();
            }
        }
    }

    // Guardar sesión de usuario
    private void saveUserSession(String token, UserResponse user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_NAME, user.getFullName());
        editor.putString(KEY_USER_TYPE, user.getUserType());
        editor.apply();

        isLoggedIn = true;
        currentUser = user;
        ApiConfig.setAuthToken(token);

        Log.i(TAG, "Sesión de usuario guardada: " + user.getEmail());
    }

    // Limpiar sesión de usuario
    private void clearUserSession() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_IS_LOGGED_IN);
        editor.remove(KEY_TOKEN);
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_USER_EMAIL);
        editor.remove(KEY_USER_NAME);
        editor.remove(KEY_USER_TYPE);
        editor.apply();

        isLoggedIn = false;
        currentUser = null;
        ApiConfig.clearAuthToken();

        Log.i(TAG, "Sesión de usuario limpiada");
    }

    // Login
    public CompletableFuture<AuthResult> login(String email, String password) {
        CompletableFuture<AuthResult> future = new CompletableFuture<>();

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            future.complete(new AuthResult(false, "Email y contraseña son requeridos", null));
            return future;
        }

        Log.i(TAG, "Intentando login para: " + email);

        LoginRequest loginRequest = new LoginRequest(email.trim(), password);
        Call<ApiResponse<AuthResponse>> call = apiService.login(loginRequest);

        call.enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<AuthResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        AuthResponse authResponse = apiResponse.getData();
                        saveUserSession(authResponse.getToken(), authResponse.getUser());

                        Log.i(TAG, "Login exitoso para: " + email);
                        future.complete(new AuthResult(true, "Login exitoso", authResponse.getUser()));
                    } else {
                        Log.w(TAG, "Login fallido: " + apiResponse.getMessage());
                        future.complete(new AuthResult(false, apiResponse.getMessage(), null));
                    }
                } else {
                    Log.w(TAG, "Error en respuesta de login: " + response.code());
                    future.complete(new AuthResult(false, "Error en el servidor", null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                Log.e(TAG, "Error en login", t);
                future.complete(new AuthResult(false, "Error de conexión", null));
            }
        });

        return future;
    }

    // Registro
    public CompletableFuture<AuthResult> register(String email, String password, String fullName, String studentId) {
        CompletableFuture<AuthResult> future = new CompletableFuture<>();

        if (email == null || email.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                fullName == null || fullName.trim().isEmpty()) {
            future.complete(new AuthResult(false, "Todos los campos son requeridos", null));
            return future;
        }

        if (password.length() < 6) {
            future.complete(new AuthResult(false, "La contraseña debe tener al menos 6 caracteres", null));
            return future;
        }

        Log.i(TAG, "Intentando registro para: " + email);

        RegisterRequest registerRequest = new RegisterRequest(email.trim(), password, fullName.trim());
        if (studentId != null && !studentId.trim().isEmpty()) {
            registerRequest.setStudentId(studentId.trim());
        }
        registerRequest.setUserType("VISITOR"); // Por defecto VISITOR

        Call<ApiResponse<AuthResponse>> call = apiService.register(registerRequest);

        call.enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<AuthResponse> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        AuthResponse authResponse = apiResponse.getData();
                        saveUserSession(authResponse.getToken(), authResponse.getUser());

                        Log.i(TAG, "Registro exitoso para: " + email);
                        future.complete(new AuthResult(true, "Registro exitoso", authResponse.getUser()));
                    } else {
                        Log.w(TAG, "Registro fallido: " + apiResponse.getMessage());
                        future.complete(new AuthResult(false, apiResponse.getMessage(), null));
                    }
                } else {
                    Log.w(TAG, "Error en respuesta de registro: " + response.code());
                    future.complete(new AuthResult(false, "Error en el servidor", null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                Log.e(TAG, "Error en registro", t);
                future.complete(new AuthResult(false, "Error de conexión", null));
            }
        });

        return future;
    }

    // Validar email
    public CompletableFuture<Boolean> validateEmail(String email) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        if (email == null || email.trim().isEmpty()) {
            future.complete(false);
            return future;
        }

        Call<ApiResponse<EmailValidationResponse>> call = apiService.validateEmail(email.trim());
        call.enqueue(new Callback<ApiResponse<EmailValidationResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<EmailValidationResponse>> call, Response<ApiResponse<EmailValidationResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    boolean available = response.body().getData().isAvailable();
                    future.complete(available);
                } else {
                    future.complete(false);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<EmailValidationResponse>> call, Throwable t) {
                Log.e(TAG, "Error validando email", t);
                future.complete(false);
            }
        });

        return future;
    }

    // Obtener usuario actual
    public CompletableFuture<UserResponse> getCurrentUser() {
        CompletableFuture<UserResponse> future = new CompletableFuture<>();

        if (!isLoggedIn) {
            future.complete(null);
            return future;
        }

        Call<ApiResponse<UserResponse>> call = apiService.getCurrentUser();
        call.enqueue(new Callback<ApiResponse<UserResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    UserResponse user = response.body().getData();
                    currentUser = user;
                    future.complete(user);
                } else {
                    // Token posiblemente expirado
                    clearUserSession();
                    future.complete(null);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                Log.e(TAG, "Error obteniendo usuario actual", t);
                future.complete(currentUser); // Retornar el usuario en caché
            }
        });

        return future;
    }

    // Logout
    public void logout() {
        Log.i(TAG, "Cerrando sesión de usuario");
        clearUserSession();
    }

    // Getters
    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public UserResponse getCurrentUserSync() {
        return currentUser;
    }

    public boolean isVisitor() {
        return currentUser == null || "VISITOR".equals(currentUser.getUserType());
    }

    public boolean isStudent() {
        return currentUser != null && "STUDENT".equals(currentUser.getUserType());
    }

    public boolean isAdmin() {
        return currentUser != null && "ADMIN".equals(currentUser.getUserType());
    }

    // Clase de resultado de autenticación
    public static class AuthResult {
        private final boolean success;
        private final String message;
        private final UserResponse user;

        public AuthResult(boolean success, String message, UserResponse user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public UserResponse getUser() {
            return user;
        }
    }
}