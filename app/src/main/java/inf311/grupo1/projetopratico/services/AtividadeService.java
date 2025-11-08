package inf311.grupo1.projetopratico.services;

import static inf311.grupo1.projetopratico.Data_master.origem;
import static inf311.grupo1.projetopratico.Data_master.token;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import inf311.grupo1.projetopratico.App_main;
import inf311.grupo1.projetopratico.Data_master;

public class AtividadeService{

    public static String cadastrarAtividade(
            String contato,
            String vencimento,
            int formaContato,
            int tipo,
            Integer duracao,
            String descricao,
            Integer concluido,
            Integer responsavel,
            Integer assinatura,
            String tipoVinculo,
            Integer tempoNotificacao,
            Integer tipoTempo,
            int[] pessoas,
            String agendamentoUnico,
            int[] oportunidades,
            Integer razaoOportunidade,
            Integer responsavelUnico,
            Integer enviarSemAssinatura,
            Integer baseLegalEnvioSemAssinatura,
            String justificativaEnvioSemAssinatura
    ) throws IOException {

        String url = "https://crmufvgrupo1.apprubeus.com.br/api/Atividade/cadastroApi";

        HashMap<String, Object> params = new HashMap<>();
        params.put("contato", contato);
        params.put("vencimento", vencimento);
        params.put("formaContato", formaContato);
        params.put("tipo", tipo);
        params.put("origem", Data_master.origem);
        params.put("token", Data_master.token);

        if (duracao != null) params.put("duracao", duracao);
        if (descricao != null) params.put("descricao", descricao);
        if (concluido != null) params.put("concluido", concluido);
        if (responsavel != null) params.put("responsavel", responsavel);
        if (assinatura != null) params.put("assinatura", assinatura);
        if (tipoVinculo != null) params.put("tipoVinculo", tipoVinculo);
        if (tempoNotificacao != null) params.put("tempoNotificacao", tempoNotificacao);
        if (agendamentoUnico != null) params.put("agendamentoUnico", agendamentoUnico);
        if (tipoTempo != null) params.put("tipoTempo", tipoTempo);
        if (pessoas != null && pessoas.length > 0) params.put("pessoas", toJsonArray(pessoas));
        if (oportunidades != null && oportunidades.length > 0) params.put("oportunidades", toJsonArray(oportunidades));
        if (razaoOportunidade != null) params.put("razaoOportunidade", razaoOportunidade);
        if (responsavelUnico != null) params.put("responsavelUnico", responsavelUnico);
        if (enviarSemAssinatura != null) params.put("enviarSemAssinatura", enviarSemAssinatura);
        if (baseLegalEnvioSemAssinatura != null) params.put("baseLegalEnvioSemAssinatura", baseLegalEnvioSemAssinatura);
        if (justificativaEnvioSemAssinatura != null) params.put("justificativaEnvioSemAssinatura", justificativaEnvioSemAssinatura);

        String res = App_main.do_api_call(params, url);

        return "";
    }

    private static String toJsonArray(int[] array) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i != array.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    public static ListaAtividades listarAtividades(
            Integer id,
            String pesquisa,
            String colunaPesquisa,
            Integer filtro,
            Integer status,
            String atividade,
            Integer razaoOportunidade,
            Integer statusOportunidade,
            Integer objecao,
            Integer curso,
            Integer ofertaCurso,
            Integer responsavel,
            String vencimento,
            String conclusao,
            String criacao,
            Integer formaContato,
            Integer tipo,
            Integer unidade,
            Integer localOferta,
            Integer processoSeletivo,
            Integer processo,
            Integer etapa,
            String tag,
            String campoPersonalizadoJson, // Ex: "{\"coluna\":\"valor\"}"
            Integer limite,
            Integer quantidade,
            Integer comunicacaoContato
    ) throws IOException {

        String url = "https://crmufvgrupo1.apprubeus.com.br/api/Atividade/listarAtividades";
        HashMap<String, Object> params = new HashMap<>();

        if (id != null) params.put("id", id);
        if (pesquisa != null) params.put("pesquisa", pesquisa);
        if (colunaPesquisa != null) params.put("colunaPesquisa", colunaPesquisa);
        if (filtro != null) params.put("filtro", filtro);
        if (status != null) params.put("status", status);
        if (atividade != null) params.put("atividade", atividade);
        if (razaoOportunidade != null) params.put("razaoOportunidade", razaoOportunidade);
        if (statusOportunidade != null) params.put("statusOportunidade", statusOportunidade);
        if (objecao != null) params.put("objecao", objecao);
        if (curso != null) params.put("curso", curso);
        if (ofertaCurso != null) params.put("ofertaCurso", ofertaCurso);
        if (responsavel != null) params.put("responsavel", responsavel);
        if (vencimento != null) params.put("vencimento", vencimento);
        if (conclusao != null) params.put("conclusao", conclusao);
        if (criacao != null) params.put("criacao", criacao);
        if (formaContato != null) params.put("formaContato", formaContato);
        if (tipo != null) params.put("tipo", tipo);
        if (unidade != null) params.put("unidade", unidade);
        if (localOferta != null) params.put("localOferta", localOferta);
        if (processoSeletivo != null) params.put("processoSeletivo", processoSeletivo);
        if (processo != null) params.put("processo", processo);
        if (etapa != null) params.put("etapa", etapa);
        if (tag != null) params.put("tag", tag);
        if (campoPersonalizadoJson != null) params.put("campoPersonalizado", campoPersonalizadoJson);
        if (limite != null) params.put("limite", limite);
        if (quantidade != null) params.put("quantidade", quantidade);
        if (comunicacaoContato != null) params.put("comunicacaoContato", comunicacaoContato);

        params.put("origem", Data_master.origem); // obrigatório
        params.put("token", Data_master.token);   // obrigatório

        String res = App_main.do_api_call(params, url);
        JSONObject obj = null;
        try{
            obj = new JSONObject(res);
        } catch (JSONException e) {
            Log.e("App_main", "Erro na chamada da API listarAtividades", e);
            throw new IOException("Erro ao processar resposta da API", e);
        }
        JSONObject dadosObj = null;
        try{
            if (obj != null && obj.has("dados")) {
                dadosObj = obj.getJSONObject("dados");
            } else {
                throw new JSONException("Resposta inválida ou sem 'dados'");
            }
        } catch (JSONException e) {
            Log.e("App_main", "Erro na chamada da API listarAtividades", e);
            throw new IOException("Erro ao processar resposta da API", e);
        }
        Gson gson = new Gson();
        ListaAtividades listaAtividades = gson.fromJson(dadosObj.toString(), ListaAtividades.class);

        return listaAtividades;

    }

    public class Atividade {
        public int id;
        public String vencimento;
        public int tipo;
        public String contato;
        public int pessoa;
        public String pessoaNome;
        public String status;
        public String statusNome;
        public String descricao;
        public String tipoNome;
        public String duracao;
        public String formaContato;
        public String email;
        public String telefone;
        public String responsavel;
        public String responsavelNome;
    }

    public class ListaAtividades {
        public int qtdTotal;
        public List<Atividade> dados;
    }

    public static void CadastroAtividade (
        String contato, // obrigatorio
        String formaContato, // obrigatorio
        String tipo, // obrigatorio
        String tipoSalvar, // obrigatorio
        String vencimento, // obrigatorio
        String pessoas, // obrigatorio
        String descricao, // obrigatorio
        String responsavel,
        String mostrarNotificacao,
        String tempoNotificacao,
        String tipoTempo,
        String concluido
    ) throws IOException {
        String url = "https://crmufvgrupo1.apprubeus.com.br/api/Agendamento/cadastro";
        HashMap<String, Object> params = new HashMap<>();
        params.put("mostrarNotificacao", mostrarNotificacao);
        params.put("tempoNotificacao", tempoNotificacao);
        params.put("tipoTempo", tipoTempo);
        params.put("contato", contato);
        params.put("formaContato", formaContato);
        params.put("tipo", tipo);
        params.put("dataVisao", "");
        params.put("duracaoVisao", "00:05:00");
        params.put("razaoOportunidade", "1");
        params.put("alterarResumo", "0");
        params.put("responsavelUnico", "82");
        params.put("oportunidades", "");
        params.put("oportunidade", "");
        params.put("hora", "23:59:59");
        params.put("descricao", descricao);
        params.put("tipoVinculo", "pessoa");
        params.put("tipoAtividade", tipo);
        params.put("concluido", concluido);
        params.put("duracao", "300");
        params.put("vencimento", vencimento + " 23:59:59");
        params.put("bloquearRazao", "");
        params.put("pessoas[]", pessoas);
        params.put("agendamentoUnico", "true");
        params.put("tipoSalvar", tipoSalvar);
        params.put("indiceFilaRequisicao", 1);
        //params.put("pessoa", pessoas);

        params.put("origem", Data_master.origem);
        params.put("token", Data_master.token);

        String res = App_main.do_api_call(params, url);
    }
}
