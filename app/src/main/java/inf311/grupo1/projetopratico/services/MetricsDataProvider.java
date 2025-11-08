package inf311.grupo1.projetopratico.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import inf311.grupo1.projetopratico.App_main;
import inf311.grupo1.projetopratico.Contato;
import inf311.grupo1.projetopratico.models.ChartData;
import inf311.grupo1.projetopratico.models.ConsultorFirebase;
import inf311.grupo1.projetopratico.utils.AppConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class MetricsDataProvider {
    
    private static final String TAG = "MetricsDataProvider";
    private static MetricsDataProvider instance;
    
    private FirebaseFirestore db;
    
    // Callback interfaces
    public interface MetricsCallback {
        void onSuccess();
        void onError(String error);
    }
    
    public interface PieChartCallback {
        void onSuccess(ChartData.PieChartData data);
        void onError(String error);
    }
    
    public interface BarChartCallback {
        void onSuccess(ChartData.BarChartData data);
        void onError(String error);
    }
    
    public interface IndividualPerformanceCallback {
        void onSuccess(List<ChartData.ConsultorData> consultores);
        void onError(String error);
    }
    
    public interface TeamMetricsCallback {
        void onSuccess(TeamMetrics metrics);
        void onError(String error);
    }
    
    // Cache para consultores do Firebase
    private List<ConsultorFirebase> consultoresCache = null;
    private long lastCacheUpdate = 0;
    private static final long CACHE_DURATION = 5 * 60 * 1000; // 5 minutos
    
    private MetricsDataProvider() {
        db = FirebaseFirestore.getInstance();
    }
    
    public static MetricsDataProvider getInstance() {
        if (instance == null) {
            instance = new MetricsDataProvider();
        }
        return instance;
    }
    
    /**
     * Obtém dados para o gráfico de pizza (distribuição de leads por consultor)
     * Integra Firebase + API Rubeus
     */
    public void getPieChartData(String userEmail, boolean isAdmin, App_main app, PieChartCallback callback) {
        Log.d(TAG, "Obtendo dados do gráfico de pizza - Admin: " + isAdmin);
        
        getConsultores(isAdmin, userEmail, new ConsultoresCallback() {
            @Override
            public void onSuccess(List<ConsultorFirebase> consultores) {
                try {
                    // Garantir que os dados da API estejam atualizados
                    if (app != null && !app.updated) {
                        app.update();
                    }
                    
                    List<ChartData.ConsultorData> consultorDataList = new ArrayList<>();
                    
                    // Usar as novas cores harmônicas do AppConstants
                    String[] cores = AppConstants.CORES_GRAFICOS;
                    int corIndex = 0;
                    
                    for (ConsultorFirebase consultor : consultores) {
                        // Buscar leads deste consultor na API
                        ConsultorMetrics metrics = calcularMetricasConsultor(consultor.getUid(), app);
                        
                        String cor = cores[corIndex % cores.length];
                        ChartData.ConsultorData data = new ChartData.ConsultorData(
                            consultor.getName(),
                            metrics.getLeads(),
                            metrics.getConversoes(),
                            cor
                        );
                        
                        consultorDataList.add(data);
                        corIndex++;
                        
                        Log.d(TAG, "Consultor: " + consultor.getName() + 
                                  " - Leads: " + metrics.getLeads() + 
                                  ", Conversões: " + metrics.getConversoes());
                    }
                    
                    ChartData.PieChartData pieData = new ChartData.PieChartData(consultorDataList);
                    callback.onSuccess(pieData);
                    
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao processar dados do gráfico de pizza", e);
                    callback.onError("Erro ao calcular dados dos consultores");
                }
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Erro ao buscar consultores para gráfico de pizza " + error);
                callback.onError(error);
            }
        });
    }
    
    /**
     * Obtém dados para o gráfico de barras (leads vs conversões)
     * Integra Firebase + API Rubeus
     */
    public void getBarChartData(String userEmail, boolean isAdmin, App_main app, BarChartCallback callback) {
        Log.d(TAG, "Obtendo dados do gráfico de barras - Admin: " + isAdmin);
        
        getConsultores(isAdmin, userEmail, new ConsultoresCallback() {
            @Override
            public void onSuccess(List<ConsultorFirebase> consultores) {
                try {
                    // Garantir que os dados da API estejam atualizados
                    if (app != null && !app.updated) {
                        app.update();
                    }
                    
                    List<ChartData.ConsultorData> consultorDataList = new ArrayList<>();
                    
                    // Usar as novas cores harmônicas do AppConstants
                    String[] cores = AppConstants.CORES_GRAFICOS;
                    int corIndex = 0;
                    
                    for (ConsultorFirebase consultor : consultores) {
                        // Buscar leads deste consultor na API
                        ConsultorMetrics metrics = calcularMetricasConsultor(consultor.getUid(), app);
                        
                        String cor = cores[corIndex % cores.length];
                        ChartData.ConsultorData data = new ChartData.ConsultorData(
                            consultor.getName(),
                            metrics.getLeads(),
                            metrics.getConversoes(),
                            cor
                        );
                        
                        consultorDataList.add(data);
                        corIndex++;
                    }
                    
                    ChartData.BarChartData barData = new ChartData.BarChartData(consultorDataList);
                    callback.onSuccess(barData);
                    
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao processar dados do gráfico de barras", e);
                    callback.onError("Erro ao calcular dados dos consultores");
                }
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Erro ao buscar consultores para gráfico de barras " + error);
                callback.onError(error);
            }
        });
    }
    
    /**
     * Obtém dados de desempenho individual
     * Integra Firebase + API Rubeus
     */
    public void getIndividualPerformanceData(String userEmail, boolean isAdmin, App_main app, IndividualPerformanceCallback callback) {
        Log.d(TAG, "Obtendo dados de desempenho individual - Admin: " + isAdmin);
        
        getConsultores(isAdmin, userEmail, new ConsultoresCallback() {
            @Override
            public void onSuccess(List<ConsultorFirebase> consultores) {
                try {
                    // Garantir que os dados da API estejam atualizados
                    if (app != null && !app.updated) {
                        app.update();
                    }
                    
                    List<ChartData.ConsultorData> consultorDataList = new ArrayList<>();
                    
                    // Usar as novas cores harmônicas do AppConstants
                    String[] cores = AppConstants.CORES_GRAFICOS;
                    int corIndex = 0;
                    
                    for (ConsultorFirebase consultor : consultores) {
                        // Buscar leads deste consultor na API
                        ConsultorMetrics metrics = calcularMetricasConsultor(consultor.getUid(), app);
                        
                        String cor = cores[corIndex % cores.length];
                        ChartData.ConsultorData data = new ChartData.ConsultorData(
                            consultor.getName(),
                            metrics.getLeads(),
                            metrics.getConversoes(),
                            cor
                        );
                        
                        consultorDataList.add(data);
                        corIndex++;
                    }
                    
                    callback.onSuccess(consultorDataList);
                    
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao processar dados de desempenho individual", e);
                    callback.onError("Erro ao calcular dados dos consultores");
                }
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Erro ao buscar consultores para desempenho individual "+ error);
                callback.onError(error);
            }
        });
    }
    
    /**
     * Obtém métricas consolidadas da equipe
     * Integra Firebase + API Rubeus
     */
    public void getTeamMetrics(String userEmail, boolean isAdmin, App_main app, TeamMetricsCallback callback) {
        Log.d(TAG, "Obtendo métricas da equipe - Admin: " + isAdmin);
        
        getConsultores(isAdmin, userEmail, new ConsultoresCallback() {
            @Override
            public void onSuccess(List<ConsultorFirebase> consultores) {
                try {
                    // Garantir que os dados da API estejam atualizados
                    if (app != null && !app.updated) {
                        app.update();
                    }
                    
                    int totalLeads = 0;
                    int totalConversoes = 0;
                    int totalConsultores = consultores.size();
                    
                    for (ConsultorFirebase consultor : consultores) {
                        ConsultorMetrics metrics = calcularMetricasConsultor(consultor.getUid(), app);
                        totalLeads += metrics.getLeads();
                        totalConversoes += metrics.getConversoes();
                    }
                    
                    double taxaConversaoMedia = totalLeads > 0 ? 
                        (double) totalConversoes / totalLeads * 100.0 : 0.0;
                    
                    TeamMetrics teamMetrics = new TeamMetrics(
                        totalLeads, totalConversoes, totalConsultores, taxaConversaoMedia
                    );
                    
                    Log.d(TAG, "Métricas da equipe calculadas - Total leads: " + totalLeads + 
                              ", Conversões: " + totalConversoes + 
                              ", Taxa: " + String.format("%.1f%%", taxaConversaoMedia));
                    
                    callback.onSuccess(teamMetrics);
                    
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao processar métricas da equipe", e);
                    callback.onError("Erro ao calcular métricas da equipe");
                }
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Erro ao buscar consultores para métricas da equipe "+ error);
                callback.onError(error);
            }
        });
    }
    
    /**
     * Busca consultores do Firebase baseado na permissão do usuário
     */
    private void getConsultores(boolean isAdmin, String userEmail, ConsultoresCallback callback) {
        // Verificar cache
        if (consultoresCache != null && 
            (System.currentTimeMillis() - lastCacheUpdate) < CACHE_DURATION) {
            Log.d(TAG, "Usando consultores do cache (" + consultoresCache.size() + " itens)");
            
            if (isAdmin) {
                callback.onSuccess(consultoresCache);
            } else {
                // Filtrar apenas o usuário atual
                List<ConsultorFirebase> usuarioAtual = new ArrayList<>();
                for (ConsultorFirebase consultor : consultoresCache) {
                    if (userEmail.equals(consultor.getEmail())) {
                        usuarioAtual.add(consultor);
                        break;
                    }
                }
                callback.onSuccess(usuarioAtual);
            }
            return;
        }
        
        Log.d(TAG, "Buscando consultores do Firebase...");
        
        db.collection(AppConstants.COLLECTION_USERS)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        List<ConsultorFirebase> todosConsultores = new ArrayList<>();
                        
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                ConsultorFirebase consultor = ConsultorFirebase.fromMap(
                                    document.getId(), 
                                    document.getData()
                                );
                                todosConsultores.add(consultor);
                                
                                Log.d(TAG, "Consultor carregado: " + consultor.getName() + 
                                          " (Admin: " + consultor.isAdmin() + ")");
                                
                            } catch (Exception e) {
                                Log.w(TAG, "Erro ao converter consultor: " + document.getId(), e);
                            }
                        }
                        
                        // Atualizar cache
                        consultoresCache = todosConsultores;
                        lastCacheUpdate = System.currentTimeMillis();
                        
                        Log.d(TAG, "Consultores carregados do Firebase: " + todosConsultores.size());
                        
                        if (isAdmin) {
                            // Admin vê todos os consultores
                            callback.onSuccess(todosConsultores);
                        } else {
                            // Consultor vê apenas seus próprios dados
                            List<ConsultorFirebase> usuarioAtual = new ArrayList<>();
                            for (ConsultorFirebase consultor : todosConsultores) {
                                if (userEmail.equals(consultor.getEmail())) {
                                    usuarioAtual.add(consultor);
                                    break;
                                }
                            }
                            
                            Log.d(TAG, "Filtrando para usuário atual: " + userEmail + 
                                      " (" + usuarioAtual.size() + " resultado(s))");
                            
                            callback.onSuccess(usuarioAtual);
                        }
                        
                    } else {
                        Log.e(TAG, "Erro ao buscar consultores do Firebase", task.getException());
                        callback.onError("Erro ao carregar dados dos consultores");
                    }
                }
            });
    }
    
    /**
     * Calcula métricas de um consultor específico baseado nos leads da API
     */
    private ConsultorMetrics calcularMetricasConsultor(String consultorUid, App_main app) {
        if (app == null) {
            Log.w(TAG, "App_main é null, retornando métricas zeradas");
            return new ConsultorMetrics(0, 0);
        }
        
        List<Contato> todosLeads = app.get_leads();
        if (todosLeads == null) {
            Log.w(TAG, "Lista de leads é null, retornando métricas zeradas");
            return new ConsultorMetrics(0, 0);
        }
        
        int leadsDoConsultor = 0;
        int conversoesDoConsultor = 0;
        
        for (Contato lead : todosLeads) {
            // Verificar se este lead pertence ao consultor (comparar UID)
            if (consultorUid.equals(lead.uid)) {
                leadsDoConsultor++;
                
                // Verificar se é uma conversão (lead matriculado)
                if (lead.interesse != null && 
                    lead.interesse.toLowerCase().contains("matriculado")) {
                    conversoesDoConsultor++;
                }
            }
        }
        
        Log.d(TAG, "Métricas calculadas para UID " + consultorUid + 
                  ": " + leadsDoConsultor + " leads, " + conversoesDoConsultor + " conversões");
        
        return new ConsultorMetrics(leadsDoConsultor, conversoesDoConsultor);
    }
    
    /**
     * Limpa o cache de consultores (forçar nova busca)
     */
    public void clearCache() {
        consultoresCache = null;
        lastCacheUpdate = 0;
        Log.d(TAG, "Cache de consultores limpo");
    }
    
    // ===================== MÉTODOS SÍNCRONOS PARA COMPATIBILIDADE =====================
    
    /**
     * Métodos síncronos mantidos para compatibilidade com código existente
     * Retornam dados simulados como fallback
     */
    public ChartData.PieChartData getPieChartData(String userEmail, boolean isAdmin) {
        Log.w(TAG, "Usando método síncrono deprecado - retornando dados simulados");
        return getFallbackPieChartData(userEmail, isAdmin);
    }
    
    public ChartData.BarChartData getBarChartData(String userEmail, boolean isAdmin) {
        Log.w(TAG, "Usando método síncrono deprecado - retornando dados simulados");
        return getFallbackBarChartData(userEmail, isAdmin);
    }
    
    public TeamMetrics getTeamMetrics(String userEmail, boolean isAdmin) {
        Log.w(TAG, "Usando método síncrono deprecado - retornando dados simulados");
        return getFallbackTeamMetrics(userEmail, isAdmin);
    }
    
    // ===================== DADOS SIMULADOS PARA FALLBACK =====================
    
    public ChartData.PieChartData getFallbackPieChartData(String userEmail, boolean isAdmin) {
        List<ChartData.ConsultorData> consultores = new ArrayList<>();
        
        // Usar as novas cores harmônicas
        String[] cores = AppConstants.CORES_GRAFICOS;
        
        if (isAdmin) {
            consultores.add(new ChartData.ConsultorData("Ana", 26, 8, cores[0]));      // Verde principal
            consultores.add(new ChartData.ConsultorData("Carlos", 19, 5, cores[1]));   // Verde escuro
            consultores.add(new ChartData.ConsultorData("Juliana", 22, 7, cores[2]));  // Ciano
            consultores.add(new ChartData.ConsultorData("Roberto", 17, 4, cores[3]));  // Azul
        } else {
            String nomeConsultor = getNomeFromEmail(userEmail);
            consultores.add(new ChartData.ConsultorData(nomeConsultor, 48, 12, cores[0])); // Verde principal
        }
        
        return new ChartData.PieChartData(consultores);
    }
    
    private ChartData.BarChartData getFallbackBarChartData(String userEmail, boolean isAdmin) {
        return new ChartData.BarChartData(getFallbackPieChartData(userEmail, isAdmin).getConsultores());
    }
    
    
    private TeamMetrics getFallbackTeamMetrics(String userEmail, boolean isAdmin) {
        if (isAdmin) {
            return new TeamMetrics(0, 0, 0, 0.0);
        } else {
            return new TeamMetrics(0, 0, 0, 0.0);
        }
    }
    
    // ===================== CLASSES AUXILIARES =====================
    
    /**
     * Interface para callback de consultores
     */
    private interface ConsultoresCallback {
        void onSuccess(List<ConsultorFirebase> consultores);
        void onError(String error);
    }
    
    /**
     * Classe para métricas de um consultor
     */
    private static class ConsultorMetrics {
        private final int leads;
        private final int conversoes;
        
        public ConsultorMetrics(int leads, int conversoes) {
            this.leads = leads;
            this.conversoes = conversoes;
        }
        
        public int getLeads() {
            return leads;
        }
        
        public int getConversoes() {
            return conversoes;
        }
    }
    
    /**
     * Classe para métricas da equipe
     */
    public static class TeamMetrics {
        private final int totalLeads;
        private final int totalConversoes;
        private final int totalConsultores;
        private final double taxaConversaoMedia;
        
        public TeamMetrics(int totalLeads, int totalConversoes, int totalConsultores, double taxaConversaoMedia) {
            this.totalLeads = totalLeads;
            this.totalConversoes = totalConversoes;
            this.totalConsultores = totalConsultores;
            this.taxaConversaoMedia = taxaConversaoMedia;
        }
        
        public int getTotalLeads() {
            return totalLeads;
        }
        
        public int getTotalConversoes() {
            return totalConversoes;
        }
        
        public int getTotalConsultores() {
            return totalConsultores;
        }
        
        public double getTaxaConversaoMedia() {
            return taxaConversaoMedia;
        }
    }
    
    // ===================== MÉTODOS UTILITÁRIOS =====================
    
    private String getNomeFromEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "Usuário";
        }
        String nome = email.split("@")[0];
        return Character.toUpperCase(nome.charAt(0)) + nome.substring(1);
    }
    
    private String getNomeCompletoFromEmail(String email) {
        String nome = getNomeFromEmail(email);
        return nome;
    }
} 