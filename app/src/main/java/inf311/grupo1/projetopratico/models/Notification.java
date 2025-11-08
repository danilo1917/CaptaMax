package inf311.grupo1.projetopratico.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.firebase.Timestamp;
import inf311.grupo1.projetopratico.R;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Notification {
    private String id;
    private String title;
    private String body;
    private String type;
    private String priority;
    private boolean read;
    private Date timestamp;
    @Nullable
    private Date readAt;
    private Map<String, Object> data;
    private String senderUid;
    private String senderName;
    @Nullable
    private String deviceId; // Para rastrear o dispositivo que recebeu
    @Nullable
    private String fcmMessageId; // ID da mensagem FCM original
    private boolean delivered; // Se foi entregue com sucesso
    
    // Constantes para tipos de notificação
    public static final String TYPE_NEW_LEAD = "new_lead";
    public static final String TYPE_LEAD_UPDATE = "lead_update";
    public static final String TYPE_ACTIVITY_REMINDER = "activity_reminder";
    public static final String TYPE_GOAL_ACHIEVEMENT = "goal_achievement";
    public static final String TYPE_SYSTEM_ALERT = "system_alert";
    public static final String TYPE_TEAM_UPDATE = "team_update";
    
    // Constantes para prioridades
    public static final String PRIORITY_LOW = "low";
    public static final String PRIORITY_NORMAL = "normal";
    public static final String PRIORITY_HIGH = "high";
    public static final String PRIORITY_URGENT = "urgent";
    
    public Notification() {
        // Construtor vazio necessário para Firestore
        this.data = new HashMap<>();
        this.timestamp = new Date();
        this.read = false;
        this.delivered = false;
        this.priority = PRIORITY_NORMAL;
        this.type = TYPE_SYSTEM_ALERT;
    }
    
    public Notification(@NonNull String title, @NonNull String body, @NonNull String type) {
        this();
        this.title = title;
        this.body = body;
        this.type = type;
    }
    
    public Notification(@NonNull String title, @NonNull String body, @NonNull String type, @NonNull String priority) {
        this(title, body, type);
        this.priority = priority;
    }
    
    public Notification(@NonNull String title, @NonNull String body, @NonNull String type, @NonNull String priority, @Nullable String fcmMessageId) {
        this(title, body, type, priority);
        this.fcmMessageId = fcmMessageId;
        this.delivered = true; // Se chegou aqui, foi entregue
    }
    
    // Getters e Setters
    @Nullable
    public String getId() { return id; }
    public void setId(@Nullable String id) { this.id = id; }
    
    @Nullable
    public String getTitle() { return title; }
    public void setTitle(@Nullable String title) { this.title = title; }
    
    @Nullable
    public String getBody() { return body; }
    public void setBody(@Nullable String body) { this.body = body; }
    
    @NonNull
    public String getType() { return type != null ? type : TYPE_SYSTEM_ALERT; }
    public void setType(@Nullable String type) { this.type = type; }
    
    @NonNull
    public String getPriority() { return priority != null ? priority : PRIORITY_NORMAL; }
    public void setPriority(@Nullable String priority) { this.priority = priority; }
    
    public boolean isRead() { return read; }
    public void setRead(boolean read) { 
        this.read = read; 
        if (read && this.readAt == null) {
            this.readAt = new Date();
        }
    }
    
    @NonNull
    public Date getTimestamp() { return timestamp != null ? timestamp : new Date(); }
    public void setTimestamp(@Nullable Date timestamp) { this.timestamp = timestamp; }
    
    @Nullable
    public Date getReadAt() { return readAt; }
    public void setReadAt(@Nullable Date readAt) { this.readAt = readAt; }
    
    @NonNull
    public Map<String, Object> getData() { 
        if (this.data == null) {
            this.data = new HashMap<>();
        }
        return this.data; 
    }
    public void setData(@Nullable Map<String, Object> data) { this.data = data; }
    
    @Nullable
    public String getSenderUid() { return senderUid; }
    public void setSenderUid(@Nullable String senderUid) { this.senderUid = senderUid; }
    
    @Nullable
    public String getSenderName() { return senderName; }
    public void setSenderName(@Nullable String senderName) { this.senderName = senderName; }
    
    @Nullable
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(@Nullable String deviceId) { this.deviceId = deviceId; }
    
    @Nullable
    public String getFcmMessageId() { return fcmMessageId; }
    public void setFcmMessageId(@Nullable String fcmMessageId) { this.fcmMessageId = fcmMessageId; }
    
    public boolean isDelivered() { return delivered; }
    public void setDelivered(boolean delivered) { this.delivered = delivered; }
    
    // Métodos utilitários
    public void addData(@NonNull String key, @Nullable Object value) {
        if (this.data == null) {
            this.data = new HashMap<>();
        }
        this.data.put(key, value);
    }
    
    @Nullable
    public Object getData(@NonNull String key) {
        return this.data != null ? this.data.get(key) : null;
    }
    
    @NonNull
    public String getFormattedTime() {
        if (timestamp == null) return "agora";
        
        long now = System.currentTimeMillis();
        long notificationTime = timestamp.getTime();
        long diff = now - notificationTime;
        
        if (diff < 60000) { // menos de 1 minuto
            return "agora";
        } else if (diff < 3600000) { // menos de 1 hora
            return (diff / 60000) + "min";
        } else if (diff < 86400000) { // menos de 1 dia
            return (diff / 3600000) + "h";
        } else if (diff < 2592000000L) { // menos de 30 dias
            return (diff / 86400000) + "d";
        } else {
            return "há muito tempo";
        }
    }
    
    @NonNull
    public String getTypeDisplayName() {
        switch (getType()) {
            case TYPE_NEW_LEAD: return "Novo Lead";
            case TYPE_LEAD_UPDATE: return "Atualização de Lead";
            case TYPE_ACTIVITY_REMINDER: return "Lembrete";
            case TYPE_GOAL_ACHIEVEMENT: return "Meta Atingida";
            case TYPE_SYSTEM_ALERT: return "Alerta do Sistema";
            case TYPE_TEAM_UPDATE: return "Atualização da Equipe";
            default: return "Notificação";
        }
    }
    
    public int getPriorityColor() {
        switch (getPriority()) {
            case PRIORITY_LOW: return 0xFF4CAF50; // Verde
            case PRIORITY_NORMAL: return 0xFF2196F3; // Azul
            case PRIORITY_HIGH: return 0xFFFF9800; // Laranja
            case PRIORITY_URGENT: return 0xFFF44336; // Vermelho
            default: return 0xFF757575; // Cinza
        }
    }
    
    public int getTypeIcon() {
        switch (getType()) {
            case TYPE_NEW_LEAD: return R.drawable.ic_lead_new;
            case TYPE_LEAD_UPDATE: return R.drawable.ic_lead_update;
            case TYPE_ACTIVITY_REMINDER: return R.drawable.ic_reminder;
            case TYPE_GOAL_ACHIEVEMENT: return R.drawable.ic_goal_achievement;
            case TYPE_SYSTEM_ALERT: return R.drawable.ic_system_alert;
            case TYPE_TEAM_UPDATE: return R.drawable.ic_team_update;
            default: return R.drawable.ic_notifications_active;
        }
    }
    
    /**
     * Verifica se a notificação é válida
     */
    public boolean isValid() {
        return title != null && !title.trim().isEmpty() &&
               body != null && !body.trim().isEmpty() &&
               type != null && !type.trim().isEmpty();
    }
    
    /**
     * Retorna um resumo da notificação para logs
     */
    @NonNull
    public String getLogSummary() {
        return String.format("[%s] %s - %s (Prioridade: %s, Lida: %s)", 
            getType(), 
            title != null ? title : "Sem título", 
            body != null ? (body.length() > 50 ? body.substring(0, 50) + "..." : body) : "Sem corpo",
            getPriority(),
            isRead() ? "Sim" : "Não");
    }
    
    // Método para converter para Map (para Firestore)
    @NonNull
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("title", title);
        map.put("body", body);
        map.put("type", type);
        map.put("priority", priority);
        map.put("read", read);
        map.put("timestamp", timestamp);
        map.put("readAt", readAt);
        map.put("data", data);
        map.put("senderUid", senderUid);
        map.put("senderName", senderName);
        map.put("deviceId", deviceId);
        map.put("fcmMessageId", fcmMessageId);
        map.put("delivered", delivered);
        return map;
    }
    
    // Método para criar a partir de Map (do Firestore)
    @NonNull
    public static Notification fromMap(@Nullable String id, @NonNull Map<String, Object> map) {
        Notification notification = new Notification();
        notification.setId(id);
        notification.setTitle((String) map.get("title"));
        notification.setBody((String) map.get("body"));
        notification.setType((String) map.get("type"));
        notification.setPriority((String) map.get("priority"));
        
        // Tratar valores booleanos que podem ser null
        Object readValue = map.get("read");
        notification.setRead(readValue instanceof Boolean ? (Boolean) readValue : false);
        
        Object deliveredValue = map.get("delivered");
        notification.setDelivered(deliveredValue instanceof Boolean ? (Boolean) deliveredValue : false);
        
        // Tratar timestamps
        Object timestamp = map.get("timestamp");
        if (timestamp instanceof Timestamp) {
            notification.setTimestamp(((Timestamp) timestamp).toDate());
        } else if (timestamp instanceof Date) {
            notification.setTimestamp((Date) timestamp);
        }
        
        Object readAt = map.get("readAt");
        if (readAt instanceof Timestamp) {
            notification.setReadAt(((Timestamp) readAt).toDate());
        } else if (readAt instanceof Date) {
            notification.setReadAt((Date) readAt);
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) map.get("data");
        notification.setData(data);
        
        notification.setSenderUid((String) map.get("senderUid"));
        notification.setSenderName((String) map.get("senderName"));
        notification.setDeviceId((String) map.get("deviceId"));
        notification.setFcmMessageId((String) map.get("fcmMessageId"));
        
        return notification;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Notification that = (Notification) obj;
        return id != null ? id.equals(that.id) : that.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
    @Override
    @NonNull
    public String toString() {
        return "Notification{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", priority='" + priority + '\'' +
                ", read=" + read +
                ", timestamp=" + timestamp +
                '}';
    }
} 