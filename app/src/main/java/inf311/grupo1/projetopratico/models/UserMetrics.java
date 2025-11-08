package inf311.grupo1.projetopratico.models;

public class UserMetrics {
    private int totalLeads;
    private int convertidos;
    private double taxaConversao;
    private int esteMes;
    private int metaMensal;
    private int diasTrabalhados;
    
    public UserMetrics() {}
    
    public UserMetrics(int totalLeads, int convertidos, double taxaConversao, 
                      int esteMes, int metaMensal, int diasTrabalhados) {
        this.totalLeads = totalLeads;
        this.convertidos = convertidos;
        this.taxaConversao = taxaConversao;
        this.esteMes = totalLeads;
        this.metaMensal = metaMensal;
        this.diasTrabalhados = diasTrabalhados;
    }
    
    // Getters e Setters
    public int getTotalLeads() { return totalLeads; }
    public void setTotalLeads(int totalLeads) { this.totalLeads = totalLeads; }
    
    public int getConvertidos() { return convertidos; }
    public void setConvertidos(int convertidos) { this.convertidos = convertidos; }
    
    public double getTaxaConversao() { return taxaConversao; }
    public void setTaxaConversao(double taxaConversao) { this.taxaConversao = taxaConversao; }
    
    public int getEsteMes() { return esteMes; }
    public void setEsteMes(int esteMes) { this.esteMes = esteMes; }
    
    public int getMetaMensal() { return metaMensal; }
    public void setMetaMensal(int metaMensal) { this.metaMensal = metaMensal; }
    
    public int getDiasTrabalhados() { return diasTrabalhados; }
    public void setDiasTrabalhados(int diasTrabalhados) { this.diasTrabalhados = diasTrabalhados; }
    
    public String getTaxaConversaoFormatted() {
        return String.format("%.1f%%", taxaConversao);
    }
} 