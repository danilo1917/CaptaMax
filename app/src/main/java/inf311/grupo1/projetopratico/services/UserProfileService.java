package inf311.grupo1.projetopratico.services;

import android.util.Log;

import inf311.grupo1.projetopratico.App_main;
import inf311.grupo1.projetopratico.Contato;
import inf311.grupo1.projetopratico.models.UserMetrics;
import inf311.grupo1.projetopratico.models.UserProfile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserProfileService {
    
    private static final String TAG = "UserProfileService";
    private static UserProfileService instance;
    
    // Status possíveis dos leads baseados na API Rubeus
    private static final String STATUS_POTENCIAL = "Potencial";
    private static final String STATUS_INTERESSADO = "Interessado";
    private static final String STATUS_INSCRITO_PARCIAL = "Inscrito parcial";
    private static final String STATUS_INSCRITO = "Inscrito";
    private static final String STATUS_CONFIRMADO = "Confirmado";
    private static final String STATUS_CONVOCADO = "Convocado";
    private static final String STATUS_MATRICULADO = "Matriculado";
    
    private UserProfileService() {}
    
    public static UserProfileService getInstance() {
        if (instance == null) {
            instance = new UserProfileService();
        }
        return instance;
    }
    
    /**
     * Obtém o perfil completo do usuário baseado nos dados reais da API
     */
    public UserProfile getUserProfile(String userUid, String userEmail, boolean isAdmin, App_main app) {
        try {
            // Obter métricas baseadas nos dados reais
            UserMetrics metricas = getUserMetrics(userEmail, isAdmin, app);
            
            return new UserProfile(
                userUid,
                extractNameFromEmail(userEmail),
                userEmail,
                isAdmin ? "Administrador" : "Consultor de Vendas",
                isAdmin,
                getCurrentDate(),
                metricas
            );
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao obter perfil do usuário", e);
            // Retornar perfil básico em caso de erro
            return new UserProfile(
                userUid,
                extractNameFromEmail(userEmail),
                userEmail,
                isAdmin ? "Administrador" : "Consultor de Vendas",
                isAdmin,
                getCurrentDate(),
                new UserMetrics(0, 0, 0.0, 0, 0, 0)
            );
        }
    }
    
    /**
     * Obtém o perfil completo do usuário (sobrecarga para compatibilidade)
     */
    public UserProfile getUserProfile(String userUid, String userEmail, boolean isAdmin) {
        Log.w(TAG, "Método getUserProfile chamado sem App_main - usando dados básicos");
        Log.w(TAG, "Use getUserProfile(userUid, userEmail, isAdmin, app) para obter dados reais");
        
        // Simula dados dinâmicos - futuramente virá da API
        UserMetrics metricas = getUserMetrics(userEmail, isAdmin, null);
        
        return new UserProfile(
            userUid,
            extractNameFromEmail(userEmail),
            userEmail,
            isAdmin ? "Administrador" : "Consultor de Vendas",
            isAdmin,
            getCurrentDate(),
            metricas
        );
    }
    
    /**
     * Obtém as métricas específicas do usuário baseadas nos dados reais da API
     */
    public UserMetrics getUserMetrics(String userEmail, boolean isAdmin, App_main app) {
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
            int convertidos = contarLeadsConvertidos(contatos);
            double taxaConversao = totalLeads > 0 ? (double) convertidos / totalLeads * 100.0 : 0.0;
            
            // Leads deste mês
            // int esteMes = contarLeadsEsteMes(contatos);
            int esteMes = totalLeads;
            
            // Meta mensal baseada no tipo de usuário e performance atual
            int metaMensal = calcularMetaMensal(totalLeads, isAdmin);
            
            // Dias trabalhados (baseado em dias com atividade)
            int diasTrabalhados = calcularDiasTrabalhados(contatos);
            
            Log.d(TAG, "Métricas do usuário calculadas - Total: " + totalLeads + 
                      ", Convertidos: " + convertidos + 
                      ", Taxa: " + String.format("%.1f%%", taxaConversao) +
                      ", Este mês: " + esteMes);
            
            return new UserMetrics(
                totalLeads,
                convertidos,
                taxaConversao,
                esteMes,
                metaMensal,
                diasTrabalhados
            );
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao calcular métricas do usuário", e);
            // Retornar métricas zeradas em caso de erro
            return new UserMetrics(0, 0, 0.0, 0, 0, 0);
        }
    }
    
   
    
    // Métodos auxiliares privados
    
    private int contarLeadsConvertidos(List<Contato> contatos) {
        // Considerar convertidos: "Matriculado"
        return (int) contatos.stream()
                .filter(c -> STATUS_MATRICULADO.equals(c.interesse))
                .count();
    }
    
    private int contarLeadsEsteMes(List<Contato> contatos) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date inicioMes = cal.getTime();
        
        return (int) contatos.stream()
                .filter(c -> c.ultimo_contato != null && c.ultimo_contato.after(inicioMes))
                .count();
    }
    
    private int calcularMetaMensal(int totalLeads, boolean isAdmin) {
        if (isAdmin) {
            // Meta baseada no tamanho da equipe
            return Math.max(50, totalLeads / 4); // 25% do total como meta
        } else {
            // Meta individual baseada na performance atual
            return Math.max(15, totalLeads / 8); // 12.5% do total como meta
        }
    }
    
    private int calcularDiasTrabalhados(List<Contato> contatos) {
        if (contatos.isEmpty()) return 0;
        
        // Contar dias únicos com atividade baseado na data de último contato
        return contatos.stream()
                .filter(c -> c.ultimo_contato != null)
                .map(c -> {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(c.ultimo_contato);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    return cal.getTime();
                })
                .collect(java.util.stream.Collectors.toSet())
                .size();
    }
    
    /**
     * Extrai nome a partir do email
     */
    private String extractNameFromEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "Usuário";
        }
        
        String localPart = email.substring(0, email.indexOf("@"));
        
        // Remover números e caracteres especiais, capitalizar primeira letra
        String nome = localPart.replaceAll("[0-9._-]", " ")
                              .trim()
                              .replaceAll("\\s+", " ");
        
        if (nome.isEmpty()) {
            return "Usuário";
        }
        
        // Capitalizar primeira letra de cada palavra
        String[] palavras = nome.split(" ");
        StringBuilder nomeFormatado = new StringBuilder();
        
        for (String palavra : palavras) {
            if (!palavra.isEmpty()) {
                if (nomeFormatado.length() > 0) {
                    nomeFormatado.append(" ");
                }
                nomeFormatado.append(palavra.substring(0, 1).toUpperCase())
                            .append(palavra.substring(1).toLowerCase());
            }
        }
        
        return nomeFormatado.toString();
    }
    
    /**
     * Obtém a data atual formatada
     */
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }
} 