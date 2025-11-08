package inf311.grupo1.projetopratico.services;

import android.util.Log;

import inf311.grupo1.projetopratico.App_main;
import inf311.grupo1.projetopratico.Contato;
import inf311.grupo1.projetopratico.utils.AppConstants;

import java.util.ArrayList;
import java.util.List;

public class FunilDataProvider {
    
    private static final String TAG = "FunilDataProvider";
    private static FunilDataProvider instance;
    
    private FunilDataProvider() {}
    
    public static FunilDataProvider getInstance() {
        if (instance == null) {
            instance = new FunilDataProvider();
        }
        return instance;
    }
    
    /**
     * Obtém as métricas do funil baseadas nos dados reais da API Rubeus
     */
    public FunilData getFunilData(String userEmail, boolean isAdmin, App_main app) {
        try {
            if (!app.updated) {
                app.update();
            }
            
            List<Contato> contatos = app.get_leads();
            if (contatos == null) {
                contatos = new ArrayList<>();
            }
            
            FunilEtapa[] etapas = calcularEtapasFunil(contatos);
            List<FunilAnalise> analises = gerarAnaliseGargalos(etapas);
            
            Log.d(TAG, "Dados do funil calculados - Total de leads: " + contatos.size());
            
            return new FunilData(etapas, analises, contatos.size());
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao calcular dados do funil", e);
            return getFunilDataFallback();
        }
    }
    
    /**
     * Obtém os dados do funil (sobrecarga para compatibilidade)
     */
    public FunilData getFunilData(String userEmail, boolean isAdmin) {
        Log.w(TAG, "Método getFunilData chamado sem App_main - retornando dados simulados");
        return getFunilDataFallback();
    }

    /**
     * Calcula as etapas do funil baseadas nos status reais
     * Usa exatamente os mesmos status da área de Leads
     */
    private FunilEtapa[] calcularEtapasFunil(List<Contato> contatos) {
        int totalLeads = contatos.size();
        
        // Contar leads por cada status real (usando AppConstants.STATUS_LEADS)
        int potenciais = contarPorStatus(contatos, AppConstants.STATUS_LEADS[0]); // "Potencial"
        int interessados = contarPorStatus(contatos, AppConstants.STATUS_LEADS[1]); // "Interessado"
        int inscritosParciais = contarPorStatus(contatos, AppConstants.STATUS_LEADS[2]); // "Inscrito parcial"
        int inscritos = contarPorStatus(contatos, AppConstants.STATUS_LEADS[3]); // "Inscrito"
        int confirmados = contarPorStatus(contatos, AppConstants.STATUS_LEADS[4]); // "Confirmado"
        int convocados = contarPorStatus(contatos, AppConstants.STATUS_LEADS[5]); // "Convocado"
        int matriculados = contarPorStatus(contatos, AppConstants.STATUS_LEADS[6]); // "Matriculado"
        
        // Calcular percentuais baseados no total
        double percPotenciais = totalLeads > 0 ? (double) potenciais / totalLeads * 100 : 0;
        double percInteressados = totalLeads > 0 ? (double) interessados / totalLeads * 100 : 0;
        double percInscritosParciais = totalLeads > 0 ? (double) inscritosParciais / totalLeads * 100 : 0;
        double percInscritos = totalLeads > 0 ? (double) inscritos / totalLeads * 100 : 0;
        double percConfirmados = totalLeads > 0 ? (double) confirmados / totalLeads * 100 : 0;
        double percConvocados = totalLeads > 0 ? (double) convocados / totalLeads * 100 : 0;
        double percMatriculados = totalLeads > 0 ? (double) matriculados / totalLeads * 100 : 0;
        
        return new FunilEtapa[] {
            new FunilEtapa(AppConstants.STATUS_LEADS[0], potenciais, (int) percPotenciais), // Potencial
            new FunilEtapa(AppConstants.STATUS_LEADS[1], interessados, (int) percInteressados), // Interessado
            new FunilEtapa(AppConstants.STATUS_LEADS[2], inscritosParciais, (int) percInscritosParciais), // Inscrito parcial
            new FunilEtapa(AppConstants.STATUS_LEADS[3], inscritos, (int) percInscritos), // Inscrito
            new FunilEtapa(AppConstants.STATUS_LEADS[4], confirmados, (int) percConfirmados), // Confirmado
            new FunilEtapa(AppConstants.STATUS_LEADS[5], convocados, (int) percConvocados), // Convocado
            new FunilEtapa(AppConstants.STATUS_LEADS[6], matriculados, (int) percMatriculados) // Matriculado
        };
    }
    
    /**
     * Gera análises de gargalos baseadas nos dados reais
     */
    private List<FunilAnalise> gerarAnaliseGargalos(FunilEtapa[] etapas) {
        List<FunilAnalise> analises = new ArrayList<>();
        
        if (etapas.length >= 7) {
            // Análise 1: Potencial → Interessado
            double convPotenciais = etapas[0].getQuantidade() > 0 ? 
                (double) etapas[1].getQuantidade() / etapas[0].getQuantidade() * 100 : 0;
            
            if (convPotenciais < 50) {
                analises.add(new FunilAnalise(
                    "Potencial → Interessado",
                    String.format("Taxa de conversão de %.1f%%. Recomendamos melhorar a qualificação inicial.", convPotenciais),
                    "critico" // vermelho
                ));
            } else if (convPotenciais < 70) {
                analises.add(new FunilAnalise(
                    "Potencial → Interessado",
                    String.format("Taxa de conversão de %.1f%%. Performance aceitável, mas pode melhorar.", convPotenciais),
                    "atencao" // amarelo
                ));
            }
            
            // Análise 2: Interessado → Inscrito
            double convInscricao = etapas[1].getQuantidade() > 0 ? 
                ((double) etapas[2].getQuantidade() + etapas[3].getQuantidade()) / etapas[1].getQuantidade() * 100 : 0;
            
            if (convInscricao < 40) {
                analises.add(new FunilAnalise(
                    "Interessado → Inscrito",
                    String.format("Taxa de inscrição de %.1f%%. Revisar processo de inscrição.", convInscricao),
                    "critico"
                ));
            } else if (convInscricao >= 70) {
                analises.add(new FunilAnalise(
                    "Interessado → Inscrito",
                    String.format("Excelente taxa de inscrição de %.1f%%! Continue assim.", convInscricao),
                    "sucesso" // verde
                ));
            }
            
            // Análise 3: Convocado → Matriculado
            double convMatriculados = etapas[5].getQuantidade() > 0 ? 
                (double) etapas[6].getQuantidade() / etapas[5].getQuantidade() * 100 : 0;
            
            if (convMatriculados >= 80) {
                analises.add(new FunilAnalise(
                    "Convocado → Matriculado",
                    String.format("Excelente taxa de matrícula de %.1f%%!", convMatriculados),
                    "sucesso"
                ));
            } else if (convMatriculados < 60) {
                analises.add(new FunilAnalise(
                    "Convocado → Matriculado",
                    String.format("Taxa de matrícula de %.1f%%. Revisar processo de fechamento.", convMatriculados),
                    "atencao"
                ));
            }
            
            // Análise 4: Performance geral
            double taxaGeralConversao = etapas[0].getQuantidade() > 0 ?
                (double) etapas[6].getQuantidade() / etapas[0].getQuantidade() * 100 : 0;
            
            if (taxaGeralConversao >= 20) {
                analises.add(new FunilAnalise(
                    "Performance Geral",
                    String.format("Taxa de conversão geral de %.1f%%. Excelente performance!", taxaGeralConversao),
                    "sucesso"
                ));
            } else if (taxaGeralConversao < 10) {
                analises.add(new FunilAnalise(
                    "Performance Geral",
                    String.format("Taxa de conversão geral de %.1f%%. Revisar todo o processo.", taxaGeralConversao),
                    "critico"
                ));
            }
        }
        
        if (analises.isEmpty()) {
            analises.add(new FunilAnalise(
                "Performance Geral",
                "Funil funcionando dentro dos parâmetros esperados. Continue monitorando.",
                "sucesso"
            ));
        }
        
        return analises;
    }
    
    /**
     * Dados de fallback em caso de erro
     */
    private FunilData getFunilDataFallback() {
        FunilEtapa[] etapas = new FunilEtapa[] {
            new FunilEtapa(AppConstants.STATUS_LEADS[0], 0, 0), // Potencial
            new FunilEtapa(AppConstants.STATUS_LEADS[1], 0, 0), // Interessado
            new FunilEtapa(AppConstants.STATUS_LEADS[2], 0, 0), // Inscrito parcial
            new FunilEtapa(AppConstants.STATUS_LEADS[3], 0, 0), // Inscrito
            new FunilEtapa(AppConstants.STATUS_LEADS[4], 0, 0), // Confirmado
            new FunilEtapa(AppConstants.STATUS_LEADS[5], 0, 0), // Convocado
            new FunilEtapa(AppConstants.STATUS_LEADS[6], 0, 0)  // Matriculado
        };
        
        List<FunilAnalise> analises = new ArrayList<>();
        analises.add(new FunilAnalise(
            "Dados Indisponíveis",
            "Não foi possível carregar dados do funil. Verifique a conexão.",
            "atencao"
        ));
        
        return new FunilData(etapas, analises, 0);
    }
    
    private int contarPorStatus(List<Contato> contatos, String status) {
        return (int) contatos.stream()
                .filter(c -> status.equals(c.interesse))
                .count();
    }
    
    public static class FunilData {
        private FunilEtapa[] etapas;
        private List<FunilAnalise> analises;
        private int totalLeads;
        
        public FunilData(FunilEtapa[] etapas, List<FunilAnalise> analises, int totalLeads) {
            this.etapas = etapas;
            this.analises = analises;
            this.totalLeads = totalLeads;
        }
        
        public FunilEtapa[] getEtapas() { return etapas; }
        public List<FunilAnalise> getAnalises() { return analises; }
        public int getTotalLeads() { return totalLeads; }
    }
    
    public static class FunilEtapa {
        private String nome;
        private int quantidade;
        private int percentual;
        
        public FunilEtapa(String nome, int quantidade, int percentual) {
            this.nome = nome;
            this.quantidade = quantidade;
            this.percentual = percentual;
        }
        
        public String getNome() { return nome; }
        public int getQuantidade() { return quantidade; }
        public int getPercentual() { return percentual; }
    }
    
    public static class FunilAnalise {
        private String titulo;
        private String descricao;
        private String tipo; // "critico", "atencao", "sucesso"
        
        public FunilAnalise(String titulo, String descricao, String tipo) {
            this.titulo = titulo;
            this.descricao = descricao;
            this.tipo = tipo;
        }
        
        public String getTitulo() { return titulo; }
        public String getDescricao() { return descricao; }
        public String getTipo() { return tipo; }
    }
} 