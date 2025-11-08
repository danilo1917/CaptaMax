package inf311.grupo1.projetopratico.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class NotificationPermissionHelper {
    
    private static final String TAG = "NotificationPermHelper";
    public static final int REQUEST_NOTIFICATION_PERMISSION = 1001;
    
    /**
     * Verifica se as permissões de notificação estão concedidas
     */
    public static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean hasPermission = ContextCompat.checkSelfPermission(context, 
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
            Log.d(TAG, "POST_NOTIFICATIONS permission: " + hasPermission);
            return hasPermission;
        }
        
        // Para versões anteriores ao Android 13, verificar se notificações estão habilitadas
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        boolean enabled = notificationManager.areNotificationsEnabled();
        Log.d(TAG, "Notifications enabled (pre-Android 13): " + enabled);
        return enabled;
    }
    
    /**
     * Solicita permissão de notificação
     */
    public static void requestNotificationPermission(Activity activity) {
        Log.d(TAG, "=== SOLICITANDO PERMISSÃO DE NOTIFICAÇÃO ===");
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission(activity)) {
                Log.d(TAG, "Solicitando permissão POST_NOTIFICATIONS");
                ActivityCompat.requestPermissions(activity, 
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 
                    REQUEST_NOTIFICATION_PERMISSION);
            } else {
                Log.d(TAG, "Permissão POST_NOTIFICATIONS já concedida");
            }
        } else {
            // Para versões anteriores, verificar se notificações estão desabilitadas
            if (!hasNotificationPermission(activity)) {
                Log.d(TAG, "Notificações desabilitadas, direcionando para configurações");
                openNotificationSettings(activity);
            } else {
                Log.d(TAG, "Notificações já habilitadas");
            }
        }
    }
    
    /**
     * Abre as configurações de notificação do app
     */
    public static void openNotificationSettings(Context context) {
        Log.d(TAG, "Abrindo configurações de notificação");
        
        try {
            Intent intent = new Intent();
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
            } else {
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
            }
            
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao abrir configurações de notificação", e);
            
            // Fallback: abrir configurações gerais do app
            try {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception ex) {
                Log.e(TAG, "Erro no fallback das configurações", ex);
            }
        }
    }
    
    /**
     * Verifica se o dispositivo está em modo de economia de bateria
     */
    public static boolean isBatteryOptimizationEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                Intent intent = new Intent();
                String packageName = context.getPackageName();
                android.os.PowerManager pm = (android.os.PowerManager) context.getSystemService(Context.POWER_SERVICE);
                
                if (pm != null) {
                    boolean isIgnoring = pm.isIgnoringBatteryOptimizations(packageName);
                    Log.d(TAG, "Battery optimization ignored: " + isIgnoring);
                    return !isIgnoring; // Retorna true se a otimização está ATIVA
                }
            } catch (Exception e) {
                Log.e(TAG, "Erro ao verificar otimização de bateria", e);
            }
        }
        return false;
    }
    
    /**
     * Solicita desabilitar otimização de bateria
     */
    public static void requestDisableBatteryOptimization(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                Intent intent = new Intent();
                String packageName = activity.getPackageName();
                android.os.PowerManager pm = (android.os.PowerManager) activity.getSystemService(Context.POWER_SERVICE);
                
                if (pm != null && !pm.isIgnoringBatteryOptimizations(packageName)) {
                    Log.d(TAG, "Solicitando desabilitar otimização de bateria");
                    intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + packageName));
                    activity.startActivity(intent);
                } else {
                    Log.d(TAG, "Otimização de bateria já desabilitada");
                }
            } catch (Exception e) {
                Log.e(TAG, "Erro ao solicitar desabilitar otimização de bateria", e);
            }
        }
    }
    
    /**
     * Realiza verificação completa de permissões e configurações
     */
    public static void performCompleteCheck(Activity activity) {
        Log.d(TAG, "=== VERIFICAÇÃO COMPLETA DE NOTIFICAÇÕES ===");
        
        // 1. Verificar permissão básica
        boolean hasPermission = hasNotificationPermission(activity);
        Log.d(TAG, "1. Permissão de notificação: " + (hasPermission ? "✓" : "✗"));
        
        // 2. Verificar otimização de bateria
        boolean batteryOptimized = isBatteryOptimizationEnabled(activity);
        Log.d(TAG, "2. Otimização de bateria ativa: " + (batteryOptimized ? "⚠️" : "✓"));
        
        // 3. Verificar Google Play Services
        boolean hasPlayServices = isGooglePlayServicesAvailable(activity);
        Log.d(TAG, "3. Google Play Services: " + (hasPlayServices ? "✓" : "✗"));
        
        // 4. Verificar conectividade
        boolean hasInternet = hasInternetConnection(activity);
        Log.d(TAG, "4. Conectividade: " + (hasInternet ? "✓" : "✗"));
        
        // Solicitar correções se necessário
        if (!hasPermission) {
            requestNotificationPermission(activity);
        }
        
        if (batteryOptimized) {
            requestDisableBatteryOptimization(activity);
        }
        
        Log.d(TAG, "=== FIM DA VERIFICAÇÃO ===");
    }
    
    /**
     * Verifica se Google Play Services está disponível
     */
    private static boolean isGooglePlayServicesAvailable(Context context) {
        try {
            com.google.android.gms.common.GoogleApiAvailability googleAPI = 
                com.google.android.gms.common.GoogleApiAvailability.getInstance();
            int result = googleAPI.isGooglePlayServicesAvailable(context);
            return result == com.google.android.gms.common.ConnectionResult.SUCCESS;
        } catch (Exception e) {
            Log.e(TAG, "Erro ao verificar Google Play Services", e);
            return false;
        }
    }
    
    /**
     * Verifica conectividade com a internet
     */
    private static boolean hasInternetConnection(Context context) {
        try {
            android.net.ConnectivityManager cm = (android.net.ConnectivityManager) 
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
            
            if (cm != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    android.net.Network network = cm.getActiveNetwork();
                    android.net.NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
                    return capabilities != null && 
                           capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET);
                } else {
                    android.net.NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                    return networkInfo != null && networkInfo.isConnected();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao verificar conectividade", e);
        }
        return false;
    }
    
    /**
     * Retorna status detalhado das configurações de notificação
     */
    public static String getNotificationStatus(Context context) {
        StringBuilder status = new StringBuilder();
        
        status.append("=== STATUS DAS NOTIFICAÇÕES ===\n");
        
        // Permissão
        boolean hasPermission = hasNotificationPermission(context);
        status.append("📱 Permissão: ").append(hasPermission ? "✓ Concedida" : "✗ Negada").append("\n");
        
        // Otimização de bateria
        boolean batteryOptimized = isBatteryOptimizationEnabled(context);
        status.append("🔋 Otimização de bateria: ").append(batteryOptimized ? "⚠️ Ativa" : "✓ Desabilitada").append("\n");
        
        // Google Play Services
        boolean hasPlayServices = isGooglePlayServicesAvailable(context);
        status.append("🎮 Google Play Services: ").append(hasPlayServices ? "✓ Disponível" : "✗ Indisponível").append("\n");
        
        // Conectividade
        boolean hasInternet = hasInternetConnection(context);
        status.append("🌐 Internet: ").append(hasInternet ? "✓ Conectado" : "✗ Desconectado").append("\n");
        
        // Versão do Android
        status.append("📋 Android: ").append(Build.VERSION.RELEASE).append(" (API ").append(Build.VERSION.SDK_INT).append(")\n");
        
        status.append("==============================");
        
        return status.toString();
    }
} 