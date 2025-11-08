package inf311.grupo1.projetopratico.services;

import android.util.Log;

import inf311.grupo1.projetopratico.App_main;
import inf311.grupo1.projetopratico.Contato;
import inf311.grupo1.projetopratico.models.DashboardMetrics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DashboardDataProvider {
    
    private static final String TAG = "DashboardDataProvider";
    private static DashboardDataProvider instance;
    
    // Status possíveis dos leads baseados na API Rubeus
    private static final String STATUS_POTENCIAL = "Potencial";
    private static final String STATUS_INTERESSADO = "Interessado";
    private static final String STATUS_INSCRITO_PARCIAL = "Inscrito parcial";
    private static final String STATUS_INSCRITO = "Inscrito";
    private static final String STATUS_CONFIRMADO = "Confirmado";
    private static final String STATUS_CONVOCADO = "Convocado";
    private static final String STATUS_MATRICULADO = "Matriculado";
    
    private DashboardDataProvider() {}
    
    public static DashboardDataProvider getInstance() {
        if (instance == null) {
            instance = new DashboardDataProvider();
        }
        return instance;
    }
    
    /**
     * Obtém as métricas do dashboard baseadas nos dados reais da API Rubeus
     */
    public DashboardMetrics getDashboardMetrics(String userEmail, boolean isAdmin, App_main app) {
        try {
            // Garantir que os dados estejam atualizados
            if (!app.updated) {
                app.update();
            }
            
            List<Contato> contatos = app.get_leads();
            if (contatos == null) {
                contatos = new ArrayList<>();
            }
            
            // Calcular métricas baseadas nos dados reais
            int totalLeads = contatos.size();
            int leadsNovos = contarLeadsPorStatus(contatos, STATUS_POTENCIAL);
            int leadsContatados = contarLeadsContatados(contatos);
            int leadsConvertidos = contarLeadsConvertidos(contatos);
            double taxaConversao = totalLeads > 0 ? (double) leadsConvertidos / totalLeads * 100.0 : 0.0;
            
            Log.d(TAG, "Métricas calculadas - Total: " + totalLeads + 
                      ", Novos: " + leadsNovos + 
                      ", Convertidos: " + leadsConvertidos + 
                      ", Taxa: " + String.format("%.1f%%", taxaConversao));
            
            return new DashboardMetrics(
                totalLeads,
                leadsNovos,
                leadsContatados,
                leadsConvertidos,
                taxaConversao
            );
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao calcular métricas do dashboard", e);
            // Retornar métricas zeradas em caso de erro
            return new DashboardMetrics(0, 0, 0, 0, 0.0);
        }
    }
    
    /**
     * Obtém as métricas do dashboard para um usuário específico (sobrecarga para compatibilidade)
     */
    public DashboardMetrics getDashboardMetrics(String userEmail, boolean isAdmin) {
        Log.w(TAG, "Método getDashboardMetrics chamado sem App_main - retornando métricas zeradas");
        Log.w(TAG, "Use getDashboardMetrics(userEmail, isAdmin, app) para obter dados reais");
        return new DashboardMetrics(0, 0, 0, 0, 0.0);
    }
    
    /**
     * Obtém os leads recentes para exibir no dashboard
     */
    public List<Contato> getLeadsRecentes(String userEmail, boolean isAdmin, int limite, App_main app) {
        try {
            // Garantir que os dados estejam atualizados
            if (!app.updated) {
                app.update();
            }
            
            List<Contato> contatos = app.get_leads();
            if (contatos == null) {
                contatos = new ArrayList<>();
            }
            
            // Ordenar por prioridade (leads que precisam de atenção primeiro)
            contatos.sort((c1, c2) -> {
                int prioridade1 = calcularPrioridadeLead(c1);
                int prioridade2 = calcularPrioridadeLead(c2);
                return Integer.compare(prioridade2, prioridade1); // Ordem decrescente
            });
        
        // Limitar o número de resultados
        if (contatos.size() > limite) {
            return contatos.subList(0, limite);
        }
        
            Log.d(TAG, "Retornando " + contatos.size() + " leads recentes");
        return contatos;
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao obter leads recentes", e);
            return new ArrayList<>();
        }
    }
    
    // Métodos auxiliares para cálculos
    
    private int contarLeadsPorStatus(List<Contato> contatos, String status) {
        return (int) contatos.stream()
                .filter(c -> status.equals(c.interesse))
                .count();
    }
    
    private int contarLeadsContatados(List<Contato> contatos) {
        // Considerar contatados: todos exceto "Potencial"
        return (int) contatos.stream()
                .filter(c -> c.interesse != null && !STATUS_POTENCIAL.equals(c.interesse))
                .count();
    }
    
    private int contarLeadsConvertidos(List<Contato> contatos) {
        // Considerar convertidos: "Matriculado"
        return contarLeadsPorStatus(contatos, STATUS_MATRICULADO);
    }
    
    private int calcularPrioridadeLead(Contato contato) {
        // Calcular prioridade baseada no status e tempo desde último contato
        if (contato.interesse == null) return 0;
        
        int prioridadeStatus = 0;
        switch (contato.interesse) {
            case STATUS_POTENCIAL:
                prioridadeStatus = 5; // Alta prioridade - precisa ser contatado
                break;
            case STATUS_INTERESSADO:
                prioridadeStatus = 4; // Precisa de acompanhamento
                break;
            case STATUS_CONFIRMADO:
            case STATUS_CONVOCADO:
                prioridadeStatus = 3; // Precisa de atenção
                break;
            case STATUS_INSCRITO_PARCIAL:
                prioridadeStatus = 2; // Acompanhar inscrição
                break;
            case STATUS_INSCRITO:
                prioridadeStatus = 1; // Baixa prioridade
                break;
            case STATUS_MATRICULADO:
                prioridadeStatus = 0; // Menor prioridade
                break;
            default:
                prioridadeStatus = 1;
        }
        
        // Ajustar prioridade baseada no tempo sem contato
        if (contato.ultimo_contato != null) {
            long diasSemContato = (System.currentTimeMillis() - contato.ultimo_contato.getTime()) / (24 * 60 * 60 * 1000);
            if (diasSemContato > 7) {
                prioridadeStatus += 2; // Aumentar prioridade se faz tempo sem contato
            } else if (diasSemContato > 3) {
                prioridadeStatus += 1;
            }
        }
        
        return prioridadeStatus;
    }
    
   
    
   
} 