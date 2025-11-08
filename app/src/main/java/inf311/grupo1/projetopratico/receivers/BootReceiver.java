package inf311.grupo1.projetopratico.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import inf311.grupo1.projetopratico.services.NotificationService;

public class BootReceiver extends BroadcastReceiver {
    
    private static final String TAG = "BootReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "=== BOOT RECEIVER ATIVADO ===");
        
        String action = intent.getAction();
        Log.d(TAG, "Ação recebida: " + action);
        
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || 
            "android.intent.action.QUICKBOOT_POWERON".equals(action)) {
            
            Log.d(TAG, "Dispositivo reiniciado, inicializando serviços...");
            
            try {
                // Verificar se há usuário logado
                FirebaseAuth auth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = auth.getCurrentUser();
                
                if (currentUser != null) {
                    Log.d(TAG, "Usuário autenticado encontrado: " + currentUser.getEmail());
                    
                    // Inicializar serviço de notificações
                    NotificationService notificationService = NotificationService.getInstance();
                    notificationService.initializeForCurrentUser();
                    
                    Log.d(TAG, "✓ Serviço de notificações inicializado após boot");
                } else {
                    Log.d(TAG, "Nenhum usuário autenticado encontrado");
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Erro ao inicializar serviços após boot", e);
            }
        }
    }
} 