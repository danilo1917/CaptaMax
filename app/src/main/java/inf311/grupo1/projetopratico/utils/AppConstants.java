package inf311.grupo1.projetopratico.utils;

public class AppConstants {
    
    // Séries disponíveis para cadastro de leads
    public static final String[] SERIES_DISPONVEIS = {
        "1º Ano", "2º Ano", "3º Ano", "4º Ano", "5º Ano", "6º Ano", 
        "7º Ano", "8º Ano", "9º Ano", "1º EM", "2º EM", "3º EM"
    };
    
    // Tipos de interesse disponíveis
    public static final String[] TIPOS_INTERESSE = {
        "Selecione", "Matrícula imediata", "Conhecer a escola", 
        "Informações", "Valores", "Bolsa de estudos"
    };
    
    // Status possíveis dos leads
    public static final String[] STATUS_LEADS = {
        "Potencial", "Interessado", "Inscrito parcial", "Inscrito", "Confirmado", "Convocado", "Matriculado"
    };
  
    // Prioridades dos leads
    public static final String[] PRIORIDADES = {
        "Baixa", "Média", "Alta", "Urgente"
    };
    
    // Estágios do funil de vendas
    public static final String[] ESTAGIOS_FUNIL = STATUS_LEADS;
    
    // Cores modernas e harmônicas para gráficos - paleta expandida com 30 cores bem diferenciadas
    public static final String[] CORES_GRAFICOS = {
        "#14b8a6", // Verde principal - teal-500
        "#0d9488", // Verde escuro - teal-600
        "#06b6d4", // Ciano moderno - sky-500
        "#0ea5e9", // Azul claro - sky-600
        "#3b82f6", // Azul vibrante - blue-500
        "#2563eb", // Azul escuro - blue-600
        "#6366f1", // Índigo - indigo-500
        "#4f46e5", // Índigo escuro - indigo-600
        "#8b5cf6", // Violeta - violet-500
        "#7c3aed", // Violeta escuro - violet-600
        "#a855f7", // Roxo - purple-500
        "#9333ea", // Roxo escuro - purple-600
        "#ec4899", // Rosa - pink-500
        "#db2777", // Rosa escuro - pink-600
        "#f43f5e", // Vermelho rosa - rose-500
        "#e11d48", // Vermelho escuro - rose-600
        "#f59e0b", // Laranja - amber-500
        "#d97706", // Laranja escuro - amber-600
        "#eab308", // Amarelo - yellow-500
        "#10b981", // Verde esmeralda - emerald-500
        "#059669", // Verde esmeralda escuro - emerald-600
        "#84cc16", // Lima - lime-500
        "#65a30d", // Lima escuro - lime-600
        "#22c55e", // Verde claro - green-500
        "#16a34a", // Verde claro escuro - green-600
        "#ef4444", // Vermelho - red-500
        "#dc2626", // Vermelho escuro - red-600
        "#f97316", // Laranja vivo - orange-500
        "#ea580c", // Laranja vivo escuro - orange-600
        "#8b5cf6"  // Extra violeta - violet-500
    };
    
    // Cores específicas para tipos de dados em gráficos
    public static final String COR_LEADS = "#14b8a6";           // Verde principal
    public static final String COR_CONVERSOES = "#0d9488";      // Verde escuro
    public static final String COR_POTENCIAL = "#06b6d4";       // Ciano
    public static final String COR_INTERESSADO = "#3b82f6";     // Azul
    public static final String COR_INSCRITO = "#6366f1";        // Índigo
    public static final String COR_CONFIRMADO = "#8b5cf6";      // Violeta
    public static final String COR_MATRICULADO = "#10b981";     // Verde esmeralda
    
    // Limites de paginação
    public static final int LIMITE_LEADS_DASHBOARD = 5;
    public static final int LIMITE_LEADS_POR_PAGINA = 20;
    
    // Keys para Bundle/Intent
    public static final String KEY_USER_EMAIL = "user_email";
    public static final String KEY_USER_UID = "user_uid";
    public static final String KEY_USER_NAME = "user_name";
    public static final String KEY_IS_ADMIN = "is_admin";
    public static final String KEY_CONTATO = "contato";
    
    // Mensagens de erro padrão
    public static final String MSG_ERRO_CARREGAR_DADOS = "Erro ao carregar dados";
    public static final String MSG_ERRO_SALVAR = "Erro ao salvar informações";
    public static final String MSG_ERRO_CONEXAO = "Erro de conexão";
    public static final String MSG_SUCESSO_SALVAR = "Informações salvas com sucesso";
    
    // Configurações de validação
    public static final int MIN_TELEFONE_LENGTH = 10;
    public static final int MAX_TELEFONE_LENGTH = 15;
    public static final int MIN_NOME_LENGTH = 2;
    public static final int MAX_OBSERVACOES_LENGTH = 500;
    
    // Firebase Collections
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_NOTIFICATIONS = "notifications";
    public static final String COLLECTION_LEADS = "leads";
    public static final String COLLECTION_ACTIVITIES = "activities";
    
    // Notification Channel
    public static final String NOTIFICATION_CHANNEL_ID = "captamax_notifications";
    public static final String NOTIFICATION_CHANNEL_NAME = "captaMax Notificações";
    
    // Shared Preferences
    public static final String PREFS_NAME = "CaptaMaxPrefs";
    public static final String PREF_FCM_TOKEN = "fcm_token";
    public static final String PREF_LAST_NOTIFICATION_CHECK = "last_notification_check";
    
    // Request Codes
    public static final int REQUEST_NOTIFICATION_PERMISSION = 1001;
    public static final int REQUEST_CAMERA_PERMISSION = 1002;
    public static final int REQUEST_STORAGE_PERMISSION = 1003;
    
    // Fragment Tags
    public static final String FRAGMENT_DASHBOARD = "dashboard";
    public static final String FRAGMENT_LEADS = "leads";
    public static final String FRAGMENT_ALERTAS = "alertas";
    public static final String FRAGMENT_PERFIL = "perfil";
    
    // Date Formats
    public static final String DATE_FORMAT_DEFAULT = "dd/MM/yyyy";
    public static final String DATE_FORMAT_WITH_TIME = "dd/MM/yyyy HH:mm";
    public static final String DATE_FORMAT_ISO = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    
    // API Endpoints (se houver)
    public static final String BASE_URL = "";
    public static final String ENDPOINT_LEADS = "leads";
    public static final String ENDPOINT_NOTIFICATIONS = "notifications";
    
    // Limits
    public static final int MAX_NOTIFICATIONS_PER_PAGE = 50;
    public static final int MAX_LEADS_PER_PAGE = 20;
    public static final int MAX_UPLOAD_SIZE_MB = 5;
    
    // Error Messages
    public static final String ERROR_NETWORK = "Erro de conexão. Verifique sua internet.";
    public static final String ERROR_AUTH = "Erro de autenticação. Faça login novamente.";
    public static final String ERROR_PERMISSION = "Permissão necessária para continuar.";
    public static final String ERROR_GENERIC = "Ocorreu um erro inesperado.";
    
    // Success Messages
    public static final String SUCCESS_NOTIFICATION_SENT = "Notificação enviada com sucesso";
    public static final String SUCCESS_DATA_SAVED = "Dados salvos com sucesso";
    public static final String SUCCESS_LEAD_CREATED = "Lead criado com sucesso";
    
    // Validation
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_TITLE_LENGTH = 100;
    public static final int MAX_DESCRIPTION_LENGTH = 500;
    
    // Default Values
    public static final String DEFAULT_AVATAR_COLOR = "#2196F3";
    public static final int DEFAULT_NOTIFICATION_PRIORITY = 0;
    public static final boolean DEFAULT_NOTIFICATIONS_ENABLED = true;
    
    private AppConstants() {
        // Classe utilitária - não deve ser instanciada
    }
} 