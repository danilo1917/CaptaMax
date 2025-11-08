package inf311.grupo1.projetopratico;
import android.app.Application;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class App_main extends  Application
{


    public ArrayList<Contato> contatos;
    public boolean updated;

    @Override
    public void onCreate() {
        super.onCreate();
        contatos = new ArrayList<>();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    public static String do_api_call(@NonNull HashMap<String, Object> params, String url) throws IOException
    {
        StringBuilder sbParams = new StringBuilder();
        int i = 0;
        for (String key : params.keySet()) {
            try {
                if (i != 0) {
                    sbParams.append("&");
                }
                sbParams.append(key).append("=").append(URLEncoder.encode(params.get(key).toString(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            i++;
        }


        URL urlObj = new URL(url);
        //Log.w("myapp","Api call...");
        HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();

        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Accept-Charset", "UTF-8");
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);
        conn.connect();

        String paramsString = sbParams.toString();
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(paramsString);
        wr.flush();
        wr.close();

        InputStream in = new BufferedInputStream(conn.getInputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        Log.d("test", "result from server: " + result.toString());

        return  result.toString();
    }


    public void update()
    {
        Log.d("App_main", "Iniciando atualização dos dados da API");

        ArrayList<Contato> conts = new ArrayList<Contato>();

        String url = "";

        HashMap<String, Object> params = new HashMap<>();
        params.put("origem", Data_master.origem);
        params.put("token",Data_master.token);
        String ret = "";

        try {
            ret = do_api_call(params,"https://crmufvgrupo1.apprubeus.com.br/api/Pessoa/listarPessoas");

            Log.w("myApp",ret);
        }
        catch (IOException c)
        {
            Log.e("App_main", "Erro na chamada da API listarPessoas", c);
            return;
        }


        JSONObject obj = null;

        try
        {
            obj = new JSONObject(ret);
            conts = get_id_contacts(obj, Data_master.admin);
        }

        catch (org.json.JSONException j) {
            Log.w("myApp",j.toString());
            return;
        }

        if(!conts.isEmpty())
        {
            for(Contato c:conts)
            {
                try {
                    params.put("id",c.id);
                    ret = do_api_call(params,"https://crmufvgrupo1.apprubeus.com.br/api/Contato/listarOportunidades");

                    try
                    {
                        JSONObject objo = new JSONObject(ret);

                        if(objo.getBoolean("success"))
                        {
                            c.interesse = objo.getJSONArray("dados").getJSONObject(0).getString("etapaNome");
                        }

                    }

                    catch (org.json.JSONException jxe)
                    {

                    }

                    Log.w("myApp",ret);
                }
                catch (IOException cx)
                {

                }
            }
        }
        contatos = conts;
        
        // Definir updated como true apenas no final após sucesso
        updated = true;
        Log.d("App_main", "Atualização dos dados da API concluída com sucesso. Total de contatos: " + contatos.size());
    }
    
    /**
     * Força uma nova atualização dos dados da API, ignorando o flag updated
     */
    public void forceUpdate() {
        Log.d("App_main", "Forçando atualização dos dados da API");
        updated = false; // Resetar o flag para forçar atualização
        update(); // Executar atualização
    }

    
    public static ArrayList<Contato> get_id_contacts(JSONObject obj,boolean admin) throws org.json.JSONException
    {
        var users = obj.getJSONObject("dados").getJSONArray("dados");
        ArrayList<Contato> conts = new ArrayList<Contato>();

        for(int a=0; a<users.length(); a++)
        {
            var ob = users.getJSONObject(a);
            var ob_custom = ob.getJSONObject("camposPersonalizados");

            if(ob_custom.isNull("campopersonalizado_4_compl_cont")){
                continue;
            }

            var ob_array = ob_custom.getJSONArray("campopersonalizado_4_compl_cont");
            boolean user = false;

            for(int b=0; b< ob_array.length(); b++)
            {
                if(Objects.equals(ob_array.getString(b), Data_master.user_id) || admin)
                {
                    user=true;
                    break;
                }
            }

            if(user)
            {
                Contato c = new Contato(users.getJSONObject(a));
                conts.add(c);
            }
        }

        return conts;
    }


    public ArrayList<Contato> get_leads()
    {
        if(!updated)
        {

            update();
        }
        return  contatos;
    }

    /**
     * Adiciona um novo lead ao sistema, linkado ao usuário que o cadastrou
     */
    public static void adicionarLead(Contato contato) throws IOException{
        Log.w("App_main","Início do processo de cadastro do lead");
        HashMap<String, Object> params = new HashMap<>();
        params.put("origem", Data_master.origem);
        params.put("token", Data_master.token);

        params.put("nome", contato.nome);
        params.put("emailPrincipal", contato.email);
        params.put("telefonePrincipal", contato.telefone);
        params.put("escolaOrigem",contato.escola);
        
        params.put("camposPersonalizados[campopersonalizado_4_compl_cont][0]", Data_master.user_id);
        params.put("camposPersonalizados[campopersonalizado_1_compl_cont]", contato.responsavel);
        params.put("camposPersonalizados[campopersonalizado_6_compl_cont]", contato.serie);

        String url = "https://crmufvgrupo1.apprubeus.com.br/api/Contato/cadastro";
       
        try{
            String result = do_api_call(params, url);
            Log.w("App_main",result);
        } catch (IOException e) {
            Log.w("App_main", "Erro ao cadastrar lead: " + e.getMessage());
        }

        Log.w("App_main","Fim do request de cadastro do lead");
    }

}
