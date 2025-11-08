package inf311.grupo1.projetopratico;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;

import inf311.grupo1.projetopratico.services.MetricsDataProvider;

public class TelaLogin extends AppCompatActivity {

    private static final String TAG = "TelaLogin";
    
    // Firebase Manager
    private FirebaseManager firebaseManager;
    
    // UI Elements
    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvEsqueciSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tela_login);

        // Inicializar Firebase Manager
        firebaseManager = FirebaseManager.getInstance();
        
        // Limpar todos os caches ao iniciar a tela de login
        clearAllCaches();
        
        // Inicializar elementos da UI
        initializeUI();
        
        // Configurar listeners
        setupListeners();
        
        Log.d(TAG, "TelaLogin inicializada com Firebase Auth");
    }

    /**
     * Limpa todos os caches do sistema para garantir dados atualizados
     */
    private void clearAllCaches() {
        Log.d(TAG, "=== LIMPANDO TODOS OS CACHES NO LOGIN ===");
        
        try {
            // Limpar cache do MetricsDataProvider
            MetricsDataProvider metricsProvider = MetricsDataProvider.getInstance();
            metricsProvider.clearCache();
            Log.d(TAG, "✅ Cache do MetricsDataProvider limpo");
            
            // Forçar reset do App_main se disponível
            App_main app = (App_main) getApplication();
            if (app != null) {
                app.updated = false; // Forçar nova atualização dos dados
                Log.d(TAG, "✅ App_main marcado para atualização forçada");
            }
            
            // Limpar Data_master para evitar dados do usuário anterior
            Data_master.admin = false;
            Data_master.user_id = null;
            Log.d(TAG, "✅ Data_master limpo");
            
            Log.d(TAG, "=== LIMPEZA DE CACHES NO LOGIN CONCLUÍDA ===");
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao limpar caches no login", e);
        }
    }

    /**
     * Inicializa todos os elementos da interface do usuário
     */
    private void initializeUI() {
        etEmail = findViewById(R.id.email_edit);
        etPassword = findViewById(R.id.password_edit);
        btnLogin = findViewById(R.id.login_button);
        tvEsqueciSenha = findViewById(R.id.forgot_password);
    }

    /**
     * Configura os listeners para os elementos da UI
     */
    private void setupListeners() {
        // Listener para o botão de login
        if (btnLogin != null) {
            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performLogin();
                }
            });
        }
        
        // Listener para "Esqueci a senha"
        if (tvEsqueciSenha != null) {
            tvEsqueciSenha.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigateToEsqueciSenha();
                }
            });
        }
    }

    /**
     * Realiza o processo de login com Firebase usando FirebaseManager
     */
    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validar campos
        if (!validateInputs(email, password)) {
            return;
        }

        setLoadingState(true);

        // Fazer login usando FirebaseManager
        firebaseManager.signInWithEmail(email, password, new FirebaseManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                Log.d(TAG, "Login realizado com sucesso para: " + user.getEmail());
                Toast.makeText(TelaLogin.this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();
                
                // Buscar dados do usuário no Firestore após login bem-sucedido
                loadUserDataAndNavigate();
            }

            @Override
            public void onError(String errorMessage) {
                setLoadingState(false);
                Log.e(TAG, "Erro no login: " + errorMessage);
                Toast.makeText(TelaLogin.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Busca dados do usuário no Firestore e navega para o dashboard
     */
    private void loadUserDataAndNavigate() {
        firebaseManager.getCurrentUserData(new FirebaseManager.UserDataCallback() {
            @Override
            public void onUserDataLoaded(boolean isAdmin, String name, String email) {
                setLoadingState(false);
                
                Log.d(TAG, "Dados do usuário carregados - Admin: " + isAdmin + ", Nome: " + name);
                
                Data_master.admin = isAdmin;
                Data_master.user_id = firebaseManager.getCurrentUserUid();
                
                // Forçar atualização dos leads para este usuário após login
                forceUpdateLeadsDataAfterLogin();
                
                navigateToDashboard(isAdmin, name, email);
            }

            @Override
            public void onError(String errorMessage) {
                setLoadingState(false);
                Log.e(TAG, "Erro ao carregar dados do usuário: " + errorMessage);
                Toast.makeText(TelaLogin.this, "Erro ao carregar dados do usuário: " + errorMessage, Toast.LENGTH_LONG).show();
                
                firebaseManager.signOut();
            }
        });
    }

    /**
     * Força atualização dos dados dos leads para o usuário atual após login
     */
    private void forceUpdateLeadsDataAfterLogin() {
        Log.d(TAG, "=== FORÇANDO ATUALIZAÇÃO DOS LEADS APÓS LOGIN ===");
        
        try {
            App_main app = (App_main) getApplication();
            if (app != null) {
                // Executar em thread separada para não bloquear a UI
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            app.forceUpdate(); // Força nova busca na API
                            Log.d(TAG, "✅ Leads atualizados com sucesso após login");
                        } catch (Exception e) {
                            Log.e(TAG, "❌ Erro ao atualizar leads após login", e);
                        }
                    }
                }).start();
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao forçar atualização dos leads após login", e);
        }
    }

    /**
     * Valida os campos de entrada
     */
    private boolean validateInputs(String email, String password) {
        // Validar email
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email é obrigatório");
            etEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Digite um email válido");
            etEmail.requestFocus();
            return false;
        }

        // Validar senha
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Senha é obrigatória");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Senha deve ter pelo menos 6 caracteres");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Gerencia o estado de loading da interface
     */
    private void setLoadingState(boolean isLoading) {
        if (btnLogin != null) {
            btnLogin.setEnabled(!isLoading);
            btnLogin.setText(isLoading ? "Entrando..." : "Entrar");
        }
        
        if (etEmail != null) {
            etEmail.setEnabled(!isLoading);
        }
        
        if (etPassword != null) {
            etPassword.setEnabled(!isLoading);
        }
    }

    
    private void navigateToDashboard(boolean isAdmin, String name, String email) {
        Intent intent = new Intent(TelaLogin.this, MainActivityNova.class);
        
        intent.putExtra("is_admin", isAdmin);
        intent.putExtra("user_email", email);
        intent.putExtra("user_name", name);
        intent.putExtra("user_uid", firebaseManager.getCurrentUserUid());
        
        Log.d(TAG, "Navegando para dashboard - Admin: " + isAdmin + ", Email: " + email);
        
        startActivity(intent);
        finish();
    }

    /**
     * Navega para a tela de recuperação de senha
     */
    private void navigateToEsqueciSenha() {
        Intent intent = new Intent(TelaLogin.this, EsqueciSenhaActivity.class);
        startActivity(intent);
        Log.d(TAG, "Navegando para tela de recuperação de senha");
    }

}
