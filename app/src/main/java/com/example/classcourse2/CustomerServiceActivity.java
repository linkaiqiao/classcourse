package com.example.classcourse2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CustomerServiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_service);

        // 初始化视图
        initViews();
    }

    private void initViews() {
        // 返回按钮
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // 客服电话点击
        LinearLayout itemPhone = findViewById(R.id.itemPhone);
        itemPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makePhoneCall("4001234567");
            }
        });

        // 客服邮箱点击
        LinearLayout itemEmail = findViewById(R.id.itemEmail);
        itemEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail("support@example.com");
            }
        });

        // 在线客服点击
//        LinearLayout itemOnline = findViewById(R.id.itemOnline);
//        itemOnline.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                openOnlineChat();
//            }
//        });

        // 常见问题点击
        findViewById(R.id.faq1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFAQDetail(1);
            }
        });

        findViewById(R.id.faq2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFAQDetail(2);
            }
        });
    }

    /**
     * 拨打电话
     */
    private void makePhoneCall(String phoneNumber) {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "无法拨打电话", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 发送邮件
     */
    private void sendEmail(String emailAddress) {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + emailAddress));
            intent.putExtra(Intent.EXTRA_SUBJECT, "课程表应用 - 用户咨询");
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "未找到邮件应用", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 打开在线聊天
     */
    private void openOnlineChat() {
        // 这里可以集成第三方在线客服系统，如环信、融云等
        Toast.makeText(this, "在线客服功能开发中", Toast.LENGTH_SHORT).show();

        // 或者打开网页版在线客服
        // openWebChat();
    }

    /**
     * 显示常见问题详情
     */
    private void showFAQDetail(int faqId) {
        String title = "";
        String content = "";

        switch (faqId) {
            case 1:
                title = "如何修改课表？";
                content = "1. 在课程表主界面，点击课程卡片可以查看课程详情\n" +
                        "2. 长按课程卡片可以进行删除操作\n" +
                        "3. 点击右上角的添加按钮可以添加新课程\n" +
                        "4. 在课程详情页面可以修改课程信息";
                break;
            case 2:
                title = "数据如何备份？";
                content = "1. 当前版本支持本地数据存储\n" +
                        "2. 请确保不要清除应用数据\n" +
                        "3. 未来版本将支持云端备份功能\n" +
                        "4. 建议定期检查应用更新";
                break;
        }

        new android.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(content)
                .setPositiveButton("确定", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}