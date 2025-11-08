package inf311.grupo1.projetopratico;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;

import inf311.grupo1.projetopratico.services.MetricsDataProvider;

/**
 * Activity de splash que verifica o estado de autenticação
 * e redireciona para a tela apropriada
 */
public class SplashActivity extends AppCompatActivity {
    
    private static final String TAG = "SplashActivity";
    
    private FirebaseManager firebaseManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        Log.d(TAG, "SplashActivity iniciada");
        
        firebaseManager = FirebaseManager.getInstance();
        
        // Limpar todos os caches antes de verificar autenticação
        clearAllCaches();
        
        checkAuthenticationAndNavigate();
    }
    
    /**
     * Limpa todos os caches do sistema para garantir dados atualizados
     */
    private void clearAllCaches() {
        Log.d(TAG, "=== LIMPANDO TODOS OS CACHES ===");
        
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
            
            Log.d(TAG, "=== LIMPEZA DE CACHES CONCLUÍDA ===");
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao limpar caches", e);
        }
    }
    
    /**
     * Verifica o estado de autenticação e navega para a tela apropriada
     */
    private void checkAuthenticationAndNavigate() {
        Log.d(TAG, "Verificando estado de autenticação...");
        
        firebaseManager.checkCurrentUser(new FirebaseManager.UserCheckCallback() {
            @Override
            public void onUserLoggedIn(FirebaseUser user) {
                Log.d(TAG, "Usuário já está logado: " + user.getEmail());
                // Carregar dados do usuário e navegar para o dashboard
                loadUserDataAndNavigateToDashboard();
            }

            @Override
            public void onUserNotLoggedIn() {
                Log.d(TAG, "Nenhum usuário logado - navegando para login");
                // Navegar para tela de login
                navigateToLogin();
            }
        });
    }
    
    /**
     * Carrega dados do usuário e navega para o dashboard
     */
    private void loadUserDataAndNavigateToDashboard() {
        firebaseManager.getCurrentUserData(new FirebaseManager.UserDataCallback() {
            @Override
            public void onUserDataLoaded(boolean isAdmin, String name, String email) {
                Log.d(TAG, "Dados do usuário carregados - Admin: " + isAdmin + ", Nome: " + name);
                
                // Atualizar variáveis globais
                Data_master.admin = isAdmin;
                Data_master.user_id = firebaseManager.getCurrentUserUid();
                
                // Forçar atualização dos leads para este usuário
                forceUpdateLeadsData();
                
                // Navegar para o dashboard com os dados do usuário
                navigateToDashboard(isAdmin, name, email);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Erro ao carregar dados do usuário: " + errorMessage);
                // Em caso de erro, fazer logout e ir para login
                firebaseManager.signOut();
                navigateToLogin();
            }
        });
    }
    
    /**
     * Força atualização dos dados dos leads para o usuário atual
     */
    private void forceUpdateLeadsData() {
        Log.d(TAG, "=== FORÇANDO ATUALIZAÇÃO DOS LEADS ===");
        
        try {
            App_main app = (App_main) getApplication();
            if (app != null) {
                // Executar em thread separada para não bloquear a UI
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            app.forceUpdate(); // Força nova busca na API
                            Log.d(TAG, "✅ Leads atualizados com sucesso na SplashScreen");
                        } catch (Exception e) {
                            Log.e(TAG, "❌ Erro ao atualizar leads na SplashScreen", e);
                        }
                    }
                }).start();
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao forçar atualização dos leads", e);
        }
    }
    
    /**
     * Navega para a tela de login
     */
    private void navigateToLogin() {
        Intent intent = new Intent(SplashActivity.this, TelaLogin.class);
        startActivity(intent);
        finish();
        
        Log.d(TAG, "Navegação para TelaLogin concluída");
    }
    
    /**
     * Navega para o dashboard principal
     */
    private void navigateToDashboard(boolean isAdmin, String name, String email) {
        Intent intent = new Intent(SplashActivity.this, MainActivityNova.class);
        
        // Passar dados do usuário
        intent.putExtra("is_admin", isAdmin);
        intent.putExtra("user_email", email);
        intent.putExtra("user_name", name);
        intent.putExtra("user_uid", firebaseManager.getCurrentUserUid());
        
        Log.d(TAG, "Navegando para dashboard - Admin: " + isAdmin + ", Email: " + email);
        
        startActivity(intent);
        finish();
    }
} 