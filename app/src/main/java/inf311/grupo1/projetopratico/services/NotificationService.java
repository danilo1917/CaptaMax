package inf311.grupo1.projetopratico.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import inf311.grupo1.projetopratico.models.Notification;

public class NotificationService {
    
    private static final String TAG = "NotificationService";
    private static NotificationService instance;
    
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseMessaging messaging;
    
    // Interfaces para callbacks
    public interface NotificationCallback {
        void onSuccess();
        void onError(String error);
    }
    
    public interface NotificationListCallback {
        void onSuccess(List<Notification> notifications);
        void onError(String error);
    }
    
    public interface TokenCallback {
        void onSuccess(String token);
        void onError(String error);
    }
    
    public interface UnreadCountCallback {
        void onSuccess(int count);
        void onError(String error);
    }
    
    private NotificationService() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        messaging = FirebaseMessaging.getInstance();
    }
    
    public static synchronized NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }
    
    /**
     * Inicializa o serviço de notificações para o usuário atual
     */
    public void initializeForCurrentUser() {
        Log.d(TAG, "=== INICIALIZANDO SERVIÇO DE NOTIFICAÇÕES ===");
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "Usuário autenticado: " + currentUser.getEmail());
            // Obter e salvar o token FCM
            messaging.getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Falha ao obter token FCM", task.getException());
                            return;
                        }

                        // Obter novo token FCM
                        String token = task.getResult();
                        Log.d(TAG, "=== TOKEN FCM OBTIDO ===");
                        Log.d(TAG, "Token: " + token);
                        
                        // Salvar token no Firestore
                        updateFCMToken(token);
                    }
                });
        } else {
            Log.w(TAG, "Usuário não autenticado ao inicializar serviço");
        }
    }
    
    /**
     * Atualiza o token FCM do usuário no Firestore
     */
    public void updateFCMToken(String token) {
        Log.d(TAG, "=== ATUALIZANDO TOKEN FCM ===");
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "Usuário não autenticado para atualizar token FCM");
            return;
        }
        
        String userUid = currentUser.getUid();
        Log.d(TAG, "UID do usuário: " + userUid);
        Log.d(TAG, "Token a ser salvo: " + token);
        
        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("fcmToken", token);
        tokenData.put("tokenUpdatedAt", new Date());
        
        db.collection("users").document(userUid)
            .update(tokenData)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "✓ Token FCM atualizado com sucesso no Firestore");
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Erro ao atualizar token FCM, tentando criar documento", e);
                    // Se o documento não existir, criar um novo
                    createUserDocument(userUid, token);
                }
            });
    }
    
    /**
     * Cria documento do usuário se não existir
     */
    private void createUserDocument(String userUid, String token) {
        Log.d(TAG, "=== CRIANDO DOCUMENTO DO USUÁRIO ===");
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "Usuário não autenticado ao criar documento");
            return;
        }
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", userUid);
        userData.put("email", currentUser.getEmail());
        userData.put("fcmToken", token);
        userData.put("tokenUpdatedAt", new Date());
        userData.put("createdAt", new Date());
        
        Log.d(TAG, "Criando documento para usuário: " + currentUser.getEmail());
        
        db.collection("users").document(userUid)
            .set(userData)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "✓ Documento do usuário criado com sucesso");
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "✗ Erro ao criar documento do usuário", e);
                }
            });
    }
    
    /**
     * Salva uma notificação no Firestore
     */
    public void saveNotification(Notification notification, NotificationCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            if (callback != null) {
                callback.onError("Usuário não autenticado");
            }
            return;
        }
        
        String userUid = currentUser.getUid();
        
        db.collection("users").document(userUid).collection("notifications")
            .add(notification.toMap())
            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    Log.d(TAG, "Notificação salva com ID: " + documentReference.getId());
                    if (callback != null) {
                        callback.onSuccess();
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Erro ao salvar notificação", e);
                    if (callback != null) {
                        callback.onError(e.getMessage());
                    }
                }
            });
    }
    
    /**
     * Busca todas as notificações do usuário
     */
    public void getAllNotifications(NotificationListCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            if (callback != null) {
                callback.onError("Usuário não autenticado");
            }
            return;
        }
        
        String userUid = currentUser.getUid();
        
        db.collection("users").document(userUid).collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        List<Notification> notifications = new ArrayList<>();
                        
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Notification notification = Notification.fromMap(
                                    document.getId(), 
                                    document.getData()
                                );
                                notifications.add(notification);
                            } catch (Exception e) {
                                Log.w(TAG, "Erro ao converter notificação: " + document.getId(), e);
                            }
                        }
                        
                        Log.d(TAG, "Carregadas " + notifications.size() + " notificações");
                        if (callback != null) {
                            callback.onSuccess(notifications);
                        }
                    } else {
                        Log.w(TAG, "Erro ao buscar notificações", task.getException());
                        if (callback != null) {
                            callback.onError(task.getException().getMessage());
                        }
                    }
                }
            });
    }
    
    /**
     * Busca notificações não lidas
     */
    public void getUnreadNotifications(NotificationListCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            if (callback != null) {
                callback.onError("Usuário não autenticado");
            }
            return;
        }
        
        String userUid = currentUser.getUid();
        
        db.collection("users").document(userUid).collection("notifications")
            .whereEqualTo("read", false)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        List<Notification> notifications = new ArrayList<>();
                        
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Notification notification = Notification.fromMap(
                                    document.getId(), 
                                    document.getData()
                                );
                                notifications.add(notification);
                            } catch (Exception e) {
                                Log.w(TAG, "Erro ao converter notificação: " + document.getId(), e);
                            }
                        }
                        
                        if (callback != null) {
                            callback.onSuccess(notifications);
                        }
                    } else {
                        Log.w(TAG, "Erro ao buscar notificações não lidas", task.getException());
                        if (callback != null) {
                            callback.onError(task.getException().getMessage());
                        }
                    }
                }
            });
    }
    
    /**
     * Marca uma notificação como lida
     */
    public void markAsRead(String notificationId, NotificationCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            if (callback != null) {
                callback.onError("Usuário não autenticado");
            }
            return;
        }
        
        String userUid = currentUser.getUid();
        
        db.collection("users").document(userUid).collection("notifications")
            .document(notificationId)
            .update("read", true)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "Notificação marcada como lida: " + notificationId);
                    if (callback != null) {
                        callback.onSuccess();
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Erro ao marcar notificação como lida", e);
                    if (callback != null) {
                        callback.onError(e.getMessage());
                    }
                }
            });
    }
    
    /**
     * Marca todas as notificações como lidas
     */
    public void markAllAsRead(NotificationCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            if (callback != null) {
                callback.onError("Usuário não autenticado");
            }
            return;
        }
        
        String userUid = currentUser.getUid();
        
        // Buscar todas as notificações não lidas
        db.collection("users").document(userUid).collection("notifications")
            .whereEqualTo("read", false)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot.isEmpty()) {
                            Log.d(TAG, "Nenhuma notificação não lida encontrada");
                            if (callback != null) {
                                callback.onSuccess();
                            }
                            return;
                        }
                        
                        // Usar WriteBatch para operação atômica e mais eficiente
                        com.google.firebase.firestore.WriteBatch batch = db.batch();
                        
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            batch.update(document.getReference(), "read", true, "readAt", new Date());
                        }
                        
                        // Executar batch
                        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> batchTask) {
                                if (batchTask.isSuccessful()) {
                                    Log.d(TAG, "✅ " + querySnapshot.size() + " notificações marcadas como lidas (batch)");
                                    if (callback != null) {
                                        callback.onSuccess();
                                    }
                                } else {
                                    Log.e(TAG, "❌ Erro no batch para marcar como lidas", batchTask.getException());
                                    if (callback != null) {
                                        callback.onError("Erro ao marcar notificações: " + 
                                            (batchTask.getException() != null ? batchTask.getException().getMessage() : "Erro desconhecido"));
                                    }
                                }
                            }
                        });
                        
                    } else {
                        Log.w(TAG, "Erro ao buscar notificações não lidas", task.getException());
                        if (callback != null) {
                            callback.onError("Erro ao buscar notificações: " + 
                                (task.getException() != null ? task.getException().getMessage() : "Erro desconhecido"));
                        }
                    }
                }
            });
    }
    
    /**
     * Obtém a contagem de notificações não lidas
     */
    public void getUnreadCount(UnreadCountCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            if (callback != null) {
                callback.onError("Usuário não autenticado");
            }
            return;
        }
        
        String userUid = currentUser.getUid();
        
        db.collection("users").document(userUid).collection("notifications")
            .whereEqualTo("read", false)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        int count = task.getResult().size();
                        Log.d(TAG, "Notificações não lidas: " + count);
                        if (callback != null) {
                            callback.onSuccess(count);
                        }
                    } else {
                        Log.w(TAG, "Erro ao contar notificações não lidas", task.getException());
                        if (callback != null) {
                            callback.onError(task.getException().getMessage());
                        }
                    }
                }
            });
    }
    
    /**
     * Deleta uma notificação
     */
    public void deleteNotification(String notificationId, NotificationCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            if (callback != null) {
                callback.onError("Usuário não autenticado");
            }
            return;
        }
        
        String userUid = currentUser.getUid();
        
        db.collection("users").document(userUid).collection("notifications")
            .document(notificationId)
            .delete()
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "Notificação deletada: " + notificationId);
                    if (callback != null) {
                        callback.onSuccess();
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Erro ao deletar notificação", e);
                    if (callback != null) {
                        callback.onError(e.getMessage());
                    }
                }
            });
    }
    
    /**
     * Cria uma notificação local para testes
     */
    public void createTestNotification(String type, String priority) {
        String title, body;
        
        switch (type) {
            case Notification.TYPE_NEW_LEAD:
                title = "Novo Lead Recebido";
                body = "Um novo lead foi cadastrado no sistema e precisa de atenção.";
                break;
            case Notification.TYPE_ACTIVITY_REMINDER:
                title = "Lembrete de Atividade";
                body = "Você tem uma atividade agendada para hoje.";
                break;
            case Notification.TYPE_GOAL_ACHIEVEMENT:
                title = "Meta Atingida!";
                body = "Parabéns! Você atingiu sua meta mensal de leads.";
                break;
            default:
                title = "Notificação de Teste";
                body = "Esta é uma notificação de teste do sistema CaptaMax.";
                break;
        }
        
        Notification notification = new Notification(title, body, type, priority);
        
        saveNotification(notification, new NotificationCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Notificação de teste criada com sucesso");
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Erro ao criar notificação de teste: " + error);
            }
        });
    }
} 