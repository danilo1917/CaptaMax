package inf311.grupo1.projetopratico.models;

public class UserProfile {
    private String uid;
    private String nome;
    private String email;
    private String cargo;
    private boolean isAdmin;
    private String createdAt;
    private UserMetrics metricas;
    
    public UserProfile() {}
    
    public UserProfile(String uid, String nome, String email, 
                      String cargo, boolean isAdmin, 
                      String createdAt, UserMetrics metricas) {
        this.uid = uid;
        this.nome = nome;
        this.email = email;
        this.cargo = cargo;
        this.isAdmin = isAdmin;
        this.createdAt = createdAt;
        this.metricas = metricas;
    }
    
    // Getters e Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    
    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }
    
    public String getcreatedAt() { return createdAt; }
    public void setcreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public UserMetrics getMetricas() { return metricas; }
    public void setMetricas(UserMetrics metricas) { this.metricas = metricas; }
    
    public String getNomeExibicao() {
        if (nome != null && !nome.isEmpty()) {
            return nome;
        }
        
        if (email != null) {
            String nomeEmail = email.split("@")[0];
            return nomeEmail.substring(0, 1).toUpperCase() + nomeEmail.substring(1);
        }
        
        return "Usuário";
    }
    
    public String getCargoFormatado() {
        return isAdmin ? "Administrador" : "Consultor de Vendas";
    }
} 