package inf311.grupo1.projetopratico.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import inf311.grupo1.projetopratico.Contato;
import inf311.grupo1.projetopratico.MainActivityNova;
import inf311.grupo1.projetopratico.R;

import java.util.HashMap;

/**
 * Classe utilitária para renderizar cards de leads de forma padronizada
 * Evita duplicação de código entre LeadsFragment e DashboardFragment
 */
public class LeadCardHelper {
    
    private static final String TAG = "LeadCardHelper";
    
    /**
     * Cria um card de lead moderno usando o layout padrão
     */
    public static View createModernLeadCard(Context context, Contato lead, 
                                          LinearLayout container, 
                                          HashMap<Integer, Contato> leadDict,
                                          Fragment fragment) {
        if (context == null || container == null || lead == null) {
            Log.e(TAG, "Parâmetros inválidos para criar card");
            return null;
        }
        
        try {
            // Inflar o layout do card de lead
            LayoutInflater inflater = LayoutInflater.from(context);
            View cardView = inflater.inflate(R.layout.item_lead_card, container, false);
            
            cardView.setId(View.generateViewId());
            
            TextView tvLeadName = cardView.findViewById(R.id.tv_lead_name);
            TextView tvLeadSchool = cardView.findViewById(R.id.tv_lead_school);
            TextView tvLeadStatus = cardView.findViewById(R.id.tv_lead_status);
            TextView tvLeadInterest = cardView.findViewById(R.id.tv_lead_interest);
            TextView tvLeadContact = cardView.findViewById(R.id.tv_lead_contact);
            TextView tvLastContact = cardView.findViewById(R.id.tv_last_contact);
            CardView statusChip = cardView.findViewById(R.id.status_chip);
            View priorityIndicator = cardView.findViewById(R.id.priority_indicator);
            CardView btnCall = cardView.findViewById(R.id.btn_call);
            
            if (tvLeadName != null) {
                tvLeadName.setText(lead.nome != null ? lead.nome : "Nome não informado");
            }
            
            if (tvLeadSchool != null) {
                String schoolInfo = "";
                if (lead.escola != null && !lead.escola.isEmpty()) {
                    schoolInfo = lead.escola;
                    if (lead.serie != null && !lead.serie.isEmpty()) {
                        schoolInfo += " • " + lead.serie;
                    }
                } else {
                    schoolInfo = "Escola não informada";
                }
                tvLeadSchool.setText(schoolInfo);
            }
            
            if (tvLeadStatus != null && statusChip != null) {
                String status = getLeadStatus(lead);
                tvLeadStatus.setText(status);
                
                int statusColor = getStatusColor(context, status);
                statusChip.setCardBackgroundColor(statusColor);
            }
            
            if (tvLeadInterest != null) {
                String displayInfo = "";
                if (lead.responsavel != null && !lead.responsavel.isEmpty()) {
                    displayInfo = "Responsável: " + lead.responsavel;
                } else if (lead.serie != null && !lead.serie.isEmpty()) {
                    displayInfo = "Série: " + lead.serie;
                } else {
                    displayInfo = "Informações complementares";
                }
                tvLeadInterest.setText(displayInfo);
            }
            
            if (tvLeadContact != null) {
                String contact = lead.telefone != null && !lead.telefone.isEmpty() ? 
                    lead.telefone : (lead.email != null ? lead.email : "Sem contato");
                tvLeadContact.setText(contact);
            }
            
            if (tvLastContact != null && lead.ultimo_contato != null) {
                String timeAgo = DateUtils.getRelativeTimeSpanString(lead.ultimo_contato.getTime()).toString();
                tvLastContact.setText(timeAgo);
            }
            
            if (priorityIndicator != null && isHighPriority(lead)) {
                priorityIndicator.setVisibility(View.VISIBLE);
            }
            
            setupCallButton(context, btnCall, lead);
            
            container.addView(cardView);
            
            if (leadDict != null) {
                leadDict.put(cardView.getId(), lead);
            }
            
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigateToDetails(v, leadDict, fragment);
                }
            });
            
            Log.d(TAG, "Card moderno de lead criado: " + lead.nome);
            return cardView;
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao criar card moderno para: " + lead.nome, e);
            return null;
        }
    }
    
    /**
     * Configura o botão de ligar com funcionalidade de telefone
     */
    private static void setupCallButton(Context context, CardView btnCall, Contato lead) {
        if (btnCall == null) return;
        
        if (isTelefoneValido(lead.telefone)) {
            btnCall.setAlpha(1.0f);
            btnCall.setClickable(true);
            btnCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ligarTelefone(context, lead.telefone);
                }
            });
        } else {
            btnCall.setAlpha(0.5f);
            btnCall.setClickable(true);
            btnCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "Telefone não informado para " + lead.nome, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    /**
     * Abre o app de telefone para ligar para o lead
     */
    private static void ligarTelefone(Context context, String telefone) {
        try {
            Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
            phoneIntent.setData(Uri.parse("tel:" + telefone));
            
            context.startActivity(phoneIntent);
            Log.d(TAG, "Abrindo discador para: " + telefone);
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao abrir discador", e);
            Toast.makeText(context, "Erro ao abrir discador", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Verifica se o telefone é válido
     */
    private static boolean isTelefoneValido(String telefone) {
        return telefone != null && 
               !telefone.trim().isEmpty() && 
               !telefone.equalsIgnoreCase("null") &&
               !telefone.equalsIgnoreCase("undefined") &&
               !telefone.equals("0") &&
               !telefone.trim().equals("-");
    }
    
    /**
     * Navega para os detalhes do lead
     */
    private static void navigateToDetails(View view, HashMap<Integer, Contato> leadDict, Fragment fragment) {
        if (leadDict == null || fragment == null) return;
        
        Contato lead = leadDict.get(view.getId());
        if (lead != null && fragment.getActivity() instanceof MainActivityNova) {
            MainActivityNova mainActivity = (MainActivityNova) fragment.getActivity();
            mainActivity.navigateToLeadDetails(lead);
            Log.d(TAG, "Navegando para detalhes do lead: " + lead.nome);
        }
    }
    
    /**
     * Determina o status do lead baseado nos dados disponíveis
     */
    private static String getLeadStatus(Contato lead) {
        if (lead.interesse != null && !lead.interesse.isEmpty()) {
            return lead.interesse;
        }
        return "Novo";
    }
    
    /**
     * Retorna a cor apropriada para o status
     */
    private static int getStatusColor(Context context, String status) {
        switch (status.toLowerCase()) {
            case "novo":
                return ContextCompat.getColor(context, R.color.text_secondary);
            case "potencial":
                return ContextCompat.getColor(context, R.color.info_blue);
            case "interessado":
                return ContextCompat.getColor(context, R.color.primary_green);
            case "inscrito parcial":
                return ContextCompat.getColor(context, R.color.warning_orange);
            case "inscrito":
                return ContextCompat.getColor(context, R.color.success_green);
            case "confirmado":
                return ContextCompat.getColor(context, R.color.verdeagua);
            case "convocado":
                return ContextCompat.getColor(context, R.color.roxo_barra);
            case "matriculado":
                return ContextCompat.getColor(context, R.color.verdeagua_dark);
            default:
                return ContextCompat.getColor(context, R.color.text_secondary);
        }
    }
    
    /**
     * Determina se um lead é de alta prioridade
     */
    private static boolean isHighPriority(Contato lead) {
        if (lead.ultimo_contato != null) {
            long daysSinceContact = (System.currentTimeMillis() - lead.ultimo_contato.getTime()) / (1000 * 60 * 60 * 24);
            return daysSinceContact <= 3; // Contato há 3 dias ou menos
        }
        return false;
    }
} 