package inf311.grupo1.projetopratico.models;

public class DashboardMetrics {
    private int totalLeads;
    private int leadsNovos;
    private int leadsContatados;
    private int leadsConvertidos;
    private double taxaConversao;
    
    public DashboardMetrics() {}
    
    public DashboardMetrics(int totalLeads, int leadsNovos, int leadsContatados, 
                           int leadsConvertidos, double taxaConversao) {
        this.totalLeads = totalLeads;
        this.leadsNovos = totalLeads;
        this.leadsContatados = leadsContatados;
        this.leadsConvertidos = leadsConvertidos;
        this.taxaConversao = taxaConversao;
    }
    
    // Getters e Setters
    public int getTotalLeads() { return totalLeads; }
    public void setTotalLeads(int totalLeads) { this.totalLeads = totalLeads; }
    
    public int getLeadsNovos() { return leadsNovos; }
    public void setLeadsNovos(int leadsNovos) { this.leadsNovos = leadsNovos; }
    
    public int getLeadsContatados() { return leadsContatados; }
    public void setLeadsContatados(int leadsContatados) { this.leadsContatados = leadsContatados; }
    
    public int getLeadsConvertidos() { return leadsConvertidos; }
    public void setLeadsConvertidos(int leadsConvertidos) { this.leadsConvertidos = leadsConvertidos; }
    
    public double getTaxaConversao() { return taxaConversao; }
    public void setTaxaConversao(double taxaConversao) { this.taxaConversao = taxaConversao; }
} 