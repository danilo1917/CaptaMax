package inf311.grupo1.projetopratico;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import inf311.grupo1.projetopratico.services.FunilDataProvider;
import inf311.grupo1.projetopratico.utils.App_fragment;
import inf311.grupo1.projetopratico.utils.AppConstants;

import java.util.List;

public class FunilFragment extends App_fragment {
    
    private static final String TAG = "FunilFragment";
    
    // Informações do usuário
    private boolean isAdmin = false;
    private String userEmail;
    private String userUid;
    
    // Serviços de dados
    private FunilDataProvider funilDataProvider;
    
    // Views do funil
    private TextView tvTotalLeads;
    private TextView[] tvEtapaNomes;
    private TextView[] tvEtapaQuantidades;
    private ProgressBar[] progressBarsEtapas;
    private LinearLayout containerAnalises;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "FunilFragment onCreateView");
        
        // Inflar o layout do fragment
        View view = inflater.inflate(R.layout.fragment_funil, container, false);
        
        // Inicializar serviços
        initializeServices();
        
        // Obter informações do usuário dos argumentos
        getUserDataFromArguments();
        
        Log.d(TAG, "FunilFragment iniciado para usuário: " + userEmail);
        
        return view;
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Inicializar views
        initializeViews(view);
        
        // Executar update inicial
        update();
    }
    
    /**
     * Inicializa os serviços
     */
    private void initializeServices() {
        funilDataProvider = FunilDataProvider.getInstance();
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
     * Inicializa as views
     */
    private void initializeViews(View view) {
        // Total de leads
        tvTotalLeads = view.findViewById(R.id.funil_total_leads);
        
        // Container para análises
        containerAnalises = view.findViewById(R.id.funil_container_analises);
        
        // Inicializar arrays das etapas do funil
        initializeFunilStepViews(view);
        
        // SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.funil_swipe_refresh);
        
        // Configurar pull to refresh
        setupPullToRefresh();
        
        Log.d(TAG, "Views inicializadas");
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
                    Log.d(TAG, "Pull to refresh ativado - atualizando dados do funil");
                    update(true);
                }
            });
            
            Log.d(TAG, "Pull to refresh configurado para o funil");
        } else {
            Log.w(TAG, "SwipeRefreshLayout não encontrado");
        }
    }
    
    /**
     * Inicializa as views específicas das etapas do funil
     */
    private void initializeFunilStepViews(View view) {
        // Arrays para as 7 etapas do funil (status reais)
        tvEtapaNomes = new TextView[7];
        tvEtapaQuantidades = new TextView[7];
        progressBarsEtapas = new ProgressBar[7];
        
        // Mapear as views das etapas do funil com os IDs corretos para os 7 status reais
        tvEtapaQuantidades[0] = view.findViewById(R.id.funil_potenciais_count);        // Potencial
        tvEtapaQuantidades[1] = view.findViewById(R.id.funil_interessados_count);      // Interessado
        tvEtapaQuantidades[2] = view.findViewById(R.id.funil_inscritos_parciais_count); // Inscrito parcial
        tvEtapaQuantidades[3] = view.findViewById(R.id.funil_inscritos_count);         // Inscrito
        tvEtapaQuantidades[4] = view.findViewById(R.id.funil_confirmados_count);       // Confirmado
        tvEtapaQuantidades[5] = view.findViewById(R.id.funil_convocados_count);        // Convocado
        tvEtapaQuantidades[6] = view.findViewById(R.id.funil_matriculados_count);      // Matriculado
        
        progressBarsEtapas[0] = view.findViewById(R.id.funil_potenciais_progress);     // Potencial
        progressBarsEtapas[1] = view.findViewById(R.id.funil_interessados_progress);   // Interessado
        progressBarsEtapas[2] = view.findViewById(R.id.funil_inscritos_parciais_progress); // Inscrito parcial
        progressBarsEtapas[3] = view.findViewById(R.id.funil_inscritos_progress);      // Inscrito
        progressBarsEtapas[4] = view.findViewById(R.id.funil_confirmados_progress);    // Confirmado
        progressBarsEtapas[5] = view.findViewById(R.id.funil_convocados_progress);     // Convocado
        progressBarsEtapas[6] = view.findViewById(R.id.funil_matriculados_progress);   // Matriculado
        
        Log.d(TAG, "Views das 7 etapas do funil mapeadas corretamente para os status reais");
    }
    
    /**
     * Atualiza os dados do funil com dados reais da API
     */
    public void update() {
        update(false); // Por padrão, não força atualização
    }
    
    /**
     * Atualiza os dados do funil com dados reais da API
     * @param forceRefresh se true, força nova busca na API ignorando cache
     */
    public void update(boolean forceRefresh) {
        Log.d(TAG, "Atualizando dados do funil" + (forceRefresh ? " (forçando refresh)" : ""));
        
        if (forceRefresh) {
            // Se é um refresh forçado (pull to refresh), executar em thread separada
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(true);
            }
            
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
                                        // Obter dados reais do funil
                                        FunilDataProvider.FunilData funilData = null;
                                        if (app_pointer != null) {
                                            funilData = funilDataProvider.getFunilData(
                                                userEmail, isAdmin, app_pointer);
                                            Log.d(TAG, "Dados reais do funil carregados - Total leads: " + funilData.getTotalLeads());
                                        } else {
                                            Log.w(TAG, "app_pointer não disponível, usando fallback");
                                            funilData = funilDataProvider.getFunilData(
                                                userEmail, isAdmin);
                                            Log.w(TAG, "Usando dados simulados como fallback");
                                        }
                                        
                                        // Atualizar interface com dados
                                        updateFunilInterface(funilData);
                                        
                                    } catch (Exception e) {
                                        Log.e(TAG, "Erro ao atualizar interface do funil", e);
                                        showError("Erro ao carregar dados do funil");
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
        } else {
            // Carregamento normal (não é pull to refresh) - pode executar na UI thread
            try {
                // Garantir que o app_pointer esteja disponível e atualizado
                if (app_pointer != null) {
                    // Atualização normal - só atualiza se necessário
                    if (!app_pointer.updated) {
                        Log.d(TAG, "Atualizando dados da API antes de carregar funil");
                        app_pointer.update();
                    }
                    
                    // Obter dados reais do funil
                    FunilDataProvider.FunilData funilData = funilDataProvider.getFunilData(
                        userEmail, isAdmin, app_pointer);
                    
                    // Atualizar interface com dados reais
                    updateFunilInterface(funilData);
                    
                    Log.d(TAG, "Dados reais do funil carregados - Total leads: " + funilData.getTotalLeads());
                    
                } else {
                    Log.w(TAG, "app_pointer não disponível, usando fallback");
                    // Fallback para dados simulados
                    FunilDataProvider.FunilData funilData = funilDataProvider.getFunilData(
                        userEmail, isAdmin);
                    updateFunilInterface(funilData);
                    Log.w(TAG, "Usando dados simulados como fallback");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Erro ao atualizar dados do funil", e);
                showError("Erro ao carregar dados do funil");
            }
        }
    }
    
    /**
     * Atualiza a interface com os dados do funil
     */
    private void updateFunilInterface(FunilDataProvider.FunilData funilData) {
        if (funilData == null) {
            Log.w(TAG, "Dados do funil são nulos");
            return;
        }
        
        try {
            // Atualizar total de leads
            if (tvTotalLeads != null) {
                tvTotalLeads.setText(funilData.getTotalLeads() + " leads");
            }
            
            // Atualizar etapas do funil
            updateFunilSteps(funilData.getEtapas());
            
            // Atualizar análises
            updateAnalises(funilData.getAnalises());
            
            Log.d(TAG, "Interface do funil atualizada com dados reais");
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao atualizar interface do funil", e);
        }
    }
    
    /**
     * Atualiza as etapas do funil na interface
     */
    private void updateFunilSteps(FunilDataProvider.FunilEtapa[] etapas) {
        if (etapas == null || etapas.length == 0) {
            Log.w(TAG, "Etapas do funil são nulas ou vazias");
            return;
        }
        
        Log.d(TAG, "Atualizando " + etapas.length + " etapas do funil:");
        
        // Atualizar cada etapa do funil
        for (int i = 0; i < etapas.length && i < tvEtapaQuantidades.length; i++) {
            FunilDataProvider.FunilEtapa etapa = etapas[i];
            
            // Atualizar TextView da quantidade
            if (tvEtapaQuantidades[i] != null) {
                String texto = etapa.getQuantidade() + " leads";
                tvEtapaQuantidades[i].setText(texto);
            }
            
            // Atualizar ProgressBar
            if (progressBarsEtapas[i] != null) {
                progressBarsEtapas[i].setProgress(etapa.getPercentual());
            }
            
            Log.d(TAG, "Etapa " + i + ": " + etapa.getNome() + " = " + 
                      etapa.getQuantidade() + " leads (" + etapa.getPercentual() + "%)");
        }
        
        Log.d(TAG, "Interface das etapas do funil atualizada com dados reais");
    }
        
    /**
     * Atualiza as análises de gargalos
     */
    private void updateAnalises(List<FunilDataProvider.FunilAnalise> analises) {
        if (analises == null || analises.isEmpty()) {
            Log.w(TAG, "Análises são nulas ou vazias");
            return;
        }
        
        if (containerAnalises == null) {
            Log.w(TAG, "Container de análises não encontrado");
            return;
        }
        
        // Limpar análises existentes
        containerAnalises.removeAllViews();
        
        // Adicionar novas análises
        for (FunilDataProvider.FunilAnalise analise : analises) {
            addAnaliseCard(analise);
        }
        
        Log.d(TAG, "Análises atualizadas: " + analises.size() + " itens");
    }
    
    /**
     * Adiciona um card de análise
     */
    private void addAnaliseCard(FunilDataProvider.FunilAnalise analise) {
        if (getContext() == null || containerAnalises == null) {
            Log.w(TAG, "Context ou containerAnalises é null");
            return;
        }
        
        try {
            // Criar o layout do card
            FrameLayout frameLayout = new FrameLayout(getContext());
            
            // Definir background baseado no tipo da análise
            int backgroundResource;
            switch (analise.getTipo()) {
                case "critico":
                    backgroundResource = R.drawable.card_red;
                    break;
                case "atencao":
                    backgroundResource = R.drawable.card_yellow;
                    break;
                case "sucesso":
                    backgroundResource = R.drawable.card_green;
                    break;
                default:
                    backgroundResource = R.drawable.card_yellow;
                    break;
            }
            
            frameLayout.setBackgroundResource(backgroundResource);
            
            // Configurar layout params
            LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            frameParams.setMargins(
                dpToPx(10), dpToPx(5), dpToPx(10), dpToPx(5)
            );
            frameLayout.setLayoutParams(frameParams);
            
            // Criar LinearLayout interno
            LinearLayout linearLayout = new LinearLayout(getContext());
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
            
            // Título
            TextView tvTitulo = new TextView(getContext());
            tvTitulo.setText(analise.getTitulo());
            tvTitulo.setTextSize(16);
            tvTitulo.setTypeface(null, android.graphics.Typeface.BOLD);
            tvTitulo.setTextColor(getResources().getColor(R.color.black, null));
            
            // Descrição
            TextView tvDescricao = new TextView(getContext());
            tvDescricao.setText(analise.getDescricao());
            tvDescricao.setTextSize(14);
            tvDescricao.setTextColor(getResources().getColor(R.color.black, null));
            LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            descParams.topMargin = dpToPx(4);
            tvDescricao.setLayoutParams(descParams);
            
            // Adicionar views ao layout
            linearLayout.addView(tvTitulo);
            linearLayout.addView(tvDescricao);
            frameLayout.addView(linearLayout);
            
            // Adicionar ao container
            containerAnalises.addView(frameLayout);
            
            Log.d(TAG, "Card de análise adicionado: " + analise.getTitulo());
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao criar card de análise", e);
            // Fallback: apenas log
            Log.d(TAG, "Análise: " + analise.getTitulo() + " - " + 
                      analise.getDescricao() + " (Tipo: " + analise.getTipo() + ")");
        }
    }
    
    /**
     * Converte dp para pixels
     */
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
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
     * Força uma atualização dos dados
     */
    public void forceUpdate() {
        Log.d(TAG, "Forçando atualização dos dados");
        update();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        Log.d(TAG, "FunilFragment onResume - atualizando dados");
        
        // Atualizar dados sempre que o fragment voltar ao foco
        update();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "FunilFragment destruído");
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
} 