package inf311.grupo1.projetopratico;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList; // Import ArrayList
import java.util.Calendar;
import java.util.List; // Import List
import java.util.Locale;

import inf311.grupo1.projetopratico.services.AtividadeService;
import inf311.grupo1.projetopratico.utils.App_fragment;

public class AcompanhamentoFragment extends App_fragment {

    private static final String TAG = "AcompanhamentoFragment";

    private LinearLayout ligacao, email, tarefa, mensagem, visita;
    private Button cadastrar;
    private EditText descricao, nomeAtividade;
    private List<LinearLayout> actionButtons;

    private TextView lead_nome, escola;

    private int last_pressed = -1;
    private Button calendarioButton;
    private Calendar selectedDateCalendar;
    private Contato contato;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: AcompanhamentoFragment onCreateView");
        View view = inflater.inflate(R.layout.fragment_acompanhamento, container, false);

        Bundle args = getArguments();
        if(args != null) {
            contato = args.getParcelable("contato");
        }
        if (contato == null) {
            Log.e(TAG, "Contato não encontrado nos argumentos");
            if (getActivity() != null) {
                Toast.makeText(getActivity(), "Erro ao carregar dados do lead", Toast.LENGTH_SHORT).show();
            }
        }

        lead_nome = view.findViewById(R.id.acompanhamento_lead_nome);
        escola = view.findViewById(R.id.acompanhamento_escola);
        
        if (contato != null) {
            lead_nome.setText(contato.nome);
            escola.setText(contato.escola + " • " + contato.serie);
        }

        descricao = view.findViewById(R.id.acompanhamento_descricao);
        nomeAtividade = view.findViewById(R.id.acompanhamento_nome_atividade_caixa);

        calendarioButton = view.findViewById(R.id.acompanhamento_calendario_btn);
        selectedDateCalendar = Calendar.getInstance();
        updateCalendarButtonText();
        
        // Desabilitar funcionalidade - não adicionar listeners
        // calendarioButton.setOnClickListener(v -> showDatePickerDialog());

        ligacao = view.findViewById(R.id.acompanhamento_ligacao_btn);
        email = view.findViewById(R.id.acompanhamento_email_btn);
        tarefa = view.findViewById(R.id.acompanhamento_tarefa_btn);
        mensagem = view.findViewById(R.id.acompanhamento_mensagem_btn);
        visita = view.findViewById(R.id.acompanhamento_visita_btn);
        cadastrar = view.findViewById(R.id.acompanhamento_add_activity_btn);

        actionButtons = new ArrayList<>();
        actionButtons.add(ligacao);
        actionButtons.add(email);
        actionButtons.add(mensagem);
        actionButtons.add(visita);
        actionButtons.add(tarefa);

        // Funcionalidade desabilitada - não adicionar listeners
        // ligacao.setOnClickListener(v -> handleActionButtonClick(1, ligacao));
        // email.setOnClickListener(v -> handleActionButtonClick(2, email));
        // mensagem.setOnClickListener(v -> handleActionButtonClick(3, mensagem));
        // tarefa.setOnClickListener(v -> handleActionButtonClick(4, tarefa));
        // visita.setOnClickListener(v -> handleActionButtonClick(5, visita));
        // cadastrar.setOnClickListener(v -> {
        //     try {
        //         handleCadastrar();
        //     } catch (IOException e) {
        //         Log.e(TAG, "CAIMO NO CATCH", e);
        //         throw new RuntimeException(e);
        //     }
        // });

        setupDisabledInteractions();

        updateButtonBackgrounds();

        return view;
    }

    /**
     * Configura interações que mostram que a funcionalidade está desabilitada
     */
    private void setupDisabledInteractions() {
        View.OnClickListener disabledClickListener = v -> {
            if (getContext() != null) {
                Toast.makeText(getContext(), "🚀 Funcionalidade em desenvolvimento! Em breve estará disponível.", Toast.LENGTH_SHORT).show();
            }
        };

        // Aplicar listener desabilitado para todos os elementos interativos
        if (ligacao != null) ligacao.setOnClickListener(disabledClickListener);
        if (email != null) email.setOnClickListener(disabledClickListener);
        if (mensagem != null) mensagem.setOnClickListener(disabledClickListener);
        if (tarefa != null) tarefa.setOnClickListener(disabledClickListener);
        if (visita != null) visita.setOnClickListener(disabledClickListener);
        if (cadastrar != null) cadastrar.setOnClickListener(disabledClickListener);
        if (calendarioButton != null) calendarioButton.setOnClickListener(disabledClickListener);
    }

    private void handleActionButtonClick(int buttonIndex, LinearLayout clickedButton) {
        // Funcionalidade desabilitada
        last_pressed = buttonIndex;
        updateButtonBackgrounds();
        Log.d(TAG, "Button " + buttonIndex + " pressed (disabled).");
    }

    private void handleCadastrar() throws IOException {
        // Funcionalidade desabilitada - não executar
        /*
        AtividadeService.CadastroAtividade(
                nomeAtividade.getText().toString(),
                "0",
                String.valueOf(last_pressed),
                "1",
                calendarioButton.getText().toString(),
                String.valueOf(contato.id),
                descricao.getText().toString(),
                "",
                "",
                "",
                "",
                "0"
        );
        */
        Toast.makeText(getContext(), "🚀 Funcionalidade em desenvolvimento!", Toast.LENGTH_SHORT).show();
    }

    private void updateButtonBackgrounds() {
        for (int i = 0; i < actionButtons.size(); i++) {
            LinearLayout button = actionButtons.get(i);
            // button.setSelected(i == last_pressed); // Comentado para manter visual desabilitado
        }
    }

    private void showDatePickerDialog() {
        // Funcionalidade desabilitada
        Toast.makeText(getContext(), "🚀 Seleção de data estará disponível em breve!", Toast.LENGTH_SHORT).show();
        /*
        int year = selectedDateCalendar.get(Calendar.YEAR);
        int month = selectedDateCalendar.get(Calendar.MONTH);
        int day = selectedDateCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year1, monthOfYear, dayOfMonth) -> {
                    selectedDateCalendar.set(Calendar.YEAR, year1);
                    selectedDateCalendar.set(Calendar.MONTH, monthOfYear);
                    selectedDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateCalendarButtonText();
                },
                year,
                month,
                day);
        datePickerDialog.show();
        */
    }

    private void updateCalendarButtonText() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateString = dateFormat.format(selectedDateCalendar.getTime());
        if (calendarioButton != null) {
            calendarioButton.setText(dateString);
        }
    }
}