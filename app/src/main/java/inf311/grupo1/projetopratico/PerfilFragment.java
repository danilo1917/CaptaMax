package inf311.grupo1.projetopratico;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import de.hdodenhof.circleimageview.CircleImageView;

import inf311.grupo1.projetopratico.models.ChartData;
import inf311.grupo1.projetopratico.models.UserProfile;
import inf311.grupo1.projetopratico.models.UserMetrics;
import inf311.grupo1.projetopratico.services.MetricsDataProvider;
import inf311.grupo1.projetopratico.services.UserProfileService;
import inf311.grupo1.projetopratico.utils.AppConstants;
import inf311.grupo1.projetopratico.utils.App_fragment;
import inf311.grupo1.projetopratico.utils.BarChartHelper;

import java.util.ArrayList;
import java.util.List;

public class PerfilFragment extends App_fragment {
    
    private static final String TAG = "PerfilFragment";
    
    private boolean isAdmin = false;
    private String userEmail;
    private String userUid;
    
    // Referência ao FirebaseManager
    private FirebaseManager firebaseManager;
    
    // Serviços de dados
    private UserProfileService userProfileService;
    private MetricsDataProvider metricsDataProvider;
    
    // Elementos da UI
    private CircleImageView ivAvatar;
    private TextView tvNome;
    private TextView tvCargo;
    
    // Informações pessoais
    private TextView tvEmail;
    private TextView tvCargoInfo;
    
    // Configurações
    private LinearLayout llSair;
    private BarChart perfilBarChart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "PerfilFragment onCreateView");
        
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);
        
        // Inicializar serviços
        initializeServices();
        
        // Obter dados do usuário dos argumentos
        getUserDataFromArguments();
        
        Log.d(TAG, "PerfilFragment iniciado para usuário: " + userEmail);
        
        return view;
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupClickListeners();
        loadUserData();
    }
    
    /**
     * Inicializa os serviços
     */
    private void initializeServices() {
        firebaseManager = FirebaseManager.getInstance();
        userProfileService = UserProfileService.getInstance();
        metricsDataProvider = MetricsDataProvider.getInstance();
        Log.d(TAG, "Serviços inicializados");
    }
    
    /**
     * Obtém dados do usuário dos argumentos do fragment
     */
    private void getUserDataFromArguments() {
        Bundle args = getArguments();
        if (args != null) {
            isAdmin = args.getBoolean(AppConstants.KEY_IS_ADMIN, false);
            userEmail = args.getString(AppConstants.KEY_USER_EMAIL);
            userUid = args.getString(AppConstants.KEY_USER_UID);
        }
        
        Log.d(TAG, "Dados do usuário - Email: " + userEmail + ", Admin: " + isAdmin);
    }
    
    /**
     * Inicializa os elementos da UI
     */
    private void initViews(View view) {
        // Avatar e informações básicas
        ivAvatar = view.findViewById(R.id.iv_avatar);
        tvNome = view.findViewById(R.id.tv_nome);
        tvCargo = view.findViewById(R.id.tv_cargo);
        
        // Informações pessoais
        tvEmail = view.findViewById(R.id.tv_email);
        tvCargoInfo = view.findViewById(R.id.tv_cargo_info);
        
        // Opções de configuração
        llSair = view.findViewById(R.id.ll_sair);
        
        // Gráfico de barras
        perfilBarChart = view.findViewById(R.id.perfil_bar_chart);
        
        Log.d(TAG, "UI inicializada");
    }
    
    /**
     * Configura os listeners dos elementos clicáveis
     */
    private void setupClickListeners() {
        if (llSair != null) {
            llSair.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSairClick();
                }
            });
        }
        
        Log.d(TAG, "Listeners configurados");
    }
    
    /**
     * Carrega os dados do usuário usando o serviço
     */
    private void loadUserData() {
        Log.d(TAG, "Carregando dados do usuário");
        
        try {
            // Obter perfil completo do usuário com dados reais da API
            UserProfile userProfile = userProfileService.getUserProfile(userUid, userEmail, isAdmin, app_pointer);
            
            // Atualizar informações básicas
            updateBasicInfo(userProfile);
            
            // Carregar gráfico de barras individual
            loadIndividualBarChart();
            
            Log.d(TAG, "Dados do usuário carregados com sucesso usando dados reais da API");
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao carregar dados reais, tentando fallback", e);
            
            try {
                // Fallback para método sem App_main
                UserProfile userProfile = userProfileService.getUserProfile(userUid, userEmail, isAdmin);
                updateBasicInfo(userProfile);
                Log.w(TAG, "Usando dados simulados como fallback");
                
            } catch (Exception e2) {
                Log.e(TAG, "Erro no fallback", e2);
                showError("Erro ao carregar perfil");
                loadBasicUserData();
            }
        }
    }
    
    /**
     * Atualiza informações básicas do perfil
     */
    private void updateBasicInfo(UserProfile userProfile) {
        if (userProfile == null) return;
        
        if (tvNome != null) tvNome.setText(userProfile.getNomeExibicao());
        if (tvCargo != null) tvCargo.setText(userProfile.getCargoFormatado());
        if (tvEmail != null) tvEmail.setText(userProfile.getEmail());
        if (tvCargoInfo != null) tvCargoInfo.setText(userProfile.getCargoFormatado());
        
        Log.d(TAG, "Informações básicas atualizadas para: " + userProfile.getNomeExibicao());
    }
    
    /**
     * Carrega dados básicos como fallback
     */
    private void loadBasicUserData() {
        Log.d(TAG, "Carregando dados básicos como fallback");
        
        if (userEmail != null) {
            if (tvEmail != null) tvEmail.setText(userEmail);
            
            String nome = userEmail.split("@")[0];
            nome = nome.substring(0, 1).toUpperCase() + nome.substring(1);
            if (tvNome != null) tvNome.setText(nome);
        }
        
        String cargo = isAdmin ? "Administrador" : "Consultor de Vendas";
        if (tvCargo != null) tvCargo.setText(cargo);
        if (tvCargoInfo != null) tvCargoInfo.setText(cargo);
        
        Log.d(TAG, "Dados básicos carregados");
    }
    
    public void updateUserInfo(String nome, String email, String cargo) {
        Log.d(TAG, "Atualizando informações do usuário: " + nome);
        
        if (tvNome != null) tvNome.setText(nome);
        if (tvEmail != null) tvEmail.setText(email);
        if (tvCargo != null) tvCargo.setText(cargo);
        if (tvCargoInfo != null) tvCargoInfo.setText(cargo);
    }
    
    /**
     * Recarrega os dados do perfil
     */
    public void refreshProfile() {
        Log.d(TAG, "Recarregando dados do perfil");
        loadUserData();
    }
    
    /**
     * Exibe mensagem de erro
     */
    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Handler para sair (logout)
     */
    private void onSairClick() {
        Log.d(TAG, "Clique em sair");
        
        showLogoutDialog();
    }
    
    /**
     * Exibe dialog de confirmação de logout
     */
    private void showLogoutDialog() {
        if (getContext() == null) return;
        
        new AlertDialog.Builder(getContext())
                .setTitle("Sair")
                .setMessage("Tem certeza que deseja sair?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    performLogout();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
    
    /**
     * Executa o logout do usuário
     */
    private void performLogout() {
        Log.d(TAG, "Executando logout do Firebase");
        
        // Fazer logout do Firebase
        if (firebaseManager != null) {
            firebaseManager.signOut();
            Log.d(TAG, "Logout do Firebase realizado");
        }
        
        // Mostrar mensagem de confirmação
        if (getContext() != null) {
            Toast.makeText(getContext(), "Logout realizado com sucesso", Toast.LENGTH_SHORT).show();
        }
        
        // Redirecionar para tela de login
        redirectToLogin();
    }
    
    /**
     * Redireciona para a tela de login e limpa o stack de activities
     */
    private void redirectToLogin() {
        if (getContext() != null) {
            Intent intent = new Intent(getContext(), TelaLogin.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            
            // Finalizar a activity atual
            if (getActivity() != null) {
                getActivity().finish();
            }
            
            Log.d(TAG, "Redirecionamento para TelaLogin executado");
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "PerfilFragment destruído");
    }
    
    /**
     * Getters para informações do usuário (compatibilidade)
     */
    public String getCurrentUserEmail() {
        return userEmail;
    }
    
    public String getCurrentUserUid() {
        return userUid;
    }
    
    public boolean isCurrentUserAdmin() {
        return isAdmin;
    }
    
    /**
     * Carrega dados para o gráfico de barras individual do usuário
     */
    private void loadIndividualBarChart() {
        Log.d(TAG, "Carregando dados do gráfico de barras individual para: " + userEmail);
        
        // Carregar dados individuais (não da equipe) - sempre forçar isAdmin=false para dados individuais
        metricsDataProvider.getBarChartData(userEmail, false, app_pointer, 
            new MetricsDataProvider.BarChartCallback() {
                @Override
                public void onSuccess(ChartData.BarChartData data) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setupIndividualBarChart(data);
                            }
                        });
                    }
                }
                
                @Override
                public void onError(String error) {
                    Log.e(TAG, "Erro ao carregar dados do gráfico individual: " + error);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Usar dados de fallback individuais
                                ChartData.BarChartData fallback = 
                                    metricsDataProvider.getBarChartData(userEmail, false);
                                setupIndividualBarChart(fallback);
                            }
                        });
                    }
                }
            });
    }
    
    /**
     * Configura o gráfico de barras individual moderno com os dados do usuário
     */
    private void setupIndividualBarChart(ChartData.BarChartData chartData) {
        try {
            if (perfilBarChart == null || chartData == null) {
                Log.w(TAG, "BarChart do perfil ou dados são null");
                return;
            }
            
            // Para o perfil, vamos mostrar apenas os dados do usuário atual
            // Filtrar dados para mostrar apenas o usuário atual
            ChartData.ConsultorData dadosUsuario = null;
            String nomeUsuario = getNomeFromEmail(userEmail);
            
            for (ChartData.ConsultorData consultor : chartData.getConsultores()) {
                if (consultor.getNome().toLowerCase().contains(nomeUsuario.toLowerCase())) {
                    dadosUsuario = consultor;
                    break;
                }
            }
            
            // Se não encontrou dados específicos, usar o primeiro consultor ou criar dados básicos
            if (dadosUsuario == null && !chartData.getConsultores().isEmpty()) {
                dadosUsuario = chartData.getConsultores().get(0);
            } else if (dadosUsuario == null) {
                // Criar dados básicos se não houver dados
                dadosUsuario = BarChartHelper.createFallbackUserData(nomeUsuario);
            }
            
            // Usar a classe utilitária modernizada para gráficos de barras do perfil
            BarChartHelper.setupProfileBarChart(perfilBarChart, dadosUsuario);
            
            Log.d(TAG, "Gráfico de barras moderno do perfil configurado para: " + dadosUsuario.getNome() +
                      " - Leads: " + dadosUsuario.getLeads() + ", Conversões: " + dadosUsuario.getConversoes());
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao configurar gráfico de barras moderno do perfil", e);
        }
    }
    
    /**
     * Extrai nome do email para identificação
     */
    private String getNomeFromEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "Usuário";
        }
        String nome = email.split("@")[0];
        return Character.toUpperCase(nome.charAt(0)) + nome.substring(1);
    }
} 