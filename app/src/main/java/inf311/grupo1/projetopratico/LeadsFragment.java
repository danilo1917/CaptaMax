package inf311.grupo1.projetopratico;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import inf311.grupo1.projetopratico.services.LeadsDataProvider;
import inf311.grupo1.projetopratico.utils.AppConstants;
import inf311.grupo1.projetopratico.utils.App_fragment;
import inf311.grupo1.projetopratico.utils.LeadCardHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class LeadsFragment extends App_fragment {
    
    private static final String TAG = "LeadsFragment";
    
    private List<Contato> contatos;
    private List<Contato> all_contatos;
    private HashMap<Integer, Contato> cont_dict;
    
    // Informações do usuário
    private boolean isAdmin = false;
    private String userEmail;
    private String userUid;
    
    // UI Elements
    private SearchView searchView;
    private FloatingActionButton fabAddLead;
    private LinearLayout leadsContainer;
    private SwipeRefreshLayout swipeRefreshLayout;
    
    // Filter Buttons (not Chips)
    private Button chipTodos, chipPotenciais, chipInteressados, chipInscritosParciais, 
            chipInscritos, chipConfirmados, chipConvocados;
    
    // Estado de cadastro ativo
    private static boolean isCadastroAtivo = false;
    
    // Serviços de dados
    private LeadsDataProvider leadsDataProvider;
    
    // Filtro atual
    private String currentFilter = "todos";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "LeadsFragment onCreateView");
        
        // Inflar o layout do fragment
        View view = inflater.inflate(R.layout.fragment_leads, container, false);
        
        // Inicializar serviços
        initializeServices();
        
        // Obter informações do usuário dos argumentos
        getUserDataFromArguments();
        
        Log.d(TAG, "LeadsFragment iniciado para usuário: " + userEmail);
        
        return view;
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Inicializar dados
        initializeData();
        
        // Inicializar UI
        initializeUI(view);
        
        // Configurar listeners
        setupListeners();
        
        // Carregar leads
        loadLeads();
    }
    
    /**
     * Inicializa os serviços de dados
     */
    private void initializeServices() {
        leadsDataProvider = LeadsDataProvider.getInstance();
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
     * Inicializa as estruturas de dados
     */
    private void initializeData() {
        all_contatos = new ArrayList<>();
        contatos = new ArrayList<>();
        cont_dict = new HashMap<>();
        
        Log.d(TAG, "Estruturas de dados inicializadas");
    }
    
    /**
     * Inicializa os elementos da UI
     */
    private void initializeUI(View view) {
        searchView = view.findViewById(R.id.lead_search_bar);
        fabAddLead = view.findViewById(R.id.fab_add_lead);
        leadsContainer = view.findViewById(R.id.leads_container);
        swipeRefreshLayout = view.findViewById(R.id.leads_swipe_refresh);
        
        // Inicializar chips de filtro
        chipTodos = view.findViewById(R.id.leads_filter1);
        chipPotenciais = view.findViewById(R.id.leads_filter2);
        chipInteressados = view.findViewById(R.id.leads_filter3);
        chipInscritosParciais = view.findViewById(R.id.leads_filter4);
        chipInscritos = view.findViewById(R.id.leads_filter5);
        chipConfirmados = view.findViewById(R.id.leads_filter6);
        chipConvocados = view.findViewById(R.id.leads_filter7);
        
        // Configurar pull to refresh
        setupPullToRefresh();
        
        Log.d(TAG, "UI inicializada");
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
                    Log.d(TAG, "Pull to refresh ativado - atualizando leads");
                    refreshLeads();
                }
            });
            
            Log.d(TAG, "Pull to refresh configurado para leads");
        } else {
            Log.w(TAG, "SwipeRefreshLayout não encontrado");
        }
    }
    
    /**
     * Configura os listeners dos elementos
     */
    private void setupListeners() {
        // Search listener
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    filterLeads(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (newText.isEmpty()) {
                        filterLeads("");
                    }
                    return false;
                }
            });
        }
        
        // FAB listener
        if (fabAddLead != null) {
            fabAddLead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigateToNovoLead();
                }
            });
        }
        
        // Filter chips listeners
        setupFilterChips();
        
        Log.d(TAG, "Listeners configurados");
    }
    
    /**
     * Configura os chips de filtro
     */
    private void setupFilterChips() {
        View.OnClickListener filterListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleFilterClick((Button) v);
            }
        };
        
        if (chipTodos != null) chipTodos.setOnClickListener(filterListener);
        if (chipPotenciais != null) chipPotenciais.setOnClickListener(filterListener);
        if (chipInteressados != null) chipInteressados.setOnClickListener(filterListener);
        if (chipInscritosParciais != null) chipInscritosParciais.setOnClickListener(filterListener);
        if (chipInscritos != null) chipInscritos.setOnClickListener(filterListener);
        if (chipConfirmados != null) chipConfirmados.setOnClickListener(filterListener);
        if (chipConvocados != null) chipConvocados.setOnClickListener(filterListener);
        
        Log.d(TAG, "Chips de filtro configurados");
    }
    
    /**
     * Manipula clique nos chips de filtro
     */
    private void handleFilterClick(Button clickedChip) {
        resetAllChips();
        clickedChip.setSelected(true);
        
        String filtro = clickedChip.getText().toString().toLowerCase();
        applyFilter(filtro);
        
        Log.d(TAG, "Filtro aplicado: " + filtro);
    }
    
    /**
     * Carrega e exibe os leads usando o provedor de dados
     */
    private void loadLeads() {
        try {
            // Obter leads do provedor de dados
            if ("todos".equals(currentFilter)) {
                all_contatos = leadsDataProvider.getAllLeads(userEmail, isAdmin,app_pointer);
                contatos = new ArrayList<>(all_contatos);
            } else {
                contatos = leadsDataProvider.getLeadsByStatus(userEmail, isAdmin, currentFilter,app_pointer);
                if (all_contatos.isEmpty()) {
                    all_contatos = leadsDataProvider.getAllLeads(userEmail, isAdmin,app_pointer);
                }
            }
            
            // Limpar e recriar cards
            clearCards();
            
            for (Contato contato : contatos) {
                add_lead_card(contato);
            }
            
            Log.d(TAG, "Leads carregados: " + contatos.size() + " (filtro: " + currentFilter + ")");
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao carregar leads", e);
            showError("Erro ao carregar leads");
            
            // Fallback para lista vazia
            contatos = new ArrayList<>();
            all_contatos = new ArrayList<>();
            clearCards();
        }
    }
    
    /**
     * Aplica filtro baseado no chip selecionado
     */
    private void applyFilter(String filtro) {
        currentFilter = filtro;
        loadLeads();
        
        Log.d(TAG, "Filtro aplicado: " + filtro);
    }
    
    /**
     * Filtra leads por texto de busca
     */
    public void filterLeads(String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                // Se não há busca, aplicar filtro atual
                loadLeads();
                return;
            }
            
            // Buscar leads usando o provedor
            List<Contato> resultados = leadsDataProvider.searchLeads(userEmail, isAdmin, query.trim(),app_pointer);
            
            // Atualizar lista de contatos
            contatos = resultados;
            
            // Limpar e recriar cards
            clearCards();
            
            for (Contato contato : contatos) {
                add_lead_card(contato);
            }
            
            Log.d(TAG, "Busca realizada: '" + query + "' - " + resultados.size() + " resultados");
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao filtrar leads", e);
            showError("Erro ao buscar leads");
        }
    }
    
    /**
     * Atualiza os dados dos leads
     */
    public void refreshLeads() {
        Log.d(TAG, "Iniciando atualização dos dados dos leads");
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
            // Resetar filtros
        currentFilter = "todos";
        resetAllChips();
        if (chipTodos != null) {
            chipTodos.setSelected(true);
        }
            
            // Recarregar leads
        loadLeads();
            
            Log.d(TAG, "Leads atualizados com sucesso");
            
        } catch (Exception e) {
                                    Log.e(TAG, "Erro ao atualizar interface dos leads", e);
            showError("Erro ao atualizar leads");
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
                                showError("Erro ao atualizar leads");
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
     * Obtém estatísticas dos leads
     */
    public void loadLeadsStats() {
        try {
            LeadsDataProvider.LeadsStats stats = leadsDataProvider.getLeadsStats(userEmail, isAdmin,app_pointer);
            
            // TODO: Implementar exibição das estatísticas na interface
            // Por exemplo, atualizar TextViews com os valores das estatísticas
            
            Log.d(TAG, "Estatísticas carregadas - Total: " + stats.getTotal() +
                      ", Potenciais: " + stats.getPotenciais() +
                      ", Interessados: " + stats.getInteressados() +
                      ", Inscritos Parciais: " + stats.getInscritosParciais() +
                      ", Inscritos: " + stats.getInscritos() +
                      ", Confirmados: " + stats.getConfirmados() +
                      ", Convocados: " + stats.getConvocados() +
                      ", Matriculados: " + stats.getMatriculados());
                      
        } catch (Exception e) {
            Log.e(TAG, "Erro ao carregar estatísticas dos leads", e);
        }
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
     * Navega para a tela de novo lead
     */
    private void navigateToNovoLead() {
        if (getActivity() instanceof MainActivityNova) {
            MainActivityNova mainActivity = (MainActivityNova) getActivity();
            mainActivity.novo_lead(null); // Usar o método da MainActivity
            Log.d(TAG, "Navegando para novo lead");
        }
    }
    
    /**
     * Controla o estado do FAB baseado no cadastro ativo
     */
    private void setFabEnabled(boolean enabled) {
        if (fabAddLead != null) {
            fabAddLead.setEnabled(enabled);
            fabAddLead.setAlpha(enabled ? 1.0f : 0.5f);
        }
    }
    
    /**
     * Define se o cadastro está ativo
     */
    public static void setCadastroAtivo(boolean ativo) {
        isCadastroAtivo = ativo;
    }
    
    /**
     * Verifica se o cadastro está ativo
     */
    public static boolean isCadastroAtivo() {
        return isCadastroAtivo;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        if (isCadastroAtivo()) {
            setFabEnabled(false);
            Log.d(TAG, "FAB desabilitado - cadastro ativo");
        } else {
            setFabEnabled(true);
            Log.d(TAG, "FAB habilitado");
        }
    }

    /**
     * Adiciona um card de lead na interface usando a classe utilitária
     */
    public void add_lead_card(Contato cont) {
        if (getContext() == null || leadsContainer == null) {
            Log.e(TAG, "Context ou leadsContainer é null");
            return;
        }
        
        try {
            // Usar a classe utilitária para criar o card moderno
            View cardView = LeadCardHelper.createModernLeadCard(
                getContext(), 
                cont, 
                leadsContainer, 
                cont_dict, 
                this
            );
            
            if (cardView == null) {
                // Fallback para método original se houver erro
                addSimpleLeadCard(cont);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao criar card de lead para: " + cont.nome, e);
            // Fallback para método original se houver erro
            addSimpleLeadCard(cont);
        }
    }
    
    /**
     * Método de fallback para criar card simples se houver erro
     */
    private void addSimpleLeadCard(Contato cont) {
        if (getContext() == null || leadsContainer == null) return;
        
        int dp_16 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
        int dp_12 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());
        int dp_8 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        int dp_4 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());

        String st_name = cont.nome;
        String st_email = cont.email;
        String st_phone = isTelefoneValido(cont.telefone) ? cont.telefone : "Telefone não informado";
        String st_interest = cont.interesse;
        String st_time = cont.ultimo_contato != null ? 
            DateUtils.getRelativeTimeSpanString(cont.ultimo_contato.getTime()).toString() : "Sem contato";

        CardView cv = new CardView(getContext());
        cv.setId(View.generateViewId());
        CardView.LayoutParams cvl = new CardView.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT, CardView.LayoutParams.WRAP_CONTENT);
        cvl.setMargins(0, 0, 0, dp_12);
        cv.setCardElevation(dp_4);
        cv.setRadius(dp_8);
        cv.setLayoutParams(cvl);

        RelativeLayout rl = new RelativeLayout(getContext());
        rl.setId(View.generateViewId());
        rl.setPadding(dp_16, dp_16, dp_16, dp_16);

        TextView name = new TextView(getContext());
        name.setId(View.generateViewId());
        RelativeLayout.LayoutParams nameParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        name.setLayoutParams(nameParams);
        name.setText(st_name);
        name.setTypeface(name.getTypeface(), Typeface.BOLD);
        name.setTextSize(16);

        TextView time = new TextView(getContext());
        time.setId(View.generateViewId());
        RelativeLayout.LayoutParams timeParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        timeParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        time.setLayoutParams(timeParams);
        time.setText(st_time);
        time.setTextSize(12);

        TextView email = new TextView(getContext());
        email.setId(View.generateViewId());
        RelativeLayout.LayoutParams emailParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        emailParams.addRule(RelativeLayout.BELOW, name.getId());
        emailParams.topMargin = dp_4;
        email.setLayoutParams(emailParams);
        email.setText(st_email);
        email.setTextSize(14);

        TextView phone = new TextView(getContext());
        phone.setId(View.generateViewId());
        RelativeLayout.LayoutParams phoneParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        phoneParams.addRule(RelativeLayout.BELOW, email.getId());
        phoneParams.topMargin = dp_4;
        phone.setLayoutParams(phoneParams);
        phone.setText(st_phone);
        phone.setTextSize(14);
        
        // Adicionar funcionalidade de ligar ao clicar no telefone
        if (isTelefoneValido(cont.telefone)) {
            phone.setTextColor(ContextCompat.getColor(getContext(), R.color.primary_green));
            phone.setTypeface(phone.getTypeface(), Typeface.BOLD);
            phone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ligarTelefone(cont.telefone);
                }
            });
        } else {
            // Se não há telefone, deixar o texto em cinza e adicionar listener para mostrar toast
            phone.setTextColor(ContextCompat.getColor(getContext(), R.color.text_secondary));
            phone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), "Telefone não informado para " + cont.nome, Toast.LENGTH_SHORT).show();
                }
            });
        }

        TextView interest = new TextView(getContext());
        interest.setId(View.generateViewId());
        RelativeLayout.LayoutParams interestParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        interestParams.addRule(RelativeLayout.BELOW, phone.getId());
        interestParams.topMargin = dp_8;
        interest.setLayoutParams(interestParams);
        interest.setText(st_interest);
        interest.setTypeface(interest.getTypeface(), Typeface.BOLD);
        interest.setTextSize(14);

        rl.addView(name);
        rl.addView(time);
        rl.addView(email);
        rl.addView(phone);
        rl.addView(interest);

        cv.addView(rl);
        leadsContainer.addView(cv);

        cont_dict.put(cv.getId(), cont);
        cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                to_details(v);
            }
        });
        
        Log.d(TAG, "Card simples de lead adicionado: " + cont.nome);
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
     * Remove todos os cards da interface
     */
    public void clearCards() {
        if (leadsContainer != null) {
            leadsContainer.removeAllViews();
            cont_dict.clear();
            Log.d(TAG, "Cards limpos");
        }
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
     * Reseta todos os chips para estado não selecionado
     */
    private void resetAllChips() {
        if (chipTodos != null) chipTodos.setSelected(false);
        if (chipPotenciais != null) chipPotenciais.setSelected(false);
        if (chipInteressados != null) chipInteressados.setSelected(false);
        if (chipInscritosParciais != null) chipInscritosParciais.setSelected(false);
        if (chipInscritos != null) chipInscritos.setSelected(false);
        if (chipConfirmados != null) chipConfirmados.setSelected(false);
        if (chipConvocados != null) chipConvocados.setSelected(false);
    }

    /**
     * Abre o app de telefone para ligar para o lead
     */
    private void ligarTelefone(String telefone) {
        try {
            Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
            phoneIntent.setData(Uri.parse("tel:" + telefone));
            
            startActivity(phoneIntent);
            Log.d(TAG, "Abrindo discador para: " + telefone);
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao abrir discador", e);
            if (getActivity() != null) {
                Toast.makeText(getActivity(), "Erro ao abrir discador", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    /**
     * Verifica se o telefone é válido (não null, não vazio, não "null")
     */
    private boolean isTelefoneValido(String telefone) {
        return telefone != null && 
               !telefone.trim().isEmpty() && 
               !telefone.equalsIgnoreCase("null") &&
               !telefone.equalsIgnoreCase("undefined") &&
               !telefone.equals("0") &&
               !telefone.trim().equals("-");
    }
} 