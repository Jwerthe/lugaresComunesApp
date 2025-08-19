package com.example.lugarescomunes;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.lugarescomunes.repository.AuthRepository;
import com.example.lugarescomunes.models.api.UserResponse;

public class AuthActivity extends AppCompatActivity {

    private static final String TAG = "AuthActivity";

    // Views generales
    private ProgressBar loadingProgressBar;
    private TextView continueWithoutRegistrationTextView;

    // Tab views
    private Button loginTabButton;
    private Button registerTabButton;

    // Form containers
    private LinearLayout loginFormLayout;
    private LinearLayout registerFormLayout;

    // Login views
    private EditText loginEmailEditText;
    private EditText loginPasswordEditText;
    private ImageView togglePasswordVisibility;
    private CheckBox rememberMeCheckBox;
    private TextView forgotPasswordTextView;
    private Button loginButton;

    // Register views
    private EditText registerFullNameEditText;
    private EditText registerEmailEditText;
    private EditText registerStudentIdEditText;
    private EditText registerPasswordEditText;
    private ImageView toggleRegisterPasswordVisibility;
    private Button registerButton;

    // State
    private boolean isLoginMode = true;
    private boolean isPasswordVisible = false;
    private boolean isRegisterPasswordVisible = false;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        Log.d(TAG, "=== AUTH ACTIVITY INICIADA ===");

        // Inicializar repositorio de autenticación
        authRepository = AuthRepository.getInstance(this);

        // Verificar si ya está logueado
        if (authRepository.isLoggedIn()) {
            Log.d(TAG, "Usuario ya logueado, navegando a MainActivity");
            navigateToMainActivity();
            return;
        }

        initializeViews();
        setupClickListeners();
        setupInitialState();
    }

    private void initializeViews() {
        // Views generales
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        continueWithoutRegistrationTextView = findViewById(R.id.continueWithoutRegistrationTextView);

        // Tab views
        loginTabButton = findViewById(R.id.loginTabButton);
        registerTabButton = findViewById(R.id.registerTabButton);

        // Form containers
        loginFormLayout = findViewById(R.id.loginFormLayout);
        registerFormLayout = findViewById(R.id.registerFormLayout);

        // Login views
        loginEmailEditText = findViewById(R.id.loginEmailEditText);
        loginPasswordEditText = findViewById(R.id.loginPasswordEditText);
        togglePasswordVisibility = findViewById(R.id.togglePasswordVisibility);
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        loginButton = findViewById(R.id.loginButton);

        // Register views
        registerFullNameEditText = findViewById(R.id.registerFullNameEditText);
        registerEmailEditText = findViewById(R.id.registerEmailEditText);
        registerStudentIdEditText = findViewById(R.id.registerStudentIdEditText);
        registerPasswordEditText = findViewById(R.id.registerPasswordEditText);
        toggleRegisterPasswordVisibility = findViewById(R.id.toggleRegisterPasswordVisibility);
        registerButton = findViewById(R.id.registerButton);
    }

    private void setupClickListeners() {
        // Tab clicks
        loginTabButton.setOnClickListener(v -> switchToLoginMode());
        registerTabButton.setOnClickListener(v -> switchToRegisterMode());

        // Password visibility toggles
        togglePasswordVisibility.setOnClickListener(v -> togglePasswordVisibility());
        toggleRegisterPasswordVisibility.setOnClickListener(v -> toggleRegisterPasswordVisibility());

        // Form buttons
        loginButton.setOnClickListener(v -> performLogin());
        registerButton.setOnClickListener(v -> performRegister());

        // Other actions
        continueWithoutRegistrationTextView.setOnClickListener(v -> continueWithoutRegistration());
        forgotPasswordTextView.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void setupInitialState() {
        switchToLoginMode();
    }

    private void switchToLoginMode() {
        isLoginMode = true;

        // Update tab appearance
        loginTabButton.setBackgroundResource(R.drawable.tab_selected_background);
        loginTabButton.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        registerTabButton.setBackgroundResource(android.R.color.transparent);
        registerTabButton.setTextColor(ContextCompat.getColor(this, R.color.gray_text));

        // Show/hide forms
        loginFormLayout.setVisibility(View.VISIBLE);
        registerFormLayout.setVisibility(View.GONE);
    }

    private void switchToRegisterMode() {
        isLoginMode = false;

        // Update tab appearance
        registerTabButton.setBackgroundResource(R.drawable.tab_selected_background);
        registerTabButton.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        loginTabButton.setBackgroundResource(android.R.color.transparent);
        loginTabButton.setTextColor(ContextCompat.getColor(this, R.color.gray_text));

        // Show/hide forms
        loginFormLayout.setVisibility(View.GONE);
        registerFormLayout.setVisibility(View.VISIBLE);
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            loginPasswordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            togglePasswordVisibility.setImageResource(R.drawable.ic_visibility_off);
        } else {
            loginPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            togglePasswordVisibility.setImageResource(R.drawable.ic_visibility);
        }

        // Move cursor to end
        loginPasswordEditText.setSelection(loginPasswordEditText.getText().length());
    }

    private void toggleRegisterPasswordVisibility() {
        isRegisterPasswordVisible = !isRegisterPasswordVisible;

        if (isRegisterPasswordVisible) {
            registerPasswordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            toggleRegisterPasswordVisibility.setImageResource(R.drawable.ic_visibility_off);
        } else {
            registerPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            toggleRegisterPasswordVisibility.setImageResource(R.drawable.ic_visibility);
        }

        // Move cursor to end
        registerPasswordEditText.setSelection(registerPasswordEditText.getText().length());
    }

    private void performLogin() {
        String email = loginEmailEditText.getText().toString().trim();
        String password = loginPasswordEditText.getText().toString();

        // Validación básica
        if (email.isEmpty()) {
            loginEmailEditText.setError("Email es requerido");
            loginEmailEditText.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            loginPasswordEditText.setError("Contraseña es requerida");
            loginPasswordEditText.requestFocus();
            return;
        }

        // Validar formato de email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            loginEmailEditText.setError("Email inválido");
            loginEmailEditText.requestFocus();
            return;
        }

        Log.d(TAG, "Iniciando login para: " + email);

        // Mostrar loading
        setLoading(true);

        // Realizar login
        authRepository.login(email, password)
                .thenAccept(result -> {
                    runOnUiThread(() -> {
                        setLoading(false);

                        Log.d(TAG, "Resultado de login - Success: " + result.isSuccess() + ", Message: " + result.getMessage());

                        if (result.isSuccess()) {
                            UserResponse user = result.getUser();
                            String welcomeMessage = "¡Bienvenido" + (user != null ? " " + user.getFullName() : "") + "!";
                            Toast.makeText(this, welcomeMessage, Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Login exitoso, navegando a UpdatedMainActivity");
                            navigateToMainActivity();
                        } else {
                            String errorMessage = result.getMessage();
                            if (errorMessage == null || errorMessage.isEmpty()) {
                                errorMessage = "Error de autenticación";
                            }
                            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                            Log.w(TAG, "Login fallido: " + errorMessage);
                        }
                    });
                })
                .exceptionally(throwable -> {
                    runOnUiThread(() -> {
                        setLoading(false);
                        Log.e(TAG, "Error inesperado en login", throwable);
                        Toast.makeText(this, "Error inesperado. Intenta de nuevo.", Toast.LENGTH_SHORT).show();
                    });
                    return null;
                });
    }

    private void performRegister() {
        String fullName = registerFullNameEditText.getText().toString().trim();
        String email = registerEmailEditText.getText().toString().trim();
        String studentId = registerStudentIdEditText.getText().toString().trim();
        String password = registerPasswordEditText.getText().toString();

        // Validación básica
        if (fullName.isEmpty()) {
            registerFullNameEditText.setError("Nombre completo es requerido");
            registerFullNameEditText.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            registerEmailEditText.setError("Email es requerido");
            registerEmailEditText.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            registerPasswordEditText.setError("Contraseña es requerida");
            registerPasswordEditText.requestFocus();
            return;
        }

        // Validar formato de email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            registerEmailEditText.setError("Email inválido");
            registerEmailEditText.requestFocus();
            return;
        }

        // Validar longitud de contraseña
        if (password.length() < 6) {
            registerPasswordEditText.setError("La contraseña debe tener al menos 6 caracteres");
            registerPasswordEditText.requestFocus();
            return;
        }

        Log.d(TAG, "Iniciando registro para: " + email);

        // Mostrar loading
        setLoading(true);

        // Realizar registro
        authRepository.register(email, password, fullName, studentId.isEmpty() ? null : studentId)
                .thenAccept(result -> {
                    runOnUiThread(() -> {
                        setLoading(false);

                        Log.d(TAG, "Resultado de registro - Success: " + result.isSuccess() + ", Message: " + result.getMessage());

                        if (result.isSuccess()) {
                            UserResponse user = result.getUser();
                            String welcomeMessage = "¡Registro exitoso! Bienvenido" + (user != null ? " " + user.getFullName() : "") + "!";
                            Toast.makeText(this, welcomeMessage, Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Registro exitoso, navegando a UpdatedMainActivity");
                            navigateToMainActivity();
                        } else {
                            String errorMessage = result.getMessage();
                            if (errorMessage == null || errorMessage.isEmpty()) {
                                errorMessage = "Error en el registro";
                            }
                            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                            Log.w(TAG, "Registro fallido: " + errorMessage);
                        }
                    });
                })
                .exceptionally(throwable -> {
                    runOnUiThread(() -> {
                        setLoading(false);
                        Log.e(TAG, "Error inesperado en registro", throwable);
                        Toast.makeText(this, "Error inesperado en el registro. Intenta de nuevo.", Toast.LENGTH_SHORT).show();
                    });
                    return null;
                });
    }

    private void continueWithoutRegistration() {
        // Navegar directamente a MainActivity sin autenticación
        // El usuario funcionará como VISITOR
        Toast.makeText(this, "Continuando como visitante", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "Usuario continúa como visitante, navegando a UpdatedMainActivity");
        navigateToMainActivity();
    }

    private void showForgotPasswordDialog() {
        // Por ahora solo mostrar un mensaje
        Toast.makeText(this, "Funcionalidad de recuperación de contraseña próximamente", Toast.LENGTH_LONG).show();
    }

    private void setLoading(boolean loading) {
        loadingProgressBar.setVisibility(loading ? View.VISIBLE : View.GONE);

        // Deshabilitar botones mientras carga
        loginButton.setEnabled(!loading);
        registerButton.setEnabled(!loading);
        continueWithoutRegistrationTextView.setEnabled(!loading);

        // Cambiar texto del botón activo
        if (isLoginMode) {
            loginButton.setText(loading ? "Iniciando sesión..." : "Login");
        } else {
            registerButton.setText(loading ? "Registrando..." : "Register");
        }
    }

    private void navigateToMainActivity() {
        Log.d(TAG, "Navegando a UpdatedMainActivity");
        // ✅ CORRECCIÓN: Navegar a UpdatedMainActivity que está declarada en AndroidManifest.xml
        Intent intent = new Intent(this, UpdatedMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Prevenir que el usuario regrese al splash accidentalmente
        // En su lugar, mover la app al background
        moveTaskToBack(true);
    }
}