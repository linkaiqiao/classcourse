package com.example.classcourse2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.classcourse2.SystemMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SystemMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_DATE_HEADER = 0;
    private static final int TYPE_MESSAGE = 1;

    private static List<SystemMessage> messageList;
    private static OnItemClickListener listener;

    public interface OnItemClickListener {
        void onMessageClick(int position, SystemMessage message);
        void onActionClick(int position, SystemMessage message);
    }

    public SystemMessageAdapter(List<SystemMessage> messageList) {
        this.messageList = messageList != null ? messageList : new ArrayList<>();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<SystemMessage> newList) {
        this.messageList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
//        // 安全检查
//        if (messageList == null || messageList.isEmpty()) {
//            return TYPE_MESSAGE;
//        }
//
//        // 第一条消息总是显示日期头
//        if (position == 0) {
//            return TYPE_DATE_HEADER;
//        }
//
//        // 计算实际的消息索引（减去日期头的数量）
//        int messageIndex = position - 1;
//
//        // 安全检查
//        if (messageIndex < 0 || messageIndex >= messageList.size()) {
//            return TYPE_MESSAGE;
//        }
//
//        // 如果是第一条消息，显示日期头
//        if (messageIndex == 0) {
//            return TYPE_DATE_HEADER;
//        }
//
//        // 检查当前消息和上一条消息的日期是否相同
//        SystemMessage currentMessage = messageList.get(messageIndex);
//        SystemMessage previousMessage = messageList.get(messageIndex - 1);
//
//        // 使用日期比较逻辑
//        if (!isSameDay(currentMessage.getDate(), previousMessage.getDate())) {
//            return TYPE_DATE_HEADER;
//        }

        return TYPE_MESSAGE;
    }

    // 辅助方法：判断两个日期是否是同一天
    private boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }

        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_system_message, parent, false);
        return new MessageViewHolder(view);
//        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
//
//        if (viewType == TYPE_DATE_HEADER) {
//            View view = inflater.inflate(R.layout.item_date_header, parent, false);
//            return new DateHeaderViewHolder(view);
//        } else {
//            View view = inflater.inflate(R.layout.item_system_message, parent, false);
//            return new MessageViewHolder(view);
//        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // 安全检查
        if (messageList == null || messageList.isEmpty()) {
            return;
        }

        int viewType = getItemViewType(position);

        if (viewType == TYPE_DATE_HEADER) {
            // 计算实际的消息索引
            int messageIndex = position;
            if (messageIndex >= messageList.size()) {
                messageIndex = messageList.size() - 1;
            }

            if (messageIndex >= 0 && messageIndex < messageList.size()) {
                SystemMessage message = messageList.get(messageIndex);
                ((DateHeaderViewHolder) holder).bind(message);
            }
        } else {
            // 计算实际的消息索引（减去日期头的数量）
            int messageIndex = position - countDateHeadersBeforePosition(position);

            if (messageIndex >= 0 && messageIndex < messageList.size()) {
                SystemMessage message = messageList.get(messageIndex);
                ((MessageViewHolder) holder).bind(message, messageIndex);
            }
        }
    }

    // 计算在指定位置之前的日期头数量
    private int countDateHeadersBeforePosition(int position) {
        int count = 0;
        for (int i = 0; i < position; i++) {
            if (getItemViewType(i) == TYPE_DATE_HEADER) {
                count++;
            }
        }
        return count;
    }

    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
//        if (messageList == null || messageList.isEmpty()) {
//            return 0;
//        }
//
//        // 基本数量：消息数量 + 第一个日期头
//        int count = messageList.size() + 1;
//
//        // 计算其他日期分隔符
//        for (int i = 1; i < messageList.size(); i++) {
//            SystemMessage current = messageList.get(i);
//            SystemMessage previous = messageList.get(i - 1);
//
//            if (!isSameDay(current.getDate(), previous.getDate())) {
//                count++;
//            }
//        }
//
//        return count;
    }

    // ViewHolder 类保持不变
    static class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateHeader;

        DateHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDateHeader = itemView.findViewById(R.id.tvDateHeader);
        }

        void bind(SystemMessage message) {
            if (message != null && message.getDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault());
                tvDateHeader.setText(sdf.format(message.getDate()));
            } else {
                tvDateHeader.setText("未知日期");
            }
        }
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        ImageView ivBanner;
        TextView tvTitle;
        TextView tvSubtitle;
        TextView tvDescription;
        TextView tvTag;
        Button btnAction;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            ivBanner = itemView.findViewById(R.id.ivBanner);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTag = itemView.findViewById(R.id.tvTag);
            btnAction = itemView.findViewById(R.id.btnAction);
        }
        private boolean shouldShowFullDate(int position) {
            if (position == 0) {
                return true;  // 第一条消息总是显示完整日期
            }

            // 检查是否和前一条消息同一天
            SystemMessage current = messageList.get(position);
            SystemMessage previous = messageList.get(position - 1);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
            String currentDate = dateFormat.format(current.getDate());
            String previousDate = dateFormat.format(previous.getDate());

            return !currentDate.equals(previousDate);
        }
        void bind(SystemMessage message, int position) {
            if (message == null) return;
            boolean showFullDate = shouldShowFullDate(position);

            SimpleDateFormat format;
            if (showFullDate) {
                // 显示完整日期+时间
                format = new SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault());
            } else {
                // 只显示时间
                format = new SimpleDateFormat("HH:mm", Locale.getDefault());
            }

            String dateText = format.format(message.getDate());
            tvDate.setText(dateText);

//            // 设置日期
//            if (message.getDate() != null) {
//                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
//                tvDate.setText(sdf.format(message.getDate()));
//            } else {
//                tvDate.setText("");
//            }

            // 设置横幅图片
            if (message.getBannerResId() != 0) {
                ivBanner.setImageResource(message.getBannerResId());
            }

            // 设置标题
            tvTitle.setText(message.getTitle() != null ? message.getTitle() : "");

            // 设置副标题
            tvSubtitle.setText(message.getSubtitle() != null ? message.getSubtitle() : "");

            // 设置描述
            tvDescription.setText(message.getDescription() != null ? message.getDescription() : "");

            // 设置标签
            tvTag.setText(message.getTag() != null ? message.getTag() : "");

            // 设置按钮文本
            btnAction.setText(message.getActionText() != null ? message.getActionText() : "查看详情");

            // 点击事件
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int adapterPosition = getAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        int messageIndex = adapterPosition - countDateHeadersInRange(0, adapterPosition);
                        if (messageIndex >= 0 && messageIndex < messageList.size()) {
                            listener.onMessageClick(messageIndex, messageList.get(messageIndex));
                        }
                    }
                }
            });

            btnAction.setOnClickListener(v -> {
                if (listener != null) {
                    int adapterPosition = getAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        int messageIndex = adapterPosition - countDateHeadersInRange(0, adapterPosition);
                        if (messageIndex >= 0 && messageIndex < messageList.size()) {
                            listener.onActionClick(messageIndex, messageList.get(messageIndex));
                        }
                    }
                }
            });
        }

        private int countDateHeadersInRange(int start, int end) {
            int count = 0;
            for (int i = start; i <= end; i++) {
                // 这里需要访问适配器的方法，简化处理
                count++;
            }
            return count;
        }
    }
}