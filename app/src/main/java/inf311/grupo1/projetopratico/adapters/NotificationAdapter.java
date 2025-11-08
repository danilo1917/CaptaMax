package inf311.grupo1.projetopratico.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import inf311.grupo1.projetopratico.R;
import inf311.grupo1.projetopratico.models.Notification;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context context;
    private List<Notification> notifications;
    private OnNotificationClickListener clickListener;
    private OnNotificationActionListener actionListener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public interface OnNotificationActionListener {
        void onMarkAsRead(Notification notification);
        void onDelete(Notification notification);
        void onShowMenu(Notification notification, View anchorView);
    }

    public NotificationAdapter(Context context) {
        this.context = context;
        this.notifications = new ArrayList<>();
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    public void addNotification(Notification notification) {
        notifications.add(0, notification); // Adiciona no topo
        notifyItemInserted(0);
    }

    public void updateNotification(Notification notification) {
        int index = findNotificationIndex(notification.getId());
        if (index != -1) {
            notifications.set(index, notification);
            notifyItemChanged(index);
        }
    }

    public void removeNotification(String notificationId) {
        int index = findNotificationIndex(notificationId);
        if (index != -1) {
            notifications.remove(index);
            notifyItemRemoved(index);
        }
    }

    private int findNotificationIndex(String id) {
        for (int i = 0; i < notifications.size(); i++) {
            if (notifications.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    public void setOnNotificationClickListener(OnNotificationClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnNotificationActionListener(OnNotificationActionListener listener) {
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivNotificationIcon;
        private ImageView ivUnreadIndicator;
        private TextView tvNotificationTitle;
        private TextView tvNotificationMessage;
        private TextView tvNotificationTime;
        private TextView tvNotificationType;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            
            ivNotificationIcon = itemView.findViewById(R.id.iv_notification_icon);
            ivUnreadIndicator = itemView.findViewById(R.id.iv_unread_indicator);
            tvNotificationTitle = itemView.findViewById(R.id.tv_notification_title);
            tvNotificationMessage = itemView.findViewById(R.id.tv_notification_message);
            tvNotificationTime = itemView.findViewById(R.id.tv_notification_time);
            tvNotificationType = itemView.findViewById(R.id.tv_notification_type);
        }

        public void bind(Notification notification) {
            // Título
            tvNotificationTitle.setText(notification.getTitle());
            
            // Mensagem
            tvNotificationMessage.setText(notification.getBody());
            
            // Tempo
            tvNotificationTime.setText(notification.getFormattedTime());
            
            // Tipo
            tvNotificationType.setText(notification.getTypeDisplayName());
            
            // Ícone do tipo
            ivNotificationIcon.setImageResource(notification.getTypeIcon());
            
            // Configurar cores por prioridade
            setupPriorityColors(notification);
            
            // Estado de leitura
            setupReadState(notification);
            
            // Click listeners
            setupClickListeners(notification);
        }

        private void setupPriorityColors(Notification notification) {
            int iconColor, badgeColor, badgeBackgroundColor;
            
            switch (notification.getPriority()) {
                case Notification.PRIORITY_URGENT:
                    iconColor = ContextCompat.getColor(context, R.color.notification_high_priority);
                    badgeColor = ContextCompat.getColor(context, R.color.notification_high_priority);
                    badgeBackgroundColor = ContextCompat.getColor(context, R.color.notification_high_priority_bg);
                    break;
                case Notification.PRIORITY_HIGH:
                    iconColor = ContextCompat.getColor(context, R.color.notification_medium_priority);
                    badgeColor = ContextCompat.getColor(context, R.color.notification_medium_priority);
                    badgeBackgroundColor = ContextCompat.getColor(context, R.color.notification_medium_priority_bg);
                    break;
                case Notification.PRIORITY_LOW:
                    iconColor = ContextCompat.getColor(context, R.color.notification_low_priority);
                    badgeColor = ContextCompat.getColor(context, R.color.notification_low_priority);
                    badgeBackgroundColor = ContextCompat.getColor(context, R.color.notification_low_priority_bg);
                    break;
                case Notification.PRIORITY_NORMAL:
                default:
                    iconColor = ContextCompat.getColor(context, R.color.notification_info_priority);
                    badgeColor = ContextCompat.getColor(context, R.color.notification_info_priority);
                    badgeBackgroundColor = ContextCompat.getColor(context, R.color.notification_info_priority_bg);
                    break;
            }
            
            // Aplicar cores ao ícone
            ivNotificationIcon.setColorFilter(iconColor);
            
            // Aplicar cores ao badge do tipo
            tvNotificationType.setTextColor(badgeColor);
            
            // Aplicar cor de fundo ao container do ícone
            CardView iconContainer = itemView.findViewById(R.id.icon_container);
            if (iconContainer != null) {
                iconContainer.setCardBackgroundColor(badgeBackgroundColor);
            }
            
            // Aplicar cor de fundo ao badge do tipo
            ViewParent typeBadgeParent = tvNotificationType.getParent();
            if (typeBadgeParent instanceof CardView) {
                ((CardView) typeBadgeParent).setCardBackgroundColor(badgeBackgroundColor);
            }
        }

        private void setupReadState(Notification notification) {
            if (notification.isRead()) {
                // Notificação lida
                ivUnreadIndicator.setVisibility(View.GONE);
                tvNotificationTitle.setAlpha(0.8f);
                tvNotificationMessage.setAlpha(0.7f);
            } else {
                // Notificação não lida
                ivUnreadIndicator.setVisibility(View.VISIBLE);
                tvNotificationTitle.setAlpha(1.0f);
                tvNotificationMessage.setAlpha(1.0f);
            }
        }

        private void setupClickListeners(Notification notification) {
            // Click no item inteiro
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onNotificationClick(notification);
                }
            });
        }
    }
} 