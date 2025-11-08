 package inf311.grupo1.projetopratico;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;

public class EsqueciSenhaActivity extends AppCompatActivity {

    private static final String TAG = "EsqueciSenhaActivity";
    
    private FirebaseManager firebaseManager;
    
    private EditText etEmail;
    private Button btnEnviarEmail;
    private Button btnVoltarLogin;
    private ImageView ivBack;
    private TextView tvStatus;
    
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_esqueci_senha);

        firebaseManager = FirebaseManager.getInstance();
        
        initializeUI();
        
        setupListeners();
        
        Log.d(TAG, "EsqueciSenhaActivity inicializada");
    }

   
    private void initializeUI() {
        etEmail = findViewById(R.id.et_email_recuperacao);
        btnEnviarEmail = findViewById(R.id.btn_enviar_email);
        btnVoltarLogin = findViewById(R.id.btn_voltar_login);
        ivBack = findViewById(R.id.iv_back);
        tvStatus = findViewById(R.id.tv_status_recuperacao);
        
        updateUIState(false);
    }

  
    private void setupListeners() {
        if (btnEnviarEmail != null) {
            btnEnviarEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    enviarEmailRecuperacao();
                }
            });
        }
        
        if (btnVoltarLogin != null) {
            btnVoltarLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    voltarParaLogin();
                }
            });
        }
        
        if (ivBack != null) {
            ivBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    voltarParaLogin();
                }
            });
        }
    }

    /**
     * Realiza o processo de envio de email de recuperação
     */
    private void enviarEmailRecuperacao() {
        String email = etEmail.getText().toString().trim();

        if (!validarEmail(email)) {
            return;
        }

        setLoadingState(true);
        updateStatusMessage(getString(R.string.enviando_email), false);

        // Enviar email usando FirebaseManager
        firebaseManager.sendPasswordResetEmail(email, new FirebaseManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                Log.d(TAG, "Email de recuperação enviado com sucesso para: " + email);
                setLoadingState(false);
                
                updateStatusMessage(
                    getString(R.string.email_enviado_sucesso),
                    true
                );
                
                Toast.makeText(EsqueciSenhaActivity.this, 
                    "Email enviado! Verifique sua caixa de entrada.", 
                    Toast.LENGTH_LONG).show();
                
                if (btnVoltarLogin != null) {
                    btnVoltarLogin.setVisibility(View.VISIBLE);
                    btnVoltarLogin.setText(getString(R.string.btn_voltar_login));
                }
            }

            @Override
            public void onError(String errorMessage) {
                setLoadingState(false);
                Log.e(TAG, "Erro ao enviar email de recuperação: " + errorMessage);
                
                updateStatusMessage(
                    "Erro ao enviar email: " + errorMessage,
                    false
                );
                
                Toast.makeText(EsqueciSenhaActivity.this, 
                    "Erro: " + errorMessage, 
                    Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Valida o email inserido
     */
    private boolean validarEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email é obrigatório");
            etEmail.requestFocus();
            updateStatusMessage("Por favor, insira seu email.", false);
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Digite um email válido");
            etEmail.requestFocus();
            updateStatusMessage("Por favor, insira um email válido.", false);
            return false;
        }

        if (email.length() < 5) {
            etEmail.setError("Email muito curto");
            etEmail.requestFocus();
            updateStatusMessage("Email parece ser muito curto.", false);
            return false;
        }

        etEmail.setError(null);
        return true;
    }

   
    private void setLoadingState(boolean loading) {
        isLoading = loading;
        updateUIState(loading);
    }
    
   
    private void updateUIState(boolean loading) {
        if (btnEnviarEmail != null) {
            btnEnviarEmail.setEnabled(!loading);
            btnEnviarEmail.setText(loading ? "Enviando..." : getString(R.string.btn_enviar_email_recuperacao));
        }
        
        if (etEmail != null) {
            etEmail.setEnabled(!loading);
        }
        
        if (ivBack != null) {
            ivBack.setEnabled(!loading);
        }
    }
    
    /**
     * Atualiza a mensagem de status
     */
    private void updateStatusMessage(String message, boolean isSuccess) {
        if (tvStatus != null) {
            tvStatus.setText(message);
            tvStatus.setVisibility(View.VISIBLE);
            
            if (isSuccess) {
                tvStatus.setTextColor(getResources().getColor(R.color.primary_green));
            } else {
                tvStatus.setTextColor(getResources().getColor(R.color.text_secondary));
            }
        }
    }

    /**
     * Volta para a tela de login
     */
    private void voltarParaLogin() {
        Intent intent = new Intent(EsqueciSenhaActivity.this, TelaLogin.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        
        Log.d(TAG, "Voltando para tela de login");
    }
    
    @Override
    public void onBackPressed() {
        if (!isLoading) {
            voltarParaLogin();
        } else {
            Toast.makeText(this, getString(R.string.aguarde_envio), Toast.LENGTH_SHORT).show();
        }
    }
} 