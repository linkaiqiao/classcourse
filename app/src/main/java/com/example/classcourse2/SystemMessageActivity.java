package com.example.classcourse2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.classcourse2.MainActivity;
import com.example.classcourse2.ProfileActivity;
//import com.example.courseschedule.adapter.SystemMessageAdapter;
//import com.example.courseschedule.model.SystemMessage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SystemMessageActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvMessages;
    private LinearLayout llEmptyState;
    private TextView tvBadge;
    private EditText etSearch;

    private SystemMessageAdapter adapter;
    private List<SystemMessage> messageList = new ArrayList<>();
    private List<SystemMessage> filteredList = new ArrayList<>();

    private Handler handler = new Handler();

    // 模拟数据
    private void initMockData() {
        messageList.clear();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        try {
            // 第一条消息
            SystemMessage message1 = new SystemMessage();
            message1.setId(1);
            message1.setTitle("鸿蒙生态加速发展中");
            message1.setSubtitle("你想成为-鸿蒙开发者吗?-");
            message1.setDescription("鸿蒙开发及课程校园偏好调研问卷\n快来反馈你心仪的鸿蒙课程和内容吧!");
            message1.setActionText("前往了解");
            message1.setTag("活动通知");
            message1.setDate(sdf.parse("2025-10-21 12:13"));
            message1.setBannerResId(R.drawable.banner_harmony);
            message1.setRead(false);
            message1.setActionUrl("https://harmonyos.com");
            messageList.add(message1);

            // 第二条消息
            SystemMessage message2 = new SystemMessage();
            message2.setId(2);
            message2.setTitle("珍会玩！来'疯'一下瓜分超万元现金大奖");
            message2.setSubtitle("看视频GET同款糖，还能瓜分超万元奖金好礼~");
            message2.setDescription("参与活动即有机会赢取现金大奖，还有各种好礼等你拿！");
            message2.setActionText("立即参与");
            message2.setTag("活动通知");
            message2.setDate(sdf.parse("2025-10-13 16:37"));
            message2.setBannerResId(R.drawable.banner_candy);
            message2.setRead(true);
            message2.setActionUrl("https://activity.example.com");
            messageList.add(message2);

            // 第三条消息
            SystemMessage message3 = new SystemMessage();
            message3.setId(3);
            message3.setTitle("益力多校园活力大使招募中！");
            message3.setSubtitle("赢3000奖金 品牌出演机会");
            message3.setDescription("加入我们成为校园活力大使，展现你的才华，赢取丰厚奖励！");
            message3.setActionText("查看详情");
            message3.setTag("活动通知");
            message3.setDate(sdf.parse("2025-09-28 10:14"));
            message3.setBannerResId(R.drawable.banner_yakult);
            message3.setRead(true);
            message3.setActionUrl("https://recruit.example.com");
            messageList.add(message3);

            // 添加更多测试数据
            SystemMessage message4 = new SystemMessage();
            message4.setId(4);
            message4.setTitle("课程提醒：数据结构考试通知");
            message4.setSubtitle("考试时间：本周五 14:00-16:00");
            message4.setDescription("请携带学生证和准考证准时参加考试，考场：信息楼201");
            message4.setActionText("查看详情");
            message4.setTag("课程提醒");
            message4.setDate(sdf.parse("2025-10-21 09:00"));
            message4.setBannerResId(R.drawable.banner_harmony);
            message4.setRead(false);
            messageList.add(message4);

            SystemMessage message5 = new SystemMessage();
            message5.setId(5);
            message5.setTitle("系统维护通知");
            message5.setSubtitle("服务器将于今晚进行维护");
            message5.setDescription("为了提供更好的服务，系统将于今晚23:00-01:00进行维护，期间可能无法访问");
            message5.setActionText("知道了");
            message5.setTag("系统通知");
            message5.setDate(sdf.parse("2025-10-20 18:30"));
            message5.setBannerResId(R.drawable.banner_harmony);
            message5.setRead(true);
            messageList.add(message5);

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        // 初始化模拟数据
        initMockData();
        filteredList.addAll(messageList);

        // 初始化视图
        initViews();

        // 初始化RecyclerView
        initRecyclerView();

        // 更新未读数量
        updateUnreadCount();
    }

    private void initViews() {
        // 返回按钮
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        // 搜索框
        etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMessages(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 下拉刷新
        swipeRefresh = findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(() -> {
            // 模拟刷新数据
            handler.postDelayed(() -> {
                // 这里应该是从服务器获取新数据
                Toast.makeText(SystemMessageActivity.this, "已刷新", Toast.LENGTH_SHORT).show();
                swipeRefresh.setRefreshing(false);
                updateUnreadCount();
            }, 1000);
        });

        // RecyclerView
        rvMessages = findViewById(R.id.rvMessages);

        // 空状态视图
        llEmptyState = findViewById(R.id.llEmptyState);

        // 未读徽章
        tvBadge = findViewById(R.id.tvBadge);

        // 底部导航
        setupBottomNavigation();
    }

    private void initRecyclerView() {
        // 设置布局管理器
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvMessages.setLayoutManager(layoutManager);

        // 创建适配器
        adapter = new SystemMessageAdapter(filteredList);
        adapter.setOnItemClickListener(new SystemMessageAdapter.OnItemClickListener() {
            @Override
            public void onMessageClick(int position, SystemMessage message) {
                // 点击消息卡片
                openMessageDetail(message);
            }

            @Override
            public void onActionClick(int position, SystemMessage message) {
                // 点击操作按钮
                handleActionClick(message);
            }
        });

        rvMessages.setAdapter(adapter);

        // 根据数据显示/隐藏空状态
        updateEmptyState();
    }

    private void filterMessages(String query) {
        filteredList.clear();

        if (query.isEmpty()) {
            filteredList.addAll(messageList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (SystemMessage message : messageList) {
                if (message.getTitle().toLowerCase().contains(lowerQuery) ||
                        message.getSubtitle().toLowerCase().contains(lowerQuery) ||
                        message.getDescription().toLowerCase().contains(lowerQuery) ||
                        message.getTag().toLowerCase().contains(lowerQuery)) {
                    filteredList.add(message);
                }
            }
        }

        adapter.updateData(filteredList);
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredList.isEmpty()) {
            rvMessages.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvMessages.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);
        }
    }

    private void updateUnreadCount() {
        int unreadCount = 0;
        for (SystemMessage message : messageList) {
            if (!message.isRead()) {
                unreadCount++;
            }
        }

        if (unreadCount > 0) {
            tvBadge.setVisibility(View.VISIBLE);
            tvBadge.setText(String.valueOf(unreadCount));
        } else {
            tvBadge.setVisibility(View.GONE);
        }
    }

    private void openMessageDetail(SystemMessage message) {
        // 标记为已读
        message.setRead(true);
        updateUnreadCount();

        // 这里可以跳转到消息详情页面
        // Intent intent = new Intent(this, MessageDetailActivity.class);
        // intent.putExtra("message", message);
        // startActivity(intent);

        // 暂时用Toast提示
        Toast.makeText(this, "查看消息：" + message.getTitle(), Toast.LENGTH_SHORT).show();
    }

    private void handleActionClick(SystemMessage message) {
        Toast.makeText(this, "执行操作：" + message.getActionText(), Toast.LENGTH_SHORT).show();

        // 根据不同的actionText执行不同的操作
        switch (message.getActionText()) {
            case "前往了解":
                // 跳转到网页
                // openWebPage(message.getActionUrl());
                break;
            case "立即参与":
                // 打开活动页面
                // openActivityPage(message.getId());
                break;
            case "查看详情":
                // 查看详情
                openMessageDetail(message);
                break;
        }
    }

    private void setupBottomNavigation() {
        LinearLayout navSchedule = findViewById(R.id.nav_schedule);
        LinearLayout navMessage = findViewById(R.id.nav_message);
        LinearLayout navProfile = findViewById(R.id.nav_profile);

        // 设置当前选中的导航项
        setNavItemSelected(navSchedule, false);
        setNavItemSelected(navMessage, true);
        setNavItemSelected(navProfile, false);

        // 课程表点击事件
        navSchedule.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // 消息点击事件（当前页面）
        navMessage.setOnClickListener(v -> {
            // 已经是消息页面，不做操作
        });

        // 我的点击事件
        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void setNavItemSelected(LinearLayout navItem, boolean selected) {
        View iconView = navItem.getChildAt(0);
        TextView textView = (TextView) navItem.getChildAt(1);

        if (selected) {
            if (iconView instanceof ImageButton) {
                ((ImageButton) iconView).setColorFilter(getResources().getColor(R.color.primary_color));
            } else if (iconView instanceof ImageView) {
                ((ImageView) iconView).setColorFilter(getResources().getColor(R.color.primary_color));
            }
            textView.setTextColor(getResources().getColor(R.color.primary_color));
            textView.setTypeface(textView.getTypeface(), android.graphics.Typeface.BOLD);
        } else {
            if (iconView instanceof ImageButton) {
                ((ImageButton) iconView).setColorFilter(getResources().getColor(R.color.nav_unselected));
            } else if (iconView instanceof ImageView) {
                ((ImageView) iconView).setColorFilter(getResources().getColor(R.color.nav_unselected));
            }
            textView.setTextColor(getResources().getColor(R.color.nav_unselected));
            textView.setTypeface(textView.getTypeface(), android.graphics.Typeface.NORMAL);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}