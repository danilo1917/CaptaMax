package inf311.grupo1.projetopratico.services;

import android.util.Log;

import inf311.grupo1.projetopratico.App_main;
import inf311.grupo1.projetopratico.Contato;
import inf311.grupo1.projetopratico.utils.AppConstants;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LeadsDataProvider {
    
    private static LeadsDataProvider instance;
    
    private LeadsDataProvider() {}
    
    public static LeadsDataProvider getInstance() {
        if (instance == null) {
            instance = new LeadsDataProvider();
        }
        return instance;
    }
    
    /**
     * Obtém todos os leads do usuário
     * Futuramente este método fará uma chamada à API
     */
    public List<Contato> getAllLeads(String userEmail, boolean isAdmin, App_main app) {
        List<Contato> leads = new ArrayList<>();
        
        // Simula dados dinâmicos - futuramente virá da API
        if (isAdmin) {
            // Admin vê leads de todos os consultores
            leads.addAll(getAdminLeads(app));
        } else {
            // Consultor vê apenas seus leads
            leads.addAll(getConsultorLeads(app));
        }
        
        return leads;
    }
    
    /**
     * Obtém leads filtrados por status
     * Futuramente este método fará uma chamada à API
     */
    public List<Contato> getLeadsByStatus(String userEmail, boolean isAdmin, String status,App_main app) {
        List<Contato> todosLeads = getAllLeads(userEmail, isAdmin,app);
        List<Contato> leadsFiltrados = new ArrayList<>();
        
        for (Contato lead : todosLeads) {
            if (matchesStatus(lead, status)) {
                leadsFiltrados.add(lead);
            }
        }
        
        return leadsFiltrados;
    }
    
    /**
     * Busca leads por nome ou email
     * Futuramente este método fará uma chamada à API
     */
    public List<Contato> searchLeads(String userEmail, boolean isAdmin, String query,App_main app) {
        List<Contato> todosLeads = getAllLeads(userEmail, isAdmin,app
        );
        List<Contato> resultados = new ArrayList<>();
        
        String queryLower = query.toLowerCase();
        
        for (Contato lead : todosLeads) {
            if (lead.nome.toLowerCase().contains(queryLower) ||
                lead.email.toLowerCase().contains(queryLower) ||
                lead.responsavel.toLowerCase().contains(queryLower)) {
                resultados.add(lead);
            }
        }
        
        return resultados;
    }
    
    /**
     * Obtém estatísticas dos leads
     */
    public LeadsStats getLeadsStats(String userEmail, boolean isAdmin,App_main app) {
        List<Contato> leads = getAllLeads(userEmail, isAdmin,app);
        
        int potenciais = 0, interessados = 0, inscritosParciais = 0, inscritos = 0, confirmados = 0, convocados = 0, matriculados = 0;
        
        for (Contato lead : leads) {
            switch (getLeadStatus(lead)) {
                case "Potencial": potenciais++; break;
                case "Interessado": interessados++; break;
                case "Inscrito parcial": inscritosParciais++; break;
                case "Inscrito": inscritos++; break;
                case "Confirmado": confirmados++; break;
                case "Convocado": convocados++; break;
                case "Matriculado": matriculados++; break;
                default: break;
            }
        }
        
        return new LeadsStats(leads.size(), potenciais, interessados, inscritosParciais, 
                             inscritos, confirmados, convocados, matriculados);
    }
    
    /**
     * Dados de leads para admin (todos os consultores)
     */
    private List<Contato> getAdminLeads(App_main app) {
        
        return app.get_leads();
    }
    
    /**
     * Dados de leads para consultor específico
     */
    private List<Contato> getConsultorLeads(App_main app) {
        
        return app.get_leads();
    }
    
    /**
     * Verifica se um lead corresponde ao status especificado
     */
    private boolean matchesStatus(Contato lead, String status) {
        String leadStatus = getLeadStatus(lead);

        // Log.d("LeadsDataProvider", "Lead status: " + leadStatus);
        // Log.d("LeadsDataProvider", "Status: " + status);
        
        switch (status.toLowerCase()) {
            case "todos": return true;
            case "potenciais": return leadStatus.equals("Potencial");
            case "interessados": return leadStatus.equals("Interessado");
            case "inscritos parciais": return leadStatus.equals("Inscrito parcial");
            case "inscritos": return leadStatus.equals("Inscrito");
            case "confirmados": return leadStatus.equals("Confirmado");
            case "convocados": return leadStatus.equals("Convocado");
            case "matriculados": return leadStatus.equals("Matriculado");
            default: return false;
        }
    }
    

    private String getLeadStatus(Contato lead) {
        return lead.interesse;
    }
    
    /**
     * Classe para estatísticas dos leads
     */
    public static class LeadsStats {
        private int total;
        private int potenciais;
        private int interessados;
        private int inscritosParciais;
        private int inscritos;
        private int confirmados;
        private int convocados;
        private int matriculados;
        
        public LeadsStats(int total, int potenciais, int interessados, int inscritosParciais,
                         int inscritos, int confirmados, int convocados, int matriculados) {
            this.total = total;
            this.potenciais = potenciais;
            this.interessados = interessados;
            this.inscritosParciais = inscritosParciais;
            this.inscritos = inscritos;
            this.confirmados = confirmados;
            this.convocados = convocados;
            this.matriculados = matriculados;
        }
        
        // Getters
        public int getTotal() { return total; }
        public int getPotenciais() { return potenciais; }
        public int getInteressados() { return interessados; }
        public int getInscritosParciais() { return inscritosParciais; }
        public int getInscritos() { return inscritos; }
        public int getConfirmados() { return confirmados; }
        public int getConvocados() { return convocados; }
        public int getMatriculados() { return matriculados; }
    }
    
    /**
     * Obtém as opções de séries disponíveis
     */
    public String[] getSeries() {
        return AppConstants.SERIES_DISPONVEIS;
    }
    
    /**
     * Obtém os tipos de interesse disponíveis
     */
    public String[] getInterests() {
        return AppConstants.TIPOS_INTERESSE;
    }
    
    /**
     * Adiciona um novo lead
     */
    public boolean adicionarLead(Contato novoLead) {
        try {
            // Cadastrar o lead via api
            App_main.adicionarLead(novoLead);
            return true;
            
        } catch (Exception e) {
            // Log do erro e retorno false em caso de falha
            Log.e("LeadsDataProvider", "Erro ao adicionar lead: " + e.getMessage());
            return false;
        }
    }
} 