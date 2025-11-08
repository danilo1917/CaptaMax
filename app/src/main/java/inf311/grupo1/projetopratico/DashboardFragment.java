package inf311.grupo1.projetopratico;

import android.graphics.Typeface;    
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.mikephil.charting.charts.PieChart;

import inf311.grupo1.projetopratico.models.ChartData;
import inf311.grupo1.projetopratico.models.DashboardMetrics;
import inf311.grupo1.projetopratico.services.DashboardDataProvider;
import inf311.grupo1.projetopratico.services.MetricsDataProvider;
import inf311.grupo1.projetopratico.utils.AppConstants;
import inf311.grupo1.projetopratico.utils.App_fragment;
import inf311.grupo1.projetopratico.utils.LeadCardHelper;
import inf311.grupo1.projetopratico.utils.PieChartHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class DashboardFragment extends App_fragment {

    private static final String TAG = "DashboardFragment";

    private List<Contato> contatos;
    private HashMap<Integer, Contato> cont_dict;
    
    // Informações do usuário
    private boolean isAdmin = false;
    private String userEmail;
    private String userUid;
    private String userName;
    
    // Views
    private LinearLayout dashScrollLinear2;
    private TextView welcomeMessage;
    private SwipeRefreshLayout swipeRefreshLayout;
    private PieChart dashPieChart;
    
    // TextViews das métricas do dashboard - IDs atualizados
    private TextView tvTotalLeads;
    private TextView tvConvertidos;
    private TextView tvTaxaConversao;
    private TextView tvEsteMes;
    
    // Serviços de dados
    private DashboardDataProvider dashboardDataProvider;
    private MetricsDataProvider metricsDataProvider;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "DashboardFragment onCreateView");
        
        // Inflar o layout do fragment
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        
        // Inicializar serviços
        initializeServices();
        
        // Obter informações do usuário dos argumentos
        getUserDataFromArguments();

        Log.d(TAG, "DashboardFragment iniciado para usuário: " + userEmail);
        
        return view;
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Inicializar views
        initializeViews(view);
        
        // Mostrar informações do usuário
        displayUserInfo();
        
        // Configurar listeners
        setupListeners(view);
        
        // Carregar dados após inicializar as views
        loadDashboardData();
    }
    
    /**
     * Inicializa os serviços de dados
     */
    private void initializeServices() {
        dashboardDataProvider = DashboardDataProvider.getInstance();
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
            userName = args.getString(AppConstants.KEY_USER_NAME);
        }
        
        Log.d(TAG, "Dados do usuário - Email: " + userEmail + ", Nome: " + userName + ", Admin: " + isAdmin);
    }
    
    /**
     * Inicializa as views do fragment
     */
    private void initializeViews(View view) {
        dashScrollLinear2 = view.findViewById(R.id.dash_scroll_linear2);
        welcomeMessage = view.findViewById(R.id.welcome_message);
        swipeRefreshLayout = view.findViewById(R.id.dashboard_swipe_refresh);
        dashPieChart = view.findViewById(R.id.dash_pie_chart);
        
        // Inicializar TextViews das métricas com os novos IDs
        tvTotalLeads = view.findViewById(R.id.tv_total_leads);
        tvConvertidos = view.findViewById(R.id.tv_convertidos);
        tvTaxaConversao = view.findViewById(R.id.tv_taxa_conversao);
        tvEsteMes = view.findViewById(R.id.tv_este_mes);
        
        // Ocultar gráfico de pizza para usuários não-admin
        View pieChartCard = view.findViewById(R.id.dash_pie_chart_card);
        if (pieChartCard != null) {
            pieChartCard.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
            Log.d(TAG, "Gráfico de pizza " + (isAdmin ? "visível" : "oculto") + " para usuário " + (isAdmin ? "admin" : "não-admin"));
        }
        
        // Ocultar botão de métricas da equipe para usuários não-admin
        View btnMetricas = view.findViewById(R.id.dash_btn_metricas);
        View btnFunil = view.findViewById(R.id.dash_btn_funil);
        if (btnMetricas != null && btnFunil != null) {
            if (isAdmin) {
                btnMetricas.setVisibility(View.VISIBLE);
                // Manter layout original com dois botões
                LinearLayout.LayoutParams paramsMetricas = (LinearLayout.LayoutParams) btnMetricas.getLayoutParams();
                paramsMetricas.weight = 1;
                btnMetricas.setLayoutParams(paramsMetricas);
                
                LinearLayout.LayoutParams paramsFunil = (LinearLayout.LayoutParams) btnFunil.getLayoutParams();
                paramsFunil.weight = 1;
                btnFunil.setLayoutParams(paramsFunil);
            } else {
                btnMetricas.setVisibility(View.GONE);
                // Fazer o botão de funil ocupar toda a largura
                LinearLayout.LayoutParams paramsFunil = (LinearLayout.LayoutParams) btnFunil.getLayoutParams();
                paramsFunil.weight = 1;
                paramsFunil.setMarginStart(0); // Remover margem esquerda
                btnFunil.setLayoutParams(paramsFunil);
            }
            Log.d(TAG, "Botão de métricas " + (isAdmin ? "visível" : "oculto") + " para usuário " + (isAdmin ? "admin" : "não-admin"));
        }
        
        // Configurar pull to refresh
        setupPullToRefresh();
        
        Log.d(TAG, "Views inicializadas para usuário " + (isAdmin ? "admin" : "não-admin"));
    }
    
    /**
     * Configura o pull to refresh
     */
    private void setupPullToRefresh() {
        if (swipeRefreshLayout != null) {
            // Configurar cores do loading
            swipeRefreshLayout.setColorSchemeResources(
                R.color.primary_green,
                R.color.secondary_green,
                R.color.accent_green
            );
            
            // Configurar o listener de refresh
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    Log.d(TAG, "Pull to refresh ativado - atualizando dados do dashboard");
                    refreshDashboardData();
                }
            });
            
            Log.d(TAG, "Pull to refresh configurado para o dashboard");
        } else {
            Log.w(TAG, "SwipeRefreshLayout não encontrado");
        }
    }
    
    /**
     * Carrega os dados do dashboard usando o provedor de dados
     */
    private void loadDashboardData() {
        Log.d(TAG, "Iniciando carregamento de dados do dashboard");
        
        // Carregar métricas do dashboard
        loadDashboardMetrics();
        
        // Carregar leads recentes
        loadRecentLeads();
        
        // Carregar dados do gráfico de pizza apenas para admins
        if (isAdmin) {
            loadPieChartData();
            Log.d(TAG, "Carregando gráfico de pizza para usuário admin");
        } else {
            Log.d(TAG, "Pulando carregamento do gráfico de pizza para usuário não-admin");
        }
        
        // Atualizar interface após carregar os dados
        setupUI();
        
        Log.d(TAG, "Dados do dashboard carregados e interface atualizada");
    }
    
    /**
     * Carrega as métricas do dashboard
     */
    private void loadDashboardMetrics() {
        try {
            // Garantir que o app_pointer esteja disponível e atualizado
            if (app_pointer != null) {
                // Verificar se dados precisam ser atualizados (só atualiza se necessário)
                if (!app_pointer.updated) {
                    Log.d(TAG, "Atualizando dados da API antes de carregar métricas");
                    app_pointer.update();
                }
                
                // Usar o novo método que aceita App_main para obter dados reais
                DashboardMetrics metrics = dashboardDataProvider.getDashboardMetrics(userEmail, isAdmin, app_pointer);
                displayDashboardMetrics(metrics);
                
                Log.d(TAG, "Métricas carregadas com dados reais - Total leads: " + metrics.getTotalLeads());
            } else {
                Log.w(TAG, "app_pointer não disponível, usando fallback");
                // Fallback para método sem App_main em caso de app_pointer não disponível
                DashboardMetrics metrics = dashboardDataProvider.getDashboardMetrics(userEmail, isAdmin);
                displayDashboardMetrics(metrics);
                Log.w(TAG, "Usando dados simulados como fallback");
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao carregar métricas do dashboard", e);
            // Fallback para método sem App_main em caso de erro
            try {
                DashboardMetrics metrics = dashboardDataProvider.getDashboardMetrics(userEmail, isAdmin);
                displayDashboardMetrics(metrics);
                Log.w(TAG, "Usando dados simulados como fallback após erro");
            } catch (Exception e2) {
                Log.e(TAG, "Erro no fallback", e2);
                showError("Erro ao carregar métricas");
                // Exibir métricas zeradas como último recurso
                displayDashboardMetrics(new DashboardMetrics(0, 0, 0, 0, 0.0));
            }
        }
    }
    
    /**
     * Carrega os leads recentes
     */
    private void loadRecentLeads() {
        try {
            // Garantir que o app_pointer esteja disponível e atualizado
            if (app_pointer != null) {
                // Verificar se dados precisam ser atualizados (só atualiza se necessário)
                if (!app_pointer.updated) {
                    Log.d(TAG, "Atualizando dados da API antes de carregar leads");
                    app_pointer.update();
                }
                
                contatos = dashboardDataProvider.getLeadsRecentes(userEmail, isAdmin, 
                                                                AppConstants.LIMITE_LEADS_DASHBOARD,
                        app_pointer);
                cont_dict = new HashMap<>();
                
                Log.d(TAG, "Leads recentes carregados com dados reais: " + contatos.size() + " leads");
            } else {
                Log.w(TAG, "app_pointer não disponível para carregar leads recentes");
                contatos = new ArrayList<>();
                cont_dict = new HashMap<>();
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao carregar leads recentes", e);
            showError("Erro ao carregar leads");
            contatos = new ArrayList<>();
            cont_dict = new HashMap<>();
        }
    }
    
    /**
     * Exibe as métricas do dashboard na interface
     */
    private void displayDashboardMetrics(DashboardMetrics metrics) {
        if (metrics == null) {
            Log.w(TAG, "Métricas nulas recebidas");
            return;
        }
        
        try {
            // Card 1: Total de Leads
            if (tvTotalLeads != null) {
                tvTotalLeads.setText(String.valueOf(metrics.getTotalLeads()));
            }
            
            // Card 2: Conversões - CORRIGIDO para usar leadsConvertidos
            if (tvConvertidos != null) {
                tvConvertidos.setText(String.valueOf(metrics.getLeadsConvertidos()));
            }
            
            // Card 3: Taxa de Conversão
            if (tvTaxaConversao != null) {
                tvTaxaConversao.setText(String.format("%.1f%%", metrics.getTaxaConversao()));
            }
            
            // Card 4: Leads Este Mês
            if (tvEsteMes != null) {
                tvEsteMes.setText(String.valueOf(metrics.getLeadsNovos()));
            }
            
            Log.d(TAG, "Interface atualizada com métricas reais - " +
                      "Total Leads: " + metrics.getTotalLeads() + 
                      ", Conversões: " + metrics.getLeadsConvertidos() +
                      ", Taxa Conversão: " + String.format("%.1f%%", metrics.getTaxaConversao()) + 
                      ", Este Mês: " + metrics.getLeadsNovos());
                      
        } catch (Exception e) {
            Log.e(TAG, "Erro ao atualizar interface com métricas", e);
        }
    }

    /**
     * Configura a interface do usuário
     */
    private void setupUI() {
        // Limpar cards existentes
        if (dashScrollLinear2 != null) {
            dashScrollLinear2.removeAllViews();
        }
        
        // Adicionar cards de leads
        if (contatos != null) {
            for (Contato contato : contatos) {
                add_lead_card(contato);
            }
        }
        
        Log.d(TAG, "Interface configurada com " + (contatos != null ? contatos.size() : 0) + " leads");
    }

    /**
     * Mostra informações do usuário autenticado
     */
    private void displayUserInfo() {
        if (userName != null && !userName.isEmpty()) {
            // Usar o nome do usuário se disponível
            String welcomeText = "Bem-vindo, " + userName + "!";
            if (welcomeMessage != null) {
                welcomeMessage.setText(welcomeText);
                Log.d(TAG, "Mensagem de boas-vindas definida: " + welcomeText);
            }
        } else if (userEmail != null) {
            // Fallback para usar o email se o nome não estiver disponível
            String emailName = userEmail.split("@")[0];
            String welcomeText = "Bem-vindo, " + emailName + "!";
            if (welcomeMessage != null) {
                welcomeMessage.setText(welcomeText);
                Log.d(TAG, "Mensagem de boas-vindas definida com email: " + welcomeText);
            }
        } else {
            // Fallback genérico
            if (welcomeMessage != null) {
                welcomeMessage.setText("Bem-vindo!");
                Log.d(TAG, "Mensagem de boas-vindas genérica definida");
            }
        }
    }
    
    /**
     * Configura os listeners
     */
    private void setupListeners(View view) {
        // Botão Métricas da Equipe
        View btnMetricas = view.findViewById(R.id.dash_btn_metricas);
        if (btnMetricas != null) {
            btnMetricas.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigateToMetricas();
                }
            });
        }
        
        // Botão Funil
        View btnFunil = view.findViewById(R.id.dash_btn_funil);
        if (btnFunil != null) {
            btnFunil.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigateToFunil();
                }
            });
        }
        
        Log.d(TAG, "Listeners configurados");
    }

    /**
     * Adiciona um card de lead na interface usando cards modernos padronizados
     */
    public void add_lead_card(Contato cont) {
        if (getContext() == null || dashScrollLinear2 == null) {
            Log.e(TAG, "Context ou dashScrollLinear2 é null");
            return;
        }
        
        try {
            // Usar a classe utilitária para criar o card moderno
            View cardView = LeadCardHelper.createModernLeadCard(
                getContext(), 
                cont, 
                dashScrollLinear2, 
                cont_dict, 
                this
            );
            
            if (cardView == null) {
                // Fallback para método simples se houver erro
                addSimpleDashboardCard(cont);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao criar card de lead para: " + cont.nome, e);
            // Fallback para método simples se houver erro
            addSimpleDashboardCard(cont);
        }
    }
    
    /**
     * Método de fallback para criar card simples no dashboard
     */
    private void addSimpleDashboardCard(Contato cont) {
        if (getContext() == null || dashScrollLinear2 == null) return;
        
        int dp_16 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
        int dp_12 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());
        int dp_8 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        int dp_4 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());

        String st_name = cont.nome;
        String st_alert = determineLeadPriority(cont);
        String st_status = cont.interesse;

        TextView name = new TextView(getContext());
        name.setId(View.generateViewId());
        RelativeLayout.LayoutParams nm_para = new RelativeLayout.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);
        nm_para.topMargin = dp_4;
        name.setGravity(Gravity.START);
        name.setLayoutParams(nm_para);
        name.setText(st_name);
        name.setTypeface(name.getTypeface(), Typeface.BOLD);

        TextView alert = new TextView(getContext());
        alert.setId(View.generateViewId());
        RelativeLayout.LayoutParams al_para = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        al_para.topMargin = dp_4;
        alert.setGravity(Gravity.END);
        alert.setLayoutParams(al_para);
        alert.setText(st_alert);

        TextView status_time = new TextView(getContext());
        status_time.setId(View.generateViewId());
        RelativeLayout.LayoutParams st_para = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        st_para.topMargin = dp_8;
        st_para.addRule(RelativeLayout.BELOW, name.getId());
        status_time.setLayoutParams(st_para);
        status_time.setText(st_status);
        status_time.setGravity(Gravity.START);

        TextView visita = new TextView(getContext());
        visita.setId(View.generateViewId());
        RelativeLayout.LayoutParams vt_para = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        vt_para.topMargin = dp_8;
        vt_para.addRule(RelativeLayout.BELOW, alert.getId());
        visita.setLayoutParams(vt_para);
        visita.setGravity(Gravity.END);

        RelativeLayout rl = new RelativeLayout(getContext());
        rl.setId(View.generateViewId());
        RelativeLayout.LayoutParams rl_para = new RelativeLayout.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);
        rl.setLayoutParams(rl_para);
        rl.setPadding(dp_16, dp_16, dp_16, dp_16);

        CardView cv = new CardView(getContext());
        cv.setId(View.generateViewId());
        CardView.LayoutParams cvl = new CardView.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);
        cvl.setMargins(0, 0, 0, dp_12);
        cv.setCardElevation(dp_4);
        cv.setRadius(dp_8);
        cv.setLayoutParams(cvl);

        rl.addView(name);
        rl.addView(alert);
        rl.addView(status_time);
        rl.addView(visita);

        cv.addView(rl);
        dashScrollLinear2.addView(cv);

        cont_dict.put(cv.getId(), cont);
        cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                to_details(v);
            }
        });
        
        Log.d(TAG, "Card simples de lead adicionado no dashboard: " + cont.nome);
    }
    
    /**
     * Determina a prioridade do lead baseado nos dados
     */
    private String determineLeadPriority(Contato lead) {
        if (lead.interesse.toLowerCase().contains("Interessado")) {
            return "Alta";
        } else if (lead.interesse.toLowerCase().contains("Potencial")) {
            return "Média";
        } else {
            return "Normal";
        }
    }
    
    /**
     * Atualiza os dados do dashboard
     */
    public void refreshDashboardData() {
        Log.d(TAG, "Iniciando atualização dos dados do dashboard");
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }
        
        // Executar o carregamento de dados em thread separada para não bloquear a UI
        new Thread(new Runnable() {
            @Override
            public void run() {
        try {
            // Forçar atualização dos dados da API sempre no pull to refresh
            if (app_pointer != null) {
                Log.d(TAG, "Forçando atualização da API via pull to refresh");
                        app_pointer.forceUpdate(); // Executado em thread separada
                    }
                    
                    // Voltar para a UI thread para atualizar a interface
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
            // Recarregar métricas
            loadDashboardMetrics();
            
            // Recarregar leads recentes
            loadRecentLeads();
            
            // Atualizar interface
            setupUI();
                                    
                                    // Carregar dados atualizados na UI thread
                                    loadDashboardData();
                                    
                                    // Carregar dados do gráfico de pizza também apenas para admins
                                    if (isAdmin) {
                                        loadPieChartData();
                                        Log.d(TAG, "Recarregando gráfico de pizza para usuário admin");
                                    }
            
            Log.d(TAG, "Dashboard atualizado com sucesso");
            
        } catch (Exception e) {
                                    Log.e(TAG, "Erro ao atualizar interface do dashboard", e);
            showError("Erro ao atualizar dados");
        } finally {
            // Parar o loading do SwipeRefreshLayout
                                    if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
                Log.d(TAG, "Pull to refresh finalizado");
            }
        }
                            }
                        });
                    }
                    
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao atualizar dados da API", e);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showError("Erro ao atualizar dados");
                                if (swipeRefreshLayout != null) {
                                    swipeRefreshLayout.setRefreshing(false);
                                    Log.d(TAG, "Pull to refresh finalizado com erro");
                                }
                            }
                        });
                    }
                }
            }
        }).start();
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
     * Navega para os detalhes do lead
     */
    public void to_details(View v) {
        Contato c = cont_dict.get(v.getId());
        if (c != null && getActivity() instanceof MainActivityNova) {
            MainActivityNova mainActivity = (MainActivityNova) getActivity();
            mainActivity.navigateToLeadDetails(c);
            Log.d(TAG, "Navegando para detalhes do lead: " + c.nome);
        }
    }

    /**
     * Navega para a tela de métricas
     */
    private void navigateToMetricas() {
        Log.d(TAG, "Navegando para Métricas");
        
        if (getActivity() instanceof MainActivityNova) {
            MainActivityNova mainActivity = (MainActivityNova) getActivity();
            mainActivity.navigateToMetricas();
        }
    }

    /**
     * Navega para a tela de funil
     */
    private void navigateToFunil() {
        Log.d(TAG, "Navegando para Funil");
        
        if (getActivity() instanceof MainActivityNova) {
            MainActivityNova mainActivity = (MainActivityNova) getActivity();
            mainActivity.navigateToFunil();
        }
    }

    public String getCurrentUserEmail() {
        return userEmail;
    }

    public String getCurrentUserUid() {
        return userUid;
    }

    public boolean isCurrentUserAdmin() {
        return isAdmin;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "DashboardFragment onResume - verificando estado dos dados");
        
        // Verificar se temos dados carregados e se estão atualizados
        boolean temDadosCarregados = false;
        
        // Verificar se temos contatos carregados
        boolean temContatos = (contatos != null && !contatos.isEmpty());
        
        // Verificar se as métricas foram carregadas (verificar se não são valores padrão/zerados)
        boolean temMetricas = false;
        if (tvTaxaConversao != null) {
            String taxaText = tvTaxaConversao.getText().toString().trim();
            // Considerar que temos métricas se o texto não está vazio e não é apenas "0" ou "0.0%"
            temMetricas = !taxaText.isEmpty() && 
                         !taxaText.equals("0") && 
                         !taxaText.equals("0.0%") && 
                         !taxaText.equals("0%") && 
                         !taxaText.equals("25%"); // Valor padrão do layout
        }
        
        // Verificar se o app_pointer está disponível e se os dados estão atualizados
        boolean appPointerDisponivel = (app_pointer != null);
        boolean dadosAtualizados = appPointerDisponivel && app_pointer.updated;
        
        temDadosCarregados = temContatos || temMetricas;
        
        Log.d(TAG, "Estado detalhado - " +
                  "Contatos: " + (contatos != null ? contatos.size() : 0) + 
                  ", Tem contatos: " + temContatos + 
                  ", Tem métricas: " + temMetricas + 
                  ", Taxa conversão atual: '" + (tvTaxaConversao != null ? tvTaxaConversao.getText().toString() : "null") + "'" +
                  ", App pointer disponível: " + appPointerDisponivel +
                  ", Dados API atualizados: " + dadosAtualizados);
        
        // Recarregar se:
        // 1. Não temos dados carregados OU
        // 2. App pointer não está disponível OU  
        // 3. Dados da API não estão atualizados
        if (!temDadosCarregados || !appPointerDisponivel || !dadosAtualizados) {
            String motivo = !temDadosCarregados ? "sem dados carregados" : 
                           !appPointerDisponivel ? "app_pointer não disponível" : 
                           "dados API desatualizados";
            
            Log.d(TAG, "Recarregando dados do dashboard - Motivo: " + motivo);
            loadDashboardData();
        } else {
            Log.d(TAG, "Dados já estão carregados e atualizados, mantendo estado atual");
        }
    }

    /**
     * Carrega dados para o gráfico de pizza do dashboard
     */
    private void loadPieChartData() {
        Log.d(TAG, "Carregando dados do gráfico de pizza para o dashboard");
        
        metricsDataProvider.getPieChartData(userEmail, isAdmin, app_pointer, 
            new MetricsDataProvider.PieChartCallback() {
                @Override
                public void onSuccess(ChartData.PieChartData data) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setupDashboardPieChart(data);
                            }
                        });
                    }
                }
                
                @Override
                public void onError(String error) {
                    Log.e(TAG, "Erro ao carregar dados do gráfico de pizza: " + error);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Usar dados de fallback
                                ChartData.PieChartData fallback = 
                                    metricsDataProvider.getPieChartData(userEmail, isAdmin);
                                setupDashboardPieChart(fallback);
                            }
                        });
                    }
                }
            });
    }
    
    /**
     * Configura o gráfico de pizza moderno do dashboard
     */
    private void setupDashboardPieChart(ChartData.PieChartData chartData) {
        try {
            if (dashPieChart == null || chartData == null) {
                Log.w(TAG, "PieChart do dashboard ou dados são null");
                return;
            }
            
            // Usar a classe utilitária modernizada com configuração específica para dashboard
            PieChartHelper.setupDashboardPieChart(dashPieChart, chartData);
            
            Log.d(TAG, "Gráfico de pizza moderno do dashboard configurado com sucesso");
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao configurar gráfico de pizza moderno do dashboard", e);
        }
    }
} 