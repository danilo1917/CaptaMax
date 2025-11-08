package inf311.grupo1.projetopratico;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseUser;

import inf311.grupo1.projetopratico.services.NotificationService;
import inf311.grupo1.projetopratico.utils.App_fragment;
import inf311.grupo1.projetopratico.utils.NotificationPermissionHelper;

public class MainActivityNova extends AppCompatActivity {
    
    private static final String TAG = "MainActivityNova";
    
    private FirebaseManager firebaseManager;
    private NotificationService notificationService;
    private Toolbar toolbar;
    private FragmentManager fragmentManager;
    
    private boolean isAdmin = false;
    private String userEmail;
    private String userUid;
    private String userName;
    
    private static final String DASHBOARD_FRAGMENT = "dashboard_fragment";
    private static final String LEADS_FRAGMENT = "leads_fragment";
    private static final String NOVO_LEAD_FRAGMENT = "novo_lead_fragment";
    private static final String DETALHES_LEAD_FRAGMENT = "detalhes_lead_fragment";
    private static final String PERFIL_FRAGMENT = "perfil_fragment";
    private static final String METRICAS_FRAGMENT = "metricas_fragment";
    private static final String ALERTAS_FRAGMENT = "alertas_fragment";
    private static final String FUNIL_FRAGMENT = "funil_fragment";
    private static final String ACOMPANHAMENTO_FRAGMENT = "acompanhamento_fragment";
    
    private String currentFragmentTag = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_nova);
        
        Log.d(TAG, "=== MainActivityNova INICIADA ===");
        
        firebaseManager = FirebaseManager.getInstance();
        
        if (!checkAuthentication()) {
            return;
        }
        
        getUserData();
        
        // Limpar caches para garantir dados atualizados para o usuário atual
        clearCachesForCurrentUser();
        
        // Verificar e solicitar permissões ANTES de inicializar notificações
        checkAndRequestPermissions();
        
        initializeNotificationService();
        setupToolbar();
        setupFragmentManager();
        
        // Verificar se deve abrir a aba de notificações
        if (getIntent().getBooleanExtra("open_notifications", false)) {
            currentFragmentTag = ALERTAS_FRAGMENT;
        }
        
        if (savedInstanceState == null) {
            loadInitialFragment();
        } else {
            // Recuperar o fragment atual se a activity foi recriada
            Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
            if (currentFragment != null) {
                currentFragmentTag = currentFragment.getTag();
                if (currentFragmentTag == null) {
                    currentFragmentTag = DASHBOARD_FRAGMENT;
                }
                updateToolbarState(currentFragmentTag);
            }
        }
    }
    
    /**
     * Verifica se o usuário está autenticado
     */
    private boolean checkAuthentication() {
        if (!firebaseManager.isUserLoggedIn()) {
            Log.w(TAG, "Usuário não autenticado, redirecionando para login");
            redirectToLogin();
            return false;
        }
        return true;
    }
    
    /**
     * Obtém dados do usuário da intent ou Firebase
     */
    private void getUserData() {
        Intent intent = getIntent();
        if (intent != null) {
            Data_master.admin  =isAdmin = intent.getBooleanExtra("is_admin", false);
            userEmail = intent.getStringExtra("user_email");
            userName = intent.getStringExtra("user_name");
            Data_master.user_id = userUid = intent.getStringExtra("user_uid");
        }
        
        if (userEmail == null || userUid == null) {
            FirebaseUser currentUser = firebaseManager.getCurrentUser();
            if (currentUser != null) {
                userEmail = currentUser.getEmail();
                Data_master.user_id = userUid = currentUser.getUid();
            }
        }
        
        Log.d(TAG, "Dados do usuário - Email: " + userEmail + ", Nome: " + userName + ", Admin: " + isAdmin);
    }
    
    /**
     * Verifica e solicita permissões necessárias
     */
    private void checkAndRequestPermissions() {
        Log.d(TAG, "=== VERIFICANDO PERMISSÕES ===");
        
        // Verificação completa de permissões
        NotificationPermissionHelper.performCompleteCheck(this);
        
        // Log do status atual
        String status = NotificationPermissionHelper.getNotificationStatus(this);
        Log.d(TAG, status);
    }
    
    /**
     * Inicializa o serviço de notificações
     */
    private void initializeNotificationService() {
        try {
            Log.d(TAG, "=== INICIALIZANDO SERVIÇO DE NOTIFICAÇÕES ===");
            notificationService = NotificationService.getInstance();
            notificationService.initializeForCurrentUser();
            Log.d(TAG, "✅ Serviço de notificações inicializado");
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao inicializar serviço de notificações", e);
        }
    }
    
    /**
     * Configura a toolbar
     */
    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setElevation(25.0f);
            // Não atualizar o estado da toolbar ainda se currentFragmentTag for null
            if (currentFragmentTag != null) {
                updateToolbarState(currentFragmentTag);
            }
            Log.d(TAG, "Toolbar configurada");
        }
    }
    
    /**
     * Configura o gerenciador de fragments
     */
    private void setupFragmentManager() {
        fragmentManager = getSupportFragmentManager();
        Log.d(TAG, "Fragment manager configurado");
    }
    
    /**
     * Carrega o fragment inicial
     */
    private void loadInitialFragment() {
        // Se deve abrir notificações, carregar AlertasFragment
        if (ALERTAS_FRAGMENT.equals(currentFragmentTag)) {
            navigateToFragment(ALERTAS_FRAGMENT, null);
            Log.d(TAG, "Fragment inicial: Alertas (via notificação)");
        } else {
            // Forçar carregamento do dashboard independentemente do estado atual
            currentFragmentTag = null; // Resetar para garantir carregamento
            
            // Forçar atualização dos dados antes de carregar o dashboard inicial
            forceUpdateDataForInitialLoad();
            
            navigateToFragment(DASHBOARD_FRAGMENT, null);
            Log.d(TAG, "Fragment inicial: Dashboard");
        }
    }
    
    /**
     * Força atualização dos dados para o carregamento inicial
     */
    private void forceUpdateDataForInitialLoad() {
        Log.d(TAG, "=== FORÇANDO ATUALIZAÇÃO PARA CARREGAMENTO INICIAL ===");
        
        try {
            App_main app = (App_main) getApplication();
            if (app != null) {
                // Executar em thread separada para não bloquear a UI
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            app.forceUpdate(); // Força nova busca na API
                            Log.d(TAG, "✅ Dados atualizados com sucesso para carregamento inicial");
                        } catch (Exception e) {
                            Log.e(TAG, "❌ Erro ao atualizar dados para carregamento inicial", e);
                        }
                    }
                }).start();
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao forçar atualização para carregamento inicial", e);
        }
    }
    
    /**
     * Navega para um fragment específico
     */
    private void navigateToFragment(String fragmentTag, Bundle args) {
        // Permitir recarregamento do fragment se currentFragmentTag for null (caso inicial)
        if (currentFragmentTag != null && fragmentTag.equals(currentFragmentTag)) {
            Log.d(TAG, "Já estamos no fragment: " + fragmentTag);
            return;
        }
        
        Fragment fragment = createFragment(fragmentTag, args);
        if (fragment == null) {
            Log.e(TAG, "Não foi possível criar fragment: " + fragmentTag);
            return;
        }
        
        if (args != null) {
            fragment.setArguments(args);
        }
        
        Bundle userArgs = fragment.getArguments();
        if (userArgs == null) {
            userArgs = new Bundle();
        }
        userArgs.putString("user_email", userEmail);
        userArgs.putString("user_uid", userUid);
        userArgs.putString("user_name", userName);
        userArgs.putBoolean("is_admin", isAdmin);
        fragment.setArguments(userArgs);
        
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        
        applyContextualAnimations(transaction, fragmentTag, currentFragmentTag);
        
        transaction.replace(R.id.fragment_container, fragment, fragmentTag);
        
        // Só adicionar ao backstack se não for o fragment inicial
        if (currentFragmentTag != null) {
            transaction.addToBackStack(null);
        }
        
        transaction.commit();
        
        currentFragmentTag = fragmentTag;
        updateToolbarState(fragmentTag);
        
        Log.d(TAG, "Navegando para fragment: " + fragmentTag);
    }
    
    /**
     * Cria um fragment baseado na tag fornecida
     */
    private Fragment createFragment(String tag, Bundle userData) {
        App_fragment fragment = null;

        var app = (App_main) getApplication();
        
        switch (tag) {
            case DASHBOARD_FRAGMENT:
            case "dashboard":
                fragment = new DashboardFragment();
                fragment.app_pointer = app;
                Log.d(TAG, "Criando DashboardFragment");
                break;
                
            case LEADS_FRAGMENT:
            case "leads":
                fragment = new LeadsFragment();
                fragment.app_pointer = app;
                Log.d(TAG, "Criando LeadsFragment");
                break;
                
            case NOVO_LEAD_FRAGMENT:
            case "novo_lead":
                fragment = new NovoLeadFragment();
                fragment.app_pointer = app;
                Log.d(TAG, "Criando NovoLeadFragment");
                break;
                
            case DETALHES_LEAD_FRAGMENT:
            case "detalhes_lead":
                fragment = new DetalhesLeadFragment();
                fragment.app_pointer = app;
                Log.d(TAG, "Criando DetalhesLeadFragment");
                break;
                
            case PERFIL_FRAGMENT:
            case "perfil":
                fragment = new PerfilFragment();
                fragment.app_pointer = app;
                Log.d(TAG, "Criando PerfilFragment");
                break;
                
            case METRICAS_FRAGMENT:
            case "metricas":
                fragment = new MetricasFragment();
                fragment.app_pointer = app;
                Log.d(TAG, "Criando MetricasFragment");
                break;
                
            case ALERTAS_FRAGMENT:
            case "alertas":
                fragment = new AlertasFragment();
                fragment.app_pointer = app;
                Log.d(TAG, "Criando AlertasFragment");
                break;
                
            case FUNIL_FRAGMENT:
            case "funil":
                fragment = new FunilFragment();
                fragment.app_pointer = app;
                Log.d(TAG, "Criando FunilFragment");
                break;

            case ACOMPANHAMENTO_FRAGMENT:
            case "acompanhamento":
                fragment = new AcompanhamentoFragment();
                fragment.app_pointer = app;
                Log.d(TAG, "Criando AcompanhamentoFragment");
                break;
                
            default:
                Log.w(TAG, "Fragment tag desconhecida: " + tag + ". Criando DashboardFragment por padrão");
                fragment = new DashboardFragment();
                break;
        }
        
        // Adicionar dados do usuário ao fragment se userData não for null
        if (fragment != null && userData != null) {
            fragment.setArguments(userData);
        }
        
        return fragment;
    }
    
    /**
     * Atualiza o estado visual da toolbar baseado no fragment atual
     */
    private void updateToolbarState(String fragmentTag) {
        resetToolbarButtons();
        
        switch (fragmentTag) {
            case DASHBOARD_FRAGMENT:
                highlightToolbarButton(R.id.toolbar_btn1);
                break;
            case LEADS_FRAGMENT:
                highlightToolbarButton(R.id.toolbar_btn2);
                break;
            case ALERTAS_FRAGMENT:
                highlightToolbarButton(R.id.toolbar_btn4);
                break;
            case PERFIL_FRAGMENT:
                highlightToolbarButton(R.id.toolbar_btn5);
                break;
        }
        
        Log.d(TAG, "Estado da toolbar atualizado para: " + fragmentTag);
    }
    
    /**
     * Reseta todos os botões da toolbar para o estado padrão
     */
    private void resetToolbarButtons() {
        resetModernButton(R.id.toolbar_btn1);
        resetModernButton(R.id.toolbar_btn2);
        resetModernButton(R.id.toolbar_btn4);
        resetModernButton(R.id.toolbar_btn5);
        
        Log.d(TAG, "Botões da toolbar resetados");
    }
    
    /**
     * Reseta um botão moderno para o estado inativo
     */
    private void resetModernButton(int buttonId) {
        if (toolbar == null) return;
        
        androidx.cardview.widget.CardView button = toolbar.findViewById(buttonId);
        if (button == null) return;
        
        // Encontrar os elementos internos
        android.widget.ImageView icon = null;
        android.widget.TextView text = null;
        androidx.cardview.widget.CardView iconCard = null;
        
        // Navegar pela hierarquia para encontrar os elementos
        android.view.ViewGroup container = (android.view.ViewGroup) button.getChildAt(0);
        if (container != null) {
            for (int i = 0; i < container.getChildCount(); i++) {
                android.view.View child = container.getChildAt(i);
                if (child instanceof androidx.cardview.widget.CardView) {
                    iconCard = (androidx.cardview.widget.CardView) child;
                    // Procurar o ícone dentro do card
                    if (iconCard.getChildCount() > 0) {
                        android.view.View cardChild = iconCard.getChildAt(0);
                        if (cardChild instanceof android.widget.ImageView) {
                            icon = (android.widget.ImageView) cardChild;
                        } else if (cardChild instanceof android.view.ViewGroup) {
                            // Para o caso do FrameLayout dos alertas
                            android.view.ViewGroup frame = (android.view.ViewGroup) cardChild;
                            for (int j = 0; j < frame.getChildCount(); j++) {
                                if (frame.getChildAt(j) instanceof android.widget.ImageView) {
                                    icon = (android.widget.ImageView) frame.getChildAt(j);
                                    break;
                                }
                            }
                        }
                    }
                } else if (child instanceof android.widget.TextView) {
                    text = (android.widget.TextView) child;
                }
            }
        }
        
        // Aplicar estilo inativo
        if (iconCard != null) {
            iconCard.setCardBackgroundColor(getResources().getColor(R.color.background_white));
            iconCard.setCardElevation(2.0f);
        }
        if (icon != null) {
            icon.setColorFilter(getResources().getColor(R.color.text_secondary));
        }
        if (text != null) {
            text.setTextColor(getResources().getColor(R.color.text_secondary));
            text.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
    }
    
    /**
     * Destaca um botão específico da toolbar
     */
    private void highlightToolbarButton(int buttonId) {
        if (toolbar == null) return;
        
        highlightModernButton(buttonId);
        
        Log.d(TAG, "Botão da toolbar destacado: " + buttonId);
    }
    
    /**
     * Destaca um botão moderno específico
     */
    private void highlightModernButton(int buttonId) {
        androidx.cardview.widget.CardView button = toolbar.findViewById(buttonId);
        if (button == null) return;
        
        // Encontrar os elementos internos
        android.widget.ImageView icon = null;
        android.widget.TextView text = null;
        androidx.cardview.widget.CardView iconCard = null;
        
        // Navegar pela hierarquia para encontrar os elementos
        android.view.ViewGroup container = (android.view.ViewGroup) button.getChildAt(0);
        if (container != null) {
            for (int i = 0; i < container.getChildCount(); i++) {
                android.view.View child = container.getChildAt(i);
                if (child instanceof androidx.cardview.widget.CardView) {
                    iconCard = (androidx.cardview.widget.CardView) child;
                    // Procurar o ícone dentro do card
                    if (iconCard.getChildCount() > 0) {
                        android.view.View cardChild = iconCard.getChildAt(0);
                        if (cardChild instanceof android.widget.ImageView) {
                            icon = (android.widget.ImageView) cardChild;
                        } else if (cardChild instanceof android.view.ViewGroup) {
                            // Para o caso do FrameLayout dos alertas
                            android.view.ViewGroup frame = (android.view.ViewGroup) cardChild;
                            for (int j = 0; j < frame.getChildCount(); j++) {
                                if (frame.getChildAt(j) instanceof android.widget.ImageView) {
                                    icon = (android.widget.ImageView) frame.getChildAt(j);
                                    break;
                                }
                            }
                        }
                    }
                } else if (child instanceof android.widget.TextView) {
                    text = (android.widget.TextView) child;
                }
            }
        }
        
        // Aplicar estilo ativo
        if (iconCard != null) {
            iconCard.setCardBackgroundColor(getResources().getColor(R.color.primary_green));
            iconCard.setCardElevation(4.0f);
        }
        if (icon != null) {
            icon.setColorFilter(getResources().getColor(R.color.text_white));
        }
        if (text != null) {
            text.setTextColor(getResources().getColor(R.color.primary_green));
            text.setTypeface(null, android.graphics.Typeface.BOLD);
        }
    }
    
    // Métodos da toolbar
    
    public void dashboard(View v) {
        navigateToFragment(DASHBOARD_FRAGMENT, null);
    }
    
    public void leads(View v) {
        navigateToFragment(LEADS_FRAGMENT, null);
    }
    
    public void novo_lead(View v) {
        navigateToFragment(NOVO_LEAD_FRAGMENT, null);
    }
    
    public void alertas(View v) {
        navigateToFragment(ALERTAS_FRAGMENT, null);
    }
    
    public void profile(View v) {
        navigateToFragment(PERFIL_FRAGMENT, null);
    }
    
    /**
     * Navega para detalhes do lead
     */
    public void navigateToLeadDetails(Contato contato) {
        Bundle args = new Bundle();
        args.putParcelable("contato", contato);
        navigateToFragment(DETALHES_LEAD_FRAGMENT, args);
    }

    /**
     * Navega para acompanhamento do lead
     */
    public void navigateToAcompanhamento(Contato contato) {
        Bundle args = new Bundle();
        args.putParcelable("contato", contato);
        navigateToFragment(ACOMPANHAMENTO_FRAGMENT, args);
    }

    /**
     * Navega para métricas da equipe
     */
    public void navigateToMetricas() {
        navigateToFragment(METRICAS_FRAGMENT, null);
    }
    
    /**
     * Navega para funil
     */
    public void navigateToFunil() {
        navigateToFragment(FUNIL_FRAGMENT, null);
    }
    
    /**
     * Realiza logout
     */
    public void logout(View v) {
        Log.d(TAG, "Iniciando logout");
        
        firebaseManager.signOut();
        Toast.makeText(this, "Logout realizado com sucesso", Toast.LENGTH_SHORT).show();
        
        redirectToLogin();
    }
    
    /**
     * Redireciona para tela de login
     */
    private void redirectToLogin() {
        Intent intent = new Intent(this, TelaLogin.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    /**
     * Getters para informações do usuário (para uso dos fragments)
     */
    public String getUserEmail() {
        return userEmail;
    }
    
    public String getUserUid() {
        return userUid;
    }
    
    public boolean isUserAdmin() {
        return isAdmin;
    }
    
    public FirebaseManager getFirebaseManager() {
        return firebaseManager;
    }
    
    @Override
    public void onBackPressed() {
        if (fragmentManager.getBackStackEntryCount() > 1) {
            fragmentManager.popBackStack();
            Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
            if (currentFragment != null) {
                currentFragmentTag = currentFragment.getTag();
                if (currentFragmentTag != null) {
                    updateToolbarState(currentFragmentTag);
                }
            }
        } else {
            super.onBackPressed();
        }
    }
    
    /**
     * Aplica animações contextuais baseadas no tipo de transição
     */
    private void applyContextualAnimations(FragmentTransaction transaction, String toFragment, String fromFragment) {
        // Caso inicial - sem animação ou animação sutil
        if (fromFragment == null) {
            transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left
            );
            return;
        }
        
        // Transições especiais para fragments importantes
        if (NOVO_LEAD_FRAGMENT.equals(toFragment) || DETALHES_LEAD_FRAGMENT.equals(toFragment)) {
            // Animação mais elaborada para ações importantes
            transaction.setCustomAnimations(
                R.anim.slide_up_fade_in,
                R.anim.slide_down_fade_out,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            );
            return;
        }
        
        // Transições entre fragments principais - animação horizontal rápida
        transaction.setCustomAnimations(
            R.anim.slide_in_right,
            R.anim.slide_out_left,
            R.anim.slide_in_left,
            R.anim.slide_out_right
        );
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == NotificationPermissionHelper.REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "✅ Permissão de notificação concedida");
                // Reinicializar serviço de notificações
                initializeNotificationService();
            } else {
                Log.w(TAG, "❌ Permissão de notificação negada");
                // Mostrar explicação ao usuário
                showPermissionExplanation();
            }
        }
    }
    
    /**
     * Mostra explicação sobre a importância das permissões
     */
    private void showPermissionExplanation() {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Permissão de Notificação")
            .setMessage("Para receber alertas importantes sobre leads e atividades, é necessário permitir notificações.\n\n" +
                       "Você pode habilitar nas configurações do aplicativo.")
            .setPositiveButton("Ir para Configurações", (dialog, which) -> {
                NotificationPermissionHelper.openNotificationSettings(this);
            })
            .setNegativeButton("Agora Não", null)
            .show();
    }
    
    /**
     * Limpa os caches para garantir dados atualizados para o usuário atual
     */
    private void clearCachesForCurrentUser() {
        Log.d(TAG, "=== LIMPANDO CACHES PARA USUÁRIO ATUAL ===");
        
        try {
            // Limpar cache do MetricsDataProvider
            inf311.grupo1.projetopratico.services.MetricsDataProvider metricsProvider = 
                inf311.grupo1.projetopratico.services.MetricsDataProvider.getInstance();
            metricsProvider.clearCache();
            Log.d(TAG, "✅ Cache do MetricsDataProvider limpo na MainActivityNova");
            
            // Forçar reset do App_main para nova atualização
            App_main app = (App_main) getApplication();
            if (app != null) {
                app.updated = false; // Marcar para atualização forçada
                Log.d(TAG, "✅ App_main marcado para atualização forçada na MainActivityNova");
            }
            
            Log.d(TAG, "=== LIMPEZA DE CACHES NA MAINACTIVITY CONCLUÍDA ===");
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao limpar caches na MainActivityNova", e);
        }
    }
} 