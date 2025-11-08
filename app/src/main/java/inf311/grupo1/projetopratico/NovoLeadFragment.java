package inf311.grupo1.projetopratico;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;

import inf311.grupo1.projetopratico.services.LeadsDataProvider;
import inf311.grupo1.projetopratico.utils.AppConstants;
import inf311.grupo1.projetopratico.utils.App_fragment;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;

public class NovoLeadFragment extends App_fragment {
    
    private static final String TAG = "NovoLeadFragment";
    
    // Informações do usuário
    private boolean isAdmin = false;
    private String userEmail;
    private String userUid;
    
    // Serviços de dados
    private LeadsDataProvider leadsDataProvider;
    
    // UI Elements
    private EditText editTextName, editTextEscola, editTextNomeResp, 
                     editTextEmail, editTextTel, editTextObservacoes;
    private Spinner serieSpinner, interesseSpinner;
    private LinearLayout btnCancelar, btnSalvar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "NovoLeadFragment onCreateView");
        
        // Inflar o layout do fragment
        View view = inflater.inflate(R.layout.fragment_novo_lead, container, false);
        
        // Inicializar serviços
        initializeServices();
        
        // Obter informações do usuário dos argumentos
        getUserDataFromArguments();
        
        Log.d(TAG, "NovoLeadFragment iniciado para usuário: " + userEmail);
        
        return view;
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Configurar StrictMode
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Inicializar UI
        initializeUI(view);
        
        // Configurar spinners
        setupSpinners();
        
        // Configurar listeners
        setupListeners();
    }
    
    /**
     * Inicializa os serviços de dados
     */
    private void initializeServices() {
        leadsDataProvider = LeadsDataProvider.getInstance();
        Log.d(TAG, "Serviços inicializados");
    }
    
    /**
     * Obtém dados do usuário dos argumentos do fragment
     */
    private void getUserDataFromArguments() {
        Bundle args = getArguments();
        if (args != null) {
            isAdmin = args.getBoolean(AppConstants.KEY_IS_ADMIN, false);
            userEmail = args.getString(AppConstants.KEY_USER_EMAIL);
            userUid = args.getString(AppConstants.KEY_USER_UID);
        }
        
        Log.d(TAG, "Dados do usuário - Email: " + userEmail + ", Admin: " + isAdmin);
    }
    
    /**
     * Inicializa os elementos da UI
     */
    private void initializeUI(View view) {
        // EditTexts
        editTextName = view.findViewById(R.id.edittext_name);
        editTextEscola = view.findViewById(R.id.edittext_escola);
        editTextNomeResp = view.findViewById(R.id.edittext_nome_resp);
        editTextEmail = view.findViewById(R.id.edittext_email);
        editTextTel = view.findViewById(R.id.edittext_tel);
        editTextObservacoes = view.findViewById(R.id.edittext_observacoes);
        
        // Spinners
        serieSpinner = view.findViewById(R.id.serie_spinner);
        interesseSpinner = view.findViewById(R.id.interesse_spinner);
        
        // Buttons
        btnCancelar = view.findViewById(R.id.btn_cancelar);
        btnSalvar = view.findViewById(R.id.btn_salvar);
        
        Log.d(TAG, "UI inicializada");
    }
    
    /**
     * Configura os spinners com seus adapters
     */
    private void setupSpinners() {
        if (getContext() == null) return;
        
        // Adapter para séries
        ArrayAdapter<String> serieAdapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, leadsDataProvider.getSeries());
        serieAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serieSpinner.setAdapter(serieAdapter);

        // Adapter para interesses
        ArrayAdapter<String> interesseAdapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, leadsDataProvider.getInterests());
        interesseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        interesseSpinner.setAdapter(interesseAdapter);
        
        Log.d(TAG, "Spinners configurados");
    }
    
    /**
     * Configura os listeners dos botões
     */
    private void setupListeners() {
        if (btnCancelar != null) {
            btnCancelar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancelarCadastro();
                }
            });
        }
        
        if (btnSalvar != null) {
            btnSalvar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    salvarLead();
                }
            });
        }
        
        Log.d(TAG, "Listeners configurados");
    }
    
    /**
     * Cancela o cadastro e volta para a tela anterior
     */
    private void cancelarCadastro() {
        Log.d(TAG, "Cancelando cadastro de lead");
        
        // Resetar estado de cadastro ativo
        LeadsFragment.setCadastroAtivo(false);
        
        // Mostrar confirmação se há dados preenchidos
        if (hasDataFilled()) {
            showCancelConfirmationDialog();
        } else {
            // Voltar diretamente se não há dados preenchidos
            navigateBack();
        }
    }
    
    /**
     * Mostra diálogo de confirmação antes de cancelar
     */
    private void showCancelConfirmationDialog() {
        if (getContext() == null) return;
        
        new AlertDialog.Builder(getContext())
                .setTitle("⚠️ Confirmar Cancelamento")
                .setMessage("Existem dados não salvos que serão perdidos.\n\nDeseja realmente cancelar o cadastro?")
                .setPositiveButton("Sim, Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        navigateBack();
                    }
                })
                .setNegativeButton("Continuar Editando", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(true)
                .show();
    }
    
    /**
     * Verifica se há dados preenchidos no formulário
     */
    private boolean hasDataFilled() {
        return !editTextName.getText().toString().trim().isEmpty() ||
               !editTextEscola.getText().toString().trim().isEmpty() ||
               !editTextNomeResp.getText().toString().trim().isEmpty() ||
               !editTextEmail.getText().toString().trim().isEmpty() ||
               !editTextTel.getText().toString().trim().isEmpty() ||
               !editTextObservacoes.getText().toString().trim().isEmpty() ||
               serieSpinner.getSelectedItemPosition() > 0 ||
               interesseSpinner.getSelectedItemPosition() > 0;
    }
    
    /**
     * Salva o novo lead usando o provedor de dados
     */
    private void salvarLead() {
        Log.d(TAG, "Salvando novo lead");
        
        try {
            // Validar campos obrigatórios
            if (!validarCampos()) {
                return;
            }
            
            // Criar objeto Contato
            Contato novoLead = criarContatoFromForm();
            
            // Salvar usando o provedor de dados
            boolean sucesso = leadsDataProvider.adicionarLead(novoLead);
            
            if (sucesso) {
                showSuccessDialog("Lead cadastrado com sucesso!");
                limparFormulario();
                
                // Notificar que um novo lead foi adicionado
                notificarNovoLead();
                
            } else {
                showErrorDialog("Erro ao cadastrar lead", "Não foi possível salvar o lead no sistema.\n\nVerifique sua conexão e tente novamente.");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao salvar lead", e);
            showErrorDialog("Erro inesperado", "Ocorreu um erro inesperado ao cadastrar o lead.\n\nDetalhes: " + e.getMessage());
        }
    }
    
    /**
     * Valida os campos obrigatórios do formulário
     */
    private boolean validarCampos() {
        boolean valido = true;
        StringBuilder erros = new StringBuilder();
        
        if (editTextName.getText().toString().trim().isEmpty()) {
            editTextName.setError("Nome é obrigatório");
            erros.append("• Nome do aluno\n");
            valido = false;
        }
        
        if (editTextEmail.getText().toString().trim().isEmpty()) {
            editTextEmail.setError("Email é obrigatório");
            erros.append("• Email\n");
            valido = false;
        } else if (!isValidEmail(editTextEmail.getText().toString().trim())) {
            editTextEmail.setError("Email inválido");
            erros.append("• Email válido\n");
            valido = false;
        }
        
        if (editTextTel.getText().toString().trim().isEmpty()) {
            editTextTel.setError("Telefone é obrigatório");
            erros.append("• Telefone\n");
            valido = false;
        }
        
        if (editTextNomeResp.getText().toString().trim().isEmpty()) {
            editTextNomeResp.setError("Nome do responsável é obrigatório");
            erros.append("• Nome do responsável\n");
            valido = false;
        }
        
        if (editTextEscola.getText().toString().trim().isEmpty()) {
            editTextEscola.setError("Escola é obrigatória");
            erros.append("• Escola atual\n");
            valido = false;
        }
        
        if (serieSpinner.getSelectedItemPosition() == 0) {
            erros.append("• Série\n");
            valido = false;
        }
        
        if (interesseSpinner.getSelectedItemPosition() == 0) {
            erros.append("• Tipo de interesse\n");
            valido = false;
        }
        
        if (!valido) {
            showErrorDialog("Campos obrigatórios", "Por favor, preencha os seguintes campos:\n\n" + erros.toString());
        }
        
        return valido;
    }
    
    /**
     * Cria um objeto Contato a partir dos dados do formulário
     */
    private Contato criarContatoFromForm() {
        String nome = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String telefone = editTextTel.getText().toString().trim();
        String responsavel = editTextNomeResp.getText().toString().trim();
        String interesse = leadsDataProvider.getInterests()[interesseSpinner.getSelectedItemPosition()];
        String serie = leadsDataProvider.getSeries()[serieSpinner.getSelectedItemPosition()];
        String escola = editTextEscola.getText().toString().trim();
        
        Contato novoContato = new Contato(nome, email, telefone, responsavel, 
                                        interesse, serie, escola, new Date());
        
        // Adicionar observações se houver
        String observacoes = editTextObservacoes.getText().toString().trim();
        if (!observacoes.isEmpty()) {
            // TODO: Adicionar campo observações na classe Contato se necessário
            Log.d(TAG, "Observações: " + observacoes);
        }
        
        return novoContato;
    }
    
    /**
     * Limpa todos os campos do formulário
     */
    private void limparFormulario() {
        editTextName.setText("");
        editTextEmail.setText("");
        editTextTel.setText("");
        editTextNomeResp.setText("");
        editTextEscola.setText("");
        editTextObservacoes.setText("");
        
        serieSpinner.setSelection(0);
        interesseSpinner.setSelection(0);
        
        // Limpar erros
        editTextName.setError(null);
        editTextEmail.setError(null);
        editTextTel.setError(null);
        editTextNomeResp.setError(null);
        editTextEscola.setError(null);
        
        Log.d(TAG, "Formulário limpo");
    }
    
    /**
     * Valida formato do email
     */
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    
    /**
     * Notifica outros componentes sobre o novo lead
     */
    private void notificarNovoLead() {
        // Notificar LeadsFragment para atualizar a lista
        LeadsFragment.setCadastroAtivo(false);
        
        Log.d(TAG, "Notificação de novo lead enviada");
    }
    
    /**
     * Navega de volta para a tela anterior
     */
    private void navigateBack() {
        if (getActivity() instanceof MainActivityNova) {
            MainActivityNova mainActivity = (MainActivityNova) getActivity();
            mainActivity.onBackPressed();
            Log.d(TAG, "Navegando de volta");
        }
    }
    
    /**
     * Exibe diálogo de sucesso
     */
    private void showSuccessDialog(String message) {
        if (getContext() == null) return;
        
        new AlertDialog.Builder(getContext())
                .setTitle("✅ Sucesso!")
                .setMessage(message + "\n\nO lead foi adicionado ao sistema e está disponível para acompanhamento.")
                .setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        navigateBack();
                    }
                })
                .setNeutralButton("Cadastrar Outro", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        // Formulário já foi limpo, usuário pode cadastrar outro lead
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .setCancelable(false)
                .show();
    }
    
    /**
     * Exibe diálogo de erro
     */
    private void showErrorDialog(String title, String message) {
        if (getContext() == null) return;
        
        new AlertDialog.Builder(getContext())
                .setTitle("❌ " + title)
                .setMessage(message)
                .setPositiveButton("Tentar Novamente", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Voltar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        navigateBack();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(true)
                .show();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        // Garantir que o estado seja resetado
        LeadsFragment.setCadastroAtivo(false);
        
        Log.d(TAG, "NovoLeadFragment destruído");
    }
    
    /**
     * Getters para informações do usuário (compatibilidade)
     */
    public String getCurrentUserEmail() {
        return userEmail;
    }
    
    public String getCurrentUserUid() {
        return userUid;
    }
    
    public boolean isCurrentUserAdmin() {
        return isAdmin;
    }
} 