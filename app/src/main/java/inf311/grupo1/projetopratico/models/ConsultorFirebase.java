package inf311.grupo1.projetopratico.models;

import java.util.Date;
import java.util.Map;

/**
 * Modelo que representa um consultor carregado do Firebase
 */
public class ConsultorFirebase {
    private String uid;
    private String name;
    private String email;
    private boolean isAdmin;
    private Date createdAt;
    private String fcmToken;
    
    public ConsultorFirebase() {
        // Construtor vazio necessário para Firestore
    }
    
    public ConsultorFirebase(String uid, String name, String email, boolean isAdmin) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.isAdmin = isAdmin;
        this.createdAt = new Date();
    }
    
    /**
     * Cria um ConsultorFirebase a partir de um Map do Firestore
     */
    public static ConsultorFirebase fromMap(String uid, Map<String, Object> data) {
        ConsultorFirebase consultor = new ConsultorFirebase();
        consultor.uid = uid;
        consultor.name = (String) data.get("name");
        consultor.email = (String) data.get("email");
        
        Boolean isAdminObj = (Boolean) data.get("isAdmin");
        consultor.isAdmin = isAdminObj != null ? isAdminObj : false;
        
        consultor.fcmToken = (String) data.get("fcmToken");
        
        // Tratar createdAt que pode vir como Timestamp ou Long
        Object createdAtObj = data.get("createdAt");
        if (createdAtObj instanceof com.google.firebase.Timestamp) {
            consultor.createdAt = ((com.google.firebase.Timestamp) createdAtObj).toDate();
        } else if (createdAtObj instanceof Long) {
            consultor.createdAt = new Date((Long) createdAtObj);
        } else {
            consultor.createdAt = new Date();
        }
        
        return consultor;
    }
    
    // Getters e Setters
    public String getUid() {
        return uid;
    }
    
    public void setUid(String uid) {
        this.uid = uid;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public boolean isAdmin() {
        return isAdmin;
    }
    
    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getFcmToken() {
        return fcmToken;
    }
    
    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
    
    @Override
    public String toString() {
        return "ConsultorFirebase{" +
                "uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", isAdmin=" + isAdmin +
                ", createdAt=" + createdAt +
                '}';
    }
} 