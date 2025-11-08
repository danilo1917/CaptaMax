package inf311.grupo1.projetopratico.models;

import java.util.List;

public class ChartData {
    
    public static class ConsultorData {
        private String nome;
        private int leads;
        private int conversoes;
        private String cor;
        
        public ConsultorData() {}
        
        public ConsultorData(String nome, int leads, int conversoes, String cor) {
            this.nome = nome;
            this.leads = leads;
            this.conversoes = conversoes;
            this.cor = cor;
        }
        
        // Getters e Setters
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        
        public int getLeads() { return leads; }
        public void setLeads(int leads) { this.leads = leads; }
        
        public int getConversoes() { return conversoes; }
        public void setConversoes(int conversoes) { this.conversoes = conversoes; }
        
        public String getCor() { return cor; }
        public void setCor(String cor) { this.cor = cor; }
        
        public double getTaxaConversao() {
            if (leads == 0) return 0.0;
            return (double) conversoes / leads * 100;
        }
    }
    
    public static class BarChartData {
        private List<ConsultorData> consultores;
        
        public BarChartData() {}
        
        public BarChartData(List<ConsultorData> consultores) {
            this.consultores = consultores;
        }
        
        public List<ConsultorData> getConsultores() { return consultores; }
        public void setConsultores(List<ConsultorData> consultores) { this.consultores = consultores; }
    }
    
    public static class PieChartData {
        private List<ConsultorData> consultores;
        
        public PieChartData() {}
        
        public PieChartData(List<ConsultorData> consultores) {
            this.consultores = consultores;
        }
        
        public List<ConsultorData> getConsultores() { return consultores; }
        public void setConsultores(List<ConsultorData> consultores) { this.consultores = consultores; }
        
        public int getTotalLeads() {
            return consultores.stream().mapToInt(ConsultorData::getLeads).sum();
        }
    }
} 