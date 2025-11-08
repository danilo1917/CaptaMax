package inf311.grupo1.projetopratico;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import inf311.grupo1.projetopratico.adapters.NotificationAdapter;
import inf311.grupo1.projetopratico.models.Notification;
import inf311.grupo1.projetopratico.services.NotificationService;
import inf311.grupo1.projetopratico.utils.AppConstants;
import inf311.grupo1.projetopratico.utils.App_fragment;
import inf311.grupo1.projetopratico.utils.NotificationPermissionHelper;

public class AlertasFragment extends App_fragment implements
    NotificationAdapter.OnNotificationClickListener,
    NotificationAdapter.OnNotificationActionListener {
    
    private static final String TAG = "AlertasFragment";
    
    // Informações do usuário
    private boolean isAdmin = false;
    private String userEmail;
    private String userUid;
    
    // Views
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerNotifications;
    private LinearLayout emptyStateLayout;
    private TextView tvEmptyState;
    
    // Chips de filtro
    private Button chipAll;
    private Button chipUnread;
    private Button chipRead;
    
    // Botões de ação
    private Button btnMarkAllRead;
    
    // Estado atual
    private String filtroAtual = "todas";
    private List<Notification> allNotifications = new ArrayList<>();
    private List<Notification> filteredNotifications = new ArrayList<>();
    
    // Serviços e Adapter
    private NotificationService notificationService;
    private NotificationAdapter notificationAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "AlertasFragment onCreateView");
        
        View view = inflater.inflate(R.layout.fragment_alertas, container, false);
        
        // Inicializar serviços
        initializeServices();
        
        // Obter dados do usuário
        getUserDataFromArguments();
        
        Log.d(TAG, "AlertasFragment iniciado para usuário: " + userEmail);
        
        return view;
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupRecyclerView();
        setupListeners();
        setupSwipeRefresh();
        
        // Configurar filtro inicial
        updateFilterButtons();
        
        // Garantir que o primeiro botão esteja selecionado por padrão
        chipAll.setSelected(true);
        
        // Carregar notificações
        loadNotifications();
    }
    
    /**
     * Inicializa os serviços
     */
    private void initializeServices() {
        notificationService = NotificationService.getInstance();
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
     * Inicializa as views
     */
    private void initializeViews(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_notifications);
        recyclerNotifications = view.findViewById(R.id.recycler_notifications);
        emptyStateLayout = view.findViewById(R.id.empty_state_layout);
        tvEmptyState = view.findViewById(R.id.tv_empty_notifications);
        
        // Chips de filtro
        chipAll = view.findViewById(R.id.chip_all);
        chipUnread = view.findViewById(R.id.chip_unread);
        chipRead = view.findViewById(R.id.chip_read);
        
        // Botões de ação
        btnMarkAllRead = view.findViewById(R.id.btn_mark_all_read);
        
        Log.d(TAG, "Views inicializadas");
    }
    
    /**
     * Configura o RecyclerView
     */
    private void setupRecyclerView() {
        notificationAdapter = new NotificationAdapter(getContext());
        notificationAdapter.setOnNotificationClickListener(this);
        notificationAdapter.setOnNotificationActionListener(this);
        
        recyclerNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerNotifications.setAdapter(notificationAdapter);
        recyclerNotifications.setHasFixedSize(true);
        
        Log.d(TAG, "RecyclerView configurado");
    }
    
    /**
     * Configura os listeners
     */
    private void setupListeners() {
        // Filtros
        chipAll.setOnClickListener(v -> mudarFiltro("todas"));
        chipUnread.setOnClickListener(v -> mudarFiltro("nao_lidas"));
        chipRead.setOnClickListener(v -> mudarFiltro("lidas"));
        
        // Ações
        btnMarkAllRead.setOnClickListener(v -> markAllAsRead());
        
        Log.d(TAG, "Listeners configurados");
    }
    
    /**
     * Configura o SwipeRefreshLayout
     */
    private void setupSwipeRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                Log.d(TAG, "Atualizando notificações via swipe");
                loadNotifications();
            });
            
            swipeRefreshLayout.setColorSchemeResources(
                R.color.primary_green,
                R.color.success_green,
                R.color.info_blue,
                R.color.warning_orange
            );
        }
    }
    
    /**
     * Carrega as notificações do Firebase
     */
    private void loadNotifications() {
        Log.d(TAG, "Carregando notificações...");
        
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }
        
        notificationService.getAllNotifications(new NotificationService.NotificationListCallback() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                Log.d(TAG, "Notificações carregadas: " + notifications.size());
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        allNotifications.clear();
                        allNotifications.addAll(notifications);
                        
                        applyCurrentFilter();
                        
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Erro ao carregar notificações: " + error);
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showError("Erro ao carregar notificações: " + error);
                        
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }
    
    /**
     * Muda o filtro atual
     */
    private void mudarFiltro(String novoFiltro) {
        filtroAtual = novoFiltro;
        updateFilterButtons();
        applyCurrentFilter();
        Log.d(TAG, "Filtro alterado para: " + novoFiltro);
    }
    
    /**
     * Atualiza o estado visual dos botões de filtro
     */
    private void updateFilterButtons() {
        // Reset todos os botões
        resetFilterButtons();
        
        // Ativar o botão correto baseado no filtro atual
        switch (filtroAtual) {
            case "todas":
                activateButton(chipAll);
                break;
            case "nao_lidas":
                activateButton(chipUnread);
                break;
            case "lidas":
                activateButton(chipRead);
                break;
        }
    }
    
    /**
     * Reseta o estado visual de todos os botões de filtro
     */
    private void resetFilterButtons() {
        deactivateButton(chipAll);
        deactivateButton(chipUnread);
        deactivateButton(chipRead);
    }
    
    /**
     * Ativa visualmente um chip
     */
    private void activateButton(Button chip) {
        chip.setSelected(true);
    }
    
    /**
     * Desativa visualmente um chip
     */
    private void deactivateButton(Button chip) {
        chip.setSelected(false);
    }
    
    /**
     * Aplica o filtro atual às notificações
     */
    private void applyCurrentFilter() {
        filteredNotifications.clear();
        
        switch (filtroAtual) {
            case "todas":
                filteredNotifications.addAll(allNotifications);
                break;
            case "nao_lidas":
                for (Notification notification : allNotifications) {
                    if (!notification.isRead()) {
                        filteredNotifications.add(notification);
                    }
                }
                break;
            case "lidas":
                for (Notification notification : allNotifications) {
                    if (notification.isRead()) {
                        filteredNotifications.add(notification);
                    }
                }
                break;
            default:
                filteredNotifications.addAll(allNotifications);
                break;
        }
        
        updateNotificationsDisplay();
    }
    
    /**
     * Atualiza a exibição das notificações
     */
    private void updateNotificationsDisplay() {
        if (filteredNotifications.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
        
        notificationAdapter.setNotifications(filteredNotifications);
    }
    
    // Implementação das interfaces do adapter
    
    @Override
    public void onNotificationClick(Notification notification) {
        Log.d(TAG, "Notificação clicada: " + notification.getTitle());
        
        // Se não foi lida, marcar como lida
        if (!notification.isRead()) {
            markAsRead(notification);
        }
        
        // Mostrar detalhes da notificação
        showNotificationDetails(notification);
    }
    
    @Override
    public void onMarkAsRead(Notification notification) {
        markAsRead(notification);
    }
    
    @Override
    public void onDelete(Notification notification) {
        deleteNotification(notification);
    }
    
    @Override
    public void onShowMenu(Notification notification, View anchorView) {
        showNotificationMenu(notification, anchorView);
    }
    
    /**
     * Mostra menu de contexto para a notificação
     */
    private void showNotificationMenu(Notification notification, View anchorView) {
        PopupMenu popup = new PopupMenu(getContext(), anchorView);
        
        if (!notification.isRead()) {
            popup.getMenu().add("Marcar como lida");
        }
        popup.getMenu().add("Deletar");
        popup.getMenu().add("Ver detalhes");
        
        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            switch (title) {
                case "Marcar como lida":
                    markAsRead(notification);
                    return true;
                case "Deletar":
                    deleteNotification(notification);
                    return true;
                case "Ver detalhes":
                    showNotificationDetails(notification);
                    return true;
                default:
                    return false;
            }
        });
        
        popup.show();
    }
    
    /**
     * Marca uma notificação como lida
     */
    private void markAsRead(Notification notification) {
        notificationService.markAsRead(notification.getId(), new NotificationService.NotificationCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Notificação marcada como lida");
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Atualizar o objeto local
                        notification.setRead(true);
                        
                        // Atualizar o adapter
                        notificationAdapter.updateNotification(notification);
                        
                        // Recarregar se necessário
                        applyCurrentFilter();
                        
                        Toast.makeText(getContext(), "Marcada como lida", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Erro ao marcar como lida: " + error);
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showError("Erro ao marcar como lida: " + error);
                    });
                }
            }
        });
    }
    
    /**
     * Deleta uma notificação
     */
    private void deleteNotification(Notification notification) {
        new AlertDialog.Builder(getContext())
            .setTitle("Deletar Notificação")
            .setMessage("Tem certeza que deseja deletar esta notificação?")
            .setPositiveButton("Deletar", (dialog, which) -> {
                notificationService.deleteNotification(notification.getId(), new NotificationService.NotificationCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Notificação deletada");
                        
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                // Remover da lista local
                                allNotifications.remove(notification);
                                
                                // Atualizar o adapter
                                notificationAdapter.removeNotification(notification.getId());
                                
                                // Recarregar se necessário
                                applyCurrentFilter();
                                
                                Toast.makeText(getContext(), "Notificação deletada", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Erro ao deletar: " + error);
                        
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                showError("Erro ao deletar: " + error);
                            });
                        }
                    }
                });
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }
    
    /**
     * Marca todas as notificações como lidas
     */
    private void markAllAsRead() {
        notificationService.markAllAsRead(new NotificationService.NotificationCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Todas as notificações marcadas como lidas");
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Atualizar objetos locais
                        for (Notification notification : allNotifications) {
                            notification.setRead(true);
                        }
                        
                        // Recarregar
                        applyCurrentFilter();
                        
                        Toast.makeText(getContext(), "Todas marcadas como lidas", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Erro ao marcar todas como lidas: " + error);
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showError("Erro ao marcar todas como lidas: " + error);
                    });
                }
            }
        });
    }
    
    /**
     * Mostra detalhes da notificação
     */
    private void showNotificationDetails(Notification notification) {
        new AlertDialog.Builder(getContext())
            .setTitle(notification.getTitle())
            .setMessage(notification.getBody() + "\n\n" +
                       "Tipo: " + notification.getTypeDisplayName() + "\n" +
                       "Prioridade: " + notification.getPriority() + "\n" +
                       "Data: " + notification.getFormattedTime() + "\n" +
                       "Status: " + (notification.isRead() ? "Lida" : "Não lida"))
            .setPositiveButton("Fechar", null)
            .show();
    }
    
    /**
     * Exibe estado vazio com mensagem contextual
     */
    private void showEmptyState() {
        if (emptyStateLayout != null && tvEmptyState != null) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            recyclerNotifications.setVisibility(View.GONE);
            
            // Personalizar mensagem baseada no filtro atual
            String message = getEmptyStateMessage();
            tvEmptyState.setText(message);
            
            Log.d(TAG, "Estado vazio exibido para filtro: " + filtroAtual);
        }
    }
    
    /**
     * Retorna mensagem contextual para estado vazio
     */
    private String getEmptyStateMessage() {
        switch (filtroAtual) {
            case "nao_lidas":
                return "🎉 Todas as notificações foram lidas!";
            case "lidas":
                return "📭 Nenhuma notificação lida ainda";
            case "todas":
            default:
                return "🔔 Nenhuma notificação encontrada";
        }
    }
    
    /**
     * Esconde estado vazio
     */
    private void hideEmptyState() {
        if (emptyStateLayout != null) {
            emptyStateLayout.setVisibility(View.GONE);
            recyclerNotifications.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Mostra mensagem de erro
     */
    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }
    
    // Métodos públicos para acesso externo
    
    public String getCurrentUserEmail() {
        return userEmail;
    }
    
    public String getCurrentUserUid() {
        return userUid;
    }
    
    public boolean isCurrentUserAdmin() {
        return isAdmin;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "AlertasFragment onResume - Atualizando dados");
        loadNotifications();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "AlertasFragment onDestroy");
    }
} 