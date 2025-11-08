package inf311.grupo1.projetopratico;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONObject;

import java.util.Date;

public class Contato implements Parcelable {
    public String nome;
    public String email;
    public String telefone;

    public String interesse;

    public String escola;

    public String serie;

    public String responsavel;

    public String uid; // UID do Firebase - campo personalizado 4 da API Rubeus

    public Date ultimo_contato;


    public Integer id;


    public Contato(String n,String e,String t,String r,String i,String s,String esc,Date d)
    {
        nome=n;
        email=e;
        telefone=t;
        responsavel=r;
        interesse=i;
        serie=s;
        escola = esc;
        ultimo_contato=d;
        uid = null; // Inicializar como null por padrão

    }

    public Contato(Parcel in)
    {
          nome=in.readString();
          Log.w("my app","Nome é :" + nome);
          email=in.readString();
          telefone=in.readString();
          responsavel=in.readString();
          interesse=in.readString();
          serie=in.readString();
          escola = in.readString();
          ultimo_contato=new Date(in.readLong());
          uid = in.readString();
    }

    public Contato(JSONObject ob)
    {
        try
        {
            nome=ob.getString("nome");
            email=ob.getString("email");
            telefone=ob.getString("telefone");
            responsavel=ob.getString("campopersonalizado_1_compl_cont");;
            interesse="";
            serie=ob.getString("campopersonalizado_6_compl_cont");
            escola = ob.getString("escolaorigem");//
            ultimo_contato=new Date();
            id=ob.getInt("id");
            
            // Extrair UID do campo personalizado 4 se existir
            uid = null;
            try {
                JSONObject camposPersonalizados = ob.getJSONObject("camposPersonalizados");
                if (camposPersonalizados.has("campopersonalizado_4_compl_cont") && 
                    !camposPersonalizados.isNull("campopersonalizado_4_compl_cont")) {
                    
                    // O campo pode ser um array ou string, vamos tratar ambos
                    Object campo4 = camposPersonalizados.get("campopersonalizado_4_compl_cont");
                    if (campo4 instanceof String) {
                        uid = (String) campo4;
                    } else if (campo4 instanceof org.json.JSONArray) {
                        org.json.JSONArray uidArray = (org.json.JSONArray) campo4;
                        if (uidArray.length() > 0) {
                            uid = uidArray.getString(0); // Pegar o primeiro UID
                        }
                    }
                }
            } catch (Exception e) {
                Log.w("Contato", "Erro ao extrair UID do campo personalizado 4", e);
                uid = null;
            }
        }

        catch (org.json.JSONException j)
        {
            Log.w("Contato", "Erro ao criar Contato do JSON", j);
        }

    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel p, int i)
    {
         p.writeString(nome);
         p.writeString(email);
         p.writeString(telefone);
         p.writeString(responsavel);
         p.writeString(interesse);
         p.writeString(serie);
         p.writeString(escola);
         p.writeLong(ultimo_contato.getTime());
         p.writeString(uid); // Escrever UID no Parcel
    }

    public static final Parcelable.Creator<Contato> CREATOR =new Parcelable.Creator<Contato>()
    {

        public Contato createFromParcel(Parcel in)
        {
            return new Contato(in);
        }

        public Contato[] newArray(int size)
        {
            return new Contato[size];
        }
    };
}
