package inf311.grupo1.projetopratico.services;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;
import java.util.Map;

import inf311.grupo1.projetopratico.MainActivityNova;
import inf311.grupo1.projetopratico.R;
import inf311.grupo1.projetopratico.models.Notification;

/** 
 * 1. RECOMENDADO: Usar apenas DATA MESSAGES com dados adicionais
 *    Exemplo no servidor/Firebase Console:
 *    {
 *      "data": {
 *        "title": "Novo Lead",
 *        "body": "João Silva se interessou pelo produto",
 *        "type": "new_lead",
 *        "priority": "high",
 *        "leadId": "12345",
 *        "leadName": "João Silva",
 *        "source": "website"
 *      }
 *    }
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static final String CHANNEL_ID = "captamax_notifications";
    private static final String CHANNEL_NAME = "captaMax Notificações";
    private static final String CHANNEL_DESCRIPTION = "Notificações do sistema captaMax";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "=== MyFirebaseMessagingService CRIADO ===");
        createNotificationChannel();
        checkNotificationPermissions();
    }

    /**
     * Verifica permissões de notificação
     */
    private void checkNotificationPermissions() {
        Log.d(TAG, "=== VERIFICANDO PERMISSÕES ===");
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "⚠️ Permissão POST_NOTIFICATIONS não concedida!");
            } else {
                Log.d(TAG, "✓ Permissão POST_NOTIFICATIONS OK");
            }
        }
        
        // Verificar se notificações estão habilitadas
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (!notificationManager.areNotificationsEnabled()) {
            Log.w(TAG, "⚠️ Notificações desabilitadas pelo usuário!");
        } else {
            Log.d(TAG, "✓ Notificações habilitadas");
        }
    }

    /**
     * Chamado quando um novo token FCM é gerado
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "=== NOVO TOKEN FCM GERADO ===");
        Log.d(TAG, "Token completo: " + token);
        Log.d(TAG, "Token resumido: " + token.substring(0, Math.min(20, token.length())) + "...");
        
        // Salvar o token no Firestore para o usuário atual
        try {
            NotificationService notificationService = NotificationService.getInstance();
            notificationService.updateFCMToken(token);
            Log.d(TAG, "✓ Token enviado para NotificationService");
        } catch (Exception e) {
            Log.e(TAG, "✗ Erro ao salvar token FCM", e);
        }
        
        // Testar notificação local imediatamente
        testLocalNotification();
    }

    /**
     * Testa notificação local para verificar se o sistema está funcionando
     */
    private void testLocalNotification() {
        Log.d(TAG, "=== TESTANDO NOTIFICAÇÃO LOCAL ===");
        try {
            showSimpleNotification("captaMax - Sistema Ativo", 
                "Serviço de notificações inicializado com sucesso!");
        } catch (Exception e) {
            Log.e(TAG, "Erro no teste de notificação local", e);
        }
    }

    /**
     * Chamado quando uma mensagem é recebida
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "=== MENSAGEM FCM RECEBIDA ===");
        Log.d(TAG, "De: " + remoteMessage.getFrom());
        Log.d(TAG, "MessageId: " + remoteMessage.getMessageId());
        Log.d(TAG, "MessageType: " + remoteMessage.getMessageType());
        Log.d(TAG, "CollapseKey: " + remoteMessage.getCollapseKey());
        Log.d(TAG, "TTL: " + remoteMessage.getTtl());
        Log.d(TAG, "Prioridade original: " + remoteMessage.getOriginalPriority());
        Log.d(TAG, "Prioridade: " + remoteMessage.getPriority());
        Log.d(TAG, "SentTime: " + new Date(remoteMessage.getSentTime()));
        
        Map<String, String> data = remoteMessage.getData();
        Log.d(TAG, "Dados recebidos: " + data.size() + " itens");
        for (Map.Entry<String, String> entry : data.entrySet()) {
            Log.d(TAG, "  📋 " + entry.getKey() + " = " + entry.getValue());
        }
        
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        if (notification != null) {
            Log.d(TAG, "📢 Notificação recebida:");
            Log.d(TAG, "  Título: " + notification.getTitle());
            Log.d(TAG, "  Corpo: " + notification.getBody());
            Log.d(TAG, "  Ícone: " + notification.getIcon());
            Log.d(TAG, "  Som: " + notification.getSound());
            Log.d(TAG, "  Tag: " + notification.getTag());
            Log.d(TAG, "  Cor: " + notification.getColor());
            Log.d(TAG, "  ClickAction: " + notification.getClickAction());
        } else {
            Log.d(TAG, "📭 Sem payload de notificação");
        }

        // Processar mensagem com lógica anti-duplicação
        try {
            boolean processed = false;
            
            // PRIORIDADE 1: Se há dados adicionais (além de title/body básicos), processar APENAS como Data Message
            if (hasAdditionalData(data)) {
                Log.d(TAG, "🔄 Processando APENAS como DATA MESSAGE (dados adicionais detectados)");
                handleDataMessage(data, remoteMessage.getMessageId());
                processed = true;
                
                // Ignorar notification payload para evitar duplicação
                if (notification != null) {
                    Log.d(TAG, "ℹ️ Notification payload ignorado para evitar duplicação");
                }
            }
            // PRIORIDADE 2: Se há notification payload mas sem dados extras, processar como Notification Message
            else if (notification != null) {
                Log.d(TAG, "🔄 Processando como NOTIFICATION MESSAGE (sem dados extras)");
                handleNotificationMessage(notification, data, remoteMessage.getMessageId());
                processed = true;
            }
            // PRIORIDADE 3: Se há apenas dados básicos (title/body), processar como Data Message
            else if (!data.isEmpty()) {
                Log.d(TAG, "🔄 Processando como DATA MESSAGE (dados básicos apenas)");
                handleDataMessage(data, remoteMessage.getMessageId());
                processed = true;
            }
            
            // Fallback se nada foi processado
            if (!processed) {
                Log.w(TAG, "⚠️ Mensagem sem dados nem notificação - criando fallback");
                Notification fallbackNotification = createFallbackNotification(remoteMessage.getMessageId());
                saveNotificationToFirestore(fallbackNotification);
                showSimpleNotification("captaMax", "Nova notificação recebida");
            }
            
            // Registrar recebimento bem-sucedido
            Log.d(TAG, "✅ Processamento da mensagem FCM concluído com sucesso");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro crítico ao processar mensagem FCM", e);
            
            // Tentar salvar notificação de erro para análise
            try {
                Notification errorNotification = createErrorNotification(e, remoteMessage.getMessageId());
                saveNotificationToFirestore(errorNotification);
            } catch (Exception ex) {
                Log.e(TAG, "❌ Falha ao salvar notificação de erro", ex);
            }
            
            // Mostrar notificação de erro para debug
            showSimpleNotification("captaMax - Erro de Processamento", 
                "Erro ao processar notificação: " + e.getMessage());
        }
    }

    /**
     * Verifica se há dados adicionais além dos campos básicos de notificação
     */
    private boolean hasAdditionalData(Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            Log.d(TAG, "🔍 Nenhum dado encontrado");
            return false;
        }
        
        // Contar campos que NÃO são básicos (title, body)
        int additionalFieldsCount = 0;
        StringBuilder additionalFields = new StringBuilder();
        
        for (String key : data.keySet()) {
            if (!"title".equals(key) && !"body".equals(key)) {
                additionalFieldsCount++;
                if (additionalFields.length() > 0) {
                    additionalFields.append(", ");
                }
                additionalFields.append(key).append("=").append(data.get(key));
            }
        }
        
        boolean hasAdditional = additionalFieldsCount > 0;
        
        if (hasAdditional) {
            Log.d(TAG, "🔍 DADOS ADICIONAIS DETECTADOS: " + hasAdditional + 
                  " (campos extras: " + additionalFieldsCount + ")");
            Log.d(TAG, "📋 Campos adicionais: " + additionalFields.toString());
        } else {
            Log.d(TAG, "🔍 Apenas dados básicos (title/body) encontrados");
        }
        
        return hasAdditional;
    }

    /**
     * Processa mensagens com dados personalizados
     */
    private void handleDataMessage(Map<String, String> data, String messageId) {
        Log.d(TAG, "=== PROCESSANDO DATA MESSAGE ===");
        
        try {
            String title = data.get("title");
            String body = data.get("body");
            
            if (title == null || body == null) {
                Log.w(TAG, "⚠️ Data message incompleto - título ou corpo ausente");
                title = title != null ? title : "captaMax";
                body = body != null ? body : "Nova notificação";
            }
            
            Log.d(TAG, "📝 Processando - Título: " + title + ", Corpo: " + body);
            
            // Criar objeto Notification com tracking
            Notification notification = createNotificationFromData(data, messageId);
            Log.d(TAG, "✓ Objeto Notification criado: " + notification.getLogSummary());
            
            // Salvar no Firestore (assíncrono)
            saveNotificationToFirestore(notification);
            
            // Exibir notificação local imediatamente
            showNotification(notification);
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao processar data message", e);
            String title = data.getOrDefault("title", "CaptaMax");
            String body = data.getOrDefault("body", "Nova notificação (erro no processamento)");
            showSimpleNotification(title, body);
        }
    }

    /**
     * Processa mensagens de notificação padrão
     */
    private void handleNotificationMessage(RemoteMessage.Notification notification, Map<String, String> data, String messageId) {
        Log.d(TAG, "=== PROCESSANDO NOTIFICATION MESSAGE ===");
        
        try {
            String title = notification.getTitle();
            String body = notification.getBody();
            
            if (title == null) title = "captaMax";
            if (body == null) body = "Nova notificação";
            
            Log.d(TAG, "📝 Processando - Título: " + title + ", Corpo: " + body);
            
            // Criar objeto Notification com tracking completo
            String type = data.getOrDefault("type", Notification.TYPE_SYSTEM_ALERT);
            String priority = data.getOrDefault("priority", Notification.PRIORITY_NORMAL);
            
            Notification notificationObj = new Notification(title, body, type, priority, messageId);
            
            // Configurar dispositivo atual
            notificationObj.setDeviceId(getCurrentDeviceId());
            
            // Adicionar dados extras
            for (Map.Entry<String, String> entry : data.entrySet()) {
                if (!isSystemField(entry.getKey())) {
                    notificationObj.addData(entry.getKey(), entry.getValue());
                }
            }
            
            // Definir remetente se especificado
            String senderUid = data.get("senderUid");
            String senderName = data.get("senderName");
            if (senderUid != null) notificationObj.setSenderUid(senderUid);
            if (senderName != null) notificationObj.setSenderName(senderName);
            
            Log.d(TAG, "✓ Objeto Notification criado: " + notificationObj.getLogSummary());
            
            // Salvar no Firestore (assíncrono)
            saveNotificationToFirestore(notificationObj);
            
            // Exibir notificação local
            showNotification(notificationObj);
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao processar notification message", e);
            // Fallback seguro
            String title = notification.getTitle() != null ? notification.getTitle() : "captaMax";
            String body = notification.getBody() != null ? notification.getBody() : "Nova notificação";
            showSimpleNotification(title, body);
        }
    }

    /**
     * Cria um objeto Notification a partir dos dados recebidos
     */
    private Notification createNotificationFromData(Map<String, String> data, String messageId) {
        Log.d(TAG, "🏗️ Criando Notification a partir dos dados");
        
        String title = data.get("title");
        String body = data.get("body");
        String type = data.getOrDefault("type", Notification.TYPE_SYSTEM_ALERT);
        String priority = data.getOrDefault("priority", Notification.PRIORITY_NORMAL);
        
        if (title == null) title = "captaMax";
        if (body == null) body = "Nova notificação";
        
        Log.d(TAG, "📋 Dados: " + title + " | " + body + " | " + type + " | " + priority);
        
        Notification notification = new Notification(title, body, type, priority, messageId);
        
        // Configurar dispositivo atual
        notification.setDeviceId(getCurrentDeviceId());
        
        // Adicionar dados extras (excluindo campos do sistema)
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (!isSystemField(entry.getKey())) {
                notification.addData(entry.getKey(), entry.getValue());
            }
        }
        
        // Definir remetente se especificado
        String senderUid = data.get("senderUid");
        String senderName = data.get("senderName");
        if (senderUid != null) notification.setSenderUid(senderUid);
        if (senderName != null) notification.setSenderName(senderName);
        
        Log.d(TAG, "✅ Notification criada: " + notification.getLogSummary());
        return notification;
    }

    /**
     * Verifica se um campo é do sistema (não deve ser salvo em data)
     */
    private boolean isSystemField(String key) {
        return "title".equals(key) || "body".equals(key) || 
               "type".equals(key) || "priority".equals(key) ||
               "senderUid".equals(key) || "senderName".equals(key);
    }

    /**
     * Obtém um ID único do dispositivo
     */
    private String getCurrentDeviceId() {
        try {
            return Settings.Secure.getString(
                getContentResolver(), 
                Settings.Secure.ANDROID_ID
            );
        } catch (Exception e) {
            Log.w(TAG, "Erro ao obter device ID", e);
            return "unknown_device";
        }
    }

    /**
     * Salva a notificação no Firestore
     */
    private void saveNotificationToFirestore(Notification notification) {
        Log.d(TAG, "💾 Salvando notificação no Firestore...");
        try {
            NotificationService notificationService = NotificationService.getInstance();
            notificationService.saveNotification(notification, new NotificationService.NotificationCallback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "✅ Notificação salva no Firestore");
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "❌ Erro ao salvar no Firestore: " + error);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "❌ Exceção ao salvar notificação", e);
        }
    }

    /**
     * Exibe a notificação local no sistema
     */
    private void showNotification(Notification notification) {
        Log.d(TAG, "=== EXIBINDO NOTIFICAÇÃO LOCAL ===");
        Log.d(TAG, "📢 " + notification.getTitle() + ": " + notification.getBody());
        
        try {
            // Verificar permissões novamente
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                    != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "❌ Sem permissão POST_NOTIFICATIONS!");
                    return;
                }
            }
            
            Intent intent = new Intent(this, MainActivityNova.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("open_notifications", true);
            
            int requestCode = (int) System.currentTimeMillis();
            PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 
                requestCode, 
                intent, 
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
            );

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(getNotificationIcon())
                    .setContentTitle(notification.getTitle())
                    .setContentText(notification.getBody())
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent)
                    .setPriority(getNotificationPriority(notification.getPriority()))
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(notification.getBody()))
                    .setShowWhen(true)
                    .setWhen(System.currentTimeMillis());

            // Configurações visuais baseadas na prioridade
            if (notification.getPriority().equals(Notification.PRIORITY_HIGH) || 
                notification.getPriority().equals(Notification.PRIORITY_URGENT)) {
                notificationBuilder.setVibrate(new long[]{0, 300, 200, 300})
                                  .setLights(0xFF0000FF, 500, 500);
            }

            // Categoria
            notificationBuilder.setCategory(getNotificationCategory(notification.getType()));

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            int notificationId = generateNotificationId();
            
            Log.d(TAG, "🔔 Exibindo notificação ID: " + notificationId);
            notificationManager.notify(notificationId, notificationBuilder.build());
            Log.d(TAG, "✅ Notificação exibida com sucesso!");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ ERRO CRÍTICO ao exibir notificação", e);
            e.printStackTrace();
        }
    }
    
    /**
     * Exibe uma notificação simples (fallback)
     */
    private void showSimpleNotification(String title, String body) {
        Log.d(TAG, "=== EXIBINDO NOTIFICAÇÃO SIMPLES ===");
        Log.d(TAG, "📢 " + title + ": " + body);
        
        try {
            // Verificar permissões
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                    != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "❌ Sem permissão para notificação simples!");
                    return;
                }
            }
            
            Intent intent = new Intent(this, MainActivityNova.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("open_notifications", true);
            
            int requestCode = (int) System.currentTimeMillis();
            PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 
                requestCode, 
                intent, 
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
            );

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(getNotificationIcon())
                    .setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                    .setShowWhen(true)
                    .setWhen(System.currentTimeMillis());

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            int notificationId = generateNotificationId();
            
            Log.d(TAG, "🔔 Exibindo notificação simples ID: " + notificationId);
            notificationManager.notify(notificationId, notificationBuilder.build());
            Log.d(TAG, "✅ Notificação simples exibida!");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ ERRO ao exibir notificação simples", e);
            e.printStackTrace();
        }
    }

    /**
     * Cria o canal de notificação 
     */
    private void createNotificationChannel() {
        Log.d(TAG, "🔧 Criando canal de notificação...");
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableLights(true);
            channel.setLightColor(0xFF0000FF);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 300, 200, 300});
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "✅ Canal criado: " + CHANNEL_ID);
            } else {
                Log.e(TAG, "❌ NotificationManager é null!");
            }
        } else {
            Log.d(TAG, "📱 Android < 8.0, canal não necessário");
        }
    }

    /**
     * Retorna o ícone da notificação
     */
    private int getNotificationIcon() {
        try {
            return R.drawable.ic_notification;
        } catch (Exception e) {
            Log.w(TAG, "⚠️ Ícone ic_notification não encontrado, usando launcher");
            return R.mipmap.ic_launcher;
        }
    }

    /**
     * Converte prioridade da notificação para prioridade do sistema
     */
    private int getNotificationPriority(String priority) {
        switch (priority) {
            case Notification.PRIORITY_LOW:
                return NotificationCompat.PRIORITY_LOW;
            case Notification.PRIORITY_HIGH:
                return NotificationCompat.PRIORITY_HIGH;
            case Notification.PRIORITY_URGENT:
                return NotificationCompat.PRIORITY_MAX;
            default:
                return NotificationCompat.PRIORITY_DEFAULT;
        }
    }

    /**
     * Retorna categoria da notificação baseada no tipo
     */
    private String getNotificationCategory(String type) {
        switch (type) {
            case Notification.TYPE_ACTIVITY_REMINDER:
                return NotificationCompat.CATEGORY_REMINDER;
            case Notification.TYPE_SYSTEM_ALERT:
                return NotificationCompat.CATEGORY_STATUS;
            case Notification.TYPE_NEW_LEAD:
            case Notification.TYPE_LEAD_UPDATE:
                return NotificationCompat.CATEGORY_EMAIL;
            default:
                return NotificationCompat.CATEGORY_MESSAGE;
        }
    }

    /**
     * Gera um ID único para a notificação
     */
    private int generateNotificationId() {
        return (int) System.currentTimeMillis();
    }

    private Notification createFallbackNotification(String messageId) {
        return new Notification("captaMax", "Nova notificação recebida", Notification.TYPE_SYSTEM_ALERT, Notification.PRIORITY_NORMAL, messageId);
    }

    private Notification createErrorNotification(Exception e, String messageId) {
        return new Notification("captaMax - Erro", "Erro ao processar notificação: " + e.getMessage(), Notification.TYPE_SYSTEM_ALERT, Notification.PRIORITY_URGENT, messageId);
    }
} 